/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;



public class Affix extends ConcatenativeMorpheme implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final String NULL_AFFIX_STRING_REPRESENTATION = "*null*";

	public Affix(String affix) {
		super(affix);
	}
	
	private Affix(MorphemeString morphemeString) {
		super(morphemeString);
	}
		
	public Affix createAffixByStrippingLeadChar() {
		MorphemeString affixStrippedOfLeadMorphemeCharacter = 
			morphemeString.createMorphemeStringByStrippingLeadChar();
		
		if (affixStrippedOfLeadMorphemeCharacter == null) {
			return null;
		}
		return new Affix(affixStrippedOfLeadMorphemeCharacter);
	}

	public Affix createAffixByAddingALeadChar(char morphemeChar) {
		MorphemeString affixAugmentedWithNewLeadMorphemeCharacter = 
			morphemeString.createMorphemeStringByAddingALeadChar(morphemeChar);
		
		return new Affix(affixAugmentedWithNewLeadMorphemeCharacter);
	}

	public int compareTo(Morpheme morpheme) {
		
		if ( ! (morpheme instanceof Affix)) {  
			super.compareTo(morpheme);
		}
		
		Affix that = (Affix)morpheme;
		
		return this.morphemeString.compareTo(that.morphemeString);
	} 
	
	public boolean isNullAffix() {
		if (morphemeString.length() == 0) {
			return true;
		}
		return false;
	}
	
	//String cachedString = null;
	@Override
	public String toString() {
		//if (cachedString != null) {
		//	return cachedString;
		//}
		String toReturn;
		if (morphemeString.length() == 0) {
			toReturn = NULL_AFFIX_STRING_REPRESENTATION;
		} else {
			toReturn = morphemeString.toString();
		}
		
		return toReturn;
	}
	
	@Override
	public String toStringForSegmentation() {
		if (morphemeString.length() == 0) {
			return "+" + NULL_AFFIX_STRING_REPRESENTATION;
		}
		
		return "+" + morphemeString.toString();
	}
}
