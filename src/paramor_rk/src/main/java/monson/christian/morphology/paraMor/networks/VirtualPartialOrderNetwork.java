/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;


import gnu.trove.set.hash.THashSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;

/** 
 * This class implements a radical departure from the algorithms for 
 * VirtualPartialOrderNetwork_storingPossibleParents (and historically the algorithms for 
 * PartialOrderNetwork_OnDemand.
 * 
 * 1) Directly find the context sizes of ALL the possible parents by
 *    a) storing the set of most specific schemes that are ancestors of all the affixes
 *       in the current scheme.
 *    b) looping through a) building a Hash:
 *       
 *       affix->sizeOfParentFormedFromAffix
 *       
 *       This b) hash can be simply computed by adding in the size from a) of the most 
 *       specific scheme that contains affix.
 *       
 * 2) At this point we can compute the actual (nth) largest parent if requested to do so.
 * 
 * This algorithm should be faster because it will:
 * 1) compute fewer full schemes (just 1 instead of however many need to be computed based 
 *    on the level 1 heuristic of the added context)
 * 2) loop through fewer most specific schemes to calculate the set of possible parents.
 *    because storing the set of most specific schemes that are ancestors of all the affixes
 *    in the current scheme IMPLICITLY also stores the set of possible parent affixes.
 *    (Instead of looping through ALL the most specific scheme ancestors of the newly added
 *    affix you only need to loop through the most specific scheme ancestors of the
 *    newly added affixs AND all the other affixes in the current scheme.)
 *    
 * On the other hand, this algorithm actually calculates the size of every parent--resulting
 * in a lot of integer adds.
 * 
 * Empirically, this class *is* faster, mainly because this class
 * computes fewer full schemes ( 1) from above) and computing full Schemes that use
 * SetOfMorphemes<M> is SLOW because the sets of morphemes are kept always sorted using 
 * TreeSets. As of Nov. 2006, I don't want to try to optimize SetOfMorphemes<M>.  
 * SetOfMorphemes<M> is meant to be EASY TO USE, STRONGLY TYPED, and FULLY COMPARABLE and 
 * not necessarily blindingly fast.
 */

public class VirtualPartialOrderNetwork extends PartialOrderNetwork 
										/*implements BottomUpSearchableNetwork*/ {

	private static final long serialVersionUID = 1L;

    protected DenseNetworkSchemeGenerator denseNetworkSchemeGenerator;


    // Be careful not to call with an identifier that has 
    // the wrong 'theNetworkClass' field
	VirtualPartialOrderNetwork(Identifier identifier) {
		assert(identifier.getTheNetworkClass().equals(VirtualPartialOrderNetwork.class));
		
		this.identifier = identifier;
		
		SchemeShell schemeShell = new SchemeShell(identifier);
		denseNetworkSchemeGenerator = new DenseNetworkSchemeGenerator(schemeShell);
	}

	public HashMap<Affix, Integer> 
	getParentAffixesWithAdherentSizes(
			Scheme child, 
			Set<Scheme> mostSpecificAncestorsOfChild) {
		
		int DEBUG = 0;
		
		// Here is the meat of the algorithm to calculate the size of each parent of current.
		//
		//  1) For each MostSpecificScheme, MSS, which is an ancestor of 
		//     all the affixes in the new 'current' scheme.
		//
		//    2) For each affix, F, in each MSS
		//
		//      3) add the inherentAdherentSize of MSS to the size of the 
		//         parent of current formed by moving up to F
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************");
			System.err.println("  Calculating adherent sizes of all parents");
			System.err.println("**********************************************");
		}
		
		HashMap<Affix, Integer> affixToParentSize = new HashMap<Affix, Integer>();
		
		for (Scheme mostSpecificSchemeAncestor : mostSpecificAncestorsOfChild) {
			SetOfMorphemes<Affix> ancestorAffixes = mostSpecificSchemeAncestor.getAffixes();
			int ancestorSize = mostSpecificSchemeAncestor.adherentSize();
			
			for (Affix ancestorAffix : ancestorAffixes) {
				int oldSize = 0;
				if (affixToParentSize.containsKey(ancestorAffix)) {
					oldSize = affixToParentSize.get(ancestorAffix);
				}
				affixToParentSize.put(ancestorAffix, oldSize + ancestorSize);
			}
		}
		// Remove all the entries for the affixes that are already *in* current
		// I think this will be faster than checking if each 'ancestorAffix' is
		// actually a 'currentAffix' because current.getAffixes() is a SetOfMorphemes<Affix>
		// which may keep the set of affixes as a TreeMap which does a lot of
		// PhonologicalAffix.compareTo()'s--which are ssslllooowww
		for (Affix anAffixOfChild : child.getAffixes()) {
			affixToParentSize.remove(anAffixOfChild);
		}
		
		return affixToParentSize;
	}

	/*
	 * Passing in 'null' for the 'helper' Scheme (and for the 'mostSpecificAncestorsOfHelper'
	 * Causes this method to just backoff to a slower method for finding the the
	 * most specific scheme ancestors for 'schemeOfInterest'.
	 */
	public Set<Scheme> 
	getMostSpecificSchemeAncestors(
			Scheme schemeOfInterest, 
			Scheme helper,
			Set<Scheme> mostSpecificAncestorsOfHelper) {
		
		SetOfMorphemes<Affix> affixesOfSchemeOfInterest = schemeOfInterest.getAffixes();
		
		Set<Scheme> mostSpecificAncestorsOfSchemeOfInterest = null;

		if (schemeOfInterest.level() == 1) {
			
			mostSpecificAncestorsOfSchemeOfInterest =
				denseNetworkSchemeGenerator.getMostSpecificSchemeAncestors(
						affixesOfSchemeOfInterest.iterator().next());
				
			// if there are no MostSpecificSchemes that are ancestors of the 1 affix in this
			// level 1 scheme, then create an empty set of MostSpecificSchemes
			if (mostSpecificAncestorsOfSchemeOfInterest == null) {  
				mostSpecificAncestorsOfSchemeOfInterest = new TreeSet<Scheme>();
				
		    // otherwise create a copy of the set of MostSpecificSchemes that was returned.
			// we are going to modify this set so we need a copy.  Specifically, we will
			// be removing MostSpecificSchemes that are NOT ancestors of other affixes
			// in the path as we move up the network.
			} else {
				mostSpecificAncestorsOfSchemeOfInterest = 
					new TreeSet<Scheme>(mostSpecificAncestorsOfSchemeOfInterest);
			}	
			
		// If we just moved up 1 level in the network, then we can update 
		// 'mostSpecificAncestorsOfCurrent'
		} else if ( (helper != null) && 
				    ((helper.level() + 1) == schemeOfInterest.level()) &&
				    (affixesOfSchemeOfInterest.containsAll(helper.getAffixes())) &&
				    (mostSpecificAncestorsOfHelper != null) ) {
			
			Affix newlyAddedAffix = 
				affixesOfSchemeOfInterest.minus(helper.getAffixes()).iterator().next();
			
			Set<Scheme> mostSpecificAncestorsOfNewlyAddedAffix =
				new THashSet<Scheme>(
						denseNetworkSchemeGenerator.getMostSpecificSchemeAncestors(newlyAddedAffix));
			
			mostSpecificAncestorsOfSchemeOfInterest.retainAll(
					mostSpecificAncestorsOfNewlyAddedAffix);
			
		} else {
			mostSpecificAncestorsOfSchemeOfInterest = 
				denseNetworkSchemeGenerator.getMostSpecificSchemeAncestors(schemeOfInterest);
		}
		
		return mostSpecificAncestorsOfSchemeOfInterest;
	}

	
	public Set<Level1Scheme> getSmallestSchemesAboveLevel0() {
		return denseNetworkSchemeGenerator.getLevel1Schemes();
	}

	public int getNumberOfParents(Scheme scheme) {
		return denseNetworkSchemeGenerator.getNumberOfParents(scheme.getAffixes());
	}
	
	public Scheme getASchemeByName(SetOfMorphemes<Affix> schemeName) {
		return denseNetworkSchemeGenerator.generateScheme(schemeName);
	}

	public boolean isAffixPresentInNetwork(Affix affix) {
		return denseNetworkSchemeGenerator.isAffixPresent(affix);
	}
	
	public Set<Scheme> getSmallers(Scheme scheme) {
		return new TreeSet<Scheme>(
					denseNetworkSchemeGenerator.computeChildren(
							scheme.getAffixes()).values());
	}


	@Override
	public Map<Affix, Level1Scheme> getLevel1SchemesByAffix() {
		return denseNetworkSchemeGenerator.getLevel1SchemesByAffix();
	}

	public Scheme generateScheme(SetOfMorphemes<Affix> affixes) {
		return denseNetworkSchemeGenerator.generateScheme(affixes);
	}
			
	public int   
	getSizeOfComplementaryNode(Scheme node, Scheme larger) {
		SetOfMorphemes<Affix> largerMinusNode 
				= larger.getAffixes().minus(node.getAffixes());
		
		if (largerMinusNode.size() == 1) {
			Affix addedAffix = largerMinusNode.iterator().next();
			Scheme complementaryScheme =
				denseNetworkSchemeGenerator.getLevel1Scheme(addedAffix);
			int sizeOfComplementaryScheme = complementaryScheme.adherentSize();
			return sizeOfComplementaryScheme;
		}

		/*
		 * This function is called by VerticalDecision where 'larger' is a parent
		 * of 'node'. In a VirtualPartialOrderNetwork, all parents are exactly one 
		 * level higher than children. Hence, I do not currently implement functionality
		 * to calculate the size of complementary nodes that are not just the simple
		 * 1 level higher.
		 */	
		
		System.err.println();
		System.err.println("!!!!! ERROR !!!!!");
		System.err.println();
		System.err.println("the method VirtualPartialOrderNetwork.getSizeOfComplementaryNode()");
		System.err.println("  has not been implemented for complementary nodes containing");
		System.err.println("  more than 1 suffix. But this method was called with the");
		System.err.println("  Schemes:");
		System.err.println(node);
		System.err.println();
		System.err.println("AND");
		System.err.println(larger);
		System.err.println();
		System.err.println("  where the complementary node has the following affixes:");
		System.err.println();
		System.err.println(largerMinusNode);
		System.err.println();
		System.err.println();
		
		return -1;
	}
}
