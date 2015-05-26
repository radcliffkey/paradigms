package cz.klic.test;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.reader.TigerExportCorpusReader;
import cz.klic.functional.transformer.LowerCaser;
import cz.klic.util.Fileutil;

public class TestTigerReader {

	public static void main(String[] args) {
		try {
			TigerExportCorpusReader reader = new TigerExportCorpusReader(Fileutil.getBufReader(args[0]));
			StringTaggedCorpus corpus = reader.readCorpus();
			System.out.println(corpus.getTaggedText().subList(0, 50));
			System.out.println("number of tokens: " + corpus.tokenCount());
			System.out.println("number of types: " + corpus.typeCount());
			System.out.println("number of lemmas: " + corpus.lemmaCount());
			System.out.println("number of tags: " + corpus.tagCount());
			
			corpus.transformWords(new LowerCaser());
			System.out.println(corpus.getTaggedText().subList(0, 50));
			System.out.println("number of tokens: " + corpus.tokenCount());
			System.out.println("number of types: " + corpus.typeCount());
			System.out.println("number of lemmas: " + corpus.lemmaCount());
			System.out.println("number of tags: " + corpus.tagCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
