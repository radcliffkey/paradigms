/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Morpheme;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.statistics.ListOfData.SummaryStat;

public class MorphemeLength_Filter implements SearchStep {

	private static final String SEARCH_STEP_NAME = "Morpheme Length Filter";
	private static final long serialVersionUID = 1L;

	public static class Parameters extends SearchStepParameters {
		private static final long serialVersionUID = 1L;

		private boolean      mustContainNullAffixToFilter;
		private SummaryStat  summaryStat;
		private List<Double> summaryCutoffs = new ArrayList<Double>();
	
		
		public Parameters() {
			// Defaults
			mustContainNullAffixToFilter = false;
			summaryStat = SummaryStat.MEAN;
			summaryCutoffs.add(1.0);
		}
	
		public void setMustContainNullAffixToFilter(boolean mustContainNullAffixToFilter) {
			this.mustContainNullAffixToFilter = mustContainNullAffixToFilter;
		}
		
		public void setSummaryCutoffs(List<Double> summaryCutoffs) {
			this.summaryCutoffs = summaryCutoffs;
		}
		
		/**
		 * 
		 * @param summaryStat
		 * @return <code>false</code> if could not set the <code>summaryStat</code> to the
		 *         passed in value.  (As of Oct 2006, can only set the summary Stat to
		 *         SummaryStat.MEAN or SummaryStat.MEDIAN.)
		 */
		public boolean setSummaryStat(SummaryStat summaryStat) {
			if ((summaryStat == SummaryStat.MEAN) ||
				(summaryStat == SummaryStat.MEDIAN)) {
				
				this.summaryStat = summaryStat;
				
				return true;
			}
			
			return false;
		}

		public boolean getMustContainNullAffixToFilter() {
			return mustContainNullAffixToFilter;
		}
		
		public SummaryStat getSummaryStat() {
			return summaryStat;
		}

		
		public Iterator<SearchStepParameterSetting> iterator() {
			
			List<SearchStepParameterSetting> allParameterSettings = 
				new ArrayList<SearchStepParameterSetting>();

			for (Double summaryCutoff : summaryCutoffs) {
				ParameterSetting parameterSetting = new ParameterSetting(mustContainNullAffixToFilter, 
																		 summaryStat, 
																		 summaryCutoff);
				allParameterSettings.add(parameterSetting);
			}
			
			return allParameterSettings.iterator();

		}

		@Override
		protected String getParametersString() {
			String toReturn = "";
			toReturn += String.format("Must Contain Null Affix To Filter: %b%n", mustContainNullAffixToFilter);
			toReturn += String.format("Summary Statistic:                  %s%n", summaryStat);
			toReturn += String.format("Summary Statistic Cutoffs:          %s%n", summaryCutoffs);
			return toReturn;
		}

		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			toReturn += String.format("# Must Contain Null Affix To Filter: %b%n", mustContainNullAffixToFilter);
			toReturn += String.format("# Summary Statistic:                  %s%n", summaryStat);
			toReturn += String.format("# Summary Statistic Cutoffs:          %s%n", summaryCutoffs);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Must Contain Null Affix to Filter, Summary Statistic, Summary Statistic Cutoff";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		private boolean     mustContainNullAffixToFilter;
		private SummaryStat summaryStat;
		private Double      summaryCutoff = 0.5;

		public ParameterSetting(boolean mustContainNullAffixToFilter, 
								SummaryStat summaryStat, 
								Double summaryCutoff) {
			
			associatedSearchStep = MorphemeLength_Filter.class;
			
			this.mustContainNullAffixToFilter = mustContainNullAffixToFilter;
			this.summaryStat = summaryStat;
			this.summaryCutoff = summaryCutoff;
		}

		@Override
		public String getStringForSpreadsheet() {
			return mustContainNullAffixToFilter + ", " + summaryStat + ", " + summaryCutoff;
		}
		
		@Override
		public String getFilenameUniqueifier() {
			return "";
		}

		@Override
		public int compareTo(SearchStepParameterSetting thatSearchStepParameterSetting) {
			if ( ! (thatSearchStepParameterSetting instanceof ParameterSetting)) {
				super.compareTo(thatSearchStepParameterSetting);
			}
			
			ParameterSetting that = (ParameterSetting)thatSearchStepParameterSetting;
			
			// First compare on 'mustContainNullAffixToFilter'
			if ((this.mustContainNullAffixToFilter == true) &&
				(that.mustContainNullAffixToFilter == false)) {
				return -1;
			}
			if ((this.mustContainNullAffixToFilter == false) &&
				(that.mustContainNullAffixToFilter == true)) {
				return 1;
			}
			
			// Then compare on 'summaryStat'
			int summaryStatCompared =  this.summaryStat.compareTo(that.summaryStat);
			if (summaryStatCompared != 0) {
				return summaryStatCompared;
			}
			
			// Finally compare on 'summaryCutoff'
			return this.summaryCutoff.compareTo(that.summaryCutoff);
		}
		
		@Override
		protected String getParameterString() {
			String toReturn = "";
			toReturn += String.format("Must Contain Null Affix to Filter: %b%n", mustContainNullAffixToFilter);
			toReturn += String.format("Summary Statistic:                  %s%n", summaryStat);
			toReturn += String.format("Summary Statistic Cutoff:           %s%n", summaryCutoff);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}
	}
	
	MorphemeLength_Filter.ParameterSetting parameterSetting;
	SearchPathList searchPaths;
	
	public MorphemeLength_Filter(
			ParameterSetting parameterSetting, 
			SearchPathList pathsToFilter) {
		
		this.parameterSetting = parameterSetting;
		this.searchPaths      = pathsToFilter;
	}

	public SearchPathList performSearchStep() {
		
		Iterator<SearchPath> searchPathsIterator = searchPaths.iterator();
		while (searchPathsIterator.hasNext()) {
			SearchPath searchPath = searchPathsIterator.next();
			
			if ( ! passesCutoff(searchPath)) {
				searchPathsIterator.remove();
			}
		}
		
		return searchPaths;
	}

	
	private boolean passesCutoff(SearchPath searchPath) {
		
		Scheme terminalScheme = searchPath.getTerminalScheme();
		
		// 'searchPath' always passes (returns true) if:
		//   1) the terminal scheme in the search path must contain a Null Affix to filter
		//   2) the terminal scheme in the search path does not contain a Null Affix
		if (parameterSetting.mustContainNullAffixToFilter) {
			SetOfMorphemes<Affix> affixes = terminalScheme.getAffixes();
			if ( ! affixes.containsAll(new Affix(""))) {
				return true;
			}
		}
		
		SetOfMorphemes<Morpheme> stemsAndAffixes = new SetOfMorphemes<Morpheme>(terminalScheme.level());
		//stemsAndAffixes.add(terminalScheme.getStems());
		stemsAndAffixes.add(terminalScheme.getAffixes());
		
		double calculatedSummaryStatistic = 0.0;
		
		switch(parameterSetting.summaryStat) {
		case MEAN:
			calculatedSummaryStatistic = stemsAndAffixes.getAverageContainedLength();
			break;
			
		case MEDIAN:
			calculatedSummaryStatistic = stemsAndAffixes.getMedianContainedLength();
			break;
			
		default:
			System.err.println();
			System.err.println("  *** WARNING *** I do not know how to filter on morpheme length");
			System.err.println("                  other than by MEAN or MEDIAN!!");
			System.err.println();
			System.err.println("                  Returning false");
			return false;
		}
		
		if (calculatedSummaryStatistic > parameterSetting.summaryCutoff) {
			return true;
		}
		return false;
	}		
	
	@Override
	public String toString() {
		return parameterSetting.toString();
	}

	public String getName() {
		return getNameStatic();
	}
	
	public static String getNameStatic() {
		return SEARCH_STEP_NAME;
	}
}
