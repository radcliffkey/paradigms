/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.morphemes;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import monson.christian.statistics.ListOfData;


/**
 * An implementation of a set of Stems or a set of Affixes.  This class implements 
 * connonical toString(), and equals(), and compareTo() methods for a set of Stems, Affixes etc. 
 * in the context of morphology induction using morphology scheme networks.
 *
 */
//                                                     The type parameter of AbstractMorpheme is for Comparable
//                                                     and so 'super' is appropriate.  See Java in a Nutshell.
public class SetOfMorphemes_TreeSetVersion<M extends Morpheme> implements Comparable<SetOfMorphemes_TreeSetVersion<M>>, 
															  	   	  Iterable<M>,
															  	   	  Serializable {
	
	private static final long serialVersionUID = 1L;

	private TreeSet<M> morphemes = new TreeSet<M>();
           
    /**
     * @param morphemes A list of M (subclasses of Morpheme) instances to insert into this
     * SetOfMorphemes
     *
     */
    @SuppressWarnings("unchecked")
	public SetOfMorphemes_TreeSetVersion(M... morphemes) {
        for (M morpheme : morphemes) {
            add(morpheme);
        }
    }
    
    public SetOfMorphemes_TreeSetVersion(Set<M> morphemes) {
    	add(morphemes);
    }
    
    /**
     * Copy constructor.  Places all the Morphemes that are in <code>those</code> into 
     * the new <code>SetOfMorphemes</code>.
     * 
     * Since Morphemes are immutable we don't need to worry about making copies of
     * the individual morphemes in the SetOfMorphemes<M> those.  (In fact we would
     * have trouble creating copies because we can't say 'new M', M is not known 
     * at runtime.)
     */
    public SetOfMorphemes_TreeSetVersion(SetOfMorphemes_TreeSetVersion<M> those) {
        add(those);
    }
    
    /**
     * The funky bounds on the type parameter FM are there to ensure that you only call
     * this method with a <code>morphemeClass</code> that is able to covert a list of Strings 
     * directly into a set of morphemes of <code>morphemeClass</code>.
     * 
     * @param affixClass The Class object representing the subclass of Affix
     * 						over which to build a SetOfMorphemes. 
     * @param affixesAsStrings A list of Strings to turn into type 'affixClass' and add to
     * 						  this SetOfMorphemes
     */
    @SuppressWarnings("unchecked")
	public static <A extends Affix> SetOfMorphemes_TreeSetVersion<A> 
    stringsToSetOfMorphemes(Class<A> affixClass,  String... affixesAsStrings) { 
		
    	SetOfMorphemes_TreeSetVersion<A> toReturn = new SetOfMorphemes_TreeSetVersion<A>();
    	
    	Constructor constructor = null;
		try {
			constructor = affixClass.getConstructor(String.class);
		
		} catch (Exception e) {	e.printStackTrace(); System.exit(0); }
    	
		for (String affixAsString : affixesAsStrings) {
    		A newAffix = null;
			try {
				
				newAffix = affixClass.cast(constructor.newInstance(affixAsString));
				
			} catch (Exception e) {	e.printStackTrace(); System.exit(0); }
    		
			toReturn.add(newAffix);
    	}
		
		return toReturn;
    }
        
    /**
     * @param morpheme A list of Morphemes (or subclasses of Morpheme) to add to this SetOfMorphemes<M>
     */
    public void add(M... morphemes) {
        for (M morpheme : morphemes) {
        	this.morphemes.add(morpheme);
        }
    }
    
    public void add(Set<? extends M> morphemes) {
    	this.morphemes.addAll(morphemes);
    }
    
    // Since Morphemes are immutable we don't need to worry about making copies of
    // the individual morphemes in the SetOfMorphemes<M> those.  (In fact we would
    // have trouble creating copies because we can't say 'new M', M is not known 
    // at runtime.)
    @SuppressWarnings("unchecked")
	public void add(SetOfMorphemes_TreeSetVersion<? extends M> morphemes) {
        for (M morpheme : morphemes) {
            add(morpheme);
        }
    }
    
    /**
     * for each morpheme passed in, if that morpheme is present in this SetOfMorphemes
     * then it is removed from this SetOfMorphemes.
     * 
     * @param morphemes
     */
    public void remove(M... morphemes) {
    	for (M morpheme : morphemes) {
    		this.morphemes.remove(morpheme);
    	}
    }
    
    /**
     * for each morpheme in the passed in collection, if that morpheme is present 
     * in this SetOfMorphemes, then it is removed from this SetOfMorphemes.
     * 
     * @param morphemes
     */
   public void remove(Collection<? extends M> morphemes) {
    	this.morphemes.removeAll(morphemes);
    }
    
    /**
     * @return the number of morphemes in this SetOfMorphemes
     */
    public int size() {
        return morphemes.size();
    }
    
    // Returns null if this SetOfMorphemes contains no morphemes, not even a
    // null morpheme.
	public int getMinimumContainedLength() {
		Integer minimumLength = null;
		for (M morpheme : morphemes) {
			if ((minimumLength == null) || (morpheme.length() < minimumLength)) {
				minimumLength = morpheme.length();
			}
		}
		return minimumLength;
	}

	// Returns null if this SetOfMorphemes contains no morphemes, not even a 
	// null morpheme.
	public Double getAverageContainedLength() {
		Double averageLength = null;
		for (M morpheme : morphemes) {
			if (averageLength == null) {
				averageLength = (double) morpheme.length();
			} else {
				averageLength += morpheme.length();
			}
		}
		if (averageLength != null) {
			averageLength /= morphemes.size();
		}
		return averageLength;
	}
    
	// Returns null if this SetOfMorphemes contains no morphemes, not even a 
	// null morpheme.
	public Double getMedianContainedLength() {
		ListOfData<Integer> lengths = new ListOfData<Integer>();
		for (M morpheme : morphemes) {
			lengths.add(morpheme.length());
		}

		return lengths.getMedian();
	}

	/**
     * @param that A SetOfMorphemes&lt;M&gt; to intersect with <code>this</code>.
     * @return A brand new SetOfMorphemes&lt;M&gt; that contains the set of M's that
     *         are the intersection of <code>this</code> and <code>that</code>.
     * @see intersectInPlace()
     */
    public SetOfMorphemes_TreeSetVersion<M> intersect(SetOfMorphemes_TreeSetVersion<M> that) {
        SetOfMorphemes_TreeSetVersion<M> smaller, larger;
        SetOfMorphemes_TreeSetVersion<M> toReturn;
        if (this.size() < that.size()) {
            smaller = this;
            larger  = that;
        } else {
            smaller = that;
            larger  = this;
        }
        
        toReturn = new SetOfMorphemes_TreeSetVersion<M>(smaller);
        toReturn.morphemes.retainAll(larger.morphemes);
        
        return toReturn;
    }
    
    /**
     * Directly changes <code>this</code> to contain only M's that are in both 
     * <code>this</code> and <code>that</code>.
     * 
     * @param that A SetOfMorphemes&lt;M&gt; to intersect with <code>this</code>.
     * @see intersect()
     */
    public void intersectInPlace(SetOfMorphemes_TreeSetVersion<M> that) {
    	morphemes.retainAll(that.morphemes);
    }
    
    public boolean containsAll(M... those) {
    	for (M that : those) {
    		if ( ! morphemes.contains(that)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean containsAll(SetOfMorphemes_TreeSetVersion<M> that) {
    	return morphemes.containsAll(that.morphemes);
    }
    
    /**
     * @param that A SetOfMorphemes&lt;M&gt; to subtract from <code>this</code>.
     * @return A brand new SetOfMorphemes&lt;M&gt; that contains the set of M's that
     *         <b>are</b> in <code>this</code> but <b>not</b> in <code>that</code>.
     */
    public SetOfMorphemes_TreeSetVersion<M> minus(SetOfMorphemes_TreeSetVersion<M> that) {
    	SetOfMorphemes_TreeSetVersion<M> toReturn = new SetOfMorphemes_TreeSetVersion<M>(this);
    	if (that != null) {
    		toReturn.morphemes.removeAll(that.morphemes);
    	}
    	return toReturn;
    }
	
	// Don't let the outside world change the morphemes in this SetOfMorphemes
	public TreeSet<M> getCopyOfMorphemes() {
		return new TreeSet<M>(morphemes);
	}
    
	public String toShortString(int maxMorphemes) {
        StringBuilder asShortStringBuilder = new StringBuilder("(");
        int countOfMorphemes = 0;
        for (M morpheme : morphemes) {
        	if (countOfMorphemes < maxMorphemes) {
        		countOfMorphemes++;
        		if (asShortStringBuilder.length() == 1) {
        			asShortStringBuilder.append(morpheme.toString());
        		} else {
        			asShortStringBuilder.append(" " + morpheme.toString());
        		}
        	} else {
        		break;
        	}
        }

        // You get one for free because it takes just as much space to print "..."
        // as it does to print the last morpheme itself.
        if (size() == (maxMorphemes +1)) {
        	asShortStringBuilder.append(" " + morphemes.last());
        } else if (size() > (maxMorphemes+1)) {
        	asShortStringBuilder.append(" ...");
        }
        asShortStringBuilder.append(")");
        
        return asShortStringBuilder.toString();
	}
    
    /**
     * @return A String representation of this SetOfMorphemes
     */
	@Override
	public String toString() {
        
        // Using a StringBuilder **significantly** speeds up creating the String
        // representation of very large SetOfMorphemes
        StringBuilder asStringBuilder = new StringBuilder("(");
        for (M morpheme : morphemes) {
            if (asStringBuilder.length() == 1) {
            	asStringBuilder.append(morpheme);
            } else {
                asStringBuilder.append(" ").append(morpheme);
            }
        }
        asStringBuilder.append(")");
        
        // Set the cache
        String asString = asStringBuilder.toString();
        
        return asString;
	}
    
    /**
     * The hashCode of a SetOfMorphemes is based on the String representation of the
     * SetOfMorphemes.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return morphemes.hashCode();
    }
    
    /**
     * @param o An object to compare for equality with this SetOfMorphemes
     * @return true if o is a SetOfMorphemes and if this SetOfMorphemes and o
     *              contain the same Morphemes according to the appropriate
     *              Morphemes.equals() method.
     * <p> Note: Two SetOfMorpheme instances are NOT equal just because their
     * String representations are equal.  In fact for REALLY BIG SetOfMorphemes
     * just creating the String representation can be 
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;  // Identical references are equal
        }
        if ( ! (o instanceof SetOfMorphemes_TreeSetVersion)) {
            return false; // SetOfMorpheme's are only equal to other SetOfMorpheme instances
        }
        SetOfMorphemes_TreeSetVersion that = (SetOfMorphemes_TreeSetVersion) o;  // Cast to a SetOfMorphemes
        
        // The SetOfMorpheme's are only equal if they have the same number of elements
        if (this.size() != that.size()) {
            return false;
        }
        
        // Since they are the same size, 'this' equals 'that' if 'this' contains all the
        // elements that 'that' contains.  I'm pretty sure that containsAll() uses equals()
        // and not '==' to check equality (the Java spec says that contains() does use
        // equals().)
        return this.morphemes.containsAll(that.morphemes);
    }
    
    /**
     * Compares 2 SetOfMorpheme instances.  A SetOfMorphemes, A, is less than a
     * SetOfMorphemes, B, if while iterating through A and B according to the natural order,
     * i.e. compareTo() method, of Morphemes:
     * <li> For the first morpheme, m_A, in A that is not equal to the corrosponding
     *      morpheme, m_B, in B, m_A &lt; m_B, OR
     * <li> The SetOfMorphemes A is a prefix of the setOfMorphemes B
     * <p> Note: Two SetOfMorpheme instances are NOT compared on the basis of their
     *           String representations, but rather by the natural order defined on Morphemes.
     * @param that A SetOfMorpheme instance to compare this SetOfMorpheme instance to
     * @return     <code>     -1</code> if this SetOfMorphemes is less than setOfMorphemes.
     *         <br><code>&nbsp;0</code> if this.equals(setOfMorphemes)
     *         <br><code>&nbsp;1</code> if this SetOfMorphemes is greater that setOfMorphemes
     */
    public int compareTo(SetOfMorphemes_TreeSetVersion<M> that) {
        if (this.equals(that)) {
            return 0;
        }
        
        Iterator<M> thisSetIterator = this.iterator();
        Iterator<M> thatSetIterator = that.iterator();
        while (thisSetIterator.hasNext() && thatSetIterator.hasNext()) {
        	// The type M is restricted to be a descendent of Morpheme,
        	// so the iterators will actually return Morphemes.
            M thisMorpheme = thisSetIterator.next();
            M thatMorpheme = thatSetIterator.next();
            
            int thisToThat = thisMorpheme.compareTo(thatMorpheme);
            
            if (thisToThat < 0) {
                return -1;
            }
            if (thisToThat > 0) {
                return 1;
            }
        }
        
        // 'this' and 'that' have the same initial sequence of Morphemes
        if (thatSetIterator.hasNext()) {
            return -1;  // 'this' must not have a next, so 'this' is shorter so 'this' < 'that'
        }
        if (thisSetIterator.hasNext()) {
            return 1;   // 'that' must not have a next, so 'that' is shorter so 'this' > 'that'
        }
        
        // 'this' and 'that' have the exact same sequence of Morphemes
        return 0;  // Should never get here as this is redundant with the first 'if' statement
        // in this function, but to satisfy the compiler I include this here
    }
    
    /**
     * Returns an iterator over the Morphemes in this SetOfMorphemes.  The elements
     * are returned in their natural order.
     */
    public Iterator<M> iterator() {
        return morphemes.iterator();
    }
        
}

