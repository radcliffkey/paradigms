/**
 * 
 */
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;

public class MorphemeCharacter implements Comparable<MorphemeCharacter>, Serializable{
	private static final long serialVersionUID = 1L;
	
	// for speed and efficiency, character is a raw char.
	private char character;
	
	// only classes in this package can create MorphemeCharacters (specifically PhonologicalCharacters)
	MorphemeCharacter(char character) {
		this.character = character;
	}
	
	@Override
	public String toString() {
		return String.valueOf(character);
	}

	public int compareTo(MorphemeCharacter that) {
		
		// Remember 'character' is a raw char
		if (this.character < that.character) {
			return -1;
		}
		if (this.character > that.character) {
			return 1;
		}
		return 0;
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;  // Identical references are equal
		}
		if ( ! (o instanceof MorphemeCharacter)) {
			return false; // MorphemeCharacters are only equal to other MorphemeCharacters
		}
		MorphemeCharacter that = (MorphemeCharacter) o;
		
		// '==' is safe because 'character' is a raw char
		return (this.character == that.character);
	}
	
	@Override
	public int hashCode() {
		return character;
	}
}