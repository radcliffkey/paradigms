package klic.radoslav.morphology.phonChange;

import java.util.Map;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class UnprefixRule implements StemChangeRule {
	
	private String prefix; 
	
	public UnprefixRule(String prefix) {
		super();
		this.prefix = prefix;
	}
	
	@Override
	public String applyUnchecked(String stem) {
		return stem.substring(this.prefix.length());
	}

	@Override
	public Context applyChecked(Context stem,
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes) {
		
		String stemStr = stem.toStringAvoidUndescore();
		if (!this.isApplicable(stemStr)) {
			return null;
		}
		
		String variantStr = this.applyUnchecked(stemStr);
		Context variantStem = new Context(variantStr, "");
		
		if (stemToAffixes.containsKey(variantStem)) {
			return variantStem;
		}
		return null;
	}

	@Override
	public boolean isApplicable(String stem) {
		return stem.startsWith(this.prefix);
	}

	@Override
	public boolean isApplicable(String stem, Affix affix) {
		return stem.startsWith(this.prefix);
	}

	@Override
	public String toString() {
		return this.prefix + "* -> *";
	}

}
