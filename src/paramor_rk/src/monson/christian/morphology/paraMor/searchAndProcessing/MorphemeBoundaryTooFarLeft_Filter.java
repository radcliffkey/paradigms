/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import klic.radoslav.morphology.searchAndProcessing.ManualClusterProtector;
import klic.radoslav.util.DebugLog;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchPath;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchPathList;

public class MorphemeBoundaryTooFarLeft_Filter implements SearchStep {

	private static final String SEARCH_STEP_NAME = "Morpheme Boundary Too Far Left Filter";
	private static final long serialVersionUID = 1L;
	
	
	public static enum LeftMetric { MAX_STEM_RATIO, ENTROPY, STEPWISE_STEM_RATIO, CUMULATIVE_STEM_RATIO } 


	public static class Parameters extends SearchStepParameters {
		private static final long serialVersionUID = 1L;

		private LeftMetric leftMetric;
		private List<Double> leftCutoffs = new ArrayList<Double>();
	
		
		public Parameters() {
			// Defaults
			leftMetric = LeftMetric.ENTROPY;
			leftCutoffs.add(0.5);
		}
	
		public void setLeftCutoffs(List<Double> leftCutoffs) {
			this.leftCutoffs = leftCutoffs;
		}
		
		public void setLeftMetric(LeftMetric leftMetric) {
			this.leftMetric = leftMetric;
		}

		public String getLeftMetric() {
			return leftMetric.toString();
		}

		
		public Iterator<SearchStepParameterSetting> iterator() {
			
			List<SearchStepParameterSetting> allParameterSettings = 
				new ArrayList<SearchStepParameterSetting>();

			for (Double  leftCutoff : leftCutoffs) {
				ParameterSetting parameterSetting = new ParameterSetting(leftMetric, leftCutoff);
				allParameterSettings.add(parameterSetting);
			}
			
			return allParameterSettings.iterator();

		}

		@Override
		protected String getParametersString() {
			String toReturn = "";
			toReturn += String.format("Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("Left-Looking Metric Cutoffs: %s%n", leftCutoffs);
			return toReturn;
		}
		
		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			toReturn += String.format("# Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("# Left-Looking Metric Cutoffs: %s%n", leftCutoffs);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "Left-looking Metric, Left-looking Cutoff";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		private LeftMetric leftMetric;
		private Double leftCutoff = 0.5;

		public ParameterSetting(LeftMetric leftMetric, Double leftCutoff) {
			
			associatedSearchStep = MorphemeBoundaryTooFarLeft_Filter.class;
			
			this.leftMetric = leftMetric;
			this.leftCutoff = leftCutoff;
		}

		@Override
		public String getStringForSpreadsheet() {
			return leftMetric + ", " + leftCutoff;
		}

		@Override
		public String getFilenameUniqueifier() {
			return "MBTFLF-" + leftMetric.toString() + "-" + leftCutoff;
		}
		
		@Override
		public int compareTo(SearchStepParameterSetting thatSearchStepParameterSetting) {
			if ( ! (thatSearchStepParameterSetting instanceof ParameterSetting)) {
				super.compareTo(thatSearchStepParameterSetting);
			}
			
			ParameterSetting that = (ParameterSetting)thatSearchStepParameterSetting;
			
			if (this.leftMetric != that.leftMetric) {
				return this.leftMetric.compareTo(that.leftMetric);
			}
			
			return this.leftCutoff.compareTo(that.leftCutoff);
		}
		
		@Override
		protected String getParameterString() {
			String toReturn = "";
			toReturn += String.format("Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("Left-Looking Metric Cutoffs: %s%n", leftCutoff);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}
	}
	

	VirtualPartialOrderNetwork searchNetwork;
	MorphemeBoundaryTooFarLeft_Filter.ParameterSetting parameterSetting;
	SearchPathList searchPaths;
	ArrayList<BottomUpSearchResultCluster> clustersToFilter;

	
	public MorphemeBoundaryTooFarLeft_Filter(
			VirtualPartialOrderNetwork searchNetwork, 
			ParameterSetting parameterSetting, 
			SearchPathList pathsToFilter) {
		
		this.searchNetwork    = searchNetwork; 
		this.parameterSetting = parameterSetting;
		this.searchPaths      = pathsToFilter;
	}
	
	public MorphemeBoundaryTooFarLeft_Filter(
			VirtualPartialOrderNetwork searchNetwork, 
			ParameterSetting parameterSetting, 
			BottomUpSearchResultClustering clustersToFilter) {
		
		this.searchNetwork    = searchNetwork; 
		this.parameterSetting = parameterSetting;
		this.clustersToFilter = new ArrayList<BottomUpSearchResultCluster>(clustersToFilter.getClusters());
	}

	public BottomUpSearchResultClustering performClusterFiltering() {
		DebugLog.write("MorphemeBoundaryTooFarLeft_Filter.performClusterFiltering()");
		DebugLog.write(clustersToFilter.size());
		Iterator<BottomUpSearchResultCluster> clusterIterator = clustersToFilter.iterator();
		while (clusterIterator.hasNext()) {
			BottomUpSearchResultCluster cluster = clusterIterator.next();
			
			if (!clusterPassesFilter(cluster)) {
				try {
					if (ManualClusterProtector.shouldBeKept(cluster)){
						DebugLog.write("Cluster protected from MBTFL filter due to similarity to manual input");
						DebugLog.write(cluster.getCoveredAffixes());
						continue;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				clusterIterator.remove();
			}
		}
		
		BottomUpSearchResultClustering filteredClustering = 
			new BottomUpSearchResultClustering(clustersToFilter);
		
		DebugLog.write(filteredClustering.getClusters().size());
		
		return filteredClustering;
	}
	
	// Perhaps the 'correct' way to implement a morpheme boundary too far left filter
	// for a cluster would be to find one overall distribution that counts all the left links
	// from all the unambiguous right schemes from all the leaf schemes of the cluster.
	// But I am pressed for time, so this method simply takes a vote among all the 
	// leaf schemes on whether to move or not.
	private boolean clusterPassesFilter(BottomUpSearchResultCluster cluster) {
		int throwOut = 0;
		
		List<BottomUpSearchResultCluster> leaves = cluster.getLeaves();
		for (BottomUpSearchResultCluster leaf : leaves) {
			Scheme leafScheme =
				searchNetwork.generateScheme(new SetOfMorphemes<Affix>(leaf.getCoveredAffixes()));
			
			// a cluster passes this filter if it is not possible to unambiguously move
			// right from even one of its leaves
			if ( ! leafScheme.isRightFSAUnambiguous()) {
				return true;
			}
			
			SearchPath tempSearchPath = new SearchPath();
			tempSearchPath.add(leafScheme);
			tempSearchPath = moveRightSuccesively(tempSearchPath);
			
			boolean leafPassedFilter = false;
			if (tempSearchPath == null) {
				leafPassedFilter = true;
			}
			
			if (leafPassedFilter) {
				throwOut--;
			} else {
				throwOut++;
			}
		}
		
		// Throw out a cluster if more leaves failed the filter than passed.
		if (throwOut > 0) {
			return false;
		}
		
		return true;
	}

	public SearchPathList performSearchStep() {
		DebugLog.write("MorphemeBoundaryTooFarLeft_Filter.performSearchStep()");
		Iterator<SearchPath> searchPathsIterator = searchPaths.iterator();
		while (searchPathsIterator.hasNext()) {
			SearchPath searchPath = searchPathsIterator.next();
			
			SearchPath newSearchPath = moveRightSuccesively(searchPath);
			if (newSearchPath != null) {
				searchPathsIterator.remove();
			}
		}
		
		return searchPaths;
	}

	
	/**
	 * Iteratively tries to move right until either:
	 * 
	 *   1) We find the first location to the right that we believe is a morpheme boundary
	 *      In this case we return a 'SearchPath' that concatenates (onto the passed in
	 *      'SearchPath') all the PartialOrderNodes that we passed through to get to that 
	 *      PartialOrderNode that represents the morpheme boundary.
	 *      
	 *   2) Before we find a location that we believe is a morpheme boundary, we hit a location
	 *      where we cannot move right unambiguously.  In this case we return 'null'.
	 * 
	 * In short, if we return null, then we did NOT find any location to the right that
	 * we think is a likely morpheme boundary.
	 * 
	 * @param searchPath
	 * @return
	 */
	private SearchPath moveRightSuccesively(SearchPath searchPath) {
		Scheme terminalSchemeOfOriginalSearchPath = searchPath.getTerminalScheme();
		Scheme currentScheme = terminalSchemeOfOriginalSearchPath;
		Scheme previousScheme = null;
		
		SearchPath continuationSearchPath = new SearchPath(searchPath);
		
		// I could implement this recursively, but I think it will be more understandable
		// if I implement it iteratively.
		
		while (true) {
		
			// If we can't unambiguously move right, then DO NOT throw out searchPath
			if ( ! currentScheme.isRightFSAUnambiguous()) {
				return null;
			}
			Map<Character, SetOfMorphemes<Affix>> rightBranches = 
				currentScheme.getRightFSAAffixSets();
		
			// currentScheme's right branches are supposed to be unambiguous 
			// i.e. exactly 1 branch.
			int numberOfRightBranches = rightBranches.size();
			assert(numberOfRightBranches == 1);
			
			// We know there is exactly one right branch
			Iterator<SetOfMorphemes<Affix>> rightBranchesIterator = 
				rightBranches.values().iterator();
			SetOfMorphemes<Affix> rightBranchSuffixes = rightBranchesIterator.next();
			
			// move the 'currentScheme' to the new node to the right.
			// And add this new node into 'continuationSearchPath'
			previousScheme = currentScheme;
			currentScheme  = searchNetwork.generateScheme(rightBranchSuffixes);
			continuationSearchPath.add(currentScheme);
			
			boolean currentSchemeIsLikelyMorphemeBounary = 
				isLikelyMorphemeBoundary(terminalSchemeOfOriginalSearchPath,
										 previousScheme,
										 currentScheme); 
			
			// If we have found the first likely morpheme boundary to the right, then
			// return the path to that scheme.
			if (currentSchemeIsLikelyMorphemeBounary) {
				return continuationSearchPath;
			}
		}
	}

	private boolean isLikelyMorphemeBoundary(Scheme terminalSchemeOfOriginalSearchPath, 
											 Scheme previousScheme, 
											 Scheme currentScheme) {
		
		double calculatedMetric = 0.0;
		
		switch(parameterSetting.leftMetric) {
		case CUMULATIVE_STEM_RATIO:
			calculatedMetric = (double)terminalSchemeOfOriginalSearchPath.getContexts().size() / 
							   (double)currentScheme.getContexts().size();
			break;
			
		case STEPWISE_STEM_RATIO:
			calculatedMetric = (double)previousScheme.getContexts().size() /
							   (double)currentScheme.getContexts().size();
			break;
			
		case MAX_STEM_RATIO:
			calculatedMetric = currentScheme.getMaxLeftRatio();
			break;
			
		case ENTROPY:
			calculatedMetric = currentScheme.getLeftEntropy();
			
			// Entropy is opposite from ratios.  So check if it is large (instead of small)
			if (calculatedMetric > parameterSetting.leftCutoff) {
				return true;
			}
			return false;
		}
		
		if (calculatedMetric < parameterSetting.leftCutoff) {
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
