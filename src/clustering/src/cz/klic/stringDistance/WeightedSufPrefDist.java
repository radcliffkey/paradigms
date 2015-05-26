package cz.klic.stringDistance;

import cz.klic.util.MathUtil;
import cz.klic.matrix.SymmetricSparseMatrix;

/**
 * 
 * Edit distance with weights for char. substitution and decreasing cost of
 * operations in the end of the string
 * 
 */
public class WeightedSufPrefDist extends WeightedLevenDist {

	public WeightedSufPrefDist(SymmetricSparseMatrix distMatrix) {
		super(distMatrix);
	}
	
	@Override
	public double getDistance(String s1, String s2) {
		int l1 = s1.length();
		int l2 = s2.length();
		
		String longer = s1;
		String shorter = s2;
		
		if (l1 < l2) {
			longer = s2;
			shorter = s1;
			int tmp = l1;
			l1 = l2;
			l2 = tmp;
		}
		
		double [] prev = new double[l2 + 1];
		double [] curr = new double[l2 + 1];
		
		for (int i = 0; i < prev.length; ++i) {
			prev[i] = i;
		}
		
		//double lsqr = l1 * l1;
		
		for (int i = 0; i < l1; ++i) {
			curr[0] = i + 1;
			for (int j = 1; j < curr.length; ++j) {
				if (longer.charAt(i) == shorter.charAt(j - 1)) {
					curr[j] = prev[j - 1];
				} else {
					
					double cost = (1.0 - (double)(i) / l1); 
					double deletionDist =  prev[j] + cost;
					double substDist =  prev[j - 1] + cost * this.getDistMatrix().get(longer.charAt(i), shorter.charAt(j - 1));
					double insertDist =  curr[j - 1] + cost;

//					double lpow = Math.pow(2, l1); 
//					double deletionDist =  prev[j] + (1.0 - Math.pow(2, i) / lpow);
//					double substDist =  prev[j - 1] + (1.0 - Math.pow(2, i) / lpow);
//					double insertDist =  curr[j - 1] + (1.0 - Math.pow(2, i) / lpow);

//					double cost = 1.0 - (1.0 / (1 + Math.exp(-(i - l1 / 2.0))));
//					double deletionDist =  prev[j] + cost;
//					double substDist =  prev[j - 1] + cost;
//					double insertDist =  curr[j - 1] + cost;
					curr[j] = MathUtil.min(deletionDist, substDist, insertDist);
				}
			}
			
			//current becomes previous and previous is reused
			double [] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		
		double norm = 3;
//		for (int i = 2; i <= l1; ++i) {
//			norm += 1.0 - (double)i / l1;
//		}
		
		double result = prev[prev.length - 1] / norm;
		
		return result;
	}
}
