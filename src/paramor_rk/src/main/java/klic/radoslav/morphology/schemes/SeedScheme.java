package klic.radoslav.morphology.schemes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import klic.radoslav.functional.FuncUtil;
import klic.radoslav.util.StringUtil;
import monson.christian.morphology.paraMor.morphemes.Affix;
import monson.christian.morphology.paraMor.morphemes.Context;
import monson.christian.morphology.paraMor.morphemes.SetOfMorphemes;
import monson.christian.morphology.paraMor.schemes.Scheme;
import monson.christian.util.Pair;

public class SeedScheme {

	private List<Scheme> subschemes;
	private Set<String> coveredTypes;

	public SeedScheme(List<Scheme> subschemes) {
		super();
		this.subschemes = new ArrayList<Scheme>(subschemes);
		Collections.sort(this.subschemes, new Scheme.ByAffixSignature());
		this.computeCoveredTypes();
	}

	public List<Scheme> getSubschemes() {
		return Collections.unmodifiableList(this.subschemes);
	}
	
	private void computeCoveredTypes() {
		this.coveredTypes = new HashSet<String>();
		for (Scheme subscheme : this.subschemes) {
			coveredTypes.addAll(subscheme.getCoveredWordTypes());
		}
	}
	
	public Set<String> getCoveredWordTypes() {
		return this.coveredTypes;
	}
	
	public List<SetOfMorphemes<Affix>> getAffixGroups() {
		List<SetOfMorphemes<Affix>> result = new  ArrayList<SetOfMorphemes<Affix>>();
		for (Scheme subscheme : this.subschemes) {
			result.add(subscheme.getAffixes());
		}
		return result;
	}
	
	public SetOfMorphemes<Affix> getAffixes() {
		SetOfMorphemes<Affix> result = new SetOfMorphemes<Affix>();
		for (Scheme subscheme : this.subschemes) {
			result.add(subscheme.getAffixes());
		}
		return result;
	}
	
	public List<Pair<Affix, Context>> getCoveredAffixContextPairs() {
		List<Pair<Affix, Context>> result = new ArrayList<Pair<Affix, Context>>();
		
		for (Scheme subscheme : this.subschemes) {
			result.addAll(subscheme.getCoveredAffixContextPairs());
		}
		
		return result;
	}

	public String affixSignature() {
		List<String> signatures = FuncUtil.transform(this.subschemes, (Scheme s) -> s.affixSignature());
		return StringUtil.join("/", signatures);
	}
	
	public int variantCount() {
		return this.subschemes.size();
	}
	
	@Override
	public String toString() {
		return subschemes.toString();
	}
}
