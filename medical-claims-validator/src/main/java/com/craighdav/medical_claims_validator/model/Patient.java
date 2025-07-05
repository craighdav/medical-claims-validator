package com.craighdav.medical_claims_validator.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public class Patient{
		@Positive(message = "Patient: id must be a valid positive integer")
		private final long id;
		
		@NotNull(message = "Patient: first name must not be null")
		private final String firstName;
		
		@NotNull(message = "Patient: last name must not be null")
		private final String lastName;
		
		@NotNull(message = "Patient: birth date must not be null")
		@Past(message = "Patient: birth date must be in the past")
		private final LocalDate birthDate;
		
		@JsonCreator
		public Patient(@JsonProperty long id, 
						@JsonProperty String firstName, 
						@JsonProperty String lastName, 
						@JsonProperty LocalDate birthDate) {
			this.id = id;
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthDate = birthDate;
		}
		
		public long getId() {
			return id;
		}
		
		public String getFirstName() {
			return firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public LocalDate getBirthDate() {
			return birthDate; 
		}
		
		@Override
		public boolean equals(Object otherObject) {
			if (this == otherObject) {
				return true;
			}
			
			if (! (otherObject instanceof Patient)) {
				return false;
			}
			
			Patient otherPatient = (Patient) otherObject;
			
			return this.id == otherPatient.id;
		}
		
		@Override
		public int hashCode() {
			return Long.hashCode(id);
		}
		
		@Override
		public String toString() {
			return "Patient { " +
					"\"id\": " + id +
					", \"firstName\": \"" + firstName +
					"\", \"lastName\": \"" + lastName +
					"\", \"birthDate\": \"" + birthDate +
					"\"}";
		}
}
