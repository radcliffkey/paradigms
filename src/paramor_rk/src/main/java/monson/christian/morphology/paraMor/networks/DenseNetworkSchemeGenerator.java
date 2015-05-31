/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import klic.radoslav.util.DebugLog;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.AffixScheme;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;

public class DenseNetworkSchemeGenerator implements Serializable {

	private static final long serialVersionUID = 1L;

	// For speed make sure this is a HashMap
	private Map<Affix, Level1Scheme> level1SchemesByAffix = null;
	
	//I now use THashSet and THashMap for speed and space - Feb., 2008
	private Map<Affix, 
  				Set<Scheme>> affixToContainingMostSpecificSchemes = 
  		new THashMap<Affix,
  					 Set<Scheme>>();
	

	public DenseNetworkSchemeGenerator(SchemeShell schemeShell) {
		System.err.println("entering DenseNetworkSchemeGenerator");
		
		// for speed this is a HashMap and not a TreeMap
		level1SchemesByAffix = 
			new THashMap<Affix, Level1Scheme>(schemeShell.getLevel1SchemesByAffix());
		
		System.err.println("created level1SchemesByAffix");
		
		affixToContainingMostSpecificSchemes = 
			new THashMap<Affix, Set<Scheme>>(
					schemeShell.getAffixToContainingMostSpecificSchemes());
		
		System.err.println("Leaving DenseNetworkSchemeGenerator");
	}
	

	/*
	 * I just trust that no malicious person will modify Level1Schemes
	 */
	public Map<Affix, Level1Scheme> getLevel1SchemesByAffix() {
		return level1SchemesByAffix;
	}
	
	/**
	 * Returns a copy of the level1Schemes.  
	 */ 
	public Set<Level1Scheme> getLevel1Schemes() {
		// I return a copy because the values() function returns a Collection by default
		// and I know it will be a set.
		return new TreeSet<Level1Scheme>(level1SchemesByAffix.values());
	}
	
	public Level1Scheme getLevel1Scheme(Affix affix) {
		return level1SchemesByAffix.get(affix);
	}
	
	// This used to return a TreeSet, but TreeSet takes too much memory, and it
	// is expensive to always keep large numbers of Schemes sorted. So just sort
	// when you absolutely need to.
	public Set<Scheme> getMostSpecificSchemeAncestors(Affix affix) {
		/*
		THashSet<Scheme> MostSpecificSchemesContainingAffix = affixToContainingMostSpecificSchemes.get(affix);
		if (MostSpecificSchemesContainingAffix == null) {
			return null;
		}
		return new TreeSet<Scheme>(affixToContainingMostSpecificSchemes.get(affix));
		*/
		// This was called when affixToContainingMostSpecificSchemes was a TreeSet itself.
		
		
		return affixToContainingMostSpecificSchemes.get(affix);
	}

	// Intersect the results of getMostSpecificSchemeAncestors(Affix)
	// for each affix in the new 'current' Scheme.
	public Set<Scheme> getMostSpecificSchemeAncestors(Scheme current) {
		
		THashSet<Scheme> mostSpecificSchemeAncestors = null;
		
		for (Affix affix : current.getAffixes()) {
			if (mostSpecificSchemeAncestors == null) {
				mostSpecificSchemeAncestors = new THashSet<Scheme>(getMostSpecificSchemeAncestors(affix));
			} else {
				mostSpecificSchemeAncestors.retainAll(getMostSpecificSchemeAncestors(affix));
			}
		}
		return mostSpecificSchemeAncestors;
	}
	
	// SchemeCollection_Level1 is used by PartialOrderNetwork_Dynamic_Dense.  Being dense and dynamic a
	// PartialOrderNetwork_Dynamic_Dense doesn't really care about the most specific schemes.
	// What it *does* care about is what all the dense parents of any (dense) node are.  Dense parents
	// of any particular node are just a single level higher than that node.  And for any particular parent
	// the additional affix is found by:
	//   for each affix in current node
	//     get set of affixes that the current affix co-occurs with in some most specific scheme
	//   intersect these sets of affixes
	//   the intersection contains all the individual affixes that can be added to the current
	//     node to generate a parent.
	// Hence we care about affixes that co-occur in some most specific scheme
	//
	// Returns a HashSet<Affix> and not a SetOfMorphemes<Affix> because the result
	// is not the name of a Scheme, nor does it need to be kept in lexicographic 
	// order, which takes up a lot of time.
	//
	//
	public HashSet<Affix> gatherCooccuringAffixes(Affix affix) {
		int DEBUG = 1;
		
		// cache once all the co-occurring affixes have been computed for a particular affix
		// As explained above, this cache EXPLODES memory and barely effects time.
		//if (cooccuringAffixes.containsKey(affix)) {
		//	return cooccuringAffixes.get(affix);
		//}
		
		if (DEBUG > 1) {
			System.err.print("Affix=" + affix);
		}

		HashSet<Affix> theseCooccuringAffixes = new HashSet<Affix>();
		
		Set<Scheme> nonL1SchemesAffixOccursIn = null;
		if (affixToContainingMostSpecificSchemes.containsKey(affix)) {
			nonL1SchemesAffixOccursIn = 
				affixToContainingMostSpecificSchemes.get(affix);
			
		// If affix only occurred in a Level 1 scheme then there are no cooccuringAffixes
	    // So just return.
		} else {
			return theseCooccuringAffixes;
		}

		int cooccuranceCounter = 0;
		for (AffixScheme affixScheme : nonL1SchemesAffixOccursIn) {
			SetOfMorphemes<Affix> affixes = affixScheme.getAffixes();
			
			for (Affix affixOther : affixes) {
				if (affixOther.equals(affix)) {
					continue;
				}
				
				if (DEBUG > 1) {
					cooccuranceCounter++;
					if ((cooccuranceCounter % 10000) == 0) {
						System.err.println("  " + (cooccuranceCounter/1000) + "Kth affix encountered in ALL nonL1SchemesAffixOccursIn.  ");
						System.err.println("    Size of cooccuringAffixes{" + affix + "}: " + 
											(theseCooccuringAffixes.size()));
					}
				}
												
				theseCooccuringAffixes.add(affixOther);
			}
		}
		
		// DON'T USE THIS CACHE
		//if (theseCooccuringAffixes.size() > 50000) {
		//	cooccuringAffixes.put(affix, theseCooccuringAffixes);
		//}
		
		if (DEBUG > 0) {
			System.err.println("  Affix : " + affix + " : " + theseCooccuringAffixes.size() + " cooccuring affixes");
		}
		
		return theseCooccuringAffixes;
	}
	
	// Returns a HashSet<Affix> and not a SetOfMorphemes<Affix> because the result
	// is not the name of a Scheme, nor does it need to be kept in lexicographic 
	// order, which takes up a lot of time.
	public HashSet<Affix> 
	getAffixesThatCooccurWithAllAffixesIn(SetOfMorphemes<Affix> affixes) {
		int DEBUG = 1;
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************************************************************");
			System.err.println("  Getting all affixes that cooccur with the affixes: " + affixes);
			System.err.println("**********************************************************************************************");
		}
		
		HashSet<Affix> toReturn = null;
		boolean toReturnIsInitialized = false;
		
		for (Affix affix : affixes) {
			HashSet<Affix> cooccuringAffixes = gatherCooccuringAffixes(affix);
			if ( ! toReturnIsInitialized) {
				
				// DO NOT HASH.  It eats memory, causing it to be unbounded and only 
				// minimally saves time.
				//
				// Need to make a copy here because the 'cooccuringAffixes' that are 
				// returned are actually a **cached** data structure that may be used again.
				//toReturn = new HashSet<Affix>(cooccuringAffixes);
				
				toReturn = cooccuringAffixes;
				
				toReturnIsInitialized = true;
			} else {
				toReturn.retainAll(cooccuringAffixes);
			}
		}
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************************************************************");
			System.err.println("  Done getting all affixes that cooccur with the affixes: " + affixes);
			System.err.println("**********************************************************************************************");
		}
		
		return toReturn;
	}

	// Potentially a costly function, as this has to calculate what affixes form parents
	// Avoid this function if possible.
	public int getNumberOfParents(SetOfMorphemes<Affix> affixes) {
		
		HashSet<Affix> affixesThatCooccurWithAllAffixesInScheme =
			getAffixesThatCooccurWithAllAffixesIn(affixes);
		
		return affixesThatCooccurWithAllAffixesInScheme.size();
	}
	
	public Map<Affix, Scheme> computeParents(SetOfMorphemes<Affix> affixes) {
		Map<Affix, SetOfMorphemes<Affix>> namesOfParents = computeNamesOfParents(affixes);
		
		Map<Affix, Scheme> parents = new TreeMap<Affix, Scheme>();
		
		for (Affix affix : namesOfParents.keySet()) {
			SetOfMorphemes<Affix> nameOfParent = namesOfParents.get(affix);
			
			Scheme parentScheme = generateScheme(nameOfParent);
			
			parents.put(affix, parentScheme);
		}
		
		return parents;
	}
	
	// Returns a SortedMap<Affix, SetOfMorphemes<Affix>> where each 
	// SetOfMorphemes<Affix> value is the name of an immediate 
	// (dense) parent of node, and the Affix key is the additional
	// affix that was added to the affixes of node to make that
	// particular parent.  The returned map is a SortedMap to
	// mirror computeNamesOfChildren()--but since we have to sort by
	// *something* I sort by decreasing adherent size of the level
	// 1 scheme corrosponding to the added affix.
	//
	// For some reason it is very expensive to sort ByDecreasingL1Size.
	// I WISH ECLIPSE HAD A DECENT PROFILER.  But since it doesn't and
	// since this time I couldn't even get the netbeans profiler to work (or
	// actually to get netbeans to compile my code) I don't have a decent
	// analysis of WHY putting the created parents into a TreeMap sorted
	// ByDecreasingL1Size is so slow.
	public Map<Affix, SetOfMorphemes<Affix>> 
	computeNamesOfParents(SetOfMorphemes<Affix> affixes) {
		
		System.err.println();
		System.err.println("  Computing Names of Parents");
		
		Map<Affix, SetOfMorphemes<Affix>> affixesToParentNamesByDecreasingL1Size = 
			//new TreeMap<Affix, SetOfMorphemes<Affix>>(new ByDecreasingL1Size());
			new HashMap<Affix, SetOfMorphemes<Affix>>();
		
		HashSet<Affix> affixesThatCooccurWithAllAffixesInNode =
			getAffixesThatCooccurWithAllAffixesIn(affixes);
		
		System.err.println("    Building the names of the parents");
		
		for(Affix affix : affixesThatCooccurWithAllAffixesInNode) {
			SetOfMorphemes<Affix> nameOfParent = 
				new SetOfMorphemes<Affix>(affixes);
			nameOfParent.add(affix);
			
			affixesToParentNamesByDecreasingL1Size.put(affix, nameOfParent);
		}
		
		System.err.println("    Done building the names of the parents");
		System.err.println();
		
		return affixesToParentNamesByDecreasingL1Size;
	}
	
	
	public Scheme generateScheme(SetOfMorphemes<Affix> schemeName) {
		Scheme scheme = new Scheme(schemeName);
		
		boolean firstTime = true;
		for (Affix affix : schemeName) {
			Scheme level1Scheme = level1SchemesByAffix.get(affix);
			
			if (firstTime) {
				firstTime = false;
				
				if (level1Scheme == null) {
					DebugLog.write("Level 1 scheme for affix " + affix + " is null!");
				}
				
				scheme.addToContexts(level1Scheme.getContexts());
				
				continue;
			}
			
			
			scheme.intersectStemsInPlace(level1Scheme);
		}
		
		return scheme;
	}
	
	public Scheme generateParentScheme(Scheme scheme, Affix affix) {
		SetOfMorphemes<Affix> parentName = new SetOfMorphemes<Affix>(scheme.getAffixes());
		parentName.add(affix);
		return generateScheme(parentName);
	}


	public boolean isAffixPresent(Affix affix) {
		return level1SchemesByAffix.containsKey(affix);
	}
	
	public SortedMap<Affix, Scheme> computeChildren(SetOfMorphemes<Affix> affixes) {
		SortedMap<Affix, SetOfMorphemes<Affix>> namesOfChildren = computeNamesOfChildren(affixes);
		
		SortedMap<Affix, Scheme> childrenSchemes = new TreeMap<Affix, Scheme>();
		
		for (Affix affix : namesOfChildren.keySet()) {
			SetOfMorphemes<Affix> childName = namesOfChildren.get(affix);
			Scheme childScheme = generateScheme(childName);
			childrenSchemes.put(affix, childScheme);
		}
		
		return childrenSchemes;
	}
	
	// Returns a Map of Affix to SetOfMorphemes<Affix>'s where each 
	// SetOfMorphemes<Affix> is the name of an immediate (dense) 
	// child of node--that is, one by one we remove a particular affix
	// from the affixes of node and the remaining affixes form the 
	// name of one of node's children.  And the key Affix is the
	// removed affix.
	//
	// For the short-circuited node insertion we want to order the 
	// children we expand by a heuristic that expands children that 
	// are most likely to be **small** first.  As a first order
	// approximation a child is likely to be small if all of its 
	// affixes have small level 1 schemes.  Hence we order the list 
	// of returned child names by the adherent size of the level 1 scheme
	// corrosponding to the affix that is **missing** from each child.  
	// First comes the child whose missing affix has the **largest** 
	// L1 scheme, then the child whose missing affix has the second 
	// **largest** L1 scheme, etc.
	public SortedMap<Affix, SetOfMorphemes<Affix>> 
	computeNamesOfChildren(SetOfMorphemes<Affix> schemeName) {
		
		SortedMap<Affix, SetOfMorphemes<Affix>> affixesToChildNamesByDecreasingL1Size = 
			new TreeMap<Affix, SetOfMorphemes<Affix>>(new ByDecreasingL1Size());
		
		for (Affix affix : schemeName) {
			SetOfMorphemes<Affix> childName = new SetOfMorphemes<Affix>(schemeName);
			childName.remove(affix);
			affixesToChildNamesByDecreasingL1Size.put(affix, childName);
		}
		
		return affixesToChildNamesByDecreasingL1Size;
	}

	private class ByDecreasingL1Size implements Comparator<Affix>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 * @param affix1
		 * @param affix2
		 * @return -1 if the level 1 scheme that contains just affix 1 has more adherents
		 * 			than the level 1 scheme that contains just affix 2
		 */
		public int compare(Affix affix1, Affix affix2) {
			int affix1sAdherentSize = 
				level1SchemesByAffix.get(affix1).adherentSize();
			int affix2sAdherentSize = 
				level1SchemesByAffix.get(affix2).adherentSize();
			
			if (affix1sAdherentSize > affix2sAdherentSize) {
				return -1;
			}
			if (affix1sAdherentSize < affix2sAdherentSize) {
				return 1;
			}
			return affix1.compareTo(affix2);
		}
	}

	/**
	 * Builds the scheme that is the lowest common ancestor of <code>descendent1</code>
	 * and <code>descendent2</code>
	 * 
	 * @param descendent1
	 * @param descendent2
	 * @return
	 */
	// This function could be static.
	Scheme buildLowestCommonAncestor(Scheme descendent1, Scheme descendent2) {
		// Build the Scheme that would exactly be lowest common ancestor of descendent1 and descendent2
	
		SetOfMorphemes<Affix> lowestCommonAncestorAffixes = 
			new SetOfMorphemes<Affix>(descendent1.getAffixes());
		lowestCommonAncestorAffixes.add(descendent2.getAffixes());
		
		// Avoid unnecessary work.  If descendent1 (OR descendent2) **IS** the lowest common
		// ancestor of descendent1 and descendent2 then just return descendent1 (OR descendent2).
		if (lowestCommonAncestorAffixes.size() == descendent1.getAffixes().size()) {
			return descendent1;
		}
		if (lowestCommonAncestorAffixes.size() == descendent2.getAffixes().size()) {
			return descendent2;
		}

		Scheme lowestCommonAncestor = new Scheme(lowestCommonAncestorAffixes);

		SetOfMorphemes<Context> lowestCommonAncestorStems = 
			descendent1.getContexts().intersect(descendent2.getContexts());
		lowestCommonAncestor.addToContexts(lowestCommonAncestorStems);
				
		return lowestCommonAncestor;
	}




}
