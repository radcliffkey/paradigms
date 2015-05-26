/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

public class Context extends Morpheme {

	private static final long serialVersionUID = 1L;

	MorphemeString initialContext;
	MorphemeString finalContext;
	
	/*
	 * Creates a Context that looks like:
	 * 
	 * 'initialString slot finalString'
	 * 
	 * if 'initialString' is null or "" then this Context is interpreted as:
	 * 
	 *   'slot finalString'
	 *   
	 * Similarly, if 'finalString' is null or "" then this Context is interpreted as:
	 * 
	 *   'initialString slot'
	 *   
	 * If both 'initialString' and 'finalString' are 'null' or "" then this Context
	 * is interpreted as a null-Context or equivalently, as nothing but a 'slot'
	 */
	public Context(String initialContext, String finalContext) {
		this.initialContext = new MorphemeString(initialContext);
		this.finalContext   = new MorphemeString(finalContext);
	}

	String getInitialString() {
		return initialContext.toString();
	}

	String getFinalString() {
		return finalContext.toString();
	}

	public Character getMorphemeCharacterToLeftOfSlot() {
		return initialContext.getFinalChar();
	}
	
	@Override
	public int length() {
		return initialContext.length() + finalContext.length();
	}
	
	@Override
	public String toString() {
		return initialContext.toString() + "_" + finalContext.toString();
	}
	
	public String toStringAvoidUndescore() {
		if (initialContext.length() > 0 && finalContext.length() == 0) {
			return initialContext.toString();
		}
		
		if (initialContext.length() == 0 && finalContext.length() > 0) {
			return finalContext.toString();
		}
		
		return initialContext.toString() + "_" + finalContext.toString();
	}
	
	@Override
	public int hashCode() {
		return initialContext.hashCode() ^ finalContext.hashCode(); 
	}

	public int compareTo(Morpheme morpheme) {

		if (morpheme instanceof Affix) {
			return -1; // Stems are always less than Affixes
		}
		
		if ( ! (morpheme instanceof Context)) { 
			throw new MorphemeException("I don't know how to compare a Context to the morpheme: " + morpheme);
		}
		
		Context that = (Context)morpheme;
		
		int thisInitialContextToThatInitialContext = 
			this.initialContext.compareTo(that.initialContext);
		
		if (thisInitialContextToThatInitialContext != 0) {
			return thisInitialContextToThatInitialContext;
		}
		
		return this.finalContext.compareTo(that.finalContext);
	}


}
