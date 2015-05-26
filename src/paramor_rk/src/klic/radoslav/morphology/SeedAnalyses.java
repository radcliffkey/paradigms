package klic.radoslav.morphology;

import java.util.ArrayList;
import java.util.Collections;
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
import monson.christian.util.Pair;

public class SeedAnalyses {

	private List<SeedScheme> seedSchemes;
	private Set<String> coveredTypes;
	private List<StemPair> stemPairs;
	
	public SeedAnalyses() {
		this(new ArrayList<SeedScheme>());
	}

	public SeedAnalyses(List<SeedScheme> schemes) {
		this.seedSchemes = schemes;
		this.computeCoveredTypes();
	}

	public SeedAnalyses(List<SeedScheme> schemes,
			List<StemPair> stemPairs) {
		this(schemes);
		this.stemPairs = stemPairs;
	}

	private void computeCoveredTypes() {
		this.coveredTypes = new HashSet<String>();
		for (SeedScheme scheme : this.seedSchemes) {
			coveredTypes.addAll(scheme.getCoveredWordTypes());
		}
	}
	
	public Map<Context, SetOfMorphemes<Affix>> getStemToAffixes() {
		Map<Context, SetOfMorphemes<Affix>> stemToAffixes = new HashMap<Context, SetOfMorphemes<Affix>>();
		for (SeedScheme scheme : this.seedSchemes) {
			List<Pair<Affix, Context>> affixContextPairs = scheme.getCoveredAffixContextPairs();
			
			for (Pair<Affix, Context> pair : affixContextPairs) {
				Affix affixToAdd = pair.getLeft();
				Context context = pair.getRight();
				
				if (stemToAffixes.containsKey(context)) {
		            SetOfMorphemes<Affix> setOfAffixes = stemToAffixes.get(context);
		            setOfAffixes.add(affixToAdd);
		        } else {
		            stemToAffixes.put(context, new SetOfMorphemes<Affix>(affixToAdd));
		        }
			}
			
		}
		
		return stemToAffixes;
	}

	public List<SeedScheme> getSeedSchemes() {
		return Collections.unmodifiableList(this.seedSchemes);
	}

	public void setSeedSchemes(List<SeedScheme> seedSchemes) {
		this.seedSchemes = seedSchemes;
		this.computeCoveredTypes();
	}
	
	public List<StemPair> getStemPairs() {
		return stemPairs;
	}

	public Set<String> getCoveredWordTypes() {
		return this.coveredTypes;
	}

}
