package cz.klic.corpus.reader;

import java.io.IOException;

import cz.klic.corpus.Corpus;

public interface CorpusReader<Token> {

	public Corpus<Token> readCorpus() throws IOException;
	
}
