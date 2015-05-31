/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.morphology.SeedAnalyses;
import klic.radoslav.morphology.SeedAnalysesParser;
import klic.radoslav.morphology.schemes.SeedScheme;
import klic.radoslav.util.DebugLog;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.util.ComparablePair;

public class BottomUpSearch implements SearchStep {
	
	public enum StartFrom   {ALL_LEVEL_1, ORACLE}
	public enum SelectWhich {ALL_ON_PATH, TERMINAL_ONLY}

	
	public static class BottomUpParameters extends SearchStepParameters {
		
		private static final long serialVersionUID = 1L;
		
		// non-multiplied parameters--In any particular batch run I only allow one setting
		// of these parameters.
		private StartFrom 	   startFrom;
		private VerticalMetric verticalMetric;
		
		// Will not start from bottom schemes with MORE than bottomMax stems
		private Integer		   bottomMax;   
		
		private boolean		   exhaustiveSearchAtLevel1;
		private boolean		   useCovered;
		private SelectWhich	   selectWhich;
		
		private boolean		   requireMoreStemsThanAffixes;
		
		// multiplied parameters--Each possible combination of these parameters is tried
		private List<Double>  verticalMetricCutoffs   = new ArrayList<Double>();
		private List<Integer> topCutoffs 			  = new ArrayList<Integer>();
		private List<Integer> bottomCutoffs			  = new ArrayList<Integer>();
		private List<Integer> maxLevelCutoffs 		  = new ArrayList<Integer>();
		
		
		public BottomUpParameters() {
			// Defaults
			startFrom      = StartFrom.ALL_LEVEL_1;
			verticalMetric = VerticalMetric.RATIO;
			
			// null means bottomMax is not used--no upper limit on 
			//the size of the bottom schemes to start from
			bottomMax                = null;  
			
			exhaustiveSearchAtLevel1 = false;
			useCovered	             = false;
			selectWhich 		     = SelectWhich.TERMINAL_ONLY;
			
			requireMoreStemsThanAffixes = true;
			
			verticalMetricCutoffs.add(0.25);
			bottomCutoffs.add(3); // Default to 3, to save time when require more stems than affixes is on
			topCutoffs.add(0);
			maxLevelCutoffs.add(null);  // The max level cutoff kicks in if the value gets too high
										// so where the other cutoffs have a natural lower bound (0)
										// maxLevelCutoffs does not.  Hence I use null as a default.
		}
	
		public void setStartFrom(StartFrom startFrom) {
			this.startFrom = startFrom;
		}
		
		public void setVerticalMetric(VerticalMetric verticalMetric) {
			this.verticalMetric = verticalMetric;
		}
		
		/** 
		 * pass in <code>null</code> to specify that bottomMax NOT be used
		 * Bottom max is an UPPER limit to the size of the bottom schemes that
		 * will be searched from.  We will not start from bottom schemes with 
		 * MORE than bottomMax stems
		 * 
		 * @param bottomMax
		 */
		public void setBottomMax(Integer bottomMax) {
			this.bottomMax = bottomMax;
		}
	
		public void setVerticalMetricCutoffs(List<Double> verticalMetricCutoffs) {
			this.verticalMetricCutoffs = verticalMetricCutoffs;
		}
		
		public void setBottomCutoffs(List<Integer> bottomCutoffs) {
			this.bottomCutoffs = bottomCutoffs;
		}
	
		public void setTopCutoffs(List<Integer> topCutoffs) {
			this.topCutoffs = topCutoffs;
		}
		
		public void setMaxLevelCutoffs(List<Integer> maxLevelCutoffs) {
			this.maxLevelCutoffs = maxLevelCutoffs;
		}
		
		public void setExhaustiveSearchAtLevel1(boolean exhaustiveSearchAtLevel1) {
			this.exhaustiveSearchAtLevel1 = exhaustiveSearchAtLevel1;
		}

		public void setSelectWhich(SelectWhich selectWhich) {
			this.selectWhich = selectWhich;
		}

		public void setUseCovered(boolean useCovered) {
			this.useCovered = useCovered;
		}

		public void setRequireMoreStemsThanAffixes(boolean requireMoreStemsThanAffixes) {
			this.requireMoreStemsThanAffixes = requireMoreStemsThanAffixes;
		}
		

		public StartFrom getStartFrom() {
			return startFrom;
		}
	
		public VerticalMetric getVerticalMetric() {
			return verticalMetric;
		}
		
		public Integer getBottomMax() {
			return bottomMax;
		}
		
		public List<Double> getVerticalMetricCutoffs() {
			return verticalMetricCutoffs;
		}
	
		public List<Integer> getTopCutoffs() {
			return topCutoffs;
		}
		
		public List<Integer> getBottomCutoffs() {
			return bottomCutoffs;
		}
		
		public List<Integer> getMaxLevelCutoffs() {
			return maxLevelCutoffs;
		}
		
		public boolean getExhaustiveSearchAtLevel1() {
			return exhaustiveSearchAtLevel1;
		}
		
		public SelectWhich getSelectWhich() {
			return selectWhich;
		}
		
		public boolean getUseCovered() {
			return useCovered;
		}
	
		public boolean getRequireMoreStemsThanAffixes() {
			return requireMoreStemsThanAffixes;
		}

		
		@Override
		protected String getParametersString() {
			String toReturn = "";
			toReturn += String.format("Starting From:                %s%n", startFrom);
			toReturn += String.format("Vertical Metric:              %s%n", verticalMetric);
			
			toReturn += String.format("Bottom Max:                   ");
			if (bottomMax == null) {
				toReturn += String.format("NONE%n");
			} else {
				toReturn += String.format(bottomMax + "%n");
			}
						
			toReturn += String.format("Exhaustive Search at Level 1: %s%n", exhaustiveSearchAtLevel1);
			toReturn += String.format("Use Covered:                  %s%n", useCovered);
			toReturn += String.format("Select Which?:                %s%n", selectWhich);
			toReturn += String.format("Require |Stems| > |Affixes|:  %s%n", requireMoreStemsThanAffixes);
			toReturn += String.format("Vertical Metric Cutoffs:      %s%n", verticalMetricCutoffs);
			toReturn += String.format("Top Cutoffs:                  %s%n", topCutoffs);
			toReturn += String.format("Bottom Cutoffs:               %s%n", bottomCutoffs);
			toReturn += String.format("Max Level Cutoffs:            %s%n", maxLevelCutoffs);
			return toReturn;
		}
		
		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			toReturn += String.format("# Starting From:                %s%n", startFrom);
			toReturn += String.format("# Vertical Metric:              %s%n", verticalMetric);
			
			toReturn += String.format("# Bottom Max:                   ");
			if (bottomMax == null) {
				toReturn += String.format("NONE%n");
			} else {
				toReturn += String.format(bottomMax + "%n");
			}
			
			toReturn += String.format("# Exhaustive Search at Level 1: %s%n", exhaustiveSearchAtLevel1);
			toReturn += String.format("# Use Covered:                  %s%n", useCovered);
			toReturn += String.format("# Select Which?:                %s%n", selectWhich);
			toReturn += String.format("# Require |Stems| > |Affixes|:  %s%n", requireMoreStemsThanAffixes);
			toReturn += String.format("# Vertical Metric Cutoffs:      %s%n", verticalMetricCutoffs);
			toReturn += String.format("# Top Cutoffs:                  %s%n", topCutoffs);
			toReturn += String.format("# Bottom Cutoffs:               %s%n", bottomCutoffs);
			toReturn += String.format("# Max Level Cutoffs:            %s%n", maxLevelCutoffs);
			return toReturn;
		}


		public Iterator<SearchStepParameterSetting> iterator() {
			List<SearchStepParameterSetting> allParameterSettings = 
				new ArrayList<SearchStepParameterSetting>();

			BottomUpParameterSetting parameterSetting;
			
			for (Double verticalMetricCutoff    : verticalMetricCutoffs)   {
			 for (Integer topCutoff              : topCutoffs)              {
			  for (Integer bottomCutoff		      : bottomCutoffs)			 {
			   for (Integer maxLevelCutoff         : maxLevelCutoffs)         {
						   
				   parameterSetting = new BottomUpParameterSetting(startFrom,
							   									   verticalMetric,
							   									   bottomMax,
							   									   exhaustiveSearchAtLevel1,
							   									   useCovered,
							   									   selectWhich,
							   									   requireMoreStemsThanAffixes,
							   									   verticalMetricCutoff, 
							   									   topCutoff,
							   									   bottomCutoff,
							   									   maxLevelCutoff);
				   allParameterSettings.add(parameterSetting);
			    
			   }
			  }
			 }
			}
						   
			return allParameterSettings.iterator();
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Start From, Vertical Metric, Bottom Max, Exhaustive Search at Level 1, Use Covered, Select Which?, Require |Stems| > |Affixes|, Vertical Cutoff, Top Cutoff, Bottom Cutoff, Max Level Cutoff";
		}

	}
	
	
	public static class BottomUpParameterSetting extends SearchStepParameterSetting {
		
		private static final long serialVersionUID = 1L;
		
		private StartFrom      startFrom                   = StartFrom.ALL_LEVEL_1;
		private VerticalMetric verticalMetric              = VerticalMetric.RATIO;
		private Integer        bottomMax                   = null;
		private boolean		   exhaustiveSearchAtLevel1    = false;
		private boolean		   useCovered		  	       = false;
		private SelectWhich    selectWhich                 = SelectWhich.TERMINAL_ONLY;
		private boolean		   requireMoreStemsThanAffixes = true;
		private Double         verticalMetricCutoff        = 0.25;
		private Integer        topCutoff                   = 0;
		private Integer        bottomCutoff		           = 3;
		private Integer        maxLevelCutoff              = null;
		
		// Only locally can we create a default BottomUpParameterSetting
		private BottomUpParameterSetting() {
			associatedSearchStep = BottomUpSearch.class;
		}
		
		public BottomUpParameterSetting(
				StartFrom      startFrom,
				VerticalMetric verticalMetric, 
				Integer        bottomMax,
				boolean		   exhaustiveSearchAtLevel1,
				boolean		   useCovered,
				SelectWhich    selectWhich,
				boolean		   requireMoreStemsThanAffixes,
				Double         verticalMetricCutoff, 
				Integer        topCutoff, 
				Integer        bottomCutoff,
				Integer        maxLevelCutoff) {
			
			this();
			
			this.startFrom				     = startFrom;
			this.verticalMetric              = verticalMetric;
			this.bottomMax				     = bottomMax;
			this.exhaustiveSearchAtLevel1    = exhaustiveSearchAtLevel1;
			this.useCovered		  		     = useCovered;
			this.selectWhich                 = selectWhich;
			this.requireMoreStemsThanAffixes = requireMoreStemsThanAffixes;
			this.verticalMetricCutoff    	 = verticalMetricCutoff;
			this.topCutoff       	     	 = topCutoff;
			this.bottomCutoff    	         = bottomCutoff;
			this.maxLevelCutoff   	   		 = maxLevelCutoff;
		}
		
		public StartFrom getStartFrom() {
			return startFrom;
		}
		
		public static String getColumnTitleStringForSpreadsheet() {
			String toReturn = "";
			toReturn += "Start From, Vertical Metric, Bottom Max, " +
					"Exhaustive Search at Level 1, Use Covered, " +
					"Select Which?, Require |Stems| > |Affixes|, Vertical Metric Cutoff, Top Cutoff, Bottom Cutoff, " +
					"Max Level Cutoff";
			return toReturn;
		}
		
		public String getStringForSpreadsheet() {
			String toReturn = "";
			toReturn += startFrom                   + ", " +
						verticalMetric              + ", ";
			
			if (bottomMax == null) {
				toReturn += "NONE";
			} else {
				toReturn += bottomMax;
			}
			toReturn +=                               ", ";
			
			toReturn +=                               ", " + 
						exhaustiveSearchAtLevel1    + ", " +
						useCovered			        + ", " +
						selectWhich                 + ", " +
						requireMoreStemsThanAffixes + ", " +
						verticalMetricCutoff        + ", " + 
						topCutoff                   + ", " + 
						bottomCutoff		        + ", " +
						maxLevelCutoff;
			
			return toReturn;
		}
		
		@Override
		protected String getParameterString() {
			String toReturn = "";
			toReturn += String.format("Starting From:                %s%n", startFrom);
			toReturn += String.format("Vertical Metric:              %s%n", verticalMetric);
			
			toReturn += String.format("Bottom Max:                   ");
			if (bottomMax == null) {
				toReturn += String.format("NONE%n");
			} else {
				toReturn += String.format(bottomMax + "%n");
			}
			
			toReturn += String.format("Exhaustive Search at Level 1: %s%n", exhaustiveSearchAtLevel1);
			toReturn += String.format("Use Covered:                  %s%n", useCovered);
			toReturn += String.format("Select Which?:                %s%n", selectWhich);
			toReturn += String.format("Require |Stems| > |Affixes|:  %s%n", requireMoreStemsThanAffixes);
			toReturn += String.format("Vertical Metric Cutoff:       %s%n", verticalMetricCutoff);
			toReturn += String.format("Top Cutoff:                   %s%n", topCutoff);
			toReturn += String.format("Bottom Cutoff:                %s%n", bottomCutoff);
			toReturn += String.format("Max Level Cutoff:             %s%n", maxLevelCutoff);
			return toReturn;
		}
		
		// TODO: This does not completely uniqueify by specifying ALL the parameters.
		//       Rather, This just uniqueifies for parameters I am interested in
		//       as of April 2007
		@Override
		public String getFilenameUniqueifier() {
			String toReturn = "";
			toReturn += "R" + verticalMetricCutoff.toString();
			return toReturn;
		}
		
		public int compareTo(SearchStepParameterSetting thatSearchStepParameterSetting) {
			if ( ! (thatSearchStepParameterSetting instanceof BottomUpParameterSetting)) {
				super.compareTo(thatSearchStepParameterSetting);
			}

			BottomUpParameterSetting that = (BottomUpParameterSetting)thatSearchStepParameterSetting;
			
			if (this.startFrom != that.startFrom) {
				return this.startFrom.compareTo(that.startFrom);
			}
			
			if ( ! this.verticalMetric.equals(that.verticalMetric)) {
				return this.verticalMetric.compareTo(that.verticalMetric);
			}
			
			if ( ! ((this.bottomMax == null) && (that.bottomMax == null))) {
				if (this.bottomMax == null) {
					return -1;
				}
				if (this.bottomMax == null) {
					return 1;
				}
				if ( ! this.bottomMax.equals(that.bottomMax)) {
					if (this.bottomMax < that.bottomMax) {
						return -1;
					}
					if (this.bottomMax > that.bottomMax) {
						return 1;
					}
				}
			}
			
			if (this.exhaustiveSearchAtLevel1 != that.exhaustiveSearchAtLevel1) {
				if (this.exhaustiveSearchAtLevel1) {
					return -1;
				}
				return 1;
			}
						
			if (this.useCovered != that.useCovered) {
				if (this.useCovered) {
					return -1;
				}
				return 1;
			}
			
			if (this.selectWhich != that.selectWhich) {
				return this.selectWhich.compareTo(that.selectWhich);
			}
			
			if (this.requireMoreStemsThanAffixes != that.requireMoreStemsThanAffixes) {
				if (this.requireMoreStemsThanAffixes) {
					return -1;
				}
				return 1;
			}
			
			if (this.verticalMetricCutoff < that.verticalMetricCutoff) {
				return -1;
			}
			if (this.verticalMetricCutoff > that.verticalMetricCutoff) {
				return 1;
			}
			
			if (this.topCutoff < that.topCutoff) {
				return -1;
			}
			if (this.topCutoff > that.topCutoff) {
				return 1;
			}
			
			if (this.bottomCutoff < that.bottomCutoff) {
				return -1;
			}
			if (this.bottomCutoff > that.bottomCutoff) {
				return 1;
			}
			
			// A null maxLevelCutoff is counted as larger than all possible real
			// maxLevelCutoffs because a null maxLevelCutoff means never stop going
			// up prematurely
			if (this.maxLevelCutoff == null ||
					that.maxLevelCutoff == null) {
				
				// 'this' is NOT null, but 'that' IS null
				if (this.maxLevelCutoff != null) {
					return -1;
				}
				
				// 'this' IS null, while 'that' is NOT null.
				if (that.maxLevelCutoff != null) {
					return 1;
				}
				
			} else {
				if (this.maxLevelCutoff < that.maxLevelCutoff) {
					return -1;
				}	
				if (this.maxLevelCutoff > that.maxLevelCutoff) {
					return 1;
				}
			}
			
			return 0;
		}
		
		@Override
		public boolean equals(Object o) {
			if ( ! (o instanceof BottomUpParameterSetting)) {
				return false;
			}
			
			BottomUpParameterSetting that = (BottomUpParameterSetting)o;
			if (this.compareTo(that) == 0) {
				return true;
			}
			
			return false;
		}
		
		@Override
		public int hashCode() {
			int hashCode = verticalMetric.hashCode();
			
			if (bottomMax != null) {
				hashCode = 31*hashCode + bottomMax.hashCode();
			}
			
			if (exhaustiveSearchAtLevel1) {
				hashCode = 31*hashCode + 1;
			} else {
				hashCode = 31*hashCode + 2;
			}
			
			if (useCovered) {
				hashCode = 31*hashCode + 1;
			} else {
				hashCode = 31*hashCode + 2;
			}
			
			hashCode = 31*hashCode + selectWhich.hashCode();
			
			if (requireMoreStemsThanAffixes) {
				hashCode = 31*hashCode + 1;
			} else {
				hashCode = 31*hashCode + 2;
			}
			
			hashCode = 31*hashCode + verticalMetricCutoff.hashCode();
			hashCode = 31*hashCode + topCutoff.hashCode();
			hashCode = 31*hashCode + bottomCutoff.hashCode();
			hashCode = 31*hashCode + maxLevelCutoff.hashCode();
			
			return hashCode;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

	}

	
	// TODO: Dead code that should be deleted once the new simplified greedy search
	// works.
	/**
	 * This class holds the current state of a Bottom-up search that is in progress.
	 * At any given time, a bottom-up search is at a current node and has a variety
	 * of options on which scheme it should go to next.
	 */
	/*
	private class SearchLocation {
		// Variables that get updated as we move about in the virtual network 


		private Scheme current = null;

		// Keeping track of this variable helps VirtualPartialOrderNetwork
		// more quickly calculate the parents of the 'current' Scheme.
		private TreeSet<Scheme> mostSpecificSchemeAncestorsOfCurrent = null;
		
		// The exact sizes of the parents that are formed using each possible affix.
		// We need the affixes sorted by the size of the parent scheme they form,
		// but, unless we absolutely have to, we don't want to sort the different affixes 
		// that produce parents that are the same size (performing this unnecessary sort 
		// significantly impacted runtime).  Hence, we use a sorted TreeMap to sort the 
		// affixes only with respect to the size of the parent Scheme they form.
		//
		//   The key is the size (number of stems),
		//   The value is the set of affixes that each individually form a parent of the 
		//     key's size. The value is a HashSet<Affix> and not a SetOfMorphemes<Affix> 
		//       because the affixes that form parents:
		//         1) do not form a scheme
		//         2) do not need to be sorted lexicographically, which takes a LOT of time
		//
	    private TreeMap<Integer, HashSet<Affix>> parentSizeToSetOfAffixes = null;
		
		private SearchLocation(Scheme current) {
			setCurrentLocation(current);
		}
				
		private void setCurrentLocation(Scheme newCurrent) {
			
			this.current = newCurrent;
			
			mostSpecificSchemeAncestorsOfCurrent = 
				partialOrderNetwork.getMostSpecificSchemeAncestors(
						newCurrent,
						current,
						mostSpecificSchemeAncestorsOfCurrent);
			
			parentSizeToSetOfAffixes = null;
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
		 * Returns the parent Scheme of the passed in scheme with the Nth most stems.
		 */
	/*
		public Scheme getNthLargestParentByAdherents(Scheme scheme, int n) {
			if ((current == null) || ( ! scheme.equals(current))) {
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
			Scheme nthLargestParent = 
				denseNetworkSchemeGenerator.generateParentScheme(
						current, affixFormingNthLargestParent);
			
			return nthLargestParent;
		}

	}
	*/

	private static final long serialVersionUID = 1L;

	private static final String SEARCH_STEP_NAME = "Bottom-Up Search";

	//private BottomUpSearchableNetwork partialOrderNetwork;
	private VirtualPartialOrderNetwork partialOrderNetwork;

	private BottomUpParameterSetting parameterSetting = new BottomUpParameterSetting();
	
	// TODO: Dead code that should be deleted once the new simplified greedy search works
	//private SearchLocation searchLocation = null;
	
	private SearchPathList pathsToSelectedSchemes; 
	
	// 'coveredAffixes' is an approximation to Alon's idea of not searching a
	// scheme S where some other previously selected scheme contains ALL the
	// affixes that S contains.  In general you would have to indeed check
	// each selected Scheme SS to see if SS contains all the affixes that S does.
	// But in a bottom-up search where we always start from the bottom of
	// the network, we only actually need to rule out *starting* a search from
	// some level 1 Scheme L1S whose affix is in some selected Scheme.
	//
	// IMPORTANT: If I ever want to use Alon's "covered" idea with some
	//            form of search that doesn't ALWAYS start from the
	//            level 1 of the network, I will need to change this.
	private TreeSet<Affix> coveredAffixes;
	
	
	// visited does not need to be sorted
	//private TreeSet<SetOfMorphemes<Affix>> visited;
	private THashSet<SetOfMorphemes<Affix>> visited;
	
	// A list of level 1 nodes from which to begin a search path
	//ArrayList<Level1Scheme> candidateSeeds = null;
	ArrayList<Scheme> candidateSeeds = null;
	
	private Map<Affix, Set<Affix>> affixesThatCooccurInSelectedSchemes;

	
	public BottomUpSearch(VirtualPartialOrderNetwork partialOrderNetwork, 
						  BottomUpParameterSetting parameterSetting) {
		
		affixesThatCooccurInSelectedSchemes = new HashMap<Affix, Set<Affix>>();
		
		this.partialOrderNetwork = partialOrderNetwork;
		
		pathsToSelectedSchemes = new SearchPathList();
		coveredAffixes = new TreeSet<Affix>();	
		
		// visited does not need to be sorted
		//visited = new TreeSet<SetOfMorphemes<Affix>>();
		visited = new THashSet<SetOfMorphemes<Affix>>();
		
		setParameterSetting(parameterSetting);
	}

	public BottomUpSearch(VirtualPartialOrderNetwork partialOrderNetwork, 
						  BottomUpParameterSetting bottomUpParameterSetting, 
						  SearchPathList pathsToSelectedSchemes) {
		
		this(partialOrderNetwork, bottomUpParameterSetting);
		
		this.pathsToSelectedSchemes = pathsToSelectedSchemes; 
		
		// Mark as covered all affixes that occur in any previously selected Scheme
		for (SearchPath selectedPath : pathsToSelectedSchemes) {
			Scheme selectedScheme = selectedPath.getTerminalScheme();
			addToCoveredAffixes(selectedScheme.getAffixes());
		}
		
		// Mark as visited all Schemes in any path to a selected Scheme
		for (SearchPath searchPath : pathsToSelectedSchemes) {
			for (Scheme scheme : searchPath) {
				addToVisited(scheme);
			}
		}
	}
	


	public BottomUpParameterSetting getParameterSetting() {
		return parameterSetting;
	}

	public void setParameterSetting(BottomUpParameterSetting parameterSetting) {
		this.parameterSetting = parameterSetting;
	}

	// TODO: Dead code that should be deleted once the new simplified greedy search works
/*
	public SearchPathList searchGreedy() {
		
		SearchPath pathToSelectedScheme;
		
		int newSearchPathCounter = 0;
		Scheme previousSeed = null;
		while (true) {
			
			// 1) Pick a new seed at the bottom of the network
			//
			// pickNewSearchSeed will return false when we do not wan't to grow any new seed
			//		
			boolean newSearchSeedWasSelected = pickNewSearchSeed();
			if ( ! newSearchSeedWasSelected) {
				break;
			}
			
			pathToSelectedScheme = new SearchPath();
			boolean printIfSelected = false;
			newSearchPathCounter++;

			if ((previousSeed == null) ||
				(searchLocation.current.adherentSize() != previousSeed.adherentSize()) ||
				((newSearchPathCounter % 1000) == 0)) {

				printIfSelected = true;

				System.err.println();
				System.err.println("------------------------------------------------");
				System.err.println(
						"Starting Search from the " + newSearchPathCounter + 
				"th Level 1 Seed:");
				System.err.println(searchLocation.current.toPrettyString(30));
				System.err.println();
				System.err.println();
			} 
			
			
			// 2) Grow the seed up until we decide to stop
			//
			// growSeed will return null when we should stop growing a seed
			//
			boolean growThisSeed = true;
			while (growThisSeed) {
				// We want to add current to the covered list and to the
				// explored path exactly when current is not null--irregardless
				// of whether or not we are just starting this path or continuing
				// in onwards.
				addToCoveredAffixes(searchLocation.current.getAffixes());
				pathToSelectedScheme.add(searchLocation.current);
				
				growThisSeed = growSearchPathGreedily();
			}
			
			// 3) Add the grown seed to the set of Selected Nodes
			//
			addToSelectedPaths(pathToSelectedScheme, printIfSelected);
		}
		
		System.err.println();
		System.err.println(pathsToSelectedSchemes.size() + " Schemes were selected during ");
		System.err.println("  this BottomUp Search.");
		
		return pathsToSelectedSchemes;
	}
*/
	
	public SearchPathList search() {
		
		int newSearchPathCounter = 0;
		Scheme previousSeed = null;
		Scheme previousCurrent = null;
		Scheme current = null;
		Scheme currentSeed = null;
		
		// Keeping track of this variable helps VirtualPartialOrderNetwork
		// more quickly calculate the parents of the 'current' Scheme.
		Set<Scheme> mostSpecificSchemeAncestors = null;
		
		while (true) {
			
			// 1) Pick a new seed at the bottom of the network
			//
			// pickNewSeed will return null when we do not wan't to grow any new seed
			//
			
			previousSeed = currentSeed;			
			current = pickNewSearchSeed();
			currentSeed = current;
			
			if (current == null) {
				break;
			}
			
			SearchPath partialPathToSelectedScheme = new SearchPath();
			newSearchPathCounter++;
			boolean printSelectionInformation = false;
			
			
			if ((previousSeed == null) ||
				(currentSeed.adherentSize() != previousSeed.adherentSize()) ||
				((newSearchPathCounter % 100) == 0)) {
				
				printSelectionInformation = true;
				
				System.err.println();
				System.err.println();
				System.err.println("------------------------------------------------------");
				System.err.println(
						"Starting Search from the " +
						newSearchPathCounter + "th Level 1 Seed:");
				System.err.println(current.toPrettyString(30));
			}

			
			partialPathToSelectedScheme.add(current);			
			
			
			// 2) Grow the seed up until we decide to stop
			//
			while (true) {
								
				// An upward path will die if it intersects a previously taken path
				addToVisited(current);
				
				// Getting the most specific Scheme ancestors is *not* integrated
				// into the same function that actually finds the parents because
				// updating ancestors is (probably) often much faster than finding 
				// them from scratch.
				mostSpecificSchemeAncestors = 
					partialOrderNetwork.getMostSpecificSchemeAncestors(
							current,
							previousCurrent,
							mostSpecificSchemeAncestors);
				
				HashMap<Affix, Integer> parentAffixesAndSizes =
					partialOrderNetwork.getParentAffixesWithAdherentSizes(
							current, 
							mostSpecificSchemeAncestors);
				
				HashSet<Affix> parentAffixes =
					getParentAffixesThatPassHardUpwardMetrics(
							parentAffixesAndSizes,
							current.level());
				
				Scheme newCurrent = 
					getBestParentToMoveTo(
							parentAffixes, 
							parentAffixesAndSizes,
							current);
				
				if (newCurrent == null) {

					boolean pathWasQualifiedToBeAdded = addToSelectedPaths(partialPathToSelectedScheme);
					if (printSelectionInformation) {
						if (pathWasQualifiedToBeAdded) {
							System.err.println();
							System.err.println("This seed led to the following selected candidate paradigm:");
							System.err.println();
							System.err.println(partialPathToSelectedScheme);
						} else {
							System.err.println();
							System.err.println("No candidate paradigm was selected from this seed.");	
						}
					}
					
					break;
				}
				
				current = newCurrent;
								
				// If some other path already visited this scheme then do not
				// pursue this path any  further.
				if (visited(current)) {
					if (printSelectionInformation) {
						System.err.println();
						System.err.println("This seed intersected a previously taken network path. ");
						System.err.println("  No candidate paradigm was selected.");
					}
					
					break;
				}
				
				// TODO: OLD CODE. DELETE WHEN THE NEW SIMPLE GREEDY ALGORITHM WORKS
				//
				// Get all the qualified parents, regardless of if they have
				// been visited or not.
				//List<Scheme> parentsToMoveToSortedBestToWorst = 
				//	getQualifiedParentsSortedBestToWorst(terminalScheme);
				
				// add the current path to the selected list if appropriate
				if (parameterSetting.selectWhich == SelectWhich.ALL_ON_PATH) {
					addToSelectedPaths(partialPathToSelectedScheme);
				}
				
				// Here I actually append the current scheme to the saved search path
				partialPathToSelectedScheme.add(current);

				
				// Push on the qualified parents in reverse order
				// so that the best parent is on the top of the stack
				//for (int indexIntoQualifiedParents = parentsToMoveToSortedBestToWorst.size()-1;
				//	 indexIntoQualifiedParents >= 0;
				//	 indexIntoQualifiedParents--) {
				//	
				//	Scheme qualifiedParent = 
				//		parentsToMoveToSortedBestToWorst.get(indexIntoQualifiedParents);
				//	
				//	// Only extend search paths for those qualified parents who have
				//	// not yet been visited.
				//	if (visited(qualifiedParent)) {
				//		continue;
				//	}
				//	
				//	SearchPath newPartialPathToSelectedScheme =
				//		new SearchPath(partialPathToSelectedScheme);
				//	
				//	newPartialPathToSelectedScheme.add(qualifiedParent);
				//	partialPathsToSelectedSchemes.push(newPartialPathToSelectedScheme);
				//}
			} //end while growing a particular seed
		} // end while selecting seeds to grow
		
		System.err.println();
		System.err.println(pathsToSelectedSchemes.size() + " Schemes were selected during ");
		System.err.println("  this BottomUp Search.");
		System.err.println(visited.size() + " Schemes were visited during this BottomUp Search.");
		
//		SeedAnalysesParser parser = new SeedAnalysesParser();
//	    SeedAnalyses seedAnalyses = new SeedAnalyses();
//		try {
//			seedAnalyses = parser.parse("seed.txt");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		for (SeedScheme seedScheme : seedAnalyses.getSeedSchemes()) {
//			Scheme fullScheme = partialOrderNetwork.generateScheme(seedScheme.getAffixes());
//			SearchPath path = new SearchPath();
//			path.add(fullScheme);
//			pathsToSelectedSchemes.add(path);
//			for (Scheme manualScheme : seedScheme.getSubschemes()) {
//				fullScheme = partialOrderNetwork.generateScheme(manualScheme.getAffixes());
//				if (fullScheme.getAffixes().size() > 1) {
//					path = new SearchPath();
//					path.add(fullScheme);
//					pathsToSelectedSchemes.add(path);
//				}
//			}
//		}
		
		return pathsToSelectedSchemes;
	}

	// Returns the parent Scheme of 'current' that is the best scheme to move to
	//         OR 'null' if no parent passes the metric threshold
	private Scheme 
	getBestParentToMoveTo(
			HashSet<Affix> parentAffixes, 
			HashMap<Affix, Integer> parentAffixesAndSizes, 
			Scheme current) {
		
		if (parentAffixes.size() == 0) {
			return null;
		}
		
		ComparablePair<Scheme, Double> bestParentAndScore = null;
		
		switch (parameterSetting.verticalMetric) {
		
		// The Ratio metric is unique in that, the best parent is simply that parent
		// with the most adherent stems, and we don't actually need to calculate what
		// the ratio IS at this point
		case RATIO:
			bestParentAndScore = 
				getBestParentByRatio(parentAffixes, parentAffixesAndSizes, current);
			break;
			
		default:
			bestParentAndScore =
				getBestParent(parentAffixes, parentAffixesAndSizes, current);
			break;
		}
		
		if (bestParentAndScore.getRight() > parameterSetting.verticalMetricCutoff) {
			return bestParentAndScore.getLeft();
		}
		
		return null;
	}

	private ComparablePair<Scheme, Double> 
	getBestParent(
			HashSet<Affix> parentAffixes, 
			HashMap<Affix, Integer> parentAffixesAndSizes, 
			Scheme current) {
		
		// Some metrics can return negative scores so 'scoreOfBestParent'
		// must be initialized to 'null'.
		Double scoreOfBestParent = null;
		HashSet<Affix> parentAffixesAtBestScore = null;
		
		// Cycle through the parent affixes to find the one(s) with the best
		// score(s) for the vertical metric specified for this bottom-up search
		for (Affix parentAffix : parentAffixes) {

			// make the parent scheme out of the current Scheme and this
			// particular parentAffix
			SetOfMorphemes<Affix> affixesOfAParent = new SetOfMorphemes<Affix>(current.level() + 1);
			affixesOfAParent.add(current.getAffixes());
			affixesOfAParent.add(parentAffix);
			Scheme parent = partialOrderNetwork.generateScheme(affixesOfAParent);

			// Use VerticalDecision to get the value of the desired vertical metric
			// in going from 'current' to this 'parent'.
			VerticalDecision verticalDecision =
				new VerticalDecision(partialOrderNetwork, current, parent);
			Double metricValue = 
				verticalDecision.calculate(parameterSetting.verticalMetric);
			

			// Some metrics can return 'null' for their score. But any numeric
			// score is better than a 'null' score. The variable 
			// 'compareThisParentToBestSoFar' follows the Java Compare convention--
			// it will get values as follows:
			//   1 : if this parent's score is better than best so far
			//   0 : if this parent's score is the same as the best score so far
			//  -1 : if this parent's score is less than the best so far
			int compareThisParentToBestSoFar = 0;
			if (scoreOfBestParent == null) {
				if (metricValue != null) {
					compareThisParentToBestSoFar = 1;
				}
				
			} else {
				if (metricValue > scoreOfBestParent) {
					compareThisParentToBestSoFar = 1;
					
				} else if (metricValue < scoreOfBestParent) {
					compareThisParentToBestSoFar = -1;
				}
			}
			
			// If this parent's score outranks the best so far then replace it
			// and clear the list of other affixes with as-good scores.
			if (compareThisParentToBestSoFar > 0) {
				scoreOfBestParent = metricValue;
				parentAffixesAtBestScore = new HashSet<Affix>();
				parentAffixesAtBestScore.add(parentAffix);
				
			// If this parent's score is as good as the best, then add the
			// current parentAffix to the list of best ones.
			} else if (compareThisParentToBestSoFar == 0) {
				parentAffixesAtBestScore.add(parentAffix);
			}
		}
		
		// Get the orthographically sorted first parent affix that gave a
		// score that is best.
		TreeSet<Affix> parentAffixesAtBestScore_Sorted =
			new TreeSet<Affix>(parentAffixesAtBestScore);
		Affix bestParentAffix = parentAffixesAtBestScore_Sorted.first();

		// Recompute the actual best parent scheme (we only saved the affix above)
		SetOfMorphemes<Affix> affixesOfBestParent = new SetOfMorphemes<Affix>();
		affixesOfBestParent.add(current.getAffixes());
		affixesOfBestParent.add(bestParentAffix);
		Scheme bestParentScheme = partialOrderNetwork.generateScheme(affixesOfBestParent);
		
		// Compute the return value
		ComparablePair<Scheme, Double> bestParentAndScore =
			new ComparablePair<Scheme, Double>(bestParentScheme, scoreOfBestParent);
		
		return bestParentAndScore;
	}

	// The Ratio metric is unique in that, the best parent is simply that parent
	// with the most adherent stems, and we don't actually need to calculate what
	// the ratio IS or what the parent stems ARE until we have found the best parent.
	private ComparablePair<Scheme, Double> 
	getBestParentByRatio(
			HashSet<Affix> parentAffixes, 
			HashMap<Affix, Integer> parentAffixesAndSizes, 
			Scheme current) {
		
		int sizeOfLargestParent = -1;
		HashSet<Affix> parentAffixesAtLargestSize = null;
		
		// Among all the possible parent affixes find the parent affix(es) that
		// form(s) the parent(s) with the largest stem size.
		for (Affix parentAffix : parentAffixes) {
			int sizeOfParent = parentAffixesAndSizes.get(parentAffix);
			
			if (sizeOfParent > sizeOfLargestParent) {
				
				sizeOfLargestParent = sizeOfParent;
				parentAffixesAtLargestSize = new HashSet<Affix>();
				parentAffixesAtLargestSize.add(parentAffix);
				
			} else if (sizeOfParent == sizeOfLargestParent) {
				
				parentAffixesAtLargestSize.add(parentAffix);
			}
		}
		
		// Get the lexicographically first affix that forms a scheme with
		// the largest size.
		TreeSet<Affix> parentAffixesAtLargestSize_Sorted =
			new TreeSet<Affix>(parentAffixesAtLargestSize);
		Affix bestParentAffix = parentAffixesAtLargestSize_Sorted.first();
		
		// Generate the actual Scheme corresponding the best parent affix
		SetOfMorphemes<Affix> affixesOfBestParent = new SetOfMorphemes<Affix>(current.level() + 1);
		affixesOfBestParent.add(current.getAffixes());
		affixesOfBestParent.add(bestParentAffix);
		Scheme bestParentScheme = partialOrderNetwork.generateScheme(affixesOfBestParent);
		
		// Calculate the actual ratio score of the best parent
		double ratio = (double)sizeOfLargestParent / (double)current.adherentSize();
		
		// Calculate the return value
		ComparablePair<Scheme, Double> bestParentAndScore =
			new ComparablePair<Scheme, Double>(bestParentScheme, ratio);
		
		return bestParentAndScore;
	}

	private HashSet<Affix> 
	getParentAffixesThatPassHardUpwardMetrics(
			HashMap<Affix, Integer> parentAffixesAndSizes, 
			int currentLevel) {
		
		HashSet<Affix> parentAffixesThatPass = new HashSet<Affix>();
		
		int parentLevel = currentLevel + 1;
		
		for (Affix parentAffix : parentAffixesAndSizes.keySet()) {
			if ( ! failsTopCutoff(parentLevel, parentAffixesAndSizes.get(parentAffix))) {
				parentAffixesThatPass.add(parentAffix);
			}
		}
		
		return parentAffixesThatPass;
	}

	// Returns true if <code>path</code> is really added to the SelectedPaths
	// <code>path</code> might not be added if it has a null terminal scheme
	// or if it's level is too small (i.e. not enough affixes).
	private boolean addToSelectedPaths(SearchPath path) {
		Scheme terminalScheme = path.getTerminalScheme();
		if (terminalScheme == null) {
			return false;
		}
		if (terminalScheme.level() >= 2) {
			
			pathsToSelectedSchemes.add(path);
			addToCoveredAffixes(terminalScheme.getAffixes());
			
			// Keep track of all the affixes that cooccured in each
			// selected Scheme
			for (Affix affixA : terminalScheme.getAffixes()) {
				for (Affix affixB : terminalScheme.getAffixes()) {
					if ( ! affixesThatCooccurInSelectedSchemes.containsKey(affixA)) {
						affixesThatCooccurInSelectedSchemes.put(affixA, new HashSet<Affix>());
					}
					Set<Affix> affixesThatOccurWithAffixA = 
						affixesThatCooccurInSelectedSchemes.get(affixA);
					affixesThatOccurWithAffixA.add(affixB);
				}
			}
			
			return true;
		}
		return false;
	}


	/**
	 * Adds the set of affixes and (to save time) all the individual affixes themselves
	 * to the list of covered Scheme names.
	 * 
	 * @param affixes
	 */
	protected void addToCoveredAffixes(SetOfMorphemes<Affix> affixes) {
		for (Affix affix : affixes) {
			coveredAffixes.add(affix);
		}
	}
	
	private boolean passesCoveredRestriction(Scheme seedScheme) {
		
		if (parameterSetting.useCovered) {
			if (coveredAffixes.containsAll(seedScheme.getAffixes().getCopyOfMorphemes())) {
				return false;
			}
		}
		return true;
	}
	
	private void addToVisited(Scheme scheme) {
		visited.add(scheme.getAffixes());
	}
	
	private boolean visited(Scheme scheme) {
		return visited.contains(scheme.getAffixes());
	}
	
	/**
	 * @return the Scheme picked from among the lowests in this.partialOrderNetwork
	 * 		   <code>null</code> if we do not want to pick any new seed
	 */
	int candidateSeedsIndex = 0;
	protected Scheme pickNewSearchSeed() {

		int DEBUG = 0;
		
		getCandidateSeedsByAdherentSize();
		
		if (DEBUG > 0) {
			System.err.println();
			System.err.println("Selecting a new oracle seed to begin growing:");
			System.err.println();
		}
		
		//Level1Scheme newSeed = null;
		Scheme newSeed = null;
		
		// Return the next qualifying seed
		
		while (true) {
		
			// We are all out of candidate seeds to return
			if (candidateSeedsIndex >= candidateSeeds.size()) {
				System.err.println();
				System.err.println("There are no uncovered seeds available.");
				System.err.println();
				System.err.println("We are done with search...");
				
				return null;
			}
			
			// get the next seed and increment candidateSeedsIndex
			// so that next time pickNewSearchSeed() is called we
			// get a new seed.
			newSeed = candidateSeeds.get(candidateSeedsIndex);
			candidateSeedsIndex++;  // AN INSTANCE VARIABLE!!
			
			// The candidate Seed must have bottomMax or fewer stems in it.
			// If it has too many stems then the next seed may have fewer.
			if (parameterSetting.bottomMax != null) {
				if (newSeed.adherentSize() > parameterSetting.bottomMax) {
					continue;
				}
			}
			
			// The candidate seed must have at least topCutoff stems in it.
			// The next seed cannot have *more* stems than this seed, so
			// If this seed has too few, return false.
			if (failsTopCutoff(newSeed)) {
				System.err.println();
				System.err.println("No uncovered seeds pass the top cutoff of: " + 
						parameterSetting.topCutoff + " stems.");
				System.err.println();
				System.err.println("We are done with search...");
				
				return null;
			}
			
			// the candidate seed must have at least bottomCutoff stems in it.
			// The next seed cannot have *more* stems than the this seed, so
			// if this seed has too few stems, return false.
			// This parameter is only ever used here so I don't have a separate 
			// method for it.
			if (newSeed.adherentSize() < parameterSetting.bottomCutoff) {
				System.err.println();
				System.err.println("No uncovered seeds pass the bottom cutoff of: " + 
						parameterSetting.bottomCutoff + " stems.");
				System.err.println();
				System.err.println("We are done with search...");
				
				return null;
			}
			
			// If newSeed is 'covered' then try the next seed
			if ( ! passesCoveredRestriction(newSeed)) {
				continue;
			}
		
			return newSeed;
		}
	}
	
	/**
	 * @return A TreeSet of PartialOrderNodes sorted by decreasing adherent size 
	 * 		   of the scheme at that node
	 */
	protected void getCandidateSeedsByAdherentSize() {
		
		// Return cached
		if (candidateSeeds != null) {
			return;
		}
		
		System.err.println();
		System.err.println("Sorting candidate seeds...");
		System.err.println();
		
		TreeSet<Scheme> candidateSeedsOrderedSet = 
			new TreeSet<Scheme>(new Scheme.ByDecreasingAdherentSize());
		
		candidateSeedsOrderedSet.addAll(partialOrderNetwork.getSmallestSchemesAboveLevel0());
		
		// add pair of affixes from mannually entered paradigms
		if (ManualData.isManualSeedAvailable()) {
			this.addManualAffixes(candidateSeedsOrderedSet);
		}
		
		candidateSeeds = new ArrayList<Scheme>(candidateSeedsOrderedSet);
	}

	private void addManualAffixes(TreeSet<Scheme> candidateSeedsOrderedSet) {
		try {
			SeedAnalyses manualAffixSchemes = ManualData.getManualAnalyses();
			Set<ComparablePair<Affix, Affix>> affixPairSet = new HashSet<ComparablePair<Affix,Affix>>();
			for (SeedScheme  manualScheme : manualAffixSchemes.getSeedSchemes()) {
				for (Affix affix1 : manualScheme.getAffixes()) {
					for (Affix affix2 : manualScheme.getAffixes()) {
						if (affix1.compareTo(affix2) < 0) {
							affixPairSet.add(new ComparablePair<Affix, Affix>(affix1, affix2));
						}
					}
				}
			}
			
			for (ComparablePair<Affix, Affix> affixPair : affixPairSet) {				
				Scheme seedScheme = partialOrderNetwork.generateScheme(new SetOfMorphemes<Affix>(affixPair.getLeft(), affixPair.getRight()));
				DebugLog.write("adding manual scheme seed " + seedScheme.getAffixes());
				candidateSeedsOrderedSet.add(seedScheme);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private boolean failsTopCutoff(Scheme scheme) {
		return failsTopCutoff(scheme.level(), scheme.adherentSize());
	}
	
	private boolean failsTopCutoff(int schemeLevel, int numOfAdherents) {
		boolean tooFewStemsInScheme = false;
		if (numOfAdherents < parameterSetting.topCutoff) {
			tooFewStemsInScheme = true;
		}
		if (parameterSetting.requireMoreStemsThanAffixes) {
			if (numOfAdherents <= schemeLevel) {
				tooFewStemsInScheme = true;
			}
		}
		return tooFewStemsInScheme;
	}


	// Currently not all BottomUp_Search_Abstracts are SearchSteps so we get a bit circuitous here
	public SearchPathList performSearchStep() {
		return search();
	}

	public String getName() {
		return getNameStatic();
	}
	
	private static String getNameStatic() {
		return SEARCH_STEP_NAME;
	}

	@Override
	public String toString() {
		return parameterSetting.toString();
	}
}
