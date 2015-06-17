package cz.klic.stringDistance;

public class LcsRatioDistance implements StringDistanceMetric {

	@Override
	public double getDistance(String s1, String s2) {
		int l1 = s1.length();
		int l2 = s2.length();
		
		double [] prev = new double[l2 + 1];
		double [] curr = new double[l2 + 1];
		
		for (int i = 0; i < prev.length; ++i) {
			prev[i] = 0;
		}
		
		for (int i = 0; i < l1; ++i) {
			curr[0] = 0;
			for (int j = 1; j < curr.length; ++j) {
				if (s1.charAt(i) == s2.charAt(j - 1)) {
					curr[j] = prev[j - 1] + 1;
				} else {
					curr[j] = 0;
				}
			}
			
			//current becomes previous and previous is reused
			double [] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		
		double result = 1.0 - (2 * prev[prev.length - 1] / (l1 + l2));
		
		return result;
	}

}
