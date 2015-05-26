/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

public class ArrayUtils {
	
	/** 
	 * You can't create an ArrayUtils
	 */
	private ArrayUtils() { }
	
	// A binary search for a number just greater than 0.5
	public static Integer binarySearchForFirstNumberGreaterThanThreshold(Double[] sortedNumbers, double threshold) {
		int loIndex = 0;
		int hiIndex = sortedNumbers.length - 1;
		
		// Some edge cases:
		
		// If no item in the array is larger than threshold then return an error value
		if (sortedNumbers[hiIndex] <= threshold) {
			return null;
		}
		// If the first item in the array is larger than threshold then return the index 0 (i.e. lowIndex)
		if (sortedNumbers[loIndex] > threshold) {
			return loIndex;
		}
		
		// Otherwise do a binary search
		while(true) {
			// When loIndex and hiIndex are right next to each other, hiIndex will be pointing to
			// the first item in the array that is larger than threshold. Return this index.
			if (hiIndex-loIndex == 1) {
				return hiIndex;
			}
			int midIndex = (loIndex + hiIndex) / 2;  // throws away any remainder
			if (sortedNumbers[midIndex] > threshold) {
				hiIndex = midIndex;
			} else {
				loIndex = midIndex;
			}
		}
	}
	
	// A binary search for a number just greater than 0.5
	public static <T extends Comparable<T>> Integer binarySearchForFirstNumberGreaterThanThreshold(T[] sortedItems, T threshold) {
		int loIndex = 0;
		int hiIndex = sortedItems.length - 1;
		
		// Some edge cases:
		
		// If no item in the array is larger than threshold then return an error value
		if (sortedItems[hiIndex].compareTo(threshold) <= 0) {
			return null;
		}
		// If the first item in the array is larger than threshold then return the index 0 (i.e. lowIndex)
		if (sortedItems[loIndex].compareTo(threshold) > 0) {
			return loIndex;
		}
		
		// Otherwise do a binary search
		while(true) {
			// When loIndex and hiIndex are right next to each other, hiIndex will be pointing to
			// the first item in the array that is larger than threshold. Return this index.
			if (hiIndex-loIndex == 1) {
				return hiIndex;
			}
			int midIndex = (loIndex + hiIndex) / 2;  // throws away any remainder
			if (sortedItems[midIndex].compareTo(threshold) > 0) {
				hiIndex = midIndex;
			} else {
				loIndex = midIndex;
			}
		}
	}
}
