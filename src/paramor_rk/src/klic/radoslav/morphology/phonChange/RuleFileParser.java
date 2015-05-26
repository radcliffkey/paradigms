package klic.radoslav.morphology.phonChange;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import monson.christian.util.FileUtils;
import monson.christian.util.FileUtils.Encoding;
import monson.christian.util.Pair;

public class RuleFileParser {

	public RuleFileParser() {

	}

	public List<StemChangeRule> parseFile(String fileName) throws Exception {
		BufferedReader file = FileUtils.openFileForReading(fileName, Encoding.UTF8);
		String line;
		List<StemChangeRule> rules = new ArrayList<StemChangeRule>();
		RuleParser ruleParser = new RuleParser();
		while ((line = file.readLine()) != null) {
			Pair<StemChangeRule, StemChangeRule> currRules = ruleParser.parseRule(line);
			rules.add(currRules.getLeft());
			rules.add(currRules.getRight());
		}
		
		file.close();
		
		return rules;
	}
}
