/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.searchAndProcessing.VerticalMetric;

/**
 * This class is not actually used any more, it has been replaced by the new VirtualPartialOrderNetwork
 * class.  However, this code is illuminating and so I am preserving it for posterity.
 *   -- Christian Nov. 2006
 * 
 * Here is an outline of the current algorithm (Nov. 2006)
 * 
 * Specifically, I could store the set of most specific schemes that are ancestors of the current
 * Scheme.  Currently to find the largest parent, I:
 *  1) Find all possible parents by
 *     a) storing the set of possible parents of the just lower scheme you came from
 *     b) intersecting a) with the set of all affixes that cooccur with the new
 *        affix added to make this current scheme.  Where b) is found by looping through
 *        all the most specific schemes that contain the new affix and storing the set
 *        of affixes that cooccur with the new affix.
 *  2) Sorting these possible parents by the size of level 1 scheme of the new affix that
 *     would be added.
 *  3) Starting with the possible parent with the largest level 1 size and working your way down
 *     a) calculate the possible parent.
 *     b) if the size of the possible parent is larger than the level 1 size of the next largest
 *        possible parent affix
 *     c) then the current possible parent is the next largest parent that has not been calculated yet    
 *     
 * Empirically, VirtualPartialOrderNetwork *is* faster than this class, mainly because 
 * VirtualPartialOrderNetwork computes fewer full schemes and computing full Schemes that use
 * SetOfMorphemes<M> is SLOW because the sets of morphemes are kept always sorted using TreeSets.
 * As of Nov. 2006, I don't want to try to optimize SetOfMorphemes<M>.  SetOfMorphemes<M> is meant
 * to be EASY TO USE, STRONGLY TYPED, and FULLY COMPARABLE and not necessarily blindingly fast.
 * If you want fast, then try to avoid building lots of Schemes.
 */


public class VirtualPartialOrderNetwork_storingPossibleParents extends PartialOrderNetwork implements BottomUpSearchableNetwork {

	private static final long serialVersionUID = 1L;

	
	// Variables that get updated as we move about in the virtual network 
	
	private Scheme current = null;

	// A HashSet<Affix> and not a SetOfMorphemes<Affix> because the affixes that form parents
	//  1) do not form a scheme
	//  2) do not need to be sorted lexicographically, which takes a LOT of time
	HashSet<Affix> affixesThatFormParentsOfCurrent = null;

	private List<Scheme> nLargestParentsOfCurrent = null;
	private PriorityQueue<ScoredSchemeAffixBundle> affixesToBecomeParents = null;

	protected DenseNetworkSchemeGenerator denseNetworkSchemeGenerator;
	



	public VirtualPartialOrderNetwork_storingPossibleParents(PartialOrderNetwork.Identifier identifier) {
		this.identifier = identifier;
		
		SchemeShell schemeShell = new SchemeShell(this.identifier);
		denseNetworkSchemeGenerator = new DenseNetworkSchemeGenerator(schemeShell);
	}
	

	
	private void resetCurrent(Scheme scheme) {
		int DEBUG = 0;
		
		nLargestParentsOfCurrent = new ArrayList<Scheme>();
		affixesToBecomeParents = new PriorityQueue<ScoredSchemeAffixBundle>();
		
		Scheme oldCurrent = current;
		current = scheme;
	
		SetOfMorphemes<Affix> currentAffixes = current.getAffixes();
		
		if (scheme.level() == 1) {
			affixesThatFormParentsOfCurrent = 
				denseNetworkSchemeGenerator.gatherCooccuringAffixes(currentAffixes.iterator().next());
		
		} else if ( (oldCurrent != null) && 
				    ((oldCurrent.level() + 1) == current.level()) &&
				    (currentAffixes.containsAll(oldCurrent.getAffixes())) &&
				    (affixesThatFormParentsOfCurrent != null) ) {
			
			Affix newlyAddedAffix = currentAffixes.minus(oldCurrent.getAffixes()).iterator().next();
			
			HashSet<Affix> affixesThatCooccurWithNewlyAddedAffix =
				denseNetworkSchemeGenerator.gatherCooccuringAffixes(newlyAddedAffix);
			
			affixesThatFormParentsOfCurrent.retainAll(affixesThatCooccurWithNewlyAddedAffix);
			
		} else {
			// If all else fails, then re-intersect the cooccuring affixes of all the affixes in the
			// new current Scheme.
			affixesThatFormParentsOfCurrent = denseNetworkSchemeGenerator.getAffixesThatCooccurWithAllAffixesIn(currentAffixes);
		}
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************");
			System.err.println("  Forming prioritized Affix queue");
			System.err.println("**********************************************");
		}
		
		for (Affix affixThatFormsParent : affixesThatFormParentsOfCurrent) {
		
			Level1Scheme level1SchemeThatFormsParent = denseNetworkSchemeGenerator.getLevel1Scheme(affixThatFormsParent);
			
			ScoredSchemeAffixBundle prioritizedAffix = 
				new ScoredSchemeAffixBundle(affixThatFormsParent, 
											 level1SchemeThatFormsParent, 
											 level1SchemeThatFormsParent.adherentSize().doubleValue());
			affixesToBecomeParents.add(prioritizedAffix);
		}
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************");
			System.err.println("  Done Forming prioritized Affix queue");
			System.err.println("**********************************************");
		}
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
		return new TreeSet<Scheme>(denseNetworkSchemeGenerator.computeChildren(scheme.getAffixes()).values());
	}

	public Scheme getNthBestParentBy(VerticalMetric verticalMetric, Scheme scheme, int n) {
		switch (verticalMetric) {
		case RATIO:
			return getNthLargestParentByAdherents(scheme, n);
			
		default:
			System.err.println();
		System.err.println("  Sorry, Only getting an nth best parent by ratio");
		System.err.println("    is thus far implemented over an on-demand network");
		System.err.println();
		return null;
		}
	}
	
	/**
	 * Returns the parent Scheme of the passed in scheme the the Nth most stems.
	 */
	public Scheme getNthLargestParentByAdherents(Scheme scheme, int n) {
		int DEBUG = 0;
		
		if ((current == null) || ( ! scheme.equals(current))) {
			resetCurrent(scheme);
		}
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("************************************************************************");
			System.err.println("  Getting the " + n + "th largest parent of: " + scheme.getAffixes());
			System.err.println("    (current has already been reset if this is the first parent");
			System.err.println("************************************************************************");
		}
		
		boolean expansionSucceeded = false;
		while (nLargestParentsOfCurrent.size() < n) {
			expansionSucceeded = expandNextLargestParentOfCurrent();
			if ( ! expansionSucceeded) {
				
				if (DEBUG > 0) {
					System.err.println();
					System.err.println("****************************************************************************");
					System.err.println("  Getting the " + n + "th largest parent of: " + scheme.getAffixes());
					System.err.println("    failed (i.e. there was no " + n + "th largest parent");
					System.err.println("****************************************************************************");
				}
				
				return null;
			}
		}
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("****************************************************************************");
			System.err.println("  Got the " + n + "th largest parent of: " + scheme.getAffixes());
			System.err.println("****************************************************************************");
		}
		
		// Careful of off By 1!!!
		return nLargestParentsOfCurrent.get(n-1);
	}
	
	
	// First directly find the next largest parent.  And then once you have found
	// the next largest parent, expand it.
	private boolean expandNextLargestParentOfCurrent() {
		
		boolean DEBUG = false;
		
		Scheme parentToInsert = null;
		
		// Loop until we either succeed in finding the next largest parent or
		// we fail to find a next largest parent
		while (true) {
						
			// Returns null if there are no more parents to expand
			ScoredSchemeAffixBundle nextUnexpandedPrioritizedAffix = affixesToBecomeParents.poll();

			// If all the possible parents of node have been expanded then expandNParents has
			// no more work to do.  And expansion fails, because we can't expand more!
			if (nextUnexpandedPrioritizedAffix == null) {
				return false;  // expansion failed.  We ran out of affixes to expand.
			}
			
			Scheme nextUnexpandedPriorityScheme = nextUnexpandedPrioritizedAffix.getScheme();

			// Build the parent of node formed by adding the affix
			// nextUnexpandedPrioritizedAffix.getAffix() to node.
			Scheme newPriorityScheme = 
				denseNetworkSchemeGenerator.buildLowestCommonAncestor(current, 
																	  nextUnexpandedPriorityScheme);

			ScoredSchemeAffixBundle prioritizedAffixAfterNext = affixesToBecomeParents.peek();
			
			// If we are looking at the last unexpanded affix then we know we need to expand it
			if (prioritizedAffixAfterNext == null) {
				
				if (DEBUG) {
					System.err.println();
					System.err.println("*** This is the last possible parent to expand.  ***");
					System.err.println("*** So we are expanding it.                      ***");
					System.err.println();
				}
				
				parentToInsert = newPriorityScheme;
				break;
			}
					
			if (DEBUG) {
				System.err.println();
				System.err.println();
				System.err.println("-------------------------------------------------------------");
				System.err.println(nextUnexpandedPrioritizedAffix.getAffix());
				System.err.println("   Evaluating what the true adherent context size of of the ");
				System.err.println("   parent that involes this affix would be");
				System.err.println("   from the Prioritizing Partial Order Node:");
				System.err.println(nextUnexpandedPrioritizedAffix.getScheme().toPrettyString(30));
			}
			
			// Build the ScoredNodeAffixBundle that we are using to prioritize what
			// affix should be expanded next
			ScoredSchemeAffixBundle newPriorityParent = 
				new ScoredSchemeAffixBundle(nextUnexpandedPrioritizedAffix.getAffix(), 
										     newPriorityScheme, 
										     newPriorityScheme.adherentSize().doubleValue());
			
			
			if (DEBUG) {
				System.err.println();
				System.err.println("The new priority of this affix is:");
				System.err.println(newPriorityParent.getScheme().toPrettyString(30));
			}
			
			// If the priority of the newPriorityParent is higher (i.e. it has a smaller rank)
			// then insert the new priority parent.  Careful to use the defined compareTo()
			// method--otherwise parents can be added in the wrong order.
			if (newPriorityParent.compareTo(prioritizedAffixAfterNext) < 0) {
				
				if (DEBUG) {
					System.err.println();
					System.err.println("*** The new priority of expanding this affix is at least as     ***");
					System.err.println("*** high as the priority of expanding the affix after this one: ***");
					System.err.println(prioritizedAffixAfterNext.getScheme().toPrettyString(30));
					System.err.println();
					System.err.println("*** So we are expanding this affix. ****************************");
					System.err.println();
				}
				
				parentToInsert = newPriorityScheme;
				break;			
			}
			
			// If the priority of the affix after this next affix is larger than the
			// the adherent size of the parent built from the nextUnexpandedPrioritizedAffix,
			// then put the affix that we just popped off the priority queue back into the queue
			// but with its new priority.
			affixesToBecomeParents.add(newPriorityParent);			
		}
			
		nLargestParentsOfCurrent.add(parentToInsert);
		
		System.err.println();
		System.err.println("------------");
		System.err.println("Successfully built the " + 
						   nLargestParentsOfCurrent.size() + "th largest parent scheme:");
		System.err.println();
		System.err.println(parentToInsert.toPrettyString(30));
		
		return true;
	}
	
	@Override
	public Map<Affix, Level1Scheme> getLevel1SchemesByAffix() {
		return denseNetworkSchemeGenerator.getLevel1SchemesByAffix();
	}
}
