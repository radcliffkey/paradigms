package klic.radoslav.morphology.phonChange;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import klic.radoslav.util.DebugLog;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.morphemes.Affix;

public class RuleInducer {
	
	private static void induceStemFinal(String stem1, String stem2,
			Set<Affix> affixes1, Set<Affix> affixes2, Set<StemChangeRule> rules) {
		int shorterLen = Math.min(stem1.length(), stem2.length());
		int i = 0;
		for (; i < shorterLen; ++i) {
			if (stem1.charAt(i) != stem2.charAt(i)) {
				break;
			}
		}
		if (i == shorterLen) {
			--i;
		}

		StemFinalChangeRule rule = new StemFinalChangeRule(stem1.substring(i),
				stem2.substring(i), affixes1, affixes2);
		rules.add(rule);

		rule = new StemFinalChangeRule(stem2.substring(i), stem1.substring(i),
				affixes2, affixes1);
		rules.add(rule);
	}
	
	/**
	 * finds internal vowel changes
	 * @param stem1
	 * @param stem2
	 * @param affixes1
	 * @param affixes2
	 * @param rules
	 */
	private static void induceStemInternal(String stem1, String stem2,
			Set<Affix> affixes1, Set<Affix> affixes2, Set<StemChangeRule> rules) {
		int l1 = stem1.length();
		int l2 = stem2.length();
		int shorterLen = Math.min(l1, l2);
		
		int left = 0;
		for (; left < shorterLen; ++left) {
			if (stem1.charAt(left) != stem2.charAt(left)) {
				break;
			}
		}
		
		if (left == 0 || left >= shorterLen - 1) {
			return;
		}
		
		int right = 1;
		for (; right < shorterLen; ++right) {
			if (stem1.charAt(l1 - right) != stem2.charAt(l2 - right)) {
				break;
			}
		}
		
		if (right == 1 || right >= shorterLen) {
			return;
		}
		
		String delta1 = stem1.substring(left, l1 - right + 1);
		String delta2 = stem2.substring(left, l2 - right + 1);
		
		if (!StringUtil.isVowelCluster(delta1) || !StringUtil.isVowelCluster(delta2)) {
			return;
		}
		
		InternalVowelChangeRule rule = new InternalVowelChangeRule(delta1, delta2);
		rules.add(rule);
		
		rule = new InternalVowelChangeRule(delta2, delta1);
		rules.add(rule);		
	}
	
	public static Set<StemChangeRule> induceRules(Iterable<StemPair> stemPairs)
			throws IOException {
		Set<StemChangeRule> rules = new HashSet<StemChangeRule>();

		for (StemPair stemPair : stemPairs) {
			String stem1 = stemPair.getStem1().toStringAvoidUndescore();
			String stem2 = stemPair.getStem2().toStringAvoidUndescore();
			
			Set<Affix> affixes1 = stemPair.getAffixes1().getCopyOfMorphemes();
			Set<Affix> affixes2 = stemPair.getAffixes2().getCopyOfMorphemes();
			
			induceStemFinal(stem1, stem2, affixes1, affixes2, rules);
			induceStemInternal(stem1, stem2, affixes1, affixes2, rules);
			
		}
		DebugLog.write(StringUtil.join("\n", rules));
		return rules;
	}

}
