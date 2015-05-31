package klic.radoslav.morphology;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import klic.radoslav.morphology.phonChange.StemPair;
import klic.radoslav.morphology.schemes.SeedScheme;

import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.util.FileUtils;

public class SeedAnalysesParser {

	public SeedAnalyses parse(String fileName) throws IOException {
		return this.parse(FileUtils.openFileForReading(fileName, FileUtils.Encoding.UTF8));
	}
	
	public SeedAnalyses parse(BufferedReader input) throws IOException {
		SeedAnalyses result = null;
		List<SeedScheme> seedSchemes = new ArrayList<SeedScheme>();
		List<StemPair> stemPairs =  new ArrayList<StemPair>();

		String line;
		while ((line = input.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			//stemVariants[0] - stems attachable to all suffixes
			//stemVariants[i] - stems attachable to i-th subset of suffixes
			List<List<Context>> stemVariants = new ArrayList<List<Context>>();
			stemVariants.add(new ArrayList<Context>());
			
			List<SetOfMorphemes<Affix>> affixSubsets = new ArrayList<SetOfMorphemes<Affix>>();
			affixSubsets.add(new SetOfMorphemes<Affix>());
			
			String [] splitLine = line.split("\\+");
			String initStr = splitLine[0];
			String finalStr = splitLine[1];
			
			String [] stemVariantsStrs = initStr.split(",");
			for (String stemVariantsStr : stemVariantsStrs) {
				String [] stemVariantArray = stemVariantsStr.split("/");
				int varCnt = stemVariantArray.length;
				if (varCnt == 1) {
					stemVariants.get(0).add(new Context(stemVariantArray[0].trim(), ""));
				} else {
					for (int i = 0; i < stemVariantArray.length; i++) {
						if (stemVariants.size() < i + 2) {
							stemVariants.add(new ArrayList<Context>());							
						}
						stemVariants.get(i + 1).add(new Context(stemVariantArray[i].trim(), ""));
					}
				}
			}
			
			int varCnt = stemVariants.size() - 1;
			
			if (varCnt == 0) {
				String [] affixes = finalStr.split(",");
				for (String affix : affixes) {
					affix = affix.trim();
					if (affix.equals("0")) {
						affix = "";
					}
					affixSubsets.get(0).add(new Affix(affix));
				}
			} else {
			
				String [] affixSubsetsStrs = finalStr.split("/");
				for (int i = 0; i < affixSubsetsStrs.length; i++) {
					affixSubsets.add(new SetOfMorphemes<Affix>());
					
					String affixSubsetStr = affixSubsetsStrs[i];
					String [] affixes = affixSubsetStr.split(",");
					for (int j = 0; j < affixes.length; j++) {
						String affix = affixes[j].trim();
						if (affix.equals("0")) {
							affix = "";
						}
						
						affixSubsets.get(0).add(new Affix(affix));
						affixSubsets.get(i + 1).add(new Affix(affix));
					}
				}
			}
			
			List<Scheme> subschemes = new ArrayList<Scheme>();
			
			for (int i = 0; i < varCnt + 1; ++i) {				
				if (stemVariants.get(i).size() > 0) {
					Set<Context> stems = new HashSet<Context>(stemVariants.get(i));
					Scheme subscheme = new Scheme(affixSubsets.get(i), new SetOfMorphemes<Context>(stems));
					subschemes.add(subscheme);
				}
			}
			
			seedSchemes.add(new SeedScheme(subschemes));
			
			if (varCnt > 0) {
				int stemCnt = stemVariants.get(1).size();
				for (int stemNo = 0; stemNo < stemCnt; ++stemNo) {
					for (int variantNo = 1; variantNo < varCnt + 1; ++variantNo) {
						for (int otherVariantNo = variantNo + 1; otherVariantNo < varCnt + 1; ++otherVariantNo) {
							Context stem1 = stemVariants.get(variantNo).get(stemNo);
							Context stem2 = stemVariants.get(otherVariantNo).get(stemNo);
							SetOfMorphemes<Affix> affixes1 = affixSubsets.get(variantNo);
							SetOfMorphemes<Affix> affixes2 = affixSubsets.get(otherVariantNo);
							StemPair stemPair =  new StemPair(stem1, stem2, affixes1, affixes2);
							stemPairs.add(stemPair);
						}
					}
				}
			}
			
			
		}
		
		seedSchemes = this.mergeByAffixSignature(seedSchemes);
		
		result = new SeedAnalyses(seedSchemes, stemPairs);
		return result;
	}

	private List<SeedScheme> mergeByAffixSignature(List<SeedScheme> seedSchemes) {
		Map<String, List<SeedScheme>> signatureToSchemes = new HashMap<String, List<SeedScheme>>();
		
		for (SeedScheme seedScheme : seedSchemes) {
			String sig = seedScheme.affixSignature();
			
			try {
				signatureToSchemes.get(sig).add(seedScheme);
			} catch (Exception e) {
				ArrayList<SeedScheme> newSchemeList = new ArrayList<SeedScheme>();
				newSchemeList.add(seedScheme);
				signatureToSchemes.put(sig, newSchemeList);
			}
		}
		
		ArrayList<SeedScheme> newSchemeList = new ArrayList<SeedScheme>();
		
		for (List<SeedScheme> schemeGroup : signatureToSchemes.values()) {
			int variantCount = schemeGroup.get(0).variantCount();
			
			List<SetOfMorphemes<Affix>> commonAffixGroups = schemeGroup.get(0).getAffixGroups();
			
			List<Scheme> newSubschemes = new ArrayList<Scheme>();
			
			for (int i = 0; i < variantCount; i++) {
				SetOfMorphemes<Context> stemsForCurrVariant = new SetOfMorphemes<Context>();
				for (SeedScheme schemeInGroup : schemeGroup) {
					SetOfMorphemes<Context> stems = schemeInGroup.getSubschemes().get(i).getContexts();
					stemsForCurrVariant.add(stems);
				}
				newSubschemes.add(new Scheme(commonAffixGroups.get(i), stemsForCurrVariant));
			}
			
			SeedScheme newSeedScheme = new SeedScheme(newSubschemes);
			newSchemeList.add(newSeedScheme);
		}
		
		return newSchemeList;
	}
	
}
