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
public class GenericLanguage extends Language<GenericLanguage.GenericLanguageSubClassName> {

	private static final long serialVersionUID = 1L;

	// The 'German' that Morphology Challenge 2007 uses does not use any
	// special characters, like ü or ö or the eszet (big B thing that is a double ss)
	// instead these characters are replace with 'ue', 'oe', 'ss', etc.
	//
	// NOTE: apostrophe (') is sometimes used in Turkish for the glottle stop of
	// Arabic loan words. And (') *does* occur in the Morpho Challenge 2007 
	// training data (although (') does not occur in any of the words that Kemal
	// Oflazer analyzed for me with his morphological analyzer (this threw me for
	// a loop GAAARRR!) (similarly for 'q' and 'x'.))
	private static final String stringOfAllWordChars = 
		"ThisShouldBeIgnored";
		
		
	/**
	 * Create an instance of the German Language class.
	 */ 
	public GenericLanguage() {
		
		languageName = LanguageName.GENERIC;
		
		subClasses = new EnumMap<GenericLanguageSubClassName, 
								 SetOfMorphemes<Affix>>(GenericLanguageSubClassName.class);
		
		subClasses.put(GenericLanguageSubClassName.NO_ACTUAL_VALUES_YET, 
				SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, (String)null));
	}

	@Override
	protected String getStringOfAllWordCharacters() {
		return stringOfAllWordChars;
	}
	
	//public enum SpanishSubClassNames {
	enum GenericLanguageSubClassName implements Language.SubClassName {

		NO_ACTUAL_VALUES_YET("NO ACTUAL VALUES HAVE YET BEEN IMPLEMENTED");
		
		private String prettyName;
		
		private GenericLanguageSubClassName(String prettyName) {
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
	public GenericLanguageSubClassName[] getSubClasses() {
		return GenericLanguageSubClassName.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		if ( ! (subClass instanceof GenericLanguageSubClassName)) {
					return new SetOfMorphemes<Affix>(0);
		}
		return subClasses.get(subClass);
	}


	

}
