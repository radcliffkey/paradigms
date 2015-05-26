/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;

import java.util.Set;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.searchAndProcessing.VerticalMetric;

/**
 * A marker interface to flag a (presumably) PartialOrderNetwork as searchable with a BottomUpSearch.
 * 
 * @author cmonson
 *
 */
public interface BottomUpSearchableNetwork {
	
	public Set<Level1Scheme> getSmallestSchemesAboveLevel0();

	public int getNumberOfParents(Scheme current);

	public Scheme getNthLargestParentByAdherents(Scheme current, int n);

	public Set<Scheme> getSmallers(Scheme current);

	public Scheme getASchemeByName(SetOfMorphemes<Affix> schemeName);

	public Scheme getNthBestParentBy(VerticalMetric verticalMetric, Scheme current, int n);

	public boolean isAffixPresentInNetwork(Affix suffix);
}
