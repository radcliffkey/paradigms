package cz.klic.corpus.reader;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.StringTaggedWord;

public class TigerExportCorpusReader implements
		TaggedCorpusReader<String, String, String> {

	private BufferedReader inputStream;
	
	public TigerExportCorpusReader(BufferedReader inputStream) {
		super();
		this.inputStream = inputStream;
	}

	@Override
	public StringTaggedCorpus readCorpus() throws Exception {
		String line;
		int lineNo = 0;
		List<StringTaggedWord> taggedText = new ArrayList<StringTaggedWord>();
		
		//get to the first sentence
		while ((line = this.inputStream.readLine()) != null) {
			++lineNo;
			
			if (line.startsWith("#BOS")) {
				break;
			}
		}
		
		while ((line = this.inputStream.readLine()) != null) {
			++lineNo;
			
			if (line.isEmpty() || line.startsWith("#") || line.startsWith("%%")) {
				continue;
			}
			
			String [] fields = line.split("\t+");
			String form = fields[0];
			String lemma = fields[1];
			String tag = String.format("%s.%s", fields[2], fields[3]);
			taggedText.add(new StringTaggedWord(form, lemma, tag));
		}
		return new StringTaggedCorpus(taggedText);
	}

}
