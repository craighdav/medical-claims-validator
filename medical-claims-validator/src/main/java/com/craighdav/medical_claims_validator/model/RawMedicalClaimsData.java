package com.craighdav.medical_claims_validator.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;

public class RawMedicalClaimsData {
	@NotNull(message = "Patients must not be null")
	private final List<Patient> patientList;
	
	@NotNull(message = "Claims must not be null")
	private final List<Claim> claimList;
	
	@NotNull(message = "Charges must not be null")
	private final List<Charge> chargeList;

	/**
	 * RawMedicalClaimsData represents the data passed to the RESTful controller via @RequestBody.
	 * 
	 * Class RawMedicalClaimsData stores the data unmarshaled by Jackson via the controller
	 * method's @RequestBody binding. Since instances of this class are not created anywhere 
	 * within the application code itself, the constructor assigns the list references to
	 * its instance fields rather than making deep copies.
	 * 
	 * Although not strictly required, each claim in claimsList should reference a patient
	 * in patientList (via patientId), while each charge in chargeList should reference
	 * a claim in claimList (via claimId).
	 * 
	 * @param patientList List of patients defined as "patients" in JSON
	 * @param claimList List of claims defined as "claims" in JSON
	 * @param chargeList List of charges defined as "charges" in JSON
	 */
	@JsonCreator
	public RawMedicalClaimsData(@JsonProperty("patients") List<Patient>patientList,
								@JsonProperty("claims") List<Claim> claimList,
								@JsonProperty("charges") List<Charge> chargeList) {
		this.patientList = patientList;
		this.claimList = claimList;
		this.chargeList = chargeList;
	}
	
	/**
	 * Method getPatientList returns a fresh, immutable copy of patientList.
	 * 
	 * @return The list of patients as List<Patient>
	 */
	public List<Patient> getPatientList() {
		return List.copyOf(patientList);
	}
	
	/**
	 * Method getClaimList returns a fresh, immutable copy of claimList.
	 * 
	 * @return The list of claims as List<Claim>
	 */
	public List<Claim> getClaimList() {
		return List.copyOf(claimList);
	}
	
	/**
	 * Method getChargeList returns a fresh, immutable copy of chargeList.
	 * 
	 * @return The list of charges as List<Charge>
	 */
	public List<Charge> getChargeList() {
		return List.copyOf(chargeList);
	}
}
