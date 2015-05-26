package klic.radoslav.test;

import java.util.ArrayList;
import java.util.Collections;

import klic.radoslav.morphology.SeedAnalyses;
import klic.radoslav.morphology.SeedAnalysesParser;
import klic.radoslav.morphology.schemes.SeedScheme;

public class TestSeedParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SeedAnalysesParser parser = new SeedAnalysesParser();
		try {
			SeedAnalyses analyses = parser.parse("/home/radcliff/temp/seed-test.txt");
			//SeedAnalyses analyses = parser.parse("seed-si.txt");
			
			for (SeedScheme  s : analyses.getSeedSchemes()) {
				System.out.println(s.affixSignature());
				System.out.println(s);
			}
			
			System.out.println(analyses.getStemToAffixes());
			ArrayList<String> wordTypes = new ArrayList<String>(analyses.getCoveredWordTypes());
			Collections.sort(wordTypes);
			System.out.println(wordTypes);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
