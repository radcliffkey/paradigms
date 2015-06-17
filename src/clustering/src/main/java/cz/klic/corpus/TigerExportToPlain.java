package cz.klic.corpus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import cz.klic.corpus.reader.TigerExportCorpusReader;

public class TigerExportToPlain {

	public static void main(String[] args) {

		try {

			TigerExportCorpusReader reader = new TigerExportCorpusReader(
					new BufferedReader(
							new InputStreamReader(System.in, "UTF-8")));
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
