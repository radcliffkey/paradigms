package klic.radoslav.test;

import klic.radoslav.morphology.phonChange.RuleParser;

public class TestRuleParse {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		RuleParser rp = new RuleParser();
		System.out.println(rp.parseRule("sf: _c + i <-> _k + a, em"));

	}

}
