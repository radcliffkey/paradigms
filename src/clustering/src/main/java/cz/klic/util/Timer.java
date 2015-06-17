package cz.klic.util;

public class Timer {

	private static long START_TIME;
	
	public static void init() {
		START_TIME = System.nanoTime();
	}
	
	public static double elapsedSecs() {
		return (System.nanoTime() - START_TIME) / (double) 1000000000;
	}

}
