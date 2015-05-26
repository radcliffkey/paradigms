package klic.radoslav.test;

import java.util.List;

import klic.radoslav.morphology.phonChange.RuleFileParser;
import klic.radoslav.morphology.phonChange.StemChangeRule;

public class TestRuleFileParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RuleFileParser p = new RuleFileParser();
		try {
			List<StemChangeRule> rules = p.parseFile(args[0]);
			for (StemChangeRule rule : rules) {
				System.out.println(rule);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
