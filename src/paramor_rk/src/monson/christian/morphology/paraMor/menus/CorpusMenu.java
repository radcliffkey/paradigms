/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.languages.Language.LanguageName;
import monson.christian.util.ComparablePair;
import monson.christian.util.PairList;

public class CorpusMenu {

	private static final String MENU_NAME = "Corpus Menu";
	
	private BufferedReader stdin;

	private Corpus corpus;
	
	private File readCorpusStrippedOfNonAlphaSequences_File = null;
	private PrintWriter readCorpusStrippedOfNonAlphaSequences_PrintWriter = null;
	
	private String previousMenuName;

	
	
	public CorpusMenu(BufferedReader stdin, 
					  Corpus corpus, 
					  String previousMenuName) {
		this.stdin = stdin;
		this.corpus = corpus;
		this.previousMenuName = previousMenuName;		
	}

	
	public void present(String instructions) throws IOException {
		
		boolean continueLoop = true;
		String choice;
		do {
			printOptions(instructions);
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			choice = choice.toLowerCase();
			
			if (choice.matches("setcp")) {
				doSetCorpusPath();
								
			} else if (choice.matches("setl")) {
				doSetLanguage();
				
			} else if (choice.matches("setd")) {
				doSetThrowingOutDigits();
				
			} else if (choice.matches("sete")) {
				doSetEncoding();
				
			} else if (choice.matches("setcs")) {
				doSetCaseSensitivity();
				
			} else if (choice.matches("settl")) {
				doSetTypeLengthToRemove();
				
			} else if (choice.matches("setisgml")) {
				doSetIgnoreSGML();
				
			} else if (choice.matches("settokts")) {
				doSetTokensToSkip();
				
			} else if (choice.matches("settoktr")) {
				doSetTokensToRead();
				
			} else if (choice.matches("settypts")) {
				doSetTypesToSkip();
				
			} else if (choice.matches("settyptr")) {
				doSetTypesToRead();
				
			} else if (choice.matches("setwoec")) {
				doSetWriteOutExactCorpus();
				
			} else if (choice.matches("rc")) {
				boolean succeeded = doReadCorpus();
				if (succeeded) {
					continueLoop = false;
				}
				
			} else if (choice.matches("wcv")) {
				doWriteCorpusVocabulary();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetCorpusPath() {
		System.err.println();
		System.err.println("Please enter the fully qualified path of the corpus you wish to read.");
		System.err.println();
		System.err.print("> ");
		
		String path;
		try {
			path = stdin.readLine();
		}
		catch (IOException e) {path = "<" + e + ">";}  // This should never happen
		
		System.err.println();
		System.err.println("path: |" + path + "|");
		System.err.println();
		
		corpus.setPathToCorpus(path);
		
		System.err.println();
		System.err.println("Press Enter to Continue...");
		try {
			path = stdin.readLine();
		}
		catch (IOException e) {path = "<" + e + ">";}  // This should never happen
		
	}

	private void doSetLanguage() {
		
		SelectAnEnumValueMenu<LanguageName> selectAnEnumValueMenu = 
			new SelectAnEnumValueMenu<LanguageName>(
					LanguageName.class, 
					stdin, 
					MENU_NAME,
					"Please Select a Language");
		LanguageName languageName = selectAnEnumValueMenu.present();
		if (languageName != null) {
			corpus.setLanguage(languageName);
		}
		
		System.err.println();
		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetThrowingOutDigits() {
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin, 
								 MENU_NAME, 
								 "If you wish to IGNORE all tokens that contain an Arabic numeral " +
								 "(i.e. the characters [0123456789]) when reading this corpus " +
								 "then choose True, if not, then choose False.");
		Boolean throwOutDigits = booleanInputMenu.present();
		if (throwOutDigits != null) {
			corpus.setThrowOutNumbers(throwOutDigits);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}
	
	private void doSetTypeLengthToRemove() {
		String instructions = "";
		instructions += 
			String.format("Enter an integer character length. The network will%n");
		instructions += 
			String.format(" only be built from types LONGER than the entered length");
		
		NonNegativeIntegerInputMenu nonNegativeInputMenu = 
			new NonNegativeIntegerInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Integer tooShortTypeLength = nonNegativeInputMenu.present();
		
		if (tooShortTypeLength != null) {
			corpus.setTooShortTypeLength(tooShortTypeLength);
		}
		
		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}
	
	private void doSetIgnoreSGML() {
		String instructions = "";
		instructions += String.format("If you wish to IGNORE all tokens contained within SGML tags%n");
		instructions += String.format(" then chose True, if not, then choose False.");
		
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Boolean ignoreSGML = booleanInputMenu.present();
		if (ignoreSGML != null) {
			corpus.setIgnoreSGMLTags(ignoreSGML);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetEncoding() {
		System.err.println();
		System.err.println("Sorry.  Not Yet Implemented.");
		System.err.println();
		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetCaseSensitivity() {
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin, 
								 MENU_NAME, 
								 "If you wish to PRESERVE case (i.e. do NOT lowercase all tokens) " +
								 "when reading this corpus " +
								 "then choose True, if not, then choose False.");
		Boolean caseSensitive = booleanInputMenu.present();
		if (caseSensitive != null) {
			corpus.setCaseSensitivity(caseSensitive);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetTokensToSkip() {
		String instructions = "";
		instructions += String.format("Enter the number of tokens you would like to skip over%n");
		instructions += String.format(" before reading in tokens from this corpus file.");
		
		NonNegativeIntegerInputMenu nonNegativeInputMenu = 
			new NonNegativeIntegerInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Integer tokensToSkip = nonNegativeInputMenu.present();
		if (tokensToSkip != null) {
			corpus.setTokensToSkip(tokensToSkip);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetTokensToRead() {
		String instructions = "";
		instructions += String.format("Enter the number of tokens you would like to read%n");
		instructions += String.format(" from this corpus file.");
		
		NonNegativeIntegerInputMenu nonNegativeInputMenu = 
			new NonNegativeIntegerInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Integer tokensToRead = nonNegativeInputMenu.present();
		if (tokensToRead != null) {
			corpus.setTokensToRead(tokensToRead);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetTypesToSkip() {
		String instructions = "";
		instructions += String.format("Enter the number of types you would like to skip over%n");
		instructions += String.format(" before reading in tokens from this corpus file.");
		
		NonNegativeIntegerInputMenu nonNegativeInputMenu = 
			new NonNegativeIntegerInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Integer typesToSkip = nonNegativeInputMenu.present();
		if (typesToSkip != null) {
			corpus.setTypesToSkip(typesToSkip);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}

	private void doSetTypesToRead() {
		String instructions = "";
		instructions += String.format("Enter the number of types you would like to %n");
		instructions += String.format(" read from this corpus file.");
		
		NonNegativeIntegerInputMenu nonNegativeInputMenu = 
			new NonNegativeIntegerInputMenu(
					stdin, 
					MENU_NAME, 
					instructions);
		Integer typesToRead = nonNegativeInputMenu.present();
		if (typesToRead != null) {
			corpus.setTypesToRead(typesToRead);
		}

		System.err.println("Press Enter to Continue...");
		try {
			stdin.readLine();
		}
		catch (IOException e) {}  // This should never happen
	}
	

	private void doSetWriteOutExactCorpus() {
		String instructions = "Please specify a file to write out the exact corpus read in " +
		                      "(i.e. stripped of numbers, punctuation, SGML tags, etc.";
		
		FileInputMenu fileInputMenu = new FileInputMenu(stdin, MENU_NAME, instructions);
		readCorpusStrippedOfNonAlphaSequences_File = fileInputMenu.present();
		if (readCorpusStrippedOfNonAlphaSequences_File == null) {
			return;
		}
		
		try {
			readCorpusStrippedOfNonAlphaSequences_PrintWriter = 
				new PrintWriter(
						new BufferedWriter(
								new FileWriter(
										readCorpusStrippedOfNonAlphaSequences_File)),
						true); // true to autoflush
			
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + readCorpusStrippedOfNonAlphaSequences_File.getAbsolutePath());
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
		}
	}
	
	private boolean doReadCorpus() throws IOException {
		return corpus.collateTypes(readCorpusStrippedOfNonAlphaSequences_PrintWriter);
	}

	private void doWriteCorpusVocabulary() {
		String instructions = "Please specify a file to write out the vocabulary of the current corpus";
		FileInputMenu fileInputMenu = new FileInputMenu(stdin, MENU_NAME, instructions);
		File outFile = fileInputMenu.present();
		if (outFile == null) {
			return;
		}
		
		PrintWriter outVocab = null;
		try {
			outVocab =
				new PrintWriter(
						new BufferedWriter(
								new OutputStreamWriter(
										new FileOutputStream(outFile),
										"UTF-8")),
										true); //true to autoflush
			
			//outVocab = new PrintWriter(new BufferedWriter(new FileWriter(outFile)),
			//					         true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
		}
		
		instructions = 
			"Do you want to sort by frequency and include frequencies" + String.format("%n") +
			"  in the output, or do you want to sort alphabetically and" + String.format("%n") +
			"  only output the word types?" + String.format("%n") +
			String.format("%n") +
			"  By Frequency: true" + String.format("%n") +
			"  Types Only:   false" + String.format("%n");
		
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin, MENU_NAME, instructions);
		Boolean byFrequency = booleanInputMenu.present();
		if (byFrequency == null) {
			return;
		}

		outVocab.println(corpus);
		outVocab.println();
		
		if (byFrequency) {
			PairList<String, Integer> vocabularyByFrequency = corpus.getVocabByFrequency();
			for (ComparablePair<String, Integer> wordWithFreq : vocabularyByFrequency) {
				outVocab.println(wordWithFreq.getRight() + "\t" + wordWithFreq.getLeft());
			}
			
		} else {
			String vocabString = corpus.getVocabularyAsString();
			outVocab.println(vocabString);
		}
		outVocab.flush();
		outVocab.close();
	}

	public void printOptions(String instructions) {
		System.err.println();
		System.err.println("----------------------------------------------");
		System.err.println("   " + MENU_NAME);
		System.err.println();
		System.err.println(instructions);
		System.err.println("----------------------------------------------");
		System.err.println();
		System.err.println(corpus);
		System.err.println();
		System.err.print("Write out the read input corpus stripped of non-alpha sequences: ");
		if (readCorpusStrippedOfNonAlphaSequences_PrintWriter == null) {
			System.err.println("NO");
		} else {
			System.err.println("YES");
			System.err.println("  Will write to: " + readCorpusStrippedOfNonAlphaSequences_File);
		}
		System.err.println("");
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetCP>    <Set> the <C>orpus <P>ath -- Specify the physical location of the corpus");
		System.err.println("  <SetL>     <Set> the <L>anguage for this corpus");
		System.err.println("  <SetD>     <Set> throwing out <D>igits");
		System.err.println("  <SetE>     <Set> the corpus <E>ncoding");
		System.err.println("  <SetCS>    <Set> <C>ase <S>ensitiveity - Turn case sensitivity on and off.");
		System.err.println("  <SetTL>    <Set> <T>ype <L>ength - Only keep types LONGER than the length specified");
		System.err.println("  <SetISGML> <Set> <I>gnore <SGML> tags");
		System.err.println();
		System.err.println("  <SetTokTS> <Set> <Tok>ens <T>o <S>kip");
		System.err.println("  <SetTokTR> <Set> <Tok>ens <T>o <R>ead");
		System.err.println("  <SetTypTS> <Set> <Typ>es <T>o <S>kip");
		System.err.println("  <SetTypTR> <Set> <Typ>es <T>o <R>ead");
		System.err.println();
		System.err.println("  <SetWOEC>  <Set> <W>rite <O>ut <E>xact <C>orpus. Use this command to set the name");
		System.err.println("              of a file to contain the read in corpus stripped of non-alpha sequences.");
		System.err.println("              (i.e. stripped of punctuation, numbers, SGML tags, etc.)");
		System.err.println("              Don't use this command if you don't want to write out a file containing");
		System.err.println("              the exact corpus that you read in.");
		System.err.println();
		System.err.println("  <RC> <R>ead in a <C>orpus with the current settings.");
		System.err.println();
		System.err.println("  <WCV> <W>rite out the <C>orpus <V>ocabulary that was read in.");
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
