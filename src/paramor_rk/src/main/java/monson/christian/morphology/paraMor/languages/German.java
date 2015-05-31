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
public class German extends Language<German.GermanSubClassName> {

	private static final long serialVersionUID = 1L;

	// The 'German' that Morphology Challenge 2007 uses does not use any
	// special characters, like ü or ö or the eszet (big B thing that is a double ss)
	// instead these characters are replace with 'ue', 'oe', 'ss', etc.
	private static final String stringOfAllWordChars = 
		"aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
		
	/**
	 * Create an instance of the German Language class.
	 */ 
	public German() {
		
		languageName = LanguageName.GERMAN;
		
		subClasses = new EnumMap<GermanSubClassName, 
								 SetOfMorphemes<Affix>>(GermanSubClassName.class);
		
		subClasses = new EnumMap<GermanSubClassName, 
		 SetOfMorphemes<Affix>>(GermanSubClassName.class);

		subClasses.put(GermanSubClassName.NO_ACTUAL_VALUES_YET, 
				SetOfMorphemes.stringsToSetOfMorphemes(Affix.class, (String)null));
	}

	@Override
	protected String getStringOfAllWordCharacters() {
		return stringOfAllWordChars;
	}
	
	//public enum SpanishSubClassNames {
	enum GermanSubClassName implements Language.SubClassName {

		NO_ACTUAL_VALUES_YET("NO ACTUAL VALUES HAVE YET BEEN IMPLEMENTED");
		
		private String prettyName;
		
		private GermanSubClassName(String prettyName) {
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
	public GermanSubClassName[] getSubClasses() {
		return GermanSubClassName.values();
	}

	@Override
	public SetOfMorphemes<Affix> getAffixesIn(SubClassName subClass) {
		if ( ! (subClass instanceof GermanSubClassName)) {
					return new SetOfMorphemes<Affix>(0);
		}
		return subClasses.get(subClass);
	}


	

}
