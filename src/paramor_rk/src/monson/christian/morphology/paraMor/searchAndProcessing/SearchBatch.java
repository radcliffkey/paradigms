/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import gnu.trove.set.hash.TCharHashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import klic.radoslav.functional.FuncUtil;
import klic.radoslav.functional.transformer.ToStringTransformer;
import klic.radoslav.morphology.ManualData;
import klic.radoslav.util.DataUtil;
import klic.radoslav.util.DebugLog;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.languages.Evaluation;
import monson.christian.morphology.paraMor.languages.Language;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.networks.BottomUpSearchableNetwork;
import monson.christian.morphology.paraMor.networks.PartialOrderNetwork;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.schemes.SchemeList;
import monson.christian.morphology.paraMor.schemes.SchemeSet;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearch.BottomUpParameterSetting;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearch.BottomUpParameters;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameterSetting;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchStep.SearchStepParameters;
import monson.christian.morphology.paraMor.segmentation.Segmentation;
import monson.christian.morphology.paraMor.segmentation.SegmentedWord;
import monson.christian.morphology.paraMor.segmentation.SegmentedWordList;
import monson.christian.morphology.paraMor.segmentation.SimpleSuffixSegmentationExplanation;
import monson.christian.util.FileUtils;
import monson.christian.util.FileUtils.Encoding;
import monson.christian.util.ComparablePair;
import monson.christian.util.Pair;

// TODO: write this class out to a file so that we can save the results of a search.

/**
 * Holds a set of parameter settings that can all be searched over/with.  And once search is
 * run, this class holds the results of all the search runs.
 * 
 * @author cmonson
 *
 */
public class SearchBatch implements Serializable { 
	
	public static class SearchStepSequence implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private List<SearchStepParameters> searchStepsInOrder = 
			new ArrayList<SearchStepParameters>();

		public void addASearchStep(SearchStepParameters searchStepParameters) {
			searchStepsInOrder.add(searchStepParameters);
		}

		public int size() {
			return searchStepsInOrder.size();
		}
		
		@Override
		public String toString() {
			StringBuilder toReturn = new StringBuilder();
			
			if (searchStepsInOrder.size() == 0) {
				toReturn.append(String.format("%nNo Search Steps have yet been performed " +
											  "with this Search Batch"));
				
			} else {
			
				toReturn.append(String.format("Completed Search Steps in this SearchBatch%n"));
				toReturn.append(String.format("-------------------------------------------%n%n"));

				int searchStepCounter = 0;
				for (SearchStepParameters searchStepParameters : searchStepsInOrder) {

					searchStepCounter++;
					
					if (searchStepCounter > 1) {
						toReturn.append(String.format("%n%n"));
					}
					
					toReturn.append("(" + searchStepCounter + ") ");
					toReturn.append(searchStepParameters);
				}				
			}
			
			return toReturn.toString();
		}
		
		public String toStringAsComment() {
			StringBuilder toReturn = new StringBuilder();
			
			if (searchStepsInOrder.size() == 0) {
				toReturn.append(String.format("%n# No Search Steps have yet been performed " +
											  "with this Search Batch"));
				
			} else {
			
				toReturn.append(String.format("# Completed Search Steps in this SearchBatch%n"));
				toReturn.append(String.format("# -------------------------------------------%n%n"));

				int searchStepCounter = 0;
				for (SearchStepParameters searchStepParameters : searchStepsInOrder) {

					searchStepCounter++;
					
					if (searchStepCounter > 1) {
						toReturn.append(String.format("# %n%n"));
					}
					
					toReturn.append("# (" + searchStepCounter + ") ");
					toReturn.append(searchStepParameters.toStringAsComment());
				}				
			}
			
			return toReturn.toString();
		}

		public String getColumnTitleStringForGlobalScoreForSpreadsheet() {
			String toReturn = "";
			boolean first = true;
			for (SearchStepParameters searchStepParameters : searchStepsInOrder) {
				if (first) {
					first = false;
				} else {
					toReturn += ", ";
				}
				toReturn += 
					searchStepParameters.getColumnTitleStringForGlobalScoreForSpreadsheet();
			}
			
			return toReturn;
		}
	}
	
	public static 
	class SearchStepSequenceInstantiation 
			implements Comparable<SearchStepSequenceInstantiation>,
					   Serializable {
		private static final long serialVersionUID = 1L;
		
		List<SearchStepParameterSetting> searchStepParameterSettingsInOrder = null;
		
		public 
		SearchStepSequenceInstantiation(
				SearchStepSequenceInstantiation oldSearchStepSequenceInstantiation, 
				SearchStepParameterSetting newSearchStepParameterSetting) {
			
			if (oldSearchStepSequenceInstantiation == null) {
				this.searchStepParameterSettingsInOrder = 
					new ArrayList<SearchStepParameterSetting>();
			
			} else {
				this.searchStepParameterSettingsInOrder = 
					new ArrayList<SearchStepParameterSetting>(
							oldSearchStepSequenceInstantiation.
								searchStepParameterSettingsInOrder);
			}
			
			this.searchStepParameterSettingsInOrder.add(newSearchStepParameterSetting);
		}
		
		private 
		SearchStepSequenceInstantiation(
				List<SearchStepParameterSetting> searchStepParameterSettingsInOrder) {
			
			this.searchStepParameterSettingsInOrder = searchStepParameterSettingsInOrder;
		}
		
		public SearchStepSequenceInstantiation
		formSearchStepSequenceInstantiationByStrippingFinalSearchStep() {
			List<SearchStepParameterSetting> strippedSearchStepSequence = 
				new ArrayList<SearchStepParameterSetting>(searchStepParameterSettingsInOrder);
			
			// Remove the last SearchStepParameterSetting
			strippedSearchStepSequence.remove(strippedSearchStepSequence.size()-1);
			
			return new SearchStepSequenceInstantiation(strippedSearchStepSequence);
		}

		@Override
		public String toString() {
			StringBuilder toReturn = new StringBuilder();
			int searchStepCounter = 0;
			for (SearchStepParameterSetting searchStepParameterSetting : 
														searchStepParameterSettingsInOrder) {
				searchStepCounter++;
				
				if (searchStepCounter > 1) {
					toReturn.append(String.format("%n%n"));
				}
				
				toReturn.append("(" + searchStepCounter + ") ");
				toReturn.append(searchStepParameterSetting);
			}
			
			return toReturn.toString();
		}

		public String getStringForSpreadsheet() {
			StringBuilder toReturn = new StringBuilder();
			boolean first = true;
			for (SearchStepParameterSetting searchStepParameterSetting : 
														searchStepParameterSettingsInOrder) {
				if (first) {
					first = false;
				} else {
					toReturn.append(", ");
				}
				toReturn.append(searchStepParameterSetting.getStringForSpreadsheet());
			}
			return toReturn.toString();
		}
		
		public String getFilenameUniqueifier() {
			String toReturn = "";

			for (SearchStepParameterSetting searchStepParameterSetting : 
														searchStepParameterSettingsInOrder) {
				
				String uniqueString = searchStepParameterSetting.getFilenameUniqueifier();
				if (uniqueString.equals("")) {
					continue;
				}
				
				toReturn += "-" + uniqueString;
			}
			
			return toReturn;
		}


		public int compareTo(SearchStepSequenceInstantiation that) {
			
			Iterator<SearchStepParameterSetting> thisSettingIterator = 
				this.searchStepParameterSettingsInOrder.iterator();
			Iterator<SearchStepParameterSetting> thatSettingIterator = 
				that.searchStepParameterSettingsInOrder.iterator();
			
			while(thisSettingIterator.hasNext() && thatSettingIterator.hasNext()) {
				SearchStepParameterSetting thisSetting = thisSettingIterator.next();
				SearchStepParameterSetting thatSetting = thatSettingIterator.next();
				int thisSettingComparedToThatSetting = thisSetting.compareTo(thatSetting);
				
				if (thisSettingComparedToThatSetting != 0) {
					return thisSettingComparedToThatSetting;
				}
			}
			
			// If this SearchStepSequenceInstantiation is shorter than that
			// SearchStepSequenceInstantiation then this is smaller than that
			if (thatSettingIterator.hasNext()) {
				return -1;
			}
			
			// If that SearchStepSequenceInstantiation is shorter than this
			// SearchStepSequenceInstantiation then that is smaller than this
			if (thisSettingIterator.hasNext()) {
				return 1;
			}
			
			// The two SearchStepSequenceInstantiations are identical
			return 0;
		}

	}

	
	private static final long serialVersionUID = 1L;

	private static final String AUTO_SEED_FILE_NAME = "autoSeed.txt";

	private Language<?> answerKeyLanguage = null;
	
	// If we want to save a SearchBatch we can serialize the network identifier 
	// but not the network itself.  The network may be big, and we assume that 
	// any relevant parts can be regenerated on the fly. 
	private PartialOrderNetwork.Identifier theSearchNetworkIdentifier = null;
	public void setTheSearchNetworkIdentifier(
			PartialOrderNetwork.Identifier theSearchNetworkIdentifier) {
		this.theSearchNetworkIdentifier = theSearchNetworkIdentifier;
	}



	transient private PartialOrderNetwork searchNetwork = null;
	
	private SearchStepSequence appliedSearchStepsInOrder = new SearchStepSequence();


	// Making paths and evaluations transient helps to save memory when segmenting
	// very large corpora. But I need the paths to save useful SearchBatches before
	// I actually cluster.
	private Map<SearchStepSequenceInstantiation, 
				SearchPathList> 
		pathsToSelectedSchemesBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, 
						SearchPathList>();
	
	transient private Map<SearchStepSequenceInstantiation, SearchPathList> 
		previousPathsToSelectedSchemesBySearchStepSequenceInstantiation = null;
	
	
	// TODO: Put evaluationOfSchemesBySearchStepSequenceInstantiation and the functions that 
	//		 calculate it into a contained class--Because the evaluation currently can be by 
	//		 a different language than the language in this class--which is wierd.
	//
	// The evaluation is transient, you can always just recalculate it, and generally when 
	// you load in an old search batch you are planning on running another search step and
	// so you don't care what the former evaluation was, you care about what the evaluation 
	// is AFTER running the search step.  (And running a search step automatically clobbers 
	// the old evaluation.)  
	//
	transient private Map<SearchStepSequenceInstantiation, Evaluation> 
		evaluationOfSchemesBySearchStepSequenceInstantiation;
	
	transient private Map<SearchStepSequenceInstantiation, Evaluation> 
		evaluationOfClustersBySearchStepSequenceInstantiation;

	
	private Map<SearchStepSequenceInstantiation, 
				BottomUpSearchResultClustering> 
		clustersBySearchStepSequenceInstantiation = null;
	
	private Map<SearchStepSequenceInstantiation,
				BottomUpSearchResultClustering>
		previousClustersBySearchStepSequenceInstantiation = null;

	private Map<SearchStepSequenceInstantiation, SegmentedWordList>
		segmentedCorpusBySearchStepSequenceInstantiation = null;
	
	private Map<String, Set<String>> wordToClusters;
	
	public SearchBatch(PartialOrderNetwork partialOrderNetwork, 
					   Language<?> answerKeyLanguage) {
		this.searchNetwork = partialOrderNetwork; 
		theSearchNetworkIdentifier = partialOrderNetwork.getIdentifier();
		
		resetEvaluations();
		
		this.answerKeyLanguage = answerKeyLanguage;
	}
	
	private void resetEvaluations() {
		evaluationOfSchemesBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, 
						Evaluation>();
	}
	
	public String getSelectedSchemesWithPathsString() {
		
		StringBuilder selectedSchemesWithPathsString = new StringBuilder();
			
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
					pathsToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {

			List<SearchPath> paths = 
				pathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);

			selectedSchemesWithPathsString.append(String.format("%n%n"));
			selectedSchemesWithPathsString.append(searchStepSequenceInstantiation.toString());
			selectedSchemesWithPathsString.append(String.format("%n"));
			selectedSchemesWithPathsString.append(String.format("----------------------------%n"));
			selectedSchemesWithPathsString.append("  " + paths.size() + " Schemes Selected");
			selectedSchemesWithPathsString.append(String.format("%n"));
			selectedSchemesWithPathsString.append(String.format("----------------------------%n"));
			
			for (SearchPath searchPath : paths) {
				selectedSchemesWithPathsString.append(searchPath.toString());
				selectedSchemesWithPathsString.append(String.format("%n%n"));
			}
		}
		
		return selectedSchemesWithPathsString.toString();
	}
	
	public void evaluateSearchBatch() {

		evaluationOfSchemesBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, Evaluation>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
				pathsToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {
			
			SearchPathList paths = 
				pathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			SchemeList<Scheme> selectedSchemes = paths.getTerminalSchemes();
			
			Evaluation evaluation = 
				new Evaluation(answerKeyLanguage, searchNetwork.getLevel1SchemesByAffix());
			List<SetOfMorphemes<Affix>> setsOfAffixes = selectedSchemes.getSetsOfAffixes();
			evaluation.evaluate(setsOfAffixes);
			
			evaluationOfSchemesBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation, 
					evaluation);
		}
	}
	
	public void evaluateClusters() {
		evaluationOfClustersBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, Evaluation>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
							clustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering clusters = 
				clustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);

			Evaluation evaluation = 
				new Evaluation(answerKeyLanguage, searchNetwork.getLevel1SchemesByAffix());
			
			List<SetOfMorphemes<Affix>> setsOfAffixes = clusters.getSetsOfCoveredAffixes();
			evaluation.evaluate(setsOfAffixes);
			
			evaluationOfClustersBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation, 
					evaluation);
		}
	}
	
			
	public String getEvaluationStringForSpreadsheet() {
			
		StringBuilder evaluationString = new StringBuilder();
				
		for (SearchStepSequenceInstantiation 
				searchStepSequenceInstantiation : 
					evaluationOfSchemesBySearchStepSequenceInstantiation.
						keySet()) {
			
			// Get the titles of all the parameters of all the applied search steps in this 
			// search step sequence.
			evaluationString.append(
					appliedSearchStepsInOrder.
						getColumnTitleStringForGlobalScoreForSpreadsheet());
			evaluationString.append(String.format("%n"));
			
			// Get the parameter values used for all the applied search steps in this sequence
			evaluationString.append(
					searchStepSequenceInstantiation.getStringForSpreadsheet());
			evaluationString.append(String.format("%n%n"));
			
			
			// Get and write out the evaluation of this sequence of search steps
			Evaluation evaluation = 
				evaluationOfSchemesBySearchStepSequenceInstantiation.
					get(searchStepSequenceInstantiation);
		
			evaluationString.append(
		        evaluation.getEvaluationStringForSpreadsheet());
		}
		
		return evaluationString.toString();
	}


	public String getClustersEvaluationStringForSpreadsheet() {
		
		StringBuilder evaluationString = new StringBuilder();
				
		for (SearchStepSequenceInstantiation 
				searchStepSequenceInstantiation : 
					evaluationOfClustersBySearchStepSequenceInstantiation.
						keySet()) {
			
			// Get the titles of all the parameters of all the applied search steps in this 
			// search step sequence.
			evaluationString.append(
					appliedSearchStepsInOrder.
						getColumnTitleStringForGlobalScoreForSpreadsheet());
			evaluationString.append(String.format("%n"));
			
			// Get the parameter values used for all the applied search steps in this sequence
			evaluationString.append(
					searchStepSequenceInstantiation.getStringForSpreadsheet());
			evaluationString.append(String.format("%n%n"));
			
			
			// Get and write out the evaluation of the current clusters
			Evaluation evaluation = 
				evaluationOfClustersBySearchStepSequenceInstantiation.
					get(searchStepSequenceInstantiation);
		
			evaluationString.append(
		        evaluation.getEvaluationStringForSpreadsheet());
		}
		
		return evaluationString.toString();
	}
	
	public String getSearchStepDeltaString() {

		String delta_string = "";

		// If there are no previous selected schemes
		if ((previousPathsToSelectedSchemesBySearchStepSequenceInstantiation == null) ||
			(previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.size() == 0)) {
			
			delta_string += "This Search Batch has only performed a single search step so far.";
			delta_string += String.format("%n");
			delta_string += "Hence, you cannot compare the current selected Schemes to any sets";
			delta_string += String.format("%n");
			delta_string += "of previous selected Schemes";
			return delta_string;
		}
		
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
			pathsToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {
			
			SearchPathList currentPaths = 
				pathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			SearchStepSequenceInstantiation previousSearchStepSequenceInstantiation =
				searchStepSequenceInstantiation.
					formSearchStepSequenceInstantiationByStrippingFinalSearchStep();
			
			if ( ! previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.
					containsKey(previousSearchStepSequenceInstantiation)) {
				
				String errorString = "ERROR: Given the Search Step Sequence Instantiation: ";
				errorString += String.format("%n%n");
				errorString += searchStepSequenceInstantiation;
				errorString += String.format("%n%n");
				errorString += "the previous Search Step Sequence Instantiation: ";
				errorString += String.format("%n%n");
				errorString += previousSearchStepSequenceInstantiation;
				errorString += String.format("%n%n");
				errorString += "should have been stored--BUT IT WASN'T";
				
				throw new RuntimeException(errorString);
			}
			
			SearchPathList previous_paths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
						previousSearchStepSequenceInstantiation);
			
			delta_string += searchStepSequenceInstantiation.toString();
			delta_string += String.format("%n%n");
			delta_string += getSingleSearchStepDeltaString(previous_paths, currentPaths);
		}
		
		return delta_string;
	}
	
	private String 
	getSingleSearchStepDeltaString(SearchPathList beforePaths, SearchPathList afterPaths) {
		String deltaString = "";
		
//		Perhaps this is the first search step that has been performed.  Then the beforePaths
//		may be null.
		if (beforePaths == null) {
			beforePaths = new SearchPathList();
		}
		SchemeList<Scheme> beforeSchemes = beforePaths.getTerminalSchemes();
		SchemeList<Scheme> afterSchemes  = afterPaths.getTerminalSchemes();
		
		SchemeSet<Scheme> removedSchemes = new SchemeSet<Scheme>(beforeSchemes);
		removedSchemes.removeAll(afterSchemes);
		
		SchemeSet<Scheme> addedSchemes   = new SchemeSet<Scheme>(afterSchemes);
		addedSchemes.removeAll(beforeSchemes);
		
		SetOfMorphemes<Affix> allTrueAffixes = answerKeyLanguage.getAllAffixes();
		
	
		SetOfMorphemes<Affix> allBeforeAffixes = beforeSchemes.getAllAffixes();
		SetOfMorphemes<Affix> allAfterAffixes  = afterSchemes.getAllAffixes();
		
		SetOfMorphemes<Affix> removedAffixes = allBeforeAffixes.minus(allAfterAffixes);
		SetOfMorphemes<Affix> addedAffixes   = allAfterAffixes.minus(allBeforeAffixes);
				
		SetOfMorphemes<Affix> allTrueRemovedAffixes = 
													removedAffixes.intersect(allTrueAffixes);
		SetOfMorphemes<Affix> allTrueAddedAffixes   = addedAffixes.intersect(allTrueAffixes);
		
		if (allTrueRemovedAffixes.size() > 0) {
			deltaString += "--------------------------------------------------------------" + 
							String.format("%n");
			deltaString += "TOTALLY REMOVED True Affixes, with the schemes that occured in" + 
							String.format("%n");
			deltaString += "--------------------------------------------------------------" + 
							String.format("%n%n");
		}
		for (Affix trueRemovedAffix : allTrueRemovedAffixes) {
			deltaString += "  " + trueRemovedAffix + String.format("%n");
			deltaString += "--------------" + String.format("%n");
			for (Scheme removedScheme : removedSchemes) {
				if (removedScheme.getAffixes().containsAll(trueRemovedAffix)) {
					deltaString += removedScheme.toPrettyString(30);
					deltaString += String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		if (allTrueAddedAffixes.size() > 0) {
			deltaString += "-------------------------------------------------------------" + 
							String.format("%n");
			deltaString += "NEW ADDED True Affixes, with the schemes they can be found in" + 
							String.format("%n");
			deltaString += "-------------------------------------------------------------" + 
							String.format("%n%n");
		}
		for (Affix trueAddedAffix : allTrueAddedAffixes) {
			deltaString += "  " + trueAddedAffix;
			deltaString += "--------------";
			for (Scheme addedScheme : addedSchemes) {
				if (addedScheme.getAffixes().containsAll(trueAddedAffix)) {
					deltaString += addedScheme.toPrettyString(30);
					deltaString += String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		deltaString += "-----------------------------------------" + String.format("%n");		
		deltaString += "Removed Schemes that contain a true Affix" + String.format("%n");
		deltaString += "-----------------------------------------" + String.format("%n");
		for (Affix trueAffix : allTrueAffixes) {
			deltaString += trueAffix + String.format("%n");
			deltaString += "----------" + String.format("%n%n");
			for (Scheme removedScheme : removedSchemes) {
				if (removedScheme.getAffixes().containsAll(trueAffix)) {
					deltaString += removedScheme.toPrettyString(30) + String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		deltaString += "-----------------------------------------" + String.format("%n");
		deltaString += "Added Schemes that contain a true Affix"   + String.format("%n");
		deltaString += "-----------------------------------------" + String.format("%n");
		for (Affix trueAffix : allTrueAffixes) {
			deltaString += trueAffix + String.format("%n");
			deltaString += "----------" + String.format("%n%n");
			for (Scheme addedScheme : addedSchemes) {
				if (addedScheme.getAffixes().containsAll(trueAffix)) {
					deltaString += addedScheme.toPrettyString(30) + String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		return deltaString;
	}

	
	public String getSearchStepDeltaStringForClusters() {

		String deltaString = "";

		// If there are no previous selected schemes
		if ((previousClustersBySearchStepSequenceInstantiation == null) ||
			(previousClustersBySearchStepSequenceInstantiation.size() == 0)) {
			
			deltaString += "This Search Batch has had no action performed on it clusters yet.";
			deltaString += String.format("%n");
			deltaString += "Hence, you cannot compare the current clusters to any sets";
			deltaString += String.format("%n");
			deltaString += "of previous clusters";
			return deltaString;
		}
		
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
			clustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering currentClusters = 
				clustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			SearchStepSequenceInstantiation previousSearchStepSequenceInstantiation =
				searchStepSequenceInstantiation.
					formSearchStepSequenceInstantiationByStrippingFinalSearchStep();
			
			if ( ! previousClustersBySearchStepSequenceInstantiation.
					containsKey(previousSearchStepSequenceInstantiation)) {
				
				String errorString = "ERROR: Given the Search Step Sequence Instantiation: ";
				errorString += String.format("%n%n");
				errorString += searchStepSequenceInstantiation;
				errorString += String.format("%n%n");
				errorString += "the previous Search Step Sequence Instantiation: ";
				errorString += String.format("%n%n");
				errorString += previousSearchStepSequenceInstantiation;
				errorString += String.format("%n%n");
				errorString += "should have been stored as a key for the previous clusters" +
							   "--BUT IT WASN'T";
				
				throw new RuntimeException(errorString);
			}
			
			BottomUpSearchResultClustering previousClusters = 
				previousClustersBySearchStepSequenceInstantiation.get(
						previousSearchStepSequenceInstantiation);
			
			deltaString += searchStepSequenceInstantiation.toString();
			deltaString += String.format("%n%n");
			deltaString += getSingleSearchStepDeltaString(previousClusters, currentClusters);
		}
		
		return deltaString;
	}
	
	private String 
	getSingleSearchStepDeltaString(
			BottomUpSearchResultClustering beforeClusters, 
			BottomUpSearchResultClustering afterClusters) {
		
		String deltaString = "";
		
//		Perhaps this is the first search step that has been performed.  Then the beforePaths
//		may be null.
		if (beforeClusters == null) {
			beforeClusters = 
				new BottomUpSearchResultClustering(
						new ArrayList<BottomUpSearchResultCluster>());
		}
		
		Set<BottomUpSearchResultCluster> removedClusters = 
			new HashSet<BottomUpSearchResultCluster>();
		removedClusters.addAll(beforeClusters.getClusters());
		removedClusters.removeAll(afterClusters.getClusters());
		
		Set<BottomUpSearchResultCluster> addedClusters   = 
			new HashSet<BottomUpSearchResultCluster>();
		addedClusters.addAll(afterClusters.getClusters());
		addedClusters.removeAll(beforeClusters.getClusters());
		
		SetOfMorphemes<Affix> allTrueAffixes = answerKeyLanguage.getAllAffixes();
		
	
		SetOfMorphemes<Affix> allBeforeAffixes = beforeClusters.getAllCoveredAffixes();
		SetOfMorphemes<Affix> allAfterAffixes  = afterClusters.getAllCoveredAffixes();
		
		SetOfMorphemes<Affix> removedAffixes = allBeforeAffixes.minus(allAfterAffixes);
		SetOfMorphemes<Affix> addedAffixes   = allAfterAffixes.minus(allBeforeAffixes);
				
		SetOfMorphemes<Affix> allTrueRemovedAffixes = 
													removedAffixes.intersect(allTrueAffixes);
		SetOfMorphemes<Affix> allTrueAddedAffixes   = addedAffixes.intersect(allTrueAffixes);
		
		if (allTrueRemovedAffixes.size() > 0) {
			deltaString += "--------------------------------------------------------------" + 
							String.format("%n");
			deltaString += "TOTALLY REMOVED True Affixes, with the clusters they occured in" + 
							String.format("%n");
			deltaString += "--------------------------------------------------------------" + 
							String.format("%n%n");
		}
		for (Affix trueRemovedAffix : allTrueRemovedAffixes) {
			deltaString += "  " + trueRemovedAffix + String.format("%n");
			deltaString += "--------------" + String.format("%n");
			for (BottomUpSearchResultCluster removedCluster : removedClusters) {
				if (removedCluster.getCoveredAffixes().contains(trueRemovedAffix)) {
					deltaString += removedCluster.toString();
					deltaString += String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		if (allTrueAddedAffixes.size() > 0) {
			deltaString += "-------------------------------------------------------------" + 
							String.format("%n");
			deltaString += "NEW ADDED True Affixes, with the clusters they can be found in" + 
							String.format("%n");
			deltaString += "-------------------------------------------------------------" + 
							String.format("%n%n");
		}
		for (Affix trueAddedAffix : allTrueAddedAffixes) {
			deltaString += "  " + trueAddedAffix;
			deltaString += "--------------";
			for (BottomUpSearchResultCluster addedCluster : addedClusters) {
				if (addedCluster.getCoveredAffixes().contains(trueAddedAffix)) {
					deltaString += addedCluster.toString();
					deltaString += String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		deltaString += "-----------------------------------------" + String.format("%n");		
		deltaString += "Removed Clusters that contain a true Affix" + String.format("%n");
		deltaString += "-----------------------------------------" + String.format("%n");
		for (Affix trueAffix : allTrueAffixes) {
			deltaString += trueAffix + String.format("%n");
			deltaString += "----------" + String.format("%n%n");
			for (BottomUpSearchResultCluster removedCluster : removedClusters) {
				if (removedCluster.getCoveredAffixes().contains(trueAffix)) {
					deltaString += removedCluster.toString() + String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		deltaString += "-----------------------------------------" + String.format("%n");
		deltaString += "Added Clusters that contain a true Affix"   + String.format("%n");
		deltaString += "-----------------------------------------" + String.format("%n");
		for (Affix trueAffix : allTrueAffixes) {
			deltaString += trueAffix + String.format("%n");
			deltaString += "----------" + String.format("%n%n");
			for (BottomUpSearchResultCluster addedCluster : addedClusters) {
				if (addedCluster.getCoveredAffixes().contains(trueAffix)) {
					deltaString += addedCluster.toString() + String.format("%n%n");
				}
			}
			deltaString += String.format("%n%n%n");
		}
		
		return deltaString;
	}

	
	public PartialOrderNetwork getSearchNetwork() {
		return searchNetwork;
	}


	@Override
	public String toString() {
		String toReturn = "";
		toReturn += searchNetwork.getIdentifier();
		toReturn += String.format("%n%n");
		toReturn += appliedSearchStepsInOrder.toString();
		return toReturn;
	}

	public String toStringAsComment() {
		String toReturn = "";
		toReturn += searchNetwork.getIdentifier().toStringAsComment();
		toReturn += String.format("%n%n");
		toReturn += appliedSearchStepsInOrder.toStringAsComment();
		return toReturn;
	}

	
	/**
	 * 
	 * @param stdin 
	 * @return <code>false</code> if the evaluation of the results of search has not yet
	 * 								been performed
	 */
	public boolean evaluationHasBeenPerformed() {	
		if (evaluationOfSchemesBySearchStepSequenceInstantiation == null) {
			return false;
		}
		return true;
	}
	
	public boolean evaluationOfClustersHasBeenPerformed() {
		if (evaluationOfClustersBySearchStepSequenceInstantiation == null) {
			return false;
		}
		return true;
	}
	
	public boolean isClustersReadyToBeWritten(BufferedReader stdin) {
		if (clustersBySearchStepSequenceInstantiation == null) {
			System.err.println();
			System.err.println(" SORRY.  The the clustered selected schemes in the " +
							   "search batch:");
			System.err.println();
			System.err.println(this);
			System.err.println();
			System.err.println(" are not ready to be written.  You must cluster the");
			System.err.println(" selected schemes in this seach batch before writing.");
			System.err.println();
			System.err.println(" Press Enter to continue...");
			System.err.println();
			try { stdin.readLine();	} catch (IOException e) {}  // This exception should never happen
			return false;
		}
		
		return true;
	}


	
	Map<SearchStepSequenceInstantiation, 
		Set<StringWithSetOfSearchPaths>> 
			coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation = null;
	
	
	/**
	 * 
	 * @return <code>true</code> if calculating the covered types to selected schemes data 
	 * 							 structure succeeded.
	 *         <code>false</code> if it failed
	 */
	public boolean calculateCoveredTypesToSelectedSchemes() {
		
		// Do not recalculate if the covered types to selected schemes data structure
		// has already been calculated.
		if (coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation != null) {
			return true;
		}
		
		coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation =
			new TreeMap<SearchStepSequenceInstantiation, Set<StringWithSetOfSearchPaths>>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
					pathsToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {
			
			List<SearchPath> paths = 
				pathsToSelectedSchemesBySearchStepSequenceInstantiation.get(searchStepSequenceInstantiation);
			
			Map<String, StringWithSetOfSearchPaths> stringsToSearchPaths = 
				new TreeMap<String, StringWithSetOfSearchPaths> ();
			
			for (SearchPath path : paths) {
				Scheme selectedScheme = path.getTerminalScheme();
				
				List<String> coveredWordTypes = selectedScheme.getCoveredWordTypes();
				
				for (String coveredWordType : coveredWordTypes) {
					if ( ! stringsToSearchPaths.containsKey(coveredWordType)) {
						stringsToSearchPaths.put(coveredWordType, 
								new StringWithSetOfSearchPaths(coveredWordType));
					}
					StringWithSetOfSearchPaths stringWithSetOfPaths = 
						stringsToSearchPaths.get(coveredWordType);
					
					// add the path and set the String if it hasn't been set already
					stringWithSetOfPaths.addPath(path);
				}
			}
			coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation, 
					new TreeSet<StringWithSetOfSearchPaths>(stringsToSearchPaths.values()));
		}
		
		return true;
	}
	
	public String getCoveredTypesToSelectedSchemesString() {
		StringBuilder toReturn = new StringBuilder();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
				coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {
			
			toReturn.append(searchStepSequenceInstantiation);
			toReturn.append(String.format("%n%n"));
			
			Set<StringWithSetOfSearchPaths> coveredTypesToSelectedSchemes =
				coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			for (StringWithSetOfSearchPaths coveredType : coveredTypesToSelectedSchemes) {
				toReturn.append(coveredType);
			}
		}
		
		return toReturn.toString();
	}
	
	/**
	 * Return false if this SearchStep cannot be performed--its prerequisites 
	 * have not been met.
	 * 
	 * @param newSearchStepParameters
	 * @return
	 */
	public boolean performSearchStep(SearchStepParameters newSearchStepParameters) {
		
		// Check preconditions for the different possible kinds of SearchSteps
		if (newSearchStepParameters instanceof BottomUpParameters) {
			
			if ( ! (searchNetwork instanceof VirtualPartialOrderNetwork)) {

				System.err.println("Sorry.  You must first create a");
				System.err.println("  network that is compatible with an upward search.");

				return false;
			}
			
		} else if ((newSearchStepParameters instanceof TypeCoveredByCompetingSchemes_Filter.Parameters) ||
				   (newSearchStepParameters instanceof MorphemeLength_Filter.Parameters) ||
				   (newSearchStepParameters instanceof TypesCovered_Filter.Parameters) ||
				   (newSearchStepParameters instanceof MorphemeBoundaryTooFarLeft_Filter.Parameters) ||
				   (newSearchStepParameters instanceof MorphemeBoundaryTooFarRight_Filter.Parameters)) {
			
			if (appliedSearchStepsInOrder.size() == 0) {
				
				System.err.println("Sorry.  You can only perform a Filtering step AFTER");
				System.err.println("  you have done some basic search step such as Bottom-Up");

				return false;
			}
		}
		
		previousPathsToSelectedSchemesBySearchStepSequenceInstantiation =
			pathsToSelectedSchemesBySearchStepSequenceInstantiation;
		
		pathsToSelectedSchemesBySearchStepSequenceInstantiation =
			new TreeMap<SearchStepSequenceInstantiation, SearchPathList>();
				
		appliedSearchStepsInOrder.addASearchStep(newSearchStepParameters);
		
		// If this is the first search step, then just do it.
		if (appliedSearchStepsInOrder.size() == 1) {
			performSearchStepOnOneSearchStepSequenceInstantiation(
					null, 
					newSearchStepParameters);

		// If this is not the first search step, then we need to apply this search step to
		// every previously explored search step.
		} else {
			
			Iterator<SearchStepSequenceInstantiation> 
				previousSearchStepSequenceInstantiationIterator = 
					previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.
						keySet().iterator();
			
			while (previousSearchStepSequenceInstantiationIterator.hasNext()) {
				SearchStepSequenceInstantiation previousSearchStepSequenceInstantiation =
					previousSearchStepSequenceInstantiationIterator.next();
				
			performSearchStepOnOneSearchStepSequenceInstantiation(
						previousSearchStepSequenceInstantiation, 
						newSearchStepParameters);
			}
		}
		
		// Reset the evaluations after performing a search step.
		evaluationOfSchemesBySearchStepSequenceInstantiation = null;
		
		return true;
	}
	
	private void performSearchStepOnOneSearchStepSequenceInstantiation(
			SearchStepSequenceInstantiation previousSearchStepSequenceInstantiation, 
			SearchStepParameters currentSearchStepParameters) {
		
		// For each possible parameter setting of the new search step perform the search step
		// and add the results to the selected schemes map
		for (SearchStepParameterSetting currentSearchStepParameterSetting : 
															currentSearchStepParameters) {
			
			SearchStepSequenceInstantiation newSearchStepSequenceInstantiation = 
				new SearchStepSequenceInstantiation(previousSearchStepSequenceInstantiation, 
													currentSearchStepParameterSetting);
			
			System.err.println();
			System.err.println("Searching with the following parameters:");
			System.err.println();
			System.err.println(newSearchStepSequenceInstantiation);
			
			SearchStep searchStep = 
				searchStepFactory(previousSearchStepSequenceInstantiation,
									currentSearchStepParameterSetting);
				
			SearchPathList searchStepResults = searchStep.performSearchStep();
			pathsToSelectedSchemesBySearchStepSequenceInstantiation.put(
					newSearchStepSequenceInstantiation, 
					searchStepResults);			
		}
	}

	private SearchStep 
	searchStepFactory(SearchStepSequenceInstantiation oldSearchStepSequenceInstantiation, 
					  SearchStepParameterSetting newSearchStepParameterSetting) {
		
		
		SearchStep searchStep = null;
		
		if (newSearchStepParameterSetting instanceof BottomUpParameterSetting) {
			
			BottomUpParameterSetting bottomUpParameterSetting =
				(BottomUpParameterSetting)newSearchStepParameterSetting;
			
			// The class of the partialOrderNetwork should have been checked earlier in
			// performSearchStep()
			assert(searchNetwork instanceof BottomUpSearchableNetwork);
			
			VirtualPartialOrderNetwork partialOrderNetwork_Dynamic_Dense = 
				(VirtualPartialOrderNetwork)searchNetwork;

			
			switch (bottomUpParameterSetting.getStartFrom()) {
			
			case ALL_LEVEL_1:
				
				// If this is the first search step we are performing, then
				// BottomUpSearch doesn't need to know what the 'covered' schemes are
				// Or rather there are no 'covered' schemes
				if (oldSearchStepSequenceInstantiation == null) {
					searchStep = new BottomUpSearch(partialOrderNetwork_Dynamic_Dense,
													bottomUpParameterSetting);
				  
				  // If this bottom up search step is being performed after other search
				  // steps, then we do care what the covered schemes are.
				} else {
					
					System.err.println();
					System.err.println("  WARNING: BottomUpSearch may not work if it isn't " +
									   "the first search step");
					System.err.println();
					System.err.println(" Press Enter to Continue...");
					System.err.println();
					try { 
						BufferedReader stdin = 
							new BufferedReader(new InputStreamReader(System.in));
						stdin.readLine();
					} catch (IOException e) {}; // This should never happen

					
					SearchPathList selectedSearchPaths = 
						previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
												oldSearchStepSequenceInstantiation);
				
					SearchPathList copyOfSelectedSearchPaths = 
						new SearchPathList(selectedSearchPaths);

					searchStep = new BottomUpSearch(partialOrderNetwork_Dynamic_Dense, 
													bottomUpParameterSetting,
													copyOfSelectedSearchPaths);
				}
				break;
				
			default:
				System.err.println();
				System.err.println("Sorry, you may only start this bottom-up search from ");
				System.err.println("  all level 1 schemes.");
				System.err.println();
				System.err.println("  Hopefully you never get here");
				System.err.println();
				System.err.println("  EXITING!!!");
				System.err.println();
				
				System.exit(0);
			}
			
		} else if (newSearchStepParameterSetting instanceof 
				TypeCoveredByCompetingSchemes_Filter.ParameterSetting) {
			
			TypeCoveredByCompetingSchemes_Filter.ParameterSetting filterParameterSetting = 
				(TypeCoveredByCompetingSchemes_Filter.ParameterSetting)
														newSearchStepParameterSetting;
			
			// To perform a Filter Search step there must be some selected schemes to filter!
			// This fact should have been checked in performSearchStep()
			assert(oldSearchStepSequenceInstantiation != null);
			
			SearchPathList selectedSearchPaths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
											oldSearchStepSequenceInstantiation);
			SearchPathList copyOfSelectedSearchPaths = 
				new SearchPathList(selectedSearchPaths);
			
			calculateCoveredTypesToSelectedSchemes();
			
			Set<StringWithSetOfSearchPaths> coveredTypesToSchemes = 
				coveredTypesToSelectedSchemesBySearchStepSequenceInstantiation.get(
						oldSearchStepSequenceInstantiation);
			
			searchStep = new TypeCoveredByCompetingSchemes_Filter(filterParameterSetting,
															  	  copyOfSelectedSearchPaths,
															  	  coveredTypesToSchemes);
			
		} else if (newSearchStepParameterSetting instanceof
				MorphemeLength_Filter.ParameterSetting) {
			
			MorphemeLength_Filter.ParameterSetting filterParameterSetting = 
				(MorphemeLength_Filter.ParameterSetting)newSearchStepParameterSetting;
			
			// To perform a Filter Search step there must be some selected schemes to filter!
			// This fact should have been checked in performSearchStep()
			assert(oldSearchStepSequenceInstantiation != null);
			
			List<SearchPath> selectedSearchPaths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
											oldSearchStepSequenceInstantiation);
			
			SearchPathList copyOfSelectedSearchPaths = 
				new SearchPathList(selectedSearchPaths);
			
			searchStep = 
				new MorphemeLength_Filter(filterParameterSetting, copyOfSelectedSearchPaths);
			
		} else if (newSearchStepParameterSetting instanceof
				TypesCovered_Filter.ParameterSetting) {
			
			TypesCovered_Filter.ParameterSetting filterParameterSetting = 
				(TypesCovered_Filter.ParameterSetting)newSearchStepParameterSetting;
			
			// To perform a Filter Search step there must be some selected schemes to filter!
			// This fact should have been checked in performSearchStep()
			assert(oldSearchStepSequenceInstantiation != null);
			
			List<SearchPath> selectedSearchPaths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
											oldSearchStepSequenceInstantiation);
			
			SearchPathList copyOfSelectedSearchPaths = 
				new SearchPathList(selectedSearchPaths);
			
			searchStep = 
				new TypesCovered_Filter(filterParameterSetting, copyOfSelectedSearchPaths);
			
		} else if (newSearchStepParameterSetting instanceof
				MorphemeBoundaryTooFarLeft_Filter.ParameterSetting) {
			
			MorphemeBoundaryTooFarLeft_Filter.ParameterSetting parameterSetting =
				(MorphemeBoundaryTooFarLeft_Filter.ParameterSetting)
								newSearchStepParameterSetting;
			
			// To perform a Filter Search step there must be some selected schemes to filter!
			// This fact should have been checked in performSearchStep()
			assert(oldSearchStepSequenceInstantiation != null);
			
			List<SearchPath> selectedSearchPaths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
											oldSearchStepSequenceInstantiation);
			
			SearchPathList copyOfSelectedSearchPaths = 
				new SearchPathList(selectedSearchPaths);
			
			searchStep = 
				new MorphemeBoundaryTooFarLeft_Filter(
						(VirtualPartialOrderNetwork)searchNetwork, 
						parameterSetting, 
						copyOfSelectedSearchPaths);
			
		} else if (newSearchStepParameterSetting instanceof
				MorphemeBoundaryTooFarRight_Filter.ParameterSetting) {
			
			MorphemeBoundaryTooFarRight_Filter.ParameterSetting parameterSetting =
				(MorphemeBoundaryTooFarRight_Filter.ParameterSetting)
								newSearchStepParameterSetting;
			
			// To perform a Filter Search step there must be some selected schemes to filter!
			// This fact should have been checked in performSearchStep()
			assert(oldSearchStepSequenceInstantiation != null);
			
			List<SearchPath> selectedSearchPaths = 
				previousPathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
											oldSearchStepSequenceInstantiation);
			
			SearchPathList copyOfSelectedSearchPaths = new SearchPathList(selectedSearchPaths);
			
			searchStep = 
				new MorphemeBoundaryTooFarRight_Filter(
						(VirtualPartialOrderNetwork)searchNetwork, 
						parameterSetting, 
						copyOfSelectedSearchPaths);
			
		} else {
			
			System.err.println();
			System.err.println("  WARNING: I do not know how to perform the passed in " +
							   "search step:");
			System.err.println("           " + newSearchStepParameterSetting);
			System.err.println();
			
			return null;
		}
		
		return searchStep;
	}

	public void cluster(BottomUpSearchResultClustering.Parameters clusteringParameters) {
		
		evaluationOfClustersBySearchStepSequenceInstantiation = null;
		
		appliedSearchStepsInOrder.addASearchStep(clusteringParameters);
		
	
		// When using java serialization to read in an out of date SearchBatch that 
		// did not have a 'clusters' field, this field will be set to null.
		// Hence I now default to initializing 'clusters' to null and newing
		// it here.
		if (clustersBySearchStepSequenceInstantiation == null) {
			
			previousClustersBySearchStepSequenceInstantiation = 
				new TreeMap<SearchStepSequenceInstantiation, 
							BottomUpSearchResultClustering>();
			clustersBySearchStepSequenceInstantiation = 
				new TreeMap<SearchStepSequenceInstantiation, 
							BottomUpSearchResultClustering>();
			
			for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
					pathsToSelectedSchemesBySearchStepSequenceInstantiation.keySet()) {
				
				List<SearchPath> selectedSchemes = 
					pathsToSelectedSchemesBySearchStepSequenceInstantiation.get(
							searchStepSequenceInstantiation);
				
				for (SearchStepParameterSetting 
						clusteringParameterSettingAsSearchStepParameterSetting : 
																clusteringParameters) {
				
					SearchStepSequenceInstantiation newSearchStepSequenceInstantiation =
						new SearchStepSequenceInstantiation(
								searchStepSequenceInstantiation,
								clusteringParameterSettingAsSearchStepParameterSetting);
					
					System.err.println();
					System.err.println("Clustering with the following parameters:");
					System.err.println(newSearchStepSequenceInstantiation);
					
					BottomUpSearchResultClustering.ParameterSetting 
						clusteringParameterSetting =
							(BottomUpSearchResultClustering.ParameterSetting)
								clusteringParameterSettingAsSearchStepParameterSetting;
						
					/* Don't put in the 'Clustering' until both the clustering
					 * and the type covered filtering steps are completed.
					 *
					clustersBySearchStepSequenceInstantiation.put(
							newSearchStepSequenceInstantiation, 
							new BottomUpSearchResultClustering(
									selectedSchemes, 
									clusteringParameterSetting,
									(VirtualPartialOrderNetwork)searchNetwork));
					*/
					
					BottomUpSearchResultClustering bottomUpSearchResultClustering =
						new BottomUpSearchResultClustering(
								selectedSchemes, 
								clusteringParameterSetting,
								(VirtualPartialOrderNetwork)searchNetwork);
					
					bottomUpSearchResultClustering.cluster();
	
					// If we just want to cluster and NOT remove schemes/clusters
					// that cover fewer types than the merger credit cutoff size
					// stipulates, then do this 'if'.
					if ( ! clusteringParameterSetting.getTieInTypeCoveredFilter()) {
						clustersBySearchStepSequenceInstantiation.put(
								newSearchStepSequenceInstantiation, 
								bottomUpSearchResultClustering);
						
					// Not only do we want to cluster, but you might as well just
					// filter the smaller schemes out now that clustering is over.
					} else {


						// We may be clustering where we only cluster
						// if at least one of the clusters coveres more types than
						// the 'childTypesCoveredCutoff'. So we immediately follow the
						// clustering step with a filtering step to throw out all the
						// small schemes that were never clustered.

						// The next step is extremely round about, but this is the
						// way the current code is set up, and I decided to work with
						// the system instead of modifying the system.

						// Save the current cluster in 
						// 'previousClustersBySearchStepSequenceInstantiation'.
						// This way, we'll know what clusters were thrown out after
						// any child-types-covered filtering.
						previousClustersBySearchStepSequenceInstantiation.put(
								newSearchStepSequenceInstantiation,
								bottomUpSearchResultClustering);


						// Create the 'TypesCoveredClusterFilter.Parameters' for this round
						// of clustering.
						Integer clusteringChildTypesCoveredCutoff =
							clusteringParameterSetting.getChildTypesCoveredCutoff();

						TypesCoveredClusterFilter.Parameters typesCoveredClusterFilterParameters =
							new TypesCoveredClusterFilter.Parameters();

						List<Integer> listConsistingJustOfClusteringChildTypesCoveredCutoff =
							new ArrayList<Integer>();
						listConsistingJustOfClusteringChildTypesCoveredCutoff.add(
								clusteringChildTypesCoveredCutoff);

						typesCoveredClusterFilterParameters.
						setClusterMustCoverAtLeastNtypes(
								listConsistingJustOfClusteringChildTypesCoveredCutoff);

						// Iterate over the (single) parameter setting contained within
						// the TypesCoveredClusterFilter.Parameters' for this round of clustering
						for (SearchStepParameterSetting parameterSetting : typesCoveredClusterFilterParameters) {

							// tack on the second step of this compound merger step
							newSearchStepSequenceInstantiation =
								new SearchStepSequenceInstantiation(
										newSearchStepSequenceInstantiation,
										parameterSetting);

							TypesCoveredClusterFilter filter = 
								new TypesCoveredClusterFilter(
										(TypesCoveredClusterFilter.ParameterSetting)parameterSetting,
										bottomUpSearchResultClustering);

							BottomUpSearchResultClustering newClustering = filter.filter();

							clustersBySearchStepSequenceInstantiation.put(
									newSearchStepSequenceInstantiation, 
									newClustering);
						}
					}
				}
			}
		}
		
		/* TODO: Remove once we cluster as we build the new Clusterings
		for (BottomUpSearchResultClustering bottomUpSearchResultClustering : 
			clustersBySearchStepSequenceInstantiation.values()) {
			
			bottomUpSearchResultClustering.cluster();
		}
		*/
	}
	
	// I tried having one single function to handle all possible search steps, and I think
	// it has just gotten completely out of hand.  So now for each filtering applied to 
	// the clusters, I am just going to write a separate function.
	public boolean 
	doTypesCoveredFilterOnClusters(TypesCoveredClusterFilter.Parameters parameters) {
		if (clustersBySearchStepSequenceInstantiation == null) {
			System.err.println(" SORRY. You must first cluster before applying a filter to");
			System.err.println("   those clusters");
			System.err.println();
			return false;
		}
		
		evaluationOfClustersBySearchStepSequenceInstantiation = null;
		
		appliedSearchStepsInOrder.addASearchStep(parameters);
		
		previousClustersBySearchStepSequenceInstantiation = 
			clustersBySearchStepSequenceInstantiation;

		clustersBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, BottomUpSearchResultClustering>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				previousClustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering oldClustering =
				previousClustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			for (SearchStepParameterSetting parameterSetting : parameters) {
				SearchStepSequenceInstantiation newSearchStepParameterSetting =
					new SearchStepSequenceInstantiation(
							searchStepSequenceInstantiation,
							parameterSetting);
				
				TypesCoveredClusterFilter filter = 
					new TypesCoveredClusterFilter(
							(TypesCoveredClusterFilter.ParameterSetting)parameterSetting,
							oldClustering);
				
				BottomUpSearchResultClustering newClustering = filter.filter();
				
				clustersBySearchStepSequenceInstantiation.put(
						newSearchStepParameterSetting, 
						newClustering);
			}
		}
		
		return true;
	}
	
	// I tried having one single function to handle all possible search steps, and I think
	// it has just gotten completely out of hand.  So now for each filtering applied to 
	// the clusters, I am just going to write a separate function.
	public boolean 
	doMorphemeBoundaryTooFarLeftFilterOnClusters(
			MorphemeBoundaryTooFarLeft_Filter.Parameters parameters) {
		
		if (clustersBySearchStepSequenceInstantiation == null) {
			System.err.println(" SORRY. You must first cluster before applying a filter to");
			System.err.println("   those clusters");
			System.err.println();
			return false;
		}
		
		evaluationOfClustersBySearchStepSequenceInstantiation = null;
		
		appliedSearchStepsInOrder.addASearchStep(parameters);
		
		previousClustersBySearchStepSequenceInstantiation = 
			clustersBySearchStepSequenceInstantiation;

		clustersBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, BottomUpSearchResultClustering>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				previousClustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering oldClustering =
				previousClustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			for (SearchStepParameterSetting parameterSetting : parameters) {
				SearchStepSequenceInstantiation newSearchStepParameterSetting =
					new SearchStepSequenceInstantiation(
							searchStepSequenceInstantiation,
							parameterSetting);
				
				MorphemeBoundaryTooFarLeft_Filter filter = 
					new MorphemeBoundaryTooFarLeft_Filter(
							(VirtualPartialOrderNetwork)searchNetwork,
							(MorphemeBoundaryTooFarLeft_Filter.ParameterSetting)
									parameterSetting,
							oldClustering);
				
				BottomUpSearchResultClustering newClustering = 
					filter.performClusterFiltering();
				
				clustersBySearchStepSequenceInstantiation.put(
						newSearchStepParameterSetting, 
						newClustering);
			}
		}
		
		return true;
	}
	
	// I tried having one single function to handle all possible search steps, and I think
	// it has just gotten completely out of hand.  So now for each filtering applied to 
	// the clusters, I am just going to write a separate function.
	public boolean 
	doMorphemeBoundaryTooFarRightFilterOnClusters(
			MorphemeBoundaryTooFarRight_Filter.Parameters parameters) {
		
		if (clustersBySearchStepSequenceInstantiation == null) {
			System.err.println(" SORRY. You must first cluster before applying a filter to");
			System.err.println("   those clusters");
			System.err.println();
			return false;
		}
		
		evaluationOfClustersBySearchStepSequenceInstantiation = null;
		
		appliedSearchStepsInOrder.addASearchStep(parameters);
		
		previousClustersBySearchStepSequenceInstantiation = 
			clustersBySearchStepSequenceInstantiation;

		clustersBySearchStepSequenceInstantiation = 
			new TreeMap<SearchStepSequenceInstantiation, BottomUpSearchResultClustering>();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				previousClustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering oldClustering =
				previousClustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			for (SearchStepParameterSetting parameterSetting : parameters) {
				SearchStepSequenceInstantiation newSearchStepParameterSetting =
					new SearchStepSequenceInstantiation(
							searchStepSequenceInstantiation,
							parameterSetting);
				
				MorphemeBoundaryTooFarRight_Filter filter = 
					new MorphemeBoundaryTooFarRight_Filter(
							(VirtualPartialOrderNetwork)searchNetwork,
							(MorphemeBoundaryTooFarRight_Filter.ParameterSetting)
											parameterSetting,
							oldClustering);
				
				BottomUpSearchResultClustering newClustering = 
					filter.performClusterFiltering();
				
				clustersBySearchStepSequenceInstantiation.put(
						newSearchStepParameterSetting, 
						newClustering);
			}
		}
		
		return true;
	}

	/**
	 * Segment the words in 'corpusToSegment' using the affixes of the clusters built
	 * for each setting of parameters this SearchBatch represents--BUT including as
	 * stems/contexts in each cluster both stems/contexts from the SearchBatch corpus
	 * AND stems/contexts from the 'corpusToSegment'.
	 * 
	 * @param corpusToSegment
	 */
	
	public void segment(Corpus corpusToSegment) {
		
		PartialOrderNetwork.Identifier networkIdentifier =
			new PartialOrderNetwork.Identifier(
					VirtualPartialOrderNetwork.class,
					corpusToSegment,
					PartialOrderNetwork.MorphemicAnalysis.SUFFIX,
					false);
		
		SortedSet<Character> charactersThatBeginSomeWordInCorpus = 
			corpusToSegment.getAllCharactersThatBeginSomeWord();
		
		Map<SearchStepSequenceInstantiation, 
			SegmentedWordList> segmentedWordListBySearchStepSequenceInstantiation = 
			new HashMap<SearchStepSequenceInstantiation, SegmentedWordList>();

		
		// For each parameter setting
		//
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				clustersBySearchStepSequenceInstantiation.keySet()) {
			
			this.wordToClusters =  new HashMap<String, Set<String>>();
			
			BottomUpSearchResultClustering clustering =
				clustersBySearchStepSequenceInstantiation.get(searchStepSequenceInstantiation);
			
			System.err.println();
			System.err.println("Segmenting the corpus:");
			System.err.println();
			System.err.println(corpusToSegment);
			System.err.println();
			System.err.println("with the following parameters:");
			System.err.println();
			System.err.println(searchStepSequenceInstantiation);

			SetOfMorphemes<Affix> allAffixesInAnyCluster = clustering.getAllCoveredAffixes();
			
			SegmentedWordList segmentedWordList = new SegmentedWordList();			
			
			int sizeOfWordList = corpusToSegment.getVocabularySize();
			int segmentedWordCounter = 0;
			
			/* It takes too much memory to segment the full Morpho Challenge data. So I am
			 * going to split the data on the first character of each word. And build separate
			 * networks for each first character. This is of course specific to suffixes!!
			 * - Sept. 2008
			 */
			
			VirtualPartialOrderNetwork partialOrderNetwork = (VirtualPartialOrderNetwork) PartialOrderNetwork.factory(networkIdentifier);
			
			for (String word : corpusToSegment.getVocabulary()) {

				segmentedWordCounter++;
				if ((segmentedWordCounter % 100) == 0) {
					System.err.println(
							"  " + segmentedWordCounter + " words segmented of " + sizeOfWordList +
							" " + word);
				}
		
				SegmentedWord segmentedWord = 
					segmentOneWord(
							word, 
							searchStepSequenceInstantiation, 
							partialOrderNetwork);
			
				segmentedWordList.add(segmentedWord);
			
			}
			
//			for (Character restrictVocabToBeginWithThisCharacter : charactersThatBeginSomeWordInCorpus) {
//				
//			
//				// Build a network just over the words in the corpus to segment and
//				// just restricted to affixes from the current clustering of schemes
//				//
//				VirtualPartialOrderNetwork 
//				restrictedNetworkBuiltOverCorpusToSegment = 
//					new VirtualPartialOrderNetwork(
//							networkIdentifier, 
//							allAffixesInAnyCluster, 
//							restrictVocabToBeginWithThisCharacter);
//			
//
//			
//				for (String word : corpusToSegment.getVocabulary()) {
//					if (word.charAt(0) != restrictVocabToBeginWithThisCharacter) {
//						continue;
//					}
//					segmentedWordCounter++;
//					if ((segmentedWordCounter % 100) == 0) {
//						System.err.println(
//								"  " + segmentedWordCounter + " words segmented of " + sizeOfWordList +
//								" " + word);
//					}
//			
//					SegmentedWord segmentedWord = 
//						segmentOneWord(
//								word, 
//								searchStepSequenceInstantiation, 
//								restrictedNetworkBuiltOverCorpusToSegment);
//				
//					segmentedWordList.add(segmentedWord);
//				
//				}
//				
//			} // end loop over word-initial characters
			
			segmentedWordListBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation,
					segmentedWordList);
			
			this.printWordToClusters();
			this.printClusterToWords();
			
		}
		
		this.segmentedCorpusBySearchStepSequenceInstantiation = 
			segmentedWordListBySearchStepSequenceInstantiation;
	}
	
	/* Old way that builds a network over *all* the types in the corpus at once.
	 * As opposed to the new way that builds a network over types that begin with 'a',
	 * then a network over types that begin with 'b', etc.
	 */
/*
	public void segment(Corpus corpusToSegment) {
		
		PartialOrderNetwork.Identifier networkIdentifier =
			new PartialOrderNetwork.Identifier(
					VirtualPartialOrderNetwork.class,
					corpusToSegment,
					PartialOrderNetwork.MorphemicAnalysis.SUFFIX,
					false);
		
		Map<SearchStepSequenceInstantiation, 
			SegmentedWordList> segmentedWordListBySearchStepSequenceInstantiation = 
			new HashMap<SearchStepSequenceInstantiation, SegmentedWordList>();

		
		// For each parameter setting
		//
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				clustersBySearchStepSequenceInstantiation.keySet()) {
			
			BottomUpSearchResultClustering clustering =
				clustersBySearchStepSequenceInstantiation.get(searchStepSequenceInstantiation);
			
			System.err.println();
			System.err.println("Segmenting the corpus:");
			System.err.println();
			System.err.println(corpusToSegment);
			System.err.println();
			System.err.println("with the following parameters:");
			System.err.println();
			System.err.println(searchStepSequenceInstantiation);

			SetOfMorphemes<Affix> allAffixesInAnyCluster = clustering.getAllCoveredAffixes();
			
			// Build a network just over the words in the corpus to segment and
			// just restricted to affixes from the current clustering of schemes
			//
			VirtualPartialOrderNetwork 
			networkBuiltOverCorpusToSegmentRestrictedToAffixesInCurrentClustering = 
				new VirtualPartialOrderNetwork(networkIdentifier, allAffixesInAnyCluster, null);
			
			SegmentedWordList segmentedWordList = new SegmentedWordList();
			
			int sizeOfWordList = corpusToSegment.getVocabularySize();
			int wordCounter = 0;
			for (String word : corpusToSegment.getVocabulary()) {
				wordCounter++;
				if ((wordCounter % 100) == 0) {
					System.err.println(
							"  " + wordCounter + " words segmented of " + sizeOfWordList +
							" " + word);
				}
			
				SegmentedWord segmentedWord = 
					segmentOneWord(
							word, 
							searchStepSequenceInstantiation, 
							networkBuiltOverCorpusToSegmentRestrictedToAffixesInCurrentClustering);
				
				segmentedWordList.add(segmentedWord);
			}
			
			segmentedWordListBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation,
					segmentedWordList);
		}
		
		this.segmentedCorpusBySearchStepSequenceInstantiation = 
			segmentedWordListBySearchStepSequenceInstantiation;
	}
*/
	
	public void segmentCorpus() {
		SortedSet<String> corpusVocabulary = 
			searchNetwork.getIdentifier().getCorpus().getVocabulary();
		
		List<String> corpusVocabularyAsList = new ArrayList<String>(corpusVocabulary);
		
		segmentedCorpusBySearchStepSequenceInstantiation = segment(corpusVocabularyAsList);
	}
	
	/**
	 * Segment any list of words according to the Scheme clusters formed for each
	 * setting of search step parameters this SearchBatch is over.
	 * 
	 * @param wordList
	 * @return
	 */
	public Map<SearchStepSequenceInstantiation, SegmentedWordList> 
	segment(List<String> wordList) {
		
		Map<SearchStepSequenceInstantiation, 
			SegmentedWordList> segmentedWordListBySearchStepSequenceInstantiation = 
				new HashMap<SearchStepSequenceInstantiation, SegmentedWordList>();
		
		System.err.println();
		System.err.println("Segmenting a list of words");
		System.err.println("--------------------------");

		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation :
				clustersBySearchStepSequenceInstantiation.keySet()) {
			
			System.err.println();
			System.err.println("Segmenting with the following parameters:");
			System.err.println();
			System.err.println(searchStepSequenceInstantiation);
		
			this.wordToClusters =  new HashMap<String, Set<String>>();
			
			SegmentedWordList segmentedWordList = new SegmentedWordList();
			
			int sizeOfWordList = wordList.size();
			int wordCounter = 0;
			for (String word : wordList) {
				wordCounter++;
				if ((wordCounter % 100) == 0) {
					System.err.println(
							"  " + wordCounter + " words segmented of " + sizeOfWordList +
							" " + word);
				}
			
				SegmentedWord segmentedWord = 
					segmentOneWord(
							word, 
							searchStepSequenceInstantiation,
							searchNetwork);
				
				segmentedWordList.add(segmentedWord);
			}
			
			segmentedWordListBySearchStepSequenceInstantiation.put(
					searchStepSequenceInstantiation,
					segmentedWordList);
			this.printWordToClusters();
			this.printClusterToWords();
		}
		
		return segmentedWordListBySearchStepSequenceInstantiation;
	}

	private void printWordToClusters() {
		PrintWriter outFile = FileUtils.openFileForWriting("wordToClusters.txt", Encoding.UTF8);
		for (String word : this.wordToClusters.keySet()) {
			String clusterIds = StringUtil.join("\t", this.wordToClusters.get(word));
			outFile.println(word + "\t" + clusterIds);
		}
		outFile.close();
	}	
	
	private void printClusterToWords() {
		Map<String, Set<String>> clusterToWords = new HashMap<String, Set<String>>();
		for (String word : this.wordToClusters.keySet()) {
			Set<String> clusters = this.wordToClusters.get(word);
			for (String clust : clusters) {
				try {
					clusterToWords.get(clust).add(word);
				} catch (Exception e) {
					Set<String> words = new HashSet<String>();
					words.add(word);
					clusterToWords.put(clust, words);
				}
			}
		}
		
		PrintWriter outFile = FileUtils.openFileForWriting("clusterToWords.txt", Encoding.UTF8);
		for (String clust : clusterToWords.keySet()) {
			String words = StringUtil.join("\t", clusterToWords.get(clust));
			outFile.println(clust + "\t" + words);
		}
		outFile.close();		
	}

	/**
	 * For each cluster (or selected scheme) for 'searchStepSequenceInstantiation' 
	 * if 1) 'word' matches an affix, A, in that cluster, and
	 *	   2) there is another word in 'networkToLookForMutuallyExclusiveAffixesIn'
	 *          that is formed by substituting out A and placing another affix, B, in
	 *	 	    its place, where B is in the same cluster (or scheme) A.
	 *		then stripping off A from word is a segmentation
	 *
	 * @param word
	 * @param searchStepSequenceInstantiation
	 * @param networkToLookForMutuallyExclusiveAffixesIn
	 * @return
	 */
	private SegmentedWord 
	segmentOneWord(
			String word, 
			SearchStepSequenceInstantiation searchStepSequenceInstantiation, 
			PartialOrderNetwork networkToLookForMutuallyExclusiveAffixesIn) {
		
		int DEBUG = 0;

		if (DEBUG > 0) {
			System.err.println("Segmenting the word: " + word);
		}
		
		SegmentedWord segmentedWord = new SegmentedWord(word);
		
		BottomUpSearchResultClustering clustering = 
			clustersBySearchStepSequenceInstantiation.get(searchStepSequenceInstantiation);

		Map<Affix, Segmentation> segmentationsByAffix = new TreeMap<Affix, Segmentation>();
		
		

		int clusterCounter = 0;
		for (BottomUpSearchResultCluster cluster : clustering.getClusters()) {
			clusterCounter++;

			//List<BottomUpSearchResultCluster> leaves = cluster.getLeaves();
			//for (BottomUpSearchResultCluster leaf : leaves) {

			for (Affix affix : cluster.getCoveredAffixes()) {
				//for (Affix affix : leaf.getCoveredAffixes()) {

				// It is too memory intensive to save all the explanations for all
				// the possible segmentations. So for now I am going to keep just
				// the first explanation.
				//
				// TODO: SO I NO LONGER AM KEEPING TRACK OF EVERY CLUSTER THAT SUGGESTS
				//       A PARTICULAR SEGMENTATION!!!!!!!  Sept. 2008.
//				if (segmentationsByAffix.containsKey(affix)) {
//					continue;
//				}
				
				// We don't try to match null affixes
//				if (affix.isNullAffix()) {
//					continue;
//				}

				// The following regular expression breaks when the alphabet in this orthography
				// contains characters that are special to regexes -- as the Buckwalter transliteration
				// of arabic does. The Buckwalter transliteration contains both '|' and '&', etc.
				// So just use String.endsWith(affix.toString()).
				//Pattern affixPattern = Pattern.compile("^(.+)" + affix.toString() + "$");
				//Matcher affixMatcher = affixPattern.matcher(word);
				//boolean wordContainsAffix = affixMatcher.matches();
				boolean wordContainsAffix = word.endsWith(affix.toString()) || affix.isNullAffix();
				if (wordContainsAffix) {
					// As described above, regexes are brittle
					//String stem = affixMatcher.group(1);
					String stem = word.substring(0, word.length() - affix.length());  // Take the front half of the word excluding affix
					Context stemAsContext = new Context(stem, "");
					
					if (ManualData.areDeepStemsAvailable()) {
						Context deepStem = ManualData.getDeepStem(stemAsContext);
						if (deepStem != null) {
							stemAsContext = deepStem;
							DebugLog.write("Segmentation: changing " + stem + " to " + deepStem);
						}
					}
					
					if (DEBUG > 0) {
						System.err.println("  Stripped off the affix: " + affix);
						System.err.println("  Yielding the stem:      " + stem);
					}

					SetOfMorphemes<Affix> substituteableAffixes = new SetOfMorphemes<Affix>();

					for (Affix affixInCluster : cluster.getCoveredAffixes()) {
						
						//for (Affix affixInCluster : leaf.getCoveredAffixes()) {
						if (affixInCluster.equals(affix)) {
							continue;
						}

						Scheme schemeOfAffixInCluster = 
							networkToLookForMutuallyExclusiveAffixesIn.
							getLevel1SchemesByAffix().get(affixInCluster);

						// If you are segmenting a corpus other than the corpus
						// you built the clusters from, it may be that the current
						// corpus has not examples of some particular suffix in
						// some cluster. If that is the case, then clearly it is not
						// possible to substitute the missing suffix in for the suffix
						// that matched a word. So we can safely skip the missing
						// suffix
						if (schemeOfAffixInCluster == null) {
							continue;
							
							/*
							System.err.println();
							System.err.println(
									"ERROR: Somehow the restricted network did " +
							"not contain");
							System.err.println(
									"       a level one scheme for the affix: " + 
									affixInCluster + "!?!?!");
							*/
						}

						if (DEBUG > 0) {
							System.err.println(
									"    Looking for the stem: " + stem + 
									" in the level 1 scheme of the affix: " + affixInCluster);
							System.err.println();
							System.err.println(schemeOfAffixInCluster.toString());
						}

						if (schemeOfAffixInCluster.getContexts().containsAll(stemAsContext)) {

							if (DEBUG > 0) {
								System.err.println("      FOUND THE STEM!");
							}

							substituteableAffixes.add(affixInCluster);

							// Require finding some number of mutually exclusive 
							// affixes from this cluster.
							//if ((substituteableAffixes.size() >= 2) ||
							//		
							//	(substituteableAffixes.size() >= 
							//		(leaf.getCoveredAffixes().size() - 1))) {  // minus 1 for the original affix being substituted for



							SimpleSuffixSegmentationExplanation simpleExplanation =
								new SimpleSuffixSegmentationExplanation(
										word,
										affix,
										cluster,
										substituteableAffixes);
							
							//DebugLog.write(word + "\t" + clusterCounter);
							String clustId = String.valueOf(clusterCounter) + '-' + stemAsContext;
							try {
								this.wordToClusters.get(word).add(clustId);
							} catch (Exception e) {
								Set<String> clustSet = new HashSet<String>();
								clustSet.add(clustId);
								this.wordToClusters.put(word, clustSet);
							}

							if ( ! segmentationsByAffix.containsKey(affix)) {
								segmentationsByAffix.put(
										affix,
										new Segmentation(word, simpleExplanation));
							} else {
								Segmentation segmentation = segmentationsByAffix.get(affix);
								segmentation.addExplanation(simpleExplanation);
							}

							break; // continue to examine the remaining suffixes
							// in this cluster. It may be that due to some
							// wierdness a cluster matches more than once!
							// against a single word. In the case of such
							// a multiple match I don't want to arbitrarily
							// only match the orthographically first or
							// even randomly selected first suffix, but 
							// rather let all possible matches fire.
						}
					}
				}
			}

			/*
				if (matches(word, affix)) {
					Scheme highestSchemeBoundedByClusterThatMatchesWord =
						getHighestSchemeBoundedByClusterThatMatchesWord(
								word, affix, cluster);

					if (highestSchemeBoundedByClusterThatMatchesWord.level() > 1) {
						SimpleSuffixSegmentationExplanation simpleExplanation =
							new SimpleSuffixSegmentationExplanation(
									word,
									affix,
									cluster, 
									highestSchemeBoundedByClusterThatMatchesWord);

						simpleSuffixSegmentationExplanations.add(simpleExplanation);
					}
				}
			 */
		}
		//}  // For cycling through leaves of clusters

		DEBUG = 0;

		for (Segmentation segmentation : segmentationsByAffix.values()) {

			segmentedWord.add(segmentation);
		}

		return segmentedWord;
	}

	/*
	private Map<SetOfMorphemes<Affix>, Scheme> segmentationCache = 
		new HashMap<SetOfMorphemes<Affix>, Scheme>();
	private Map<SetOfMorphemes<Affix>, List<String>> coveredWordsCache =
		new HashMap<SetOfMorphemes<Affix>, List<String>>();
	
	@SuppressWarnings("unused")
	private Scheme getHighestSchemeBoundedByClusterThatMatchesWord(
			String word, Affix affix, BottomUpSearchResultCluster cluster) {
		
		if (segmentationCache == null) {
			segmentationCache = new HashMap<SetOfMorphemes<Affix>, Scheme>();
			coveredWordsCache = new HashMap<SetOfMorphemes<Affix>, List<String>>();
		}
		
		SetOfMorphemes<Affix> currentAffixes = new SetOfMorphemes<Affix>();
		currentAffixes.add(affix);
		Scheme currentScheme = 
			((BottomUpSearchableNetwork)searchNetwork).getASchemeByName(currentAffixes);
		 
		boolean moveUp = true;
		while (moveUp) {
			
			Affix affixOfLargestParent = null;
			int sizeOfLargestParent = 0;
			Scheme largestParentScheme = null;
			
			for (Affix affixInCluster : cluster.getCoveredAffixes()) {
				if (currentAffixes.containsAll(affixInCluster)) {
					continue;
				}
				
				SetOfMorphemes<Affix> parentName = new SetOfMorphemes<Affix>(currentAffixes);
				parentName.add(affixInCluster);
				
				Scheme parentScheme;
				List<String> coveredWords;
				if (segmentationCache.containsKey(parentName)) {
					parentScheme = segmentationCache.get(parentName);
					coveredWords = coveredWordsCache.get(parentName);
				} else {
					parentScheme = 
						((BottomUpSearchableNetwork)searchNetwork).getASchemeByName(parentName);
					segmentationCache.put(parentName, parentScheme);
					
					coveredWords = parentScheme.getCoveredWordTypes();
					coveredWordsCache.put(parentName, coveredWords);
				}
								
				// move to the highest parent _that_contains_the_word_!
				if ( ! coveredWords.contains(word)) {
					continue;
				}
				
				if (parentScheme.adherentSize() > sizeOfLargestParent) {	
					affixOfLargestParent = affixInCluster;
					sizeOfLargestParent = parentScheme.adherentSize();
					largestParentScheme = parentScheme;
				}
			}
			
			if (sizeOfLargestParent > 0) {
				currentAffixes.add(affixOfLargestParent);
				currentScheme = largestParentScheme;
				
				// for now actually don't move up beyond level 2
				moveUp = false;
			} else {
				moveUp = false;
			}
		}
		
		return currentScheme;
	}
	

	// The stem must have at least one character in it to match
	private boolean matches(String word, Affix affix) {
		//if (word.matches("^.+" + affix.toString() + "$")) {
		//	return true;
		//}
		
		if (word.endsWith(affix.toString())) {
			return true;
		}
		
		return false;
	}
	*/

	public boolean segmentationHasBeenCompleted() {
		if (segmentedCorpusBySearchStepSequenceInstantiation == null) {
			return false;
		}
		return true;
	}
	
	public String 
	getSegmentationString(
			SearchStepSequenceInstantiation searchStepSequenceInstantiation,
			SegmentedWord.OutputSegmentation outputSegmentation) {
		
		SegmentedWordList segmentedWordList =
			segmentedCorpusBySearchStepSequenceInstantiation.get(
					searchStepSequenceInstantiation);
		
		//return segmentedWordList.getSegmentationAs_shortestStemPlusAllFullAffixStrings();
		return segmentedWordList.toString(outputSegmentation);
	}
	
	public String 
	getSegmentationExplanationString(
			SearchStepSequenceInstantiation searchStepSequenceInstantiation) {
		
		SegmentedWordList segmentedWordList =
			segmentedCorpusBySearchStepSequenceInstantiation.get(
					searchStepSequenceInstantiation);
		
		return segmentedWordList.getSegmentationExplanationString();
	}
	
	public String getClustersString() {
		StringBuilder toReturn = new StringBuilder();
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
									clustersBySearchStepSequenceInstantiation.keySet()) {
			
			toReturn.append(searchStepSequenceInstantiation);
			toReturn.append(String.format("%n%n"));
			
			BottomUpSearchResultClustering aClustering = 
				clustersBySearchStepSequenceInstantiation.get(
						searchStepSequenceInstantiation);
			
			toReturn.append(aClustering);
			toReturn.append(String.format("%n"));
		}
		
		return toReturn.toString();
	}
	
	public Iterator<SearchStepSequenceInstantiation> 
	getIteratorOverCurrentSearchStepSequenceInstantiations() {
		Iterator<SearchStepSequenceInstantiation> iterToReturn =
			clustersBySearchStepSequenceInstantiation.keySet().iterator();
		return iterToReturn;
	}

	private void 
	readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		searchNetwork = PartialOrderNetwork.factory(theSearchNetworkIdentifier);
	}

	private boolean hasMoreInitChars(Set<Affix> affixSet) {
		Iterator<Affix> it = affixSet.iterator();
		if (!it.hasNext()) {
			return false;
		}
		char initChar = it.next().getLeadMorphemeCharacter();
		
		while (it.hasNext()) {
			if (it.next().getLeadMorphemeCharacter() != initChar) {
				return true;
			}
		}
		
		return false;
	}

	private void filterShiftedAffixes(
			Map<Context, Set<Affix>> shiftedStemsToAffixes,
			SetOfMorphemes<Affix> allOrigAffixes) {
		Iterator<Entry<Context, Set<Affix>>> it = shiftedStemsToAffixes.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<Context, Set<Affix>> entry = it.next();
			if (!allOrigAffixes.containsAny(entry.getValue())) {
				it.remove();
			} else {
				//calculate the intersection with the original affix set for
				//debug purposes
				HashSet<Affix> affCopy = new HashSet<Affix>(entry.getValue());
				affCopy.retainAll(allOrigAffixes.getCopyOfMorphemes());
				//DebugLog.write("support: " + affCopy);
			}
		}
		
		if (shiftedStemsToAffixes.size() < 2) {
			shiftedStemsToAffixes.clear();
		}
	}

	private String toSeedFormat(Map<Context, Set<Affix>> shiftedStemsToAffixes) {
		List<String> stemStrs = FuncUtil.transform(shiftedStemsToAffixes.keySet(), 
				new ToStringTransformer<Context>(){
			@Override
			public String transform(Context obj) {
				return obj.toStringAvoidUndescore();
			}
		});
		
		String stemStr = StringUtil.join("/", stemStrs);
		List<String> affixGroups = new ArrayList<String>();
		for (Set<Affix> affixSet : shiftedStemsToAffixes.values()) {
			List<String> affixStrs = FuncUtil.transform(affixSet, new ToStringTransformer<Affix>() {
				@Override
				public String transform(Affix obj) {
					if (obj.isNullAffix()) {
						return "0";
					} else {
						return obj.toString();
					}
				}
			});
			affixGroups.add(StringUtil.join(", ", affixStrs));
		}
		String affixStr = StringUtil.join("/", affixGroups);
		String result = stemStr + " + " + affixStr;
		
		return result;
	}
	
	public void detectPhonChanges() throws Exception {
		
		String filename = AUTO_SEED_FILE_NAME;		
		PrintWriter autoSeedFile = FileUtils.openFileForWriting(new File(filename), Encoding.UTF8);

		if (this.clustersBySearchStepSequenceInstantiation.size() != 1) {
			throw new Exception("phonological change detection works only for exactly one set of clusters");
		}
		
		SearchStepSequenceInstantiation firstKey = clustersBySearchStepSequenceInstantiation.keySet().iterator().next();
		BottomUpSearchResultClustering clustering = clustersBySearchStepSequenceInstantiation.get(firstKey);
		SetOfMorphemes<Affix> allAffixes = clustering.getAllCoveredAffixes();
		
		List<BottomUpSearchResultCluster> clusters = clustering.getClusters();
		
		for (BottomUpSearchResultCluster cluster : clusters) {
			Set<Affix> affixes = cluster.getCoveredAffixes();
			DebugLog.write("testin for phon. changes:" + affixes);
			if (!isPhonChangeCandidate(affixes)) {
				DebugLog.write("detected: false");
				continue;
			}
			DebugLog.write("candidate: true");
			Set<ComparablePair<Affix, Context>> affixStemPairs = cluster.getCoveredAffixStemPairs();
			Map<Context, Set<Affix>> stemToAffixes = DataUtil.groupBySecond(affixStemPairs);
			int ouputStems = 0;
			
			for (Entry<Context, Set<Affix>> affixesForStem : stemToAffixes.entrySet()) {
				Set<Affix> affixSet = affixesForStem.getValue();
				if (hasMoreInitChars(affixSet)) {
					Context stem = affixesForStem.getKey();
					Map<Context, Set<Affix>> shiftedStemsToAffixes = shiftMB(stem, affixSet);
					filterShiftedAffixes(shiftedStemsToAffixes, allAffixes);
					
					if (!shiftedStemsToAffixes.isEmpty()) {						
						String seedStr = toSeedFormat(shiftedStemsToAffixes);
						autoSeedFile.println(seedStr);
						++ouputStems;
					}
				}
			}
			
			if (ouputStems > 0) {
				DebugLog.write("passed: true");
			} else {
				DebugLog.write("passed: false");
			}
		}
	}

	private Map<Context, Set<Affix>> shiftMB(Context stem, Set<Affix> affixSet) {
		Set<Pair<Context, Affix>> shifted = new HashSet<Pair<Context,Affix>>();
		String stemStr = stem.toStringAvoidUndescore();
		for (Affix affix : affixSet) {
			char firstChar = affix.getLeadMorphemeCharacter();
			if (!StringUtil.isVowel(firstChar)) {
				Affix shiftedAffix = affix.createAffixByStrippingLeadChar();
				Context shiftedStem = new Context(stemStr + firstChar, "");
				shifted.add(new Pair<Context, Affix>(shiftedStem, shiftedAffix));
			} else {
				Affix shiftedAffix = affix.createAffixByStrippingLeadChar();
				String shiftedStemStr = stemStr + firstChar;
				do {
					firstChar = shiftedAffix.getLeadMorphemeCharacter();
					shiftedStemStr += firstChar;					
					shiftedAffix = shiftedAffix.createAffixByStrippingLeadChar();					
				} while (StringUtil.isVowel(firstChar));
				Context shiftedStem = new Context(shiftedStemStr, "");
				shifted.add(new Pair<Context, Affix>(shiftedStem, shiftedAffix));
			}
		}
		Map<Context, Set<Affix>> shiftedGrouped = DataUtil.groupByFirst(shifted);
		return shiftedGrouped;
	}

	private Character checkEpenthesis(Affix affix) {
		int len = affix.length();
		if (len <= 1) {
			return null;
		}
		String affixStr = affix.toString();
		int i = 0;
		for (;i < len; ++i) {
			Character c = affixStr.charAt(i);
			if (!StringUtil.isVowel(c)) {
				return c;
			}
		}
		
		return null;
	}
	
	private boolean isPhonChangeCandidate(Set<Affix> affixes) {

		TCharHashSet consonants = new TCharHashSet();
		TCharHashSet epenCons = new TCharHashSet();
		boolean wasVowel = false;
		for (Affix affix : affixes) {
			if (affix.isNullAffix()) {
				return false;
			}
			char first = affix.getLeadMorphemeCharacter();
			if (!StringUtil.isVowel(first)) {
				if (affix.length() < 2) {
					continue;
				}
				consonants.add(first);
			} else {
				wasVowel = true;
				Character finalConsonant = checkEpenthesis(affix);
				if (finalConsonant != null) {
					epenCons.add(finalConsonant);
				} else {
					return false;
				}
			}
		}
		
		if (!consonants.containsAll(epenCons)) {
			return false;
		}
		
		if (consonants.size() == 2 
				|| (consonants.size() == 1 && wasVowel == true)) {
			return true;
		}
		return false;
	}

	

}
