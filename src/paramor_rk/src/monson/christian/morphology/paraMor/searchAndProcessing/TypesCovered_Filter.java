/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import klic.radoslav.util.DebugLog;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;

public class TypesCovered_Filter implements SearchStep {

	private static final String SEARCH_STEP_NAME = "Types Covered Filter";
	private static final long serialVersionUID = 1L;

	public static class Parameters extends SearchStepParameters {
		private static final long serialVersionUID = 1L;

		private boolean       mustContainNullAffixToFilter;

		// a 'null' in this list means there is no limit on the level of the
		// scheme that will be filtered
		private List<Integer> mustBeLevelN_orLess = new ArrayList<Integer>();
		
		private List<Integer> schemeMustCoverAtLeastNtypes = new ArrayList<Integer>(); 
	
		
		public Parameters() {
			// Defaults
			mustContainNullAffixToFilter = false;
			
			// null means there is no limit on the size of a scheme that will
			// be filtered.
			mustBeLevelN_orLess.add(null);
			
			schemeMustCoverAtLeastNtypes.add(30);
		}
	
	
		public void setMustContainNullAffixToFilter(boolean mustContainNullAffixToFilter) {
			this.mustContainNullAffixToFilter = mustContainNullAffixToFilter;
		}
		
		public void setMustBeLevelN_orLess(List<Integer> mustBeLevelN_orLess) {
			this.mustBeLevelN_orLess = mustBeLevelN_orLess;
		}
		
		public void setSchemeMustCoverAtLeastNtypes(List<Integer> schemeMustCoverAtLeastNTypes) {
			this.schemeMustCoverAtLeastNtypes = schemeMustCoverAtLeastNTypes;
		}
		
				
		public Iterator<SearchStepParameterSetting> iterator() {
			
			List<SearchStepParameterSetting> allParameterSettings = 
				new ArrayList<SearchStepParameterSetting>();

			for (Integer mustBeLevelN_orLessValue : mustBeLevelN_orLess) {
				for (Integer typesCoveredCutoff : schemeMustCoverAtLeastNtypes) {
					
					ParameterSetting parameterSetting = 
						new ParameterSetting(mustContainNullAffixToFilter,
											 mustBeLevelN_orLessValue,
											 typesCoveredCutoff);
				
					allParameterSettings.add(parameterSetting);
				}
			}
			
			return allParameterSettings.iterator();
		}

		@Override
		protected String getParametersString() {
			String toReturn = "";
			toReturn += String.format("Must contain Null Affix to filter:         %b%n", mustContainNullAffixToFilter);
			
			toReturn += String.format("Scheme must be level N or less to Filter:   [");
			boolean first = true;
			for (Integer mustBeLevelN_orLessValue : mustBeLevelN_orLess) {
				if (first) first = false;
				else toReturn += ", ";

				if (mustBeLevelN_orLessValue == null) {
					toReturn += "No Limit";
				} else {
					toReturn += mustBeLevelN_orLessValue;
				}
			}
			toReturn += String.format("]%n");
			
			toReturn += String.format("Scheme must cover at least this many types: %s%n", schemeMustCoverAtLeastNtypes);
			return toReturn;
		}
		
		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			toReturn += String.format("# Must contain Null Affix to filter:         %b%n", mustContainNullAffixToFilter);
			
			toReturn += String.format("# Scheme must be level N or less to Filter:   [");
			boolean first = true;
			for (Integer mustBeLevelN_orLessValue : mustBeLevelN_orLess) {
				if (first) first = false;
				else toReturn += ", ";

				if (mustBeLevelN_orLessValue == null) {
					toReturn += "No Limit";
				} else {
					toReturn += mustBeLevelN_orLessValue;
				}
			}
			toReturn += String.format("]%n");
			
			toReturn += String.format("# Scheme must cover at least this many types: %s%n", schemeMustCoverAtLeastNtypes);
			return toReturn;
		}


		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Must Contain Null Affix to Filter, Must be Level N or Less to Filter, Types Covered Cutoff";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		private boolean mustContainNullAffixToFilter = false;
		private Integer mustBeLevelN_orLessToFilter = null;
		private Integer schemeMustContainAtLeastNTypes = 30;

		public ParameterSetting(boolean mustContainNullAffixToFilter,
								Integer mustBeLevelN_orLess,
								Integer typesCoveredCutoff) {
			
			associatedSearchStep = TypesCovered_Filter.class;

			this.mustContainNullAffixToFilter  = mustContainNullAffixToFilter;
			this.mustBeLevelN_orLessToFilter            = mustBeLevelN_orLess;
			this.schemeMustContainAtLeastNTypes = typesCoveredCutoff;
		}

		@Override
		public String getStringForSpreadsheet() {
			return mustContainNullAffixToFilter + ", " + 
				   mustBeLevelN_orLessToFilter + ", " + 
				   schemeMustContainAtLeastNTypes;
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
		
			// Then compare on 'mustBeLevelN_orLessToFilter'
			if (this.mustBeLevelN_orLessToFilter != that.mustBeLevelN_orLessToFilter) {
				if (this.mustBeLevelN_orLessToFilter == null) {
					return -1;
				}
				if (that.mustBeLevelN_orLessToFilter == null) {
					return 1;
				}
				return this.mustBeLevelN_orLessToFilter.compareTo(that.mustBeLevelN_orLessToFilter);
			}
			
			// finally compare on 'schemeMustContainAtLeastNTypes'
			return this.schemeMustContainAtLeastNTypes.compareTo(that.schemeMustContainAtLeastNTypes);
		}
		
		@Override
		protected String getParameterString() {
			String toReturn = "";
			toReturn += String.format("Must Contain Null Affix to Filter:         %b%n", mustContainNullAffixToFilter);
			
			toReturn += String.format("Must be Level N or Less to Filter:          ");
			if (mustBeLevelN_orLessToFilter == null) {
				toReturn += "No Limit";
			} else {
				toReturn += mustBeLevelN_orLessToFilter;
			}
			toReturn += String.format("%n");
			
			toReturn += String.format("Scheme must cover at least this many types: %s%n", schemeMustContainAtLeastNTypes);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}
	}
	
	TypesCovered_Filter.ParameterSetting parameterSetting;
	SearchPathList searchPaths;
	
	public TypesCovered_Filter(
			ParameterSetting parameterSetting, 
			SearchPathList pathsToFilter) {
		
		this.parameterSetting = parameterSetting;
		this.searchPaths      = pathsToFilter;
	}

	public SearchPathList performSearchStep() {
		DebugLog.write("TypesCovered_Filter.performSearchStep()");
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
		
		// 'searchPath' always passes (returns true) if:
		// the terminal scheme in the search path is at a level higher than
		// 'mustBeLevelN_orLower'.  A 'null' value of 'mustBeLevelN_orLower'
		// means this ParameterSetting is not using the 'mustBeLevelN_orLower'
		// parameter.
		if (parameterSetting.mustBeLevelN_orLessToFilter != null) {
			if (terminalScheme.level() > parameterSetting.mustBeLevelN_orLessToFilter) {
				return true;
			}
		}
				
		// The number of types covered is simply the number of stems times the number of affixes
		double typesCovered = (double)(terminalScheme.getContexts().size() * 
									   terminalScheme.getAffixes().size());
		
		if (typesCovered >= parameterSetting.schemeMustContainAtLeastNTypes) {
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
