package cz.klic.stringDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.klic.util.MathUtil;

import cz.klic.functional.FuncUtil;
import cz.klic.functional.condition.GraterThanCondition;
import cz.klic.functional.condition.LessThanCondition;
import cz.klic.matrix.SymmetricSparseMatrix;
import cz.klic.segmentation.StringSegmentation;

public class MorphBoundEditDistance implements StringDistanceMetric {

	private SymmetricSparseMatrix distMatrix;
	private StringSegmentation segmentation;
	
	public MorphBoundEditDistance(SymmetricSparseMatrix distMatrix,
			StringSegmentation segmentation) {
		super();
		this.distMatrix = distMatrix;
		this.segmentation = segmentation;
	}
	
	protected SymmetricSparseMatrix getDistMatrix() {
		return distMatrix;
	}

	protected void setDistMatrix(SymmetricSparseMatrix distMatrix) {
		this.distMatrix = distMatrix;
	}

	protected StringSegmentation getSegmentation() {
		return segmentation;
	}

	protected void setSegmentation(StringSegmentation segmentation) {
		this.segmentation = segmentation;
	}

	protected List<Double> getImportantBoundaries(int[] cumLens1) {
		ArrayList<Double> bounds = new ArrayList<Double>();
		int boundCnt = cumLens1.length;
		int candLen = Math.min(3, cumLens1.length);
		
		int[] cands = Arrays.copyOfRange(cumLens1, boundCnt - candLen, boundCnt);
		
		for (int i = 0; i < cands.length - 1; i++) {
			//the morp after the boundary is short enough
			if (cands[i + 1] - cands[i] < 5) {
				bounds.add(cands[i] - 0.5);
			} else {
				//it was too long, let's remove the preceding ones
				bounds.clear();
			}
		}
		
		//last boundary always gets there
		bounds.add(cands[candLen - 1] - 0.5);
		FuncUtil.filter(bounds, new GraterThanCondition<Double>(2.0));
		return bounds;
	}
	
	@Override
	public double getDistance(String s1, String s2){
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
		
		int[] cumLens1 = this.segmentation.cumLengths(longer);
		int[] cumLens2 = this.segmentation.cumLengths(shorter);
		
		List<Double> bounds1 = this.getImportantBoundaries(cumLens1);
		List<Double> bounds2 = this.getImportantBoundaries(cumLens2);
		
		double[] prev = new double[l2 + 1];
		double[] curr = new double[l2 + 1];
		
		prev[0] = 0;
		for (int i = 1; i < prev.length; ++i) {
			double boundDist1 = this.getBoundDist(-1, bounds1);
			double boundDist2 = this.getBoundDist(i - 1, bounds2);
			
			double cost = boundDist1 + boundDist2; 
			prev[i] = cost;
		}
		
		for (int i = 0; i < l1; ++i) {
			double boundDist1 = this.getBoundDist(i, bounds1);
			double boundDist2 = this.getBoundDist(-1, bounds2);
			
			double cost = boundDist1 + boundDist2; 
			curr[0] = cost;
			for (int j = 1; j < curr.length; ++j) {
				if (longer.charAt(i) == shorter.charAt(j - 1)) {
					curr[j] = prev[j - 1];
				} else {
					
					boundDist1 = this.getBoundDist(i, bounds1);
					boundDist2 = this.getBoundDist(j - 1, bounds2);
					
					cost = boundDist1 + boundDist2; 
					double deletionDist =  prev[j] + cost;
					double substDist =  prev[j - 1] + cost * this.getDistMatrix().get(longer.charAt(i), shorter.charAt(j - 1));
					double insertDist =  curr[j - 1] + cost;

					double minDist  = MathUtil.min(deletionDist, substDist, insertDist);
					curr[j] = minDist;
				}
			}
			
			//current becomes previous and previous is reused
			double [] tmp = prev;
			prev = curr;
			curr = tmp;
		}
		
		double norm = 5.0;

		double result = prev[prev.length - 1] / norm;
		
		return result;
	}

	private double getBoundDist(int i, List<Double> bounds) {
		double dist = Double.MAX_VALUE;
		for (Double bound : bounds) {
			double currDist = Math.abs(bound - i);
			if (currDist < dist) {
				dist = currDist;
			}
		}
		return dist;
	}
	
}
