/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.segmentation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Stem;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster;

public class SegmentedWord implements Comparable<SegmentedWord>, Serializable {

	private static final long serialVersionUID = 1L;

	String word = null;
	TreeSet<Segmentation> segmentations = new TreeSet<Segmentation>();
	
	public enum OutputSegmentation {
		ALL_SEGMENTATIONS_AS_SEPARATE_ANALYSES(
				"allSegsAsSeparateAnalyses"),
		SINGLE_SEGMENTATION_FROM_SINGLE_LARGEST_CLUSTER(
				"segFromSingleLargestCluster"),
		SINGLE_SEGMENTATION_WHOSE_EXPLANATORY_CLUSTERS_JOINTLY_COVER_THE_MOST_TYPES(
				"segWhoseClustersJointlyCoverTheMostTypes"),
		COMBINED_SEGMENTATION(
				"combinedSegmentation");
		
		String shortDescriptionForFileNames;
		
		OutputSegmentation(String shortDescriptionForFileNames) {
			this.shortDescriptionForFileNames = shortDescriptionForFileNames;
		}

		public String getShortDescriptionOfOutputSegmentation() {
			return shortDescriptionForFileNames;
		}
	}
	
	public SegmentedWord(String word) {
		this.word = word;
	}
	
	public void addSegmentation(Segmentation segmentation) {
		segmentations.add(segmentation);
	}

	public void add(Segmentation segmentation) {
		segmentations.add(segmentation);
	}
	
	public String getSegmentationAs_shortestStemPlusAllFullAffixStrings() {
		String toReturn = "";
		toReturn += word + "\t";
		
		Stem stem = null;;
		Set<String> allFullAffixStrings = new TreeSet<String>();
		for (Segmentation segmentation : segmentations) {
			
			// Get the stem of the current 'segmentation', and save it if it is shorter
			// than any yet found.
			Set<Stem> stemsOfSegmentation = segmentation.getStems();
			if (stemsOfSegmentation.size() != 1) {
				String errorString = 
					"ERROR: the segmentation: " + segmentation.toString() +
					", of the word: " + word + ", has >1 stem: " + stemsOfSegmentation;
				System.err.println();
				System.err.println(errorString);
				System.err.println();
				
				return errorString;
			}
			
			// if a stem in 'stemsOfSegmentation' is shorter than the current 'stem'
			// then the new stem is now the shortest.
			for (Stem stemOfSegmentation : stemsOfSegmentation) {
				if ((stem == null) || 
					(stem.toString().matches("^" + stemOfSegmentation.toString() + ".*$"))) {
					
					stem = stemOfSegmentation;
				} else {
					if ( ! stemOfSegmentation.toString().matches(
							"^" + stem.toString() + ".*$")) {
						
						String errorString =
							"ERROR: neither the current shortest stem: " + stem.toString() +
							", nor the stem of the segmentation under examination: " +
							stemOfSegmentation.toString() + ", is an initial substring of " +
							"the other. This case has not yet been implemented";
						System.err.println();
						System.err.println(errorString);
						System.err.println();
						return errorString;
					}
				}
			}
						
			// Add all the affixes in the current segmentation to the set of all affixes
			// in any originally proposed segmentation.
			Set<Affix> affixesOfSegmentation = segmentation.getAffixes();
			for (Affix affixOfSegmentation : affixesOfSegmentation) {
				allFullAffixStrings.add(affixOfSegmentation.toStringForSegmentation());
			}		
		}
		
		if (stem != null) {  // equivalent to 'segmentations.size == 0
			toReturn += stem;
			for (String affixSurfaceForm : allFullAffixStrings) {
				toReturn += " " + affixSurfaceForm;
			}
		}
		
		if (segmentations.size() == 0) {
			toReturn += word;
		}
		return toReturn;		
	}
	
	public Segmentation getTheSegmentationCorrespondingToTheLargestCluster() {
		
		Segmentation segmentationFromLargestCluster = null;
		int sizeOfLargestCluster = 0;
		for (Segmentation segmentation : segmentations) {
			
			Set<SimpleSuffixSegmentationExplanation> explanations = 
				segmentation.getExplanations();
			
			for (SimpleSuffixSegmentationExplanation explanation : explanations) {

				BottomUpSearchResultCluster cluster = explanation.getCluster();

				if (cluster.getCoveredTypes().size() > sizeOfLargestCluster) {
					sizeOfLargestCluster = cluster.getCoveredTypes().size();
					segmentationFromLargestCluster = segmentation;
				}
			}
		}
		
		return segmentationFromLargestCluster;
	}
	
	public Segmentation getTheSegmentationWithTheMostWeightedVotes() {

		Segmentation segmentationWithTheMostWeightedVotes = null;
		int largestWeightedVotes = 0;
		for (Segmentation segmentation : segmentations) {
			
			int totalTypesCoveredByExplanationsOfThisSegmentation =
				segmentation.getTotalTypesCoveredByExplanations();
			
			if (totalTypesCoveredByExplanationsOfThisSegmentation > largestWeightedVotes) {
				largestWeightedVotes = totalTypesCoveredByExplanationsOfThisSegmentation;
				segmentationWithTheMostWeightedVotes = segmentation;
			}
		}
		
		return segmentationWithTheMostWeightedVotes;
	}
	
	// This method contains very strong assumptions. It assumes each 'Segmentation' in
	// 'segmentations' contains at most 2 'ConcatenativeMorpheme's. If 1 then that 1
	// morpheme is a Stem, if 2 then 1 is a Stem and the other a suffix (Affix).
	//
	// Given a set of segmentations like:
	//
	// abc +defg
	// abcd +efg
	// abcdef +g
	//
	// This method combines them as:
	//
	// abc +d +ef +g
	//
	// The algorithm to perform this combination takes the shortest stem of any
	// segmentation: abc, and then examines the remining suffix +defg. Moving on to
	// the next shortest suffix +efg, it strips the next shortest suffix off leaving +d.
	// And continues to the next shortest suffix etc.
	//
	// This all works fine because I am not even pretending to think about
	// morphophonology--but just pure and simple segmentation at character
	// boundaries.
	//
	private String getCombinedSegmentationString() {
		String combinedSegmentationAsString = "";
		
		combinedSegmentationAsString += word + "\t";
		
		// If this was has been analyzed as monomorphemic then just return
		// the word as its own segmentation.
		if (segmentations.size() == 0) {
			combinedSegmentationAsString += word;
			return combinedSegmentationAsString;
		}
		
		// Since 'segmentations' is sorted (in a TreeSet) and
		// since 'Segmentation's are compared lexicographically
		// with respect to their toString() methods, just iterating
		// through 'segmentations' proceedes in the order of
		// shorest to longest stem (and longest to shortest suffix)
		boolean first = true;
		String currentSuffix = null;
		for (Segmentation segmentation : segmentations) {

			// I was trying to be very general when I wrote 'Segmentation'.
			// Here I throw all that generality out the window and just get
			// the single assumed stem (respectively suffix) out of 'segmentation'.
			
			Iterator<Affix> affixIter = segmentation.getAffixes().iterator();
			String suffix = null;
			if (affixIter.hasNext()) {
				Affix affix = affixIter.next();
				if (affix.isNullAffix()) {
					suffix = "";
				} else {
					suffix = affix.toString();
				}
			}
			
			if (first) {
				first = false;
				combinedSegmentationAsString += 
					segmentation.getStems().iterator().next().toString(); // the stem
			
			} else {
				if (suffix == null) {
					if (segmentations.size() != 1) {
						System.err.println();
						System.err.println("ERROR!");
						System.err.println();
						System.err.println("The suffix of a Segmentation is 'null'");
						System.err.println("  In which case there should only be");
						System.err.println("  a single Segmentation in this");
						System.err.print("  'SegmentedWord', but there were: ");
						System.err.println(segmentations.size());
						System.err.println();
						System.err.println(this.toString());
						System.err.println();
						System.err.println();
					}
				}
				
				// An alphabet (such as the Buckwalter transliteration of Arabic) may employ characters
				// that are treated special by regexes. So use String.substring(startpos, one-past-end-position);
				//String newAffixToAdd = currentSuffix.replaceAll(suffix + "$", "");
				String newAffixToAdd = null;
				try {
					newAffixToAdd = currentSuffix.substring(0, currentSuffix.length() - suffix.length());
				} catch (Exception e) {
					System.err.println(currentSuffix);
					System.err.println(suffix);
					System.err.println(currentSuffix.length());
					System.err.println(suffix.length());
				}
				combinedSegmentationAsString +=	" +" + newAffixToAdd;
			}
			
			currentSuffix = suffix;
		}
		
		if (currentSuffix != null && !currentSuffix.isEmpty()) {
			combinedSegmentationAsString += " +" + currentSuffix;
		}
		
		return combinedSegmentationAsString;
	}
	
	
	public String toString(OutputSegmentation outputSegmentation) {
		
		String toReturn = "";
		toReturn += word + "\t";
		
		switch (outputSegmentation) {
		case ALL_SEGMENTATIONS_AS_SEPARATE_ANALYSES:
			return toString();
			
		case SINGLE_SEGMENTATION_FROM_SINGLE_LARGEST_CLUSTER:
			Segmentation singleSegmentationFromSingleLargestCluster =
				getTheSegmentationCorrespondingToTheLargestCluster();
			toReturn += singleSegmentationFromSingleLargestCluster.toString();
			return toReturn;

		case SINGLE_SEGMENTATION_WHOSE_EXPLANATORY_CLUSTERS_JOINTLY_COVER_THE_MOST_TYPES:
			Segmentation singleSegmentationWhoseExplanatoryClustersJointlyCoverTheMostTypes =
				getTheSegmentationWithTheMostWeightedVotes();
			toReturn += 
				singleSegmentationWhoseExplanatoryClustersJointlyCoverTheMostTypes.toString();
			return toReturn;
			
		case COMBINED_SEGMENTATION:
			String combinedSegmentationString =
				getCombinedSegmentationString();
			return combinedSegmentationString;
		}
		
		String errorString = "ERROR: I FAILED TO CORRECTLY OUTPUT A SEGMENTED WORD" +
							 "  I DO NOT KNOW HOW TO WRITE OUT SEGMENTATIONS FORMATTED AS:" +
							 "    " + outputSegmentation;
		
		System.err.println(errorString);
		System.err.println();
		
		return errorString;
	}

	public CharSequence getSegmentationExplanationString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(word);
		toReturn.append("\n");
		
		boolean first = true;
		for (Segmentation segmentation : segmentations) {
			toReturn.append(String.format("%n"));
			if (first) {
				first = false;
			} else {
				toReturn.append(String.format("%n"));
			}
			toReturn.append("\t");
			toReturn.append(segmentation.toString());
			
			Iterator<SimpleSuffixSegmentationExplanation> segmentationExplanationsIterator =
				segmentation.explanationIterator();
			
			while(segmentationExplanationsIterator.hasNext()) {
				SimpleSuffixSegmentationExplanation segmentationExplanation =
					segmentationExplanationsIterator.next();
				
				toReturn.append(String.format("%n\t\t"));
				toReturn.append(segmentationExplanation.toString());
				
			}
		}

		if (segmentations.size() == 0) {
			toReturn.append(String.format("%n\tNO SEGMENTATIONS"));
		}
		
		return toReturn.toString();
	}

	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(word);
		toReturn.append("\t");
		
		boolean first = true;
		for (Segmentation segmentation : segmentations) {

			if ( ! first) {
				toReturn.append(", ");
			} else {
				first = false;
			}
			toReturn.append(segmentation.toString());
		}

		if (segmentations.size() == 0) {
			toReturn.append(word);
		}
		
		return toReturn.toString();
	}

	public int compareTo(SegmentedWord that) {
		if ( ! this.word.equals(that.word)) {
			return this.word.compareTo(that.word);
		}
		
		Iterator<Segmentation> thisSegmentationsIter = this.segmentations.iterator();
		Iterator<Segmentation> thatSegmentationsIter = that.segmentations.iterator();
		if (thisSegmentationsIter.hasNext() && thatSegmentationsIter.hasNext()) {
			Segmentation thisSegmentation = thisSegmentationsIter.next();
			Segmentation thatSegmentation = thatSegmentationsIter.next();
			
			if ( ! thisSegmentation.equals(thatSegmentation)) {
				return thisSegmentation.compareTo(thatSegmentation);
			}
		}
		
		if (thatSegmentationsIter.hasNext()) { // thisSegmentationsIter must not have a next
			return -1;
		}
		if (thisSegmentationsIter.hasNext()) { // thatSegmentationsIter must not have a next
			return 1;
		}
		return 0;
	}

}
