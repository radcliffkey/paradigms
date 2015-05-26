package klic.radoslav.test;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import klic.radoslav.settings.Settings;

public class TestSettings {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Settings.setOption("allowedAffixes", new SetOfMorphemes<Affix> (new Affix("a"), new Affix("b")));
		SetOfMorphemes<Affix> s = Settings.getOption("allowedAffixes");
		System.out.println(s);

	}

}
