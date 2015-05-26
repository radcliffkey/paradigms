package klic.radoslav.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import monson.christian.util.Pair;

public class DataUtil {

	public static <T1, T2> Map<T2, Set<T1>> groupBySecond(Iterable<? extends Pair<T1, T2>> pairs) {
		HashMap<T2, Set<T1>> result = new HashMap<T2, Set<T1>>();
		
		for (Pair<T1, T2> pair : pairs) {
			try {
				result.get(pair.getRight()).add(pair.getLeft());
			} catch (Exception e) {
				Set<T1> group = new HashSet<T1>();
				group.add(pair.getLeft());
				result.put(pair.getRight(), group);
			}
		}
		
		return result;
	}
	
	public static <T1, T2> Map<T1, Set<T2>> groupByFirst(Iterable<? extends Pair<T1, T2>> pairs) {
		HashMap<T1, Set<T2>> result = new HashMap<T1, Set<T2>>();
		
		for (Pair<T1, T2> pair : pairs) {
			try {
				result.get(pair.getLeft()).add(pair.getRight());
			} catch (Exception e) {
				Set<T2> group = new HashSet<T2>();
				group.add(pair.getRight());
				result.put(pair.getLeft(), group);
			}
		}
		
		return result;
	}

}
