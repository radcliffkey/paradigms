package klic.radoslav.morphology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import klic.radoslav.morphology.phonChange.PrefixRule;
import klic.radoslav.morphology.phonChange.RuleInducer;
import klic.radoslav.morphology.phonChange.StemChangeRule;
import klic.radoslav.morphology.phonChange.StemPair;
import klic.radoslav.morphology.phonChange.UnprefixRule;
import klic.radoslav.util.DebugLog;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.util.FileUtils;
import monson.christian.util.FileUtils.Encoding;

public class ManualData {

	private static String seedFileName; 
	
	private static String autoSeedFileName;
	
	private static String prefixFileName;
	
	private static SeedAnalyses manualAnalyses;
	
	private static SeedAnalyses autoSeed;
	
	private static List<String> prefixes;
	
	private static Set<StemChangeRule> manualRules;
	
	private static Set<StemChangeRule> rules;
	
	private static Map<Context, Context> surfaceToDeep;
	
	public static void setSeedFileName(String fileName) {
		seedFileName = fileName;
	}
	
	public static boolean isManualSeedAvailable() {
		return seedFileName != null;
	}
	
	public static SeedAnalyses getManualAnalyses() throws IOException {
		if (manualAnalyses == null) {
			SeedAnalysesParser parser =  new SeedAnalysesParser();
			manualAnalyses = parser.parse(seedFileName);
		}
		return manualAnalyses;
	}
	
	public static void setManualRules(Set<StemChangeRule> rules) {
		manualRules = rules;
	}
	
	public static Set<StemChangeRule> getManualRules() {
		return manualRules;
	}
	
	public static boolean isManualRulesAvailable() {
		return manualRules != null;
	}
	
	public static String getAutoSeedFileName() {
		return autoSeedFileName;
	}
	
	public static void setAutoSeedFileName(String fileName) {
		autoSeedFileName = fileName;
	}
	
	public static boolean isAutoSeedAvailable() {
		return autoSeedFileName != null;
	}
	
	public static SeedAnalyses getAutoAnalyses() throws IOException {
		if (autoSeed == null) {
			SeedAnalysesParser parser =  new SeedAnalysesParser();
			autoSeed = parser.parse(autoSeedFileName);
		}
		return autoSeed;
	}
	
	public static void setPrefixFileName(String fileName) {
		prefixFileName = fileName;
	}
	
	public static void readPrefixes() throws IOException {
		BufferedReader reader = FileUtils.openFileForReading(prefixFileName, FileUtils.Encoding.UTF8);
		prefixes = new ArrayList<String>();
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			String prefix = line.trim();
			prefixes.add(prefix);
		}
	}
	
	public static boolean isPrefixesAvailable() {
		return prefixes != null;
	}
	
	public static Set<StemChangeRule> getStemChageRules() throws IOException {
		if (rules == null) {
			
			Set<StemPair> stempairSet = new HashSet<StemPair>();
			
			if (isManualSeedAvailable()) {
				List<StemPair> stemPairs = getManualAnalyses().getStemPairs();
				stempairSet.addAll(stemPairs);
			}			
			
			if (isAutoSeedAvailable()) {			
				List<StemPair> stemPairs = getAutoAnalyses().getStemPairs();
				stempairSet.addAll(stemPairs);
			}
			
			rules = RuleInducer.induceRules(stempairSet);

			if (isPrefixesAvailable()) {
				for (String prefix : prefixes) {
					rules.add(new PrefixRule(prefix));
					rules.add(new UnprefixRule(prefix));
				}
			}
			
			if (isManualRulesAvailable()) {
				rules.addAll(manualRules);
			}
		}
		return rules;
	}
	
	public static boolean areStemChangeRulesAvailable() throws IOException {
		return getStemChageRules().size() > 0;
	}
	
	public static boolean areDeepStemsAvailable() {
		return surfaceToDeep != null;
	}
	
	public static Context getDeepStem(Context stem) {
		return surfaceToDeep.get(stem);
	}
	
	private static SetOfMorphemes<Context> expand(Context stem,
			Set<StemChangeRule> rules,
			Map<Context, SetOfMorphemes<Affix>> stemToAffixes) {
		SetOfMorphemes<Context> variants = new SetOfMorphemes<Context>(stem);
		for (StemChangeRule rule : rules) {
			Context variant = rule.applyChecked(stem, stemToAffixes);
			if (variant != null) {
				variants.add(variant);
			}
		}
		return variants;
	}
	
	public static void joinStemVariants(Map<Context, SetOfMorphemes<Affix>> stemToAffixes) throws IOException {
		
		surfaceToDeep = new HashMap<Context, Context>();
		Map<Context, SetOfMorphemes<Affix>> stemsToAdd =  new HashMap<Context, SetOfMorphemes<Affix>>();
		SetOfMorphemes<Context> stemsToRemove =  new SetOfMorphemes<Context>();
		Set<StemChangeRule> rules = ManualData.getStemChageRules();
		
		for (Context stem : stemToAffixes.keySet()) {
			if (stemsToRemove.containsAll(stem)){
				continue;
			}
			
			SetOfMorphemes<Context> allVariants = new SetOfMorphemes<Context>(stem);
			LinkedList<Context> stemsToExpand = new LinkedList<Context>();
			stemsToExpand.add(stem);
			while (!stemsToExpand.isEmpty()) {
				Context stemToExpand = stemsToExpand.pollFirst();
				SetOfMorphemes<Context> variants = expand(stemToExpand, rules, stemToAffixes);
				if (allVariants.containsAll(variants)) {
					continue;
				}
				for (Context newVariant : variants) {
					if (!allVariants.containsAll(newVariant)) {
						stemsToExpand.add(newVariant);
						allVariants.add(newVariant);
					}
				}
				
			}

			if (allVariants.size() > 1) {
				SetOfMorphemes<Affix> allAffixes = new SetOfMorphemes<Affix>();
				for (Context variant : allVariants) {
					stemsToRemove.add(variant);
					allAffixes.add(stemToAffixes.get(variant));
				}
				
				ArrayList<Context> variantList = new ArrayList<Context>(allVariants.getCopyOfMorphemes());
				Collections.sort(variantList);
				Context newStem = new Context(StringUtil.join("", variantList),	"");
				stemsToAdd.put(newStem, allAffixes);

				for (Context variant : allVariants) {
					surfaceToDeep.put(variant, newStem);
					stemsToRemove.add(variant);
				}				
			}
		}
		
		stemsToAdd.keySet().retainAll(surfaceToDeep.values());
		
		PrintWriter variantFile = FileUtils.openFileForWriting("stemVariants.txt", Encoding.UTF8);
		for (Context variant : surfaceToDeep.keySet()) {
			Context newStem = surfaceToDeep.get(variant);
			variantFile.printf("%s\t%s\n", variant.toStringAvoidUndescore(), newStem.toStringAvoidUndescore());			
		}
		variantFile.close();
		
		DebugLog.write("size before removing variant stems:" + stemToAffixes.size());		
		stemToAffixes.keySet().removeAll(stemsToRemove.getCopyOfMorphemes());
		DebugLog.write("size after removing variant stems:" + stemToAffixes.size());
		
		stemToAffixes.putAll(stemsToAdd);
		DebugLog.write("newly added stems:");
		for (Context stem : stemsToAdd.keySet()) {
			DebugLog.write(String.format("%s %s", stem, stemsToAdd.get(stem)));
		}
		DebugLog.write("size after adding:" + stemToAffixes.size());
	}

}
