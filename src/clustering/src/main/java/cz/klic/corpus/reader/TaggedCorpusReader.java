package cz.klic.corpus.reader;

import cz.klic.corpus.TaggedCorpus;

public interface TaggedCorpusReader<WordType, LemmaType, TagType> {

	public TaggedCorpus<WordType, LemmaType, TagType> readCorpus() throws Exception;
	
}
