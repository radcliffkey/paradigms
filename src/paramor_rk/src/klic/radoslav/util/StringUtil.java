package klic.radoslav.util;

import gnu.trove.set.hash.TCharHashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {

	private static final String vowels = "aeouyiôéíáýúůěöäü";
	private static final TCharHashSet vowelSet = new TCharHashSet(vowels.toCharArray());
	
	/**
	 * Appends toString() values of collection members, adding delimiter
	 * between them.
	 * 
	 * @param <T>
	 * @param delim
	 * @param items
	 * @return
	 */
	public static <T> String join(String delim, Collection<T> items) {
		if (items.size() == 0) {
			return "";
		}
		
		StringBuilder resultSb = new StringBuilder();
		Iterator<? extends Object> it = items.iterator();
		
		int i = 0;
		for (; i < items.size() - 1; ++i) {
			resultSb.append(it.next());
			resultSb.append(delim);
		}
		
		resultSb.append(it.next());
		
		String result = resultSb.toString();
		
		return result;
	}
	
	/**
	 * Appends toString() values of the items, adding delimiter
	 * between them.
	 * 
	 * @param <T>
	 * @param delim
	 * @param items
	 * @return
	 */
	public static <T> String join(String delim, T ... items) {
		return join(delim, Arrays.asList(items));
	}
	
	public static String join(String delim, double ... items) {
		if (items.length == 0) {
			return "";
		}
		
		StringBuilder resultSb = new StringBuilder(String.valueOf(items[0]));
		
		for (int i = 1; i < items.length; i++) {
			resultSb.append(delim);
			resultSb.append(items[i]);
		}
		
		String result = resultSb.toString();
		
		return result;
	}
	
	public static String join(String delim, int ... items) {
		if (items.length == 0) {
			return "";
		}
		
		StringBuilder resultSb = new StringBuilder(String.valueOf(items[0]));
		
		for (int i = 1; i < items.length; i++) {
			resultSb.append(delim);
			resultSb.append(items[i]);
		}
		
		String result = resultSb.toString();
		
		return result;
	}

	public static boolean isVowel(char c) {
		return vowelSet.contains(c);
	}
	
	public static boolean isVowelCluster(String s) {
		for (int i = 0; i < s.length(); ++i) {
			if (!isVowel(s.charAt(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean matchesAny(String str, List<Pattern> patterns) {
		for (Pattern pattern: patterns) {
			if (pattern.matcher(str).matches()) {
				return true;
			}
		}
		
		return false;
	}
}
