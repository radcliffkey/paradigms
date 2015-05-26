package cz.klic.corpus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cz.klic.functional.FuncUtil;
import cz.klic.functional.condition.Condition;
import cz.klic.functional.transformer.SingleTypeTransformer;

public class SimpleCorpus<Token> implements Corpus<Token> {

	public SimpleCorpus(List<Token> text, List<Token> vocab) {
		super();
		this.text = text;
		if (vocab == null) {
			this.vocab = new ArrayList<Token>(new HashSet<Token>(text));
		}
	}

	public SimpleCorpus(List<Token> text) {
		this(text, null);
	}

	private List<Token> text;
	
	private List<Token> vocab;	

	@Override
	public List<Token> getText() {
		return this.text;
	}

	public void setText(List<Token> text) {
		this.text = text;
	}
	
	@Override
	public List<Token> getVocab() {
		return this.vocab;
	}

	public void setVocab(List<Token> vocab) {
		this.vocab = vocab;
	}

	@Override
	public int tokenCount() {
		return this.text.size();
	}

	@Override
	public int typeCount() {
		return this.vocab.size();
	}

	@Override
	public void filterWords(Condition<Token> condition) {
		FuncUtil.filter(this.text, condition);
		this.vocab = new ArrayList<Token>(new HashSet<Token>(text));	
	}

	@Override
	public void transformWords(SingleTypeTransformer<Token> transformer) {
		FuncUtil.transform(this.text, transformer);
		this.vocab = new ArrayList<Token>(new HashSet<Token>(text));
	}
}
