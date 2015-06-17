package cz.klic.stringDistance;

import cz.klic.segmentation.StringSegmentation;

/**
 * 
 * Distance of two segmented strings. Depends on ratio of common string-initial
 * sequence of segments.
 * 
 */
public class SegmentDistance implements StringDistanceMetric {

	protected StringSegmentation getSegmentation() {
		return segmentation;
	}

	private StringSegmentation segmentation;
	
	public SegmentDistance(StringSegmentation segmentation) {
		super();
		this.segmentation = segmentation;
	}

	@Override
	public double getDistance(String s1, String s2) {
		String[] segments1 = this.segmentation.getSegments(s1);
		String[] segments2 = this.segmentation.getSegments(s2);
		
		int l1 = segments1.length;
		int l2 = segments2.length;
		int shorterLen = Math.min(l1, l2);
		
		int i = 0;
		while (i < shorterLen && segments1[i].equals(segments2[i])) {
			++i;
		}
		
		double sim = (2.0 * i) / (l1 + l2);
		//double sim = i / (double) shorterLen;
		double dist = 1 - sim;
		
		return dist;
	}

}
