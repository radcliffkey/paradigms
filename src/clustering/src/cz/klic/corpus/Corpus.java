package cz.klic.corpus;

import java.util.List;
import java.util.function.Predicate;

import cz.klic.functional.transformer.SingleTypeTransformer;

public interface Corpus<Token> {

	public List<Token> getVocab();
	
	public List<Token> getText();
	
	public int tokenCount();
	
	public int typeCount();
	
	public void filterWords(Predicate<Token> condition);
	
	public void transformWords(SingleTypeTransformer<Token> transformer);
}
