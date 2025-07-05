package com.craighdav.util;

public class MathUtils {

		public static int getLeftmostDigit(long longValue) {
			
			if (longValue > 1_000_000_000_000L) {
				longValue /= 1_000_000_000_000L;
			}
			
			if (longValue > 1_000_000L) {
				longValue /= 1_000_000L;
			}
			
			if (longValue > 1_000L) {
				longValue /= 1_000L;
			}
			
			while (longValue > 10) {
				longValue /= 10;
			}
			
			return (int) longValue;
		}
}
