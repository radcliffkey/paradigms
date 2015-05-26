package klic.radoslav.morphology.phonChange;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class StemPair {
	
	private Context stem1;
	private Context stem2;
	private SetOfMorphemes<Affix> affixes1;
	private SetOfMorphemes<Affix> affixes2;
	
	public StemPair(Context stem1, Context stem2,
			SetOfMorphemes<Affix> affixes1, SetOfMorphemes<Affix> affixes2) {
		super();
		this.stem1 = stem1;
		this.stem2 = stem2;
		this.affixes1 = affixes1;
		this.affixes2 = affixes2;
	}

	public Context getStem1() {
		return stem1;
	}
	
	public Context getStem2() {
		return stem2;
	}

	public SetOfMorphemes<Affix> getAffixes1() {
		return affixes1;
	}

	public SetOfMorphemes<Affix> getAffixes2() {
		return affixes2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((affixes1 == null) ? 0 : affixes1.hashCode());
		result = prime * result
				+ ((affixes2 == null) ? 0 : affixes2.hashCode());
		result = prime * result + ((stem1 == null) ? 0 : stem1.hashCode());
		result = prime * result + ((stem2 == null) ? 0 : stem2.hashCode());
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
		StemPair other = (StemPair) obj;
		if (affixes1 == null) {
			if (other.affixes1 != null)
				return false;
		} else if (!affixes1.equals(other.affixes1))
			return false;
		if (affixes2 == null) {
			if (other.affixes2 != null)
				return false;
		} else if (!affixes2.equals(other.affixes2))
			return false;
		if (stem1 == null) {
			if (other.stem1 != null)
				return false;
		} else if (!stem1.equals(other.stem1))
			return false;
		if (stem2 == null) {
			if (other.stem2 != null)
				return false;
		} else if (!stem2.equals(other.stem2))
			return false;
		return true;
	}
}
