package cz.klic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MathUtil {

	public static double min(double ... numbers) {
		double min = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < min) {
				min = numbers[i];
			}
		}
		return min;
	}
	
	public static double min(int ... numbers) {
		int min = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < min) {
				min = numbers[i];
			}
		}
		return min;
	}
	
	public static double max(double ... numbers) {
		double max = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > max) {
				max = numbers[i];
			}
		}
		return max;
	}
	
	public static int max(int ... numbers) {
		int max = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > max) {
				max = numbers[i];
			}
		}
		return max;
	}
	
	/**
	 * Sample without replacement
	 * 
	 * @param <T>
	 * @param collection
	 * @param size
	 *            Size of the sample. must be lower than collection.size()
	 * @return
	 */
	public static <T> List<T> sample(Collection<T> collection, int size) {
		ArrayList<T> result = new ArrayList<T>(size);
		double chance = (double) size / collection.size();

		int drawn = 0;

		for (T member : collection) {
			if (drawn == size) {
				break;
			}
			if (Math.random() < chance) {
				result.add(member);
				++drawn;
			}
		}

		return result;
	}

}
