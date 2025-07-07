package com.craighdav.medical_claims_validator.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessedMedicalClaimsData {
	private final Set<Long> validClaimIdSet;
	private final Map<Long, Set<String>> invalidClaimWithIssuesMap;
	
	public ProcessedMedicalClaimsData(Set<Long> validClaimIdSet, 
										Map<Long, Set<String>> invalidClaimWithIssuesMap ) {
		this.validClaimIdSet = validClaimIdSet;
		this.invalidClaimWithIssuesMap = invalidClaimWithIssuesMap;
	}
	
	@JsonProperty("validClaimIds")
	public Set<Long> getValidClaimIdSet() {
		return Collections.unmodifiableSet(validClaimIdSet);
	}
	
	@JsonProperty("invalidClaimIds")
	public Map<Long, Set<String>> getInvalidClaimWithIssuesMap() {
		return Collections.unmodifiableMap(invalidClaimWithIssuesMap);
	}
}
