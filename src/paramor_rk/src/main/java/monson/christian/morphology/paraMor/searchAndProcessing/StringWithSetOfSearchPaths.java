/**
 * 
 */
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.morphemes.Analysis;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.schemes.Scheme;

// TODO: make package visible once this class has been moved out of the networks package
public class StringWithSetOfSearchPaths implements Comparable<StringWithSetOfSearchPaths>, 
														  Serializable {
	private static final long serialVersionUID = 1L;
	
	String string;
	Map<Context, Set<SearchPath>> searchPathsByStem = 
		new TreeMap<Context, Set<SearchPath>>();
	
	// once the 'string' field is set there is no way to change it
	public StringWithSetOfSearchPaths(String string) {
		this.string = string;
	}
	
	public int compareTo(StringWithSetOfSearchPaths that) {
		if (this.searchPathsByStem.size() < that.searchPathsByStem.size()) {
			return -1;
		}
		if (this.searchPathsByStem.size() > that.searchPathsByStem.size()) {
			return 1;
		}
		
		return this.string.compareTo(that.string);
	}
	
	public void addPath(SearchPath path) {
		Scheme terminalString = path.getTerminalScheme();
		Analysis analysis = terminalString.analyze(string);
		Context context = analysis.getStem();
		if ( ! searchPathsByStem.containsKey(context)) {
			searchPathsByStem.put(context, new TreeSet<SearchPath>());
		}
		Set<SearchPath> searchPaths = searchPathsByStem.get(context);
		searchPaths.add(path);
	}
	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append(String.format("--------------------------%n"));
		toReturn.append(" " + string);
		toReturn.append(String.format("%n"));
		toReturn.append(String.format("--------------------------%n%n"));
		
		for(Context context : searchPathsByStem.keySet()) {
			
			toReturn.append("  " + context);
			toReturn.append(String.format("%n"));
			toReturn.append(String.format("  ============%n%n"));
			
			Set<SearchPath> paths = searchPathsByStem.get(context);
			
			for (SearchPath path : paths) {
				toReturn.append(path);
				toReturn.append(String.format("%n%n"));
			}
		}
		
		toReturn.append(String.format("%n"));
		
		return toReturn.toString();
	}
	
	Map<Context, Set<SearchPath>> getSearchPathsByStem() {
		return searchPathsByStem;
	}
	
	String getString() {
		return string;
	}
	
	void remove(SearchPath searchPath) {
		Iterator<Context> stemIter = searchPathsByStem.keySet().iterator();
		while (stemIter.hasNext()) {
			Context context = stemIter.next();
			
			Set<SearchPath> searchPaths = searchPathsByStem.get(context);
			Set<SearchPath> copyOfSearchPaths = new TreeSet<SearchPath>(searchPaths);
			
			// iterate over the copy
			for (SearchPath searchPathToExamine : copyOfSearchPaths) {
				if (searchPathToExamine.equals(searchPath)) {
					
					// remove from the original
					searchPaths.remove(searchPathToExamine);
				}
			}
			if (searchPaths.size() == 0) {
				stemIter.remove();  // safely remove while iterating
			}
		}
	}
}