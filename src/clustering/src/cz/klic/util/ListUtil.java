package cz.klic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListUtil {

	public static <T> List<T> subList(List<T> list, Integer ... indices) {
		List<T> result =  new ArrayList<T>(indices.length);
		for (int i : indices) {
			result.add(list.get(i));
		}
		
		return result;
	}
	
	public static <T> void retainAll(List<T> list, Collection<Integer> indices) {
		retainAll(list, new HashSet<Integer>(indices));
	}
	
	public static <T> void retainAll(List<T> list, Integer ... indices) {
		retainAll(list, Arrays.asList(indices));
	}
	
	public static <T> void retainAll(List<T> list, Set<Integer> indices) {
		
		int i = 0;
		for (Iterator<T> iterator = list.iterator(); iterator.hasNext();) {
			iterator.next();
			if (!indices.contains(i)) {
				iterator.remove();
			}
			++i;
		}
	}
	
	public static <T extends Comparable<T>> List<T> sortedSet(Set<T> set) {
		List<T> result = new ArrayList<T>(set);
		Collections.sort(result);
		return result;
	}
	
}
