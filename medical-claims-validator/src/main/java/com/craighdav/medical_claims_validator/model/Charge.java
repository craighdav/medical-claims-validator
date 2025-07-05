package com.craighdav.medical_claims_validator.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

public class Charge {
		@Positive(message = "Charge: id must be a valid positive integer")
		private final long id;
		
		@Positive(message = "Charge: claim id must be a valid positive integer")
		private final long claimId;

		@Positive(message = "Charge: procedure code must be a valid positive integer")
		private final long procedureCode;
		
		@Digits(integer = 10, fraction = 0, message = "Charge: amount must be a valid integer")
		private final int amount;
		
		@JsonCreator
		public Charge(@JsonProperty long id, 
						@JsonProperty long claimId, 
						@JsonProperty long procedureCode, 
						@JsonProperty int amount) {
			this.id = id;
			this.claimId = claimId;
			this.procedureCode = procedureCode;
			this.amount = amount;
		}

		public long getId() {
			return id;
		}

		public long getClaimId() {
			return claimId;
		}

		public long getProcedureCode() {
			return procedureCode;
		}

		public int getAmount() {
			return amount;
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			
			if (! (otherObject instanceof Charge)) {
				return false;
			}
			
			Charge otherCharge = (Charge) otherObject;
			
			return this.id == otherCharge.id;
		}
		
		@Override
		public int hashCode() {
			return Long.hashCode(id);
		}
		
		@Override
		public String toString() {
			return "Charge { " +
					"\"id\": " + id +
					", \"claimId\": " + claimId +
					", \"procedureCode\": " + procedureCode +
					", \"amount\": " + amount +
					"}";
		}
		
}
		
