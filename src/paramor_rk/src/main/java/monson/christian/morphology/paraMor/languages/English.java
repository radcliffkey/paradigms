/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.languages;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * @author cmonson
 *
 */
public class English extends Language<English.EnglishSubClassName> {

	private static final long serialVersionUID = 1L;

	
	private static final String stringOfAllWordChars = 
		"'aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
		
	
	/*
	 * A regex character class of characters that mark token boundaries.
	 */
	private final String tokenBoundaryRegex = "[\\s]+";
	
	/**
	 * Create an instance of the Spanish Language class.
	 */ 
	public English() {
		
		languageName = LanguageName.ENGLISH;
		
		subClasses = new EnumMap<EnglishSubClassName, 
								 SetOfMorphemes<Affix>>(EnglishSubClassName.class);
		
		subClasses.put(EnglishSubClassName.VERBS, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
							   null, "s", "ed", "ing"));
		
		subClasses.put(EnglishSubClassName.VERBS_SILENT_E,
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
							   "e", "es", "ed", "ing"));
		
		subClasses.put(EnglishSubClassName.NOUNS_NULL_S, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "s"));
		
		subClasses.put(EnglishSubClassName.NOUNS_NULL_ES, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "es"));
		
		subClasses.put(EnglishSubClassName.ADJECTIVES, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   null, "er", "est"));

	}

	// This is the old way to tokenize.  But this leaves punctuation in and stuff
	// so I have now moved the tokenize method up to the Language class, and each
	// individual langugage merely needs to specify a list of valid characters
	// for that language.
	@Deprecated
	public List<String> tokenizeOnWhiteSpace(String line) {
		String[] tokensArray = line.split(tokenBoundaryRegex);
		List<String> tokens = new ArrayList<String>();
		for (String token : tokensArray) {
			tokens.add(token);
		}
		return tokens;
	}
	
	@Override
	protected String getStringOfAllWordCharacters() {
		return stringOfAllWordChars;
	}
	
	//public enum SpanishSubClassNames {
	enum EnglishSubClassName implements Language.SubClassName {

		/**
		 * 2 Major inflection classes for verbs
		 */
		VERBS("Verbs: Regular"),
		VERBS_SILENT_E("Verbs: Silent e"),
		
		/**
		 * {Null, s} Nouns
		 */
		NOUNS_NULL_S("Nouns: " + (new Affix("")).toString() + " & s"),
		
		/**
		 * {Null, es} Nouns
		 */
		NOUNS_NULL_ES("Nouns: " + (new Affix("")).toString() + " & es"),

		/**
		 * Adjectives: *null*, er, est
		 */
		ADJECTIVES("Adjectives");
		
		
		private String prettyName;
		
		private EnglishSubClassName(String prettyName) {
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
	}

	@Override
	public EnglishSubClassName[] getSubClasses() {
		return EnglishSubClassName.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		return subClasses.get(subClass);
	}


	

}
