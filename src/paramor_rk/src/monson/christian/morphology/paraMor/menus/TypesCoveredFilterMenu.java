/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.TypesCovered_Filter;

public class TypesCoveredFilterMenu {

	private static final String MENU_NAME = "Types Covered Filter";
	
	private BufferedReader stdin;

	private SearchBatch searchBatch;
	private TypesCovered_Filter.Parameters filterParameters = 
		new TypesCovered_Filter.Parameters();
	
	private String previousMenuName;
	
	
	public TypesCoveredFilterMenu(BufferedReader stdin, 
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
			
			if (choice.matches("setns|setnf")) {
				doSetMustContainNullSuffixToFilter();
				
			} else if (choice.matches("setl")) {
				doSetMustBeLevelN_orLessToFilter();
				
			} else if (choice.matches("setctc")) {
				doSetCoveredTypesCutoffs();
				
			} else if (choice.matches("f")) {
				continueLoop = doCoveredTypesFilter();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetMustContainNullSuffixToFilter() {
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin, 
								 MENU_NAME, 
								 "If you wish to require that a scheme contain the null suffix " +
								 "in order to filter, then choose True, if not, then choose False.");
		Boolean mustContainNullSuffixToFilter = booleanInputMenu.present();
		if (mustContainNullSuffixToFilter != null) {
			filterParameters.setMustContainNullAffixToFilter(mustContainNullSuffixToFilter);
		}
	}
	
	private void doSetMustBeLevelN_orLessToFilter() {
		ListOfIntegerInputMenu listOfIntegerInputMenu = 
			new ListOfIntegerInputMenu(stdin, 
									  MENU_NAME, 
									  "Please enter a list of levels (integers). " +
									  "No schemes will be filtered that are at a HIGHER " +
									  "level than that entered.  A level of NULL means" +
									  "ALL schemes will be subject to filtering.");
		List<Integer> mustBeLevelN_orLessToFilter = listOfIntegerInputMenu.present();
		if (mustBeLevelN_orLessToFilter != null) {
			filterParameters.setMustBeLevelN_orLess(mustBeLevelN_orLessToFilter);
		}		
	}

	private void doSetCoveredTypesCutoffs() {
		ListOfIntegerInputMenu listOfIntegerInputMenu = 
			new ListOfIntegerInputMenu(stdin, 
									  MENU_NAME, 
									  "Please enter a list of cutoffs " +
									  "(integers) to filter over.  ");
		List<Integer> coveredTypesCutoffs = listOfIntegerInputMenu.present();
		if (coveredTypesCutoffs != null) {
			filterParameters.setSchemeMustCoverAtLeastNtypes(coveredTypesCutoffs);
		}
	}
		
	
	// Returns false if we should completely stop doing this search and go back to the entirely
	// previous menu.
	private boolean doCoveredTypesFilter() {
		
		System.err.println();
		System.err.println("Do you wish to perform morpheme length filtering with the");
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
		
		
		boolean filteringSucceeded = searchBatch.performSearchStep(filterParameters);
		
		// If filtering doesn't succeed then some failure message will be printed in place of
		// the following success message.
		if (filteringSucceeded) {
			System.err.println();
			System.err.println("Covered Types filtering complete.  ");
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
		System.err.println("  Covered Types Filter current parameters");
		System.err.println("-----------------------------------------------------------------");
		System.err.println(filterParameters.toString());
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetNS>     <Set> Must Contain <N>ull <S>uffix to Filter");
		System.err.println("  <SetL>      <Set> Must be <L>evel N or Less to Filter");
		System.err.println("  <SetCTC>    <Set> <C>overed <T>ypes <C>utoff parameters to search over");
		System.err.println();
		System.err.println("  <F>ilter");		
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
