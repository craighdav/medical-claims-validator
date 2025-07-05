package com.craighdav.medical_claims_validator.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicalClaimsValidatorConfig {

	@Bean
	public Clock clock() {
		Clock clock = Clock.systemDefaultZone();
		
		return clock;
	}
}
