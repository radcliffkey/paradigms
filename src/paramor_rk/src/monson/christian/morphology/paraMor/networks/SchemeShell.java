/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.networks;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import klic.radoslav.morphology.ManualData;
import klic.radoslav.settings.Settings;
import klic.radoslav.util.DebugLog;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Level1Scheme;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.schemes.SchemeSet;

public class SchemeShell implements Serializable {

	private static final long serialVersionUID = 1L;

	
	private Set<Scheme> mostSpecificSchemes = new TreeSet<Scheme>();
	
	private Map<Integer, 
				Set<Scheme>> mostSpecificSchemesByLevel =
	    new TreeMap<Integer, Set<Scheme>>();
	
	private Map<SetOfMorphemes<Affix>, 
				Scheme> mostSpecificSchemesByName =
		new TreeMap<SetOfMorphemes<Affix>,
					Scheme>();

	private Set<Level1Scheme> level1Schemes = new TreeSet<Level1Scheme>();
	
	private Map<Affix,
				Level1Scheme> level1SchemesByAffix = 
		new TreeMap<Affix, 
					Level1Scheme>();
		
	@SuppressWarnings("unchecked")
	public SchemeShell(PartialOrderNetwork.Identifier identifier) {
		this(identifier, (List<Pattern>) Settings.getOption("allowedSuffixes"), null);
	}
	
	public SchemeShell(
			PartialOrderNetwork.Identifier identifier,
			List<Pattern> allowedAffixes,
			Character restrictWordsToBeginWithThisCharacter) {

		computeMostSpecificSchemes(identifier,
				allowedAffixes,
				restrictWordsToBeginWithThisCharacter);
		computeLevel1Schemes();
		computeSchemesByName();
	}

	private void computeSchemesByName() {
		
		System.err.println("Computing schemes by name");
		
		for (Scheme mostSpecificScheme : mostSpecificSchemes) {
			SetOfMorphemes<Affix> name = mostSpecificScheme.getAffixes();
			mostSpecificSchemesByName.put(name, mostSpecificScheme);
		}
		
		for (Level1Scheme level1Scheme : level1Schemes) {
			Affix affix = level1Scheme.getAffix();
			level1SchemesByAffix.put(affix, level1Scheme);
		}
		
		System.err.println("Done computing schemes by name");
	}
	
	private void computeMostSpecificSchemes(
			PartialOrderNetwork.Identifier identifier,
			List<Pattern> allowedAffixes, 
			Character restrictWordsToBeginWithThisCharacter) {
		
		System.err.println();
		System.err.println();
		System.err.println("Computing the Most Specific Schemes from the corpus:");
		System.err.println();
		System.err.println(identifier.getCorpus());
		
		if (allowedAffixes != null) {
			System.err.println();
			System.err.println("Restricted to schemes containing only affixes among");
			System.err.println("  the following:");
			System.err.println(allowedAffixes.toString());
			DebugLog.write("Restricted to schemes containing only affixes among"
					+ allowedAffixes);
		}
		
		if (restrictWordsToBeginWithThisCharacter != null) {
			System.err.println();
			System.err.println("Restricted to words in the corpus which begin with the");
			System.err.println("  character: " + restrictWordsToBeginWithThisCharacter);
		}
		
		System.err.println("-------------------------------------------------------" +
						   "------------");
		
		System.err.println();
		System.err.println("  Collecting all candidate affixes from all words in the vocabulary.");
		System.err.println("  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		int vocabularyCounter = 0;
		int validVocabularyCounter = 0;

		Map<Context, SetOfMorphemes<Affix>> stemToAffixes = new HashMap<Context, SetOfMorphemes<Affix>>();

	    Corpus corpus = identifier.getCorpus();
    	PartialOrderNetwork.MorphemicAnalysis morphemicAnalysis = identifier.getMorphemicAnalysis();
	    
	    // Create the hash from candidate context to set of affixes that
	    // can attach to this context
	    for (String type : corpus.getVocabulary()) {
	    	
	    	vocabularyCounter++;
	    	if ((vocabularyCounter%10000) == 0) {
	    		System.err.println("    " + vocabularyCounter + "th vocabulary item, of " + 
	    						   corpus.getVocabularySize() + " : " + type);
	    	}
	    	if (restrictWordsToBeginWithThisCharacter != null) {
	    		if (type.charAt(0) != restrictWordsToBeginWithThisCharacter) {
	    			continue;
	    		}
	    		validVocabularyCounter++;
	    		if ((validVocabularyCounter%1000) == 0) {
	    			System.err.println("      " + validVocabularyCounter + "th vocabulary item that" +
	    			                   "        begins with the character: " +
	    			                   restrictWordsToBeginWithThisCharacter +
	    			                   " : " + type);
	    		}
	    	}
	    	
	    	Map<Context, Affix> stemAffixPairs = null;
	    	
	    	switch (morphemicAnalysis) {
	    	case SUFFIX:
	    		stemAffixPairs = 
	    			getStemAffixPairsUsingASingleBoundary(type, false, true, identifier.getAllowEmptyStems());
	    		break;
	    	case PREFIX:
	    		stemAffixPairs = 
	    			getStemAffixPairsUsingASingleBoundary(type, true, false, identifier.getAllowEmptyStems());
	    		break;
	    	case SUFFIX_PREFIX:
	    		stemAffixPairs = 
	    			getStemAffixPairsUsingASingleBoundary(type, true, true, identifier.getAllowEmptyStems());
	    		break;
	    	case SLOT:
	    		stemAffixPairs = getStemAffixPairsUsingTwoBoundaries(type, identifier.getAllowEmptyStems());
	    		break;
	    	default:
	    		assert false : "Unrecognized 'MorphemicAnalysis': " + morphemicAnalysis;
	    	} 
		        
	        for (Context context : stemAffixPairs.keySet()) {
	            Affix affixToAdd = stemAffixPairs.get(context);
	            
				// Restrict to legal Affixes
				if (allowedAffixes != null) {
					if (!StringUtil.matchesAny(affixToAdd.getSurfaceString(), allowedAffixes)) {
						continue; 
					}
				}
	            
	            if (stemToAffixes.containsKey(context)) {
	                SetOfMorphemes<Affix> setOfAffixes = stemToAffixes.get(context);
	                setOfAffixes.add(affixToAdd);
	            } else {
	                stemToAffixes.put(context, new SetOfMorphemes<Affix>(affixToAdd));
	            }
	        }	        
	    }
	    
	    Integer lenLimit = identifier.getCorpus().getMaxSuffixLength();
	    if (lenLimit != null) {
	    	this.removeLongAffixes(stemToAffixes, lenLimit);
	    }
	    
	    try {
	    	if (ManualData.areStemChangeRulesAvailable()) {
	    		ManualData.joinStemVariants(stemToAffixes);
	    	}
		} catch (IOException e) {
			DebugLog.write(e.getMessage());
		}
	    
	    System.err.println();
	    System.err.println("  Binning candidate stems by the affixes that can attach to them.");
	    System.err.println("  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
	    int stemCounter = 0;
	    
	    // And now go the other way putting stems into hashes based on the
	    // setOfMorphemes<Affix> that they point to.  This is then a most
	    // specific scheme.
	    Map<SetOfMorphemes<Affix>, SetOfMorphemes<Context>> affixesToStems
	    	= new HashMap<SetOfMorphemes<Affix>, SetOfMorphemes<Context>>();
	    
	    for (Context context : stemToAffixes.keySet()) {
	    	stemCounter++;
	    	if ((stemCounter%10000) == 0) {
	    		System.err.println("    " + stemCounter + "th candidate context (" +
	    						   context + "), of " + stemToAffixes.size());
	    	}
	    	
	        SetOfMorphemes<Affix> affixes = stemToAffixes.get(context);
	        
	        if (affixesToStems.containsKey(affixes)) {
	            SetOfMorphemes<Context> contexts = affixesToStems.get(affixes);
	            contexts.add(context);
	        } else {
	            affixesToStems.put(affixes, new SetOfMorphemes<Context>(context));
	        }
	    }
	    
	    
	    System.err.println();
	    System.err.println("  Creating most specific schemes.");
	    System.err.println("  - - - - - - - - - - - - - - - -");
	    int mostSpecificSchemeCounter = 0;
	    
	    // And turn these pairs of SetOfMorphemes<Suffix> and SetOfMorphemes<Context>
	    // into MostSpecificSchemes
	    Scheme mostSpecificScheme;
	    for (SetOfMorphemes<Affix> affixes : affixesToStems.keySet()) {
	    	mostSpecificSchemeCounter++;
	    	if ((mostSpecificSchemeCounter%10000) == 0) {
	    		System.err.println("    " + mostSpecificSchemeCounter + 
	    						   "th most specific scheme (" + affixes + "), of " +
	    						   affixesToStems.size());
	    	}
	    	
	        SetOfMorphemes<Context> contexts = affixesToStems.get(affixes);

	        mostSpecificScheme = new Scheme(affixes, contexts);

	        // Add the new MostSpecificScheme to the set of all MostSpecificSchemes
	        mostSpecificSchemes.add(mostSpecificScheme);

	        // Add the new MostSpecificScheme to the set of MostSpecificSchemes
	        // organized by level
	        int level = mostSpecificScheme.level();

	        if ( ! mostSpecificSchemesByLevel.containsKey(level)) {
	        	mostSpecificSchemesByLevel.put(level, new HashSet<Scheme>());
	        }
	        Set<Scheme> mostSpecificSchemesAtALevel = 
	        	mostSpecificSchemesByLevel.get(level);

	        mostSpecificSchemesAtALevel.add(mostSpecificScheme);
	    }
	    
		System.err.println();
	    System.err.println("  " + mostSpecificSchemes.size()
	    				   + " most specific schemes arise from the read in corpus.");
	}

	/**
	 * Removes affixes longer than given limit
	 * @param stemToAffixes a map stem -> {affixes}
	 * @param limit
	 */
	private void removeLongAffixes(
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes, int limit) {
		for (SetOfMorphemes<Affix> affixSet : stemToAffixes.values()) {
			Iterator<Affix> it = affixSet.iterator();
			while (it.hasNext()) {
				if (it.next().length() > limit) {
					it.remove();
				}
			}
		}
	}

	protected Map<Context, Affix> getStemAffixPairsUsingASingleBoundary(
			String type, 
			boolean getPrefixes, 
			boolean getSuffixes,
			boolean allowEmptyStems) {
		
		Map<Context, Affix> allPairs = new HashMap<Context, Affix>();
		
		String initialContext = null;
		String finalContext   = null;
		
		Context context;
		Affix affix;
		
		for (int indexIntoType = 0; 
			 indexIntoType <= type.length(); 
			 indexIntoType++) {
			
			initialContext = type.substring(0, indexIntoType);
			finalContext   = type.substring(indexIntoType, type.length());
			
			
			// Magically, since Prefixes are not distinguished from Suffixes,
			// if 'get_prefixes' and 'get_suffixes' are both true, it does
			// not matter that '|abcde' will generate the same Context-Affix
			// pair using Suffixes that 'abcde|' will generate using
			// Prefixes (namely, (_, abcde) OR (*null*, abcde)).  The 
			// "Suffix" version will just overwrite the identical "Prefix"
			// version in 'all_pairs'.
			
			if (getPrefixes) {
				
				if ( ! allowEmptyStems) {
					if (finalContext.equals("")) {
						continue;
					}
				}
				
				context  = new Context("", finalContext);
				affix = new Affix(initialContext);        
				allPairs.put(context, affix);
			}
			
			if (getSuffixes) {
				
				if ( ! allowEmptyStems) {
					if (initialContext.equals("")) {
						continue;
					}
				}
				
				context  = new Context(initialContext, "");
				affix = new Affix(finalContext);
				allPairs.put(context, affix);
			}
			
		}
			
		return allPairs;
	}
	
	/*
	 * a b c d e		context			affix
	 * ---------------------------------------
	 * ||a b c d e		_abcde			*null*	
	 * |a|b c d e		_bcde			a
	 * |a b|c d e		_cde			ab
	 * |a b c|d e		_de				abc
	 * |a b c d|e		_e				abcd
	 * |a b c d e|		*null* OR _	OR simply not generated	abcde   <------- NOTE THIS LINE
	 * ----
	 * a||b c d e		a_bcde			*null*
	 * a|b|c d e		a_cde			b
	 * a|b c|d e		a_de			bc
	 * a|b c d|e		a_e				bcd
	 * a|b c d e|		a_				bcde
	 * ----
	 * a b||c d e		ab_cde			*null*
	 * a b|c|d e		ab_de			c
	 * a b|c d|e		ab_e			cd
	 * a b|c d e|		ab_				cde
	 * -----
	 * a b c||d e		abc_de			*null*
	 * a b c|d|e		abc_e			d
	 * a b c|d e|		abc_			de
	 * ----
	 * a b c d||e		abcd_e			*null*
	 * a b c d|e|		abcd_			e
	 * ----
	 * a b c d e||		abcde_			*null*
	 */
	protected Map<Context, Affix> getStemAffixPairsUsingTwoBoundaries(String type, boolean allowEmptyStems) {
		
	    Map<Context, Affix> allPairs = new HashMap<Context, Affix>();
	    
	    String initialContext, slotFiller, finalContext;
	    
	    Context context;
	    Affix affix;
	    
	    for (int typeIndex1 = 0; typeIndex1 <= type.length(); typeIndex1++) {
	    	for (int typeIndex2 = typeIndex1; typeIndex2 <= type.length(); typeIndex2++) {
	    		
	    		initialContext = type.substring(0,            typeIndex1);
	    		slotFiller     = type.substring(typeIndex1, typeIndex2);
	    		finalContext   = type.substring(typeIndex2, type.length());
	    		
	    		// Getting the right Context is conceptually a little tricky due to initial and final 
	    		// strings of length 0.  Please see extensive example above.
	    		// But all this trickyness is handled in the Context class and here
	    		// everything is very simple.
	    		
	    		// Do not generate empty stems if they are not allowed
				if ( ! allowEmptyStems) {
					if (initialContext.equals("") && finalContext.equals("")) {
						continue;
					}
				}
	    		
	    		context = new Context(initialContext, finalContext);
	    		affix = new Affix(slotFiller);
	    		
	    		allPairs.put(context, affix);
	    	}
	    }
	    
	    return allPairs;
	}


	
	private void computeLevel1Schemes() {
		System.err.println();
		System.err.println();
		System.err.println("Computing the Level 1 Schemes. There are " + 
							mostSpecificSchemes.size() + " most specific " +
							"schemes to push down to level 1");
		System.err.println("----------------------------------------------------------------");
		
	    Map<Affix, Level1Scheme> affixToLevel1Schemes 
	    	= new HashMap<Affix, Level1Scheme>();
	    
	    int mostSpecificSchemeCounter = 0;
	    System.err.println("  ");
	    // Run through all the mostSpecificSchemes passed in
	    for (Scheme mostSpecificScheme : mostSpecificSchemes) {
	    	mostSpecificSchemeCounter++;
	    	if ((mostSpecificSchemeCounter % 10000) == 0) {
	    		System.err.println("  " + mostSpecificSchemeCounter + " of " + 
	    						   mostSpecificSchemes.size() + " most specific schemes");
	    	}
	    	
	        SetOfMorphemes<Affix> affixes       = mostSpecificScheme.getAffixes();
	        SetOfMorphemes<Context>  inherentStems = mostSpecificScheme.getContexts();
	        
	        // Run through each affix in the current mostSpecificScheme
	        for (Affix affix : affixes) {
	        	Level1Scheme level1Scheme;
	            // If a Level1Scheme has already been created that corresponds to affix,
	            // then get that Level1Scheme so we can modify it.
	            if (affixToLevel1Schemes.containsKey(affix)) {
	                level1Scheme = affixToLevel1Schemes.get(affix);
	                
	            // If a level 1 scheme that corresponds to affix has not yet been created,
	            // create it.
	            } else {
	            	level1Scheme = new Level1Scheme(affix);
	            }
	            
	            // add to stems not to inherentStems
	            level1Scheme.addToContexts(inherentStems);
	            
	            // And put level1Scheme into level1Schemes if it isn't already there
	            affixToLevel1Schemes.put(affix, level1Scheme);
	        }
	    }
	    
	    level1Schemes = new HashSet<Level1Scheme>(affixToLevel1Schemes.values());
	    
	    System.err.println();
	    System.err.println("  " + level1Schemes.size() + " Level 1 Schemes occur");
	}

	/**
	 * Returns a data structure that looks like:
	 * 
	 * Map<Suffix, Set<MostSpecificScheme>>
	 *
	 */
	Map<Affix, Set<Scheme>> getAffixToContainingMostSpecificSchemes() {
		
		System.err.println("Entering getAffixToContainingMostSpecificSchemes");
		
		Map<Affix, 
		  	Set<Scheme>> affixToContainingMostSpecificSchemes = 
		  		new THashMap<Affix,
		  					Set<Scheme>>();
		
		Set<Integer> levels = mostSpecificSchemesByLevel.keySet(); 
		
		// for each level starting small
		for (Integer level : levels) {
			
			System.err.println("  Level " + level);
			
			
			if (level < 2) {
				continue;  // two affixes cannot co-occur in a scheme that has less than 2 affixes!
			}
			
			Set<Scheme> mostSpecificSchemesAtLevel = mostSpecificSchemesByLevel.get(level);
			
			System.err.println("    There are " + mostSpecificSchemesAtLevel.size() + " most specific schemes at level " + level);
			
			// for each scheme in the current level
			for (Scheme mostSpecificScheme : mostSpecificSchemesAtLevel) {
				SetOfMorphemes<Affix> affixes = mostSpecificScheme.getAffixes();
				for (Affix affix : affixes) {
					if ( ! affixToContainingMostSpecificSchemes.containsKey(affix)) {

						affixToContainingMostSpecificSchemes.
							put(affix, new THashSet<Scheme>(1));
					}
					affixToContainingMostSpecificSchemes.
						get(affix).add(mostSpecificScheme);
				}
			}
		}
		
		System.err.println("Leaving getSuffixToContainingMostSpecificSchemes");
		
		return affixToContainingMostSpecificSchemes;
	}
	

	public SchemeSet<Level1Scheme> getLevel1Schemes() {
		return new SchemeSet<Level1Scheme>(level1Schemes);
	}
	
	// I just trust that no evil person maliciously changes this 'SchemeShell's
	// level1SchemesByAffix.
	public Map<Affix, Level1Scheme> getLevel1SchemesByAffix() {
		return level1SchemesByAffix;
	}

	public List<Integer> getLevelsInShell() {
		return new ArrayList<Integer>(mostSpecificSchemesByLevel.keySet());
	}

	Set<Scheme> getMostSpecificSchemesAtLevel(Integer level) {
		return mostSpecificSchemesByLevel.get(level);
	}

	Set<Scheme> getMostSpecificSchemes() {
		return mostSpecificSchemes;
	}

	public Scheme getAMostSpecificScheme(SetOfMorphemes<Affix> name) {
		return mostSpecificSchemesByName.get(name);
	}
	
	/**
	 * Returns <code>null</code> if <code>name</code> does not consist of exactly one <code>Suffix</code>.
	 */	
	public Level1Scheme getALevel1Scheme(SetOfMorphemes<Affix> name) {
		if (name.size() != 1) {
			return null;
		}
		return getALevel1Scheme(name.iterator().next());
	}

	public Level1Scheme getALevel1Scheme(Affix affix) {
		return level1SchemesByAffix.get(affix);
	}
}
