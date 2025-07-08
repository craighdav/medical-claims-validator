package com.craighdav.medical_claims_validator.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.craighdav.medical_claims_validator.model.Charge;
import com.craighdav.medical_claims_validator.model.Claim;
import com.craighdav.medical_claims_validator.model.InvalidClaim;
import com.craighdav.medical_claims_validator.model.Patient;
import com.craighdav.medical_claims_validator.model.ProcessedMedicalClaimsData;
import com.craighdav.medical_claims_validator.model.RawMedicalClaimsData;
import com.craighdav.util.MathUtils;

@Service
public class MedicalClaimsValidatorService {

	private final Clock clock;

	public MedicalClaimsValidatorService(Clock clock) {
		this.clock = clock;
	}

	public ProcessedMedicalClaimsData validateMedicalClaims(
													RawMedicalClaimsData rawMedicalClaimsData) {

		/*
		 * Do NOT include claim if: 1. procedureCode begins with "9" AND placeOfService
		 * != "office" 2. procedureCode begins with "6" AND placeOfService == "office"
		 * 3. procedureCode == "99129" AND patientAge > 18 4. procedureCode == "99396"
		 * AND (patientAge < 18 OR patientAge > 39) 5. Count(procedureCode) > 1 for any
		 * claim / claimID
		 * 
		 */

		List<Patient> patientList = rawMedicalClaimsData.getPatientList();
		List<Claim> claimList = rawMedicalClaimsData.getClaimList();
		List<Charge> chargeList = rawMedicalClaimsData.getChargeList();

		// Populate a map to retrieve a patient's age
		Map<Long, Patient> patientMap = patientList.stream()
				.collect(Collectors.toMap(Patient::getId, Function.identity()));

		// Populate a map to retrieve a claim's place of service and patient ID
		Map<Long, Claim> claimMap = claimList.stream()
						.collect(Collectors.toMap(Claim::getId, Function.identity()));

		/*
		 * Set<Long> invalidClaimIdSet = chargeList.stream() .filter(charge ->
		 * excludeClaimByCharge(charge, claimMap, patientMap)) .map(charge ->
		 * charge.getClaimId()) .collect(Collectors.toSet());
		 */
		
		Set<InvalidClaim> invalidClaimSet = chargeList.stream()
				.map(charge -> invalidateClaimByCharge(charge, claimMap, patientMap))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<InvalidClaim> invalidClaimSetByDuplicates = excludeClaimsByDuplicates(chargeList);
		
		invalidClaimSet.addAll(invalidClaimSetByDuplicates);
		
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
				= invalidClaimSet.stream()
					.collect(Collectors.groupingBy(InvalidClaim::getClaimId, 
								Collectors.mapping(InvalidClaim::getIssue, Collectors.toSet())));

		
		Set<Long> invalidClaimIdSet = invalidClaimWithIssuesMap.keySet();
		Set<Long> validClaimIdSet = claimList.stream().map(claim -> claim.getId())
				.filter(claimId -> !(invalidClaimIdSet.contains(claimId))).collect(Collectors.toSet());

		ProcessedMedicalClaimsData processedMedicalClaimsData = new ProcessedMedicalClaimsData(
				Collections.unmodifiableSet(validClaimIdSet), 
				Collections.unmodifiableMap(invalidClaimWithIssuesMap));

		return processedMedicalClaimsData;
	}

	/*
	 * Invalidate claim associated with charge based on the following business rules:
	 * 
	 * 1. procedureCode begins with "9" AND placeOfService != "office" 
	 * 2. procedureCode begins with "6" AND placeOfService == "office" 
	 * 3. procedureCode == "99129" AND patientAge > 18 
	 * 4. procedureCode == "99396" AND (patientAge < 18 OR patientAge > 39)
	 * 
	 */
	private InvalidClaim invalidateClaimByCharge(Charge charge, Map<Long, Claim> claimMap, Map<Long, Patient> patientMap) {

		long chargeId = charge.getId();
		long procedureCode = charge.getProcedureCode();
		long claimId = charge.getClaimId();
		
		Claim claim = claimMap.get(claimId);
		if (claim == null) {
			return new InvalidClaim(claimId, "No matching claim found with claimId: " + claimId
									+ " for charge: " + chargeId);
		}

		String placeOfService = claim.getPlaceOfService();
		int procedureLeftmostDigit = MathUtils.getLeftmostDigit(procedureCode);

		if (procedureLeftmostDigit == 9) {
			String claimIssue = "";
			boolean isValid = true;
			
			if (!placeOfService.equals("office")) {
				claimIssue = "Charge: " + chargeId 
								+ " has procedure code starting with 9 for NOT 'office'. ";
				isValid = false;
			}
			
			Patient patient = patientMap.get(claim.getPatientId());
			
			// If no patient is found matching the claim, return InvalidClaim immediately
			// since no more processing can be done in this logic branch.
			// NOTE: This validation check is only performed on when the claim matches
			// a charge with procedureCode beginning with 9 since the patient age
			// is needed only to validate specific procedureCodes beginning with 9.
			if (patient == null) {
				claimIssue += "No matching patient found for claim: " + claimId + ". ";
				return new InvalidClaim(claimId, claimIssue.trim());
			}
			
			LocalDate patientBirthDate = patient.getBirthDate();
			LocalDate today = LocalDate.now(clock);
			int patientAge = patientBirthDate.until(today).getYears();

			if (procedureCode == 99129L) {
				if (patientAge >= 18) {
					claimIssue += "Charge: " + chargeId 
								+ " has procedure code 99129 with patientAge: " + patientAge + ". ";
					isValid = false;
				}
			}

			if (procedureCode == 99396L) {
				if ((patientAge < 18) || (patientAge > 39)) {
					claimIssue += "Charge: " + chargeId 
							+ " has procedure code 99396 with patientAge: " + patientAge + ". ";
					isValid = false;
				}
			}
			
			if (!isValid) {
				return new InvalidClaim(claimId, claimIssue.trim());
			}
		}

		if (procedureLeftmostDigit == 6) {
			if (placeOfService.equals("office")) {
				return new InvalidClaim(claimId, "Charge: " + chargeId 
						+ " has procedure code starting with 6 for 'office'.");
			}
		}

		return null;
	}

	/*
	 * Exclude a claim when it contains duplicate charges for any procedure code
	 */
	private Set<InvalidClaim> excludeClaimsByDuplicates(List<Charge> chargeList) {
		
		Map<Long, Map<Long, Long>> chargeByClaimByProcedureMap = chargeList.stream()
				.collect(Collectors.groupingBy(Charge::getClaimId,
					Collectors.groupingBy(Charge::getProcedureCode, Collectors.counting())));
		
		Set<Map.Entry<Long, Map<Long, Long>>> chargeByClaimByProcedureEntrySet 
												= chargeByClaimByProcedureMap.entrySet();

		Set<InvalidClaim> invalidClaimSet =  chargeByClaimByProcedureEntrySet.stream()
			.filter(chargeByClaimByProcedureEntry -> {
				Map<Long, Long> chargeByProcedureMap = chargeByClaimByProcedureEntry.getValue();
				
				Collection<Long> chargeByProcedureValues = chargeByProcedureMap.values();
				
				return chargeByProcedureValues.stream()
						.anyMatch(chargeByProcedureValue -> chargeByProcedureValue > 1);
			})
			.map(chargeByClaimByProcedureEntry -> 
					new InvalidClaim(chargeByClaimByProcedureEntry.getKey(), 
										"Claim has duplicate charges for at least one procedure."))
			.collect(Collectors.toSet());
		
		return invalidClaimSet;
	}

}
