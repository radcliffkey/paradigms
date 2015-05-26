/**
 *
 */
package monson.christian.morphology.paraMor.schemes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Analysis;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.Morpheme;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
//import monson.christian.morphology.paraMor.searchAndProcessing.NetworkSearchProcedure.Result;
import monson.christian.statistics.ListOfData;
import monson.christian.util.ComparablePair;
import monson.christian.util.PairList;


public class Scheme extends AffixScheme /*implements NetworkSearchProcedure.Result*/ {
    
    private static final long serialVersionUID = 1L;
    
	/**
	 * Sorts Schemes first by decreasing adherent size and then by increasing
	 * lexicographic order.
	 * 
	 * @author cmonson
	 *
	 */
	public static class ByDecreasingAdherentSize implements Comparator<Scheme>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * @param scheme1 The first <code>Scheme</code> to be compared 
		 * @param scheme2 The second <code>Scheme</code> to be compared
		 * @return <code>-1</code> if <code>scheme1</code>'s adherent size is greater than 
		 * 				<code>scheme2</code>'s adherent size.
		 * 	   <br><code>&nbsp1</code> if <code>scheme2</code>'s adherent size is greater than
		 * 			    <code>scheme1</code>'s adherent size.
		 *     <br> if <code>scheme1</code>'s adherent size equals <code>scheme2</code>'s adherent size
		 *              then <code>scheme1</code> and <code>scheme2</code> are compared
		 *              lexicographically.
		 */
		public int compare(Scheme scheme1, Scheme scheme2) {
			if (scheme1.adherentSize() > scheme2.adherentSize()) {
				return -1;
			}
			if (scheme1.adherentSize() < scheme2.adherentSize()) {
				return 1;
			}
			// If the adherent sizes of node1 and node2 are equal then compare the two
			// nodes lexicographically.  
			return scheme1.compareTo(scheme2);
		}
	}
	
	public static class ByIncreasingAdherentSize implements Comparator<Scheme>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * @param scheme1 The first <code>Scheme</code> to be compared 
		 * @param scheme2 The second <code>Scheme</code> to be compared
		 * @return <code>-1</code> if <code>scheme1</code>'s adherent size is greater than 
		 * 				<code>scheme2</code>'s adherent size.
		 * 	   <br><code>&nbsp1</code> if <code>scheme2</code>'s adherent size is greater than
		 * 			    <code>scheme1</code>'s adherent size.
		 *     <br> if <code>scheme1</code>'s adherent size equals <code>scheme2</code>'s adherent size
		 *              then <code>scheme1</code> and <code>scheme2</code> are compared
		 *              lexicographically.
		 */
		public int compare(Scheme scheme1, Scheme scheme2) {
			if (scheme1.adherentSize() < scheme2.adherentSize()) {
				return -1;
			}
			if (scheme1.adherentSize() > scheme2.adherentSize()) {
				return 1;
			}
			// If the adherent sizes of node1 and node2 are equal then compare the two
			// nodes lexicographically.  
			return scheme1.compareTo(scheme2);
		}
	}
	
	public static class ByAffixSignature implements Comparator<Scheme>, Serializable {
		private static final long serialVersionUID = 1L;

		public int compare(Scheme scheme1, Scheme scheme2) {
			return scheme1.affixSignature().compareTo(scheme2.affixSignature());
		}
	}

	
	
	//////////////////////////////////
	// Members and Methods
	
    
	private SetOfMorphemes<Context> contexts;
    
	/**
	 * Creates an empty <code>Scheme</code> with no affixes and no contexts
	 */
    public Scheme() {
        super();
        contexts = new SetOfMorphemes<Context>();
    }
    
	/**
	 * @param likelyAffixCount : better initial space allocation for this Scheme's set of affixes
	 */
    public Scheme(int likelyAffixCount) {
    	super(likelyAffixCount);
    	contexts = new SetOfMorphemes<Context>();
    }
 
	/**
	 * @param likelyAffixCount : better initial space allocation for this Scheme's set of affixes
	 * @param likelyContextCount : better initial space allocation for this Schemes set of contexts
	 */
    public Scheme(int likelyAffixCount, int likelyContextCount) {
    	super(likelyAffixCount);
    	contexts = new SetOfMorphemes<Context>(likelyAffixCount);
    }
      
    /** 
     * This constructor assumes that the passed in <code>affixes</code> are a
     * good estimate of the number of affixes you will ever want in this <code>Scheme</code>.
     * 
     * Creates an empty contexts for this Scheme
     */
    public Scheme(Affix... affixes) {
        super(affixes);
    	contexts = new SetOfMorphemes<Context>();
    }
    
    /**
     * Shallowly assigns <code>affixes</code> as this Scheme's set of affixes
     * 
 	 * Creates an empty contexts for this Scheme
     */
    public Scheme(SetOfMorphemes<Affix> affixes) {
    	super(affixes);
    	contexts = new SetOfMorphemes<Context>();
    }
    
	/**
	 * Shallowly assigns <code>affixes</code> as this Scheme's set of affixes and
	 * <code>contexts</code> as this Scheme's set of contexts.
	 */
	public Scheme(SetOfMorphemes<Affix> affixes, SetOfMorphemes<Context> contexts) {
		super(affixes);
		this.contexts = contexts;
	}
	
    /**
     * Deep conversion to Scheme.  
     * Copy over affixes to mostSpecificScheme.
     * Create an empty contexts.
     * 
     * @param mostSpecificScheme
     */
	public Scheme(AffixScheme affixScheme) {
		super(affixScheme);
		contexts = new SetOfMorphemes<Context>();
	}
	
	/**
	 * Deep copy of <code>scheme</code>'s affixes and contexts
	 *
	 */
	public Scheme(Scheme scheme) {
    	super(scheme);
    	contexts = new SetOfMorphemes<Context>(scheme.contexts);
	}
    

    public void addToContexts(Context... contexts) {
        this.contexts.add(contexts);
    }
    
    public void addToContexts(SetOfMorphemes<Context> contexts) {
        this.contexts.add(contexts);
    }
    
	public SetOfMorphemes<Context> getContexts() {
		return contexts;
	}
	
	public void intersectStemsInPlace(Scheme that) {
		contexts.intersectInPlace(that.contexts);
	}
	
	public Integer adherentSize() {
		return contexts.size();
	}
	
	public int getMinimumLengthOfStems() {
		return contexts.getMinimumContainedLength();
	}

	public Double getAverageLengthOfStems() {
		return contexts.getAverageContainedLength();
	}
	
	/**
	 *
	 * @return The set of Strings formed by concatenating each context to each affix,
	 * 		   one by one, in this <code>Scheme</code>
	 */
	public Set<String> getStringsInThisScheme() {
		Set<String> stringsInThisScheme = new HashSet<String>();
		for (Context context : contexts) {
			for (Affix affix : affixes) {
				String aStringInThisScheme = Morpheme.computeSurfaceString(context, affix);
				stringsInThisScheme.add(aStringInThisScheme);
			}
		}
		return stringsInThisScheme;
	}
	
	/**
	 * For each unique context-final character <code>c</code> in the contexts of <code>this Scheme</code> the
	 * left trie affix set is that set of <code>Affixes</code> formed by prepending <code>c</code>
	 * to the front of all the affixes in <code>this Scheme</code>.
	 * 
	 * *** NOTE *** If the NULL context is in this Scheme the returned Map does ***NOT** include a mapping:
	 * 
	 * *null-character* -> 'this scheme'             <- NOT INCLUDED !!
	 * 
	 * Not including this recursive mapping parallels the behavior of getRightFSAAffixSets(), although
	 * things are a bit more confusing here because we are dealing with contexts AND affixes.  You can think
	 * of it as getRightFSAAffixSets() does not follow the *null-character* to that scheme with
	 * the identical set of STEMS (namely a circular loop.)
	 * 
	 * This function cannot be defined in <code>AffixScheme</code> because unlike
	 * <code>getRightFSAAffixSets()</code> this function depends not only on a set of affixes
	 * but also on the set of contexts in that set of affixes. 
	 * 
	 * @return a <code>Map</code> from the character prepended on the current set of affixes in this scheme
	 *                            to the the new set of affixes formed by prepending the character.
	 *                            
	 *                            <code>null</code> if the parameter type 'A' is NOT 'Affix'
	 *                            
	 * @see AffixScheme.getRightFSAAffixSets()
	 */
	public Map<Character, SetOfMorphemes<Affix>> getLeftTrieAffixSets() {
		
		Map<Character, SetOfMorphemes<Affix>> leftTrieAffixSets = 
			new TreeMap<Character, SetOfMorphemes<Affix>>();
		
		Set<Character> stemFinalCharacters = new HashSet<Character>();
		
		for (Context context : contexts) {
			Character stemFinalCharacter = context.getMorphemeCharacterToLeftOfSlot();			
			
			if (stemFinalCharacter == null) {
				continue;
			}
			
			stemFinalCharacters.add(stemFinalCharacter);
		}
		
		for (Character aStemFinalCharacter : stemFinalCharacters) {
			SetOfMorphemes<Affix> prependedAffixes = new SetOfMorphemes<Affix>(affixes.size());
			for (Affix oldAffix : affixes) {
				Affix newAffix = oldAffix.createAffixByAddingALeadChar(aStemFinalCharacter);
				prependedAffixes.add(newAffix);
			}
			
			leftTrieAffixSets.put(aStemFinalCharacter, prependedAffixes);
		}
			
		return leftTrieAffixSets;
	}
	
	/**
	 * Get the counts of each unique context-final character in this Scheme.
	 * 
	 * The counts here INCLUDE NullCharacters!!
	 * 
	 * This behavior parallels that of isRightFSAUnambiguous().  Just as scheme is rightward
	 * ambiguous if it contains the *null-affix*, a scheme is one more time ambiguous to the
	 * left if it contains *null-context*. 
	 */
	public PairList<Character, Integer> getLeftTrieStemCounts() {
		
		Map<Character, Integer> leftTrieStemCounts = 
			new HashMap<Character, Integer>();  // HashMap to allow null 'stemFinalCharacter's
		
		for (Context context : contexts) {
			Character stemFinalCharacter = context.getMorphemeCharacterToLeftOfSlot();
			
			int currentCharCount = 0;
			if (leftTrieStemCounts.containsKey(stemFinalCharacter)) {
				currentCharCount = leftTrieStemCounts.get(stemFinalCharacter);
			}
		
			leftTrieStemCounts.put(stemFinalCharacter, currentCharCount+1);
		}
		
		PairList<Character, Integer> leftTrieStemCountsPairList = 
			PairList.pairListFromMap(leftTrieStemCounts);
		
		return leftTrieStemCountsPairList;
	}
	
	public int getMaxLeftStemCount() {
		PairList<Character, Integer> leftTrieStemCounts = getLeftTrieStemCounts();
		int maxLeftStemCount = 0;
		for (Integer leftStemCount : leftTrieStemCounts.rights()) {
			if (leftStemCount > maxLeftStemCount) {
				maxLeftStemCount = leftStemCount;
			}
		}
		
		return maxLeftStemCount;
	}
	
	public double getMaxLeftRatio() {
		PairList<Character, Integer> leftTrieStemCounts = getLeftTrieStemCounts();
		int maxLeftStemCount = 0;
		for (Integer leftStemCount : leftTrieStemCounts.rights()) {
			if (leftStemCount > maxLeftStemCount) {
				maxLeftStemCount = leftStemCount;
			}
		}
		
		double maxLeftRatio = (double)maxLeftStemCount / (double)contexts.size();
		return maxLeftRatio;
	}
	
	public double getLeftEntropy() {
		PairList<Character, Integer> leftTrieStemCounts = getLeftTrieStemCounts();
		ListOfData<Double> leftTrieStemRatios = new ListOfData<Double>();

		for (Integer leftStemCount : leftTrieStemCounts.rights()) {
			leftTrieStemRatios.add((double)leftStemCount / (double)contexts.size());
		}
		
		double entropy = leftTrieStemRatios.getEntropy();
		return entropy;
	}
	
	public String getLeftwardSummaryString() {
		String toReturn = "";
		
		toReturn += String.format("Leftward Summary%n");
		toReturn += String.format("--------------------------%n%n");
		toReturn += String.format("  Max Left (Ratio): %d/%d (%5.3f)%n%n", 
								  getMaxLeftStemCount(), 
								  adherentSize(),
								  getMaxLeftRatio());
		toReturn += String.format("  Left Entropy:     %-6.3f%n%n", getLeftEntropy());
		
		PairList<Character, Integer> leftTrieStemCounts = getLeftTrieStemCounts();
		Collections.sort(leftTrieStemCounts, new ComparablePair.ByRightDecreasing<Character, Integer>());		
		
		for (ComparablePair<Character, Integer> pair : leftTrieStemCounts) {
			toReturn += "  " + pair.getLeft() + " : " + pair.getRight();
			toReturn += String.format("%n");
		}
		
		toReturn += String.format("%n");
		
		return toReturn;
	}

	
	public String toPrettyString(int maxMorphemes) {
		// Since affixes or contexts.toShortString() may actually contain the 
		// character '%' (or possibly other meta characters) we can't put 
		// them inside a String.format() call.
		String toReturn;
		toReturn  = String.format( "(affixes  [%5d] = ", affixes.size());
		toReturn += affixes;
		toReturn += String.format(",%n contexts    [%5d] = ", contexts.size());
		toReturn += contexts.toShortString(maxMorphemes) + ")";
			
		return toReturn;
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		toReturn += String.format( "(affixes       [%5d] = ", affixes.size());
		toReturn += affixes; 
		toReturn += String.format(",%n contexts         [%5d] = ", contexts.size());
		toReturn += contexts;
		toReturn += String.format(")");
		
		return toReturn;
	}

	public List<String> getCoveredWordTypes() {
		List<String> coveredWordTypes = new ArrayList<String>();
		for (Context context : contexts) {
			for (Affix affix : affixes) {
				String coveredType = Morpheme.computeSurfaceString(context, affix);
				coveredWordTypes.add(coveredType);
			}
		}
		
		return coveredWordTypes;
	}

	public List<ComparablePair<Affix, Context>> getCoveredAffixContextPairs() {
		List<ComparablePair<Affix, Context>> toReturn = new ArrayList<ComparablePair<Affix, Context>>();

		for (Affix affix : affixes) {
			for (Context context : contexts) {
				ComparablePair<Affix, Context> affixStemPair = new ComparablePair<Affix, Context>(affix, context);
				toReturn.add(affixStemPair);
			}
		} 
		
		return toReturn;
	}
	
	/**
	 * 
	 * @param type
	 * @return A new <code>Analysis</code> if <code>this Scheme</code> can successfully
	 * 		   analyze <code>type</code>, <code>null</code> otherwise.  A <code>Scheme</code>
	 * 		   can analyze <code>type</code> if the <code>Scheme</code> contains a <code>Context</code> and a
	 * 		   <code>Affix</code> that when combined into a surface string yield the
	 * 		   target <code>type String</code>.
	 */
	public Analysis analyze(String type) {
		for (Context context : contexts) {
			for (Affix affix : affixes) {
				String coveredType = Morpheme.computeSurfaceString(context, affix);
				if (coveredType.equals(type)) {
					return new Analysis(context, affix);
				}
			}
		}
		return null;
	}




}
