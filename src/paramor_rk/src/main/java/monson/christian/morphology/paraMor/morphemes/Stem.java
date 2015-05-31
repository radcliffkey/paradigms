/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;



public class Stem extends ConcatenativeMorpheme implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String NULL_STEM_STRING_REPRESENTATION = "*null*";

	public Stem(String stem) {
		super(stem);
	}
	
	public int compareTo(Morpheme morpheme) {
		
		if ( ! (morpheme instanceof Stem)) {  
			super.compareTo(morpheme);
		}
		
		Stem that = (Stem)morpheme;
		
		return this.morphemeString.compareTo(that.morphemeString);
	} 
	
	@Override
	public String toString() {
		if (morphemeString.length() == 0) {
			return NULL_STEM_STRING_REPRESENTATION;
		}
		
		return morphemeString.toString();
	}

	@Override
	public String toStringForSegmentation() {
		return toString();
	}
}
