/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import monson.christian.morphology.paraMor.ParaMor;
import monson.christian.morphology.paraMor.networks.PartialOrderNetwork;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.TypeCoveredByCompetingSchemes_Filter;

public class SearchAndProcessingMenu {

	private static final String MENU_NAME = "Search and Processing Menu";
	
	private BufferedReader stdin;

	private ParaMor morphologyInducer;
	
	private String previousMenuName;
	
	private File outEvaluationFile         = new File("./junk-evaluations.csv");
	private File outSchemesFile            = new File("./junk-schemes.txt");
	private File outSearchStepDeltaFile    = new File("./junk-searchStepDelta.txt");
	private File outClustersFile           = new File("./junk-clusters.txt");
	private File outClustersEvaluationFile = new File("./junk-clustersEvaluations.csv");
	private File outClustersDeltaFile      = new File("./junk-clustersDelta.txt");
	private File outSerializedSearchBatchZippedFile = new File("./junk-serializedSearchBatch.gz");
	private File outPossibleSegmentationsFile = new File("./junk-outPossibleSegmentations.txt");
	
	private PrintWriter outEvaluation;
	private PrintWriter outSchemes;
	private PrintWriter outSearchStepDelta;
	private PrintWriter outPossibleSegmentations;
	
	private PrintWriter outClusters;
	private PrintWriter outClustersEvaluation;
	private PrintWriter outClustersDelta;

	
	private SearchBatch searchBatch;



	
	
	public SearchAndProcessingMenu(BufferedReader stdin, 
					  ParaMor morphologyInducer, 
					  String previousMenuName) {
		this.stdin = stdin;
		this.morphologyInducer = morphologyInducer;
		this.previousMenuName = previousMenuName;		
	}


	private void initializeSearchBatch() {
		// If search batch has not yet been initialized, then try to initialize it
		if (searchBatch == null) {
			PartialOrderNetwork partialOrderNetwork = morphologyInducer.getPartialOrderNetwork();
			
			// searchBatch can be initialized if morphologyInducer has a partialOrderNetwork
			// that has been initialized to the correct type of network (Dynamic_Dense).
			if (partialOrderNetwork != null) {
				searchBatch = new SearchBatch(partialOrderNetwork,
						morphologyInducer.getCorpusLanguage());
			}
		}
	}
	
	public void present() throws IOException {
		
		initializeSearchBatch();
		
		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			choice = choice.toLowerCase();
			
			if ( ! (choice.matches("rsb") || choice.matches("r"))) {
				if (searchBatch == null) {
					System.err.println();
					System.err.println("Sorry.  You must either first build a morphology scheme network");
					System.err.println("  that can be searched, OR you must read in a search batch (RSB)");
					System.err.println();
					System.err.println("  Press Enter to Continue...");
					try {stdin.readLine();}	catch (IOException e) {}  // This error should never happen
					continue;
				}
			}
			
			if (choice.matches("setout")) {
				doSetOutputFiles();
								
			} else if (choice.matches("bus")) {
				doBottomUpSearch();
				
			} else if (choice.matches("mlf")) {
				doMorphemeLengthFilter();
				
			} else if (choice.matches("mbtflf")) {
				doMorphemeBoundaryTooFarLeftFilter();
				
			} else if (choice.matches("mbtfrf")) {
				doMorphemeBoundaryTooFarRightFilter();
				
			} else if (choice.matches("tcf")) {
				doTypesCoveredFilter();
				
			} else if (choice.matches("ctbsf")) {
				doCoveredTypeByScheme_Filter();
				
			} else if (choice.matches("c")) {
				clusterSearchBatch();
				
			} else if (choice.matches("tcfc")) {
				doTypesCoveredFilterAppliedToCluaters();
				
			} else if (choice.matches("mbtflfc")) {
				doMorphemeBoundaryTooFarLeftFilterOnClusters();
					
			} else if (choice.matches("mbtfrfc")) {
				doMorphemeBoundaryTooFarRightFilterOnClusters();
					
			} else if (choice.matches("wr")) {
				writeResults();
				
			} else if (choice.matches("wcr")) {
				writeClusterResults();
				
			} else if (choice.matches("wcwfts")) {
				writeCoveredWordFormsToSchemes();
				
			} else if (choice.matches("dpc")) {
				try {
					detectPhonChange();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				
			} else if (choice.matches("seg")) {
				doSegmentationMenu();
				
			} else if (choice.matches("wsb")) {
				writeSearchBatch();
				
			} else if (choice.matches("rsb")) {
				readSearchBatch();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}

	// Returns false if the user chose not to enter a file name prefix
	private boolean doSetOutputFiles() {
		String instructions = 
			"Enter the fully qualified file name **prefix** to write the results of this bottom up search to";
		
		StringInputMenu stringInputMenu = 
			new StringInputMenu(stdin, MENU_NAME, instructions);
		String outputFilePrefix = stringInputMenu.present();
		
		if (outputFilePrefix == null) {
			return false;
		}
		
		outEvaluationFile            = new File(outputFilePrefix + "-evaluations.csv");
		outSchemesFile               = new File(outputFilePrefix + "-schemes.txt");
		outSearchStepDeltaFile       = new File(outputFilePrefix + "-searchStepDelta.txt");
		outPossibleSegmentationsFile = new File(outputFilePrefix + "-possibleSegmentations.txt");
		outClustersFile              = new File(outputFilePrefix + "-clusters.txt");
		outClustersEvaluationFile    = new File(outputFilePrefix + "-clustersEvaluations.csv");
		outClustersDeltaFile		 = new File(outputFilePrefix + "-clustersDelta.txt");
		outSerializedSearchBatchZippedFile = new File(outputFilePrefix + "-serializedSearchBatch.gz");
		
		return true;
	}

	
	private void doBottomUpSearch() {
		BottomUpSearchMenu bottomUpSearchMenu = 
			new BottomUpSearchMenu(stdin, searchBatch, MENU_NAME);
		bottomUpSearchMenu.present();
	}

	private void doMorphemeLengthFilter() {
		MorphemeLengthFilterMenu filterMenu =
			new MorphemeLengthFilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}
	
	private void doMorphemeBoundaryTooFarLeftFilter() {
		MorphemeBoundaryTooFarLeft_FilterMenu filterMenu =
			new MorphemeBoundaryTooFarLeft_FilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}
	
	private void doMorphemeBoundaryTooFarRightFilter() {
		MorphemeBoundaryTooFarRight_FilterMenu filterMenu =
			new MorphemeBoundaryTooFarRight_FilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}
	
	private void doTypesCoveredFilter() {
		TypesCoveredFilterMenu filterMenu =
			new TypesCoveredFilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}
	
	private void doCoveredTypeByScheme_Filter() {
		TypeCoveredByCompetingSchemes_Filter.Parameters filterParameters = 
			new TypeCoveredByCompetingSchemes_Filter.Parameters();
		searchBatch.performSearchStep(filterParameters);
	}
	
	
	private void clusterSearchBatch() {
		ClusteringMenu clusterMenu = new ClusteringMenu(stdin, searchBatch, MENU_NAME);
		clusterMenu.present();
	}
	
	private void doTypesCoveredFilterAppliedToCluaters() {
		TypesCoveredFilterAppliedToClustersMenu menu =
			new TypesCoveredFilterAppliedToClustersMenu(stdin, searchBatch, MENU_NAME);
		menu.present(); 
	}
	

	private void doMorphemeBoundaryTooFarLeftFilterOnClusters() {
		MorphemeBoundaryTooFarLeft_ClusterFilterMenu filterMenu =
			new MorphemeBoundaryTooFarLeft_ClusterFilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}
	
	private void doMorphemeBoundaryTooFarRightFilterOnClusters() {
		MorphemeBoundaryTooFarRight_ClusterFilterMenu filterMenu =
			new MorphemeBoundaryTooFarRight_ClusterFilterMenu(stdin, searchBatch, MENU_NAME);
		filterMenu.present();
	}

	private void evaluateSearchBatch() {
		System.err.println();
		System.err.println("Evaluating the current search batch...");
		System.err.println();
		
		// Actually calculate what the quantitative evaluation metric evaluates to.
		searchBatch.evaluateSearchBatch();
		
		System.err.println();
		System.err.println("Done evaluating the current search batch.");
		System.err.println();
		System.err.println("  Press Enter to Continue...");
		try {stdin.readLine();}	catch (IOException e) {}  // This error should never happen
	}
	
	/*
	private void evaluateClusters() {
		System.err.println();
		System.err.println("Evaluating the current search batch clusters...");
		System.err.println();
		
		// Actually calculate what the quantitative evaluation metric evaluates to.
		searchBatch.evaluateClusters();
		
		System.err.println();
		System.err.println("Done evaluating the current search batch clusters.");
		System.err.println();
		System.err.println("  Press Enter to Continue...");
		try {stdin.readLine();}	catch (IOException e) {}  // This error should never happen
	}
	*/
	
	private void writeResults() {
		
		// 1) Make sure the results are ready to be written
		if ( ! searchBatch.evaluationHasBeenPerformed()) {
			evaluateSearchBatch();
		}
		
		// 2) Choose an output file if one has not been chosen yet
		if ((outEvaluationFile      == null) ||
			(outSchemesFile         == null) ||
			(outSearchStepDeltaFile == null)) {
			
			boolean choseOutputFile = doSetOutputFiles();
			if ( ! choseOutputFile) {
				return;
			}
		}
		
		// 3) Verify the output files to be written to
		String instructions = String.format("Do you wish to write out the evaluation results of the %n");
		instructions += String.format("  current search batch to the following files?%n%n");
		instructions += String.format("  " + outEvaluationFile + "%n");
		instructions += String.format("  " + outSchemesFile + "%n");
		instructions += String.format("  " + outSearchStepDeltaFile);
		if ( ! verifyOutputFile(instructions)) {
			return;
		}
		
		System.err.println();
		System.err.println("Printing the results of the current search batch...");
		System.err.println();
		
		
		// 4) open the files to output search results to
		boolean openingOutputFilesSucceeded = openOutputFiles();
		if ( ! openingOutputFilesSucceeded) {
			return;  
		}
		
		// Write parameter details to output files -- both details from the Network
		// and details from this parameter setting.

		outEvaluation.println(searchBatch);
		outEvaluation.println();
		outEvaluation.flush();
		
		outSchemes.println(searchBatch);
		outSchemes.println();
		outSchemes.flush();
		
		outSearchStepDelta.println(searchBatch);
		outSearchStepDelta.println();
		outSearchStepDelta.flush();

		String selectedSchemesWithPathsString = searchBatch.getSelectedSchemesWithPathsString();
		outSchemes.println(selectedSchemesWithPathsString);
		outSchemes.flush();
		outSchemes.close();
		
		String evaluationString = searchBatch.getEvaluationStringForSpreadsheet();
		outEvaluation.println(evaluationString);
		outEvaluation.flush();
		outEvaluation.close();
		
		String searchStepDeltaString = searchBatch.getSearchStepDeltaString();
		outSearchStepDelta.println(searchStepDeltaString);
		outSearchStepDelta.flush();
		outSearchStepDelta.close();
		
		System.err.println();
		System.err.println("The evaluation results of the current search batch have been written.");
		System.err.println(" to the files:");
		System.err.println();
		System.err.println("Evaluations Output File: " + outEvaluationFile);
		System.err.println("Schemes Output File:     " + outSchemesFile);
		System.err.println("Search Step Delta File:  " + outSearchStepDeltaFile);
		System.err.println();
		System.err.println("  Press Enter to continue...");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
	}

	private boolean openOutputFiles() {
		try {
			outEvaluation = new PrintWriter(new BufferedWriter(new FileWriter(outEvaluationFile)),
								  true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outEvaluationFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		try {
			outSchemes = new PrintWriter(new BufferedWriter(new FileWriter(outSchemesFile)),
								  true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outSchemesFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		try {
			outSearchStepDelta = new PrintWriter(new BufferedWriter(new FileWriter(outSearchStepDeltaFile)),
								  true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outSearchStepDeltaFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		return true;
	}
	
	private void writeClusterResults() {
				
		// 1) Make sure clusters are ready to be written
		if ( ! searchBatch.isClustersReadyToBeWritten(stdin)) {
			return;
		}
		
		if ( ! searchBatch.evaluationOfClustersHasBeenPerformed()) {
			searchBatch.evaluateClusters();
		}
		
		// 2) Choose an output file if we haven't chosen one yet
		if (outClustersFile == null) {
			boolean choseOutputFile = doSetOutputFiles();
			if ( ! choseOutputFile) {
				return;
			}
		}
		
		// 3) Verify the output file to be written to
		String instructions = String.format("Do you wish to write out the clustered selected schemes%n");
		instructions += String.format("  and an evaluation of them to the following files?%n%n");
		instructions += String.format("  " + outClustersFile + "%n");
		instructions += String.format("  " + outClustersEvaluationFile + "%n");
		instructions += String.format("  " + outClustersDeltaFile);
		if ( ! verifyOutputFile(instructions)) {
			return;
		}


		// 4) Try to open the output file
		boolean openingOutputFilesSucceeded = openClusterOutputFiles();
		if ( ! openingOutputFilesSucceeded) {
			return;  
		}
		

		// Write parameter details to output files -- both details from the Network
		// and details from this parameter setting.
		
		outClusters.println(searchBatch);
		outClusters.println();
		outClusters.flush();
		
		outClustersEvaluation.println(searchBatch);
		outClustersEvaluation.println();
		outClustersEvaluation.flush();
		
		outClustersDelta.println(searchBatch);
		outClustersDelta.println();
		outClustersDelta.flush();

		String clustersString = searchBatch.getClustersString();
		outClusters.println(clustersString);
		outClusters.flush();
		outClusters.close();
		
		String clustersEvaluationString = searchBatch.getClustersEvaluationStringForSpreadsheet();
		outClustersEvaluation.println(clustersEvaluationString);
		outClustersEvaluation.flush();
		outClustersEvaluation.close();
		
		String clustersDeltaString = searchBatch.getSearchStepDeltaStringForClusters();
		outClustersDelta.println(clustersDeltaString);
		outClustersDelta.flush();
		outClustersDelta.close();

		
		System.err.println();
		System.err.println("A HUMAN readable version of the clustered selected schemes, and");
		System.err.println("  and evaluation of those clusters have been written to the files:");
		System.err.println();
		System.err.println("  " + outClustersFile);
		System.err.println("  " + outClustersEvaluationFile);
		System.err.println("  " + outClustersDeltaFile);
		System.err.println();
		System.err.println(" Press Enter to Continue... ");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
	}


	private boolean openClusterOutputFiles() {
		try {
			outClusters = new PrintWriter(new BufferedWriter(new FileWriter(outClustersFile)),
								  true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outClustersFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Did not write an output file containing clustered schemes");
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file");
			System.err.println("  " + outClustersFile.getAbsolutePath());
			System.err.println("  because of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		
		try {
			outClustersEvaluation = 
				new PrintWriter(new BufferedWriter(new FileWriter(outClustersEvaluationFile)),
								true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outClustersEvaluationFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Did not write an output file containing an evaluation of the");
			System.err.println("    clustered schemes");
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file:");
			System.err.println("  " + outClustersEvaluationFile.getAbsolutePath());
			System.err.println("  because of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		
		try {
			outClustersDelta = 
				new PrintWriter(new BufferedWriter(new FileWriter(outClustersDeltaFile)),
								true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outClustersDeltaFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Did not write an output file containing the new and deleted");
			System.err.println("    clustered schemes");
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file");
			System.err.println("  " + outClustersDeltaFile.getAbsolutePath());
			System.err.println("  because of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return false;
		}
		
		return true;
	}

	private void detectPhonChange() throws Exception {
		this.searchBatch.detectPhonChanges();		
	}

	private void doSegmentationMenu() throws IOException {
		SegmentationMenu segmentationMenu = 
			new SegmentationMenu(searchBatch, stdin, MENU_NAME);

		segmentationMenu.present();
	}
		
	private void writeSearchBatch() {
		
		// 1) Choose an output file if one has not been chosen yet
		if (outSerializedSearchBatchZippedFile == null) {
			
			boolean choseOutputFile = doSetOutputFiles();
			if ( ! choseOutputFile) {
				return;
			}
		}
		
		// 2) Verify the output files to be written to
		String instructions = String.format("Do you wish to write out the serialization of the current%n");
		instructions += String.format("  search batch to the following file?%n%n");
		instructions += String.format("  " + outSerializedSearchBatchZippedFile);
		if ( ! verifyOutputFile(instructions)) {
			return;
		}

		
		System.err.println();
		System.err.println("Printing the serialization of the current search batch...");
		System.err.println();
		
		
		// 4) create the ObjectOutputStream that will write the serialization of the Search
		//      batch to file.

		
		try {
			ObjectOutputStream objectOutputStream =
				new ObjectOutputStream(
						new GZIPOutputStream (
								new BufferedOutputStream(
										new FileOutputStream(outSerializedSearchBatchZippedFile))));
			objectOutputStream.writeObject(searchBatch);
			objectOutputStream.close();
		}
		catch (IOException e) {
			System.err.println();
			System.err.println("Could not write to the filename: " + outSerializedSearchBatchZippedFile);
			System.err.println("  Action Aborted with the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e2) {}  // This should never happen
			return;
		}
		
		System.err.println();
		System.err.println("The search batch serialization has been saved to the file:");
		System.err.println();
		System.err.println("  " + outSerializedSearchBatchZippedFile);
		System.err.println();
		System.err.println(" Press Enter to Continue... ");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should exception never happen
	}
	
	private void readSearchBatch() {
		String instructions = "";
		instructions += String.format("Enter a filename to read a (gzipped) Java Serialization of a%n");
		instructions += String.format("  Search Batch from.%n%n");
		instructions += String.format("NOTE: The read in Search Batch will replace any current%n");
		instructions += String.format("  Search Batch!%n%n");
		instructions += String.format("NOTE: The Java Serialization is assumed to be gzipped, and a .gz%n");
		instructions += String.format("      extension will be automatically added to the name of the%n");
		instructions += String.format("      entered input file. ");
		FileInputMenu fileInputMenu = new FileInputMenu(stdin, MENU_NAME, instructions);
		File searchBatchInputFile = fileInputMenu.present();
		if (searchBatchInputFile == null) {
			return;
		}
		File searchBatchZippedInputFile =
			new File(searchBatchInputFile.getAbsolutePath() + ".gz");
		
		try {
			ObjectInputStream objectInputStream =
				new ObjectInputStream(
						new GZIPInputStream (
								new BufferedInputStream(
										new FileInputStream(searchBatchZippedInputFile))));
			
			searchBatch = (SearchBatch)objectInputStream.readObject();
			
			objectInputStream.close();
			
		} catch (IOException e) {
			System.err.println();
			System.err.println("Could not read from the filename: " + searchBatchZippedInputFile);
			System.err.println("  Action Aborted with the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e2) {}  // This should never happen
			return;
			
		} catch (ClassNotFoundException e) {
			System.err.println();
			System.err.println("Action Aborted because of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e2) {}  // This should never happen
			return;
		}
		
		System.err.println();
		System.err.println("The search batch results have been read from the file:");
		System.err.println();
		System.err.println("  " + searchBatchZippedInputFile);
		System.err.println();
		System.err.println(" The newly read in search batch is:");
		System.err.println();
		System.err.println(searchBatch);
		System.err.println();
		System.err.println(" Press Enter to Continue... ");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should exception never happen

	}

	private void writeCoveredWordFormsToSchemes() {
		
		System.err.println();
		
		if (outPossibleSegmentationsFile == null) {
			boolean choseOutputFile = doSetOutputFiles();
			if ( ! choseOutputFile) {
				return;
			}
		}
		
		// 3) Verify the output file to be written to
		String instructions = String.format("  Do you wish to write the detailed results file that%n");
		instructions += String.format("   specifies which word types are covered by some selected%n");
		instructions += String.format("   scheme and which selected scheme it is that covers it to%n");
		instructions += String.format("   the file: %n%n");
		instructions += String.format("   " + outPossibleSegmentationsFile);

		if ( ! verifyOutputFile(instructions)) {
			return;
		}

		
		try {
			outPossibleSegmentations = 
				new PrintWriter(new BufferedWriter(new FileWriter(outPossibleSegmentationsFile)),
								true); // true to autoflush
		} catch (FileNotFoundException e) {
			System.err.println();
			System.err.println("Cannot set the output file:");
			System.err.println("  " + outPossibleSegmentationsFile.getAbsolutePath());
			System.err.println();
			System.err.println("  Did not write an output file containing information on how the");
			System.err.println("  covered word forms map to the selected schemes that cover them");
			System.err.println();
			System.err.println("  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
			
		} catch (IOException e) {
			System.err.println("Failed to open the output file because");
			System.err.println("  of the following internal error:");
			e.printStackTrace();
			System.err.println();
			System.err.println("  Aborting this search round.  Press Enter to contineue...");
			try {stdin.readLine();}	catch (IOException e2) {}  // This error should never happen
			return;
		}
		
		outPossibleSegmentations.println(searchBatch);
		outPossibleSegmentations.println();
		outPossibleSegmentations.flush();
		
		searchBatch.calculateCoveredTypesToSelectedSchemes();
		outPossibleSegmentations.println(searchBatch.getCoveredTypesToSelectedSchemesString());
		outPossibleSegmentations.flush();
		outPossibleSegmentations.close();
		
		System.err.println();
		System.err.println("The detailed results file that specifies which word");
		System.err.println(" types are covered by some selected scheme and which");
		System.err.println(" selected scheme it is that covers it, has been written");
		System.err.println(" to the file:");
		System.err.println();
		System.err.println("  " + outPossibleSegmentationsFile);
		System.err.println();
		System.err.println(" Press Enter to Continue... ");
		System.err.println();
		try { stdin.readLine();	} catch (IOException e) {}  // This should exception never happen

	}
		
	private boolean verifyOutputFile(String instructions) {
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
			doSetOutputFiles();
			
		} else if ( ! choice.matches("y|Y")) {
			System.err.println();
			System.err.println("  Will NOT print the detailed type covering file.  Press Enter to continue...");
			System.err.println();
			try { stdin.readLine();	} catch (IOException e) {}  // This should never happen
			
			return false;  // yes, continue at the current menu level
		}

		return true;
	}


	private void printSearchSettings() {

		System.err.println("Current Search Batch Details");
		System.err.println("----------------------------");
		System.err.println();
		
		if (searchBatch != null) {
			System.err.println(searchBatch);
		} else {
			System.err.println("No Search Batch has yet been specified");
		}
	}

	public void printOptions() {
		System.err.println();
		System.err.println("----------------------------------------------");
		System.err.println("   " + MENU_NAME);
		System.err.println("----------------------------------------------");
		System.err.println();
		printSearchSettings();
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetOut>  <Set> the prefix to the <Out>put files");
		System.err.println();
		System.err.println("  <BUS>    <B>ottom-<U>p <S>earch");
		System.err.println("  <MLF>    <M>orpheme <L>ength <F>ilter");
		System.err.println("  <MBTFLF> <M>orpheme <B>oundary <T>oo <F>ar <L>eft <F>ilter");
		System.err.println("  <MBTFRF> <M>orpheme <B>oundary <T>oo <F>ar <R>ight <F>ilter");
		System.err.println("  <TCF>    <T>ype <C>overed <F>ilter");
		System.err.println("  <CTBSF>  <C>overed <T>ype <B>y <S>cheme <F>ilter.");
		System.err.println("              Filter by comparing the schemes that cover");
		System.err.println("              individual word types.");
		System.err.println();
		System.err.println("  <C>luster the current set of selected schemes");
		System.err.println("  <TCFC>   <T>ype <C>overed <F>ilter applied to <C>lusters");
		System.err.println("  <MBTFLFC><M>orpheme <B>oundary <T>oo <F>ar <L>eft <F>ilter applied to <C>lusters");
		System.err.println("  <MBTFRFC><M>orpheme <B>oundary <T>oo <F>ar <R>ight <F>ilter applied to <C>lusters");
		System.err.println();
		System.err.println("  <WR>     <W>rite the <R>esults of the current search batch.  Both the");
		System.err.println("              evaluation results and the raw list of selected schemes.");
		System.err.println("  <WCR>    <W>rite <C>luster <R>esults.  Write the clustered selected");
		System.err.println("              schemes in HUMAN readable form.  And write out the evaluation");
		System.err.println("              of the clusters.");
		System.err.println("  <WCWFTS> <W>rite <C>overed-<W>ord-<F>orms-<T>o-<S>chemes data ");
		System.err.println("              structure to a file (HUMAN readable, writes to a file)");
		System.err.println("              specified with <SetOut>");
		System.err.println();
		System.err.println("  <DPC> <D>etect <P>honological <C>hanges");
		System.err.println();
		System.err.println("  <Seg>ment -- go to the segmentation menu");
		System.err.println();
		System.err.println("  <WSB>    <W>rite a <S>earch <B>atch to file (COMPUTER readable)");
		System.err.println("  <RSB>    <R>ead  a <S>earch <B>atch from a file (COMPUTER readable)");
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
