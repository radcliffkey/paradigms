/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.segmentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.ConcatenativeMorpheme;
import monson.christian.morphology.paraMor.morphemes.Stem;

public class Segmentation implements Comparable<Segmentation>, Serializable {

	private static final long serialVersionUID = 1L;
	
	/*
	 * This 'explanations' field can have various interpretations depending on the use
	 * to which the 'Segmentation' class is put. But basically the 'explanations' are a
	 * place to hold WHY this 'Segmentation' contains the segmentation it does. 
	 */
	private Set<SimpleSuffixSegmentationExplanation> explanations = 
		new TreeSet<SimpleSuffixSegmentationExplanation>();
	
	private List<ConcatenativeMorpheme> morphemes = new ArrayList<ConcatenativeMorpheme>();
	
	public Segmentation(
			SimpleSuffixSegmentationExplanation explanation,
			ConcatenativeMorpheme... morphemes) {
		
		explanations.add(explanation);
		
		for (ConcatenativeMorpheme morpheme : morphemes) {
			this.morphemes.add(morpheme);
		}
	}
	
	public 
	Segmentation(
			String word, 
			SimpleSuffixSegmentationExplanation simpleSuffixSegmentationExplanation) {
		
		explanations.add(simpleSuffixSegmentationExplanation);
		
		
		// Regexes are brittle in the face of orthographic representations that use characters special to regexes.
		// The Buckwalter transliteration for Arabic uses, '|', '$', '&', etc. So just use String.subString().
		Affix suffixToStrip = simpleSuffixSegmentationExplanation.getAffix();
		//Pattern suffixToStripPattern = Pattern.compile("^(.*)" + suffixToStrip.toString() + "$");
		//Matcher suffixToStripMatcher = suffixToStripPattern.matcher(word);
		//suffixToStripMatcher.matches();
		//String stemPortionOfWord = suffixToStripMatcher.group(1);
		String stemPortionOfWord = word.substring(0, word.length() - suffixToStrip.length());
		Stem stem = new Stem(stemPortionOfWord);
		
		morphemes.add(stem);
		morphemes.add(suffixToStrip);
	}
	
	public Set<Affix> getAffixes() {
		
		Set<Affix> affixes = new HashSet<Affix>();
		
		for (ConcatenativeMorpheme concatenativeMorpheme : morphemes) {
			if (concatenativeMorpheme instanceof Affix) {
				Affix affix = (Affix)concatenativeMorpheme;
				affixes.add(affix);
			}
		}
		return affixes;
	}
	
	public Set<Stem> getStems() {
		
		Set<Stem> stems = new HashSet<Stem>();
		
		for (ConcatenativeMorpheme concatenativeMorpheme : morphemes) {
			if (concatenativeMorpheme instanceof Stem) {
				Stem stem = (Stem)concatenativeMorpheme;
				stems.add(stem);
			}
		}
		return stems;
	}
	
	public Set<SimpleSuffixSegmentationExplanation> getExplanations() {
		return explanations;
	}
	
	public void addExplanation(SimpleSuffixSegmentationExplanation explanation) {
		explanations.add(explanation);
	}
	
	/**
	 * This metho
	 */
	public int getTotalTypesCoveredByExplanations() {
		
		Set<String> allCoveredTypes = new HashSet<String>();
		for (SimpleSuffixSegmentationExplanation explanation : explanations) {
			allCoveredTypes.addAll(explanation.getCluster().getCoveredTypes());
		}
		
		int totalTypesCoveredByExplanations = allCoveredTypes.size();
		
		return totalTypesCoveredByExplanations;
	}
	
	/*
	 * Two Segmentations are equal if the sequences of morphemes in the segmentations
	 * are the same. So compareTo() IGNORES the 'explanations'.
	 */
	public int compareByMorphemeSequence(Segmentation that) {
		Iterator<ConcatenativeMorpheme> thisMorphemesIter = this.morphemes.iterator();
		Iterator<ConcatenativeMorpheme> thatMorphemesIter = that.morphemes.iterator();
		while (thisMorphemesIter.hasNext() && thatMorphemesIter.hasNext()) {
			ConcatenativeMorpheme thisMorpheme = thisMorphemesIter.next();
			ConcatenativeMorpheme thatMorpheme = thatMorphemesIter.next();
			
			if ( ! thisMorpheme.equals(thatMorpheme)) {
				return thisMorpheme.compareTo(thatMorpheme);
			}
		}
		
		if (thatMorphemesIter.hasNext()) { // thisSegmentationsIter must not have a next
			return -1;
		}
		if (thisMorphemesIter.hasNext()) { // thatSegmentationsIter must not have a next
			return 1;
		}

		return 0;
	}
	
	/*
	 * Two Segmentations are equal if the sequences of morphemes in the segmentations
	 * are the same. So compareTo() IGNORES the 'explanations'.
	 */
	public int compareTo(Segmentation that) {
		return compareByMorphemeSequence(that);
	}
	
	Iterator<SimpleSuffixSegmentationExplanation> explanationIterator() {
		return explanations.iterator();
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		boolean first = true;
		for (ConcatenativeMorpheme morpheme : morphemes) {
			if ( ! first) {
				toReturn.append(" ");
			} else {
				first = false;
			}
			toReturn.append(morpheme.toStringForSegmentation());
		}
		return toReturn.toString();
	}



}
