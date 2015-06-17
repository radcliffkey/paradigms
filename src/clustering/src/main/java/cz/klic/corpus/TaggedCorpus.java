package cz.klic.corpus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import cz.klic.functional.transformer.SingleTypeTransformer;

public interface TaggedCorpus<WordType, LemmaType, TagType> extends Corpus<WordType> {

	public List<? extends TaggedWord<WordType, LemmaType, TagType>> getTaggedText();
	
	public List<LemmaType> getLemmaSet();
	
	public List<TagType> getTagSet();
	
	public int lemmaCount();
	
	public int tagCount();
	
	public void filter(Predicate<TaggedWord<WordType, LemmaType, TagType>> condition);
	
	public void filterTags(Predicate<TagType> condition);
	
	public void filterLemmas(Predicate<LemmaType> condition);
	
	public void transform(SingleTypeTransformer<TaggedWord<WordType, LemmaType, TagType>> transformer);
	
	public void transformWords(SingleTypeTransformer<WordType> transformer);
	
	public void transformTags(SingleTypeTransformer<TagType> transformer);
	
	public void transformLemmas(SingleTypeTransformer<LemmaType> transformer);
	
	public Map<LemmaType, Set<WordType>> getLemmaToFormsMap();
}
