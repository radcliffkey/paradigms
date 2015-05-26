/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.initialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import monson.christian.morphology.paraMor.ParaMor;

/**
 * Parses and interprets an initialization file for MorphologyInducer
 * 
 * An initialization file is simply a text file with various specific commands
 * that InitFile understands.
 * 
 * Valid commands include:
 * 
 * corpus <path-to-text-corpus>
 * 
 * graphsDir <path-to-directory>
 *   <path-to-directory> : The name of the directory in
 *   which to store graphics (pictures) of portions
 *   of the morphology scheme network that is built");
 * 
 * typeCutoff <integer>
 *   Where <integer> is the exact number of unique types to read
 *     from the corpus
 *     
 * tokenCutoff <integer>
 *   Where <integer> is the exact number of tokens to read
 *     from the corpus
 *     
 * language <language-name>
 *   Where the valid <language-name>'s are the strings defined in Language  
 */

/*
 * To define a new command to be interpreted by InitFile follow the examples of the
 * other commands already defined.  Basically the steps are:
 * 
 * 1) Define a new subclass of the InitFile.Command class
 * 2) Implement the getCommandUsage() and parseCommand() methods of the new InitFileCommand class
 * 3) Add a new case to the CommandFactory.getCommand() method.
 * 4) Add lines on the syntax of a command to the printHelp() methodTokensToReadCommand.java
 * 
 */
public class InitFile {

	private File initFile;
	private BufferedReader initFileStream;
	
	/**
	 * Prepares to read the file named in <code>initFilename</code> as a MorphologyInducer
	 * initialization file.
	 * 
	 * @param initFilename a filename as a string
	 */
	public InitFile (String initFilename) {
		this();
		
		initFile = new File(initFilename);
		
		BufferedReader tempStream = null;
		try {
			tempStream = new BufferedReader(new FileReader(initFile.getAbsolutePath()));
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("  The file: " + initFile.getAbsolutePath() + " could not be found");
			System.err.println("  " + e.getMessage());
			System.err.println();
			System.err.println("  Failed initializing the InitFile: " + toString());
			System.err.println();
		}
		initFileStream = tempStream;
	}
	
	/**
	 * Initialize a basic InitFile
	 */
	public InitFile() {	}

	/** 
	 * @return the absolute path to this initializations file.
	 */
	@Override 
	public String toString() {
		return initFile.getAbsolutePath();
	}
	
	/**
	 * Gives the format of an initializations file on <code>System.out</code>.
	 */
	public static void printHelp() {
		System.err.println();
		System.err.println("The format of an initializations (init) file:");
		System.err.println();
		System.err.println("One command per line.");
		System.err.println("'#' is the line comment character.");
		System.err.println();
		System.err.println("Setup Commands");
		System.err.println("--------------");
		System.err.println();
		System.err.println(CorpusCommand.getCommandUsage());
		System.err.println();
		System.err.println(ThrowOutNumbersCommand.getCommandUsage());
		System.err.println();
		System.err.println(CorpusEncodingCommand.getCommandUsage());
		System.err.println();
		System.err.println(CaseSensitiveCommand.getCommandUsage());
		System.err.println();
		System.err.println(LanguageCommand.getCommandUsage());
		System.err.println();
		System.err.println(TokensToReadCommand.getCommandUsage());
		System.err.println();
		System.err.println(TypesToReadCommand.getCommandUsage());
		System.err.println();
		System.err.println(SetCorpusStartingPositionByTokenCommand.getCommandUsage());
		System.err.println();
		System.err.println(SetCorpusStartingPositionByTypeCommand.getCommandUsage());
		System.err.println();
	}

	/**
	 * Reads an initializations file for morphology induction with morphology scheme networks.
	 * As this InitFile is read, the different commands in this InitFile may change the state
	 * of morphologyInducer.
	 * 
	 * @param morphologyInducer The result of reading an InitFile is to change the state of
	 * 							morphologyInducer.  
	 * 
	 * @throws IOException
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public void 
	readInitFile(ParaMor morphologyInducer) throws IOException {
		boolean encounteredParseErrors = false;
		boolean parseSucceeded = true;
		boolean languageWasSpecified = false;
		
		System.err.println();
		System.err.println("Reading the initializations file: " + initFile.getAbsolutePath());
		
		String line;
		int lineNum=0;
		while ((line = initFileStream.readLine()) != null) {
			
			String lcLine = line.toLowerCase();
			lineNum++;
			
			if (lcLine.matches("\\s*#.*")) {       // line comment
				continue;  //Move to the next line
				
			} else if (lcLine.matches("\\s*")) {   // blank line
				continue;  //Move to the next line
				
			} 
			
			AbstractInitFileCommand command = 
				CommandFactory.getCommand(line, 
				 						  initFile.getAbsolutePath(), 
										  lineNum);
			
			if (command instanceof LanguageCommand) {
				languageWasSpecified = true;
			}
			
			parseSucceeded = command.parseCommand(morphologyInducer,
												  line,
												  initFile.getAbsolutePath(),
												  lineNum);
			if ( ! parseSucceeded) {
				encounteredParseErrors = true;
			}
		}
		
		System.err.println();
		
		if (encounteredParseErrors) {
			System.err.println();
			System.err.println("A fatal syntax error occured in the initializations file:");
			System.err.println("  " + this.initFile.getAbsolutePath());
			System.err.println();
			System.err.println("See above output for details.  Exiting...");
			System.exit(0);
		}
		
		if ( ! languageWasSpecified) {
			System.err.println();
			System.err.println("No language was specified with the initializations file command:");
			System.err.println();
			System.err.println(LanguageCommand.getCommandUsage());
			System.err.println();
			System.err.println("But a language must be specified.  Exiting...");
			System.exit(0);
		}

		System.err.println();
		System.err.println("Done Reading init file");
		System.err.println("------------------------------------");
		System.err.println();
	}

}
