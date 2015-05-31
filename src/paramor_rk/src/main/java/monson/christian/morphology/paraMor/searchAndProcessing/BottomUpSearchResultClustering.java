/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearch.BottomUpParameterSetting;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster.CalculateSimilarityAs;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster.ClusterWRT;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameterSetting;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameters;

public class BottomUpSearchResultClustering implements Serializable {
	
	public static class Parameters extends SearchStepParameters implements Serializable {
		private static final long serialVersionUID = 1L;

		// See the ParaMor Readme file (as of Nov. 2009) for a description of what these parameters do
		//
		private CalculateSimilarityAs calculateSimilarityAs  = CalculateSimilarityAs.COSINE;
		private ClusterWRT clusterWRT                        = ClusterWRT.AFFIX_STEM_PAIRS;
		
		private boolean filterAtClusterTime                  = false;
		
		private boolean discriminativeClustering             = false;

		private boolean networkBasedDiscriminativeClustering = true;
		private int level2SchemeMustHaveAtLeastNStems        = 1;

		private boolean affixesInCommonRequired              = false;
		private boolean affixesInCommonForbidden             = false;

		private List<Integer> childTypesCoveredCutoffs       = new ArrayList<Integer>();
		
		private boolean useMergeCredits                      = true;
		private int numOfPositiveMergeCredits                = 1;
		
		private boolean tieInTypeCoveredFiltering			 = true;

		
		public Parameters() {
			childTypesCoveredCutoffs.add(TypesCoveredClusterFilter.DEFAULT_CLUSTER_MUST_COVER_AT_LEAST_N_TYPES);
		}
		
		public boolean getFilterAtClusterTime() {
			return filterAtClusterTime;
		}
		
		public BottomUpSearchResultCluster.CalculateSimilarityAs getCalculateSimilarityAs() {
			return calculateSimilarityAs;
		}

		public BottomUpSearchResultCluster.ClusterWRT getClusterWRT() {
			return clusterWRT;
		}

		public boolean getAffixesInCommonForbidden() {
			return affixesInCommonForbidden;
		}

		public boolean getAffixesInCommonRequired() {
			return affixesInCommonRequired;
		}

		public boolean getDiscriminativeClustering() {
			return discriminativeClustering;
		}

		public boolean getNetworkBasedDiscriminativeClustering() {
			return networkBasedDiscriminativeClustering;
		}
		
		public int getLevel2SchemeMustHaveAtLeastNStems() {
			return level2SchemeMustHaveAtLeastNStems;
		}
		
		public List<Integer> getChildTypesCoveredCutoffs() {
			return childTypesCoveredCutoffs;
		}

		public int getNumOfPositiveMergerCredits() {
			return numOfPositiveMergeCredits;
		}

		public boolean getUseMergeCredits() {
			return useMergeCredits;
		}
		
		public boolean getTieInTypeCoveredFiltering() {
			return tieInTypeCoveredFiltering;
		}


		public void setNumOfPositiveMergerCredits(int numOfPositiveMergerCredits) {
			this.numOfPositiveMergeCredits = numOfPositiveMergerCredits;
		}
		
		public void setUseMergeCredits(boolean useMergeCredits) {
			this.useMergeCredits = useMergeCredits;
		}

		public void setFilterAtClusterTime(boolean filterAtClusterTime) {
			this.filterAtClusterTime = filterAtClusterTime;
		}
		
		public void setChildTypesCoveredCutoffs(List<Integer> childTypesCoveredCutoffs) {
			this.childTypesCoveredCutoffs = childTypesCoveredCutoffs;
		}

		public void setLevel2SchemeMustHaveAtLeastNStems(
				int level2SchemeMustHaveAtLeastNStems) {
			this.level2SchemeMustHaveAtLeastNStems = level2SchemeMustHaveAtLeastNStems;
		}

		public void setCalculateSimilarityAs(
				BottomUpSearchResultCluster.CalculateSimilarityAs calculate_similarity_as) {
			
			this.calculateSimilarityAs = calculate_similarity_as;
		}
		
		public void setAffixesInCommonForbidden(boolean affixesInCommonForbidden) {
			this.affixesInCommonForbidden = affixesInCommonForbidden;
		}

		public void setAffixesInCommonRequired(boolean affixesInCommonRequired) {
			this.affixesInCommonRequired = affixesInCommonRequired;
		}

		public void setDiscriminativeClustering(boolean discriminativeClustering) {
			this.discriminativeClustering = discriminativeClustering;
		}

		public void setNetworkBasedDiscriminativeClustering(
				boolean networkBasedDiscriminativeClustering) {
			this.networkBasedDiscriminativeClustering = networkBasedDiscriminativeClustering;
		}

		public void setClusterWRT(BottomUpSearchResultCluster.ClusterWRT clusterWRT) {
			this.clusterWRT = clusterWRT;
		}
		
		public void setTieInTypeCoveredFiltering(boolean tieInTypeCoveredFiltering) {
			this.tieInTypeCoveredFiltering = tieInTypeCoveredFiltering;
		}
		
		@Override
		public String toString() {
			String toReturn = "";
			
			toReturn += "Calculate Similarity As:                 " + calculateSimilarityAs + String.format("%n");
			toReturn += "Cluster with Respect to:                 " + clusterWRT + String.format("%n");
			toReturn += "Filter at Cluster Time:                  " + filterAtClusterTime + String.format("%n");
			toReturn += "Discriminative Clustering:               " + discriminativeClustering + String.format("%n");
			toReturn += "Network Based Discriminative Clustering: " + networkBasedDiscriminativeClustering + String.format("%n");
			toReturn += "  - level 2 Scheme must have >=N Stems:  " + level2SchemeMustHaveAtLeastNStems + String.format("%n");
			toReturn += "Affixes in Common Required:              " + affixesInCommonRequired + String.format("%n");
			toReturn += "Affixes in Common Forbidden:             " + affixesInCommonForbidden + String.format("%n");
			toReturn += "At least one child must cover >N Types:  " + childTypesCoveredCutoffs + String.format("%n");
			toReturn += "Use Merge Credits:                       " + useMergeCredits + String.format("%n");
			toReturn += "  - # of Positive Merge Credits          " + numOfPositiveMergeCredits + String.format("%n");
			toReturn += "Tie In Type Covered Filtering            " + tieInTypeCoveredFiltering;
			
			return toReturn;
		}
		
		public String toStringAsComment() {
			String toReturn = "";
			
			toReturn += "# Calculate Similarity As:                 " + calculateSimilarityAs + String.format("%n");
			toReturn += "# Cluster with Respect to:                 " + clusterWRT + String.format("%n");
			toReturn += "# Filter at Cluster Time:                  " + filterAtClusterTime + String.format("%n");
			toReturn += "# Discriminative Clustering:               " + discriminativeClustering + String.format("%n");
			toReturn += "# Network Based Discriminative Clustering: " + networkBasedDiscriminativeClustering + String.format("%n");
			toReturn += "#   - level 2 Scheme must have >=N Stems:  " + level2SchemeMustHaveAtLeastNStems + String.format("%n");
			toReturn += "# Affixes in Common Required:              " + affixesInCommonRequired + String.format("%n");
			toReturn += "# Affixes in Common Forbidden:             " + affixesInCommonForbidden + String.format("%n");
			toReturn += "# At least one child must cover >N Types:  " + childTypesCoveredCutoffs + String.format("%n");
			toReturn += "# Use Merge Credits:                       " + useMergeCredits + String.format("%n");
			toReturn += "#   - # of Positive Merge Credits          " + numOfPositiveMergeCredits + String.format("%n");
			toReturn += "# Tie in Type Covered Filtering            " + tieInTypeCoveredFiltering;
			
			return toReturn;
		}


		public Iterator<SearchStepParameterSetting> iterator() {
			List<SearchStep.SearchStepParameterSetting> parameterSettings = 
				new ArrayList<SearchStep.SearchStepParameterSetting>();
			
			for (Integer childTypesCoveredCutoff : childTypesCoveredCutoffs) {
				parameterSettings.add(
					new BottomUpSearchResultClustering.ParameterSetting(
							calculateSimilarityAs,
							clusterWRT,
							filterAtClusterTime,
							discriminativeClustering,
							networkBasedDiscriminativeClustering,
							level2SchemeMustHaveAtLeastNStems,
							affixesInCommonRequired,
							affixesInCommonForbidden,
							childTypesCoveredCutoff,
							useMergeCredits,
							numOfPositiveMergeCredits,
							tieInTypeCoveredFiltering));
			}
			
			return parameterSettings.iterator();
		}

		@Override
		protected String getParametersString() {
			return this.toString();
		}
		
		@Override
		protected String getParametersStringAsComment() {
			return this.toStringAsComment();
		}


		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Calculate Similarity As, Cluster WRT, Filter at Cluster Time, " +
				   "Discriminative Clustering, " +
				   "Network Based Discriminative Clustering, " +
				   "level 2 Scheme must have >= N stems in network, " +
				   "Affixes In Common Required, Affixes in Common Forbidden, " +
				   "At least one child must cover >N types, " +
				   "Use Merge Credits, # of Positive Merge Credits, " +
				   "Tie in Type Covered Filtering";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting implements Serializable {
		private static final long serialVersionUID = 1L;
		
		CalculateSimilarityAs calculateSimilarityAs  = CalculateSimilarityAs.COSINE;
		ClusterWRT clusterWRT                        = ClusterWRT.AFFIX_STEM_PAIRS;
		
		boolean filterAtClusterTime                  = false;
		
		boolean discriminativeClustering             = false;
		boolean networkBasedDiscriminativeClustering = true;
		int level2SchemeMustHaveAtLeastNStems        = 1; // to further refine 'networkBasedDiscriminativeClustering'
		
		boolean affixesInCommonRequired              = false;
		boolean affixesInCommonForbidden             = false;
		
		int childTypesCoveredCutoff                  = TypesCoveredClusterFilter.DEFAULT_CLUSTER_MUST_COVER_AT_LEAST_N_TYPES;

		private boolean useMergeCredits              = true;
		private int numOfPositiveMergeCredits        = 1;
		
		private boolean tieInTypeCoveredFiltering    = true;

		
		public ParameterSetting() {}
		
		public ParameterSetting(CalculateSimilarityAs calculateSimilarityAs, 
						  		ClusterWRT clusterWRT,
						  		boolean filterAtClusterTime,
								boolean discriminativeClustering,
								boolean networkBasedDiscriminativeClustering,
								int level2SchemeMustHaveAtLeastNStems,
								boolean affixesInCommonRequired,
								boolean affixesInCommonForbidden,
								int childTypesCoveredCutoff,
								boolean useMergeCredits,
								int numOfPositiveMergeCredits,
								boolean tieInTypeCoveredFiltering) {
			
			this.calculateSimilarityAs = calculateSimilarityAs;
			this.clusterWRT = clusterWRT;
			this.filterAtClusterTime = filterAtClusterTime;
			
			this.discriminativeClustering             = discriminativeClustering;
			this.networkBasedDiscriminativeClustering = networkBasedDiscriminativeClustering;
			this.level2SchemeMustHaveAtLeastNStems    = level2SchemeMustHaveAtLeastNStems;
			this.affixesInCommonRequired              = affixesInCommonRequired;
			this.affixesInCommonForbidden             = affixesInCommonForbidden;
			this.childTypesCoveredCutoff              = childTypesCoveredCutoff;
			this.useMergeCredits                      = useMergeCredits;
			this.numOfPositiveMergeCredits            = numOfPositiveMergeCredits;
			this.tieInTypeCoveredFiltering            = tieInTypeCoveredFiltering;
		}

		public BottomUpSearchResultCluster.CalculateSimilarityAs getCalculateSimilarityAs() {
			return calculateSimilarityAs;
		}

		public void setCalculateSimilarityAs(
				BottomUpSearchResultCluster.CalculateSimilarityAs calculateSimilarityAs) {
			
			this.calculateSimilarityAs = calculateSimilarityAs;
		}

		public BottomUpSearchResultCluster.ClusterWRT getClusterWRT() {
			return clusterWRT;
		}

		public void setClusterWRT(BottomUpSearchResultCluster.ClusterWRT clusterWRT) {
			this.clusterWRT = clusterWRT;
		}
		
		public boolean getFilterAtClusterTime() {
			return filterAtClusterTime;
		}

		public void setFilterAtClusterTime(boolean filterAtClusterTime) {
			this.filterAtClusterTime = filterAtClusterTime;
		}

		public boolean getAffixesInCommonForbidden() {
			return affixesInCommonForbidden;
		}

		public boolean getAffixesInCommonRequired() {
			return affixesInCommonRequired;
		}

		public boolean getDiscriminativeClustering() {
			return discriminativeClustering;
		}

		public boolean getNetworkBasedDiscriminativeClustering() {
			return networkBasedDiscriminativeClustering;
		}
		
		public int getLevel2SchemeMustHaveAtLeastNStems() {
			return level2SchemeMustHaveAtLeastNStems;
		}
		
		public int getChildTypesCoveredCutoff() {
			return childTypesCoveredCutoff;
		}

		public int getNumOfPositiveMergeCredits() {
			return numOfPositiveMergeCredits;
		}

		public boolean getUseMergeCredits() {
			return useMergeCredits;
		}
		
		public boolean getTieInTypeCoveredFilter() {
			return tieInTypeCoveredFiltering;
		}
		
		
		public void setTieInTypeCoveredFilter() {
			this.tieInTypeCoveredFiltering = tieInTypeCoveredFiltering;
		}
		
		public void setNumOfPositiveMergeCredits(int numOfPositiveMergeCredits) {
			this.numOfPositiveMergeCredits = numOfPositiveMergeCredits;
		}

		public void setUseMergeCredits(boolean useMergeCredits) {
			this.useMergeCredits = useMergeCredits;
		}

		public void setChildTypesCoveredCutoff(int childTypesCoveredCutoff) {
			this.childTypesCoveredCutoff = childTypesCoveredCutoff;
		}

		public void setLevel2SchemeMustHaveAtLeastNStems(
				int level2SchemeMustHaveAtLeastNStems) {
			this.level2SchemeMustHaveAtLeastNStems = level2SchemeMustHaveAtLeastNStems;
		}

		public void setAffixesInCommonForbidden(boolean affixesInCommonForbidden) {
			this.affixesInCommonForbidden = affixesInCommonForbidden;
		}

		public void setAffixesInCommonRequired(boolean affixesInCommonRequired) {
			this.affixesInCommonRequired = affixesInCommonRequired;
		}

		public void setDiscriminativeClustering(boolean discriminativeClustering) {
			this.discriminativeClustering = discriminativeClustering;
		}

		public void setNetworkBasedDiscriminativeClustering(
				boolean networkBasedDiscriminativeClustering) {
			this.networkBasedDiscriminativeClustering = networkBasedDiscriminativeClustering;
		}

		@Override
		public String toString() {
			String toReturn = "";
			
			toReturn += "Calculate Similarity As: " + calculateSimilarityAs + String.format("%n");
			toReturn += "Cluster with Respect to: " + clusterWRT + String.format("%n");
			toReturn += "Filter at Cluster Time:  " + filterAtClusterTime + String.format("%n");
			toReturn += "Discriminative Clustering:               " + discriminativeClustering + String.format("%n");
			toReturn += "Network Based Discriminative Clustering: " + networkBasedDiscriminativeClustering + String.format("%n");
			toReturn += "  - level 2 Scheme must have >=N Stems:  " + level2SchemeMustHaveAtLeastNStems + String.format("%n");
			toReturn += "Affixes in Common Required:              " + affixesInCommonRequired + String.format("%n");
			toReturn += "Affixes in Common Forbidden:             " + affixesInCommonForbidden + String.format("%n");
			toReturn += "At least one child must cover >N Types:  " + childTypesCoveredCutoff + String.format("%n");
			toReturn += "Use Merge Credits:                       " + useMergeCredits + String.format("%n");
			toReturn += "  - # of Positive Merge Credits          " + numOfPositiveMergeCredits + String.format("%n");
			toReturn += "Tie in Type Covered Filter               " + tieInTypeCoveredFiltering;
			
			return toReturn;
		}

		@Override
		public String getStringForSpreadsheet() {
			return calculateSimilarityAs                + ", " + 
				   clusterWRT                           + ", " +
				   filterAtClusterTime                  + ", " +
				   discriminativeClustering             + ", " +
				   networkBasedDiscriminativeClustering + ", " +
				   level2SchemeMustHaveAtLeastNStems    + ", " +
				   affixesInCommonRequired              + ", " +
				   affixesInCommonForbidden             + ", " +
				   childTypesCoveredCutoff              + ", " +
				   useMergeCredits                      + ", " +
				   numOfPositiveMergeCredits            + ", " +
				   tieInTypeCoveredFiltering;
		}
		
		// NOTE: This only uniqeifies with respect to the childTypesCoveredCutoff
		//       parameter
		@Override
		public String getFilenameUniqueifier() {
			String toReturn = "";
			toReturn += "ClusterSize" + childTypesCoveredCutoff;
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		protected String getParameterString() {
			return this.toString();
		}
		
		@Override
		public int compareTo(SearchStepParameterSetting thatSearchStepParameterSetting) {
			if ( ! (thatSearchStepParameterSetting instanceof BottomUpParameterSetting)) {
				super.compareTo(thatSearchStepParameterSetting);
			}

			ParameterSetting that = (ParameterSetting)thatSearchStepParameterSetting;
			
			if (this.calculateSimilarityAs != that.calculateSimilarityAs) {
				return this.calculateSimilarityAs.compareTo(that.calculateSimilarityAs);
			}

			if (this.clusterWRT != that.clusterWRT) {
				return this.clusterWRT.compareTo(that.clusterWRT);
			}

			if (this.filterAtClusterTime != that.filterAtClusterTime) {
				if (this.filterAtClusterTime) {
					return -1;
				}
				return 1;
			}
			
			if (this.discriminativeClustering != that.discriminativeClustering) {
				if (this.discriminativeClustering) {
					return -1;
				}
				return 1;
			}
			
			if (this.networkBasedDiscriminativeClustering != that.networkBasedDiscriminativeClustering) {
				if (this.networkBasedDiscriminativeClustering) {
					return -1;
				}
				return 1;
			}
			
			if (this.level2SchemeMustHaveAtLeastNStems != that.level2SchemeMustHaveAtLeastNStems) {
				if (this.level2SchemeMustHaveAtLeastNStems < that.level2SchemeMustHaveAtLeastNStems) {
					return -1;
				}
				return 1;
			}

			if (this.affixesInCommonRequired != that.affixesInCommonRequired) {
				if (this.affixesInCommonRequired) {
					return -1;
				}
				return 1;
			}
			
			if (this.affixesInCommonForbidden != that.affixesInCommonForbidden) {
				if (this.affixesInCommonForbidden) {
					return -1;
				}
				return 1;
			}

			if (this.childTypesCoveredCutoff != that.childTypesCoveredCutoff) {
				if (this.childTypesCoveredCutoff < that.childTypesCoveredCutoff) {
					return -1;
				}
				return 1;
			}
			
			if (this.useMergeCredits != that.useMergeCredits) {
				if (this.useMergeCredits) {
					return -1;
				}
				return 1;
			}
			
			if (this.numOfPositiveMergeCredits != that.numOfPositiveMergeCredits) {
				if (this.numOfPositiveMergeCredits < that.numOfPositiveMergeCredits) {
					return -1;
				}
				return 1;
			}
			
			if (this.tieInTypeCoveredFiltering != that.tieInTypeCoveredFiltering) {
				if (this.tieInTypeCoveredFiltering) {
					return -1;
				}
				return 1;
			}
			

			return 0;
		}

	}
	
	/* 
	 * ClusterPair serves a very important role. Since there are |current_clusters|^2
	 * potential new clusters at every step, creating a full blown 'Cluster' for each
	 * of these potential clusters runs out of memory.  Memory disappears because each
	 * full 'Cluster' stores the Affixes, Stems, and Types that it covers.
	 * But a ClusterPair stores nothing but the two sub-clusters and the similarity
	 * value of those two clusters.
	 * 
	 * BUT IN THE END I FORCED ALL THIS FUNCTIONALITY INTO 'CLUSTER' ITSELF.  IT WORKS
	 * NOW AND I WANT TO GET ON TO REAL WORK SO I AM NOT GOING TO REINTRODUCE
	 * CLUSTERpAIR.
	 *
	private class ClusterPair implements Serializable, Comparable<ClusterPair> {
		private static final long serialVersionUID = 1L;
		
		BottomUpSearchResultCluster orthographicallySmallerCluster;
		BottomUpSearchResultCluster orthographicallyLargerCluster;
		
		CalculateSimilarityAs calculateSimilarityAs = null;
		Double similarity = null;
		
		public ClusterPair(BottomUpSearchResultCluster clusterA, 
						   BottomUpSearchResultCluster clusterB,
						   CalculateSimilarityAs calculateSimilarityAs) {
			
			if (clusterA.compareTo(clusterB) < 0) {
				orthographicallySmallerCluster = clusterA;
				orthographicallyLargerCluster  = clusterB;
			} else {
				orthographicallySmallerCluster = clusterB;
				orthographicallyLargerCluster  = clusterA;				
			}
			
			this.calculateSimilarityAs = calculateSimilarityAs;
			similarity = 
				orthographicallySmallerCluster.computeSimilarity(orthographicallyLargerCluster,
																 calculateSimilarityAs);
		}

		// Sorts into REVERSE (DESCENDING) order
		public int compareTo(ClusterPair that) {
			if (this.similarity < that.similarity) {
				return 1;
			}
			if (this.similarity > that.similarity) {
				return -1;
			}
			
			int orthographicallySmallerClustersCompared = 
				this.orthographicallySmallerCluster.compareTo(that.orthographicallySmallerCluster);
			if (orthographicallySmallerClustersCompared != 0) {
				return orthographicallySmallerClustersCompared;
			}

			int orthographicallyLargerClustersCompared = 
				this.orthographicallyLargerCluster.compareTo(that.orthographicallyLargerCluster);
			if (orthographicallyLargerClustersCompared != 0) {
				return orthographicallyLargerClustersCompared;
			}

			return 0;
		}
		
		@Override
		public String toString() {
			String toReturn = "";
			toReturn += String.format("------------------------%n");
			toReturn += String.format("Similarity: " + similarity + "%n%n");
			toReturn += String.format("Calculate Similarity As: " + calculateSimilarityAs + "%n%n");
			toReturn += orthographicallySmallerCluster;
			toReturn += String.format("%n%n");
			toReturn += orthographicallyLargerCluster;
			
			return toReturn;
		}
		
	}
	*/

	private static final long serialVersionUID = 1L;

	private static final String SEARCH_STEP_NAME = "Clustering";
	
	List<BottomUpSearchResultCluster> clusters = new ArrayList<BottomUpSearchResultCluster>();
	
	// This Map allows us to:
	// if we decide to merge cluster A with cluster B we can remove all other
	// ClusterPairs that involve A and B from the priority queue of Clusters to merge.
	//
	// Having a TreeMap also lets us keep around a sorted list of the clusters.
	// NOTE: toString actually gets its clusters from this Map!! Hence we ASSUME
	// that 'clusters' and 'clustersToMergePairs' will always be in sync!!
	private Map<BottomUpSearchResultCluster, 
				List<BottomUpSearchResultCluster>> currentClustersToNewPossibleClusters = 
		new HashMap<BottomUpSearchResultCluster, 
					List<BottomUpSearchResultCluster>>();
	
	private SortedSet<BottomUpSearchResultCluster> mergerQueue = 
		new TreeSet<BottomUpSearchResultCluster>(
				BottomUpSearchResultCluster.byDecreasingInternalSimilarity);
	
	private ParameterSetting parameterSetting = null;
	
	// 
	private Map<Affix, Set<Affix>> discriminativeFilter = new HashMap<Affix, Set<Affix>>();
	
	private VirtualPartialOrderNetwork searchNetwork;
	
	Map<Affix, Map<Affix, Integer>> previouslyComputedLevel2Sizes = new HashMap<Affix, Map<Affix, Integer>>();
	
	// Build a separate cluster for each SearchPath in <code>searchResults</code>
	public BottomUpSearchResultClustering(Collection<SearchPath> searchResults,
										  ParameterSetting parameterSetting,
										  VirtualPartialOrderNetwork searchNetwork) {
		
		this.parameterSetting = parameterSetting;
		
		this.searchNetwork = searchNetwork;
		
		int clusterInitializationCounter = 0;
		for (SearchPath searchPath : searchResults) {
			clusterInitializationCounter++;
			if ((clusterInitializationCounter%100) == 0) {
				System.err.println("Initialized " + clusterInitializationCounter + 
								   " clusters of " + searchResults.size());
			}
			
			BottomUpSearchResultCluster bottomUpSearchResultCluster = 
				new BottomUpSearchResultCluster(searchPath, parameterSetting);
			
			clusters.add(bottomUpSearchResultCluster);
			currentClustersToNewPossibleClusters.put(bottomUpSearchResultCluster, 
									 new ArrayList<BottomUpSearchResultCluster>());
		}
		
		if (parameterSetting.getDiscriminativeClustering()) {
			initializeDiscriminativeFilter();
		}
		
		initializeClusterSimilarities();
	}
	
	public BottomUpSearchResultClustering(BottomUpSearchResultClustering oldClustering) {
		this.clusters = new ArrayList<BottomUpSearchResultCluster>(oldClustering.clusters);
	}

	public BottomUpSearchResultClustering(List<BottomUpSearchResultCluster> clustersToFilter) {
		this.clusters = clustersToFilter;
	}

	public static String getNameStatic() {
		return SEARCH_STEP_NAME;
	}
	
	public SetOfMorphemes<Affix> getAllCoveredAffixes() {
		SetOfMorphemes<Affix> allCoveredAffixes = new SetOfMorphemes<Affix>();
		for (BottomUpSearchResultCluster cluster : clusters) {
			allCoveredAffixes.add(cluster.getCoveredAffixes());
		}
		return allCoveredAffixes;
	}

	private void initializeDiscriminativeFilter() {
		for (BottomUpSearchResultCluster cluster : clusters) {
			
			Set<Affix> affixesInCluster = cluster.getCoveredAffixes();
			for (Affix affixA : affixesInCluster) {
				for (Affix affixB : affixesInCluster) {
					if (affixA.equals(affixB)) {
						continue;
					}
					if ( ! discriminativeFilter.containsKey(affixA)) {
						discriminativeFilter.put(affixA, new HashSet<Affix>());
					}
					Set<Affix> affixesThatOccurWithAffixA = discriminativeFilter.get(affixA);
					affixesThatOccurWithAffixA.add(affixB);
				}
			}
		}
	}

	private int mergeCount = 0;
	public boolean cluster() {

		mergeCount = 0;
		
		System.err.println(); 
		System.err.println();
		System.err.println("Begin Clustering. " + mergerQueue.size() + " mergers to consider.");
		System.err.println();

		while (shouldContinueClustering()) {

			merge();
		}
		
		return true;
	}
	
	private boolean shouldContinueClustering() {
		if ((mergerQueue == null) ||
			(mergerQueue.size() == 0)) {
			
			return false;
		}
		
		return true;
	}

	private void merge() {
		// The first element in the sortedSet is the element with the *largest* similarity
		// because the natural order of ClusterPairs sorts into descending order.
		// NOTE: pairToMerge will be removed from mergerQueue along with the other irrelevant
		// merge pairs later in this algorithm.
		BottomUpSearchResultCluster bestNewCluster = mergerQueue.first();  
		mergerQueue.remove(bestNewCluster);  // actually pop the top off the heap 
		
		if (parameterSetting.getFilterAtClusterTime()) {
			
			// Don't merge if we filter at cluster time
			if (getAffixesCommonToBothChildren(bestNewCluster).size() == 0) {
				filterOneChildOf(bestNewCluster);
				return; 
			}
			
			// The logic of filtering at cluster time was that we would initially
			// compare schemes by type similarity, regardless of other possible
			// restrictions on the clusters. And then we greedily compute the
			// affix similarities of the clusters with the highest type similarities.
			// Only if a cluster has high type AND affix similarities do we
			// check other restrictions such as network based discriminitive
			// criteria.
			if ( ! passesInexpensiveClusterRestrictions(
					bestNewCluster.getChildWithLargerInternalSimilarity(),
					bestNewCluster.getChildWithSmallerInternalSimilarity())) {
				return;
			}			
			if ( ! passesExpensiveClusterRestrictions(bestNewCluster)) {
				return;
			}
		}
		
		performMerge(bestNewCluster);
	}
	
	private void filterOneChildOf(BottomUpSearchResultCluster cluster) {
		
		int DEBUG = 1;
		
		BottomUpSearchResultCluster childA = cluster.getChildWithLargerInternalSimilarity();
		BottomUpSearchResultCluster childB = cluster.getChildWithSmallerInternalSimilarity();
		
		double leftwardEntropyOfChildA = childA.getAveLeftwardEntropyOfLeaves();
		double leftwardEntropyOfChildB = childB.getAveLeftwardEntropyOfLeaves();
		
		// The child with the smaller entropy is less like a morpheme boundary, so discard it.
		BottomUpSearchResultCluster childToPermanentlyFilter;
		BottomUpSearchResultCluster childToKeep;
		double smallerEntropy;
		double largerEntropy;
		if (leftwardEntropyOfChildA < leftwardEntropyOfChildB) {
			childToPermanentlyFilter = childA;
			childToKeep = childB;
			smallerEntropy = leftwardEntropyOfChildA;
			largerEntropy = leftwardEntropyOfChildB;
		} else {
			childToPermanentlyFilter = childB;
			childToKeep = childA;
			smallerEntropy = leftwardEntropyOfChildB;
			largerEntropy = leftwardEntropyOfChildA;
		}
		
		
		if (DEBUG > 0 ) {
			System.err.println();
			System.err.println("Entropies: " + smallerEntropy + " < " + largerEntropy);
			System.err.println("Permanently filtering the cluster:");
			System.err.println(childToPermanentlyFilter);
			System.err.println("Because the following cluster has larger entropy:");
			System.err.println(childToKeep);
		}
		
		// Remove the the 'bad' child permanently from record.
		removeClusterFromDataStructures(childToPermanentlyFilter);
	}

	/**
	 * @param cluster
	 */
	private void removeClusterFromDataStructures(BottomUpSearchResultCluster cluster) {
		clusters.remove(cluster);
		mergerQueue.removeAll(currentClustersToNewPossibleClusters.get(cluster));
		
		// Let C be a cluster different from 'cluster'.
		// in clustersToMergePairs.get(C) there are still clusters C2 where
		// one of the children of C2 is 'cluster'.  But I
		// don't think this is actually a problem.  At some point later when
		// I try to remove C2 from the mergerQueue,  I'll find C2 is simply not
		// present in the Queue and nothing will break. (I would try to remove C2 from the mergerQueue
		// when I perform the immediately preceeding line:
		//  'mergerQueue.removeAll(currentClustersToNewPossibleClusters.get(C.getChildWithSmallerInteralSimilarity()));'
		// by passing in C to this method.
		currentClustersToNewPossibleClusters.remove(cluster);
	}

	/**
	 * @param bestNewCluster
	 */
	private void performMerge(BottomUpSearchResultCluster bestNewCluster) {
		mergeCount++;
		
		if ((mergeCount % 100) == 0) {
			System.err.println(); 
			System.err.println();
			System.err.println(mergeCount + "th Merger.  " + 
							   mergerQueue.size() + " mergers are left to consider.");
			System.err.println();
			System.err.println(bestNewCluster);
		}
		
		// Remove the old clusters from all the data structures
		removeClusterFromDataStructures(bestNewCluster.getChildWithLargerInternalSimilarity());
		removeClusterFromDataStructures(bestNewCluster.getChildWithSmallerInternalSimilarity());
		
		
		// Insert the new cluster into all the data structures
		clusters.add(bestNewCluster);
		currentClustersToNewPossibleClusters.put(
				bestNewCluster, 
				new ArrayList<BottomUpSearchResultCluster>());
		
		for (int clusterIndex=0; clusterIndex<clusters.size()-1; clusterIndex++) {
			BottomUpSearchResultCluster otherCluster = clusters.get(clusterIndex);
			createAndInsertAPotentialCluster(bestNewCluster, otherCluster);			
		}
	}
	

	private void initializeClusterSimilarities() {
		long similarityCount = 0;
		long totalSimilarities = 
			((((long)clusters.size()) * 
			  ((long)clusters.size())) - 
			 ((long)clusters.size())) / 
			((long)2);
		
		long intervalsAtWhichToPrintProgress = totalSimilarities / ((long)300);
		System.err.println();
		System.err.println("Will print progress every, " + intervalsAtWhichToPrintProgress + 
						   " cluster initializations");
		
		for (int smallerIndex=0; smallerIndex<clusters.size()-1; smallerIndex++) {
			BottomUpSearchResultCluster clusterWithSmallerIndex = clusters.get(smallerIndex);
			
			for (int largerIndex=smallerIndex+1; largerIndex<clusters.size(); largerIndex++) {
				BottomUpSearchResultCluster clusterWithLargerIndex = 
					clusters.get(largerIndex);
				
				similarityCount++;
				if ((intervalsAtWhichToPrintProgress == 0) ||
					((similarityCount % intervalsAtWhichToPrintProgress) == 0)) {
					System.err.println();
					System.err.println("Initialized " + similarityCount + 
									   " merges of " + totalSimilarities);
					System.err.println(clusterWithSmallerIndex);
					System.err.println(clusterWithLargerIndex);
				}
				
				createAndInsertAPotentialCluster(clusterWithSmallerIndex, clusterWithLargerIndex);
			}
		}		
	}

	/**
	 * @param orthographicallySmallerCluster
	 * @param orthographicallyLargerCluster
	 */
	private void createAndInsertAPotentialCluster(BottomUpSearchResultCluster clusterA, 
												  BottomUpSearchResultCluster clusterB) {
		
		if ( ( ! parameterSetting.getFilterAtClusterTime()) && 
			 ( ! passesInexpensiveClusterRestrictions(clusterA, clusterB))) {
			return;
		}
		
		
		// Currently, I officially create the cluster and then later decide 
		// whether or not to keep around the cluster based on the similarity
		// of the cluster's children, and on whatever cluster restrictions
		// are in place for this experiment. 
		//
		BottomUpSearchResultCluster newPossibleCluster = 
			new BottomUpSearchResultCluster(
					clusterA, 
					clusterB, 
					parameterSetting);
		
		if (clusterWorthyOfPossiblyMerging(newPossibleCluster)) {
		
			currentClustersToNewPossibleClusters.get(clusterA).add(newPossibleCluster);
			currentClustersToNewPossibleClusters.get(clusterB).add(newPossibleCluster);
		
			mergerQueue.add(newPossibleCluster);
		}
	}

	private boolean 
	passesInexpensiveClusterRestrictions(BottomUpSearchResultCluster clusterA, 
			  							 BottomUpSearchResultCluster clusterB) {
		
		if (parameterSetting.getChildTypesCoveredCutoff() > 0) {
			boolean atLeastOneClusterPassesTypesCoveredCutoff = 
				atLeastOneClusterPassesTypesCoveredCutoff(clusterA, clusterB);
			if ( ! atLeastOneClusterPassesTypesCoveredCutoff) {
				return false;
			}
		}
		
		if (parameterSetting.getUseMergeCredits()) {
			boolean passesMergerCredit = passesMergerCredit(clusterA, clusterB);
			if ( ! passesMergerCredit) {
				return false;
			}
		}
		
		if (parameterSetting.getDiscriminativeClustering()) {
			boolean passesDiscriminativeCriterion = 
				passesDiscriminativeCriterion(clusterA, clusterB, discriminativeFilter);
			if ( ! passesDiscriminativeCriterion) {
				return false;
			}
		}
		
		if (parameterSetting.getAffixesInCommonRequired()) {
			boolean passesAffixesInCommonRequiredRestriction = 
				passesAffixesInCommonRequiredRestriction(clusterA, clusterB);
			if ( ! passesAffixesInCommonRequiredRestriction) {
				return false;
			}
		}
		
		if (parameterSetting.getAffixesInCommonForbidden()) {
			boolean passesAffixesInCommonForbiddenRestriction = 
				passesNoAffixesInCommonAllowedRestriction(clusterA, clusterB);
			if ( ! passesAffixesInCommonForbiddenRestriction) {
				return false;
			}
		}
		
		return true;
	}

	//
	// NOTE: CAN MODIFY previouslyComputedLevel2Sizes !!!!!!
	//
	boolean 
	passesExpensiveClusterRestrictions(BottomUpSearchResultCluster cluster) {
		
		if (parameterSetting.getNetworkBasedDiscriminativeClustering()) {
			boolean passesNetworkBasedDiscriminativeCriterion = 
				passesNetworkBasedDiscriminativeClustering(cluster);
			if ( ! passesNetworkBasedDiscriminativeCriterion) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean clusterWorthyOfPossiblyMerging(BottomUpSearchResultCluster cluster) {
		
		if (cluster.getInternalSimilarity() > 0.0) {
			
			// Don't check cluster restrictions at this time if
			// we are filtering at cluster time.
			if (parameterSetting.getFilterAtClusterTime() || 
				passesExpensiveClusterRestrictions(cluster)) {
				
				return true;
			}
		}
		return false;
	}

	private boolean passesMergerCredit(BottomUpSearchResultCluster clusterA,
									   BottomUpSearchResultCluster clusterB) {
		
		if ((clusterA.getMergeCredits() + clusterB.getMergeCredits()) < 0) {
			return false;
		}
		
		return true;
	}



	private boolean 
	atLeastOneClusterPassesTypesCoveredCutoff(BottomUpSearchResultCluster clusterA,
											  BottomUpSearchResultCluster clusterB) {
		
		clusterA.incorporateDataFromChildren_ifNotPerformedPrevisously();
		if (clusterA.getCoveredTypes().size() >	parameterSetting.getChildTypesCoveredCutoff()) {
			return true;
		}
		
		clusterB.incorporateDataFromChildren_ifNotPerformedPrevisously();
		if (clusterB.getCoveredTypes().size() >	parameterSetting.getChildTypesCoveredCutoff()) {
			return true;
		}
		
		return false;
	}


	//
	// NOTE: MODIFIES previouslyComputedLevel2Sizes !!!!!!
	//
	private boolean 
	passesNetworkBasedDiscriminativeClustering(BottomUpSearchResultCluster cluster) {
		
		int DEBUG = 0;
		
		if (DEBUG > 0) {
			System.err.println("Calculating Network Based Discriminative Clustering for the cluster");
			System.err.println(this);
		}
		
		class ByAdherentSize implements Comparator<Affix> {
			
			VirtualPartialOrderNetwork searchNetwork;
			
			ByAdherentSize(VirtualPartialOrderNetwork searchNetwork) {
				this.searchNetwork = searchNetwork;
			}
			
			public int compare(Affix a, Affix b) {
				Map<Affix, Level1Scheme> level1SchemesByAffix = searchNetwork.getLevel1SchemesByAffix();
				Level1Scheme schemeA = level1SchemesByAffix.get(a); 
				Level1Scheme schemeB = level1SchemesByAffix.get(b);
				
				if (schemeA.adherentSize() < schemeB.adherentSize()) {
					return -1;
				}
				return 1;
			}
		}
		
		ByAdherentSize byAdherentSize = new ByAdherentSize(searchNetwork);
		
		List<Affix> LsAffixes = 
			new ArrayList<Affix>(
					cluster.getChildWithLargerInternalSimilarity().getCoveredAffixes());
		Collections.sort(LsAffixes, byAdherentSize);
		List<Affix> SsAffixes = 
			new ArrayList<Affix>(
					cluster.getChildWithSmallerInternalSimilarity().getCoveredAffixes());
		Collections.sort(SsAffixes, byAdherentSize);
					
		
		for (Affix affixL : LsAffixes) {
			for (Affix affixS : SsAffixes) {
				
				if (affixL.equals(affixS)) {
					continue;
				}
				
				int sizeOfLevel2SchemeContainingAffixLAndAffixS =
					getSizeOfLevel2Scheme(affixL, affixS);
				
				if ( ! (sizeOfLevel2SchemeContainingAffixLAndAffixS >= 
							parameterSetting.getLevel2SchemeMustHaveAtLeastNStems())) {
					return false;
				}
			}
		}
		
		return true;
	} 

	//
	// NOTE: MODIFIES previouslyComputedLevel2Sizes
	//
	private int getSizeOfLevel2Scheme(Affix affixL, Affix affixS) {
		
		int DEBUG = 0;
		
		// Check to see if we already know the size of the level 2 scheme
		// containing affixS and affixL
		if (previouslyComputedLevel2Sizes.containsKey(affixS)) {
			Map<Affix, Integer> previouslyComputedSizesForAffixS = 
				previouslyComputedLevel2Sizes.get(affixS);
			
			if (previouslyComputedSizesForAffixS.containsKey(affixL)) {
				Integer sizeOfSAndL = previouslyComputedSizesForAffixS.get(affixL);
				
				return sizeOfSAndL;
			}
		}
		
		
		// We don't already know the size of the Level 2 scheme containing affixS
		// and affixL, so we have to compute it using the search network.
		
		SetOfMorphemes<Affix> affixLAndAffixS =	new SetOfMorphemes<Affix>(affixL, affixS);

		if (DEBUG > 0) {
			System.err.println("Generating the Scheme for: " + affixLAndAffixS);
		}
		
		Scheme level2SchemeContainingAffixLAndAffixS = 
			searchNetwork.generateScheme(affixLAndAffixS);
		
		if (DEBUG > 0) {
			System.err.println("  it has " + level2SchemeContainingAffixLAndAffixS.adherentSize() + " adherents");
		}
		
		int sizeOfLevl2SchemeContainingAffixLAndAffixS = 
			level2SchemeContainingAffixLAndAffixS.adherentSize();
		
		// SAVE the size of the level 2 scheme containing AffixL and AffixS
		//   save the size both ways A->S and S->A
		if ( ! previouslyComputedLevel2Sizes.containsKey(affixS)) {
			previouslyComputedLevel2Sizes.put(affixS, new HashMap<Affix, Integer>());
		}
		previouslyComputedLevel2Sizes.get(affixS).put(affixL, sizeOfLevl2SchemeContainingAffixLAndAffixS);
		
		if ( ! previouslyComputedLevel2Sizes.containsKey(affixL)) {
			previouslyComputedLevel2Sizes.put(affixL, new HashMap<Affix, Integer>());
		}
		previouslyComputedLevel2Sizes.get(affixL).put(affixS, sizeOfLevl2SchemeContainingAffixLAndAffixS);
		
		
		return sizeOfLevl2SchemeContainingAffixLAndAffixS;
	}

	private boolean 
	passesDiscriminativeCriterion(
			BottomUpSearchResultCluster clusterA,
			BottomUpSearchResultCluster clusterB,
			Map<Affix, Set<Affix>> discriminativeFilter) {
		
		int DEBUG = 0;
		
		clusterA.incorporateDataFromChildren_ifNotPerformedPrevisously();
		clusterB.incorporateDataFromChildren_ifNotPerformedPrevisously();
		
		for (Affix affixA : clusterA.getCoveredAffixes()) {
			
			for (Affix affixB : clusterB.getCoveredAffixes()) {
				
				if (affixA.equals(affixB)) {
					continue;
				}
				
				Set<Affix> affixesThatOccuredWithAffixA = discriminativeFilter.get(affixA);
				if ( ! affixesThatOccuredWithAffixA.contains(affixB)) {
					if (DEBUG > 0) {
						if (passesAffixesInCommonRequiredRestriction(clusterA, clusterB)) {
							System.err.println();
							System.err.println("------------------------------------------");
							System.err.println("WILL NOT USE THE CLUSTER:");
							System.err.println(this);
							System.err.println();
							System.err.println("Even though the children of this cluster share ");
							System.err.println("  1 or more affixes");
							System.err.println("Because the Affixes: " + affixA + " and " + affixB);
							System.err.println("  Never occured in the same selected scheme--Blocking.");
							System.err.println();
						}
					}
					
					return false;
				}
			}
		}
		return true;
	}

	private boolean 
	passesNoAffixesInCommonAllowedRestriction(BottomUpSearchResultCluster clusterA,
											  BottomUpSearchResultCluster clusterB) {
		
		clusterA.incorporateDataFromChildren_ifNotPerformedPrevisously();
		clusterB.incorporateDataFromChildren_ifNotPerformedPrevisously();
		
		Set<Affix> affixesInCommon = new HashSet<Affix>(clusterA.getCoveredAffixes());
		
		affixesInCommon.retainAll(clusterB.getCoveredAffixes());
		
		if (affixesInCommon.size() > 0) {
			return false;
		}
		return true;
	}

	private boolean 
	passesAffixesInCommonRequiredRestriction(BottomUpSearchResultCluster clusterA,
											 BottomUpSearchResultCluster clusterB) {
		
		Set<Affix> affixesInCommon = getAffixesCommonToBothChildren(clusterA, clusterB);
		
		if (affixesInCommon.size() == 0) {
			return false;
		}
		return true;
	}

	private Set<Affix> 
	getAffixesCommonToBothChildren(BottomUpSearchResultCluster bestNewCluster) {
		return getAffixesCommonToBothChildren(
				bestNewCluster.getChildWithLargerInternalSimilarity(),
				bestNewCluster.getChildWithSmallerInternalSimilarity());
	}
	
	Set<Affix> getAffixesCommonToBothChildren(BottomUpSearchResultCluster clusterA,
											  BottomUpSearchResultCluster clusterB) {
		
		clusterA.incorporateDataFromChildren_ifNotPerformedPrevisously();
		clusterB.incorporateDataFromChildren_ifNotPerformedPrevisously();
		
		Set<Affix> affixesInCommon = new HashSet<Affix>(clusterA.getCoveredAffixes());
		
		affixesInCommon.retainAll(clusterB.getCoveredAffixes());
		
		return affixesInCommon;
	}


	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		
		// parameters may be null if this '...Clustering' has been created with the
		// ...Clustering(List<...Cluster>) constructor. For example if a previous
		// ...Clustering was filtered, and the current ...Clustering was created
		// from the remaining ...Clusters.
		if (parameterSetting != null) {
			toReturn.append(String.format("%n"));
			toReturn.append(parameterSetting.toString());
			toReturn.append(String.format("%n%n"));
		}
		
		ArrayList<BottomUpSearchResultCluster> allClustersSorted = 
			new ArrayList<BottomUpSearchResultCluster>(clusters);
		//Collections.sort(allClustersSorted, BottomUpSearchResultCluster.byDecreasingNumberOfCoveredAffixes);
		Collections.sort(allClustersSorted, BottomUpSearchResultCluster.byDecreasingNumberOfCoveredTypes);
		//Collections.sort(allClustersSorted, BottomUpSearchResultCluster.orthographicallyByChildCoverings);
		
		toReturn.append(String.format("-----------------------%n"));
		toReturn.append(String.format("  " + allClustersSorted.size() + " Clusters%n"));
		toReturn.append(String.format("-----------------------%n%n"));
		
		for (BottomUpSearchResultCluster cluster : allClustersSorted) {
			toReturn.append(cluster);
			toReturn.append(String.format("%n"));
		}
		return toReturn.toString();
	}

	public List<SetOfMorphemes<Affix>> getSetsOfCoveredAffixes() {
		List<SetOfMorphemes<Affix>> setsOfCoveredAffixes = new ArrayList<SetOfMorphemes<Affix>>();
		
		for (BottomUpSearchResultCluster cluster : clusters) {
			setsOfCoveredAffixes.add(new SetOfMorphemes<Affix>(cluster.getCoveredAffixes()));
		}
		
		return setsOfCoveredAffixes;
	}

	public List<BottomUpSearchResultCluster> getClusters() {
		return clusters;
	}
}
