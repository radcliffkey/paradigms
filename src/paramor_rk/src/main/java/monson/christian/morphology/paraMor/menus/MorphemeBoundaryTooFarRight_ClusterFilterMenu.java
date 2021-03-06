/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import monson.christian.morphology.paraMor.searchAndProcessing.MorphemeBoundaryTooFarRight_Filter;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.MorphemeBoundaryTooFarLeft_Filter.LeftMetric;

public class MorphemeBoundaryTooFarRight_ClusterFilterMenu {

	private static final String MENU_NAME = "Morpheme Boundary Too Far Right Filter";
	
	private BufferedReader stdin;

	private SearchBatch searchBatch;
	private MorphemeBoundaryTooFarRight_Filter.Parameters filterParameters = 
		new MorphemeBoundaryTooFarRight_Filter.Parameters();
	
	private String previousMenuName;
	
	
	public MorphemeBoundaryTooFarRight_ClusterFilterMenu(BufferedReader stdin, 
							  					 SearchBatch searchBatch, 
							  					 String previousMenuName) {
		this.stdin = stdin;
		this.searchBatch = searchBatch;
		this.previousMenuName = previousMenuName;
	}
	
	public void present() {

		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			choice = choice.toLowerCase();
			
			if (choice.matches("setlm")) {
				doSetLeftLookingMetric();
				
			} else if (choice.matches("setlmc")) {
				doSetLeftLookingMetricCutoffs();
				
			} else if (choice.matches("f")) {
				continueLoop = doMorphemeBoundaryTooFarRight_Filter();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetLeftLookingMetric() {
		
		String instructions = "Please select the left looking metric you would like to filter with";
		SelectAnEnumValueMenu<LeftMetric> selectAMetricMenu = 
			new SelectAnEnumValueMenu<LeftMetric>(
					LeftMetric.class, 
					stdin, 
					MENU_NAME, 
					instructions);
		LeftMetric leftMetric = selectAMetricMenu.present();
		if (leftMetric == null) {
			return;
		}
		filterParameters.setLeftMetric(leftMetric);
	}

	private void doSetLeftLookingMetricCutoffs() {
		ListOfDoubleInputMenu listOfDoubleInputMenu = 
			new ListOfDoubleInputMenu(stdin, 
									  MENU_NAME, 
									  "Left looking Metric Cutoff: Please enter a list of cutoffs " +
									  "(real numbers) to filter over with the " + 
									  filterParameters.getLeftMetric() + " metric.");
		List<Double> leftLookingMetricCutoffs = listOfDoubleInputMenu.present();
		if (leftLookingMetricCutoffs != null) {
			filterParameters.setLeftCutoffs(leftLookingMetricCutoffs);
		}
	}
		
	
	// Returns false if we should completely stop doing this search and go back to the entirely
	// previous menu.
	private boolean doMorphemeBoundaryTooFarRight_Filter() {
		
		System.err.println();
		System.err.println("Do you wish to perform morpheme boundary too far RIGHT filtering over clusters with the");
		System.err.println("  following parameter settings?");
		System.err.println("------------------------------------------------------------");
		System.err.println();
		System.err.println(filterParameters.toString());
		System.err.println();
		System.err.println("  <y/n> ?");
		System.err.println();
		System.err.print("> ");
		String choice;
		try {
			choice = stdin.readLine();
		}
		catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
		
		if ( ! choice.matches("y|Y")) {
			System.err.println();
			System.err.println("  Aborting this filter.  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
			
			return true;  // yes, continue at the current menu level
		}
		
		
		boolean filteringSucceeded = 
			searchBatch.doMorphemeBoundaryTooFarRightFilterOnClusters(filterParameters);
		
		// If filtering doesn't succeed then some failure message will be printed in place of
		// the following success message.
		if (filteringSucceeded) {
			System.err.println();
			System.err.println("Morpheme Boundary too far RIGHT filtering of Clusters complete.  ");
			System.err.println("  Returning to the " + previousMenuName);
		}
		
		System.err.println();
		System.err.println("  Press Enter to Continue...");
		try {stdin.readLine();}	catch (IOException e) {}  // This error should never happen		
				
		return false;  // We are all done with this bottomUp search, so go back to the generic search menu
	}
	
	public void printOptions() {
		System.err.println();
		System.err.println("----------------------------------------------");
		System.err.println("   " + MENU_NAME);
		System.err.println("----------------------------------------------");
		System.err.println();
		System.err.println(searchBatch);
		System.err.println();
		System.err.println("  Morpheme Boundary Too Far RIGHT Filter current parameters");
		System.err.println("-----------------------------------------------------------------");
		System.err.println(filterParameters.toString());
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetLM>   <Set> <L>eft-looking <M>etric");
		System.err.println("  <SetLMC>  <Set> <L>eft-looking <M>etric <C>utoff parameters to search over");
		System.err.println();
		System.err.println("  <F>ilter");		
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
