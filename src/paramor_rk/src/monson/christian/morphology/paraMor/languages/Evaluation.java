/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.languages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import monson.christian.statistics.BinaryClassification.Measure;
import monson.christian.statistics.BinaryClassification;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.languages.Language.SubClassName;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

public class Evaluation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// provides the absolute set of affixes in this language
	private Language<?> language;  
	
	// contains all Affixes (or really just substrings of types)
	// that occured at least twice in the corpus.
	// I can't count REAL occurences of a true Affix, but only
	// occurences of Strings that match true Affixes.
	//
	private SetOfMorphemes<Affix> empiricalAffixOccurences;
	
	private List<SetOfMorphemes<Affix>> candidateSetsOfAffixes;
	private List<SetOfMorphemes<Affix>> candidateSetsOfAffixesSorted;
	
	private GlobalEvaluation globalEvaluation = null;
	private GlobalEvaluation globalAchievableEvaluation = null;
	
	// Store the evaluation of each cadidate set of Affixes as measured against
	// each sub-class in the answer key, and each sub-class in the answer key with
	// Affixes that never actually occured in the data removed--the achievable
	// sub-classes.
	private Map<SubClassName, 
				Map<SetOfMorphemes<Affix>, 
					BinaryClassification>> evaluationsPerSubClassPerCandidateAffixSet = 
		new TreeMap<SubClassName, 
					Map<SetOfMorphemes<Affix>, 
						BinaryClassification>>();
	private Map<SubClassName, 
				Map<SetOfMorphemes<Affix>, 
					BinaryClassification>> achievableEvaluationsPerSubClassPerCandidateAffixSet = 
        new TreeMap<SubClassName, 
					Map<SetOfMorphemes<Affix>, 
						BinaryClassification>>();
	
	/* NOTE: The set of candidate sets of affixes that are chosen as the 'single best' 
	 *       candidates and, more importantly, the candidate sets of affixes that are
	 *       chosen to be merged greedily acording to the oracle to maximize F1, are
	 *       *not* recalculated to maximize F1 according to the the achievable F1.
	 *       But rather the same sets of candidate sets of Affixes are used, and the
	 *       evaluation of these sets is done using the just those Affixes that
	 *       actually occured in the corpus.
	 */
	
	// For each sub-class store all the candidate sets of affixes which have the same best F1.
	// There may be more than one candidate set with the same F1.  Also store the achievable 
	// F1 of those same candidate sets.  Since the same set of Affixes is achievable for each
	// candidate set for any given sub-class, the 'single best achievable' evaluations should
	// all equal each other.
	// 
	private Map<SubClassName, ScoredSetOfSetsOfAffixes> singleBestCandidateSetOfAffixesByF1_perSubClass = 
		new TreeMap<SubClassName, ScoredSetOfSetsOfAffixes>();

	private Map<SubClassName, ScoredSetOfSetsOfAffixes> singleBest_evaluatedWithAchievable_perSubClass = 
		new TreeMap<SubClassName, ScoredSetOfSetsOfAffixes>();

	
	// For each sub-class store a set of candidate sets of affixes which have a good F1
	// when the affixes in the candidate sets are unioned.  Also store the 'achievable'
	// F1 of these same candidates merged.  I do NOT recalculate what the best oracle
	// greedy set of candidate sets of affixes to merge would be according to the
	// 'achievable' affixes in the sub-class.  The achievable best oracle greedy set
	// could conceivably differ from the strict best oracle greedy set, but it would
	// most likely only differ slightly, and the numbers would be too confusing to interpret.
	private Map<SubClassName, 
				ScoredSetOfSetsOfAffixes> 
		greedyOracleCandidateSetsOfAffixesMergedByF1_perSubClass = 
		new TreeMap<SubClassName, ScoredSetOfSetsOfAffixes>();
	
	private Map<SubClassName, 
				ScoredSetOfSetsOfAffixes> 
		greedyOracle_evaluatedWithAchievable_perSubClass = 
		new TreeMap<SubClassName, ScoredSetOfSetsOfAffixes>();
	

	public Evaluation(Language<?> language, Map<Affix, Level1Scheme> empiricalLevel1SchemesByAffix) {
		this.language = language;
		
		gatherEmpiricalAffixOccurences(empiricalLevel1SchemesByAffix);
	}
	
	// If an Affix occurs with 2 or more stems, then I count that Affix as
	// having officially occurred in the data.  I don't really expect my
	// algorithms to magically be able to discover an Affix that only occurred
	// once--such one time occurring 'Affixes' are nearly always just 
	// noise in the data.
	private void gatherEmpiricalAffixOccurences(Map<Affix, Level1Scheme> empiricalLevel1SchemesByAffix) {
		empiricalAffixOccurences = new SetOfMorphemes<Affix>();
		
		for (Affix trueAffix : language.getAllAffixes()) {
			Level1Scheme level1Scheme = null;
			if (empiricalLevel1SchemesByAffix.containsKey(trueAffix)) {
				level1Scheme = empiricalLevel1SchemesByAffix.get(trueAffix);
			}
			if ((level1Scheme != null) && (level1Scheme.adherentSize() > 1)) {
				empiricalAffixOccurences.add(trueAffix);
			}
		}
	}

	/**
	 * @param schemesToEvaluate
	 */
	private void setSchemesToEvaluate(List<SetOfMorphemes<Affix>> schemesToEvaluate) {
		this.candidateSetsOfAffixes = new ArrayList<SetOfMorphemes<Affix>>(schemesToEvaluate);
		candidateSetsOfAffixesSorted = new ArrayList<SetOfMorphemes<Affix>>(this.candidateSetsOfAffixes);
		Collections.sort(candidateSetsOfAffixesSorted);
	}
	
	public void evaluate(List<SetOfMorphemes<Affix>> schemesToEvaluate) {
		setSchemesToEvaluate(schemesToEvaluate);
		
		evaluateGlobally();
		evaluatePerSubClass();
	}
	
	private void evaluatePerSubClass() {
		// A little pre-computation to aid findBestSingleSchemes_perSubClass().
		evaluatePerSubClassPerCandidateSetOfAffixes();
		
		findBestSingleSchemesPerSubClass();
		findGreedyOracleMergeSchemesPerSubClass();
	}

	// For each sub-class find a (greedy) set of schemes which when the
	// affixes of these schemes are unioned produce a high F1--An F1
	// higher than any other set of schemes considered during this greedy
	// search.  Store the set of affixes and their score in greedyOracleMergedSchemes_byF1
	private void findGreedyOracleMergeSchemesPerSubClass() {
		for (SubClassName subClassName : language.getSubClasses()) {
			SetOfMorphemes<Affix> affixesInSubClass = language.getAffixesIn(subClassName);
			
			// At the end of this loop we'll put this in greedyOracleMergedSchemes
			ScoredSetOfSetsOfAffixes previousBestScoredSetOfSetsOfAffixes = new ScoredSetOfSetsOfAffixes();
			SetOfMorphemes<Affix> previousBestMergedAffixes = new SetOfMorphemes<Affix>();

			// During one loop over all the schemes we might add, this holds the current best
			ScoredSetOfSetsOfAffixes currentBestScoredSetOfSetsOfAffixes = new ScoredSetOfSetsOfAffixes();
			SetOfMorphemes<Affix> currentBestMergedAffixes = new SetOfMorphemes<Affix>();
			
			// The current scheme we are considering adding is held here
			ScoredSetOfSetsOfAffixes currentScoredSetOfSetsOfAffixes = new ScoredSetOfSetsOfAffixes();
			SetOfMorphemes<Affix> currentMergedAffixes = new SetOfMorphemes<Affix>();
			
			boolean foundCandidateSetOfAffixesToAdd = true;
			while (foundCandidateSetOfAffixesToAdd) {
				foundCandidateSetOfAffixesToAdd = false;
				
				// find the best scheme to add this time around
				// For reproducability march through the *sorted* list of Schemes to evaluate
				//   (there may be >1 scheme that if added would give the same increase in F1)
				for (SetOfMorphemes<Affix> candidateSetOfAffixes : candidateSetsOfAffixesSorted) {
					
					// Each time considering adding a new scheme reset the 'candidate...'
					currentScoredSetOfSetsOfAffixes  = 
						new ScoredSetOfSetsOfAffixes(previousBestScoredSetOfSetsOfAffixes);
					currentMergedAffixes = 
						new SetOfMorphemes<Affix>(previousBestMergedAffixes);
					
					currentScoredSetOfSetsOfAffixes.setsOfAffixes.add(candidateSetOfAffixes);
					currentMergedAffixes.add(candidateSetOfAffixes);
					
					BinaryClassification evaluation = 
						getCorrectnessOfAffixes(affixesInSubClass, currentMergedAffixes);
					currentScoredSetOfSetsOfAffixes.score = evaluation;
					
					if (currentScoredSetOfSetsOfAffixes.score.getMeasures().get(Measure.FMEASURE) > 
						currentBestScoredSetOfSetsOfAffixes.score.getMeasures().get(Measure.FMEASURE)) {
						
						foundCandidateSetOfAffixesToAdd = true;
						currentBestScoredSetOfSetsOfAffixes = currentScoredSetOfSetsOfAffixes;
						currentBestMergedAffixes = currentMergedAffixes;
					} 
				}
				
				// if we found a candidate set of affixes that is worth adding, then add it
				if (foundCandidateSetOfAffixesToAdd) {
					previousBestScoredSetOfSetsOfAffixes = currentBestScoredSetOfSetsOfAffixes;
					previousBestMergedAffixes = currentBestMergedAffixes;
				}
			}
			
			// put the final greedily chosen candidate sets of affixes to merge in 
			// greedyOracleAffixSetsMergedByF1_perSubClass for this sub-class
			greedyOracleCandidateSetsOfAffixesMergedByF1_perSubClass.put(
					subClassName, 
					previousBestScoredSetOfSetsOfAffixes);
			
			// And calcuate the ScoredSetOfSetsOfAffixes that results when using the
			// greedily chosen set of candiate sets of Affixes to merge together with
			// the 'achievable' answer key.  And then save it.
			ScoredSetOfSetsOfAffixes achievableScoredSetOfSetsOfAffixes =
				new ScoredSetOfSetsOfAffixes();
			achievableScoredSetOfSetsOfAffixes.setsOfAffixes =
				previousBestScoredSetOfSetsOfAffixes.setsOfAffixes;
			SetOfMorphemes<Affix> achievableAffixesInSubClass = 
				returnAchievableAffixes(affixesInSubClass);
			BinaryClassification achievableEvaluation = 
				getCorrectnessOfAffixes(
						achievableAffixesInSubClass, 
						previousBestMergedAffixes);
			achievableScoredSetOfSetsOfAffixes.score = achievableEvaluation;
			greedyOracle_evaluatedWithAchievable_perSubClass.put(
					subClassName,
					achievableScoredSetOfSetsOfAffixes);
		}	
	}

	// For each sub-class find all the schemes which have the same best F1
	// (there may be more than one), and store them in singleBestSchemes_byF1
	private void findBestSingleSchemesPerSubClass() {
		for (SubClassName subClassName : language.getSubClasses()) {
	
			Map<SetOfMorphemes<Affix>, 
				BinaryClassification> candidateSetOfAffixesToEvaluation = 
					evaluationsPerSubClassPerCandidateAffixSet.get(subClassName);
			
			for (SetOfMorphemes<Affix> candidateSetOfAffixes : candidateSetsOfAffixes) {
				
				BinaryClassification evaluationOfCandidateSetOfAffixes = 
					candidateSetOfAffixesToEvaluation.get(candidateSetOfAffixes);
				double newFMeasure = evaluationOfCandidateSetOfAffixes.getMeasures().get(Measure.FMEASURE);
				
				boolean currentCandidateSetOfAffixesIsBestSoFar = false;
				if ( ! singleBestCandidateSetOfAffixesByF1_perSubClass.containsKey(subClassName)) {
					currentCandidateSetOfAffixesIsBestSoFar = true;
				} else {
					double bestFMeasureSoFar = 
						singleBestCandidateSetOfAffixesByF1_perSubClass.
							get(subClassName).score.getMeasures().get(Measure.FMEASURE);
					if (newFMeasure > bestFMeasureSoFar) {
						
						currentCandidateSetOfAffixesIsBestSoFar = true;
						
					} else if ((newFMeasure == bestFMeasureSoFar) &&
							   (newFMeasure != 0.0)) {
						
						// If we have found a candidate set of Affixes that is just as good
						// as the current best candidate set, then add this new candidate
						// set to 'singleBest...' and 'singleBest...achievable'--I don't
						// actually verify that the F1 of the candidate set measured wrt
						// the achievalbe suffixes is the same as the F1 of the previously
						// found candidate set--but I don't see how it couldn't be, since
						// the achievable answer key doesn't change within the same sub-class.
						singleBestCandidateSetOfAffixesByF1_perSubClass.
							get(subClassName).setsOfAffixes.add(candidateSetOfAffixes);
						singleBest_evaluatedWithAchievable_perSubClass.
							get(subClassName).setsOfAffixes.add(candidateSetOfAffixes);
					}
				}
				
				// If we have found a candidate set of Affixes that is better than any we
				// have found so far, then reset 'singleBest...' and 'singleBest...achievable'
				if (currentCandidateSetOfAffixesIsBestSoFar) {
					ScoredSetOfSetsOfAffixes scoredSetOfSetsOfAffixes = new ScoredSetOfSetsOfAffixes();
					ScoredSetOfSetsOfAffixes achievableScoredSetOfSetsOfAffixes = new ScoredSetOfSetsOfAffixes();
					if (newFMeasure != 0.0) {
						scoredSetOfSetsOfAffixes.setsOfAffixes.add(candidateSetOfAffixes);
						scoredSetOfSetsOfAffixes.score = evaluationOfCandidateSetOfAffixes;
						
						achievableScoredSetOfSetsOfAffixes.setsOfAffixes.add(candidateSetOfAffixes);
						achievableScoredSetOfSetsOfAffixes.score = 
							achievableEvaluationsPerSubClassPerCandidateAffixSet.
								get(subClassName).get(candidateSetOfAffixes); 
					}
					
					singleBestCandidateSetOfAffixesByF1_perSubClass.put(
							subClassName, 
							scoredSetOfSetsOfAffixes);
					singleBest_evaluatedWithAchievable_perSubClass.put(
							subClassName, 
							achievableScoredSetOfSetsOfAffixes);
				}
			}
		}
		
	}

	private void evaluatePerSubClassPerCandidateSetOfAffixes() {
		
		for (SubClassName subClassName : language.getSubClasses()) {
			SetOfMorphemes<Affix> affixesInSubClass = language.getAffixesIn(subClassName);
			
			SetOfMorphemes<Affix> achievableAffixesInSubClass = 
				returnAchievableAffixes(affixesInSubClass);
	
			// Initialize evaluationsPerSubClassPerCandidateAffixSet and
			//   achievableEvaluationsPerSubClassPerCandidateAffixSet
			Map<SetOfMorphemes<Affix>, 
				BinaryClassification> candidateSetOfAffixesToEvaluation = 
				new TreeMap<SetOfMorphemes<Affix>, 
							BinaryClassification>();
			Map<SetOfMorphemes<Affix>, 
				BinaryClassification> achievableCandidateSetOfAffixesToEvaluation = 
					new TreeMap<SetOfMorphemes<Affix>, 
								BinaryClassification>();
			
			evaluationsPerSubClassPerCandidateAffixSet.put(
					subClassName, 
					candidateSetOfAffixesToEvaluation);
			achievableEvaluationsPerSubClassPerCandidateAffixSet.put(
					subClassName, 
					achievableCandidateSetOfAffixesToEvaluation);
			
			// Calculate and save the perSubClass perCandidateAffixSet evaluations 
			for (SetOfMorphemes<Affix> candidateSetOfAffixes : candidateSetsOfAffixes) {
				
				BinaryClassification evaluationOfScheme = 
					getCorrectnessOfAffixes(affixesInSubClass, candidateSetOfAffixes);
				
				BinaryClassification achievableEvaluationOfScheme = 
					getCorrectnessOfAffixes(achievableAffixesInSubClass, candidateSetOfAffixes);

				candidateSetOfAffixesToEvaluation.put(
						candidateSetOfAffixes, 
						evaluationOfScheme);
				achievableCandidateSetOfAffixesToEvaluation.put(
						candidateSetOfAffixes, 
						achievableEvaluationOfScheme);
			}
		}
	}

	private SetOfMorphemes<Affix> returnAchievableAffixes(SetOfMorphemes<Affix> affixesInSubClass) {
		// gather the set of Affixes from this sub-class that actually occurred
		// in the corpus.
		SetOfMorphemes<Affix> achievableAffixesInSubClass = new SetOfMorphemes<Affix>();
		for (Affix affix : affixesInSubClass) {
			if (empiricalAffixOccurences.containsAll(affix)) {
				achievableAffixesInSubClass.add(affix);
			}
		}
		return achievableAffixesInSubClass;
	}

	private void evaluateGlobally() {
		
		// Place each affix that occurs in some true affix into one big set
		SetOfMorphemes<Affix> allTrueAffixes = language.getAllAffixes();
		
		// Place each affix that occurs in some selected scheme into one big set
		SetOfMorphemes<Affix> allAffixesInCandidateSetsOfAffixesToEvaluate = 
			new SetOfMorphemes<Affix>();
		for (SetOfMorphemes<Affix> candidateSetOfAffixes : candidateSetsOfAffixes) {
			for (Affix affix : candidateSetOfAffixes) {
				allAffixesInCandidateSetsOfAffixesToEvaluate.add(affix);
			}
		}
		
		BinaryClassification affixCorrectness = 
			getCorrectnessOfAffixes(allTrueAffixes,
									allAffixesInCandidateSetsOfAffixesToEvaluate);
		
		BinaryClassification achievableAffixCorrectness =
			getCorrectnessOfAffixes(empiricalAffixOccurences, 
									allAffixesInCandidateSetsOfAffixesToEvaluate);
		
		globalEvaluation = 
			new Evaluation.GlobalEvaluation(affixCorrectness,
											candidateSetsOfAffixes.size());
		globalAchievableEvaluation =
			new Evaluation.GlobalEvaluation(achievableAffixCorrectness,
											candidateSetsOfAffixes.size());
	}

	private BinaryClassification getCorrectnessOfAffixes(SetOfMorphemes<Affix> answers,
												  		 SetOfMorphemes<Affix> affixesToEvaluate) {
		// The set of true positive affixes is the intersection of allTrueAffixes and 
		// allSelectedAffixes.
		SetOfMorphemes<Affix> truePositiveAffixes = answers.intersect(affixesToEvaluate);
		
		// The number of false positive affixes is the number of affixes that were selected that
		// are not real affixes in the language.
		int numberOfFalsePositiveAffixes = 
			affixesToEvaluate.size() - truePositiveAffixes.size();
		
		// The number of false negative affixes is the number of true affixes that were not selected
		// with any scheme.
		int numberOfFalseNegativeAffixes = answers.size() - truePositiveAffixes.size();
		
		// The zero '0' in this new BinaryClassification is because there
		// are no true negatives.  Or you could think of there being a skazillion
		// true negatives-one true negative for every rejected candidate affix 
		// in the network (and I don't have access to the network from the Language
		// class anyway.)
		BinaryClassification affixCorrectness = 
			new BinaryClassification(truePositiveAffixes.size(),
									 0,
									 numberOfFalsePositiveAffixes,
									 numberOfFalseNegativeAffixes);
		
		/* TODO: to see what affixes were in the corpus that we missed comment out this code
		 * or rather fix up this code so it doesn't print miles and miles of junk when all you want
		 * is the first line...
		SetOfMorphemes<Affix> missedTrueAffixes = answers.minus(truePositiveAffixes);
		System.err.println();
		System.err.println("missedTrueAffixes: " + missedTrueAffixes);
		System.err.println();
		*/
		return affixCorrectness;
	}
	
	
	public String getEvaluationStringForSpreadsheet() {
		String toReturn = "";
		
		toReturn += getColumnTitleStringForSpreadsheet();
		toReturn += String.format("%n");
		
		toReturn += globalEvaluation.getStringForSpreadsheet();
		toReturn += String.format("%n");
		toReturn += globalAchievableEvaluation.getStringForSpreadsheet();
		toReturn += String.format("%n%n");
		
		
		for (SubClassName subClassName : singleBestCandidateSetOfAffixesByF1_perSubClass.keySet()) {
			toReturn += subClassName + ", Single Best by F1, ";
			toReturn += singleBestCandidateSetOfAffixesByF1_perSubClass.get(subClassName).
							getStringForSpreadsheet();
			toReturn += String.format("%n");
			
			toReturn += subClassName + ", Single Best out of achievable by strict F1, ";
			toReturn += singleBest_evaluatedWithAchievable_perSubClass.get(subClassName).
							getStringForSpreadsheet();
			toReturn += String.format("%n%n");
		
			
			toReturn += subClassName + ", Oracle Merge, ";
			toReturn += greedyOracleCandidateSetsOfAffixesMergedByF1_perSubClass.get(subClassName).
							getStringForSpreadsheet();
			toReturn += String.format("%n");
			
			toReturn += subClassName + ", Oracle Merge out of Achievable by strict F1, ";
			toReturn += greedyOracle_evaluatedWithAchievable_perSubClass.get(subClassName).
							getStringForSpreadsheet();
			toReturn += String.format("%n%n");
		}
		
		return toReturn;
	}
	
	public static String getColumnTitleStringForSpreadsheet() {
		String to_return = "";
		to_return += "Evaluation Set, Evaluation Type, ";
		to_return += BinaryClassification.getColumnTitleStringForSpreadsheet();
		to_return += ", # of Schemes";
		return to_return;
	}
	
	public static String getColumnTitleStringForGlobalScoreForSpreadsheet() {
		return GlobalEvaluation.getColumnTitleStringForSpreadsheet();
	}
	
	public static String getColumnTitleStringForPerSubClassScoresForSpreadsheet() {
		return ScoredSetOfSetsOfAffixes.getColumnTitleStringForSpreadsheet();
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		toReturn += String.format("%n");
		toReturn += String.format("Evaluation Results%n");
		toReturn += String.format("==================%n");
		toReturn += String.format("%n");
		toReturn += String.format("Single Best Schemes%n");
		toReturn += String.format("-------------------%n");
		toReturn += String.format("%n");
		for (SubClassName subClassName : language.getSubClasses()) {
			toReturn += subClassName;
			toReturn += String.format("%n");
			toReturn += String.format("- - - - - - - - - -%n");
			toReturn += singleBestCandidateSetOfAffixesByF1_perSubClass.get(subClassName);
			toReturn += String.format("%n");
		}
		toReturn += String.format("%n");
		toReturn += String.format("Greedy Oracle Merge Schemes%n");
		toReturn += String.format("---------------------------%n");
		toReturn += String.format("%n");
		for (SubClassName subClassName : language.getSubClasses()) {
			toReturn += subClassName;
			toReturn += String.format("%n");
			toReturn += String.format("- - - - - - - - - -%n");
			toReturn += greedyOracleCandidateSetsOfAffixesMergedByF1_perSubClass.get(subClassName);
			toReturn += String.format("%n");
		}
		toReturn += String.format("%n");
		toReturn += String.format("Global Score%n");
		toReturn += String.format("---------------------------%n");
		toReturn += String.format("%n");
		toReturn += globalEvaluation;

		return toReturn;
	}
	
	
	// A simple class to bind together a set of schemes with a score of those schemes.
	private static class ScoredSetOfSetsOfAffixes implements Serializable {
		private static final long serialVersionUID = 1L;
		
		// A list so we keep track of the *order* in which the SetOfMorphemes<Affix> instances
		// were added.  Particularly helpful in interpreting the greedily chosen
		// set of Affixes to merge.
		private List<SetOfMorphemes<Affix>> setsOfAffixes = new ArrayList<SetOfMorphemes<Affix>>();
		private BinaryClassification score = new BinaryClassification();
		
		public ScoredSetOfSetsOfAffixes() {
		}

		public ScoredSetOfSetsOfAffixes(ScoredSetOfSetsOfAffixes previousBestScoredSchemes) {
			setsOfAffixes.addAll(previousBestScoredSchemes.setsOfAffixes);
			score = previousBestScoredSchemes.score;
		}

		public static String getColumnTitleStringForSpreadsheet() {
			String toReturn = BinaryClassification.getColumnTitleStringForSpreadsheet();
			toReturn += ", # of Sets of Affixes";
			return toReturn;
		}
		
		public String getStringForSpreadsheet() {
			String to_return = "";
			to_return += score.getStringForSpreadsheet();
			to_return += ", " + setsOfAffixes.size();
			for (SetOfMorphemes<Affix> setOfAffixes : setsOfAffixes) {
				to_return += ", " + setOfAffixes;
			}
			return to_return;
		}
		
		@Override
		public String toString() {
			String toReturn = "";
			toReturn += setsOfAffixes.toString();
			toReturn += score.toString();
			toReturn += String.format(" # of Sets of Affixes: %d%n", setsOfAffixes.size());
			return toReturn;
		}
	}
	
	/**
	 * Represents the compiled results of comparing some set of Schemes against the
	 * true answer key defined in a sub-class of <code>Language</code>.
	 * 
	 * The only way to get a <code>GlobalEvaluation</code> is to call a method such as
	 * <code>evaluateSchemesGlobally</code>.  And the only thing to really do with 
	 * an GlobalEvaluation is to print it (or turn it into some kind of String, either for
	 * text printing, or comma delimited for spreadsheet printing.)
	 * 
	 * @author cmonson
	 *
	 */
	public static class GlobalEvaluation implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private BinaryClassification affixCorrectness;
		private int numberOfSelectedAffixes;
		
		// The only way to get a GlobalEvaluation is to call some public method of Evaluation
		private GlobalEvaluation(BinaryClassification affixCorrectness,
								 int numberOfSelectedSchemes) {
			this.affixCorrectness = affixCorrectness;
			this.numberOfSelectedAffixes = numberOfSelectedSchemes;
		}
				
		/**
		 * @return A simple pretty printed text String of the  
		 * 		   accuracy, precision, recall, and F1-measure of this, 
		 * 		   together with the
		 * 		   number of selected affixes for this <code>GlobalEvaluation</code>.	
		 */
		@Override
		public String toString() {
			
			String toReturn = affixCorrectness.toString();
			toReturn += String.format(" # of Selected Schemes: %d%n", numberOfSelectedAffixes);
	
			return toReturn;
		}
		
		/**
		 * @return A pretty printed text String of the 2<code>x</code>2 contingency table that
		 * 		   summarizes this true positives, false positives, and false negatives of
		 * 		   <code>BinaryClassification</code>.
		 */
		public String getOutcomeTableString() {
			return affixCorrectness.getOutcomeTableString();
		}
			
		/**
		 * Returns a comma separated string with the following format:
		 * <p>truePositive, falseNegative, falsePositive, trueNegative, Total, 
		 * 	  Accuracy, Precision, Recall, F1, # of Selected Schemes
		 */
		public String getStringForSpreadsheet() {
			String to_return = "";
			to_return += "Union of All Suffixes, Global, ";
			to_return += affixCorrectness.getStringForSpreadsheet();
			to_return += String.format(", %d", numberOfSelectedAffixes);
			return to_return;
		}
	
		/**
		 * @return A pretty comma delimited String listing the order of fields returned
		 *         by getStringForSpreadsheet().  It looks like:
		 *         
		 *         True +, False -, False +, True -, Total, Accuracy, Precision, Recall, F1, # of Selected Schemes
		 */
		public static String getColumnTitleStringForSpreadsheet() {
			String toReturn = BinaryClassification.getColumnTitleStringForSpreadsheet();
			toReturn += ", # of Selected Schemes";
			return toReturn;
		}
	
	}

	public GlobalEvaluation getGlobalEvaluation() {
		return globalEvaluation;
	}

}
