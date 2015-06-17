package cz.klic.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import cz.klic.functional.FuncUtil;
import cz.klic.functional.transformer.SingleTypeTransformer;

public class SimpleTaggedCorpus<WordType, LemmaType, TagType> implements
		TaggedCorpus<WordType, LemmaType, TagType> {

	private List<? extends TaggedWord<WordType,LemmaType,TagType>> taggedText;

	private List<LemmaType> lemmaSet;

	private List<TagType> tagSet;

	private List<WordType> vocab;

	public SimpleTaggedCorpus(
			List<? extends TaggedWord<WordType, LemmaType, TagType>> taggedText) {
		super();
		this.taggedText = taggedText;
		
		this.recomputeSets();
	}

	public List<? extends TaggedWord<WordType, LemmaType, TagType>> getTaggedText() {
		return taggedText;
	}

	public List<LemmaType> getLemmaSet() {
		return lemmaSet;
	}

	public List<TagType> getTagSet() {
		return tagSet;
	}

	@Override
	public List<WordType> getVocab() {
		return this.vocab;
	}

	@Override
	public List<WordType> getText() {
		List<WordType> result = FuncUtil.map(this.taggedText, tw -> tw.getForm());
		return result;
	}

	@Override
	public int tokenCount() {
		return this.taggedText.size();
	}

	@Override
	public int typeCount() {
		return this.vocab.size();
	}

	@Override
	public int lemmaCount() {
		return this.lemmaSet.size();
	}

	@Override
	public int tagCount() {
		return this.tagSet.size();
	}

	@Override
	public void filter(Predicate<TaggedWord<WordType, LemmaType, TagType>> condition) {
		FuncUtil.filter(this.taggedText, condition);
		this.recomputeSets();
	}

	@Override
	public void filterWords(final Predicate<WordType> condition) {
		this.filter(tw -> condition.test(tw.getForm()));		
	}

	@Override
	public void filterTags(final Predicate<TagType> condition) {
	    this.filter(tw -> condition.test(tw.getTag()));
	}

	@Override
	public void filterLemmas(final Predicate<LemmaType> condition) {
		this.filter(tw -> condition.test(tw.getLemma()));
	}

	@Override
	public void transform(
			SingleTypeTransformer<TaggedWord<WordType, LemmaType, TagType>> transformer) {
		FuncUtil.mapInPlace(this.taggedText, transformer);
		this.recomputeSets();
	}

	@Override
	public void transformWords(final SingleTypeTransformer<WordType> transformer) {
	    this.taggedText.forEach(tw -> {
	        tw.setForm(transformer.apply(tw.getForm()));
		});
	    this.recomputeSets();
	}

	@Override
	public void transformTags(final SingleTypeTransformer<TagType> transformer) {
	    this.taggedText.forEach(tw -> {
            tw.setTag(transformer.apply(tw.getTag()));
        });
        this.recomputeSets();
	}

	@Override
	public void transformLemmas(final SingleTypeTransformer<LemmaType> transformer) {
	    this.taggedText.forEach(tw -> {
            tw.setLemma(transformer.apply(tw.getLemma()));
        });
        this.recomputeSets();
		
	}

	private void recomputeSets() {
		
		Set<LemmaType> lemmaSet = new HashSet<LemmaType>();
		Set<TagType> tagSet = new HashSet<TagType>();
		Set<WordType> vocabSet = new HashSet<WordType>();
		
		for (TaggedWord<WordType, LemmaType, TagType> taggedWord : taggedText) {
			lemmaSet.add(taggedWord.getLemma());
			tagSet.add(taggedWord.getTag());
			vocabSet.add(taggedWord.getForm());
		}
		
		this.lemmaSet = new ArrayList<LemmaType>(lemmaSet);
		this.tagSet = new ArrayList<TagType>(tagSet);
		this.vocab = new ArrayList<WordType>(vocabSet);
	}

	@Override
	public Map<LemmaType, Set<WordType>> getLemmaToFormsMap() {
		Map<LemmaType, Set<WordType>> lemmaFormMap = new HashMap<LemmaType, Set<WordType>>(this.lemmaCount());
		for (TaggedWord<WordType, LemmaType, TagType> taggedWord : taggedText) {
			WordType form = taggedWord.getForm();
			LemmaType lemma = taggedWord.getLemma();
			Set<WordType> formSet = lemmaFormMap.get(lemma);
			if (formSet == null) {
				formSet = new HashSet<WordType>();
				lemmaFormMap.put(lemma, formSet) ;
			}
			formSet.add(form);
		}
		return lemmaFormMap;
	}

}
