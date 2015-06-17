package cz.klic.corpus.reader;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.StringTaggedWord;

public class CsvCorpusReader implements
		TaggedCorpusReader<String, String, String> {

	private BufferedReader inputStream;
	
	private String separator;
	
	public CsvCorpusReader(BufferedReader inputStream) {
		this(inputStream, ",");
	}
	
	public CsvCorpusReader(BufferedReader inputStream, String separator) {
		super();
		this.inputStream = inputStream;
		this.separator = separator;
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
			
			String [] fields = line.split(this.separator);
			String form = fields[0];
			String lemma = fields[1];
			String tag = fields[2];
			taggedText.add(new StringTaggedWord(form, lemma, tag));
		}
		return new StringTaggedCorpus(taggedText);
	}

}
