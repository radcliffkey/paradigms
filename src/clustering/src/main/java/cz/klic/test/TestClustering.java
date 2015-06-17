package cz.klic.test;

import java.util.Arrays;
import java.util.List;

import cz.klic.clustering.HierarchicalClustering;
import cz.klic.stringDistance.LevenshteinMetric;

public class TestClustering {

	public static void main(String[] args) throws Exception {
		List<String> insts = Arrays.asList("ahoj", "ahoja", "kekes", "kekesu",
				"xfcr");
		HierarchicalClustering<String> hc = new HierarchicalClustering<String>(
				new LevenshteinMetric(),
				HierarchicalClustering.ClusterApproach.AVERAGE_DISTANCE);
		hc.cluster(insts);
	}
}
