package com.craighdav.medical_claims_validator.model;


public class InvalidClaim {

	private final long claimId;
	private final String issue;
	
	public InvalidClaim(long claimId, String issue) {
		this.claimId = claimId;
		this.issue = issue;
	}
	
	public long getClaimId() {
		return claimId;
	}

	public String getIssue() {
		return issue;
	}
	
}
