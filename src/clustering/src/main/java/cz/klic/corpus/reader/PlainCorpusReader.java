package cz.klic.corpus.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.klic.corpus.Corpus;
import cz.klic.corpus.SimpleCorpus;
import cz.klic.functional.FuncUtil;
import cz.klic.functional.transformer.LowerCaser;
import cz.klic.util.Fileutil;

public class PlainCorpusReader implements CorpusReader<String> {

	public static String DEF_ENCODING = "UTF-8";
	private BufferedReader inputStream;
	
	public PlainCorpusReader(String fileName) throws IOException {
		this(Fileutil.getBufReader(fileName, DEF_ENCODING));
	}
	
	public PlainCorpusReader(String fileName, String encoding) throws IOException {
		this(Fileutil.getBufReader(fileName, encoding));
	}

	public PlainCorpusReader(BufferedReader inputStream) {
		super();
		this.inputStream = inputStream;
	}

	@Override
	public Corpus<String> readCorpus() throws IOException {
		String line;
		List<String> text = new ArrayList<String>();
		while ((line = this.inputStream.readLine()) != null) {
			String [] words = line.split("[\\s]+");
			List<String> wordList = Arrays.asList(words);
			FuncUtil.mapInPlace(wordList, new LowerCaser());
			text.addAll(wordList);
		}
		
		SimpleCorpus<String> corpus = new SimpleCorpus<String>(text);
		
		return corpus;
	}

}
