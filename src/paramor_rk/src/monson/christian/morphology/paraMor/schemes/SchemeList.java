/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.schemes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class SchemeList<S extends Scheme> extends ArrayList<S> {

	private static final long serialVersionUID = 1L;

	public SchemeList() {}
	
	public SchemeList(Collection<? extends S> collection) {
		super(collection);
	}
	
	public SchemeList(int initialCapacity) {
		super(initialCapacity);
	}
	
	public SetOfMorphemes<Affix> getAllAffixes() {
		SetOfMorphemes<Affix> allAffixes = new SetOfMorphemes<Affix>();
		for (S scheme : this) {
			allAffixes.add(scheme.affixes);
		}
		
		return allAffixes;
	}

	public List<SetOfMorphemes<Affix>> getSetsOfAffixes() {
		List<SetOfMorphemes<Affix>> allSetsOfAffixes =
			new ArrayList<SetOfMorphemes<Affix>>();
		for (S scheme : this) {
			allSetsOfAffixes.add(scheme.getAffixes());
		}
		return allSetsOfAffixes;
	}
}
