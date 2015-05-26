/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.schemes;

import java.util.Collection;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class SchemeSet<S extends AffixScheme> extends TreeSet<S> {

	private static final long serialVersionUID = 1L;

	public SchemeSet() {}
	
	public SchemeSet(Collection<? extends S> collection) {
		super(collection);
	}
	
	// Unfortunately this method must be here AND in SchemeList because
	// because there is no multiple inheritance in Java
	public SetOfMorphemes<Affix> getAllAffixes() {
		SetOfMorphemes<Affix> allAffixes = new SetOfMorphemes<Affix>();
		for (S scheme : this) {
			allAffixes.add(scheme.affixes);
		}
		
		return allAffixes;
	}
}
