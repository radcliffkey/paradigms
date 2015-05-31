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
public class Spanish extends Language<Spanish.SpanishSubClassName> {

	private static final long serialVersionUID = 1L;

//	private static final String stringOfAllWordChars = 
//    "aAáÁbBcCdDeEéÉfFgGhHiIíÍjJkKlLmMnNñÑoOóÓpPqQrRsStTuUúÚüÜvVwWxXyYzZ";

// Enye bug on Purpose!!!!!!!!!!!!!!1
private static final String stringOfAllWordChars = 
	"aAáÁbBcCdDeEéÉfFgGhHiIíÍjJkKlLmMnNoOóÓpPqQrRsStTuUúÚüÜvVwWxXyYzZ";	
	
	//
	/*
	 * A regex character class of characters that mark token boundaries.
	 */
	private final String tokenBoundaryRegex = "[\\s]+"; 
	
	/**
	 * Create an instance of the Spanish Language class.
	 */ 
	public Spanish() {
		
		languageName = LanguageName.SPANISH;
		
		subClasses = new EnumMap<SpanishSubClassName, 
								 SetOfMorphemes<Affix>>(SpanishSubClassName.class);
		
		subClasses.put(SpanishSubClassName.AR_VERBS,      
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   	"ar", 
							   	"ando",
							   	"ado",  "ados",  "ada",  "adas",
							   	"o",    "as",    "a",    "amos",    "an",
							   	"é",    "aste",  "ó",    "amos",    "aron",
							   	"aba",  "abas",  "aba",  "ábamos",  "aban",
							   	"aré",  "arás",  "ará",  "aremos",  "arán",
							   	"aría", "arías", "aría", "aríamos", "arían",
							   	"e",    "es",    "e",    "emos",    "en",
							   	"ara",  "aras",  "ara",  "áramos",  "aran"));
		
		subClasses.put(SpanishSubClassName.ER_VERBS,      
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   "er",
							   "iendo",
							   "ido",   "idos",  "ida",  "idas",
							   "o",     "es",    "e",    "emos",    "en",
							   "í",     "iste",  "ió",   "imos",    "ieron",
							   "ía",    "ías",   "ía",   "íamos",   "ían",
							   "eré",   "erás",  "erá",  "eremos",  "erán",
							   "ería",  "erías", "ería", "eríamos", "erían",
							   "a",     "as",    "a",    "amos",    "an",
							   "iera",  "ieras", "iera", "iéramos", "ieran"));
		
		subClasses.put(SpanishSubClassName.IR_VERBS,      
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   "ir",
							   "iendo",
							   "ido",   "idos",  "ida",  "idas",
							   "o",     "es",    "e",    "imos",    "en",
							   "í",     "iste",  "ió",   "imos",    "ieron",
							   "ía",    "ías",   "ía",   "íamos",   "ían",
							   "iré",   "irás",  "irá",  "iremos",  "irán",
							   "iría",  "irías", "iría", "iríamos", "irían",
							   "a",     "as",    "a",    "amos",    "an",
							   "iera",  "ieras", "iera", "iéramos", "ieran"));
		
		subClasses.put(SpanishSubClassName.ADJECTIVES,    
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, "a", "as", "o", "os"));
		
		subClasses.put(SpanishSubClassName.NOUNS_NULL_S, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "s"));
		
		subClasses.put(SpanishSubClassName.NOUNS_NULL_ES, 
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "es"));
		
		subClasses.put(SpanishSubClassName.ACC_CLITICS,   
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   				"me", "te", "lo", "la", "nos", "los", "las"));
		
		subClasses.put(SpanishSubClassName.DAT_CLITICS,   
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   				"me", "te", "le",       "nos", "les"));
		
		subClasses.put(SpanishSubClassName.SE_CLITICS,    
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   				"me", "te", "se",       "nos", "se"));
	
		
		/*
		 * To asses how well ParaMor does at finding just the core of Spanish morphology
		 * when the vocabulary is limited, I am assessing ParaMor at identifying
		 * non-finite and 3rd person verb forms, noun paradigms, and the adjective
		 * paradigm.
		 */
//		subClasses.put(SpanishSubClassName.AR_VERBS,      
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   	"ar", 
//						   	"ando",
//						   	"ado",  "ados",  "ada",  "adas",
//						   	/*"o",    "as",   */ "a",    /*"amos",   */ "an",
//						   	/*"�",    "aste", */ "�",    /*"amos",   */ "aron",
//						   	/*"aba",  "abas", */ "aba",  /*"�bamos", */ "aban",
//						   	/*"ar�",  "ar�s", */ "ar�",  /*"aremos", */ "ar�n",
//						   	/*"ar�a", "ar�as",*/ "ar�a", /*"ar�amos",*/ "ar�an",
//						   	/*"e",    "es",   */ "e",    /*"emos",   */ "en",
//						   	/*"ara",  "aras", */ "ara",  /*"�ramos", */ "aran"));
//	
//	subClasses.put(SpanishSubClassName.ER_VERBS,      
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   "er",
//						   "iendo",
//						   "ido",   "idos",  "ida",  "idas",
//						   /*"o",     "es",   */ "e",    /*"emos",   */ "en",
//						   /*"�",     "iste", */ "i�",   /*"imos",   */ "ieron",
//						   /*"�a",    "�as",  */ "�a",   /*"�amos",  */ "�an",
//						   /*"er�",   "er�s", */ "er�",  /*"eremos", */ "er�n",
//						   /*"er�a",  "er�as",*/ "er�a", /*"er�amos",*/ "er�an",
//						   /*"a",     "as",   */ "a",    /*"amos",   */ "an",
//						   /*"iera",  "ieras",*/ "iera", /*"i�ramos",*/ "ieran"));
//	
//	subClasses.put(SpanishSubClassName.IR_VERBS,      
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   "ir",
//						   "iendo",
//						   "ido",   "idos",  "ida",  "idas",
//						   /*"o",     "es",   */ "e",    /*"imos",   */ "en",
//						   /*"�",     "iste", */ "i�",   /*"imos",   */ "ieron",
//						   /*"�a",    "�as",  */ "�a",   /*"�amos",  */ "�an",
//						   /*"ir�",   "ir�s", */ "ir�",  /*"iremos", */ "ir�n",
//						   /*"ir�a",  "ir�as",*/ "ir�a", /*"ir�amos",*/ "ir�an",
//						   /*"a",     "as",   */ "a",    /*"amos",   */ "an",
//						   /*"iera",  "ieras",*/ "iera", /*"i�ramos",*/ "ieran"));
//	
//	subClasses.put(SpanishSubClassName.ADJECTIVES,    
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, "a", "as", "o", "os"));
//	
//	subClasses.put(SpanishSubClassName.NOUNS_NULL_S, 
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "s"));
//	
//	subClasses.put(SpanishSubClassName.NOUNS_NULL_ES, 
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, null, "es"));
///*	
//	subClasses.put(SpanishSubClassName.ACC_CLITICS,   
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   				"me", "te", "lo", "la", "nos", "los", "las"));
//	
//	subClasses.put(SpanishSubClassName.DAT_CLITICS,   
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   				"me", "te", "le",       "nos", "les"));
//	
//	subClasses.put(SpanishSubClassName.SE_CLITICS,    
//				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
//						   				"me", "te", "se",       "nos", "se"));
//*/		
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
	enum SpanishSubClassName implements Language.SubClassName {

		/**
		 * An enum type to represent the 'ar' subclass of Spanish Verbs:
		 */
		AR_VERBS("ar-Verbs"),	
		/**
		 * An enum type to represent the 'er' subclass of Spanish Verbs:
		 */
		ER_VERBS("er-Verbs"),		
		/**
		 * An enum type to represent the 'ir' subclass of Spanish Verbs:
		 */
		IR_VERBS("ir-Verbs"),
		/**
		 * An enum type to represent the major sub-class of Spanish Adjectives:
		 * {a, as, o, os}
		 */
		ADJECTIVES("Adjectives"),
		/**
		 * An enum type to represent the {Null, s} sub-class of Spanish Nouns
		 */
		NOUNS_NULL_S("Nouns: " + (new Affix("")).toString() + " & s"),
		/**
		 * An enum type to represent the {Null, es} sub-calss of Spanish Nouns
		 */
		NOUNS_NULL_ES("Nouns: " + (new Affix("")).toString() + " & es"),
		
		
		/**
		 * An enum type to represent the accusative clitics of Spanish
		 */
		ACC_CLITICS("Acc Clitics"),
		/**
		 * An enum type to represent the dative clitics of Spanish
		 */
		DAT_CLITICS("Dat Clitics"),
		/**
		 * An enum type to represent the 'se' or reflexive clitics of Spanish
		 */
		SE_CLITICS("se-Clitics");

		
		
		private String prettyName;
		
		private SpanishSubClassName(String prettyName) {
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
	public SpanishSubClassName[] getSubClasses() {
		return SpanishSubClassName.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		if ( ! (subClass instanceof SpanishSubClassName)) {
					return null;
		}
		return subClasses.get(subClass);
	}


	

}
