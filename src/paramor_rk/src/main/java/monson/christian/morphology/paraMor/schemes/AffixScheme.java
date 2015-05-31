/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.schemes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import klic.radoslav.functional.FuncUtil;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;

/**
 * @author cmonson
 * 
 */
/*
 * The original intended use of AffixScheme is to represent a scheme in an
 * intersection closure for which we have not yet computed its stems.
 */
public class AffixScheme implements Comparable<AffixScheme>, Serializable {

	private static final long serialVersionUID = 1L;

	protected SetOfMorphemes<Affix> affixes;

	/**
	 * CAUTION: If you know (approximately) how many Affixes you plan on placing
	 * in this AffixScheme, it can save a significant amount of space or time
	 * (depending on the implementation) to use
	 * <code>AffixScheme(int likelyCountOfAffixes)</code>
	 */
	public AffixScheme() {
		// Create an empty AffixScheme
		affixes = new SetOfMorphemes<Affix>();
	}

	public AffixScheme(int likelyCountOfAffixes) {
		affixes = new SetOfMorphemes<Affix>(likelyCountOfAffixes);
	}

	/**
	 * Assumes that a good estimate for the total number of Affixes that you
	 * will ever add to this AffixScheme is just the count of the
	 * <code>affixes</code> that are passed in.
	 */
	public AffixScheme(Affix... affixes) {
		this.affixes = new SetOfMorphemes<Affix>(affixes);
	}

	/**
	 * @param affixes
	 *            The set of Affixes that define this AffixScheme
	 * 
	 *            Shallow assignment of this.affixes to be <code>affixes</code>
	 */
	public AffixScheme(SetOfMorphemes<Affix> affixes) {
		this.affixes = affixes;
	}

	public AffixScheme(AffixScheme scheme) {
		affixes = new SetOfMorphemes<Affix>(scheme.affixes);
	}

	/**
	 * Assumes that a good estimate for the total number of Affixes that you
	 * will ever add to this AffixScheme is just the count of the
	 * <code>affixes</code> that are passed in.
	 */
	public void addToAffixes(Affix... affixes) {
		this.affixes.add(affixes);
	}

	/**
	 * CAUTION: To save space or time (depending on the implementation), it is
	 * recommended to add affixes only when you have initialized an AffixScheme
	 * with the likely number of affixes you intend to place in it.
	 */
	protected void addToAffixes(SetOfMorphemes<Affix> affixes) {
		this.affixes.add(affixes);
	}

	public SetOfMorphemes<Affix> getAffixes() {
		return affixes;
	}

	public int level() {
		return affixes.size();
	}

	public AffixScheme intersect(AffixScheme scheme) {
		SetOfMorphemes<Affix> intersectedAffixes = affixes
				.intersect(scheme.affixes);
		return new AffixScheme(intersectedAffixes);
	}

	public boolean isAffixSubsetOf(AffixScheme that) {
		return that.affixes.containsAll(this.affixes);
	}

	public String affixSignature() {
		List<Affix> affixList = new ArrayList<Affix>(this.getAffixes().getCopyOfMorphemes());
		List<String> affixStrList = FuncUtil.transform(affixList, Affix::toString);
		
		Collections.sort(affixStrList);
		return StringUtil.join(",", affixStrList);
	}

	@Override
	public String toString() {
		String toReturn = "";

		toReturn += String.format("(affixes      [%5d] = ", affixes.size());
		toReturn += affixes;
		toReturn += String.format(")");

		return toReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof AffixScheme) {
			AffixScheme that = (AffixScheme) o;
			return this.affixes.equals(that.affixes);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return affixes.hashCode();
	}

	/**
	 * Every AffixScheme (which includes the sub-classes of AffixScheme:
	 * MostSpecificScheme, Scheme, and Level1Scheme) are equal if simply the set
	 * of affixes in that AffixScheme are equal.
	 * 
	 * @param that
	 * @return
	 */
	public int compareTo(AffixScheme that) {
		return this.affixes.compareTo(that.affixes);
	}

	/**
	 * For each unique affix-initial character <code>c</code> in the affixes of
	 * <code>this AffixScheme</code> the right FSA affix set is that set of
	 * <code>Affixes</code> formed by stripping off <code>c</code> from the
	 * front of all the affixes that begin with <code>c</code> in
	 * <code>this AffixScheme</code>.
	 * 
	 * For example if the set of affixes is: {a, apb, bc} then the result of
	 * this function is: a -> {NULL, pb} b -> {c}
	 * 
	 * And continuing from (NULL, ab) we only get ONE entry in the returned Map
	 * 
	 * p -> (b)
	 * 
	 * In other words the presence of a NULL affix is equivalent to stating that
	 * the current scheme is an end state in the rightward FSA. This also means
	 * you cannot rely on getRightFSAAffixSets() to tell if right links are
	 * unambiguous--the scheme (NULL, pb) is NOT UNambiguous and yet there is
	 * only one link in the returned link Map. Use isRightFSAUnambiguous() for
	 * this.
	 * 
	 * @return a <code>Map</code> from the character stripped from the front of
	 *         a subset of the current set of affixes in this scheme to the the
	 *         new set of affixes formed by stripping that character.
	 * 
	 * @param typeClassOfAffixScheme
	 *            Since this method must create new instances of <code>A</code>,
	 *            we need to know what <code>A</code> is at runtime.
	 * 
	 * @see Scheme.getLeftTrieAffixSets()
	 */
	// TODO: Explain why is this called FSA but the other function is called
	// getLeftTrie...()
	public Map<Character, SetOfMorphemes<Affix>> getRightFSAAffixSets() {

		Map<Character, SetOfMorphemes<Affix>> rightFSAAffixSets = new TreeMap<Character, SetOfMorphemes<Affix>>();

		for (Affix oldAffix : affixes) {
			Character affixLeadCharacter = oldAffix.getLeadMorphemeCharacter();

			// A 'oldAffix' which returns a 'null' lead character is
			// the end of the line, there is no rightward finite state
			// link to follow.
			if (affixLeadCharacter == null) {
				continue;
			}

			Affix newAffix = oldAffix.createAffixByStrippingLeadChar();

			assert newAffix != null : "Should never reach here as newAffix should only be null if "
					+ "affixLeadCharacter is null";

			if (!rightFSAAffixSets.containsKey(affixLeadCharacter)) {
				rightFSAAffixSets.put(affixLeadCharacter,
						new SetOfMorphemes<Affix>(affixes.size()));
			}

			rightFSAAffixSets.get(affixLeadCharacter).add(newAffix);
		}

		return rightFSAAffixSets;
	}

	/**
	 * @return <code>true</code> if there is exactly one non-null character that
	 *         all affixes in this <code>AffixScheme</code> begin with--being
	 *         careful of <code>NullAffix</code>es which return
	 *         <code>NullCharacter</code>s as thier leading characters.
	 */
	public boolean isRightFSAUnambiguous() {

		// There is NO continuation if there are no affixes!
		if (affixes.size() == 0) {
			return false;
		}

		// initialize to null to appease the compiler,
		// but really leadChar is initialized on the
		// first time through the loop below.
		Character leadChar = null;
		boolean firstAffix = true;

		for (Affix affix : affixes) {

			// Returns 'null' if the lead MorphemeElement in affix is a
			// MorphemeElement.Null
			Character currentLeadChar = affix.getLeadMorphemeCharacter();

			// The RightFSA will ALWAYS be ambiguous or zero (just as bad)
			// if it contains a Null Affix. If the only affix in this
			// AffixScheme is a Null Affix then this Scheme is a dead state
			// with no continuations in the rightward FSA. If a Null Affix
			// is only one among many Affixes in this Scheme then the rightward
			// FSA is still ambiguous because we don't know whether we should
			// stop at this scheme or continue onward
			if (currentLeadChar == null) {
				return false;
			}

			if (firstAffix) {
				firstAffix = false;
				leadChar = currentLeadChar;
				continue;
			}

			if (!currentLeadChar.equals(leadChar)) {
				return false;
			}
		}

		return true;
	}
}
