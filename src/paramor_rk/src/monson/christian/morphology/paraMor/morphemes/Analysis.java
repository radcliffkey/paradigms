/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

import java.util.List;


public class Analysis {
	Context context;
	List<Affix> affixes;
	
	public Analysis(Context context, Affix... affixes) {
		this.context = context;
		for (Affix affix : affixes) {
			this.affixes.add(affix);
		}
	}

	public Context getStem() {
		return context;
	}

	public List<Affix> getAffixes() {
		return affixes;
	}
	
	//TODO: This needs to be fixed to mark affixes from prefixes, etc.
	@Override
	public String toString() {
		String toReturn = context + "+" + affixes;
		return toReturn;
	}
}
