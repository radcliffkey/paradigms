/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.languages;

import java.util.EnumMap;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * @author cmonson
 *
 */
public class ArabicSuffixes extends Language<ArabicSuffixes.Arabic_SubClass_Name> {

	private static final long serialVersionUID = 1L;
	
	private static final String stringOfAllWordChars = "\'|OWI}AbptvjHxd*rzs$SDTZEg_fqklmnhwYyFNKaui~o`{PJVG";

	
	/**
	 * Create an instance of the Spanish Language class.
	 */ 
	public ArabicSuffixes() {
		
		languageName = LanguageName.ARABIC_SUFFIXES;
		
		subClasses = new EnumMap<Arabic_SubClass_Name, 
								 SetOfMorphemes<Affix>>(Arabic_SubClass_Name.class);
		
		// There are 14 (10) unique verb suffixes in Arabic when vowels are not written:
		//   NULL, (A,) (An,) n, nA, t, (tA,) tm, (tmA,) tn, wA, wn, y, yn
		//
		// 4 of these only occur in the dual which may not be used hardly at all (Alon):
		//   A, An, tA, tmA
		// 
		// But these suffixes do not all occur with the same prefixes:
		//
		// 1 Suffix occurs with the prefix 'hamza'
		//   NULL
		//
		// 6 (8) Suffixes occur with the prefix 't'
		//   NULL, (A,) (An,) n, wA, wn, y, yn 
		//
		// 4 (6) Suffixes occur with the prefix 'y'
		//   NULL, (A,) (An,) n, wA, wn 
		//
		// 1 Suffix occurs with the prefix 'n'
		//   NULL
		//
		// 7 (10) Suffixes that occur with the prefix 'NULL'
		//   NULL, (A,) n, nA, t, (tA,) tm, (tmA,) tn, wA
		// 
		// Violetta says that dual is productive in Arabic. We'll need to see empirically
		// how frequently it is used.
		subClasses.put(Arabic_SubClass_Name.VERBS_REGULAR,      
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
							   
							// 1     2     2     3     3     2      3     3     1     2     2     3     3  
							// sg    sg    sg    sg    sg    du     du    du    pl    pl    pl    pl    pl
							//       masc  fem   masc  fem          masc  fem         masc  fem   masc  fem
							   
							   //Perfect
							   "t",  "t",  "t",  null, "t",  "tmA", "A",  "tA", "nA", "tm", "tn", "wA", "n",
							   
					   		   // Imperfect Indicative
							   null, null, "yn", null, null, "An",  "An", "An", null, "wn", "n",  "wn", "n",
							   
					   		   // Imperfect Subjunctive
							   null, null, "y",  null, null, "A",   "A",  "A",  null, "wA", "n",  "wA", "n",
							   
					   		   // Imperfect Jussive (same ans Subjunctive when vowels are not written));
							   null, null, "y",  null, null, "A",   "A",  "A",  null, "wA", "n",  "wA", "n"));
				
		
		// Violetta also identified some (I think minor) phonologically induced inflection
		// classes. For now I am not including them in the answer key.
		
		
		// Number/Case endings on nouns. The masculine noun endings are very nearly a subset
		// of the verb endings! The only exception is 'w' by itself. The feminine endings
		// are all unique suffixes--except that 4 of them merely prefix a 't' to a masculine
		// suffix.
		
		subClasses.put(Arabic_SubClass_Name.NOUNS_MASC,
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
							   
							   null,   // singular
							   "An",   // nominative dual
							   "A",    //    "         "   , Noun Noun (iDafa) constructions
							   "wn",   // nominative plural
							   "w",    //    "         "   , Noun Noun (iDafa) constructions
							   "yn",   // genative/accusative dual/plural
							   "y"));  //    "         "        "    "   , Noun Noun (iDafa) constructions
							   
		subClasses.put(Arabic_SubClass_Name.NOUNS_FEM,
					   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
					   
							   "p",    // singular
						   	   "tAn",  // nominative dual
						   	   "tA",   //    "         "   , Noun Noun (iDafa) constructions
						   	   "At",   // plural, all cases
						   	   "tyn",  // genative/accusative dual
						   	   "ty")); //    "         "        " , Noun Noun (iDafa) constructions
		
		
		// Clitics
		//
		// Possessive Pronouns
		//
		subClasses.put(Arabic_SubClass_Name.POSSESSIVE_PRONOUN_CLITICS,
				   	   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
				   
					   "y",    // 1 sg
					   "nA",   // 1 m+f dl+pl
					   "k",    // 2 m+f sg
					   "kmA",  // 2 m+f dl
					   "km",   // 2 m pl
					   "kn",   // 2 f pl
					   "h",    // 3 m sg
					   "hA",   // 3 f sg
					   "hmA", // 3 m+f dl   
					   "hm",   // 3 m pl
					   "hn")); // 3 f pl
		
		// Direct Object Clitics, attached to verb
		//
		// Identical to the possessive pronoun clitics, except that the 1sg clitic
		// is 'ny' and not just 'y' (and native speakers (or at least linguists and
		// grammar books say that 'ny' is really just 'y' with a helper 'n'.
		//
		subClasses.put(Arabic_SubClass_Name.DIRECT_OBJECT_CLITICS,
			   	   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, 
			   
				   "ny",   // 1 sg
				   "nA",   // 1 m+f dl+pl
				   "k",    // 2 m+f sg
				   "kmA",  // 2 m+f dl
				   "km",   // 2 m pl
				   "kn",   // 2 f pl
				   "h",    // 3 m sg
				   "hA",   // 3 f sg
				   "hmA", // 3 m+f dl   
				   "hm",   // 3 m pl
				   "hn")); // 3 f pl
	}

	/** 
	 * @see monson.christian.morphology.paraMor.languages.Language#tokenize(java.lang.String)
	 * 
	 * This tokenizing code separates strings of alpha characters from non-alpha characters
	 * But doesn't throw away the non-alpha characters. The new tokenize method in the 
	 * Language class, actually throws away the (language specific) non-alpha characters, 
	 * which is probably what I want to do.
	 *
	@Override
	public List<String> tokenize(String line) {
		// To separate off punctuation, and bizare Arabic characters including Hindi numbers, etc.
		// Add whitespace around all sequences of NON-BUCKWALTER-XML characters
		String nonBuckwalterXMLRegexString = "([^'|OWI}AbptvjHxd*rzs$SDTZEg_fqklmnhwYyFNKaui~o`{PJVG]+)";
		String replacementString = " $1 ";
		line = line.replaceAll(nonBuckwalterXMLRegexString, replacementString);
		
		// Tokenize Arabic text on whitespace
		String[] tokensArray = line.split("[\\s]+");
		
		List<String> tokens = new ArrayList<String>();
		for (String token : tokensArray) {
			tokens.add(token);
		}
				
		return tokens;
	}
	*/
	
	
	enum Arabic_SubClass_Name implements Language.SubClassName {

		VERBS_REGULAR("regular verbs"),
		
		NOUNS_MASC("masculine nouns"),
		
		NOUNS_FEM("feminine nouns"),
		
		POSSESSIVE_PRONOUN_CLITICS("possessive pronoun clitics"),
		
		DIRECT_OBJECT_CLITICS("direct object clitics");
		
		private String prettyName;
		
		private Arabic_SubClass_Name(String prettyName) {
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
	public Arabic_SubClass_Name[] getSubClasses() {
		return Arabic_SubClass_Name.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		if ( ! (subClass instanceof Arabic_SubClass_Name)) {
					return null;
		}
		return subClasses.get(subClass);
	}

	@Override
	protected String getStringOfAllWordCharacters() {
		return stringOfAllWordChars;
	}
	

}
