package cz.klic.segmentation;

import java.util.Map;

public class StringMapSegmentation extends MapSegmentation<String> implements StringSegmentation {

	public StringMapSegmentation(Map<String, String[]> segmentMap) {
		super(segmentMap);
	}

	@Override
	public int[] cumLengths(String s) {
		String [] segments = this.getSegments(s);
		int segCnt = segments.length;
		int [] result = new int[segCnt];
		int cumSum = 0;
		for (int i = 0; i < segments.length; i++) {
			cumSum += segments[i].length();
			result[i] = cumSum;
		}
		return result;
	}

	@Override
	public String[] getSegments(String token) {
		String[] segments =  super.getSegments(token);
		if (segments == null) {
			String [] segmentArr = {token};
			return segmentArr;
		}
		
		return segments;
	}

}
