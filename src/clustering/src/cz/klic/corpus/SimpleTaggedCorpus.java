package cz.klic.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.klic.functional.FuncUtil;
import cz.klic.functional.condition.Condition;
import cz.klic.functional.transformer.SingleTypeTransformer;
import cz.klic.functional.transformer.Transformer;

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
		List<WordType> result = FuncUtil
				.transform(
						this.taggedText,
						new Transformer<TaggedWord<WordType, LemmaType, TagType>, WordType>() {
							@Override
							public WordType transform(
									TaggedWord<WordType, LemmaType, TagType> obj) {
								return obj.getForm();
							}
						});
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
	public void filter(
			Condition<TaggedWord<WordType, LemmaType, TagType>> condition) {
		FuncUtil.filter(this.taggedText, condition);
		this.recomputeSets();
	}

	@Override
	public void filterWords(final Condition<WordType> condition) {
		this.filter(new Condition<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public boolean isTrue(TaggedWord<WordType, LemmaType, TagType> o1) {
				return condition.isTrue(o1.getForm());
			}
			
		});		
	}

	@Override
	public void filterTags(final Condition<TagType> condition) {
		this.filter(new Condition<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public boolean isTrue(TaggedWord<WordType, LemmaType, TagType> o1) {
				return condition.isTrue(o1.getTag());
			}
			
		});		
	}



	@Override
	public void filterLemmas(final Condition<LemmaType> condition) {
		this.filter(new Condition<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public boolean isTrue(TaggedWord<WordType, LemmaType, TagType> o1) {
				return condition.isTrue(o1.getLemma());
			}
			
		});
	}

	@Override
	public void transform(
			SingleTypeTransformer<TaggedWord<WordType, LemmaType, TagType>> transformer) {
		FuncUtil.transform(this.taggedText, transformer);
		this.recomputeSets();
	}

	@Override
	public void transformWords(final SingleTypeTransformer<WordType> transformer) {
		this.transform(new SingleTypeTransformer<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public TaggedWord<WordType, LemmaType, TagType> transform(
					TaggedWord<WordType, LemmaType, TagType> obj) {

				obj.setForm(transformer.transform(obj.getForm()));
				return obj;
			}		
		});		
	}

	@Override
	public void transformTags(final SingleTypeTransformer<TagType> transformer) {
		this.transform(new SingleTypeTransformer<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public TaggedWord<WordType, LemmaType, TagType> transform(
					TaggedWord<WordType, LemmaType, TagType> obj) {

				obj.setTag(transformer.transform(obj.getTag()));
				return obj;
			}		
		});	
	}

	@Override
	public void transformLemmas(final SingleTypeTransformer<LemmaType> transformer) {
		this.transform(new SingleTypeTransformer<TaggedWord<WordType,LemmaType,TagType>>() {

			@Override
			public TaggedWord<WordType, LemmaType, TagType> transform(
					TaggedWord<WordType, LemmaType, TagType> obj) {

				obj.setLemma(transformer.transform(obj.getLemma()));
				return obj;
			}		
		});	
		
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
