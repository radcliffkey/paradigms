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
public class Hungarian extends Language<Hungarian.Hungarian_SubClass_Name> {

	private static final long serialVersionUID = 1L;
	
	private static final String stringOfAllWordChars = 
		"aA��bBcCdDeE��fFgGhHiI��jJkKlLmMnNoO����??pPqQrRsStTuU����??vVwWxXyYzZ";

	
	/**
	 * Create an instance of the Spanish Language class.
	 */ 
	public Hungarian() {
		
		languageName = LanguageName.HUNGARIAN;
		
		subClasses = new EnumMap<Hungarian_SubClass_Name, 
								 SetOfMorphemes<Affix>>(Hungarian_SubClass_Name.class);
		
		// Schema for Verbs
		//
		// Present, Indicative
		// Indefinite object:
		//          1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl, 1.sg>2
		// Def obj: 1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl
		// 
		// Past, Indicative
		// indefObj:1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl, 1.sg>2
		// Def obj: 1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl
		//
		// Subjunctive/Imperitive
		// ...
		// ...
		// 
		// Conditional
		// ...
		// ...
		//
		//
		// Nearly all suffixes have two forms depending on the vowel harmony
		// of the stem: a back vowel form, a front vowel form, and a front
		// rounded vowel form.
		//
		// Present Indicative, Past Indicative, and Subjunctive/Imperitive all
		// have phonological inflection classes that split the basic verbal
		// inflection class. Here is the basic bifurcations that need to
		// take place for each verbal tense/mood/person/number/definiteness
		// ending:
		//
		// Present Indicative:
		//
		// 3: Vowel Harmony:            Back, Front, Front Rounded
		// 2: Sibilant Final Consonant: +, -
		// 2: -ik verbs:                +, -
		//
		// Past:
		//
		// 3: Vowel Harmony:            Back, Front, Front Rounded
		// 3: Epenthetic [o/e/�]tt      none, 3.sg.Indef only, always
		// 
		// Imperitive/Subjunctive:
		//
		// 3: Vowel Harmony:            Back, Front, Front Rounded
		// ?: Final Consonant           Regular, Vshort+t, Vlong+t, Sibilant s, Sibilant sz
		//
		// Conditional:
		//
		// 2: Vowel Harmony:            Just Back and Front
		//
		//
		// Now some of these subclasses are subsets of one another. If they were all orthogonal
		// then we would have to multiply all these together to get the cross-product for
		// the exact number of possible (phonologically induced) inflection classes.  I haven't
		// figured out exactly the number of inflection classes, but there are at least ~30 classes.
		// Which is a horrible mess.

		
		
		subClasses.put(Hungarian_SubClass_Name.VERBS_BACK,      
					SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
					
					"ok", "sz",   null,  "unk",  "tok",   "nak",   "lak",
					"om", "od",   "ja",  "juk",  "j�tok", "j�k",
					   
					"tam", "t�l", "t",   "tunk", "tatok", "tanak", "talak",
					"tam", "tad", "ta",  "tuk",  "t�tok", "t�k",
					   
					"jak", "j�l", "jon", "junk", "jatok", "janak", "jalak",
					"jam", "jad", "ja",  "juk",  "j�tok", "j�k",
					   
					"n�k", "n�l", "na",  "n�nk", "n�tok", "n�nak", "n�lak",
					"n�m", "n�d", "n�",  "n�nk", "n�tok", "n�k"));
				
		subClasses.put(Hungarian_SubClass_Name.VERBS_FRONT,      
				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
				
				   "ek", "sz",   null, "�nk",  "tek",    "nek",   "lek",
				   "em", "ed",   "i",  "j�k",  "itek",   "ik",
				   
				   "tem", "t�l", "t",   "t�nk", "tetek", "tenek", "telek",
				   "tem", "ted", "te",  "t�k",  "t�tek", "t�k",
				   
				   "jek", "j�l", "jen", "j�nk", "jetek", "jenek", "jelek",
				   "jem", "jed", "je",  "j�k",  "j�tek", "j�k",
				   
				   "n�k", "n�l", "ne",  "n�nk", "n�tek", "n�nek", "n�lek",
				   "n�m", "n�d", "n�",  "n�nk", "n�tok", "n�k"));
		
		subClasses.put(Hungarian_SubClass_Name.VERBS_FRONT_ROUND,      
				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
				
				   "�k", "sz",   null, "�nk",  "t�k",    "nek",   "lek",
				   "�m", "�d",   "i",  "j�k",  "itok",   "ik",
				   
				   "tem", "t�l", "t",   "t�nk", "tetek", "tenek", "telek",
				   "tem", "ted", "te",  "t�k",  "t�tek", "t�k",
				   
				   "jek", "j�l", "jen", "j�nk", "jetek", "jenek", "jelek",
				   "jem", "jed", "je",  "j�k",  "j�tek", "j�k",
				   
				   "n�k", "n�l", "ne",  "n�nk", "n�tek", "n�nek", "n�lek",
				   "n�m", "n�d", "n�",  "n�nk", "n�tok", "n�k"));
		
		subClasses.put(Hungarian_SubClass_Name.NOUNS_BACK,
				   SetOfMorphemes.stringsToSetOfMorphemes(Affix.class,
						   
				   "ok",                // pl
				   "ot",   "val",       // accusative, instrumental
				   "hoz", "n�l", "t�l", // toward, at/by, (away)from
				   "ba",  "ban", "b�l", // into, in, out of
				   "ra",  "n",   "r�l", // onto, on, off of
				   
				   // possession
				   // sg possessed (i.e. one object being possessed (my table -> asztal-om)
				   // pl possessed (i.e. more than one object being possessed (my tables -> asztal-aim)
				   "om",  "od",  "ja", "unk",  "tok",   "juk",   // sg possessed: 1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl
				   "aim", "aid", "ai", "aink", "aitok", "aik")); // pl possessed: 1.sg, 2.sg, 3.sg, 1.pl, 2.pl, 3.pl
	}

	enum Hungarian_SubClass_Name implements Language.SubClassName {

		VERBS_BACK("Verbs Back"),
		VERBS_FRONT("Verbs Front"),
		VERBS_FRONT_ROUND("Verbs Front Rounded"),
		
		NOUNS_BACK("Nouns Back"),
		NOUNS_FRONT("Nouns Front"),
		NOUNS_FRONT_ROUND("Nouns Front Rounded"),
		
		ADJ_BACK("Adjectives Back"),
		ADJ_FRONT("Adjectives Front");

		
		private String prettyName;
		
		private Hungarian_SubClass_Name(String prettyName) {
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
	public Hungarian_SubClass_Name[] getSubClasses() {
		return Hungarian_SubClass_Name.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		if ( ! (subClass instanceof Hungarian_SubClass_Name)) {
					return null;
		}
		return subClasses.get(subClass);
	}

	@Override
	protected String getStringOfAllWordCharacters() {
		return stringOfAllWordChars;
	}
	

}
