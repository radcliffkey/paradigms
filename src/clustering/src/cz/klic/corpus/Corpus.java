package cz.klic.corpus;

import java.util.List;

import cz.klic.functional.condition.Condition;
import cz.klic.functional.transformer.SingleTypeTransformer;

public interface Corpus<Token> {

	public List<Token> getVocab();
	
	public List<Token> getText();
	
	public int tokenCount();
	
	public int typeCount();
	
	public void filterWords(Condition<Token> condition);
	
	public void transformWords(SingleTypeTransformer<Token> transformer);
}
