package com.craighdav.util;

/**
 * This class is purposed to provide static utility methods for numeric operations.
 * 
 * Class MathUtils is designed to be stateless, providing only static methods to assist
 * with numeric computations.
 */
public class MathUtils {

		/**
		 * This method extracts the leftmost digit of a long value.
		 * 
		 * Method  getLeftmostDigit returns the leftmost digit of any long value between
		 * Long.MIN_VALUE + 1 and Long.MAX_VALUE. (Since the method converts negative input
		 * values to positive values using Math.abs() and there is no positive value within
		 * the range of long values corresponding to Long.MIN_VALUE due to the storage mechanism
		 * of long values, the minimum negative value that can be evaluated is Long.MIN_VALUE + 1.)
		 * 
		 * @param longValue The long value to be evaluated
		 * @return The leftmost digit of longValue
		 */
		public static int getLeftmostDigit(long longValue) {
			
			longValue = Math.abs(longValue);
			
			if (longValue > 1_000_000_000_000L) {
				longValue /= 1_000_000_000_000L;
			}
			
			if (longValue > 1_000_000L) {
				longValue /= 1_000_000L;
			}
			
			if (longValue > 1_000L) {
				longValue /= 1_000L;
			}
			
			while (longValue >= 10) {
				longValue /= 10;
			}
			
			return (int) longValue;
		}
}
