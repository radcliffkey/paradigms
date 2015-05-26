/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.util.ComparablePair;

/**
 * Although <code>BottomUpSearchResultCluster</code> has a set of <code>Affix</code>es
 * and a set of <code>Context</code>s <code>BottomUpSearchResultCluster</code> is NOT a
 * <code>Scheme</code>!!  The set of affixes in a bottomUpSearchResultCluster is merely
 * the union of the sets of affixes from all the Scheme's that it covers, similarly
 * for stems.  That a given BottomUpSearchResultCluster contains a particular context and
 * and particular affix DOES NOT attest that that context-affix combination occured in
 * any language data!!
 * 
 * @author cmonson
 *
 */
public class BottomUpSearchResultCluster implements Serializable {

	/** 
	 * NOTE: This definition of 'Orthographically' is a little odd.  It is not
	 *       orthographic by leaves, but by the sets of covered intermediate children.
	 */
	public static class 
	OrthographicallyByChildCoverings implements Comparator<BottomUpSearchResultCluster>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(BottomUpSearchResultCluster cluster_a, BottomUpSearchResultCluster cluster_b) {
			return OrthographicallyByChildCoverings.compareStatic(cluster_a, cluster_b);
		}
		
		private static int compareStatic(BottomUpSearchResultCluster cluster_a, 
										  BottomUpSearchResultCluster cluster_b) {
			int DEBUG = 0;
			
			if (DEBUG > 0) {
				System.err.println();
				System.err.println("Trying to copare the clusters");
				System.err.println();
				System.err.println(cluster_a);
				System.err.println();
				System.err.println(cluster_b);
			}
			
			// First a simple reference identity check
			if (cluster_a == cluster_b) {
				return 0;
			}
			
			
			// The DEGENERATE CASE
			
			// cluster_a is degenerate (has no children), but cluster_b is not. 
			// then cluster_a is shorter, so A is smaller
			if ((cluster_a.childrenSimilarity == null) && (cluster_b.childrenSimilarity != null)) {
				return -1;
			}
			
			// cluster_b is degenerate (has no children), but cluster_a is not. 
			// then cluster_b is shorter so B is smaller
			if ((cluster_b.childrenSimilarity == null) && (cluster_a.childrenSimilarity != null)) {
				return 1;
			}
			
			// Both cluster_a and cluster_b are degenerate (have no children)
			// The 'coveredAffixes' and 'coveredStems' of degenerate clusters are 
			// guaranteed to exist.
			if ((cluster_a.childrenSimilarity == null) && (cluster_b.childrenSimilarity == null)) {
				
				// Comparing the covered *affixes*
				SetOfMorphemes<Affix> coveredAffixesOfClusterA =	
					new SetOfMorphemes<Affix>(cluster_a.coveredAffixes);
				SetOfMorphemes<Affix> coveredAffixesOfClusterB =
					new SetOfMorphemes<Affix>(cluster_b.coveredAffixes);
				int compared = 
					coveredAffixesOfClusterA.compareTo(coveredAffixesOfClusterB);
				if (compared != 0) {
					return compared;
				}
			
				// Comparing the covered *affixes*
				SetOfMorphemes<Context> coveredStemsOfClusterA =	
					new SetOfMorphemes<Context>(cluster_a.coveredStems);
				SetOfMorphemes<Context> coveredStemsOfClusterB =
					new SetOfMorphemes<Context>(cluster_b.coveredStems);
				compared = 
					coveredStemsOfClusterA.compareTo(coveredStemsOfClusterB);
				if (compared != 0) {
					return compared;
				}
				return 0;
			}
			
			
			// The NON-DEGENERATE CASE
			
			// I don't want to 'incorporateDataFromChildren()' unless I absolutely have to, becuase it
			// takes a lot of time to 'incorporateDataFromChildren()' for every potential
			// Cluster.  Hence, I don't directly compare the Affixes or Stems of THIS cluster
			// but rather I compare the Affixes and Stems of the CHILDREN of THIS cluster.
			// It's fast, so I do it.
			//
			
			// Comparing the *affixes* covered by the children with *larger* internal similarity
			SetOfMorphemes<Affix> affixesOfClusterAsChildWithLargerInternalSimilarity =
				new SetOfMorphemes<Affix>(cluster_a.childWithLargerInternalSimilarity.coveredAffixes);
			SetOfMorphemes<Affix> affixesOfClusterBsChildWithLargerInternalSimilarity =
				new SetOfMorphemes<Affix>(cluster_b.childWithLargerInternalSimilarity.coveredAffixes);
			int compared = 
				affixesOfClusterAsChildWithLargerInternalSimilarity.compareTo(
						affixesOfClusterBsChildWithLargerInternalSimilarity);
			if (compared != 0) {
				return compared;
			}
			
			// Comparing the *affixes* covered by the children with *smaller* internal similarity
			SetOfMorphemes<Affix> affixesOfClusterAsChildWithSmallerInternalSimilarity =
				new SetOfMorphemes<Affix>(cluster_a.childWithSmallerInternalSimilarity.coveredAffixes);
			SetOfMorphemes<Affix> affixesOfClusterBsChildWithSmallerInternalSimilarity =
				new SetOfMorphemes<Affix>(cluster_b.childWithSmallerInternalSimilarity.coveredAffixes);
			compared = 
				affixesOfClusterAsChildWithSmallerInternalSimilarity.compareTo(
						affixesOfClusterBsChildWithSmallerInternalSimilarity);
			if (compared != 0) {
				return compared;
			}
			
			// Comparing the *stems* covered by the children with *larger* internal similarity
			SetOfMorphemes<Context> stemsOfClusterAsChildWithLargerInternalSimilarity =
				new SetOfMorphemes<Context>(cluster_a.childWithLargerInternalSimilarity.coveredStems);
			SetOfMorphemes<Context> stemsOfClusterBsChildWithLargerInternalSimilarity =
				new SetOfMorphemes<Context>(cluster_b.childWithLargerInternalSimilarity.coveredStems);
			compared = 
				stemsOfClusterAsChildWithLargerInternalSimilarity.compareTo(
						stemsOfClusterBsChildWithLargerInternalSimilarity);
			if (compared != 0) {
				return compared;
			}
			
			// Comparing the *stems* covered by the children with *smaller* internal similarity
			SetOfMorphemes<Context> stemsOfClusterAsChildWithSmallerInternalSimilarity =
				new SetOfMorphemes<Context>(cluster_a.childWithSmallerInternalSimilarity.coveredStems);
			SetOfMorphemes<Context> stemsOfClusterBsChildWithSmallerInternalSimilarity =
				new SetOfMorphemes<Context>(cluster_b.childWithSmallerInternalSimilarity.coveredStems);
			compared = 
				stemsOfClusterAsChildWithSmallerInternalSimilarity.compareTo(
						stemsOfClusterBsChildWithSmallerInternalSimilarity);
			if (compared != 0) {
				return compared;
			}
			
			
			// Then compare children (recursively).  
			//
			// Hopefully I will hardly ever fall into
			// this code here.  For two *different* clusters' children to cover exactly the same set of
			// affixes and the same set of stems would be pretty darn strange (and rare).
			//
			// But you do fall into this sometimes, as when the similarity of the children
			// of a cluster is 1.0
			
			
			// Compare children with Larger internal similarity
			
			if ((cluster_a.childWithLargerInternalSimilarity == null) && 
				(cluster_b.childWithLargerInternalSimilarity == null)) {
				// do nothing
				
			} else 	if ((cluster_a.childWithLargerInternalSimilarity != null) && 
						(cluster_b.childWithLargerInternalSimilarity != null)) { 
				int smaller_children_compared = compareStatic(cluster_a.childWithLargerInternalSimilarity, cluster_b.childWithLargerInternalSimilarity);
				if (smaller_children_compared != 0) {
					return smaller_children_compared;
				}
				
			} else {
				
				// 'cluster_a' has a 'smaller_child' but 'that' doesn't, so 'cluster_a' is "shorter"
				// so 'cluster_a' is smaller.
				if (cluster_a.childWithLargerInternalSimilarity == null) {
					return -1;
				}
				
				// 'that' has a 'smaller_child' but 'cluster_a' doesn't, so 'that' is "shorter"
				// so 'that' is smaller.
				return 1;
			}
			
			
			// Compare children with Smaller internal similarity
			
			if ((cluster_a.childWithSmallerInternalSimilarity == null) && (cluster_b.childWithSmallerInternalSimilarity == null)) {
				// do nothing
				
			} else if ((cluster_a.childWithSmallerInternalSimilarity != null) && (cluster_b.childWithSmallerInternalSimilarity != null)) {
				int larger_children_compared = compareStatic(cluster_a.childWithSmallerInternalSimilarity, cluster_b.childWithSmallerInternalSimilarity);
				if (larger_children_compared != 0) {
					return larger_children_compared;
				}
				
			} else {
				
				// 'cluster_a' has a 'larger_child' but 'that' doesn't, so 'cluster_a' is "shorter"
				// so 'cluster_a' is smaller.
				if (cluster_a.childWithSmallerInternalSimilarity == null) {
					return -1;
				}
				
				// 'that' has a 'larger_child' but 'cluster_a' doesn't, so 'that' is "shorter"
				// so 'that' is smaller.
				return 1;
			}
			
			
			// Everything is identical between these two clusters: They have the same, tree
			// structure, and at every node in the tree, the same sets of stems and affixes
			// are covered by the children of that node.
			
			return 0;
		}

	}
	
	public static class 
	ByDecreasingInternalSimilarity implements Comparator<BottomUpSearchResultCluster>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(BottomUpSearchResultCluster clusterA, BottomUpSearchResultCluster clusterB) {
			
			// Comparing two degenerate clusters
			if ((clusterA.childrenSimilarity == null) && (clusterB.childrenSimilarity == null)) {
				return OrthographicallyByChildCoverings.compareStatic(clusterA, clusterB);
			
			}
			
			// A degenerate cluster is smaller than a cluster with children
			if (clusterA.childrenSimilarity == null) {
				return 1;
			}
			if (clusterB.childrenSimilarity == null) {
				return -1;
			}
			
			
			if (clusterA.childrenSimilarity > clusterB.childrenSimilarity) {
				return -1;
			}
			if (clusterA.childrenSimilarity < clusterB.childrenSimilarity) {
				return 1;
			}
			return OrthographicallyByChildCoverings.compareStatic(clusterA, clusterB);
		}
		
	}
	
	public static class 
	ByDecreasingNumberOfCoveredAffixes implements Comparator<BottomUpSearchResultCluster>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(BottomUpSearchResultCluster clusterA, BottomUpSearchResultCluster clusterB) {
			if (clusterA.coveredAffixes.size() > clusterB.coveredAffixes.size()) {
				return -1;
			}
			if (clusterA.coveredAffixes.size() < clusterB.coveredAffixes.size()) {
				return 1;
			}
			
			return OrthographicallyByChildCoverings.compareStatic(clusterA, clusterB);
		}
	}
	
	public static class 
	ByDecreasingNumberOfCoveredTypes implements Comparator<BottomUpSearchResultCluster>, 
												Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(BottomUpSearchResultCluster clusterA, BottomUpSearchResultCluster clusterB) {
			if (clusterA.coveredTypes.size() > clusterB.coveredTypes.size()) {
				return -1;
			}
			if (clusterA.coveredTypes.size() < clusterB.coveredTypes.size()) {
				return 1;
			}
			
			return OrthographicallyByChildCoverings.compareStatic(clusterA, clusterB);
		}
	}
	
	private static final long serialVersionUID = 1L;
	
	public static final OrthographicallyByChildCoverings orthographicallyByChildCoverings = 
		new OrthographicallyByChildCoverings();
	public static final ByDecreasingInternalSimilarity byDecreasingInternalSimilarity = 
		new ByDecreasingInternalSimilarity();
	public static final ByDecreasingNumberOfCoveredAffixes byDecreasingNumberOfCoveredAffixes =
		new ByDecreasingNumberOfCoveredAffixes();
	public static final ByDecreasingNumberOfCoveredTypes byDecreasingNumberOfCoveredTypes =
		new ByDecreasingNumberOfCoveredTypes();
	
	
	
	// NOTE: If at some point I decide I really need links to the parents
	//       as well, then I will have to modify the serialization, because
	//       searialization really doesn't like circular links.
	private BottomUpSearchResultCluster childWithLargerInternalSimilarity = null;
	private BottomUpSearchResultCluster childWithSmallerInternalSimilarity  = null;
	
	private BottomUpSearchResultClustering.ParameterSetting parameterSetting;
	
	private Double childrenSimilarity = null;
	
	
	boolean dataHasBeenIncorporatedFromChildren = false;
	
	
	// The next five pieces of data are only initialized when a parent cluster
	// needs to calculate the internal similarity of its children (and one of
	// the children is 'this' cluster).
	
	// I'm fairly sure that keeping around all these searchPaths is sucking up a huge
	// amount of memory
	//
	//private ArrayList<SearchPath> searchPaths = null;
	
	private Set<String> coveredTypes  = null;
	
	// 'coveredAffixStemPairs' serves a similar function to 'coveredTypes' but 
	// where 'coveredTypes' glues the context and affix back together, forever
	// forgetting what the morpheme boundary was, 'coveredAffixStemPairs'
	// explicitly stores the Affix and Context together.
	private Set<ComparablePair<Affix, Context>> coveredAffixStemPairs = null; 
	
	// SetOfMorphrmes has a nice compareTo function already built in,
	// BUT always keeping 'coveredAffixes' and 'coveredStems sorted
	// DRASTICALLY slows down clustering, so I use a Set<Affix> and Set<Context>.
	//
	// SetOfMorphemes might be fast enough now that I don't initialize
	// coveredAffixes and 'coveredStems' until I am reusing 'this' cluster
	// as a child of some other cluster--i.e. until I have decided that 'this'
	// potential cluster is a good cluster that I want to keep.  But the code 
	// works as is now, so I don't want to change it.
	private Set<Affix> coveredAffixes = null;
	private Set<Context>  coveredStems   = null;
	
	private int numberOfLeaves  = 0;
	private int sumOfLeafLevels = 0;
	private double aveLeafLevel = 0;
	private double sumOfLeafEntropies = 0;
	private double aveLeftwardEntropyOfLeaves = 0;

	// Accretion seems to be moving in the right direction but it
	// doesn't quite work yet. The idea behind accretion is to only merge
	// a small scheme into a big scheme--and not allow two small schemes
	// to merge. The problem, is that one cluster nucleus can attract
	// tons and tons of junk. (*null* s) keeps attracting more and
	// more tiny schemes until the original good nucleus is overwhelmed
	// with the dust of tiny scheme. So this 'mergerCredits' variable is
	// designed to allow a single (or other small number) small scheme(s)
	// for every large scheme or nucleus.
	//
	// For each leaf of a cluster that is larger than some threshold
	// 'mergerCredits' will be increased, for each leaf that is
	// smaller than the threshold, 'mergerCredits' will be decreased.
	// And I won't perform a merge if the resulting cluster would have
	// a negative number of 'mergerCredits'.
	private int mergeCredits = 0;
	
	public 
	BottomUpSearchResultCluster(
			SearchPath searchPath,
			BottomUpSearchResultClustering.ParameterSetting parameterSetting) {
		
		this.parameterSetting = parameterSetting;
		
		dataHasBeenIncorporatedFromChildren = true;
		initializeCoveredData();
		
		// I no longer store the actual searchPaths in each Cluster
		//searchPaths.add(searchPath);
		
		Scheme terminalScheme = searchPath.getTerminalScheme();
		
		coveredTypes.addAll(terminalScheme.getCoveredWordTypes());
		coveredAffixStemPairs.addAll(terminalScheme.getCoveredAffixContextPairs());
		coveredAffixes.addAll(terminalScheme.getAffixes().getCopyOfMorphemes());
		coveredStems.addAll(terminalScheme.getContexts().getCopyOfMorphemes());
		
		numberOfLeaves = 1;
		sumOfLeafLevels = terminalScheme.level();
		aveLeafLevel = (double)sumOfLeafLevels / 1.0;
		sumOfLeafEntropies = terminalScheme.getLeftEntropy();
		aveLeftwardEntropyOfLeaves = sumOfLeafEntropies / 1.0;
		
		if (parameterSetting.getUseMergeCredits()) {
			if (coveredTypes.size() > parameterSetting.getChildTypesCoveredCutoff()) {
				mergeCredits = parameterSetting.getNumOfPositiveMergeCredits();
			} else {
				mergeCredits = -1;
			}
		}
	}
	


	/**
	 * Create a cluster with two children.  To make printing a cluster more helpful, we require
	 * that to create a cluster you specify a reason for clustering--in particular you specify
	 * the way similarity was measured between <code>childA<code> and <code>childB</code>, when
	 * deciding to merge these two child clusters.  You specify the similarity measure by
	 * passing the <code>CalculateSimilarityAs</code> that was used to measure their similarity.
	 * 
	 */
	//
	// NOTE: CAN MODIFY previouslyComputedLevel2Sizes !!!!!!
	//
	public 
	BottomUpSearchResultCluster(
			BottomUpSearchResultCluster a_child, 
			BottomUpSearchResultCluster another_child,
			BottomUpSearchResultClustering.ParameterSetting parameterSetting) {
		
		if (byDecreasingInternalSimilarity.compare(a_child, another_child) < 0) {
			this.childWithLargerInternalSimilarity = a_child;
			this.childWithSmallerInternalSimilarity  = another_child;
		} else {
			this.childWithLargerInternalSimilarity = another_child;
			this.childWithSmallerInternalSimilarity  = a_child;
		}
		
		this.parameterSetting = parameterSetting;
		
		childrenSimilarity = computeChildSimilarity();
		
		mergeCredits = childWithLargerInternalSimilarity.mergeCredits +
	    				childWithSmallerInternalSimilarity.mergeCredits;
		
		numberOfLeaves  = childWithLargerInternalSimilarity.numberOfLeaves + 
						  childWithSmallerInternalSimilarity.numberOfLeaves;
		sumOfLeafLevels = childWithLargerInternalSimilarity.sumOfLeafLevels +
						  childWithSmallerInternalSimilarity.sumOfLeafLevels;
		aveLeafLevel = (double)sumOfLeafLevels / (double)numberOfLeaves;
		sumOfLeafEntropies = childWithLargerInternalSimilarity.sumOfLeafEntropies +
							 childWithSmallerInternalSimilarity.sumOfLeafEntropies;
		aveLeftwardEntropyOfLeaves = sumOfLeafEntropies / (double)numberOfLeaves;
	}

	public Double getInternalSimilarity() {
		return childrenSimilarity;
	}

	public BottomUpSearchResultCluster getChildWithLargerInternalSimilarity() {
		return childWithLargerInternalSimilarity;
	}

	public BottomUpSearchResultCluster getChildWithSmallerInternalSimilarity() {
		return childWithSmallerInternalSimilarity;
	}
	
	public int getMergeCredits() {
		return mergeCredits;
	}



	private void incorporateDataFromChildren() {
		dataHasBeenIncorporatedFromChildren = true;
		
		incorporateAChildsData(childWithLargerInternalSimilarity);
		incorporateAChildsData(childWithSmallerInternalSimilarity);
	}
	private void incorporateAChildsData(BottomUpSearchResultCluster child) {
		if (coveredTypes == null) {
			initializeCoveredData();
		}
		
		//searchPaths.addAll(child.searchPaths);
		coveredTypes.addAll(child.coveredTypes);
		coveredAffixStemPairs.addAll(child.coveredAffixStemPairs);
		coveredAffixes.addAll(child.coveredAffixes);
		coveredStems.addAll(child.coveredStems);
	}

	/**
	 * 
	 */
	private void initializeCoveredData() {
		// I no longer store the actual searchPaths in each cluster
		//searchPaths   		  = new ArrayList<SearchPath>();
		coveredTypes   		  = new TreeSet<String>();
		coveredAffixStemPairs = new HashSet<ComparablePair<Affix, Context>>();
		coveredAffixes 		  = new HashSet<Affix>();
		coveredStems   		  = new HashSet<Context>();
	}
	
	public enum CalculateSimilarityAs {
		INTERSECTION_OVER_UNION,
		COSINE
	}
	
	public enum ClusterWRT {
		TYPES,
		AFFIX_STEM_PAIRS,
		AFFIXES,
		AVE_OF_AFFIX_AND_STEM
	}
	
	//
	// NOTE: CAN MODIFY previouslyComputedLevel2Sizes !!!!!!
	//
	private Double 
	computeChildSimilarity() {
		
		incorporateDataFromGrandchildrenToChildren_ifNotPerformedPreviously();

		Double similarity = 0.0;

		switch (parameterSetting.getClusterWRT()) {
		case TYPES:
			similarity = computeChildSimilarity(childWithLargerInternalSimilarity.coveredTypes, 
												childWithSmallerInternalSimilarity.coveredTypes);
			break;
			
		case AFFIX_STEM_PAIRS:
			similarity = computeChildSimilarity(childWithLargerInternalSimilarity.coveredAffixStemPairs, 
												childWithSmallerInternalSimilarity.coveredAffixStemPairs);
			break;
			
		case AFFIXES:
			similarity = computeChildSimilarity(childWithLargerInternalSimilarity.coveredAffixes, 
												childWithSmallerInternalSimilarity.coveredAffixes);
			break;
			
		case AVE_OF_AFFIX_AND_STEM:
			similarity = computeSimilarityUsingAveOfSuffixAndStem();
			break;
		}
		
		
		/* TODO: Remove once 'passesClusterRestrictions' and all the individual
		 * restirction checking functions have been moved to '...Clustering'
		 *  
		// If we are NOT throwing schemes away during clustering, but just, possibly, deciding
		// not to merge them, then check to see if we should decide not to merge the current
		// two child clusters.
		if ( ! parameters.getFilterAtClusterTime()) {

			if (similarity > 0.0) {
				if ( ! passesClusterRestrictions(
						discriminativeFilter, 
						searchNetwork,
						previouslyComputedLevel2Sizes)) {
				
					// If we don't pass some cluster restriction, then we decide to NOT
					// merge the current two child clusters by explicitly setting their
					// similarity to zero.
					similarity = 0.0;
				}
			}
		}
		*/
		
		return similarity;
	}



	private void incorporateDataFromGrandchildrenToChildren_ifNotPerformedPreviously() {
		childWithLargerInternalSimilarity.incorporateDataFromChildren_ifNotPerformedPrevisously();
		childWithSmallerInternalSimilarity.incorporateDataFromChildren_ifNotPerformedPrevisously();
	}
	
	public void incorporateDataFromChildren_ifNotPerformedPrevisously() {
		if ( ! dataHasBeenIncorporatedFromChildren) {
			incorporateDataFromChildren();
		}
	}

	private double computeSimilarityUsingAveOfSuffixAndStem() {
		double suffixIntersectionOverUnion = 
			computeChildSimilarity(
					childWithLargerInternalSimilarity.coveredAffixes, 
					childWithSmallerInternalSimilarity.coveredAffixes);
		double stemIntersectionOverUnion =
			computeChildSimilarity(
					childWithLargerInternalSimilarity.coveredStems, 
					childWithSmallerInternalSimilarity.coveredStems);
		
		double average = ( suffixIntersectionOverUnion + stemIntersectionOverUnion ) / 2;
		return average;
	}
	
	private <X> Double computeChildSimilarity(Set<X> setA, Set<X> setB) {
		switch (parameterSetting.getCalculateSimilarityAs()) {
		case INTERSECTION_OVER_UNION:
			return calculateIntersectionOverUnion(setA, setB); 
		case COSINE:
			return calculateCosine(setA, setB);
		}
		return null;
	}

	private <X>	double calculateCosine(Set<X> setA, Set<X> setB) {
		Set<X> smaller;
		Set<X> larger;
		if (setA.size() < setB.size()) {
			smaller = setA;
			larger = setB;
		} else {
			smaller = setB;
			larger = setA;
		}
		Set<X> intersection = new HashSet<X>(smaller);
		intersection.retainAll(larger);
		
		double numerator = intersection.size();
		
		double denominator = setA.size() * setB.size();
		denominator = Math.sqrt(denominator);
		
		double cosine = numerator / denominator;
		
		return cosine;
	}
	
	/**
	 * @param that
	 * @return
	 */
	private <X> double calculateIntersectionOverUnion(Set<X> setA, Set<X> setB) {
		Set<X> intersection = new TreeSet<X>(setA);
		intersection.retainAll(setB);
		
		Set<X> union = new TreeSet<X>(setA);
		union.addAll(setB);
		
		double numberOfCoveredTypesInIntersection = intersection.size();
		double numberOfCoveredTypesInUnion  = union.size();
		
		double intersectionOverUnion = 
			numberOfCoveredTypesInIntersection / numberOfCoveredTypesInUnion;
				
		return intersectionOverUnion;
	}
	
	public List<BottomUpSearchResultCluster> getLeaves() {
		List<BottomUpSearchResultCluster> leaves = new ArrayList<BottomUpSearchResultCluster>();
		
		if ((childWithLargerInternalSimilarity == null) && (childWithSmallerInternalSimilarity == null)) {
			leaves.add(this);
		}
		if (childWithLargerInternalSimilarity != null) {
			leaves.addAll(childWithLargerInternalSimilarity.getLeaves());
		}
		if (childWithSmallerInternalSimilarity != null) {
			leaves.addAll(childWithSmallerInternalSimilarity.getLeaves());
		}
		
		return leaves;
	}

	public Set<Affix> getCoveredAffixes() {
		return coveredAffixes;
	}
	
	public Set<String> getCoveredTypes() {
		return coveredTypes;
	}
	
	public Set<ComparablePair<Affix, Context>> getCoveredAffixStemPairs() {
		return coveredAffixStemPairs;
	}

	public double getAveLeafLevel() {
		return aveLeafLevel;
	}
	
	public double getAveLeftwardEntropyOfLeaves() {
		return aveLeftwardEntropyOfLeaves;
	}
	
	public double getAveTypesCoveredByLeaves() {
		int sumOfTypesCoveredInLeaves = 0;
		List<BottomUpSearchResultCluster> leaves = getLeaves();
		for (BottomUpSearchResultCluster leaf : leaves) {
			sumOfTypesCoveredInLeaves += leaf.getCoveredTypes().size();
		}
		
		double aveTypesCoveredByLeaves = (double)sumOfTypesCoveredInLeaves / (double)leaves.size();
		
		return aveTypesCoveredByLeaves;
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		
		if ( ! dataHasBeenIncorporatedFromChildren) {
			incorporateDataFromChildren();
		}
		
		toReturn += toStringRecursive(new ArrayList<Boolean>());
		
		return toReturn;
	}

	private String toStringRecursive(ArrayList<Boolean> recursionLevelActivity) {
		String toReturn = "";

		// If there are children, print the children's similarity
		// In either case get ready to print the affixes
		if (childWithLargerInternalSimilarity != null) {
			toReturn += recursionDepthPad(recursionLevelActivity);
			toReturn += String.format(">- Children's similarity: %10.9f%n", childrenSimilarity);  
			toReturn += recursionDepthPad(recursionLevelActivity);	
			toReturn += String.format(" | ");
		} else {
			toReturn += recursionDepthPad(recursionLevelActivity);
			toReturn += String.format(">- ");  
		}
		
		SetOfMorphemes<Affix> coveredAffixesAsSetOfMorphemes = new SetOfMorphemes<Affix>(coveredAffixes);
		toReturn += String.format(   "Affixes (" + coveredAffixes.size() + "): " + 
								  coveredAffixesAsSetOfMorphemes + "%n");
		
		toReturn += recursionDepthPad(recursionLevelActivity);
		SetOfMorphemes<Context> coveredStemsAsSetOfMorphemes = new SetOfMorphemes<Context>(coveredStems);
		toReturn += String.format(" | Stems   (" + coveredStems.size() + "): " + 
								  coveredStemsAsSetOfMorphemes.toShortString(30) + "%n");
		
		toReturn += recursionDepthPad(recursionLevelActivity);
		toReturn += String.format(" | Types   (" + coveredTypes.size() + ")%n");
		
		toReturn += recursionDepthPad(recursionLevelActivity);
		
		// Connect this node to the children (if there are any)
		if (childWithLargerInternalSimilarity != null) {
			toReturn += " |";
		}
		
		toReturn += String.format("%n");
			
		// If there is a child print recursively
		if (childWithLargerInternalSimilarity != null) {
			
			recursionLevelActivity.add(true);
			toReturn += childWithLargerInternalSimilarity.toStringRecursive(recursionLevelActivity);
			
			recursionLevelActivity.set(recursionLevelActivity.size()-1, false);
			toReturn += childWithSmallerInternalSimilarity.toStringRecursive(recursionLevelActivity);
			
			recursionLevelActivity.remove(recursionLevelActivity.size()-1);
		}
		
		return toReturn;
	}

	private String recursionDepthPad(ArrayList<Boolean> recursionLevelActivity) {
		String toReturn = "";
		for (Boolean levelIsActive : recursionLevelActivity) {
			if (levelIsActive) {
				toReturn += " |";
			} else {
				toReturn += "  ";
			}
		}
		return toReturn;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		if ((childWithLargerInternalSimilarity == null) && (childWithSmallerInternalSimilarity == null)) {
			hashCode += coveredAffixes.hashCode()*31 + coveredStems.hashCode();
			return hashCode;
		}
		
		if (childWithLargerInternalSimilarity != null) {
			hashCode += childWithLargerInternalSimilarity.hashCode();
		}
		if (childWithSmallerInternalSimilarity != null) {
			hashCode = 31*hashCode + childWithSmallerInternalSimilarity.hashCode();
		}
		
		return hashCode;
	}




}
