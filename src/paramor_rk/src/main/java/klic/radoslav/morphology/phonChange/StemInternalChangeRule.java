package klic.radoslav.morphology.phonChange;

import java.util.Map;
import java.util.Set;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * a rule changing a stem a(delta1)b to a(delta2)b
 */
public class StemInternalChangeRule implements StemChangeRule {
	
	protected String delta1;
	protected String delta2;
	
	public StemInternalChangeRule(String delta1, String delta2) {
		super();
		this.delta1 = delta1;
		this.delta2 = delta2;
	}

	@Override
	public String applyUnchecked(String stem) {
		return stem.replaceFirst(delta1, delta2);
	}

	@Override
	public Context applyChecked(Context stem,
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes) {
		
		String stemStr = stem.toStringAvoidUndescore();
		if (!this.isApplicable(stemStr)) {
			return null;
		}
		
		Context altStem = new Context(this.applyUnchecked(stemStr), "");
		
		if (!stemToAffixes.containsKey(altStem)) {
			return null;
		}
		
		Set<Affix> commonAffixes = stemToAffixes.get(stem).getCopyOfMorphemes();
		commonAffixes.retainAll(stemToAffixes.get(altStem).getCopyOfMorphemes());
		
		//The stem variants should have no affixes in common, except for zero affix
		if (!commonAffixes.isEmpty()) {
			if (commonAffixes.size() == 1) {
				Affix commonAffix = commonAffixes.iterator().next();
				if (!commonAffix.isNullAffix()) {
					return null;
				}
			} else {
				return null;
			}
		}
		
		return altStem;
	}

	@Override
	public boolean isApplicable(String stem) {
		
		int idx = stem.indexOf(delta1);
		if (idx < 1 || idx == stem.length() - delta1.length()) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isApplicable(String stem, Affix affix) {
		return isApplicable(stem);
	}

	@Override
	public String toString() {
		return String.format("*%s* -> *%s*", this.delta1, this.delta2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delta1 == null) ? 0 : delta1.hashCode());
		result = prime * result + ((delta2 == null) ? 0 : delta2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StemInternalChangeRule other = (StemInternalChangeRule) obj;
		if (delta1 == null) {
			if (other.delta1 != null)
				return false;
		} else if (!delta1.equals(other.delta1))
			return false;
		if (delta2 == null) {
			if (other.delta2 != null)
				return false;
		} else if (!delta2.equals(other.delta2))
			return false;
		return true;
	}
	
	
}
