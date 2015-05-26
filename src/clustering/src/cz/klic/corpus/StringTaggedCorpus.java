package cz.klic.corpus;

import java.util.List;

public class StringTaggedCorpus extends SimpleTaggedCorpus<String, String, String> {

	public StringTaggedCorpus(
			List<StringTaggedWord> taggedText) {
		super(taggedText);
	}

}
