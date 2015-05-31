package klic.radoslav.morphology.phonChange;

import java.util.Map;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class PrefixRule implements StemChangeRule {

	private String prefix; 
	
	public PrefixRule(String prefix) {
		super();
		this.prefix = prefix;
	}

	@Override
	public String applyUnchecked(String stem) {
		return this.prefix + stem;
	}

	@Override
	public Context applyChecked(Context stem,
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes) {
		
		String stemStr = stem.toStringAvoidUndescore();
		String variantStr = this.applyUnchecked(stemStr);
		Context variantStem = new Context(variantStr, "");
		
		if (stemToAffixes.containsKey(variantStem)) {
			return variantStem;
		}
		return null;
	}

	@Override
	public boolean isApplicable(String stem) {
		return true;
	}

	@Override
	public boolean isApplicable(String stem, Affix affix) {
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("* -> %s*", this.prefix);
	}

}
