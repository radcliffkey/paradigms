package cz.klic.stringDistance;

import cz.klic.util.MathUtil;

public class LevenshteinMetric implements StringDistanceMetric {

	@Override
	public double getDistance(String s1, String s2) {
		int l1 = s1.length();
		int l2 = s2.length();
		
		double [] prev = new double[l2 + 1];
		double [] curr = new double[l2 + 1];
		
		for (int i = 0; i < prev.length; ++i) {
			prev[i] = i;
		}
		
		for (int i = 0; i < l1; ++i) {
			curr[0] = i + 1;
			for (int j = 1; j < curr.length; ++j) {
				if (s1.charAt(i) == s2.charAt(j - 1)) {
					curr[j] = prev[j - 1];
				} else {
					double deletionDist =  prev[j] + 1;
					double substDist =  prev[j - 1] + 1;
					double insertDist =  curr[j - 1] + 1;
					curr[j] = MathUtil.min(deletionDist, substDist, insertDist);
				}
			}
			
			//current becomes previous and previous is reused
			double [] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		
		double result = prev[prev.length - 1];
		
		return result;
	}

}
