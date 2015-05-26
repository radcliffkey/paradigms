package cz.klic.stringDistance.paradigmDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.klic.stringDistance.StringDistanceMetric;

public class ParadigmDistance implements StringDistanceMetric {

	private Map<String, Set<String>> wordToParadigms;
	
	public ParadigmDistance(Map<String, Set<String>> wordToParadigms) {
		super();
		this.wordToParadigms = wordToParadigms;
	}

	@Override
	public double getDistance(String s1, String s2) {
		Set<String> pdgms1 = this.wordToParadigms.get(s1);
		if (pdgms1 == null) {
			return 1.0;
		}
		
		Set<String> pdgms2 = this.wordToParadigms.get(s2);
		if (pdgms2 == null) {
			return 1.0;
		}
		HashSet<String> intersect = new HashSet<String>(pdgms1);
		intersect.retainAll(pdgms2);
		
		double dist = (double) intersect.size() / Math.sqrt(pdgms1.size() * pdgms2.size());
		dist = 1 - dist;
		return dist;
	}
	
	public static Map<String, Set<String>> readWordToParadigmsMap(BufferedReader input) throws IOException {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		
		String line ;
		while ((line = input.readLine()) != null) {
			String [] fields = line.split("\t");
			String word = fields[0];
			List<String> pdgmList = Arrays.asList(Arrays.copyOfRange(fields, 1, fields.length));
			Set<String> pdgms = new HashSet<String>(pdgmList);
			result.put(word, pdgms);
		}
		
		input.close();
		
		return result;
	}

}
