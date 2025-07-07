package com.craighdav.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MathUtilsTest {

	@Test
	@DisplayName("Input of 0 should result in 0")
	public void getLeftmostDigit_Zero_Zero() {
		
		// Arrange
		long longValue = 0L;
		int leftmostDigitExpected = 0;
		
		// Act
		int leftmostDigit = MathUtils.getLeftmostDigit(longValue);
		
		// Assert
		assertEquals(leftmostDigitExpected, leftmostDigit);
	}
	
	@Test
	@DisplayName("Input of 10 should result in 1")
	public void getLeftmostDigit_Ten_Ten() {
		
		// Arrange
		long longValue = 10L;
		int leftmostDigitExpected = 1;
		
		// Act
		int leftmostDigit = MathUtils.getLeftmostDigit(longValue);
		
		// Assert
		assertEquals(leftmostDigitExpected, leftmostDigit);
	}
	
	@Test
	@DisplayName("Input of LONG.MAX_VALUE (9,223,372,036,854,775,807) should result in 9")
	public void getLeftmostDigit_LongMaxValue_Nine() {
		
		// Arrange
		long longValue = Long.MAX_VALUE;
		int leftmostDigitExpected = 9;
		
		// Act
		int leftmostDigit = MathUtils.getLeftmostDigit(longValue);
		
		// Assert
		assertEquals(leftmostDigitExpected, leftmostDigit);
	}
	
	@Test
	@DisplayName("Input of LONG.MIN_VALUE + 1 (-9,223,372,036,854,775,807) should result in 9")
	public void getLeftmostDigit_LongMinValuePlusOne_Nine() {
		
		// Arrange
		// There is no corresponding positive value for Long.MIN_VALUE.
		// Therefore, the minimum negative value that can be converted to positive with Math.abs()
		// is Long.MIN_VALUE + 1
		long longValue = Long.MIN_VALUE + 1;
		int leftmostDigitExpected = 9;
		
		// Act
		int leftmostDigit = MathUtils.getLeftmostDigit(longValue);
		
		// Assert
		assertEquals(leftmostDigitExpected, leftmostDigit);
	}
}
