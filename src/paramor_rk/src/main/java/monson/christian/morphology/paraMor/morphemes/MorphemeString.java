/**
 * 
 */
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;

class MorphemeString implements Comparable<MorphemeString>, Serializable {

	private static final long serialVersionUID = 1L;

	// Because Morpheme_String is such a primitive type (in the sense of heavily relied on) 
	// an ArrayList<Morpheme_Character> is just too slow
	private char[] morphemeChars = new char[0];
	private int buildingMorphemeCharactersIndex = 0;
	private int hashCode = 0;
	
	private void addToMorphemeChars(char morphemeChar) {
		morphemeChars[buildingMorphemeCharactersIndex] = morphemeChar;
		buildingMorphemeCharactersIndex++;
	}
	
	private void finishOutInitialization() {
		for (char morphemeChar : morphemeChars) {
			
			// This is (basically) the hashCode value that java.util.List uses.
			// since Morpheme_Strings are immutable we can calculate the hashCode once.
			hashCode = (31 * hashCode) + morphemeChar;
		}
	}
	
	private MorphemeString(char[] morphemeChars) {
		// Don't accidentally assign 'null' to morpheme_characters
		if (morphemeChars != null) {
			this.morphemeChars = morphemeChars;
		}
		finishOutInitialization();
	}
	
	// Creates a MorphemeString that looks like:
	//
	// for a *null* morpheme string segment pass in "" or <code>null</code> for one of the Strings
	//
	MorphemeString(String string) {
		
		if (string != null) {
			morphemeChars = new char[string.length()];
			concatString(string);
		}
		
		finishOutInitialization();			
	}

	// Converts a String into a sequence of MorphemeElement.MorphemeCharacters
	// and concatenates them onto 'morphemeElement'.
	private void concatString(String string) {
		char[] chars = null;
		if (string != null) {
			chars = string.toCharArray();
		}
		
		if ((chars == null) || (chars.length == 0)) {
			return;
		}
		
		for (char aChar : chars) {
			addToMorphemeChars(aChar);
		}
	}

	/**
	 * @Return the length of this morpheme, where length
	 * is counted as the number MorphemeElement.MorphemeCharacters in this
	 * MorphemeString.
	 * */

	int length() {
		return morphemeChars.length;
	}
	
	/**
	 * Compares <code>this</code> MorphemeString to 'that' MorphemeString lexicographically 
	 * according to the lexicographic ordering of MorphemeElements.
	 * 
	 * @param that
	 * @return
	 */
	public int compareTo(MorphemeString that) {
		if (this == that) {
			return 0;
		}
		
		int morphemeCharIndex = 0;
		while((morphemeCharIndex < this.morphemeChars.length) && 
			  (morphemeCharIndex < that.morphemeChars.length)) {
			
			char thisMorphemeChar = 
				this.morphemeChars[morphemeCharIndex];
			char thatMorphemeChar = 
				that.morphemeChars[morphemeCharIndex];
			
			if (thisMorphemeChar < thatMorphemeChar) {
				return -1;
			}
			if (thisMorphemeChar > thatMorphemeChar) {
				return 1;
			}
		    
		    morphemeCharIndex++;
		}
		
		// 'this' is an initial substring of 'that' ('this' is smaller than 'that')
		// There are still more morphemeElements in 'that' (so there must be no more left in 'this')
		if (morphemeCharIndex < that.morphemeChars.length) {
			return -1;
		}
		
		// 'that' is an initial substring of 'this' ('that' is smaller than 'this')
		// There are still more morphemeElements in 'this' (so there must be no more left in 'that')
		if (morphemeCharIndex < this.morphemeChars.length) {
			return 1;
		}
	
		// it is also possible for there to be no more morphemeElements left in 'this' or in 'that'
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( ! (o instanceof MorphemeString)) {
			return false;
		}
		
		MorphemeString that = (MorphemeString)o;
		
		int thisToThat = compareTo(that);
		
		if (thisToThat == 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (char morphemeChar : morphemeChars) {
			toReturn.append(morphemeChar);
		}
		return toReturn.toString();
	}

	
	// Returns 'null' if this Morpheme_String is empty
	Character getLeadChar() {
		
		if (morphemeChars.length == 0) {
			return null;
		}
		
		char leadMorphemeChar = morphemeChars[0];
		
		return leadMorphemeChar;
	}
	
	// Returns 'null' if this Morpheme_String is empty
	Character getFinalChar() {
		
		if (morphemeChars.length == 0) {
			return null;
		}

		int indexOfLastMorphemeChar = morphemeChars.length - 1;
		
		char finalMorphemeChar = morphemeChars[indexOfLastMorphemeChar];
					
		return finalMorphemeChar;
	}


	MorphemeString createMorphemeStringByStrippingLeadChar() {

		if (morphemeChars.length == 0) {
			return null;
		}
								
		int newMorphemeCharactersLength = morphemeChars.length -1;
		
		
		char[] morphemeCharactersStrippedOfLeadingChar = 
			new char[newMorphemeCharactersLength];
		
		for (int newMorphemeElementsIndex = 0; 
			 newMorphemeElementsIndex < newMorphemeCharactersLength; 
			 newMorphemeElementsIndex++) {
			
			int oldMorphemeElementsIndex = newMorphemeElementsIndex + 1;
			
			morphemeCharactersStrippedOfLeadingChar[newMorphemeElementsIndex] = 
				morphemeChars[oldMorphemeElementsIndex];
		}
		
		
		MorphemeString toReturn = new MorphemeString(morphemeCharactersStrippedOfLeadingChar);
		 
		return toReturn;
	}

	
	MorphemeString createMorphemeStringByAddingALeadChar(char charToAdd) {
					
		char[] newMorphemeChars = null;
		
			int newMorphemeCharsLength = morphemeChars.length + 1;
			newMorphemeChars = new char[newMorphemeCharsLength];
			
			newMorphemeChars[0] = charToAdd;
			
			for (int newMorphemeElementsIndex = 1; 
				 newMorphemeElementsIndex < newMorphemeCharsLength; 
				 newMorphemeElementsIndex++) {
				
				int oldMorphemeElementsIndex = newMorphemeElementsIndex - 1;
				
				newMorphemeChars[newMorphemeElementsIndex] = 
					morphemeChars[oldMorphemeElementsIndex];
			}
		
				
		MorphemeString toReturn = new MorphemeString(newMorphemeChars);
		 
		return toReturn;
	}
}