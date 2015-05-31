package klic.radoslav.morphology.phonChange;

import java.util.HashSet;
import java.util.Set;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.util.Pair;

public class RuleParser {

	public Pair<StemChangeRule, StemChangeRule> parseRule(String ruleStr) throws Exception {
		int colonIdx = ruleStr.indexOf(':');
		
		if (colonIdx == -1) {
			throw new Exception("Rule type not specified");
		}
		
		String typeStr = ruleStr.substring(0, colonIdx).trim().toLowerCase();
		ruleStr = ruleStr.substring(colonIdx + 1).trim();
		
		Pair<StemChangeRule, StemChangeRule> rulePair = null;
		
		if (typeStr.equals("sf")) {
			rulePair = parseStemFinalRule(ruleStr);
		} else if (typeStr.equals("si")) {
			rulePair = parseStemInternalRule(ruleStr);
		} else {
			throw new Exception("Unknown rule type");
		}
		
		return rulePair;
	}
	
	private class SfRulePart {
		public String stemEnding;
		public Set<Affix> affixes;
		
		public SfRulePart(String stemEnding, Set<Affix> affixes) {
			this.stemEnding = stemEnding;
			this.affixes = affixes;
		}
		
	}
	
	private SfRulePart parseSfRulePart(String rulePartStr) {
		String[] splitStr = rulePartStr.split("\\+");
		String stemEnding = splitStr[0].trim().substring(1);
		
		String [] affixStrs = splitStr[1].split(",");
		Set<Affix> affixes = new HashSet<Affix>();
		for (String affixStr : affixStrs) {
			affixStr = affixStr.trim();
			if (affixStr.equals("0")) {
				affixStr = "";
			}
			affixes.add(new Affix(affixStr));
		}
		
		return new SfRulePart(stemEnding, affixes);
	}

	private Pair<StemChangeRule, StemChangeRule> parseStemFinalRule(String ruleStr) {
		String[] parts = ruleStr.split("<->");
		String left = parts[0];
		String right = parts[1];
		
		SfRulePart leftPart = parseSfRulePart(left);
		SfRulePart rightPart = parseSfRulePart(right);

		StemChangeRule firstRule = new StemFinalChangeRule(leftPart.stemEnding,
				rightPart.stemEnding, leftPart.affixes, rightPart.affixes);
		StemChangeRule secondRule = new StemFinalChangeRule(rightPart.stemEnding,
				leftPart.stemEnding, rightPart.affixes, leftPart.affixes);

		return new Pair<StemChangeRule, StemChangeRule>(firstRule, secondRule);
	}
	
	private String parseSiRulePart(String left) throws Exception {
		left =  left.trim();
		if (!left.startsWith("_") || !left.endsWith("_")) {
			throw new Exception("s.i. change: changing part of the stem must be marked with underscores");
		}
		
		return left.substring(1, left.length() - 1);
	}
	
	private Pair<StemChangeRule, StemChangeRule> parseStemInternalRule(String ruleStr) throws Exception {
		String[] parts = ruleStr.split("<->");
		String left = parts[0];
		String right = parts[1];
		
		String delta1 = parseSiRulePart(left);
		String delta2 = parseSiRulePart(right);
		
		StemChangeRule firstRule = new StemInternalChangeRule(delta1, delta2);
		StemChangeRule secondRule = new StemInternalChangeRule(delta2, delta1);
		
		return new Pair<StemChangeRule, StemChangeRule>(firstRule, secondRule);
	}

}
