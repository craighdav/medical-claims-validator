package com.craighdav.medical_claims_validator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.craighdav.medical_claims_validator.model.Charge;
import com.craighdav.medical_claims_validator.model.Claim;
import com.craighdav.medical_claims_validator.model.Patient;
import com.craighdav.medical_claims_validator.model.ProcessedMedicalClaimsData;
import com.craighdav.medical_claims_validator.model.RawMedicalClaimsData;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MedicalClaimsValidatorServiceTest {

	private final MedicalClaimsValidatorService medicalClaimsValidatorService;
	
	// Since medicalClaimsValidatorService is declared as final, this constructor replaces
	// the standard @BeforeAll method for initialization before running the tests.
	public MedicalClaimsValidatorServiceTest() {
		Instant fixedTestingInstant = Instant.parse("2025-07-05T12:00:00Z");
		ZoneId zoneId = ZoneId.of("UTC");
		Clock clock = Clock.fixed(fixedTestingInstant, zoneId);
		
		medicalClaimsValidatorService = new MedicalClaimsValidatorService(clock);
	}
		
	
	@Test
	@DisplayName("All claims with only valid data should return valid")
	public void validateMedicalClaims_AllGood_Valid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "home"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 90050L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 60009L, 587));
		
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);
		
		Set<Long> validClaimIdSetExpected = Set.of(5000L, 5001L, 5002L);
		
		// Claim 5002 has procedure code = "6XXXX" and place of service = "office", which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of();
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
							= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();
		
		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}	
	
	
	@Test
	@DisplayName("Claim with procedure code 6XXXX and 'office' should return invalid")
	public void validateMedicalClaims_ProcedureCode6WithOffice_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 90050L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 60009L, 587));
		
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);
		
		Set<Long> validClaimIdSetExpected = Set.of(5000L, 5001L);
		
		// Claim 5002 has procedure code = "6XXXX" and place of service = "office", which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5002L, 
						Set.of("Charge: 22003 has procedure code starting with 6 for 'office'."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
							= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}		
		
	
	@Test
	@DisplayName("Claim with procedure code 9XXXX and NOT 'office' should return invalid")
	public void validateMedicalClaims_ProcedureCode9WithNotOffice_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(2000, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "home"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99396L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 216));
		chargeList.add(new Charge(22003L, 5002L, 92345L, 287));
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);

		Set<Long> validClaimIdSetExpected = Set.of(5000L, 5001L);
		
		// Claim 5002 has procedure code = "9XXXX" and place of service = "home", which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5002L, 
					Set.of("Charge: 22003 has procedure code starting with 9 for NOT 'office'."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
							= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}
	
	@Test
	@DisplayName("Claim with procedure code 99129 and patient age > 18 should return invalid")
	public void validateMedicalClaims_ProcedureCode99129WithAgeOver18_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99129L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 73209L, 587));
		
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);
		
		Set<Long> validClaimIdSetExpected = Set.of(5001L, 5002L);
		
		// Claim 5000 has procedure code = 99129 and patient age > 18, which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5000L, 
					Set.of("Charge: 22000 has procedure code 99129 with patientAge: 65."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
							= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}
	
	@Test
	@DisplayName("Procedure code 99129 and patient age > 18 and NOT office should return invalid")
	public void validateMedicalClaims_ProcedureCode99129NotOfficeWithAgeOver18_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "home"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99129L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 73209L, 587));
		
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);
		
		Set<Long> validClaimIdSetExpected = Set.of(5001L, 5002L);
		
		// Claim 5000 has procedure code = 99129 and patient age > 18, which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5000L, 
					Set.of("Charge: 22000 has procedure code starting with 9 for NOT 'office'. "
							+ "Charge: 22000 has procedure code 99129 with patientAge: 65."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
							= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}	
	
	
	
	@Test
	@DisplayName("Claim with procedure code 99396 and patient age < 18 should return invalid")
	public void validateMedicalClaims_ProcedureCode99396WithAgeUnder18_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(2010, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
				
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "office"));
				
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99396L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 73209L, 587));
				
				
		RawMedicalClaimsData rawMedicalClaimsData 
							= new RawMedicalClaimsData(patientList, claimList, chargeList);
				
		Set<Long> validClaimIdSetExpected = Set.of(5001L, 5002L);
				
		// Claim 5000 has procedure code = 99396 and patient age < 18, which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5000L, 
					Set.of("Charge: 22000 has procedure code 99396 with patientAge: 15."));
				
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
							= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
				
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
						= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}	
	
	
	@Test
	@DisplayName("Claim with procedure code 99396 and patient age > 39 should return invalid")
	public void validateMedicalClaims_ProcedureCode99396WithAgeOver39_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
				
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "office"));
				
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99396L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 362));
		chargeList.add(new Charge(22003L, 5002L, 73209L, 587));
				
				
		RawMedicalClaimsData rawMedicalClaimsData 
							= new RawMedicalClaimsData(patientList, claimList, chargeList);
				
		Set<Long> validClaimIdSetExpected = Set.of(5001L, 5002L);
				
		// Claim 5000 has procedure code = 99396 and patient age > 39, which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5000L, 
					Set.of("Charge: 22000 has procedure code 99396 with patientAge: 65."));
				
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
							= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
				
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
						= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}	
	
	
	@Test
	@DisplayName("Should independently invalidate charges on same claim")
	public void validateMedicalClaims_MultiInvalidChargesPerClaim_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(2000, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "home"));
		claimList.add(new Claim(5003L, 1102L, LocalDate.of(2025, 5, 29), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99396L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 92345L, 216));
		chargeList.add(new Charge(22003L, 5002L, 92345L, 287));
		chargeList.add(new Charge(22004L, 5003L, 99396L, 940));
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);

		Set<Long> validClaimIdSetExpected = Set.of(5000L, 5001L, 5003L);
		
		// Claim 5002 has procedure code = "9XXXX" and place of service = "home", which is invalid
		// Duplicate charges for claim 5002 / procedure code 92345, which is invalid 
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5002L, 
					Set.of(
							"Charge: 22002 has procedure code starting with 9 for NOT 'office'.",
							"Charge: 22003 has procedure code starting with 9 for NOT 'office'.",
							"Claim has duplicate charges for at least one procedure."));
		
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
						= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
		
	}
	
	@Test
	@DisplayName("Should independently categorize each bad claim as invalid")
	public void validateMedicalClaims_MultiInvalidClaims_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(2010, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "home"));
		claimList.add(new Claim(5003L, 1102L, LocalDate.of(2025, 5, 29), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99396L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 216));
		chargeList.add(new Charge(22003L, 5002L, 92345L, 287));
		chargeList.add(new Charge(22004L, 5003L, 99396L, 940));
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);

		Set<Long> validClaimIdSetExpected = Set.of(5001L, 5003L);
		
		// Claim 5000 has procedure code = "99396" and patient age is 15, which is invalid
		// Claim 5002 has procedure code = "9XXXX" and place of service = "home", which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5000L, 
					Set.of("Charge: 22000 has procedure code 99396 with patientAge: 15."),
					5002L, 
					Set.of("Charge: 22003 has procedure code starting with 9 for NOT 'office'."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
						= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
		
	}
	
		
	// Test for duplicate procedure codes under a claim
	// (claim 104 has procedure code "99396" for two charges, which is invalid)
	@Test
	@DisplayName("Claim with duplicate charges for a single procedure should return invalid")
	public void validateMedicalClaims_DuplicateChargesPerProcedure_Invalid() {
		
		// Arrange
		List<Patient> patientList = new ArrayList<>();
		patientList.add(new Patient(1101L, "Bill", "Smith", LocalDate.of(1960, 2, 10)));
		patientList.add(new Patient(1102L, "Deepak", "Gupta", LocalDate.of(1989, 9, 24)));
		
		List<Claim> claimList = new ArrayList<>();
		claimList.add(new Claim(5000L, 1101L, LocalDate.of(2025, 5, 16), "office"));
		claimList.add(new Claim(5001L, 1101L, LocalDate.of(2025, 6, 12), "office"));
		claimList.add(new Claim(5002L, 1102L, LocalDate.of(2025, 5, 23), "home"));
		claimList.add(new Claim(5003L, 1102L, LocalDate.of(2025, 5, 29), "office"));
		
		List<Charge> chargeList = new ArrayList<>();
		chargeList.add(new Charge(22000L, 5000L, 99124L, 470));
		chargeList.add(new Charge(22001L, 5001L, 80640L, 655));
		chargeList.add(new Charge(22002L, 5002L, 50035L, 238));
		chargeList.add(new Charge(22003L, 5002L, 60009L, 304));
		chargeList.add(new Charge(22003L, 5003L, 70015L, 824));
		chargeList.add(new Charge(22003L, 5003L, 99396L, 718));
		chargeList.add(new Charge(22003L, 5003L, 99396L, 967));
		
		
		RawMedicalClaimsData rawMedicalClaimsData 
					= new RawMedicalClaimsData(patientList, claimList, chargeList);
		
		Set<Long> validClaimIdSetExpected = Set.of(5000L, 5001L, 5002L);
		
		// Duplicate charges for claim 5003 / procedure code 99396, which is invalid
		Map<Long, Set<String>> invalidClaimWithIssuesMapExpected = Map.of(5003L, 
					Set.of("Claim has duplicate charges for at least one procedure."));
		
		// Act
		ProcessedMedicalClaimsData processedMedicalClaimsData 
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		Set<Long> validClaimIdSet = processedMedicalClaimsData.getValidClaimIdSet();
		Map<Long, Set<String>> invalidClaimWithIssuesMap 
						= processedMedicalClaimsData.getInvalidClaimWithIssuesMap();

		// Assert
		assertEquals(validClaimIdSetExpected, validClaimIdSet,
							"Expected set of valid Claim Ids does not match returned set.");
		assertEquals(invalidClaimWithIssuesMapExpected, invalidClaimWithIssuesMap,
							"Expected set of invalid Claim Ids does not match returned set.");
	}
}
