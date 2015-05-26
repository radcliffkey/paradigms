package cz.klic.test;

import java.util.Map;

import cz.klic.segmentation.StringMapSegmentation;
import cz.klic.segmentation.reader.ParamorSegmentationReader;
import cz.klic.stringDistance.SegmentDistance;
import cz.klic.stringDistance.StringDistanceMetric;
import cz.klic.util.StringUtil;

public class TestSegmentation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ParamorSegmentationReader reader = new ParamorSegmentationReader(args[0]);
			StringMapSegmentation seg = reader.readSegmentation();
			Map<String, String[]> segmap = seg.getSegmentMap();
			for (String form : segmap.keySet()) {
				System.out.println(form + ": " + StringUtil.join("+", segmap.get(form)));
			}
			
			StringDistanceMetric metric = new SegmentDistance(seg);
			System.out.println(metric.getDistance("adresu", "adresy"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
