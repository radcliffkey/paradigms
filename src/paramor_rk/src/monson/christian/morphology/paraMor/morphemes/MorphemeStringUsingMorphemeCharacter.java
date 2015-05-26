/**
 * 
 */
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;


class MorphemeStringUsingMorphemeCharacter implements Comparable<MorphemeStringUsingMorphemeCharacter>, Serializable {

	private static final long serialVersionUID = 1L;

	// Because Morpheme_String is such a primitive type (in the sense of heavily relied on) 
	// an ArrayList<Morpheme_Character> is just too slow
	private MorphemeCharacter[] morphemeCharacters = new MorphemeCharacter[0];
	private int building_morpheme_characters_index = 0;
	private int hash_code = 0;
	
	private void add_to_morpheme_characters(MorphemeCharacter morpheme_character) {
		morphemeCharacters[building_morpheme_characters_index] = morpheme_character;
		building_morpheme_characters_index++;
	}
	
	private void finish_out_initialization() {
		for (MorphemeCharacter morpheme_character : morphemeCharacters) {
			
			// This is (basically) the hashCode value that java.util.List uses.
			// since Morpheme_Strings are immutable we can calculate the hashCode once.
			hash_code = (31 * hash_code) + morpheme_character.hashCode();
		}
	}
	
	private MorphemeStringUsingMorphemeCharacter(MorphemeCharacter[] morpheme_characters) {
		// Don't accidentally assign 'null' to morpheme_characters
		if (morpheme_characters != null) {
			this.morphemeCharacters = morpheme_characters;
		}
		finish_out_initialization();
	}
	
	// Creates a MorphemeString that looks like:
	//
	// for a *null* morpheme string segment pass in "" or <code>null</code> for one of the Strings
	//
	MorphemeStringUsingMorphemeCharacter(String string) {
		
		if (string != null) {
			morphemeCharacters = new MorphemeCharacter[string.length()];
			concat_string(string);
		}
		
		finish_out_initialization();			
	}

	// Converts a String into a sequence of MorphemeElement.MorphemeCharacters
	// and concatenates them onto 'morphemeElement'.
	private void concat_string(String string) {
		char[] chars = null;
		if (string != null) {
			chars = string.toCharArray();
		}
		
		if ((chars == null) || (chars.length == 0)) {
			return;
		}
		
		for (char a_char : chars) {
			MorphemeCharacter morpheme_character = new MorphemeCharacter(a_char);
			add_to_morpheme_characters(morpheme_character);
		}
	}

	/**
	 * @Return the length of this morpheme, where length
	 * is counted as the number MorphemeElement.MorphemeCharacters in this
	 * MorphemeString.
	 * */

	int length() {
		return morphemeCharacters.length;
	}
	
	/**
	 * Compares <code>this</code> MorphemeString to 'that' MorphemeString lexicographically 
	 * according to the lexicographic ordering of MorphemeElements.
	 * 
	 * @param that
	 * @return
	 */
	public int compareTo(MorphemeStringUsingMorphemeCharacter that) {
		if (this == that) {
			return 0;
		}
		
		int morpheme_character_index = 0;
		while((morpheme_character_index < this.morphemeCharacters.length) && 
			  (morpheme_character_index < that.morphemeCharacters.length)) {
			
			MorphemeCharacter this_morpheme_character = 
				this.morphemeCharacters[morpheme_character_index];
			MorphemeCharacter that_morpheme_character = 
				that.morphemeCharacters[morpheme_character_index];
			
		    int this_morpheme_character_to_that_morpheme_character = 
		    	this_morpheme_character.compareTo(that_morpheme_character);
		    
		    if (this_morpheme_character_to_that_morpheme_character != 0) {
		    	return this_morpheme_character_to_that_morpheme_character;
		    }
		    
		    morpheme_character_index++;
		}
		
		// 'this' is an initial substring of 'that' ('this' is smaller than 'that')
		// There are still more morphemeElements in 'that' (so there must be no more left in 'this')
		if (morpheme_character_index < that.morphemeCharacters.length) {
			return -1;
		}
		
		// 'that' is an initial substring of 'this' ('that' is smaller than 'this')
		// There are still more morphemeElements in 'this' (so there must be no more left in 'that')
		if (morpheme_character_index < this.morphemeCharacters.length) {
			return 1;
		}
	
		// it is also possible for there to be no more morphemeElements left in 'this' or in 'that'
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( ! (o instanceof MorphemeStringUsingMorphemeCharacter)) {
			return false;
		}
		
		MorphemeStringUsingMorphemeCharacter that = (MorphemeStringUsingMorphemeCharacter)o;
		
		int this_to_that = compareTo(that);
		
		if (this_to_that == 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hash_code;
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		for (MorphemeCharacter morphemeCharacter : morphemeCharacters) {
			toReturn.append(morphemeCharacter.toString());
		}
		return toReturn.toString();
	}

	
	// Returns 'null' if this Morpheme_String is empty
	MorphemeCharacter getLeadChar() {
		
		if (morphemeCharacters.length == 0) {
			return null;
		}
		
		MorphemeCharacter lead_morpheme_character = morphemeCharacters[0];
		
		
		return lead_morpheme_character;
	}
	
	// Returns 'null' if this Morpheme_String is empty
	MorphemeCharacter getFinalChar() {
		
		if (morphemeCharacters.length == 0) {
			return null;
		}

		int index_of_last_morpheme_character = morphemeCharacters.length - 1;
		
		MorphemeCharacter final_morpheme_character = 
			morphemeCharacters[index_of_last_morpheme_character];
					
		return final_morpheme_character;
	}


	MorphemeStringUsingMorphemeCharacter createMorphemeStringByStrippingLeadChar() {

		if (morphemeCharacters.length == 0) {
			return null;
		}
								
		int newMorphemeCharactersLength = morphemeCharacters.length -1;
		
		
		MorphemeCharacter[] morphemeCharactersStrippedOfLeadingChar = 
			new MorphemeCharacter[newMorphemeCharactersLength];
		
		for (int newMorphemeElementsIndex = 0; 
			 newMorphemeElementsIndex < newMorphemeCharactersLength; 
			 newMorphemeElementsIndex++) {
			
			int oldMorphemeElementsIndex = newMorphemeElementsIndex + 1;
			
			morphemeCharactersStrippedOfLeadingChar[newMorphemeElementsIndex] = 
				morphemeCharacters[oldMorphemeElementsIndex];
		}
		
		
		MorphemeStringUsingMorphemeCharacter toReturn = new MorphemeStringUsingMorphemeCharacter(morphemeCharactersStrippedOfLeadingChar);
		 
		return toReturn;
	}

	
	MorphemeStringUsingMorphemeCharacter createMorphemeStringByAddingALeadChar(MorphemeCharacter morpheme_character) {
					
		MorphemeCharacter[] new_morpheme_characters = null;
		
			int new_morpheme_characters_length = morphemeCharacters.length + 1;
			new_morpheme_characters = new MorphemeCharacter[new_morpheme_characters_length];
			
			new_morpheme_characters[0] = morpheme_character;
			
			for (int newMorphemeElementsIndex = 1; 
				 newMorphemeElementsIndex < new_morpheme_characters_length; 
				 newMorphemeElementsIndex++) {
				
				int oldMorphemeElementsIndex = newMorphemeElementsIndex - 1;
				
				new_morpheme_characters[newMorphemeElementsIndex] = 
					morphemeCharacters[oldMorphemeElementsIndex];
			}
		
				
		MorphemeStringUsingMorphemeCharacter toReturn = new MorphemeStringUsingMorphemeCharacter(new_morpheme_characters);
		 
		return toReturn;
	}
}