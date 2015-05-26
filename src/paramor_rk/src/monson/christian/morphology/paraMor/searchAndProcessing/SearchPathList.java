/**
 * 
 */
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.ArrayList;
import java.util.Collection;

import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.morphology.paraMor.schemes.SchemeList;

public class SearchPathList extends ArrayList<SearchPath> {

	private static final long serialVersionUID = 1L;
	
	public SearchPathList() {}
	
	public SearchPathList(Collection<? extends SearchPath> collection) {
		super(collection);
	}
	
	public SearchPathList(int initialCapacity) {
		super(initialCapacity);
	}
	
	SchemeList<Scheme> getTerminalSchemes() {
		SchemeList<Scheme> terminalSchemes = new SchemeList<Scheme>();
		for (SearchPath path : this) {
			terminalSchemes.add(path.getTerminalScheme());
		}
		return terminalSchemes;
	}
}