package cz.klic.test;

import cz.klic.functional.FuncUtil;
import cz.klic.util.Timer;

public class TestMin {

	public static double min2(Double ... numbers) {
	    return FuncUtil.reduce(numbers, Math::min);
	}
	
	public static double min2(double ... numbers) {
		double min = FuncUtil.reduce(numbers, Math::min);
		return min;
	}

	public static double min(Double ... numbers) {
		double min = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < min) {
				min = numbers[i];
			}
		}
		return min;
	}
	
	public static double min(double ... numbers) {
		double min = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < min) {
				min = numbers[i];
			}
		}
		return min;
	}
	
	public static void main(String[] args) {
		int size = 10000000;
		
		Double[] data = new Double[size];
		for (int i = 0; i < data.length; i++) {
			data[i] = Math.random() + 0.1;
		}
		
		double[] data2 = new double[size];
		for (int i = 0; i < data.length; i++) {
			data2[i] = Math.random() + 0.1;
		}
		
		Timer.init();
		double min = min(data);
		System.out.printf("Min: %g, in %f seconds\n", min, Timer.elapsedSecs());
		Timer.init();
		min = min2(data);
		System.out.printf("Min: %g, in %f seconds\n", min, Timer.elapsedSecs());
		Timer.init();
		min = min(data2);
		System.out.printf("Min: %g, in %f seconds\n", min, Timer.elapsedSecs());
		Timer.init();
		min = min2(data2);
		System.out.printf("Min: %g, in %f seconds\n", min, Timer.elapsedSecs());
	}

}
