package cz.klic.test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.reader.Pdt1Reader;

public class TestFormSet {
	public static void main(String[] args) {
		try {
			Pdt1Reader reader = new Pdt1Reader(args[0], Pdt1Reader.LemmaType.BROAD);
			StringTaggedCorpus corpus = reader.readCorpus();
			System.out.println("number of types: " + corpus.typeCount());
			Map<String, Set<String>> formSetMap = corpus.getLemmaToFormsMap();
			Collection<Set<String>> formSets = formSetMap.values();
			int formSum = 0;
			for (Set<String> formSet : formSets) {
				System.out.println(formSet);
				formSum += formSet.size();
			}
			System.out.println("Sum of formset sizes: " + formSum);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
