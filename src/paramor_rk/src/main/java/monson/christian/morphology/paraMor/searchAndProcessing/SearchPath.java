/**
 * 
 */
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;

public class SearchPath implements NetworkSearchProcedure.Result, 
								   Comparable<SearchPath>, 
								   Serializable,
								   Iterable<Scheme> {
	
	private static final long serialVersionUID = 1L;
	
	List<Scheme> pathToScheme;
	
	public SearchPath() {
		pathToScheme = new ArrayList<Scheme>();
	}
	
	public SearchPath (SearchPath pathToCopy) {
		this.pathToScheme = new ArrayList<Scheme>(pathToCopy.pathToScheme);
	}
	
	public void add(Scheme scheme) {
		pathToScheme.add(scheme);
	}
	
	public Scheme getTerminalScheme() {
		if (pathToScheme.size() > 0) {
			return pathToScheme.get(pathToScheme.size()-1);
		}
		return null;
	}
	
	/**
	 * 
	 * @param n
	 * @return the <code>n</code>th <code>Scheme</code> in this <code>SearchPath</code>.  
	 * 		   For example, <code>getNthScheme(1) will return the 1st <code>Scheme</code>
	 * 		   in the <code>SearchPath</code>.
	 * 		   Returns <code>null</code> if <code>n < 1</code> OR if <code>n > this.length()</code>
	 */
	public Scheme getNthScheme(int n) {
		if (n < 1) {
			return null;
		}
		if (length() < n) {
			return null;
		}
		
		// adjust n to be 0-based
		n--;
		return pathToScheme.get(n);
	}
	
	/* This function assumes that a SearchPath is only bottom up and that we start at
	 * a level 1 scheme.  The idea behind this method is good: namely, sometimes it might be
	 * nice to know what the difference is between successive Schemes in a SearchPath.
	 * But this method isn't quite general enough, and since I never use it anyway yet,
	 * I am commenting it out.
	 */
	/*
	public Affix getNthAddedAffix(int n) {
		if (n < 1) {
			return null;
		}
		if (length() < n) {
			return null;
		}
		
		// adjust n to be 0-based
		n--;
		if (n == 0) {
			return pathToScheme.get(n).getAffixes().iterator().next();
		}
		
		Scheme nthScheme = pathToScheme.get(n);
		Scheme previousScheme = pathToScheme.get(n-1);
		
		SetOfMorphemes<Affix> newAffixAsSet = 
			nthScheme.getAffixes().minus(previousScheme.getAffixes());
		Affix newAffix = newAffixAsSet.iterator().next();
		
		return newAffix;
	}
	*/
	
	public int length() {
		return pathToScheme.size();
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		Scheme previousSchemeInPath = null;
		for (Scheme schemeInPath : pathToScheme) {
			SetOfMorphemes<Affix> previousAffixes = null;
			if (previousSchemeInPath != null) {
				previousAffixes = previousSchemeInPath.getAffixes();
			}
			SetOfMorphemes<Affix> newAffixes = 
				schemeInPath.getAffixes().minus(previousAffixes);
			if (previousSchemeInPath != null) {
				toReturn += " -> ";
			}
			toReturn += newAffixes;
			
			previousSchemeInPath = schemeInPath;
		}
		
		Scheme finalSchemeInPath = getTerminalScheme();
		toReturn += String.format("%n%s", finalSchemeInPath.toPrettyString(20));
		
		return toReturn;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( ! (o instanceof SearchPath)) {
			return false;
		}
		
		SearchPath that = (SearchPath)o;
		
		if (this.compareTo(that) == 0) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return pathToScheme.hashCode();
	}


	// TODO: make this just sort on the affix path -- ignoring schemes altogether
	/**
	 * First compares the terminal schemes in 'this' path and 'that' path if they are
	 * the same then 'this' path and 'that' path are traversed from the beginning and
	 * the first scheme in 'this' path is compared to the first scheme in 'that' path,
	 * the second scheme in 'this' path is compared to the second scheme in 'that' path,
	 * etc. until a difference is found.  If no difference is found then the two paths
	 * are equal.
	 * 
	 * @param that
	 * @return
	 */
	public int compareTo(SearchPath that) {
		
		// Compare path final schemes
		Scheme thisTerminal = this.getTerminalScheme();
		Scheme thatTerminal = that.getTerminalScheme();
		if ( ! thisTerminal.equals(thatTerminal)) {
			return thisTerminal.compareTo(thatTerminal);
		}
		
		// compare corrosponding schemes in the two paths.
		Iterator<Scheme> thisIter = this.pathToScheme.iterator();
		Iterator<Scheme> thatIter = that.pathToScheme.iterator(); 
		while (thisIter.hasNext() && thatIter.hasNext()) {
			Scheme thisScheme = thisIter.next();
			Scheme thatScheme = thatIter.next();
			
			if ( ! thisScheme.equals(thatScheme)) {
				return thisScheme.compareTo(thatScheme);
			}
		}
		
		// The only way to get to this point in this function is for the two paths
		// to look like: A->B->C vs. A->B->C->...->C, where the final
		// schemes are both the same and the paths are the same as far as they go.
		
		// Check for different lengths.
		if (this.pathToScheme.size() < that.pathToScheme.size()) {
			return -1;
		}
		if (this.pathToScheme.size() > that.pathToScheme.size()) {
			return 1;
		}
		
		// Every corrosponding scheme in both paths are the same and the paths
		// are the same length, so the paths are equal.
		return 0;
	}

	public Iterator<Scheme> iterator() {
		return pathToScheme.iterator();
	}
}