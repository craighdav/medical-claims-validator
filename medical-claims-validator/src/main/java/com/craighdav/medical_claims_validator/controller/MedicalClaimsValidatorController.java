package com.craighdav.medical_claims_validator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.craighdav.medical_claims_validator.model.ProcessedMedicalClaimsData;
import com.craighdav.medical_claims_validator.model.RawMedicalClaimsData;
import com.craighdav.medical_claims_validator.service.MedicalClaimsValidatorService;

import jakarta.validation.Valid;

@RestController
public class MedicalClaimsValidatorController {
	private final MedicalClaimsValidatorService medicalClaimsValidatorService;
	
	public MedicalClaimsValidatorController(
							MedicalClaimsValidatorService medicalClaimsValidatorService) {
		this.medicalClaimsValidatorService = medicalClaimsValidatorService;
	}
	
	@PostMapping("/validateClaims")
	public ResponseEntity<ProcessedMedicalClaimsData> validateMedicalClaims(
							@Valid @RequestBody RawMedicalClaimsData rawMedicalClaimsData) {
		
		ProcessedMedicalClaimsData processedMedicalClaimsData
					= medicalClaimsValidatorService.validateMedicalClaims(rawMedicalClaimsData);
		
		return ResponseEntity.ok(processedMedicalClaimsData);
	}
}
