/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.segmentation;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class SegmentedWordList implements Serializable {

	private static final long serialVersionUID = 1L;

	SortedSet<SegmentedWord> segmentedWords = new TreeSet<SegmentedWord>();
	

	public void add(SegmentedWord segmentedWord) {
		segmentedWords.add(segmentedWord);
	}
	
	public String getSegmentationAs_shortestStemPlusAllFullAffixStrings() {
		String toReturn = "";
		
		for (SegmentedWord segmentedWord : segmentedWords) {
			toReturn += segmentedWord.getSegmentationAs_shortestStemPlusAllFullAffixStrings();
			toReturn += String.format("%n");
		}
		
		return toReturn;
	}

	public String toString(SegmentedWord.OutputSegmentation outputSegmentation) {
		StringBuilder toReturn = new StringBuilder();
		
		for (SegmentedWord segmentedWord : segmentedWords) {
			toReturn.append(segmentedWord.toString(outputSegmentation));
			toReturn.append(String.format("%n"));
		}
		
		return toReturn.toString();
	}
	
	public String getSegmentationExplanationString() {
		StringBuilder toReturn = new StringBuilder();
		
		boolean first = true;
		for (SegmentedWord segmentedWord : segmentedWords) {
			if (first) {
				first = false;
			} else {
				toReturn.append(String.format("%n"));
			}
			toReturn.append("----------------------------" + String.format("%n"));
			
			toReturn.append(segmentedWord.getSegmentationExplanationString());
			toReturn.append(String.format("%n"));
		}
		
		return toReturn.toString();	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		
		for (SegmentedWord segmentedWord : segmentedWords) {
			toReturn.append(segmentedWord.toString());
			toReturn.append(String.format("%n"));
		}
		
		return toReturn.toString();
	}


}
