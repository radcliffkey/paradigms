/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.menus.SelectAnEnumValueMenu;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;

/**
 * This class inherits from BottomUp_Search_Abstract, but in many ways it follows the pattern of the
 * classes in the menu package.  In particular, the command prompt styles are the same, the
 * basic control flow is modeled on the menu package classes, etc.
 * 
 * @author cmonson
 *
 */
public class BottomUpSearchInteractive {

	VirtualPartialOrderNetwork partialOrderNetwork;
	
	BufferedReader stdin;
	
	// A list of level 1 nodes from which to begin a search path
	ArrayList<Level1Scheme> candidateSeeds = null;
	
	HashMap<Affix, Integer> parentAffixesAndSizes = null;
	
	Scheme previousCurrent = null;
	Scheme current = null;
	
	// Keeping track of this variable helps VirtualPartialOrderNetwork
	Set<Scheme> mostSpecificSchemeAncestors = null;

	private TreeMap<Integer, HashSet<Affix>> parentSizeToSetOfAffixes = null;
	
	
	// BottomUp_Search_Interactive, at least for now only allows searching
	// over PartialOrderNetwork_SubNetOnDemand networks because
	// Calculating the set of 'covered to uncovered' links gets difficult 
	// and ill-defined if not all the nodes are present in the network. 
	public 
	BottomUpSearchInteractive(
			VirtualPartialOrderNetwork partialOrderNetwork, 
			BufferedReader stdin) {
		
		this.partialOrderNetwork = partialOrderNetwork;
		this.stdin = stdin;
	}

	public void search() {
		
		// current == null when we are at the bottom of the network
		Scheme current;
		
		SearchPath pathToSelectedScheme;
		
		boolean growNewSearchPath = true; 
		
		while (growNewSearchPath) {
			
			// 1) Pick a new seed at the bottom of the network
			//
			// pickNewSeed will return null when we do not wan't to grow any new seed
			//
			pathToSelectedScheme = new SearchPath();
			
			current = pickNewSearchSeed();
			if (current == null) {
				growNewSearchPath = false;
			} else {
				System.err.println();
				System.err.println("---------------------------------");
				System.err.println("Starting Search from the Level 1 Seed:");
				System.err.println(current.toPrettyString(30));
				System.err.println();
				System.err.println();
			}
			
			
			// 2) Grow the seed up until we decide to stop
			//
			// growSeed will return null when we should stop growing a seed
			//
			while (current != null) {
				// We want to add current to the covered list and to the
				// explored path exactly when current is not null--irregardless
				// of whether or not we are just starting this path or continuing
				// in onwards.
				pathToSelectedScheme.add(current);
				
				current = growSearchPath(current);
			}
		}
	}
	
	// pickNewSeed will return null when we do not want to grow any new seed
	protected Scheme pickNewSearchSeed() {

		// Sort the seeds (takes a while
		getCandidateSeedsByAdherentSize();

		System.err.println();
		System.err.println("Select a new seed to begin growing:");
		System.err.println();
		
		Scheme newSeed = null;
		
		for (int candidateSeedsIndex = 0;
				 candidateSeedsIndex < candidateSeeds.size();
				 candidateSeedsIndex++) {
			newSeed = candidateSeeds.get(candidateSeedsIndex);
			
			System.err.println();
			System.err.println("----------------------------------------");
			System.err.println("<" + candidateSeedsIndex + ">");
			printCandidateNewGrowth(newSeed, null);
			
			if (((candidateSeedsIndex+1) % 1) == 0) {
				Integer selectedCandidateSeedsIndex = selectAGrowthInteractively(candidateSeeds);
				
				// As soon as a candidate Seed is selected, 
				// stop listing more candidates to choose from.
				if (selectedCandidateSeedsIndex >= 0) {
					return candidateSeeds.get(selectedCandidateSeedsIndex);
				}
				if (selectedCandidateSeedsIndex == -2) {
					return null;
				}
			}
		}
		
		// if they haven't selected a growth yet.  Now's their last chance
		boolean userReallyWantsToNotSelectAnySeed = false;
		while ( ! userReallyWantsToNotSelectAnySeed) {
			Integer selectedCandidateSeedsIndex = selectAGrowthInteractively(candidateSeeds);
			if (selectedCandidateSeedsIndex >= 0) {
				return candidateSeeds.get(selectedCandidateSeedsIndex);
			}
			if (selectedCandidateSeedsIndex == -2) {
				return null;
			}
			System.err.println();
			System.err.println("There are no more seeds to view.")    ;
			System.err.println("  You must either select an available seed or");
			System.err.println("  explicitly stop growing seeds");
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
		}
				
		return null;
	}

	// growSeed will return null when we should stop growing a seed
	protected Scheme growSearchPath(Scheme current) {
		printDetailedNodeForInteraction(current);
		System.err.println();
		System.err.println("Select a growth node:");
		
		List<Scheme> uncoveredParents = new ArrayList<Scheme>();
		
		
		
		// 'n' is for nth largest parent
		int n = 0;
		int uncoveredParentsBySizeIndex = -1;
		
		
		// Calculating the total number of parents can be costly if things are not cached.  
		// (which they won't be to save memory from exploding.) And we don't really need 
		// to check outside the loop if there are any more decent parents. If there arn't 
		// then getNthBestParentBy() will just return null and we know we are at the end
		//
		//int totalNumberOfParents = partialOrderNetwork.getNumberOfParents(current);
		//while (n < totalNumberOfParents) {
		while(true) {
			
			n++;
			
			Scheme nthParent = getNthLargestParentByAdherents(current, n);
			
			
			System.err.println();
			System.err.println("The current scheme has " + parentAffixesAndSizes.size() + " parents");
			
			if (nthParent == null) {
				break;
			}
			
			uncoveredParentsBySizeIndex++;
			uncoveredParents.add(nthParent);
			
			SetOfMorphemes<Affix> newSuffix = 
				nthParent.getAffixes().minus(current.getAffixes()); 
			
			System.err.println();
			System.err.println("----------------------------------------");
			System.err.println("<" + uncoveredParentsBySizeIndex + "> " + newSuffix);
			printCandidateNewGrowth(nthParent, current);
			Integer selectedUncoveredParentIndex = 
				selectAGrowthInteractively(uncoveredParents);
		
			// As soon as a parent is selected, 
			// stop listing more candidates to choose from.
			if (selectedUncoveredParentIndex >= 0) {
				Scheme selectedUncoveredParent = 
					uncoveredParents.get(selectedUncoveredParentIndex);
				printSelectedParentsVerticalScores(current, selectedUncoveredParent);
				return selectedUncoveredParent;
			}
			if (selectedUncoveredParentIndex == -2) {
				return null;
			}
			// change direction of growth, up to down
			if (selectedUncoveredParentIndex == -3) {  
				return growSeedDown(current);
			}
			if (selectedUncoveredParentIndex == -4) {
				printAMetricToAParent(current, nthParent);
			}
		}
		
		// if they haven't selected a parent yet.  Now's their last chance
		while (true) {
			Integer selectedUncoveredParentIndex = 
				selectAGrowthInteractively(uncoveredParents);
			
			if (selectedUncoveredParentIndex >= 0) {
				return uncoveredParents.get(selectedUncoveredParentIndex);
			}
			if (selectedUncoveredParentIndex == -2) {
				return null;
			}
			// change direction of growth, up to down
			if (selectedUncoveredParentIndex == -3) {  
				return growSeedDown(current);
			}
			
			System.err.println();
			System.err.println("There are no more parents to view.");
			System.err.println("  You must either select an available parent or");
			System.err.println("  explicitly stop growing this seed");
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
		}
	}
	
	// This method was hastily written in March 2008 as I tried to finish up
	// my thesis.
	private void printSelectedParentsVerticalScores(Scheme localCurrent, Scheme selectedUncoveredParent) {
		VerticalDecision verticalDecision =
			new VerticalDecision(partialOrderNetwork, localCurrent, selectedUncoveredParent);
		
		Double ratio = verticalDecision.calculate(VerticalMetric.RATIO);
		Double dice  = verticalDecision.calculate(VerticalMetric.DICE);
		Double mutualInformation = verticalDecision.calculate(VerticalMetric.POINTWISE_MUTUAL_INFORMATION);
		Double chiSquared        = verticalDecision.calculate(VerticalMetric.PEARSONS_CHI_SQUARED_TEST);
		Double chiSquaredOneSided = verticalDecision.calculate(VerticalMetric.PEARSONS_CHI_SQUARED_TEST_ONE_SIDED);
		Double estimateOfVariance = verticalDecision.calculate(VerticalMetric.LARGE_SAMPLE_BERNOULLI_TEST);
		Double likelihoodRatio    = verticalDecision.calculate(VerticalMetric.CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI);
		Double likelihoodRatioOneSided = verticalDecision.calculate(VerticalMetric.CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED);
		
		System.err.println(verticalDecision);
		System.err.println();
		System.err.println("And the following vertical metric scores");
		System.err.println();
		System.err.println(" Ratio: " + ratio);
		System.err.println(" Dice:  " + dice);
		System.err.println(" MI:    " + mutualInformation);
		System.err.println(" Chi^2: " + chiSquared + " (" + chiSquaredOneSided + ")");
		System.err.println(" Var-hat: " + estimateOfVariance);
		System.err.println(" Likelihoo Ratio: " + likelihoodRatio + " (" + likelihoodRatioOneSided + ")");
		System.err.println();
		
	}

	public Scheme getNthLargestParentByAdherents(Scheme scheme, int n) {
		if ((current == null) || ( ! scheme.equals(current))) {
			current = scheme;
			getParents(scheme);
		}
		
        // Since we couldn't fully sort the affixes by the size of the parent
		// Scheme they create (because sorting would back off to lexicographic
		// sorting of affixes which takes too long), we must:
		//  First, find the size of the Nth parent, and
		//  Second, sort just those affixes that form parents of the size
		//    that the Nth parent has, and then get the Nth parent
		
		// Find the size of the Nth parent
		int numberOfParentsWithSizeGTParentSize = 0;  // NOTE: 'GT' 
		int numberOfParentsWithSizeGEParentSize = 0;  // NOTE: 'GE'
		int sizeOfNthParent = 0;
		for (Integer parentSize : parentSizeToSetOfAffixes.keySet()) {
			numberOfParentsWithSizeGTParentSize = numberOfParentsWithSizeGEParentSize;
			numberOfParentsWithSizeGEParentSize += 
				parentSizeToSetOfAffixes.get(parentSize).size();
			if (numberOfParentsWithSizeGEParentSize >= n) {
				sizeOfNthParent = parentSize;
				break;
			}
		}
		
		// if 'n' is greater than the number of affixes that form parents
		// then there is no Nth largest parent, so return null.
		if (sizeOfNthParent == 0 ){
			return null;
		}
		
		// Sort just those parents of the 'current' Scheme that have the right size
		ArrayList<Affix> parentsAtASize = 
			new ArrayList<Affix>(parentSizeToSetOfAffixes.get(sizeOfNthParent));
		Collections.sort(parentsAtASize);
		
		// Find the index of the nth parent into the list of parents that have the 
		// right size careful of off by 1
		int indexIntoParentsAtASize = n - numberOfParentsWithSizeGTParentSize - 1;
		
		// Get the Nth parent forming affix
		Affix affixFormingNthLargestParent = parentsAtASize.get(indexIntoParentsAtASize);
		
		// Form the Nth parent to be returned.
		SetOfMorphemes<Affix> affixesOfParent = new SetOfMorphemes<Affix>(current.level() + 1);
		affixesOfParent.add(current.getAffixes());
		affixesOfParent.add(affixFormingNthLargestParent);
		Scheme nthLargestParent = 
			partialOrderNetwork.generateScheme(affixesOfParent);
		
		return nthLargestParent;
	}
	
	private void getParents(Scheme scheme) {
		// Getting the most specific Scheme ancestors is *not* integrated
		// into the same function that actually finds the parents because
		// updating ancestors is (probably) often much faster than finding 
		// them from scratch.
		mostSpecificSchemeAncestors = 
			partialOrderNetwork.getMostSpecificSchemeAncestors(
					current,
					previousCurrent,
					mostSpecificSchemeAncestors);
		
		parentAffixesAndSizes =
			partialOrderNetwork.getParentAffixesWithAdherentSizes(
					current, 
					mostSpecificSchemeAncestors);
		
		calculateParentSizeToSetOfAffixes(parentAffixesAndSizes);
	}
	
	// Convert 'affixToParentSize' into 'parentSizeToSetOfAffixes'.
	// We need the affixes sorted by the size of the parent scheme they form,
	// but, unless we absolutely have to, we don't want to sort the different 
	// affixes that produce parents that are the same size (performing this 
	// unnecessary sort significantly impacted runtime).  Hence, we use a 
	// sorted TreeMap to sort the affixes only with respect to the size of 
	// the parent Scheme they form.		
	private void calculateParentSizeToSetOfAffixes(Map<Affix, Integer> affixToParentSize) {
	
		int DEBUG = 0;

		class DecreasingComparitor implements Comparator<Integer>, Serializable {

			private static final long serialVersionUID = 1L;

			public int compare(Integer a, Integer b) {
				return b.compareTo(a);
			}

		}

		parentSizeToSetOfAffixes = 
			new TreeMap<Integer, HashSet<Affix>>(new DecreasingComparitor());

		for (Affix parentAffix : affixToParentSize.keySet()) {
			Integer parentSize = affixToParentSize.get(parentAffix);
			if ( ! parentSizeToSetOfAffixes.containsKey(parentSize)) {
				parentSizeToSetOfAffixes.put(parentSize, new HashSet<Affix>());
			}
			HashSet<Affix> affixesAtSize = parentSizeToSetOfAffixes.get(parentSize);
			affixesAtSize.add(parentAffix);
		}

		if (DEBUG > 0) {
			System.err.println();
			System.err.println("**********************************************");
			System.err.println("  Done Calculating adherent sizes of all parents");
			System.err.println("**********************************************");
		}
	}

	private void printAMetricToAParent(Scheme current, Scheme parent) {
		SelectAnEnumValueMenu<VerticalMetric> selectAnEnumValueMenu =
			new SelectAnEnumValueMenu<VerticalMetric>(
					VerticalMetric.class,
					stdin,
					"Print A Metric",
					"Select a vertical metric to evaluate between the current child and parent");
		
		VerticalMetric verticalMetric = selectAnEnumValueMenu.present();
		
		VerticalDecision verticalDecision = 
			new VerticalDecision(partialOrderNetwork, current, parent);
		
		Double metricValue = verticalDecision.calculate(verticalMetric);
		
		System.err.println();
		System.err.println(verticalDecision);
		System.err.println();
		System.err.println(verticalMetric + " : " + metricValue);
		System.err.println();
	}

	protected void getCandidateSeedsByAdherentSize() {
		
		// Return cached
		if (candidateSeeds != null) {
			return;
		}
		
		System.err.println();
		System.err.println("Sorting the candidate seeds...");
		System.err.println();
		
		TreeSet<Level1Scheme> candidateSeedsOrderedSet = 
			new TreeSet<Level1Scheme>(new Scheme.ByDecreasingAdherentSize());
		
		candidateSeedsOrderedSet.addAll(partialOrderNetwork.getSmallestSchemesAboveLevel0());
		
		candidateSeeds = new ArrayList<Level1Scheme>(candidateSeedsOrderedSet);
	}

	/**
	 * @param current
	 */
	private void printDetailedNodeForInteraction(Scheme current) {
		System.err.println();
		System.err.println("Current seed's growth head node:");
		System.err.println(current.toPrettyString(30000));
		System.err.println();
		System.err.println(current.getLeftwardSummaryString());
		System.err.println();
	}

	// For now unlike growSeed(), growSeedDown() is not abstract enough to potentially
	// dinamically grow the network.  I assume, as is true for Dynamic Dense networks,
	// that if a node is present in the network, then all of its children are in the
	// network too.
	protected Scheme growSeedDown(Scheme current) {
		printDetailedNodeForInteraction(current);
		System.err.println();
		System.err.println("Select a (LOWER) growth node:");
		
		List<Scheme> children = new ArrayList<Scheme>();
		
		// 'n' is for nth largest parent
		int n = 0;
		int childrenBySizeIndex = -1;
		
		children.addAll(partialOrderNetwork.getSmallers(current));
		
		for (Scheme child : children) {
			n++;
					
			childrenBySizeIndex++;
			
			System.err.println();
			System.err.println("----------------------------------------");
			System.err.println("<" + childrenBySizeIndex + ">");
			System.err.println();
			System.err.println("FROM CHILD:");
			System.err.println(child.toPrettyString(300));
			printCandidateNewGrowth(current, child);
			Integer selectedChildIndex = selectAGrowthInteractively(children);
		
			// As soon as a parent is selected, 
			// stop listing more candidates to choose from.
			if (selectedChildIndex >= 0) {
				return children.get(selectedChildIndex);
			}
			if (selectedChildIndex == -2) {
				return null;
			}
			if (selectedChildIndex == -3) {
				return growSearchPath(current);  // Change the direction of growth up vs. down
			}

		}
		
		// if they haven't selected a child yet.  Now's their last chance
		boolean userReallyWantsToNotSelectAnyChild = false;
		while ( ! userReallyWantsToNotSelectAnyChild) {
			Integer selectedChildIndex = selectAGrowthInteractively(children);
			if (selectedChildIndex >= 0) {
				return children.get(selectedChildIndex);
			}
			if (selectedChildIndex == -2) {
				return null;
			}
			if (selectedChildIndex == -3) {
				return growSearchPath(current);  // Change the direction of growth up vs. down
			}
			System.err.println();
			System.err.println("There are no more children to view.");
			System.err.println("  You must either select an available child or");
			System.err.println("  explicitly stop growing this seed");
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
		}
				
		return null;

	}

	/**
	 * 
	 * @param candidateSeeds
	 * @param currentSeedIndex2
	 * @return <code>null</code> when we should not grow a seed any further
	 */
	
	// returns: integer >= 0 if the user actually selected a node
	//          -1           if the user wants to see more options
	//          -2           if the user wants to explicitly not choose any growth
	//			-3			 if the user wants to grown children down instead of parents up.
	//          null         if there was some error in which none of the above applied
	//
	// The PartialOrderNode that is interactively selected is directly modified in the
	// growthToReturn parameter.
	private Integer selectAGrowthInteractively(List<? extends Scheme> weededCandidateGrowths) {
		
		boolean successfullySelected = false;
		while ( ! successfullySelected) {
			
			System.err.println();
			System.err.println("  <#> to select a growth/seed.");
			System.err.println("  <C>hange direction of growth, up to down or down to up.");
			System.err.println("  <S>top growing the current seed *OR*");
			System.err.println("     if you are currently selecting a seed, stop selecting new seeds to grow");
			System.err.println("  <M>etric -- evaluate a vertical metric to the currently displayed parent");
			System.err.println("  [Return] to be presented with more candidate growths (if there are any)");
			System.err.println();
			System.err.print("> ");
			
			String choice;
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			if (choice.matches("s|S")) {
				System.err.println();
				System.err.println("Stop growing this seed <y|n> ?");
				System.err.println();
				System.err.print("> ");
				try {
					choice = stdin.readLine();
				}
				catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
				if (choice.matches("y|Y")) {
					return -2;
				}
				// Next iteration if the user accidentally hit 's'
				continue;
			}
			
			if (choice.matches("c|C")) {
				return -3; // -3 means grow down instead of up
			}
			
			if (choice.matches("m|M")) {
				return -4;
			}
			
			// Request additional choices
			if (choice.matches("\\s*")) {
				return -1;
			}
			
			int weededCandidateGrowthsIndex;
			try {
				weededCandidateGrowthsIndex = Integer.parseInt(choice);
			}
			catch (NumberFormatException e) {
				System.err.println();
				System.err.println("Please Enter an integer index.  [Press Enter...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
			

			try {  // make sure the index they chose is valid
				weededCandidateGrowths.get(weededCandidateGrowthsIndex);
				return weededCandidateGrowthsIndex;
			}
			catch (IndexOutOfBoundsException e) {
				System.err.println();
				System.err.println("Please select a growth node between the indexes 0 and " +
						(weededCandidateGrowths.size()-1));
				System.err.println();
				System.err.println("[Press Enter...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
		}

		// should never get here because we always either 'return' or 'continue'.
		assert(false);
		return null;
	}

	private void printCandidateNewGrowth(Scheme candidateNewGrowth, 
										 Scheme headOfCurrentGrowth) {
		
		System.err.println(candidateNewGrowth.toPrettyString(30));
		
		if (headOfCurrentGrowth != null) {
			
			// VerticalDecisions were meant to use StaticSparse Partial Order Nodes, so
			// I don't trust them here.  And now I am using Schemes anyway, not even
			// PartialOrderNodes
			//
			//VerticalDecision verticalDecision = 
			//	new VerticalDecision(partialOrderNetwork, headOfCurrentGrowth, candidateNewGrowth);
			//Double ratio = verticalDecision.calculate(VerticalMetric.RATIO);

			Double stemRatio = ((double)candidateNewGrowth.getContexts().size()) /
						       ((double)headOfCurrentGrowth.getContexts().size());
			double typeRatio = ((double)(candidateNewGrowth.getContexts().size() * candidateNewGrowth.getAffixes().size())) /
							   ((double)(headOfCurrentGrowth.getContexts().size() * headOfCurrentGrowth.getAffixes().size()));
			
			System.err.println("Context Ratio: " + stemRatio);
			System.err.println("Type Ratio: " + typeRatio);
			
			// This code no longer works now that I am using Schemes and not PartialOrderNodes
			// and I wasn't using this code anyway because SubNetOnDemand networks are impractical.
			//if (partialOrderNetwork instanceof PartialOrderNetwork_SubNetOnDemand) {
			//	Set<Double> coveredToUncoveredRatios_Set = 
			//		((PartialOrderNetwork_SubNetOnDemand)partialOrderNetwork).
			//			getCoveredToUncoveredRatios(headOfCurrentGrowth, candidateNewGrowth);
			//	
			//	ListOfData<Double> coveredToUncoveredRatios = 
			//		new ListOfData<Double>(coveredToUncoveredRatios_Set);
			//
			//	System.err.println("Covered to Uncovered Ratios - Summary");
			//	System.err.println(coveredToUncoveredRatios.getPrettySummary());
			//}
		}
		
	}

}
