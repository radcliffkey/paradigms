package cz.klic.test;

import cz.klic.stringDistance.LcsRatioDistance;
import cz.klic.stringDistance.LevenshteinMetric;
import cz.klic.stringDistance.StringDistanceMetric;
import cz.klic.stringDistance.SufPrefMetric;

public class TestStringMetrics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s1 = "důsledek";
		String s2 = "důsledek";
		
		StringDistanceMetric metric = new LevenshteinMetric();		
		double dist = metric.getDistance(s1, s2);
		System.out.println(dist);
		
		metric = new SufPrefMetric();		
		dist = metric.getDistance(s1, s2);
		System.out.println(dist);
	}

}
