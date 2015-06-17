package cz.klic.corpus;

public class StringTaggedWord extends
		SimpleTaggedWord<String, String, String> {

	public StringTaggedWord() {
		super();
	}

	public StringTaggedWord(String form, String lemma, String tag) {
		super(form, lemma, tag);
	}

}
