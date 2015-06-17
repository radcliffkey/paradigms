package cz.klic.corpus;

public interface TaggedWord<WordType, LemmaType, TagType> {

	public WordType getForm();
	
	public LemmaType getLemma();
	
	public TagType getTag();
	
	public void setForm(WordType form);
	
	public void setLemma(LemmaType lemma);
	
	public void setTag(TagType tag);
	
}