package com.craighdav.medical_claims_validator.model;

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessedMedicalClaimsData {
	private final Set<Long> validClaimIdSet;
	private final Set<Long> invalidClaimIdSet;
	
	public ProcessedMedicalClaimsData(Set<Long> validClaimIdSet, Set<Long> invalidClaimIdSet) {
		this.validClaimIdSet = validClaimIdSet;
		this.invalidClaimIdSet = invalidClaimIdSet;
	}
	
	@JsonProperty("validClaimIds")
	public Set<Long> getValidClaimIdSet() {
		return Collections.unmodifiableSet(validClaimIdSet);
	}
	
	@JsonProperty("invalidClaimIds")
	public Set<Long> getInvalidClaimIdSet() {
		return Collections.unmodifiableSet(invalidClaimIdSet);
	}
}
