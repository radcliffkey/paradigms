/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import monson.christian.morphology.paraMor.languages.Language;
import monson.christian.morphology.paraMor.languages.Language.LanguageName;
import monson.christian.util.ComparablePair;
import monson.christian.util.PairList;

/**
 * This class represents a corpus of text that is to be read during morphology induction.
 * 
 * <p>There are several important possible options to consider when reading a corpus of text
 * for morphology induction.  First, there is the number of tokens or types that will be read
 * from the corpus.  Using the initializations file, the user may specify either a number
 * of tokens to read from the corpus file, or a number of types to read from the corpus file.
 * If neither a type nor a token limit is specified then the system defaults to reading ALL
 * of the corpus file.  Second, you may specify whether numbers are valid tokens for 
 * morphological analysis.
 * 
 * <p>It may be useful for post processing by other programs to write out a corpus
 * that corrosponds to exactly the set of tokens that were read and processed using this class.
 * Use the <code>writeOutReadCorpus()</code> method for this
 * 
 * And Still, some 'character', ^K in my emacs display, appears to Java's regex as being,
 * or at least not not being, a Buckwalter character. So I had to manually remove one word
 * "^KdwlAr" (minus the double quotes) and place it in the 'impureBuckwalter' file.
 * 
 * @author cmonson
 * 
 * Added by R. Klic: 
 * maximum suffix length option
 *
 */
public class Corpus implements Serializable {

	private static final long serialVersionUID = 1L; 

	private File corpus;
	transient private BufferedReader corpusReader = null;
	
	private Language<?> language = null;
	
	// If both tokensToRead and typesToRead are set to 0 then the default is to read
	// from the corpus until the end of the corpus file is reached.
	
	// Only members of this package can change the data fields in this class.
	private int tokensToRead = 0;
	private int typesToRead  = 0;
	
	private Integer tokensToSkip = null;
	private Integer typesToSkip  = null;
	
	private boolean ignoreSGMLTags     = false;
	private boolean throwOutNumbers    = true;
	private boolean caseSensitive      = true;
	
	// all types as short or shorter than 'tooShortTypeLength' are discarded
	//
	// 5 is the magic number I set this at in my thesis. In fact, I never
	// did any experimentation to try to optimize the setting of this value.
	private int tooShortTypeLength = 5;  
	
	private String encoding           = "UTF-8";

	// TODO: Allow the user to change this.  For now it is hard coded.
	private String corpusLineCommentRegex = "\\|>-";
	
	// A HashMap<String, Integer> from each String that occurred 
	// in the corpus to the number of times that String occurred in the corpus
	private SortedMap<String, Integer> typesToFrequencies = new TreeMap<String, Integer>();

	private Integer maxSuffixLength = null;
	
	/**
	 * Default Corpus constructor.
	 */
	public Corpus() {}
	
	/**
	 * Set the path to the working corpus of text.
	 * 
	 * @param pathToCorpus A String path to the working corpus of text.
	 * 
	 */
	public Corpus(String pathToCorpus) {
		setPathToCorpus(pathToCorpus);
	}
	
	public void setPathToCorpus(String pathToCorpus) {
		corpus = new File(pathToCorpus);
		
		try {
		corpusReader = new BufferedReader(
						new InputStreamReader(
							new FileInputStream(corpus),
							encoding));
		}
		catch(FileNotFoundException e) {	
			System.err.println();
			System.err.println("  Sorry.  The file: " + corpus.getAbsolutePath());
			System.err.println("    could not be read.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println("  Did NOT successfully set the corpus path.");
			return;
		}
		catch(Exception e) {
			System.err.println();
			System.err.println("  Sorry.  While opening the file: " + corpus.getAbsolutePath());
			System.err.println("    an error was encountered.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println("  Did NOT successfully set the corpus path.");
			return;
		}
		
		System.err.println();
		System.err.println("The path to the text corpus has been set to:");
		System.err.println("  " + corpus.getAbsolutePath());
	}
	
	public File getCopyOfCorpusFile() {
		return new File(corpus.getAbsolutePath());
	}

	/**
	 * @return The number of tokens that will be read from the corpus
	 */
	public String getTokensOrTypesToRead() {
		if ((tokensToRead > 0) && (typesToRead > 0)) {
			throw new IllegalStateException("Both a number of tokens and a number of types to read have been specified.  This is an illegal state.");
		}
		if (tokensToRead > 0) {
			return tokensToRead + " Tokens";
		}
		if (typesToRead > 0) {
			return typesToRead + " Types";
		}
		return "NO Tokens or Types";
	}

	/**
	 * Set the number of tokens to read from the corpus.
	 * 
	 * <p>Overrides any previous number of tokens OR types that were set to be read
	 * 
	 * @param tokensToRead The number of tokens to read from the corpus
	 */
	public void setTokensToRead(int tokensToRead) {
		if (tokensToRead < 0) {
			throw new IllegalArgumentException("Cannot read a negative (" + tokensToRead
											   + ") number of tokens from a corpus");
		}
		
		System.err.println();
		System.err.println("All set to read " + tokensToRead + " tokens from the corpus:");
		System.err.println("  " + corpus.getAbsolutePath());
		
		if (typesToRead > 0) {
			System.err.println();
			System.err.println("WARNING: Ignoring the previous command to read " 
					           + typesToRead + " types.");
			System.err.println();
		}
		this.typesToRead = 0;
		this.tokensToRead = tokensToRead;
	}

	/**
	 * Set the number of tokens to read from the corpus.
	 * 
	 * <p>Overrides any previous number of tokens OR types that were set to be read
	 * 
	 * @param typesToRead The number of types to read from the corpus
	 */
	public void setTypesToRead(int typesToRead) {
		if (typesToRead < 0) {
			throw new IllegalArgumentException("Cannot read a negative (" + typesToRead
											   + ") number of tokens from a corpus");
		}		System.err.println();
		
		System.err.println("All set to read " + typesToRead + " types from the corpus:");
		System.err.println("  " + corpus.getAbsolutePath());
		
		if (tokensToRead > 0) {
			System.err.println();
			System.err.println("WARNING: Ignoring the previous command to read " 
					           + tokensToRead + " tokens.");
			System.err.println();
		}
		
		this.tokensToRead = 0;
		this.typesToRead = typesToRead;
	}

	/**
	 * @return The number of tokens that will be read from the corpus
	 */
	public String getTokensOrTypesToSkip() {
		if ((tokensToSkip != null) && (typesToSkip != null)) {
			throw new IllegalStateException("The current state declares that we must start reading " +
											"the corpus immediately after token number " + tokensToSkip + 
											" and that we must start reading the corpus immediately " +
											"after type number " + typesToSkip + 
											".  This is an illegal state.");
		}
		if (tokensToSkip != null) {
			return addEnglishOrdinateSuffix(tokensToSkip+1) + " Token";
		}
		if (typesToSkip != null) {
			return addEnglishOrdinateSuffix(typesToSkip+1) + " Type";
		}
		return "1st Token";
	}
	
	private String addEnglishOrdinateSuffix(int i) {
		String iAsString = Integer.toString(i);
		if (iAsString.matches("^.*1$")) {
			iAsString += "st";
		} else if (iAsString.matches("^.*2$")) {
			iAsString += "nd";
		} else if (iAsString.matches("^.*3$")) {
			iAsString += "rd";
		} else {
			iAsString += "th";
		}
		return iAsString;
	}

	/**
	 * Set the location at which to begin reading the corpus by specifying the token number of the 
	 * last token you do NOT wish to read.
	 * 
	 * <p>Overrides any previous corpus starting position that has been specified--independent
	 * of whether that previously specified starting position was in terms of tokens OR in terms
	 * of types.
	 * 
	 * @param tokensToSkip The token number of the last token you do NOT wish to read, or equivalently
	 *        The number of tokens to skip over at the beginning of the corpus.
	 */
	public void setTokensToSkip(int tokensToSkip) {
		if (tokensToSkip < 0) {
			throw new IllegalArgumentException("Cannot set the location at which to begin reading " +
											   "a corpus by specifying a *negative* number of tokens " +
											   "to not read (" + tokensToSkip + ")");
		}
		
		System.err.println();
		
		// Setting tokensToSkip to 0 is a special case
		if (tokensToSkip == 0) {
			System.err.println("Will start reading from the beginning of the corpus:");
		} else {
			System.err.println("Will start reading immediately after token number " + 
					           tokensToSkip + " in the corpus:");
		}
		System.err.println("  " + corpus.getAbsolutePath());

		if (typesToSkip != null) {
			System.err.println();
			System.err.println("WARNING: Ignoring the previous command to begin reading the corpus " +
							   "at the first occurance of a new type after the " + 
							   addEnglishOrdinateSuffix(typesToSkip) + " new type was encountered.");
			System.err.println();
		}
		
		this.typesToSkip = null;
		
		// setting tokensToSkip to 0 is a special case equivalent to resetting both tokensToSkip
		// and typesToSkip
		if (tokensToSkip == 0) {
			this.tokensToSkip = null;
		} else {
			this.tokensToSkip = tokensToSkip;
		}
	}

	/**
	 * Set the location at which to begin reading the corpus by specifying the type number of the
	 * last new type you do NOT wish to read.  In other words, if you state that the type number
	 * of the last new type you do NOT wish to read is N, then the corpus will be read starting from
	 * the first occurance of the N+1th type in the corpus.
	 * 
	 * <p>Overrides any previous corpus starting position that has been specified--independent
	 * of whether that previsously specified starting position was in terms of types OR in terms
	 * of tokens
	 * 
	 * @param typesToSkip The type number of the last new type you do NOT wish to read.
	 */
	public void setTypesToSkip(int typesToSkip) {
		if (typesToSkip < 0) {
			throw new IllegalArgumentException("Cannot set the location ath which to begin reading " +
											   "a corpus by specifying a *negative* number of types " +
											   "to not read (" + typesToSkip + ")");
		}		
		
		System.err.println();
		
		// Setting typesToSkip to 0 is a special case
		if (typesToSkip == 0) {
			System.err.println("Will start reading from the beginning of the corpus:");
		} else {
			System.err.println("Will start reading with the first occurance of a new type after the " +
							   addEnglishOrdinateSuffix(typesToSkip) + " new type in the corpus:");
		}
		System.err.println("  " + corpus.getAbsolutePath());
		
		if (tokensToSkip != null) {
			System.err.println();
			System.err.println("WARNING: Ignoring the previous command to begin reading immediately " +
							   "after token number " + tokensToSkip + " in the corpus.");
			System.err.println();
		}
		
		this.tokensToSkip = null;
		
		// Setting typesToSkip to 0 is a special case equivalent to resetting
		// both typesToSkip and tokensToSkip.
		if (typesToSkip == 0) {
			this.typesToSkip = null;
		} else {
			this.typesToSkip = typesToSkip;
		}
	}
	/**
	 * @return <code>true</code> if tokens that include numbers will be ignored when reading 
	 *         the corpus
	 *         <p><code>false</code> otherwise 
	 */
	public boolean isThrowOutNumbers() {
		return throwOutNumbers;
	}
	
	/**
	 * @return <code>true</code> if case will be ignored when reading the corpus
	 *         <p><code>false</code> otherwise 
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	
	public boolean isIgnoreSGMLTags() {
		return ignoreSGMLTags;
	}
	
	/**
	 * @param ignoreSGML 
	 * 
	 * <p> Pass <code>true</code> if you want to ignore SGML tags
	 * <p> Pass <code>false</code> if you want to include text within SGML tags
	 * 
	 * <p> What counts as an SGML tag is VERY simplistic: ALL TEXT between <code><</code> and
	 * <code>></code> symbols is 
	 * considered part of an SGML tag.
	 */
	public void setIgnoreSGMLTags(boolean ignoreSGMLTags) {
		this.ignoreSGMLTags = ignoreSGMLTags;
		
		System.err.println();
		if (this.ignoreSGMLTags) {
			System.err.println("*WILL* throw out all text in SGML tags, where an SGML tag is");
			System.err.println("  ANY text between '<' and '>'.");
		} else {
			System.err.println("Will *NOT* throw out text that looks like SGML tags, where an");
			System.err.println("  SGML tag is any text between '<' and '>'.");
		}
	}
	
	public void setTooShortTypeLength(Integer tooShortTypeLength) {
		this.tooShortTypeLength = tooShortTypeLength;
		
		System.err.println();
		if (this.tooShortTypeLength > 0) {
			System.err.println("*SKIPPING* all types as short or shorter than " + this.tooShortTypeLength);
		} else {
			System.err.println("No types will be discarded based on character length");
		}
	}
	
	/**
	 * @param throwOutNumbers 
	 * 
	 * <p> Pass <code>true</code> if you want to ignore tokens in the corpus
	 *     that include numbers.
	 * <p> Pass <code>false</code> if you want to include tokens in the
	 *     corpus that contain numbers.
	 */
	public void setThrowOutNumbers(boolean throwOutNumbers) {
		this.throwOutNumbers = throwOutNumbers;
		
		System.err.println();
		if (this.throwOutNumbers) {
			System.err.println("*WILL* throw out tokens from the corpus that contain digits");
		} else {
			System.err.println("Will *NOT* throw out tokens from the corpus " 
							   + "that contain digits");
		}
	}
	
	public String getCopyOfEncoding() {
		return new String(encoding);
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
		
		System.err.println();
		System.err.println("The corpus encoding has been set to: " + encoding);
	}
	
	/**
	 * @param caseSensitivity <code>true&nbsp;</code> to retain case in the corpus
	 *                    <br><code>false</code> to throw out case information in the corpus
	 */
	public void setCaseSensitivity(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		
		System.err.println();
		if (this.caseSensitive) {
			System.err.println("RETAINING case information in the corpus");
		} else {
			System.err.println("IGNORING case information in the corpus");
		}
	}

	/**
	 * @return A Set view of the vocabulary read from this corpus. 
	 */
	public SortedSet<String> getVocabulary() {
		// Is this a potential bug?  The javadoc says that "The [returned] set is
		// backed by the map, so changes to the map are reflected in the set, and 
		// vice-versa."
		//Set<String> vocabulary = typesToFrequencies.keySet();
		SortedSet<String> vocabulary = new TreeSet<String>(typesToFrequencies.keySet());
		return vocabulary;
	}
	
	public PairList<String, Integer> getVocabByFrequency() {
		PairList<String, Integer> vocabularyByFrequency = 
			PairList.pairListFromMap(typesToFrequencies);
		
		Collections.sort(vocabularyByFrequency, new ComparablePair.ByRightDecreasing<String, Integer>());
		
		return vocabularyByFrequency;
	}
	
	/**
	 * $return The number of unique types read from this corpus.
	 * 
	 * @author cmonson
	 *
	 */
	public int getVocabularySize() {
		return typesToFrequencies.size();
	}
	
	private class ComparatorByDecreasingFrequency implements Comparator<String> {

		/**
		 * Compare two Strings by their frequency in the containing corpus.
		 * 
		 * Frequent Strings are placed BEFORE less frequent strings.
		 * 
		 * @param type1 A String to examine for frequency
		 * @param type2 A String to examine for frequency
		 * 
		 * @return <code>-1</code> if type1 is more frequent than type2
		 * 	   <br><code>&nbsp;0</code> if type1 is just as frequent as type2
		 * 	   <br><code>&nbsp;1</code> if type1 is less freqquent than type2 
		 *  
		 * @see java.util.Comparator#compare(T, T)
		 */
		public int compare(String type1, String type2) {
			int type1Count;
			int type2Count;
			if ( ! typesToFrequencies.containsKey(type1)) {
				type1Count = 0;
			} else {
				type1Count = typesToFrequencies.get(type1);
			}
			if ( ! typesToFrequencies.containsKey(type2)) {
				type2Count = 0;
			} else {
				type2Count = typesToFrequencies.get(type2);
			}
			
			if (type1Count < type2Count) {
				return 1;
			}
			if (type1Count > type2Count) {
				return -1;
			}
			
			return type1.compareTo(type2);
		}
	}
	// Get an array containing the keys of this.types sorted in decreasing order by
	// frequency.
	private ArrayList<String> getTypesSortedByFrequency() {
		ArrayList<String> typesSortedByFrequency = new ArrayList<String>();		
		typesSortedByFrequency.addAll(typesToFrequencies.keySet());
		Collections.sort(typesSortedByFrequency, new ComparatorByDecreasingFrequency());
		
		return typesSortedByFrequency;
	}
	
	
	/**
	 * NOT YET IMPLEMENTED.  Will write out the exact same corpus that was read in using the
	 * <code>readCorpus()</code> method.
	 */
	public String getVocabularyAsString() {
		StringBuilder vocabularyAsStringBuilder = new StringBuilder();
		
		for (String type : typesToFrequencies.keySet()) {
			vocabularyAsStringBuilder.append(type + String.format("%n"));
		}
		
		return vocabularyAsStringBuilder.toString();
	}
	

	// This enum is used in collateTypes() and its helper functions.
	private enum ReadingState {SKIPPING, READING};

	// TODO: Let users set the corpus line comment token
	//
	// Although it is a bit ugly to do so, I handle both skipping tokens from the beginning
	// of the corpus as well as reading tokens from the corpus in this single function.
	// I do this because I want to be sure that skipping tokens and reading tokens have 
	// identical behavior as far as blank lines, throwing out numbers, case sensitivity, etc.
	//
	// TODO:
	// Reading a Corpus might benefit from a considerably more abstract implementation where
	// we have generic getNextToken() and putBackToken() functions that know about skipping numbers, 
	// case sensitiveity, etc., but separate GoToPosInCorpus() and readCorpus() functions.
	/**
	 * Read in a corpus keeping track of the counts of all the types that occur in the
	 * corpus.  The tokenization of a corpus depends on the Language that is passed.
	 * A language defines how to tokenize a line of text.  But a Corpus does define knowledge
	 * of a line comment string and case sensitivity.
	 * 
	 * @return <code>false</code> if colating failed.  For example the language of the corpus or
	 *                            the path to the corpus may not have been set yet.
	 * @throws IOException 
	 */
	public boolean 
	collateTypes(PrintWriter readCorpusStrippedOfNonAlphaSequences_PrintWriter) 
		throws IOException {
	
		int DEBUG = 0;
		
		if ( ! readyToCollate()) {
			return false;
		}
		
		
		ReadingState readingState = getStartingReadingState();
		
		String line;
		int typeCounter = 0;
		int tokenCounter = 0;
		int typesSkipped = 0;
		int tokensSkipped = 0;
		
		// label this while so I can jump out of it once I have read the exact number
		// of types or tokens that I need to.
		getMoreTokens: while ((line = corpusReader.readLine()) != null) {
			
			// skip lines of the corpus that begin with the corpusLineCommentTag
			if (line.matches("\\s*" + corpusLineCommentRegex + ".*")) {
				continue;  
			}
			// skip blank lines
			if (line.matches("\\s*")) {
				continue;  
			}
			// Make everything lower case if we should
			if ( ! caseSensitive) {
				line = line.toLowerCase();
			}
			
			List<String> tokens = language.tokenize(line, ignoreSGMLTags);
			
			for (String token : tokens) {
				// throw out numbers if this option is set
				if (throwOutNumbers && token.matches("^.*\\d.*$")) {
					continue;
				}
				
				if (token.length() <= tooShortTypeLength) {
					continue;
				}
				
				// checks if there is space yet to read (or skip) this token
				boolean spaceIsAvailableForToken = false;
				
				// This 'if' is a bit ugly but if we are in a skipping state we need to first
				// skip the correct number of tokens
				if (readingState == ReadingState.SKIPPING) {
					spaceIsAvailableForToken = 
						isSpaceAvaliableForToken(token, typeCounter, tokenCounter, readingState);
					if (spaceIsAvailableForToken) {
						tokenCounter++;
						typeCounter = acceptToken(typeCounter, token);
						
						if ((tokenCounter % 10000) == 0) {
							System.err.println("Have Skipped: " 
											   + tokenCounter + " Tokens, " 
											   + typeCounter + " Types");
						}
						
						// and go on to the next token
						continue;
						
					} else {
						// We should Not skip the current token but instead read it, so
						// Change the state to reading tokens and reset token and type counters, etc.
						readingState = ReadingState.READING;
						typesSkipped = typeCounter;
						tokensSkipped = tokenCounter;
						typeCounter = 0;
						tokenCounter = 0;
						typesToFrequencies = new TreeMap<String, Integer>();
					}
				}
				
				// We need to fall through to here when we find that the currrent token
				// should in fact not be skipped but read!
				
				spaceIsAvailableForToken =
					isSpaceAvaliableForToken(token, typeCounter, tokenCounter, readingState);
				if ( ! spaceIsAvailableForToken) {
					break getMoreTokens;  // jump out of the while loop
				}
				
				tokenCounter++;
				typeCounter = acceptToken(typeCounter, token);
				
				if (readCorpusStrippedOfNonAlphaSequences_PrintWriter != null) {
					readCorpusStrippedOfNonAlphaSequences_PrintWriter.print(token + " ");
				}
				
				if ((tokenCounter % 10000) == 0) {
				//if ((tokenCounter % 1) == 0) {
					System.err.println("Have Read: " 
									   + tokenCounter + " Tokens, " 
									   + typeCounter + " Types");
				}
			}
			
			if (readCorpusStrippedOfNonAlphaSequences_PrintWriter != null) {
				readCorpusStrippedOfNonAlphaSequences_PrintWriter.println();
			}
		}
		
		System.err.println(String.format("  In total, Skipped: %10d Tokens, %10d Types",
				   		   				 tokensSkipped, 
				   		   				 typesSkipped));
		System.err.println(String.format("  In total, Read:    %10d Tokens, %10d Types",
						   				 tokenCounter, 
						   				 typeCounter));
		
		if (DEBUG > 0) {
			ArrayList<String> typesByFrequency = getTypesSortedByFrequency();
			
			System.out.println("The types in this corpus are:");
			for (String type : typesByFrequency) {
				System.out.println("  " + type + " : " + typesToFrequencies.get(type));
			}
		}
		
		if (readCorpusStrippedOfNonAlphaSequences_PrintWriter != null) {
			readCorpusStrippedOfNonAlphaSequences_PrintWriter.close();
		}
		
		return true;
	}

	private boolean readyToCollate() {
		if (language == null) {
			System.err.println();
			System.err.println("Sorry.  Cannot yet read and collate the corpus: ");
			System.err.println();
			System.err.println(this);
			System.err.println();
			System.err.println("Because the current language is not specified.");
			return false;
		}
		
		if (corpusReader == null) {
			System.err.println();
			System.err.println("Sorry.  Cannot yet read and collate the corpus: ");
			System.err.println();
			System.err.println(this);
			System.err.println();
			System.err.println("Because the path to the corpus has not yet been specified.");
			return false;
		}
		
		return true;
	}

	/**
	 * @param typeCounter
	 * @param token
	 * @return
	 */
	private int acceptToken(int typeCounter, String token) {
		if ( ! typesToFrequencies.containsKey(token)) {
			typeCounter++;
			typesToFrequencies.put(token, 0);
		}
		
		typesToFrequencies.put(token, typesToFrequencies.get(token)+1);
		
		return typeCounter;
	}

	private ReadingState getStartingReadingState() {
		if ((tokensToSkip == null) && (typesToSkip == null)) {
			return ReadingState.READING;
		}
		return ReadingState.SKIPPING;
	}

	/**
	 * Decides based on 1) the number of tokens and types that have been read (or skipped) 
	 * from the corpus, together with 2) the limits that have been set on the number of 
	 * tokens or types that should be read (or skipped) from the corpus whether or not to 
	 * read (or skip) the candidate token.
	 * 
	 * @param token The token we are considering officially reading from the corpus
	 * @param currentTypeCount The number of types that have been read from the corpus so far
	 * @param currentTokenCount The number of tokens that have been read from the corpus so far
	 * @param readingState Specifies whether we are reading or skipping tokens in the corpus.
	 * @return <code>true&nbsp;</code> if this token should be read from the corpus
	 *      <p><code>false</code>      if this token should not be read from the corpus
	 */
	private boolean isSpaceAvaliableForToken(String token, 
									  int currentTypeCount, 
									  int currentTokenCount,
									  ReadingState readingState) {
		
		// make sure that we are not in a bad state where both a specific number of tokens
		// and a specific number of types are specified to be skipped (or read).
		checkTokenTypeConsistency(readingState);
		
		// The action is different in the default case depending on the readingState:
		//   If we are skipping tokens in the corpus then a default setting of
		//     tokensToSkip == typesToSkip == null means there is NO more space to
		//     skip any more tokens.
		//   If we are reading tokens from the corpus then a default setting of
		//     tokensToRead == typesToRead == 0 means there is always more space to
		//     read more tokens from the corpus--we should read until the end of the file.
		switch (readingState) {
		case SKIPPING:
			if ((tokensToSkip == null) && (typesToSkip == null)) {
				return false;
			}			
			break;
		case READING:
			if ((tokensToRead == 0) && (typesToRead == 0)) {
				return true;
			}
			break;
		default:
			throw new IllegalStateException("Should never reach here (1)");
		}

		Integer tokenLimit = null;
		Integer typeLimit = null;
		switch (readingState) {
		case SKIPPING:
			tokenLimit = tokensToSkip;
			typeLimit = typesToSkip;			
			break;
		case READING:
			tokenLimit = tokensToRead;
			typeLimit = typesToRead;			
			break;
		default:
			throw new IllegalStateException("Should never reach here (2)");
		}
		
		// If a specified number of tokens are to be read (or skipped)
		if ((tokenLimit != null) && (tokenLimit > 0)) {
			if (currentTokenCount < tokenLimit) {
				return true;
			}
			return false;
		}
		
		// If a specified number of types are to be read (or skipped)
		if ((typeLimit != null) && (typeLimit > 0)) {
			if (currentTypeCount < typeLimit) {
				return true;
			}
			if (typesToFrequencies.containsKey(token)) {
				return true;
			}
			return false;
		}

		throw new RuntimeException("Should never reach here either");
	}

	/**
	 * @param readingState
	 */
	private void checkTokenTypeConsistency(ReadingState readingState) {
		// Throw an exception if we are told to skip (or read) both a specific number of tokens
		// and a specific number of types.
		if (readingState == ReadingState.SKIPPING) {
			if ((tokensToSkip == null) && (typesToSkip == null)) {
				throw new IllegalStateException("The current state declares that we must start reading " +
						"the corpus immediately after token number " + tokensToSkip + 
						" and that we must start reading the corpus immediately " +
						"after type number " + typesToSkip + 
						".  This is an illegal state.");
			}
		} else {
			if ((tokensToRead > 0) && (typesToRead > 0)) {
				throw new IllegalStateException("this Corpus believes that " + tokensToRead 
												+ " tokens are to be read AND that " + typesToRead
						 						+ " types are to be read.  "
						 						+ "This is an illegal state");
			}
		}
	}

	public String toString() {
		String settings = "";
		if (corpus != null) {
			settings += String.format("Corpus File:                " + corpus.getAbsolutePath() + "%n");
		} else {
			settings += String.format("Corpus File:                Not Yet Specified%n");
		}
		if (language != null) {
			settings += String.format("Language:                   " + language.toString() + "%n");
		} else {
			settings += String.format("Language:                   Not Yet Specified%n");
		}
		settings += String.format("Encoding:                   " + encoding + "%n");
		settings += String.format("Skipping:                   " + getTokensOrTypesToSkip() + "%n");
		settings += String.format("Reading:                    " + getTokensOrTypesToRead() + "%n");
		settings += String.format("Throw Out Numbers:          " + isThrowOutNumbers() + "%n");
		settings += String.format("Case Sensitive:             " + isCaseSensitive() + "%n");
		settings += String.format("Skip Type if Length <= :    " + tooShortTypeLength + "%n");
		settings += String.format("Ignore SGML tags:           " + isIgnoreSGMLTags());
		return settings;
	}

	public String toStringAsComment() {
		String settings = "";
		settings += String.format("# Corpus File:                " + corpus.getAbsolutePath() + "%n");
		if (language != null) {
			settings += String.format("# Language:                   " + language.toString() + "%n");
		}
		settings += String.format("# Encoding:                   " + encoding + "%n");
		settings += String.format("# Skipping:                   " + getTokensOrTypesToSkip() + "%n");
		settings += String.format("# Reading:                    " + getTokensOrTypesToRead() + "%n");
		settings += String.format("# Throw Out Numbers:          " + isThrowOutNumbers() + "%n");
		settings += String.format("# Case Sensitive:             " + isCaseSensitive() + "%n");
		settings += String.format("# Skip Type if Length <= :    " + tooShortTypeLength + "%n");
		settings += String.format("# Ignore SGML tags:           " + isIgnoreSGMLTags());
		return settings;
	}

	public Language<?> getLanguage() {
		return language;
	}

	public void setLanguage(String languageName) {
		Language<?> language = Language.getNewLanguage(languageName);
		setLanguage(language);
	}
	
	public void setLanguage(LanguageName languageName) {
		Language<?> language = Language.getNewLanguage(languageName);
		setLanguage(language);
	}
	
	public void setLanguage(Language<?> language) {
		this.language = language;
		
		System.err.println();
		System.err.println("The language has been set to: " + language);
	}

	public Integer getMaxSuffixLength() {
		return maxSuffixLength;
	}

	public void setMaxSuffixLength(Integer maxSuffixLength) {
		this.maxSuffixLength = maxSuffixLength;
	}

	public SortedSet<Character> getAllCharactersThatBeginSomeWord() {
		
		System.out.println();
		System.out.println("Collecting all initial characters in the vocabulary...");
		
		SortedSet<Character> charactersThatBeginSomeVocabWord = new TreeSet<Character>();
		
		SortedSet<String> vocabulary = getVocabulary();
		for (String vocabItem : vocabulary) {
			Character leadChar = vocabItem.charAt(0);
			charactersThatBeginSomeVocabWord.add(leadChar);
		}
		
		System.out.println("  Done collecting initial characters");
		System.out.println();
		
		return charactersThatBeginSomeVocabWord;
	}

}
