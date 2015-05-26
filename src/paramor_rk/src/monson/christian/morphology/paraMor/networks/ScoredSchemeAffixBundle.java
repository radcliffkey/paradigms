/**
 * 
 */
package monson.christian.morphology.paraMor.networks;

import java.io.Serializable;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.schemes.Scheme;

class ScoredSchemeAffixBundle implements Serializable, Comparable<ScoredSchemeAffixBundle> {
	private static final long serialVersionUID = 1L;
	
	protected Affix affix;
	protected Scheme scheme;
	protected Double score;
	
	public ScoredSchemeAffixBundle(Affix affix, Scheme priorityScheme, Double score) {
		this.affix = affix;
		this.scheme = priorityScheme;
		this.score  = score;
	}

	// Sort on:
	//
	// 1) score
	// 2) then on the lexicographic ordering of affix
	// 3) then on the lexicographic ordering of node 
	public int compareTo(ScoredSchemeAffixBundle that) {
		if (this.score > that.score) {
			return -1;
		}
		if (this.score < that.score) {
			return 1;
		}
		int affixesCompared = this.affix.compareTo(that.affix);
		if (affixesCompared != 0) {
			return affixesCompared;
		}
		return this.scheme.compareTo(that.scheme);
	}

	public Double getScore() {
		return score;
	}

	public Affix getAffix() {
		return affix;
	}

	public Scheme getScheme() {
		return scheme;
	}
}