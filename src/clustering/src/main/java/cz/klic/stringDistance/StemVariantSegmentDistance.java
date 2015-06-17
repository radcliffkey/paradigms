package cz.klic.stringDistance;

import java.util.Arrays;

import cz.klic.segmentation.StringSegmentation;
import cz.klic.stemVariants.StemVariants;
import cz.klic.util.StringUtil;

public class StemVariantSegmentDistance extends SegmentDistance {

	private StemVariants stemVariants;

	public StemVariantSegmentDistance(StringSegmentation segmentation, StemVariants variants) {
		super(segmentation);
		this.stemVariants =  variants;
	}

	@Override
	public double getDistance(String s1, String s2) {
		String[] segments1 = this.getSegmentation().getSegments(s1);
		String[] segments2 = this.getSegmentation().getSegments(s2);
		
		int l1 = segments1.length;
		int l2 = segments2.length;
		int shorterLen = Math.min(l1, l2);
		
		int i = 0;
		while (i < shorterLen
				&& (segments1[i].equals(segments2[i]) ||
						this.stemVariants.isVariant(segments1[i], segments2[i]))) {
			++i;
		}
		
		double sim = (2.0 * i) / (l1 + l2);
		//double sim = i / (double) shorterLen;
		double dist = 1 - sim;
		
		if ((segments1.length > 2 || segments2.length > 2) && shorterLen > 1 && dist > 0.99) {
			String stemCand1 = StringUtil.join("", Arrays.copyOf(segments1, segments1.length - 1));
			String stemCand2 = StringUtil.join("", Arrays.copyOf(segments2, segments2.length - 1));
			if (stemCand1.equals(stemCand2) || this.stemVariants.isVariant(stemCand1, stemCand2)) {
				return 0.5;	
			}
		}
		
		return dist;
	}
}
