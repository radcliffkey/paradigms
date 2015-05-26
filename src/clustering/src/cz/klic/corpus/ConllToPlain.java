package cz.klic.corpus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import cz.klic.corpus.reader.ConllCorpusReader;

public class ConllToPlain {

	public static void main(String[] args) {
		
		try {

			ConllCorpusReader reader = new ConllCorpusReader(new BufferedReader(new InputStreamReader(System.in)));
			StringTaggedCorpus corpus = reader.readCorpus();
			List<String> text = corpus.getText();
			for (String word : text) {
				System.out.println(word);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
