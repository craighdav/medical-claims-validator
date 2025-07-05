package com.craighdav.medical_claims_validator.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class Claim {
		@Positive(message = "Claim: id must be a valid positive integer")
		private final long id;
		
		@Positive(message = "Claim: patient id must be a valid positive integer")
		private final long patientId;
		
		@NotNull(message = "Claim: service date must not be null")
		@Past(message = "Claim: service date must be in the past")
		private final LocalDate serviceDate;
		
		@NotNull(message = "Claim: place of service must not be null")
		private final String placeOfService; 
		
		@JsonCreator
		public Claim (@JsonProperty long id, 
						@JsonProperty long patientId, 
						@JsonProperty LocalDate serviceDate, 
						@JsonProperty String placeOfService) {
			this.id = id;
			this.patientId = patientId;
			this.serviceDate = serviceDate;
			this.placeOfService = placeOfService;
		}
		
		public long getId() {
			return id; 
		}
		
		public long getPatientId() {
			return patientId;
		}
		
		public LocalDate getServiceDate() {
			return serviceDate;
		}
		
		public String getPlaceOfService() {
			return placeOfService; 
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			
			if (! (otherObject instanceof Claim)) {
				return false;
			}
			
			Claim otherClaim = (Claim) otherObject;
			
			return this.id == otherClaim.id;
		}
		
		@Override
		public int hashCode() {
			return Long.hashCode(id);
		}
		
		@Override
		public String toString() {
			return "Claim { " +
					"\"id\": " + id +
					", \"patientId\": " + patientId +
					", \"serviceDate\": \"" + serviceDate +
					"\", \"placeOfService\": \"" + placeOfService +
					"\"}";
		}
}
