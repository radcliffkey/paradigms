/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;



/**
 * All Morphemes are immutable.  Once you create them their state can never change.
 * 
 * All Morphemes are Comparable to all other Morphemes.
 * 
 * @author cmonson
 * 
 */

public abstract class Morpheme implements Serializable, Comparable<Morpheme> {

	protected static final long serialVersionUID = 1L;

	
	/**
	 * @Return the length of this morpheme, where length
	 * is counted as the number of surface characters in the morpheme.
	 * */

	public abstract int length();
	

	/**
	 * 
	 * @param context 
	 * @param affix 
	 * @return The surface string resulting from combining <code>context</code> and <code>affix</code>
	 */
	public static String computeSurfaceString(Context context, Affix affix) {
		
		String stemInitialString = context.getInitialString();
		String slotFiller         = affix.getSurfaceString();
		String stemFinalString   = context.getFinalString();
		
		String surfaceString = stemInitialString + slotFiller + stemFinalString;
		
		return surfaceString;
	}

	protected static class MorphemeException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		
		public MorphemeException(String message) {
			super(message);
		}
	}
	
	public int compareTo(Morpheme that) {
		// Contexts are less than ConcatenativeMorphemes (Stems and Affixes)
		if ((this instanceof Context) && (that instanceof ConcatenativeMorpheme)) {
			return -1;
		}
		if ((this instanceof ConcatenativeMorpheme) && (that instanceof Context)) {
			return 1;
		}
		
		throw new MorphemeException("Should never get here. Both 'this' and 'that' are of " +
				"the same type, so a descendent type's compareTo() should" +
				"have been run");
	}
	
	@Override
	public boolean equals(Object o) {
		if ( ! (o instanceof Morpheme)) {
			return false;
		}
		
		Morpheme that = (Morpheme)o;
		int thisToThat = this.compareTo(that);
		if (thisToThat == 0) {
			return true;
		}
		return false;
	}
}
