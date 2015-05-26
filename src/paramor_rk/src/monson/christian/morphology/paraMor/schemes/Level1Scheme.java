/*
 * Level1Scheme.java
 *
 * Created on October 10, 2005, 2:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package monson.christian.morphology.paraMor.schemes;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * A Scheme with the added restriction that there must be exactly one suffix in its suffix set.
 *
 * @author cmonson
 */
public class Level1Scheme extends Scheme {
    
    private static final long serialVersionUID = 1L;

    /**
     * This constructor is safe to call from a space perspective: A Level1Scheme is only allowed
     * to contain a single Affix 
     */
    public Level1Scheme() {
    	// In the case of 
		super(1);
	}

	public Level1Scheme(Affix affix) {
		super(affix);
    }
	
	public Level1Scheme(Affix affix, SetOfMorphemes<Context> contexts) {
		this(affix);
		addToContexts(contexts);
	}
    
    @Override
    public void addToAffixes(Affix... affixes) {
    	for (Affix affix : affixes) {
    		if (this.level() == 0) {
    			super.addToAffixes(affix);
    		} else {
    			throw new LevelOverflowException("Tried to add the affix: " + affix 
    											 + " to a Level1Scheme that already "
    											 + "contains an affix, namely: " 
    											 + this.affixes);
    		}
    	}
    }
    
    public Affix getAffix() {
    	return affixes.iterator().next();
    }
    
    public class LevelOverflowException extends IllegalArgumentException {
		private static final long serialVersionUID = 1L;

		public LevelOverflowException(String s) {
            super(s);
        }
    }
}
