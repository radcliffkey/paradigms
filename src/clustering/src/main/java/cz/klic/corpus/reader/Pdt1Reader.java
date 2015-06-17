package cz.klic.corpus.reader;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.klic.corpus.StringTaggedCorpus;
import cz.klic.corpus.StringTaggedWord;
import cz.klic.util.Fileutil;

public class Pdt1Reader implements
		TaggedCorpusReader<String, String, String> {

	private BufferedReader inputStream;
	
	/*
	 * for original lemma rozvíjení_^(*2t)
	 * broad form is rozvíjet
	 * narrow form is rozvíjení
	 */
	public enum LemmaType {
		ORIG,
		BROAD,
		NARROW
	}	

	public static LemmaType DEFAULT_LEMMA_TYPE = LemmaType.ORIG;
	private LemmaType lemmaType;
	
	protected final static Pattern TAG_PATTERN = Pattern.compile("<[^<>]+>");
	protected final static Pattern NONTAG_PATTERN = Pattern.compile("[^<>]+");
	protected final static Pattern LEMMA_PATTERN = Pattern.compile("[\\p{L}\\d'°]+(-\\d+)?");
	
	public static String DEF_ENCODING = "UTF-8";
	
	public Pdt1Reader(BufferedReader inputStream, LemmaType lemmaType) {
		super();
		this.inputStream = inputStream;
		this.lemmaType = lemmaType;
	}
	
	public Pdt1Reader(String fileName, String encoding, LemmaType lemmaType) throws Exception {
		super();
		this.inputStream = Fileutil.getBufReader(fileName, encoding);
		this.lemmaType = lemmaType;
	}
	
	public Pdt1Reader(String fileName, LemmaType lemmaType) throws Exception {
		super();
		this.inputStream = Fileutil.getBufReader(fileName, DEF_ENCODING);
		this.lemmaType = lemmaType;
	}
	
	@Override
	public StringTaggedCorpus readCorpus() throws Exception {
		String line;
		int lineNo = 0;
		List<StringTaggedWord> taggedText = new ArrayList<StringTaggedWord>();
		
		while ((line = this.inputStream.readLine()) != null) {
			++lineNo;

			Matcher matcher = TAG_PATTERN.matcher(line);
			String markupTag = this.readTag(matcher);
			if (!markupTag.startsWith("<f")) {
				continue;
			}
			
			String form = this.readContent(matcher);
			
			markupTag = this.readTag(matcher);
			if (!markupTag.startsWith("<l")) {
				throw new Exception("<l> tag exprected on line " + lineNo);
			}
			
			String lemmaStr = this.readContent(matcher);
			String lemma;
			try {
				lemma = this.parseLemma(lemmaStr);
			} catch (Exception e) {
				throw new Exception("Lemma parsing problem at line " + lineNo + ". line text: " + line, e);
//				System.err.println("Lemma parsing problem at line " + lineNo + ". line text: " + line);
//				lemma = lemmaStr;
			}
			
			markupTag = this.readTag(matcher);
			if (!markupTag.startsWith("<t")) {
				throw new Exception("<t> tag exprected on line " + lineNo);
			}
			
			String morphTag = this.readContent(matcher);
			
			StringTaggedWord taggedWord = new StringTaggedWord(form, lemma, morphTag);
			taggedText.add(taggedWord);
		}
		
		return new StringTaggedCorpus(taggedText);
	}

	private String parseLemma(String lemmaStr) throws Exception {
		if (this.lemmaType == LemmaType.ORIG) {
			return lemmaStr;
		}		
		
		Matcher matcher = LEMMA_PATTERN.matcher(lemmaStr);
		matcher.find();
		String narrowForm = matcher.group();
		
		if (this.lemmaType == LemmaType.NARROW) {
			return narrowForm;
		}
		
		int alphaNumEnd = narrowForm.length();
		int asteriskInd = lemmaStr.indexOf("*", alphaNumEnd);
		if (asteriskInd == -1) {
			return narrowForm;
		}
		
		// case like napájecí_^(^IC**napájet)
		if (lemmaStr.charAt(asteriskInd + 1) == '*') {
			matcher = LEMMA_PATTERN.matcher(lemmaStr.substring(asteriskInd + 2));
			matcher.find();
			String replacement = matcher.group();
			return replacement;
		}
		
		//case like vypověditelný_^(*6ět)
		int NumEnd = asteriskInd + 1;
		while (NumEnd < lemmaStr.length()
				&& Character.isDigit(lemmaStr.charAt(NumEnd))) {
			++NumEnd;
		}
		int replaceLen = Integer.parseInt(lemmaStr.substring(asteriskInd + 1, NumEnd));
		
		alphaNumEnd = NumEnd;
		while (alphaNumEnd < lemmaStr.length()
				&& Character.isLetterOrDigit(lemmaStr.charAt(alphaNumEnd))) {
			++alphaNumEnd;
		}
		
		String replacement = lemmaStr.substring(NumEnd, alphaNumEnd);
		
		String broadForm = narrowForm.substring(0, narrowForm.length() - replaceLen) + replacement;

		return broadForm;
	}

	protected String readTag(Matcher matcher) {
		matcher.usePattern(TAG_PATTERN);
		matcher.find();
		String markupTag = matcher.group();
		
		return markupTag;
	}
	
	protected String readContent(Matcher matcher) {
		matcher.usePattern(NONTAG_PATTERN);
		matcher.find();
		String markupTag = matcher.group();
		
		return markupTag;
	}
}
