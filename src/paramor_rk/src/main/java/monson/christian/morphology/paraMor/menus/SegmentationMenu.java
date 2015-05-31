/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import monson.christian.morphology.paraMor.Corpus;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch.SearchStepSequenceInstantiation;
import monson.christian.morphology.paraMor.segmentation.SegmentedWord;
import monson.christian.morphology.paraMor.segmentation.SegmentedWord.OutputSegmentation;

public class SegmentationMenu {

	private static final String MENU_NAME = "Segmentation Menu";
	
	SearchBatch searchBatch;
	BufferedReader stdin;
	private String previousMenuName;

	
	private String outputFilenamePrefix = null;
	private Map<SearchStepSequenceInstantiation, File> outSegmentationFiles = null;
	private Map<SearchStepSequenceInstantiation, File> outSegmentationExplanationFiles = null;

	private Map<SearchStepSequenceInstantiation, PrintWriter> outSegmentations;
	private Map<SearchStepSequenceInstantiation, PrintWriter> outSegmentationExplanations;

	// Write out the segmentation file as latin-1 because the Morphology Challenge
	// analysis script doesn't understand utf-8. VERY SAD!
	private String encoding = "ISO-8859-1";

	public SegmentationMenu(
			SearchBatch searchBatch,
			BufferedReader stdin, 
			String previousMenuName) {
		
		this.searchBatch      = searchBatch;
		this.stdin            = stdin;
		this.previousMenuName = previousMenuName;		
	}
	

	public void present() throws IOException {
		
		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			choice = choice.toLowerCase();
			
			if (choice.matches("segcor")) {
				doSegmentation();
				
			} else if (choice.matches("segfile")) {
				doSegmentFile();
				
			} else if (choice.matches("sfaw")) {
				doSegmentFileAndWrite();
				
			} else if (choice.matches("ws")) {
				doWriteSegmentation();
				
			} else if (choice.matches("wse")) {
				doWriteSegmentationExplanation();
								
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSegmentation() {
		searchBatch.segmentCorpus();
	}
	
	private void doSegmentFile() throws IOException {
		
		Corpus corpusToSegment = new Corpus();
		
		// We are probably segmenting words from the same file that was used to
		// gather the words that were used for paradigm induction.
		corpusToSegment.setPathToCorpus(
				searchBatch.getSearchNetwork().getIdentifier().getCorpus().getCopyOfCorpusFile().getAbsolutePath());
		
		// We are probably segmenting the same language that we built paradigms for
		corpusToSegment.setLanguage(searchBatch.getSearchNetwork().getIdentifier().getCorpus().getLanguage());
		
		// We probably want to segment all words, even short ones.
		corpusToSegment.setTooShortTypeLength(0); 
 		
		CorpusMenu corpusMenu = new CorpusMenu(stdin, corpusToSegment, MENU_NAME);

		String instructions = "Read in, as a 'Corpus', the file whose types you would like to segment.";
		corpusMenu.present(instructions);
		
		boolean readyToSegment = corpusToSegment.collateTypes(null);
		
		if (readyToSegment) {
			searchBatch.segment(corpusToSegment);
		}
	}
	


	private void doSegmentFileAndWrite() {
		
	}
	
	private void doWriteSegmentation() {
		
		if ( ! searchBatch.segmentationHasBeenCompleted()) {
			System.err.println();
			System.err.println("Sorry. You must segment some text before you can");
			System.err.println("  write out their segmentations.");
			System.err.println();
			System.err.println("  Will NOT write out segmentations");
			System.err.println();
			System.err.println("  Press Enter to continue ...");
			System.err.println();
			try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
			return;
		}
			
		String instructions = "Please select which segmentations in what format to print.";
			
		SelectAnEnumValueMenu<SegmentedWord.OutputSegmentation> selectAnEnumValueMenu =
			new SelectAnEnumValueMenu<SegmentedWord.OutputSegmentation>(
					SegmentedWord.OutputSegmentation.class,
					stdin,
					MENU_NAME,
					instructions);
		
		SegmentedWord.OutputSegmentation outputSegmentation =
			selectAnEnumValueMenu.present();
		
		if (outputSegmentation == null) {
			return;
		}
		
		boolean shouldWriteSegmentation = doSetOutputFiles(outputSegmentation);
		if ( ! shouldWriteSegmentation) {
			return;
		}
		
		System.err.println();
		System.err.println("Printing the results of the segmentation(s)...");
		System.err.println();
		
		
		// 4) open the files to output search results to
		outSegmentations = openOutputFiles(outSegmentationFiles);
		if (outSegmentations == null) {
			return;  
		}
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
															outSegmentations.keySet()) {
		
			PrintWriter outSegmentation = 
				outSegmentations.get(searchStepSequenceInstantiation);
			
			// Write parameter details to output files -- both details from the Network
			// and details from this parameter setting.

			outSegmentation.println(searchBatch.toStringAsComment());
			outSegmentation.println();
			outSegmentation.flush();
			
			//outSegmentation.println(segmentedCorpus);
			outSegmentation.println(
					searchBatch.getSegmentationString(
							searchStepSequenceInstantiation,
							outputSegmentation));
			outSegmentation.flush();
			outSegmentation.close();
		
		}
		
		System.err.println();
		System.err.println("The segmentation explanation results have been written:");
		System.err.println();
		System.err.println("  Press Enter to continue...");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should never happen

	}



	private void doWriteSegmentationExplanation() {
		
		if ( ! searchBatch.segmentationHasBeenCompleted()) {
			System.err.println();
			System.err.println("Sorry. You must segment some text before you can");
			System.err.println("  write out the explanations of their segmentations.");
			System.err.println();
			System.err.println("  Will NOT write out segmentation explanations");
			System.err.println();
			System.err.println("  Press Enter to continue ...");
			System.err.println();
			try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
			return;
		}
		
		boolean shouldWriteSegmentationExplanations = 
			doSetSegmentationExplanationOutputFiles();
		if ( ! shouldWriteSegmentationExplanations) {
			return;
		}
		
		System.err.println();
		System.err.println("Printing the results of the segmentation(s)...");
		System.err.println();
		
		
		// 4) open the files to output search results to
		outSegmentationExplanations = openOutputFiles(outSegmentationExplanationFiles);
		if (outSegmentationExplanations == null) {
			return;  
		}
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
														outSegmentationExplanations.keySet()) {
		
			PrintWriter outSegmentationExplanation = 
				outSegmentationExplanations.get(searchStepSequenceInstantiation);
			
			// Write parameter details to output files -- both details from the Network
			// and details from this parameter setting.

			outSegmentationExplanation.println(searchBatch.toStringAsComment());
			outSegmentationExplanation.println();
			outSegmentationExplanation.flush();
		
			//outSegmentation.println(segmentedCorpus);
			outSegmentationExplanation.println(
					searchBatch.getSegmentationExplanationString(
							searchStepSequenceInstantiation));
			outSegmentationExplanation.flush();
			outSegmentationExplanation.close();
		
		}
		
		System.err.println();
		System.err.println("The segmentation results have been written:");
		System.err.println();
		System.err.println("  Press Enter to continue...");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should never happen

	}
	
	
	private void setOutputFilenamePrefix() {

		if (outputFilenamePrefix == null) {
			String instructions = 
				"Enter the fully qualified file name **prefix** to write " +
				"the segmentation results and explanations to";
			
			StringInputMenu stringInputMenu = 
				new StringInputMenu(stdin, MENU_NAME, instructions);
			outputFilenamePrefix = stringInputMenu.present();
		}
	}

	// Returns false if the user chose not to enter a file name prefix
	private boolean doSetOutputFiles(OutputSegmentation outputSegmentation) {
		
		Boolean filenamesAreAcceptable = null;
		boolean askForFilename = true;
		while (askForFilename) {
			askForFilename = false;

			setOutputFilenamePrefix();

			if (outputFilenamePrefix == null) {
				return false;
			}

			outSegmentationFiles = new TreeMap<SearchStepSequenceInstantiation, File>();

			Iterator<SearchStepSequenceInstantiation> 
			iteratorOverCurrentSearchStepSequenceInstantiations =
				searchBatch.getIteratorOverCurrentSearchStepSequenceInstantiations();

			while (iteratorOverCurrentSearchStepSequenceInstantiations.hasNext()) {
				SearchStepSequenceInstantiation searchStepSequenceInstantiation =
					iteratorOverCurrentSearchStepSequenceInstantiations.next();

				String searchStepSequenceInstantiationFileUniqueifier =
					searchStepSequenceInstantiation.getFilenameUniqueifier();

				String shortDescriptionOfSegmentation =
					outputSegmentation.getShortDescriptionOfOutputSegmentation();

				File outSegmentationFile = 
					new File(
							outputFilenamePrefix + 
							searchStepSequenceInstantiationFileUniqueifier + 
							"-" + shortDescriptionOfSegmentation +
					"-segmentations.txt");

				outSegmentationFiles.put(searchStepSequenceInstantiation, outSegmentationFile);


			}

			// 3) Verify the output files to be written to
			String instructions = 
				String.format("Do you wish to write out the segmentation results%n");
			instructions += String.format("  to the following file(s)?%n%n");
			for (File outSegmentationFile : outSegmentationFiles.values()) {	
				instructions += String.format("  " + outSegmentationFile + "%n");
			}

			filenamesAreAcceptable = verifyOutputFile(instructions);

			if (filenamesAreAcceptable == null) {
				outputFilenamePrefix = null;
				askForFilename = true;
			} 
		}
		
		return filenamesAreAcceptable;
	}
	
	// Returns false if the user chose not to enter a file name prefix
	private boolean doSetSegmentationExplanationOutputFiles() {
		
		Boolean filenamesAreAcceptable = null;
		boolean askForFilename = true;
		while (askForFilename) {
			askForFilename = false;

			setOutputFilenamePrefix();

			if (outputFilenamePrefix == null) {
				return false;
			}

			outSegmentationExplanationFiles = new TreeMap<SearchStepSequenceInstantiation, File>();

			Iterator<SearchStepSequenceInstantiation> 
			iteratorOverCurrentSearchStepSequenceInstantiations =
				searchBatch.getIteratorOverCurrentSearchStepSequenceInstantiations();

			while (iteratorOverCurrentSearchStepSequenceInstantiations.hasNext()) {
				SearchStepSequenceInstantiation searchStepSequenceInstantiation =
					iteratorOverCurrentSearchStepSequenceInstantiations.next();

				String searchStepSequenceInstantiationFileUniqueifier =
					searchStepSequenceInstantiation.getFilenameUniqueifier();

				File outSegmentationExplanationFile = 
					new File(
							outputFilenamePrefix + 
							searchStepSequenceInstantiationFileUniqueifier + 
					"-segmentationExplanations.txt");

				outSegmentationExplanationFiles.put(
						searchStepSequenceInstantiation, outSegmentationExplanationFile);
			}

			// 3) Verify the output files to be written to
			String instructions = 
				String.format("Do you wish to write out the segmentation results%n");
			instructions += String.format("  to the following file(s)?%n%n");
			for (File outSegmentationExplanationFile : outSegmentationExplanationFiles.values()) {	
				instructions += String.format("  " + outSegmentationExplanationFile + "%n");
			}

			filenamesAreAcceptable = verifyOutputFile(instructions);

			if (filenamesAreAcceptable == null) {
				outputFilenamePrefix = null;
				askForFilename = true;
			} 
		}

		return filenamesAreAcceptable;
	}
	
	// Returning null means specify a new outputfile
	private Boolean verifyOutputFile(String instructions) {
		System.err.println();
		System.err.println(instructions);
		System.err.println("------------------------------------------------------------");
		System.err.println();
		System.err.println("  <Y>es");
		System.err.println("  <N>o");
		System.err.println("  <S>pecify new file name.");
		System.err.println();
		System.err.print("> ");
		String choice;
		try {
			choice = stdin.readLine();
		}
		catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
		
		if (choice.matches("s|S")) {
			return null;
			
		} else if ( ! choice.matches("y|Y")) {
			System.err.println();
			System.err.println("  Will NOT print the detailed type covering file.  Press Enter to continue...");
			System.err.println();
			try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
			
			return false;  // yes, continue at the current menu level
		}

		return true;
	}

	private Map<SearchStepSequenceInstantiation, PrintWriter> 
	openOutputFiles(Map<SearchStepSequenceInstantiation, File> filesToOpen) {
		
		Map<SearchStepSequenceInstantiation, PrintWriter> printWriters = 
			new TreeMap<SearchStepSequenceInstantiation, PrintWriter>();
		
		boolean encodingSelected = false;
		while ( ! encodingSelected) {
			encodingSelected = true;
			System.err.println();
			System.err.println("What encoding would you like to write out in");
			System.err.println();
			System.err.println("  <1> utf-8");
			System.err.println("  <2> latin-1 -- more convenient for the Morpho Challenge ");
			System.err.println("                 2007 script which only accepts 8-bit max ");
			System.err.println("                 characters");
			System.err.println();
			System.err.print(" > ");
			String choice = "";
			try {choice = stdin.readLine();}	catch (IOException e2) {}  // This error should never happen

			if (choice.equals("1")) {
				encoding = "UTF-8";
			} else if (choice.equals("2")) {
				encoding = "ISO-8859-1";
			} else {
				encodingSelected = false;
				System.err.println();
				System.err.println("I'm sorry that was not a valid encoding selection.");
				System.err.println("  Please try again.");
				System.err.println();
				System.err.println("  Press Enter to continue...");
				try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen				
			}
		}
		
		for (SearchStepSequenceInstantiation searchStepSequenceInstantiation : 
														filesToOpen.keySet()) {
		
			File fileToOpen = 
				filesToOpen.get(searchStepSequenceInstantiation);
			
			try {
				PrintWriter outSegmentation = 
					new PrintWriter(
							new BufferedWriter(
									new OutputStreamWriter(
											new FileOutputStream(fileToOpen),
											encoding )),
											true); // true to autoflush

				printWriters.put(searchStepSequenceInstantiation, outSegmentation);

			} catch (FileNotFoundException e) {
				System.err.println();
				System.err.println("Cannot set the output file:");
				System.err.println("  " + fileToOpen.getAbsolutePath());
				System.err.println();
				System.err.println("  Press Enter to continue...");
				try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
				return null;

			} catch (IOException e) {
				System.err.println("Failed to open the output file because");
				System.err.println("  of the following internal error:");
				e.printStackTrace();
				System.err.println();
				System.err.println("  Press Enter to continue...");
				try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
				return null;
			}
		}
		
		return printWriters;
	}

	
	public void printOptions() {
		System.err.println();
		System.err.println("----------------------------------------------");
		System.err.println("   " + MENU_NAME);
		System.err.println("----------------------------------------------");
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SegCor>  <Seg>ment all the words in the <Cor>pus");
		System.err.println("  <SegFile> <Seg>ment all the words in some <File>");
		System.err.println("  <WS>      <W>rite <S>egmentation");
		System.err.println("  <WSE>     <W>rite <S>egmentation <E>xplanation");
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}
}
