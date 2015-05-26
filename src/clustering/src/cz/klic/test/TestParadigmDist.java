package cz.klic.test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import cz.klic.stringDistance.paradigmDistance.ParadigmDistance;
import cz.klic.util.Fileutil;

public class TestParadigmDist {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String segmentedFileName = args[0];
		Map<String, Set<String>> wordToParadigms;
		try {
			wordToParadigms = ParadigmDistance.readWordToParadigmsMap(Fileutil.getBufReader(segmentedFileName));
			ParadigmDistance pdgmDist = new ParadigmDistance(wordToParadigms);
			System.out.println(pdgmDist.getDistance("odmítl", "odmítne"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
