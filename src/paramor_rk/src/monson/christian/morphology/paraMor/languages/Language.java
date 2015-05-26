/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.languages;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * The class Language is somewhat unusual and serves two different functions.  First,
 * Language defines the enumerated type LanguageName which defines what kinds of Languages
 * are valid (i.e. have been defined.)  LanguageName knows how to convert human readable
 * language names into a type safe enum value that represents that language.  Any
 * piece of knowledge that has to do with the naming of languages is handled by 
 * LanguageName.  The second function that Language serves is as a super class of all 
 * defined languages.  But it is in the sub-classed individual languages that language 
 * specific data and methods are defined.  For example, a Language subclass, such as 
 * English or Spanish, defines how to tokenize a String from that language, and also 
 * defines the set of inflection sub-classes of that language.
 * 
 * @author Christian Monson
 */
// NetBeans REALLY doesn't like '<K extends Enum<K> & Language.SubClassNames>', and since this fancy
// parameter restriction is just in there to be tricky, the way to fix NetBeans is to comment out
// '& Language.SubClassNames'.
public abstract class Language<K extends Enum<K> & Language.SubClassName> implements Serializable {
	
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = -4272707746392456218L;


	protected LanguageName languageName;
	
	protected EnumMap<K, SetOfMorphemes<Affix>> subClasses;
	 
	protected interface SubClassName {
		// Empty marker interface to force subClasses to only contain keys that
		// implement this interface.
	}

	/**
	 * Different Languages may tokenize text in different languages.  Extreme cases
	 * occur with Asian languages which tokenize not according to spaces and
	 * punctuation.  But even languages with a western-based script may have
	 * slightly different rules on how to tokenize text, for example, are hyphens,
	 * '-' characters, valid word boundaries?  Additionally, the tokenization task
	 * needs to be customized to the end goal.  To analyze the morphological 
	 * structure of words we probably want to exclude tokens containing digits
	 * because the rules to form numbers are different from the rules to form
	 * natural language words (i.e. any digit can follow any other digit.)
	 * 
	 * But for all the languages I anticipate working with, it is fairly simple
	 * to enumerate all the valid characters of that language.  And then I can
	 * simply throw out any characters that are NOT valid language characters.
	 * In a word like "asdf." the '.' is stripped off the "asdf".
	 * In a word like asdf45qwer, I throw out the "45" and leave behind two
	 * separate strings "asdf" and "qwer" (all this assuming 'a', 's', 'd', 'f',
	 * 'q', 'w', 'e', and 'r' are all valid language characters.)
	 * 
	 * @param line A line of text presumeably straight from a raw corpus of text.
	 * @return An ArrayList of tokens found in the line.
	 */
	public List<String> tokenize(String line, boolean stripSGML) {
			
		if (stripSGML) {
			line = line.replaceAll("<.*>", " ");
		}
		
		// No longer used (unless for backwards compatibility
		//
		// I just don't want to bother with specifying exactly what the word
		// characters are for each new language. I end up missing some and
		// there are always unexpected characters in foreign words, etc.
		//
		if (languageName != LanguageName.GENERIC) {
			String allWordCharacters = getStringOfAllWordCharacters();
		
			String regexToMatchStringsOf_Non_WordChars = "([^" + allWordCharacters + "]+)";		// Replace any sequence of non-word characters with a space

			line = line.replaceAll(regexToMatchStringsOf_Non_WordChars, " ");
		}
		
		// Now tokenize on whitespace
		String[] tokensArray = line.split("[\\s]+");
		
		List<String> tokens = new ArrayList<String>();
		for (String token : tokensArray) {
			// NO EMPTY WORDS IN THE CORPUS!!!!!!
			if ( ! token.equals("")) {
				tokens.add(token);
			}
		}
				
		return tokens;
	}
	
	protected abstract String getStringOfAllWordCharacters();

	/**
	 * @param language A human readable language name that this Language instance will
	 *                 represent.
	 * @return A new instance of the subclass of Language that corresponds to the human
	 *         readable language string passed in.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public static Language getNewLanguage(String language) {
		LanguageName newLanguage = LanguageName.toLanguageName(language);
		
		return getNewLanguage(newLanguage);
	}
	
	
	/*
	 * Only functions that directly deal with the names of Languages go in the 
	 * LanguageName enum.  
	 */
	public static Language getNewLanguage(LanguageName language) {
		
		switch(language) {
		
		case ARABIC_SUFFIXES: return new ArabicSuffixes();
		
		case ENGLISH: return new English();
		
		case SPANISH: return new Spanish();
		
		case GERMAN: return new German();
		
		case TURKISH: return new Turkish();
		
		case GENERIC: return new GenericLanguage();
		
		default: throw new IllegalArgumentException("The language: " + language 
													+ ", is not a valid language.");
		}
	}


	/**
	 * An embellished enumerated type.  Each value that a LanguageName can be
	 * has a pretty String name associated with it.  See Java in a Nutshell 5.0, the
	 * section on enumerated types.
	 */
	public enum LanguageName {
		ENGLISH("English"),
		HUNGARIAN("Hungarian"),
		SPANISH("Spanish"),
		ARABIC_SUFFIXES("Arabic Suffixes"),
		GERMAN("German"),
		TURKISH("Turkish"),
		GENERIC("GENERIC");
		
		private String prettyName;
		
		private LanguageName(String prettyName) {
			this.prettyName = prettyName;
		}
		
		/**
		 * Converts a LanguageName instance to a pretty String name for that instance
		 * @return A human readable name for this language
		 */
		@Override
		public String toString() {
			return prettyName;
		}
		
		/**
		 * Converts a string representation of a language into
		 * the appropriate enumerated type LanguageName instance
		 * 
		 * @param languageName A human readable language name
		 * 
		 * @return The Language.LanguageName inferred from the String representation
		 */
		public static LanguageName toLanguageName(String languageName) {
			String lcLanguageName = languageName.toLowerCase();
			
			// Returns a instance of each possible LanguageName
			// (So we don't need to change this function at all when
			// adding a new name to LanguageName)
			final LanguageName[] allNames = LanguageName.values();
			
			for (LanguageName name : allNames) {
				String prettyName = name.prettyName;
				String lcPrettyName = prettyName.toLowerCase();
				if (lcLanguageName.matches(lcPrettyName)) {
					return name;
				}
			}
			
			// We only get here if the passed in string did not match
			// any valid language name
			throw new IllegalArgumentException(languageName + " Is not a valid language.  "
					                           + "Valid languages are: " 
					                           + LanguageName.getPrettyStringOfValidLanguages());		
		}
		
		/**
		 * @return A nicely formatted String of all the human readable names of all the
		 *         defined LanguageNames.
		 */
		public static String getPrettyStringOfValidLanguages() {
			final LanguageName[] allNames = LanguageName.values();
			
			String prettyNames = "";
			
			for (int allNamesIndex = 0; allNamesIndex < allNames.length; allNamesIndex++) {
				LanguageName name = allNames[allNamesIndex];
				prettyNames += name.toString();
				if (allNamesIndex < allNames.length -1) {
					prettyNames += ", ";
				}
			}
			
			return prettyNames;
		}
		
		/**
		 * Print to System.out the defined possible languages.  Along with a helpful
		 * message stating that these are the valid languages.
		 */
		public static void printValidLanguages() {
			System.out.println();
			System.out.println("The recognized and valid languages are:");
			System.out.println();
			System.out.println(getPrettyStringOfValidLanguages());
			System.out.println();
		}
	} // End of LanguageName

	
	/**
	 * @return All the affixes that occur in some true sub-class of this <code>Language</code>.
	 *         In the returned <code>SetOfMorphemes&ltAffix&gt</code>, each <code>Affix</code> occurs 
	 *         exactly once, even if the same <code>Affix</code> occurs in more than one
	 *         sub-class (or occurs more than once in the same sub-class.)
	 */
	// The cached set of all affixes
	private SetOfMorphemes<Affix> allAffixes = null;
	public SetOfMorphemes<Affix> getAllAffixes() {
		// Return cached set if available
		if (allAffixes != null) {
			return allAffixes;
		}
		
		allAffixes = new SetOfMorphemes<Affix>();
		for (SetOfMorphemes<Affix> subClass : subClasses.values()) {
			allAffixes.add(subClass);
		}
		return allAffixes;
	}

	/**
	 * @return true if some true sub-class of this <code>Language</code> contains all the
	 *         <code>Affix</code>'s in <code>affixes</code>.
	 */
	public boolean doesASubClassContain(SetOfMorphemes<Affix> affixes) {
		for(SetOfMorphemes<Affix> subClass : subClasses.values()) {
			if (subClass.containsAll(affixes)) {
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * @return <code>true</code> if some true sub-class of this <code>Language</code> 
	 * 			contains all the <code>Affix</code>'s in <code>affixes</code>.
	 */
	public boolean doesASubClassContain(Affix... affixes) {
		return doesASubClassContain(new SetOfMorphemes<Affix>(affixes));
	}
	
	/*
	Unfortunately this version of isIncorrectSegmentation() did not work.  Because
	NullAffix is an incorrect segmentation of EVERYTHING it works out that every possible
	affix and even every possible set of affixes returns true--if for no other reason
	than the NULL.s subClass!
	
	So, an algorithm that I think will work, and that actually captures the idea of an
	incorrect segmentation a bit better, is the following:
		
	For each sub-class find the set of incorrect right-segmentations such as:
	a.as.o.os -> NULL.s -> NULL
	OR
	o.as.a.amos.an.�.aste.�.aron.aba.abas.abamos.aban.ar�.ar�s.ar�.aremos.ar�n ->
	NULL.s.mos.n.ste.ron.ba.bas.bamos.ban.r�.r�s.r�.remos.r�n ->
	NULL.os.te.on.a.as.amos.an.�.�s.emos.�n ->
	s.e.n.NULL.mos ->
	NULL.os ->
	s ->
	NULL
	
	And if 'affixes' is a subset of any such incorrect right-segmentation of any sub-class
	then return true.
	
	Then look for incorrect left-segmentations by removing one letter at a time from the
	left edge of each affix in 'affixes' and seeing if that is a subset of any true
	sub-class.
	*/
	public boolean isIncorrectSegmentation_approximateByCharStipping(SetOfMorphemes<Affix> affixes) {
		
		// 1) Check if the passed in affixes are a subset of any incorrect
		//    right-segmentation of any true sub-class.
		
		computeSubClassIncorrectRightSegmentations();
		
		for (SetOfMorphemes<Affix> incorrectSegmentationOfASubclass : 
			 subClassIncorrectRightSegmentations) {

			if (incorrectSegmentationOfASubclass.containsAll(affixes)) {
				return true;
			}
		}
		
		
		// 2) Check if some right-segmentation of affixes is a subset of a
		//      true sub-class.
		//    Careful because the right-segmentation of affixes that is just
		//      the set consisting of the NULL morphemes is a subset of some
		//      sub-class in most languages--hence we only allow/consider
		//      right segmentations of the passed in affixes that consist of
		//      more than just a single NULL morpheme
		//    Also a set of affixes is an incorrect left segmentation of
		//      of some true sub-class only if each affix in the set of affixes
		//      has a substring in the true sub-class that it maps to and if
		//      no affix in the sub-class has more than 1 affix in the set
		//      that maps to it--see examples below.		
		SetOfMorphemes<Affix> currentSegmentationOfAffixes = affixes;
		while (true) {
			
			SetOfMorphemes<Affix> nextRightSegmentation = 
				getImmediateRightSegmentation(currentSegmentationOfAffixes);
			
			// No fair if some of the affixes have been reduced to not even the
			// NULL morpheme, as in blah.NULL -> lah
			// OR if two affixes collapsed onto a single affix as in ba.ca -> a
			if (nextRightSegmentation.size() != currentSegmentationOfAffixes.size()) {
				return false;
			}
			
			if ((nextRightSegmentation.size() == 1) &&
				(nextRightSegmentation.containsAll(new Affix("")))) {
				
				return false;
				
			} else {
				if (doesASubClassContain(nextRightSegmentation)) {
					return true;
				}
				
				currentSegmentationOfAffixes = nextRightSegmentation;
			}
		}
		
	}
	
	// Cached set of incorrect right-segmentations of the true sub-classes
	Set<SetOfMorphemes<Affix>> subClassIncorrectRightSegmentations = null;
	private void computeSubClassIncorrectRightSegmentations() {
		if (subClassIncorrectRightSegmentations != null) {
			return;
		}
		
		subClassIncorrectRightSegmentations = new TreeSet<SetOfMorphemes<Affix>>();
		
		for (SetOfMorphemes<Affix> subClass : subClasses.values()) {
			Set<SetOfMorphemes<Affix>> incorrectRightSegmentationsForThisSubClass =
				getAllRightSegmentations(subClass);
			
			subClassIncorrectRightSegmentations.addAll(incorrectRightSegmentationsForThisSubClass);
		}
	}

	// A Right-Segmentation is not a correct segmentation but rather a segmentation taking
	// morpheme boundaries to the right of the current choice.
	private Set<SetOfMorphemes<Affix>> getAllRightSegmentations(SetOfMorphemes<Affix> affixes) {
		Set<SetOfMorphemes<Affix>> allRightSegmentations = new TreeSet<SetOfMorphemes<Affix>>();
		
		boolean continueLookingForRightSegmentations = true;
		SetOfMorphemes<Affix> currentSegmentationOfAffixes = affixes;
		while (continueLookingForRightSegmentations) {
			SetOfMorphemes<Affix> nextRightSegmentation = 
				getImmediateRightSegmentation(currentSegmentationOfAffixes);
			
			if (nextRightSegmentation != null) {
				allRightSegmentations.add(nextRightSegmentation);
				currentSegmentationOfAffixes = nextRightSegmentation;
			} else {
				continueLookingForRightSegmentations = false;
			}
		}
		
		return allRightSegmentations;
	}

	/**
	 * @param affixes
	 * @return
	 */
	private SetOfMorphemes<Affix> getImmediateRightSegmentation(SetOfMorphemes<Affix> affixes) {
		SetOfMorphemes<Affix> immediateRightSegmentation = new SetOfMorphemes<Affix>(affixes.size());
		
		for (Affix affix : affixes) {
			Affix affixStrippedOfLeadChar = affix.createAffixByStrippingLeadChar();
			if (affixStrippedOfLeadChar != null) {
				immediateRightSegmentation.add(affixStrippedOfLeadChar);
			}
		}
		
		if (immediateRightSegmentation.size() == 0) {
			return null;
		}
		
		return immediateRightSegmentation;
	}


	
	public boolean isIncorrectSegmentation(Affix...affixes) {
		return isIncorrectSegmentation_approximateByCharStipping(new SetOfMorphemes<Affix>(affixes));
	}
	
	// This method just returns K.values(), where K is replaced by the actual type of K in
	// the subclass of Language
	abstract public K[] getSubClasses();

	// We need to verify that the person is calling this method with a 'subClass' that
	// is an instance of 'K', but we can't do that until we know what 'K' is at the
	// subclass of Language.
	abstract public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass);
	
	
	@Override
	public String toString() {
		return languageName.toString();
	}

}
