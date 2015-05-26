package cz.klic.corpus;

import java.io.BufferedReader;

import cz.klic.corpus.reader.ConllCorpusReader;
import cz.klic.corpus.reader.CsvCorpusReader;
import cz.klic.corpus.reader.Pdt1Reader;
import cz.klic.corpus.reader.Pdt1Reader.LemmaType;
import cz.klic.corpus.reader.TaggedCorpusReader;
import cz.klic.corpus.reader.TigerExportCorpusReader;
import cz.klic.util.Fileutil;

public class CorpusFactory {

	public static String DEF_ENCODING = "UTF-8";
	
	public enum TaggedFormat {
		CSTS,
		CONLL,
		TIGER_EXPORT,
		SPACE_SEP
	}
	
	public static TaggedCorpus<String, String, String> getCorpus(
			String fileName, TaggedFormat format)
			throws Exception {
		return getCorpus(fileName, format, DEF_ENCODING);
	}
	
	public static TaggedCorpus<String, String, String> getCorpus(
			String fileName, TaggedFormat format, String encoding)
			throws Exception {
		BufferedReader input = Fileutil.getBufReader(fileName, encoding);
		TaggedCorpusReader<String, String, String> corpusReader = null;
		switch (format) {
		case CSTS:
			corpusReader = new Pdt1Reader(input, LemmaType.BROAD);
			break;
		case CONLL:
			corpusReader = new ConllCorpusReader(input);
			break;
		case TIGER_EXPORT:
			corpusReader = new TigerExportCorpusReader(input);
			break;
		case SPACE_SEP:
			corpusReader = new CsvCorpusReader(input, " ");
			break;
		}
		return corpusReader.readCorpus();
	}
}
