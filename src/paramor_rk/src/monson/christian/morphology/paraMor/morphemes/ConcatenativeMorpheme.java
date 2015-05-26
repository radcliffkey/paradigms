/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

abstract public class ConcatenativeMorpheme extends Morpheme {
	private static final long serialVersionUID = 1L;

	MorphemeString morphemeString;
	
	protected ConcatenativeMorpheme(String affix) {
		this.morphemeString = new MorphemeString(affix);
	}
	
	protected ConcatenativeMorpheme(MorphemeString morphemeString) {
		this.morphemeString = morphemeString;
	}
		
	public Character getLeadMorphemeCharacter() {
		return morphemeString.getLeadChar();
	}
	
	public String getSurfaceString() {
		return morphemeString.toString();
	}
	
	@Override
	public int length() {
		return morphemeString.length();
	}
	
	abstract public String toStringForSegmentation();

	public int compareTo(Morpheme morpheme) {
		if ( ! (morpheme instanceof ConcatenativeMorpheme)) {
			super.compareTo(morpheme);
		}
		
		ConcatenativeMorpheme that = (ConcatenativeMorpheme)morpheme;
		
		if ((this instanceof Affix) && (that instanceof Stem)) {
			return 1;
		}
		if ((this instanceof Stem) && (that instanceof Affix)) {
			return -1;
		}
		
		throw new MorphemeException("Should never get here. Both 'this' and 'that' are of " +
									"the same type, so a descendent type's compareTo() should" +
									"have been run");
	}
	
	@Override
	public int hashCode() {
		return morphemeString.hashCode();
	}
}
