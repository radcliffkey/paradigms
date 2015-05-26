package cz.klic.corpus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import cz.klic.corpus.reader.Pdt1Reader;

public class Pdt1ToPlain {

	public static void main(String[] args) {
		
		try {

			Pdt1Reader cr = new Pdt1Reader(new BufferedReader(new InputStreamReader(System.in)), Pdt1Reader.LemmaType.BROAD);
			StringTaggedCorpus corpus = cr.readCorpus();
			List<String> text = corpus.getText();
			for (String word : text) {
				System.out.println(word);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
