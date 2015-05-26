/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PairList <L extends Comparable<? super L>, 
					   R extends Comparable<? super R>> extends ArrayList<ComparablePair<L, R>> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Unfortunately this method can't be a constructor.  But think of it as a constructor that
	 * takes a Map<L, R> and returns a PairList<L, R>
	 * 
	 * @param map
	 * @return
	 */
	public static 
	<L extends Comparable<? super L>, R extends Comparable<? super R>>
	PairList<L, R> pairListFromMap(Map<L, R> map) {
		
		Set<Map.Entry<L, R>> mapEntries = map.entrySet();
		
		PairList<L, R> pairList = new PairList<L, R>();
		
		for (Map.Entry<L, R> mapEntry : mapEntries) {
			ComparablePair<L, R> pair = new ComparablePair<L, R>(mapEntry.getKey(), mapEntry.getValue());
			pairList.add(pair);
		}
		
		return pairList;
	}
	
	public List<L> lefts() {
		ArrayList<L> lefts = new ArrayList<L>();
		for (ComparablePair<L, R> pair : this) {
			lefts.add(pair.left);
		}

		return lefts;
	}

	public List<R> rights() {
		ArrayList<R> rights = new ArrayList<R>();
		for (ComparablePair<L, R> pair : this) {
			rights.add(pair.right);
		}

		return rights;
	}

}
