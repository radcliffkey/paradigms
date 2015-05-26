/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import monson.christian.morphology.paraMor.initialization.InitFile;
import monson.christian.morphology.paraMor.languages.Language;
import monson.christian.morphology.paraMor.menus.MainMenu;
import monson.christian.morphology.paraMor.networks.PartialOrderNetwork;

/**
 * The home and main class defining morphology induction using morphology scheme networks.
 * 
 * @author Christian Monson
 *
 */
public class ParaMor {

	private static final String versionStatement =
		"  ParaMor v0.1 -- (c) July 2010, Christian Monson";
	
	// NEVER set the corpus directly but always call resetCorpus() and beginNew_XXX_Network()
	// (where XXX is a type of network (Static Sparse, Dynamic Sparse, Dynamic Dense, etc.)).
	// These functions ensure that this.corpus and this.partialOrderNetwork.schemeCollection.corpus 
	// always point to the identical corpus object
	//
	// similarly there is NO getCorpus() function.  There *is* a getPartialOrderNetwork() function
	// for ease of use.  We don't want to go around copying the giant network.  For now I will assume
	// that people will play nicely when they getPartialOrderNetwork() and NOT change the corpus.
	private Corpus corpus = null;
	private PartialOrderNetwork partialOrderNetwork = null;
	
	/**
	 * Start morphology induction using the morphology scheme network approach.
	 * 
	 * @param args  
	 * <p> The command line syntax is very simple:
	 * 
	 * <p> java MorphologyInducer -if &ltinitFilename&gt
	 * 
	 * <p> All the detailed initialization stuff is pushed off into the initFile.
	 * Ommitting the -if flag gives a help message.
	 * @throws IOException 
	 * 
	 * @throws IOException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws IOException {
		
		System.err.println();
		System.err.println(versionStatement);
		System.err.println();
		
		InitFile initFile = checkSyntax(args);
		
		@SuppressWarnings("unused") 
		ParaMor paraMor = new ParaMor(initFile);
		
		System.err.println();
		System.err.println("That was fun.  Now we're all done.");
		System.err.println();
	}
	
	private ParaMor(InitFile initFile) throws IOException {
		//TODO: document the circular execution of initFile.readInitFile.
		//      MorphologyInducer.corpus, etc. are initialized by commands in the
		//      initializations file that are read by readInitFile.  This allows
		//      feedback at init-file time on commands that don't make sense, etc.
		initFile.readInitFile(this);
		
		MainMenu userInteractionMode = new MainMenu(this);
		userInteractionMode.present();
	}
	
	/** 
	 * Checks for valid command line syntax.
	 * 
	 * <p>The command line syntax is very simple:
	 * 
	 * <p>java MorphologyInducer -if <initFilename>
	 * 
	 * <p>All the detailed initialization stuff is pushed off into the initFile.
	 * Committing the -if flag gives a help message.
	 * 
	 * <p>This method only returns if execution should continue.  (Execution should continue
	 * when a valid initializations file has been given.)
	 * 
	 * @param args A String formatted as the command line arguments are formatted
	 * @return 
	 */
	/*
	 * If an initializations file is specified with the -if flag then
	 * this.initFile = new InitFile(initFilename); 
	 */
	private static InitFile checkSyntax(String[] args) {
		InitFile initFileToReturn = null;
		
		if (args.length == 0) {
			printUsageHelp();
			System.exit(0);
		}
                
		boolean validSyntax = true;
		int argIndex = 0;
		while (validSyntax && (argIndex < args.length)) {
			
			String arg = "";
			
			try { 
				arg = args[argIndex]; 
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println();
				System.out.println("An initialization file must be specified");
				printUsageHelp();
				System.exit(0);
			}
			
			arg.toLowerCase();
			
			if (arg.matches("-if") || arg.matches("-initfile")) {
				argIndex++;
			
				String initFilename = "";
				try {
					initFilename = args[argIndex];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("The -if flag must be followed by the name of an");
					System.out.println("  initializations file");
					InitFile.printHelp();
				}
				
				initFileToReturn = new InitFile(initFilename);				
				
	        // All these other options terminate execution
			} else if (arg.matches("-h") || arg.matches("-help") || arg.matches("-?")) {
				printUsageHelp();
				System.exit(0);
			} else if (arg.matches("-ifhelp")) {
				InitFile.printHelp();
				System.exit(0);
			} else if (arg.matches("-validlanguages") || arg.matches("-validLanguages")) {
				Language.LanguageName.printValidLanguages();
				System.exit(0);
			} else {
				System.out.println("Unrecognized flag: " + arg);
				printUsageHelp();
				System.exit(0);
			}		
			
			argIndex++;
		}
		
		return initFileToReturn;
	}
	
	private static void printUsageHelp() {
		System.out.println();
		System.out.println("Usage:");
		System.out.println();
		System.out.println("java MorphologyInducer -if <initializations-filename>");
		System.out.println("  OR java MorphologyInducer -ifhelp");
		System.out.println("  OR java MorphologyInducer -validLanguages");
		System.out.println();
	}

	/**
	 * initializes MorphologyInducer to work with a new corpus.
	 * 
	 * !! DESTROYS the current PhysicalPartialOrderNetwork !!
	 * 
	 * The reason the PhysicalPartialOrderNetwork is destroyed is to keep the corpus
	 * that the current PhysicalPartialOrderNetwork in sync with the corpus that
	 * this MorphologyInducer thinks it is working with.
	 * 
	 */
	public void resetCorpus(Corpus corpus) {
		this.corpus = corpus;

		partialOrderNetwork = null;
	}
	
	/**
	 * 
	 * 
	 * @return the corpus instance that is in this MorphologyInducer AND that is in
	 *         (or will be in) the PhysicalPartialOrderNetwork that this MorphologyInducer builds.
	 *         
	 * NOTE: There is no setCorpus() on purpose.  While you can getCorpus() to modify some
	 *       of its fields, what you cannot do is point this.corpus at some other Corpus.
	 */
	public Corpus getCorpus() {
		return this.corpus;
	}

	/**
	 * @return the working instance of the SparsePartialOrderNetwork class
	 */
	public PartialOrderNetwork getPartialOrderNetwork() {
		return partialOrderNetwork;
	}

	/**
	 * Get the working Language
	 * 
	 * @return The working instance of the Language class
	 */
	public Language<?> getCorpusLanguage() {
		return corpus.getLanguage();
	}

	/**
	 * Set the working Language.
	 * 
	 * The current working language defines among other things the list of correct inflectional
	 * sub-classes.
	 * 
	 * @param languageName A String holding the human readable name of the language.
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	/*
	 * As the language is set with a static function in the Language class, I can't
	 * *not* have a setLanguage() function.  Elsewhere I can avoid setter functions
	 * (as with Corpus) in MorphologyInducer because I just create a single generic 
	 * instance of those classes and then manipulate the data in those classes.
	 */
	public void setCorpusLanguage(String languageName) {
		// Call the Language Factory method
		corpus.setLanguage(languageName);
	}

	public void setPartialOrderNetwork(PartialOrderNetwork partialOrderNetwork) {
		this.partialOrderNetwork = partialOrderNetwork;
		
		System.err.println();
		System.err.println("The Morphology Scheme Network has been set to be:");
		System.err.println();
		System.err.println(this.partialOrderNetwork);
	}

}
