package cz.klic.corpus;

public class SimpleTaggedWord<WordType, LemmaType, TagType> implements
		TaggedWord<WordType, LemmaType, TagType> {

	private WordType form;
	private LemmaType lemma;
	private TagType tag;
	
	public SimpleTaggedWord() {
		this(null, null, null);
	}

	public SimpleTaggedWord(WordType form, LemmaType lemma, TagType tag) {
		super();
		this.form = form;
		this.lemma = lemma;
		this.tag = tag;
	}
	
	public WordType getForm() {
		return form;
	}
	
	public void setForm(WordType form) {
		this.form = form;
	}
	
	public LemmaType getLemma() {
		return lemma;
	}
	
	public void setLemma(LemmaType lemma) {
		this.lemma = lemma;
	}
	
	public TagType getTag() {
		return tag;
	}
	
	public void setTag(TagType tag) {
		this.tag = tag;
	}

	@Override
	public String toString() {
		return "(form=" + form + ", lemma=" + lemma + ", tag="	+ tag + ")";
	}

}
