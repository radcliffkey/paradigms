package klic.radoslav.morphology.phonChange;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class StemFinalChangeRule implements StemChangeRule {
	
	private String fromStemEnding;
	private String toStemEnding;
	private Set<Affix> fromAffixes;
	private Set<Affix> toAffixes;
	private Set<Character> toChars;
	private Set<Character> fromChars;
	
	public StemFinalChangeRule(String fromStemEnding, String toStemEnding,
			Set<Affix> fromAffixes, Set<Affix> toAffixes) {
		super();
		this.fromStemEnding = fromStemEnding;
		this.toStemEnding = toStemEnding;
		this.fromAffixes = fromAffixes;
		this.toAffixes = toAffixes;
		this.toChars = this.getInitChars(toAffixes);
		this.fromChars = this.getInitChars(fromAffixes);
	}

	private HashSet<Character> getInitChars(Set<Affix> affixes) {
		HashSet<Character> initChars = new HashSet<Character>();
		for (Affix affix : affixes) {
			Character c = affix.getLeadMorphemeCharacter();
			// null suffix 
			if (c == null) {
				c = '0';
			}
			initChars.add(c);
		}
		
		return initChars;
	}

	
	public String getFromStemEnding() {
		return fromStemEnding;
	}

	public String getToStemEnding() {
		return toStemEnding;
	}

	public Set<Affix> getFromAffixes() {
		return fromAffixes;
	}

	public Set<Affix> getToAffixes() {
		return toAffixes;
	}
	
	@Override
	public String applyUnchecked(String stem) {
		StringBuilder sb = new StringBuilder(stem);
		sb.replace(stem.length() - fromStemEnding.length(), stem.length(), this.toStemEnding);
		return sb.toString();
	}

	@Override
	public Context applyChecked(Context stem, Map<Context, SetOfMorphemes<Affix>> stemToAffixes) {
		String stemStr = stem.toStringAvoidUndescore();
//		DebugLog.write(stemStr);
//		DebugLog.write(this);
		if (!stemStr.endsWith(fromStemEnding)) {
//			DebugLog.write("rule does not fire - stem doesn't end with required endind");
			return null;
		}
		SetOfMorphemes<Affix> affixes = stemToAffixes.get(stem);
		if (!checkAffixInitChars(fromChars, affixes) || affixes.containsAny(this.toAffixes)) {
//			DebugLog.write("rule does not fire - None of the stem's suffixes begin with triggering character");
			return null;
		}
		
		String variantStr = this.applyUnchecked(stemStr);
		Context variantStem = new Context(variantStr, "");
		SetOfMorphemes<Affix> variantAffixes = stemToAffixes.get(variantStem);
		
		if (variantAffixes == null) {			
//			DebugLog.write("proposed variant doesn't exist in the corpus");
			return null;
		}
		
		if (!affixes.containsAny(fromAffixes) && !variantAffixes.containsAny(toAffixes)) {
			return null;
		}
		
		if (!checkAffixInitChars(this.toChars, variantAffixes)) {
//			DebugLog.write("None of the variant's suffixes begin with triggering character");
			return null;
		}
		
		return variantStem;
	}
	
	private boolean checkAffixInitChars(Set<Character> chars, SetOfMorphemes<Affix> variantAffixes) {
		for (Affix affix : variantAffixes) {
			Character c = affix.getLeadMorphemeCharacter();
			if (c == null) {
				c = '0';
			}
			if (chars.contains(c)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isApplicable(String stem) {
		return stem.endsWith(fromStemEnding);
	}
	

	@Override
	public boolean isApplicable(String stem, Affix affix) {
		return stem.endsWith(fromStemEnding) && this.fromAffixes.contains(affix);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fromAffixes == null) ? 0 : fromAffixes.hashCode());
		result = prime * result
				+ ((fromChars == null) ? 0 : fromChars.hashCode());
		result = prime * result
				+ ((fromStemEnding == null) ? 0 : fromStemEnding.hashCode());
		result = prime * result
				+ ((toAffixes == null) ? 0 : toAffixes.hashCode());
		result = prime * result + ((toChars == null) ? 0 : toChars.hashCode());
		result = prime * result
				+ ((toStemEnding == null) ? 0 : toStemEnding.hashCode());
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
		StemFinalChangeRule other = (StemFinalChangeRule) obj;
		if (fromAffixes == null) {
			if (other.fromAffixes != null)
				return false;
		} else if (!fromAffixes.equals(other.fromAffixes))
			return false;
		if (fromChars == null) {
			if (other.fromChars != null)
				return false;
		} else if (!fromChars.equals(other.fromChars))
			return false;
		if (fromStemEnding == null) {
			if (other.fromStemEnding != null)
				return false;
		} else if (!fromStemEnding.equals(other.fromStemEnding))
			return false;
		if (toAffixes == null) {
			if (other.toAffixes != null)
				return false;
		} else if (!toAffixes.equals(other.toAffixes))
			return false;
		if (toChars == null) {
			if (other.toChars != null)
				return false;
		} else if (!toChars.equals(other.toChars))
			return false;
		if (toStemEnding == null) {
			if (other.toStemEnding != null)
				return false;
		} else if (!toStemEnding.equals(other.toStemEnding))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.fromStemEnding).append("->").append(this.toStemEnding);
		sb.append(" ").append(this.fromChars).append(" ").append(this.toChars);
		return sb.toString();
	}
}
