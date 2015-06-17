package cz.klic.test;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.reader.Pdt1Reader;

public class TestPdt1Reader {

	public static void main(String[] args) {
		try {
			Pdt1Reader reader = new Pdt1Reader(args[0], Pdt1Reader.LemmaType.BROAD);
			StringTaggedCorpus corpus = reader.readCorpus();
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
