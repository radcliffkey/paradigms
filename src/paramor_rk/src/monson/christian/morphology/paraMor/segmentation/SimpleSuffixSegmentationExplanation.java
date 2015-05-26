/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.segmentation;

import java.util.Set;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster;

public class 
SimpleSuffixSegmentationExplanation extends SegmentationExplanation
	implements Comparable<SimpleSuffixSegmentationExplanation> {

	private static final long serialVersionUID = 1L;

	
	private Affix 						suffix;  // The suffix being stripped off of 'super.word'
	private BottomUpSearchResultCluster cluster; // The Cluster containing 'affix'
	
	// One or more other affixes in 'cluster' that can attach to the stem to form a word.
	private SetOfMorphemes<Affix>		explanatoryAffixes = new SetOfMorphemes<Affix>();  
	
	public 
	SimpleSuffixSegmentationExplanation(
			String word,
			Affix affix,
			BottomUpSearchResultCluster cluster, 
			SetOfMorphemes<Affix> explanatoryAffixes) {
		
		super(word);
		
		this.suffix   = affix;
		this.cluster = cluster;
		this.explanatoryAffixes = explanatoryAffixes;
	}
	
	public Affix getAffix() {
		return suffix;
	}

	public BottomUpSearchResultCluster getCluster() {
		return cluster;
	}

	public SetOfMorphemes<Affix> getExplanatoryAffix() {
		return explanatoryAffixes;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(suffix.toStringForSegmentation());
		toReturn.append(" --> ");
		
		SetOfMorphemes<Affix> explanatoryAffixesAsSetOfMorpheme =
			new SetOfMorphemes<Affix>(explanatoryAffixes);
		toReturn.append(explanatoryAffixesAsSetOfMorpheme.toString());
		toReturn.append("  \t");
		
		Set<Affix> coveredAffixes = cluster.getCoveredAffixes();
		SetOfMorphemes<Affix> coveredAffixesAsSetOfMorphemes = 
			new SetOfMorphemes<Affix>(coveredAffixes);
		
		toReturn.append(coveredAffixesAsSetOfMorphemes.toString());
		
		return toReturn.toString();
	}
	

	

	public int compareTo(SimpleSuffixSegmentationExplanation that) {
		if ( ! this.word.equals(that.word)) {
			return this.word.compareTo(that.word);
		}
		
		int thisSuffixComparedToThatSuffix = this.suffix.compareTo(that.suffix);
		if (thisSuffixComparedToThatSuffix != 0) {
			return thisSuffixComparedToThatSuffix;
		}
		
		int thisClusterToThatCluster = 
			BottomUpSearchResultCluster.byDecreasingNumberOfCoveredTypes.compare(
					this.cluster,
					that.cluster);
		if (thisClusterToThatCluster != 0) {
			return thisClusterToThatCluster;
		}
		
		
		int thisExplanatoryAffixToThatExpalanatoryAffix = 
			this.explanatoryAffixes.compareTo(that.explanatoryAffixes);
		
		if (thisExplanatoryAffixToThatExpalanatoryAffix != 0) {
			return thisExplanatoryAffixToThatExpalanatoryAffix;
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return word.hashCode() * 
			   suffix.hashCode() * 
			   cluster.hashCode() * 
			   explanatoryAffixes.hashCode();
	}



}
