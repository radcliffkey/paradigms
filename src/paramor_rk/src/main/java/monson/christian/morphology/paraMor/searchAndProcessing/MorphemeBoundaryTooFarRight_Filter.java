/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import klic.radoslav.morphology.searchAndProcessing.ManualClusterProtector;
import klic.radoslav.util.DebugLog;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchPath;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchPathList;
import monson.christian.morphology.paraMor.searchAndProcessing.MorphemeBoundaryTooFarLeft_Filter.LeftMetric;

public class MorphemeBoundaryTooFarRight_Filter implements SearchStep {

	private static final String SEARCH_STEP_NAME = "Morpheme Boundary Too Far Right Filter";
	private static final long serialVersionUID = 1L;
	
	
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
			toReturn += String.format("MBTF Right: Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("MBTF Right: Left-Looking Metric Cutoffs: %s%n", leftCutoffs);
			return toReturn;
		}
		
		@Override
		protected String getParametersStringAsComment() {
			String toReturn = "";
			toReturn += String.format("# MBTF Right: Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("# MBTF Right: Left-Looking Metric Cutoffs: %s%n", leftCutoffs);
			return toReturn;
		}


		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}

		@Override
		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			return "MBTF Right: Left-looking Metric, MBTF Right: Left-looking Cutoff";
		}

	}
	
	public static class ParameterSetting extends SearchStepParameterSetting {
		private static final long serialVersionUID = 1L;

		private LeftMetric leftMetric;
		private Double leftCutoff = 0.5;

		public ParameterSetting(LeftMetric leftMetric, Double leftCutoff) {
			
			associatedSearchStep = MorphemeBoundaryTooFarRight_Filter.class;
			
			this.leftMetric = leftMetric;
			this.leftCutoff = leftCutoff;
		}

		@Override
		public String getStringForSpreadsheet() {
			return leftMetric + ", " + leftCutoff;
		}
		
		@Override
		public String getFilenameUniqueifier() {
			return "MBTFRF-" + leftMetric.toString() + "-" + leftCutoff;
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
			toReturn += String.format("MBTF Right: Left-Looking Metric:         %s%n", leftMetric);
			toReturn += String.format("MBTF Right: Left-Looking Metric Cutoffs: %s%n", leftCutoff);
			return toReturn;
		}

		@Override
		public String getAssociatedSearchStepName() {
			return getNameStatic();
		}
	}
	

	VirtualPartialOrderNetwork searchNetwork;
	MorphemeBoundaryTooFarRight_Filter.ParameterSetting parameterSetting;
	SearchPathList searchPaths;
	ArrayList<BottomUpSearchResultCluster> clustersToFilter;

	
	public MorphemeBoundaryTooFarRight_Filter(
			VirtualPartialOrderNetwork searchNetwork, 
			ParameterSetting parameterSetting, 
			SearchPathList pathsToFilter) {
		
		this.searchNetwork    = searchNetwork; 
		this.parameterSetting = parameterSetting;
		this.searchPaths      = pathsToFilter;
	}
	
	public MorphemeBoundaryTooFarRight_Filter(
			VirtualPartialOrderNetwork searchNetwork, 
			ParameterSetting parameterSetting, 
			BottomUpSearchResultClustering clustersToFilter) {
		
		this.searchNetwork    = searchNetwork; 
		this.parameterSetting = parameterSetting;
		this.clustersToFilter = new ArrayList<BottomUpSearchResultCluster>(clustersToFilter.getClusters());
	}


	public BottomUpSearchResultClustering performClusterFiltering() {
		DebugLog.write("MorphemeBoundaryTooFarRight_Filter.performClusterFiltering()");
		DebugLog.write(clustersToFilter.size());
		Iterator<BottomUpSearchResultCluster> clusterIterator = clustersToFilter.iterator();
		while (clusterIterator.hasNext()) {
			BottomUpSearchResultCluster cluster = clusterIterator.next();
			
			if (!clusterPassesFilter(cluster)) {
				try {
					if (ManualClusterProtector.shouldBeKept(cluster)){
						DebugLog.write("Cluster protected from MBTFR filter due to similarity to manual input");
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
	
	// Take a vote among all the 
	// leaf schemes on whether to move or not.
	private boolean clusterPassesFilter(BottomUpSearchResultCluster cluster) {
		int keep = 0;
		
		List<BottomUpSearchResultCluster> leaves = cluster.getLeaves();
		for (BottomUpSearchResultCluster leaf : leaves) {
			Scheme leafScheme =
				searchNetwork.generateScheme(new SetOfMorphemes<Affix>(leaf.getCoveredAffixes()));
			
			SearchPath tempSearchPath = new SearchPath();
			tempSearchPath.add(leafScheme);
			boolean isLikelyMorphemeBoundary = isLikelyMorphemeBoundary(tempSearchPath);
			
			if (isLikelyMorphemeBoundary) {
				keep++;
			} else {
				keep--;
			}
		}
		
		// Keep a cluster if more leaves looked like morpheme bounaries than didn't.
		if (keep > 0) {
			return true;
		}
		
		return false;
	}


	public SearchPathList performSearchStep() {
		DebugLog.write("MorphemeBoundaryTooFarRight_Filter.performSearchStep()");
		Iterator<SearchPath> searchPathsIterator = searchPaths.iterator();
		while (searchPathsIterator.hasNext()) {
			SearchPath searchPath = searchPathsIterator.next();
			
			boolean currentBoundaryIsLikelyMorphemeBoundary =
				isLikelyMorphemeBoundary(searchPath);
			
			if ( ! currentBoundaryIsLikelyMorphemeBoundary) {
				searchPathsIterator.remove();
			}
		}
		
		return searchPaths;
	}

	
	// TODO: only Entropy is implemented.
	private Boolean isLikelyMorphemeBoundary(SearchPath searchPath) {
		Scheme terminalScheme = searchPath.getTerminalScheme();
		
		
		Double calculatedMetric = 0.0;
		
		
		// TODO: ONLY ENTROPY is implemented 
		switch(parameterSetting.leftMetric) {
		case CUMULATIVE_STEM_RATIO:
			return null;
			
		case STEPWISE_STEM_RATIO:
			return null;
			
		case MAX_STEM_RATIO:
			return null;
			
		case ENTROPY:
			calculatedMetric = terminalScheme.getLeftEntropy();
			
			// Entropy is opposite from ratios.  So check if it is large (instead of small)
			if (calculatedMetric > parameterSetting.leftCutoff) {
				return true;
			}
			return false;
		}
		
		// To satisfy the compiler
		return null;
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
