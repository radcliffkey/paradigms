package klic.radoslav.morphology.phonChange;

import java.util.Map;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public interface StemChangeRule {

	public String applyUnchecked(String stem);

	public Context applyChecked(Context stem,
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes);

	public boolean isApplicable(String stem);

	public boolean isApplicable(String stem, Affix affix);

}