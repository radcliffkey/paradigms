/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import klic.radoslav.util.DebugLog;

import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameterSetting;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameters;

public class TypesCoveredClusterFilter {

	private static final String SEARCH_STEP_NAME = "Types Covered Filter";
	private static final long serialVersionUID = 1L;

	// Over a corpus of 50,000 unique Spanish types, setting the childTypesCoveredCutoff to 37
	// yields a morpheme recall of 81.6& while drastically reducing the number of clusters that
	// are formed (from 7511 to 137 (see Monson Thesis, 2009)). After clustering schemes
	// any remaining cluster (or singleton scheme cluster) that has 37 or fewer unique licensing
	// types (not marked for morpheme boundaries) will be removed. The childTypesCoveredCutoff
	// is used during clustering
	public static final int DEFAULT_CLUSTER_MUST_COVER_AT_LEAST_N_TYPES = 37;
	
	public static class Parameters extends SearchStepParameters {
		private static final long serialVersionUID = 1L;

		// I am not doing any checking to enforce that someone doesn't try to set both of these
		// to true or false. This code is meant for ME to use, not so much for others to use.
		//
		// In general one of these should be turned on and one should be turned off.
		private boolean cutoffOverTotalCovered = true;
		private boolean cutoffOverAveCoveredByLeaves = false;
		
		// a 'null' in this list means there is no limit on the level of the
		// scheme that will be filtered
		private List<Integer> aveSchemeInClusterMustBeLevelN_orLess = new ArrayList<Integer>();
		
		private List<Integer> clusterMustCoverAtLeastNtypes = new ArrayList<Integer>(); 
	
		
		public Parameters() {
			// Defaults
			
			cutoffOverTotalCovered = true;
			cutoffOverAveCoveredByLeaves = false;
			
			// null means there is no limit on the size of a scheme that will
			// be filtered.
			aveSchemeInClusterMustBeLevelN_orLess.add(null);
			
			clusterMustCoverAtLeastNtypes.add(DEFAULT_CLUSTER_MUST_COVER_AT_LEAST_N_TYPES);
		}
	
		
		public void setCutoffOverAveCoveredByLeaves(boolean cutoffOverAveCoveredByLeaves) {
			this.cutoffOverAveCoveredByLeaves = cutoffOverAveCoveredByLeaves;
		}

		public void setCutoffOverTotalCovered(boolean cutoffOverTotalCovered) {
			this.cutoffOverTotalCovered = cutoffOverTotalCovered;
		}

		public void setAveSchemeInClusterMustBeLevelN_orLess(List<Integer> mustBeLevelN_orLess) {
			this.aveSchemeInClusterMustBeLevelN_orLess = mustBeLevelN_orLess;
		}
		
		public void 
		setClusterMustCoverAtLeastNtypes(List<Integer> aveSchemeInClusterMustCoverAtLeastNTypes) {
			this.clusterMustCoverAtLeastNtypes = aveSchemeInClusterMustCoverAtLeastNTypes;
		}
		
				
		public Iterator<SearchStepParameterSetting> iterator() {
			
			List<SearchStepParameterSetting> allParameterSettings = 
				new ArrayList<SearchStepParameterSetting>();

			for (Integer mustBeLevelN_orLessValue : aveSchemeInClusterMustBeLevelN_orLess) {
				for (Integer typesCoveredCutoff : clusterMustCoverAtLeastNtypes) {
					
					ParameterSetting parameterSetting = 
						new ParameterSetting(cutoffOverTotalCovered,
											 cutoffOverAveCoveredByLeaves,
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
			
			toReturn += "Cutoff over TOTAL Covered: " + cutoffOverTotalCovered + String.format("%n");
			toReturn += "Cutoff over AVE Covered by LEAVES: " + cutoffOverAveCoveredByLeaves + String.format("%n");
			
			toReturn += String.format("Ave scheme in cluster must be level N or less to filter:   [");
			boolean first = true;
			for (Integer mustBeLevelN_orLessValue : aveSchemeInClusterMustBeLevelN_orLess) {
				if (first) first = false;
				else toReturn += ", ";

				if (mustBeLevelN_orLessValue == null) {
					toReturn += "No Limit";
				} else {
					toReturn += mustBeLevelN_orLessValue;
				}
			}
			toReturn += String.format("]%n");
			
			toReturn += String.format("Cluster must cover at least this many types: %s%n", 
									  clusterMustCoverAtLeastNtypes);
			return toReturn;
		}

		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			
			toReturn += "# Cutoff over TOTAL Covered: " + cutoffOverTotalCovered + String.format("%n");
			toReturn += "# Cutoff over AVE Covered by LEAVES: " + cutoffOverAveCoveredByLeaves + String.format("%n");
			
			toReturn += String.format("# Ave scheme in cluster must be level N or less to filter:   [");
			boolean first = true;
			for (Integer mustBeLevelN_orLessValue : aveSchemeInClusterMustBeLevelN_orLess) {
				if (first) first = false;
				else toReturn += ", ";

				if (mustBeLevelN_orLessValue == null) {
					toReturn += "No Limit";
				} else {
					toReturn += mustBeLevelN_orLessValue;
				}
			}
			toReturn += String.format("]%n");
			
			toReturn += String.format("# Cluster must cover at least this many types: %s%n", 
									  clusterMustCoverAtLeastNtypes);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Cutoff over Total Covered, Cutoff over Ave Covered by Leaves, Ave Scheme in Cluster Must be Level N or Less to Filter, Types Covered Cutoff";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		private boolean cutoffOverTotalCovered = true;
		private boolean cutoffOverAveCoveredByLeaves = false;

		private Integer aveSchemeInClusterMustBeLevelN_orLessToFilter = null;
		private Integer clusterMustCoverAtLeastNTypes = DEFAULT_CLUSTER_MUST_COVER_AT_LEAST_N_TYPES;

		public ParameterSetting(boolean cutoffOverTotalCovered,
								boolean cutoffOverAveCoveredByLeaves,
								Integer aveSchemeInClusterMustBeLevelN_orLess,
								Integer typesCoveredCutoff) {
			
			associatedSearchStep = TypesCoveredClusterFilter.class;

			this.cutoffOverTotalCovered = cutoffOverTotalCovered;
			this.cutoffOverAveCoveredByLeaves = cutoffOverAveCoveredByLeaves;
			
			this.aveSchemeInClusterMustBeLevelN_orLessToFilter = aveSchemeInClusterMustBeLevelN_orLess;
			this.clusterMustCoverAtLeastNTypes                 = typesCoveredCutoff;
		}

		@Override
		public String getStringForSpreadsheet() {
			return cutoffOverTotalCovered + ", " +
				   cutoffOverAveCoveredByLeaves + ", " +
				   aveSchemeInClusterMustBeLevelN_orLessToFilter + ", " + 
				   clusterMustCoverAtLeastNTypes;
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
			
			// First compare on 'cutoffOverTotalCovered'
			if ((this.cutoffOverTotalCovered == true) &&
				(that.cutoffOverTotalCovered == false)) {
				return -1;
			}
			if ((this.cutoffOverTotalCovered == false) &&
				(that.cutoffOverTotalCovered == true)) {
				return 1;
			}
			
			// then compare on 'cutoffOverAveCoveredByLeaves'
			if ((this.cutoffOverAveCoveredByLeaves == true) &&
				(that.cutoffOverAveCoveredByLeaves == false)) {
				return -1;
			}
			if ((this.cutoffOverAveCoveredByLeaves == false) &&
				(that.cutoffOverAveCoveredByLeaves == true)) {
				return 1;
			}
			
			// First compare on 'aveSchemInClusterMustBeLevelN_orLessToFilter'
			if (this.aveSchemeInClusterMustBeLevelN_orLessToFilter != that.aveSchemeInClusterMustBeLevelN_orLessToFilter) {
				if (this.aveSchemeInClusterMustBeLevelN_orLessToFilter == null) {
					return -1;
				}
				if (that.aveSchemeInClusterMustBeLevelN_orLessToFilter == null) {
					return 1;
				}
				return this.aveSchemeInClusterMustBeLevelN_orLessToFilter.compareTo(that.aveSchemeInClusterMustBeLevelN_orLessToFilter);
			}
			
			// Then compare on 'aveSchemeInClusterMustContainAtLeastNTypes'
			return this.clusterMustCoverAtLeastNTypes.compareTo(that.clusterMustCoverAtLeastNTypes);
		}
		
		@Override
		protected String getParameterString() {
			String toReturn = "";
			
			toReturn += "Cutoff over TOTAL Covered: " + cutoffOverTotalCovered + String.format("%n");
			toReturn += "Cutoff over AVE Covered by LEAVES: " + cutoffOverAveCoveredByLeaves + String.format("%n");
			
			toReturn += String.format("Ave scheme in cluster must be level N or less to filter:          ");
			if (aveSchemeInClusterMustBeLevelN_orLessToFilter == null) {
				toReturn += "No Limit";
			} else {
				toReturn += aveSchemeInClusterMustBeLevelN_orLessToFilter;
			}
			toReturn += String.format("%n");
			
			toReturn += String.format("Cluster must cover at least this many types: %s%n", 
									  clusterMustCoverAtLeastNTypes);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}
	}
	
	TypesCoveredClusterFilter.ParameterSetting parameterSetting;
	List<BottomUpSearchResultCluster> clustersToFilter;
	
	public TypesCoveredClusterFilter(
			ParameterSetting parameterSetting, 
			BottomUpSearchResultClustering clustersToFilter) {
		
		this.parameterSetting = parameterSetting;
		this.clustersToFilter = new ArrayList<BottomUpSearchResultCluster>(clustersToFilter.getClusters());
	}

	public BottomUpSearchResultClustering filter() {
		
		DebugLog.write("TypesCoveredClusterFilter.filter()");
		DebugLog.write(clustersToFilter.size());
		
		Iterator<BottomUpSearchResultCluster> clusterIterator = clustersToFilter.iterator();
		while (clusterIterator.hasNext()) {
			BottomUpSearchResultCluster clusterToFilter = clusterIterator.next();
			
			if ( ! passesCutoff(clusterToFilter)) {
				clusterIterator.remove();
			}
		}
		
		BottomUpSearchResultClustering filteredClustering = 
			new BottomUpSearchResultClustering(clustersToFilter);
		
		DebugLog.write(filteredClustering.getClusters().size());
		
		return filteredClustering;
	}

	
	private boolean passesCutoff(BottomUpSearchResultCluster clusterToFilter) {
		
		// 'searchPath' always passes (returns true) if:
		// the terminal scheme in the search path is at a level higher than
		// 'mustBeLevelN_orLower'.  A 'null' value of 'mustBeLevelN_orLower'
		// means this ParameterSetting is not using the 'mustBeLevelN_orLower'
		// parameter.
		if (parameterSetting.aveSchemeInClusterMustBeLevelN_orLessToFilter != null) {
			if (clusterToFilter.getAveLeafLevel() > 
					parameterSetting.aveSchemeInClusterMustBeLevelN_orLessToFilter) {
				return true;
			}
		}
		
		if (parameterSetting.cutoffOverAveCoveredByLeaves) {
			double aveTypesCovered = clusterToFilter.getAveTypesCoveredByLeaves();
			
			if (aveTypesCovered >= parameterSetting.clusterMustCoverAtLeastNTypes) {
				return true;
			}
		}
		
		if (parameterSetting.cutoffOverTotalCovered) {
			// The number of types covered is simply the number of stems times the number of affixes
			int typesCovered = clusterToFilter.getCoveredTypes().size();
			
			if (typesCovered >= parameterSetting.clusterMustCoverAtLeastNTypes) {
				return true;
			}
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
