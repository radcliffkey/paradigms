package klic.radoslav.test;

import java.io.IOException;
import java.util.Set;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.morphology.phonChange.StemChangeRule;

public class TestRuleInduction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String [] testWords = {"matk", "matc", "vrah", "matek", "chodit", "nechodit", "vater", "bruder", "brüder"};
		try {
			ManualData.setSeedFileName("seed-cz.txt");
			Set<StemChangeRule> rules = ManualData.getStemChageRules();
			for (StemChangeRule rule : rules) {
				System.out.println(rule);
				for (String word : testWords) {
					if (rule.isApplicable(word)) {
						System.out.println(word + "->" +rule.applyUnchecked(word));
					}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
