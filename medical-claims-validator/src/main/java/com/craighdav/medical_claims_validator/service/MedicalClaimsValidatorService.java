package com.craighdav.medical_claims_validator.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.craighdav.medical_claims_validator.model.Charge;
import com.craighdav.medical_claims_validator.model.Claim;
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

	public ProcessedMedicalClaimsData validateMedicalClaims(RawMedicalClaimsData rawMedicalClaimsData) {

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
		Map<Long, Claim> claimMap = claimList.stream().collect(Collectors.toMap(Claim::getId, Function.identity()));

		Set<Long> invalidClaimIdSet = chargeList.stream()
				.filter(charge -> excludeClaimByCharge(charge, claimMap, patientMap)).map(charge -> charge.getClaimId())
				.collect(Collectors.toSet());

		Set<Long> invalidClaimIdSetByDuplicates = excludeClaimsByDuplicates(chargeList);

		invalidClaimIdSet.addAll(invalidClaimIdSetByDuplicates);

		Set<Long> validClaimIdSet = claimList.stream().map(claim -> claim.getId())
				.filter(claimId -> !(invalidClaimIdSet.contains(claimId))).collect(Collectors.toSet());

		ProcessedMedicalClaimsData processedMedicalClaimsData = new ProcessedMedicalClaimsData(
				Collections.unmodifiableSet(validClaimIdSet), Collections.unmodifiableSet(invalidClaimIdSet));

		return processedMedicalClaimsData;
	}

	/*
	 * Exclude charges where the data is invalid, based on business rules:
	 * 
	 * 1. procedureCode begins with "9" AND placeOfService != "office" 
	 * 2. procedureCode begins with "6" AND placeOfService == "office" 
	 * 3. procedureCode == "99129" AND patientAge > 18 
	 * 4. procedureCode == "99396" AND (patientAge < 18 OR patientAge > 39)
	 * 
	 */
	private boolean excludeClaimByCharge(Charge charge, Map<Long, Claim> claimMap, Map<Long, Patient> patientMap) {

		long procedureCode = charge.getProcedureCode();
		Claim claim = claimMap.get(charge.getClaimId());
		/**** Maybe replace null check with Optional ***/
		if (claim == null) {
			return true;
		}

		String placeOfService = claim.getPlaceOfService();
		int procedureLeftmostDigit = MathUtils.getLeftmostDigit(procedureCode);

		if (procedureLeftmostDigit == 9) {
			if (!placeOfService.equals("office")) {
				return true;
			}
		}

		if (procedureLeftmostDigit == 6) {
			if (placeOfService.equals("office")) {
				return true;
			}
		}

		Patient patient = patientMap.get(claim.getPatientId());
		/**** Maybe replace null check with Optional ***/
		if (patient == null) {
			return true;
		}

		LocalDate patientBirthDate = patient.getBirthDate();
		LocalDate today = LocalDate.now(clock);
		int patientAge = patientBirthDate.until(today).getYears();

		if (procedureCode == 99129L) {
			if (patientAge >= 18) {
				return true;
			}
		}

		if (procedureCode == 99396L) {
			if ((patientAge < 18) || (patientAge > 39)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Exclude a claim when it contains duplicate charges for any procedure code
	 */
	Set<Long> excludeClaimsByDuplicates(List<Charge> chargeList) {
		
		Map<Long, Map<Long, Long>> chargeByClaimByProcedureMap = chargeList.stream()
				.collect(Collectors.groupingBy(Charge::getClaimId,
					Collectors.groupingBy(Charge::getProcedureCode, Collectors.counting())));
		
		Set<Map.Entry<Long, Map<Long, Long>>> chargeByClaimByProcedureEntrySet 
												= chargeByClaimByProcedureMap.entrySet();

		Set<Long> invalidClaimIdSet =  chargeByClaimByProcedureEntrySet.stream()
			.filter(chargeByClaimByProcedureEntry -> {
				Map<Long, Long> chargeByProcedureMap = chargeByClaimByProcedureEntry.getValue();
				
				Collection<Long> chargeByProcedureValues = chargeByProcedureMap.values();
				
				return chargeByProcedureValues.stream()
						.anyMatch(chargeByProcedureValue -> chargeByProcedureValue > 1);
			})
			.map(chargeByClaimByProcedureEntry -> chargeByClaimByProcedureEntry.getKey())
			.collect(Collectors.toSet());
		
		return invalidClaimIdSet;
	}

}
