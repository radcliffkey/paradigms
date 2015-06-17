package cz.klic.corpus.reader;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.StringTaggedWord;

public class ConllCorpusReader implements
		TaggedCorpusReader<String, String, String> {

	private BufferedReader inputStream;
	
	public ConllCorpusReader(BufferedReader inputStream) {
		super();
		this.inputStream = inputStream;
	}

	@Override
	public StringTaggedCorpus readCorpus() throws Exception {
		String line;
		int lineNo = 0;
		List<StringTaggedWord> taggedText = new ArrayList<StringTaggedWord>();
		
		while ((line = this.inputStream.readLine()) != null) {
			++lineNo;
			
			if (line.isEmpty()) {
				continue;
			}
			
			String [] fields = line.split("\t");
			String form = fields[1];
			String lemma = fields[2];
			String tag = String.format("pos=%s|%s", fields[4], fields[5]);
			taggedText.add(new StringTaggedWord(form, lemma, tag));
		}
		return new StringTaggedCorpus(taggedText);
	}

}
