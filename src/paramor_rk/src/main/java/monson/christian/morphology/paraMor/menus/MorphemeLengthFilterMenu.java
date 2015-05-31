/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import monson.christian.statistics.ListOfData.SummaryStat;

import monson.christian.morphology.paraMor.searchAndProcessing.MorphemeLength_Filter;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;

public class MorphemeLengthFilterMenu {

	private static final String MENU_NAME = "Morpheme Length Filter";
	
	private BufferedReader stdin;

	private SearchBatch searchBatch;
	private MorphemeLength_Filter.Parameters filterParameters = 
		new MorphemeLength_Filter.Parameters();
	
	private String previousMenuName;
	
	
	public MorphemeLengthFilterMenu(BufferedReader stdin, 
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
			
			if (choice.matches("setmcnstf|setmcnftf")) {
				doSetMustContainNullAffixToFilter();
				
			} else if (choice.matches("setss")) {
				doSetSummaryStatistic();
				
			} else if (choice.matches("setssc")) {
				doSetSummaryStatisticCutoffs();
				
			} else if (choice.matches("f")) {
				continueLoop = doMorphemeLengthFilter();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetMustContainNullAffixToFilter() {
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin, 
								 MENU_NAME, 
								 "If you wish to require that a scheme contain " +
								 "the null affix in order to filter, then " +
								 "choose True, if not, then choose False.");
		Boolean mustContainNullAffixToFilter = booleanInputMenu.present();
		if (mustContainNullAffixToFilter != null) {
			filterParameters.setMustContainNullAffixToFilter(
					mustContainNullAffixToFilter);
		}
	}

	private void doSetSummaryStatistic() {
		
		String instructions = "Please select MEAN or MEDIAN to filter with";
		
		SummaryStat summaryStat;
		Boolean success = false;
		do {
			SelectAnEnumValueMenu<SummaryStat> selectAMetricMenu = 
				new SelectAnEnumValueMenu<SummaryStat>(
						SummaryStat.class, 
						stdin, 
						MENU_NAME, 
						instructions);
			summaryStat = selectAMetricMenu.present();
			if (summaryStat == null) {
				return;
			}
			if ((summaryStat == SummaryStat.MEAN) ||
			    (summaryStat == SummaryStat.MEDIAN)) {
				success = true;
			} else {
				System.err.println();
				System.err.println("I'm sorry.  Please select either MEAN or MEDIAN.");
				System.err.println();
				System.err.println("  Press Enter to Continue...");
				try {stdin.readLine();}	catch (IOException e) {}  // This error should never happen		
			}
		} while ( ! success);
		
		filterParameters.setSummaryStat(summaryStat);
	}

	private void doSetSummaryStatisticCutoffs() {
		ListOfDoubleInputMenu listOfDoubleInputMenu = 
			new ListOfDoubleInputMenu(stdin, 
									  MENU_NAME, 
									  "Please enter a list of cutoffs " +
									  "(real numbers) to filter over with the " + 
									  filterParameters.getSummaryStat() + " Summary Statistic.");
		List<Double> summaryStatisticCutoffs = listOfDoubleInputMenu.present();
		if (summaryStatisticCutoffs != null) {
			filterParameters.setSummaryCutoffs(summaryStatisticCutoffs);
		}
	}
		
	
	// Returns false if we should completely stop doing this search and go back to the entirely
	// previous menu.
	private boolean doMorphemeLengthFilter() {
		
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
			System.err.println("Morpheme length filtering complete.  ");
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
		System.err.println("  Morpheme Length Filter current parameters");
		System.err.println("-----------------------------------------------------------------");
		System.err.println(filterParameters.toString());
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetMCNSTF> <Set> <M>ust <C>ontain <N>ull <S>uffix <T>o <F>ilter");
		System.err.println("  <SetSS>     <Set> <S>ummary <S>tatistic");
		System.err.println("  <SetSSC>    <Set> <S>ummary <S>tatistic <C>utoff parameters to search over");
		System.err.println();
		System.err.println("  <F>ilter");		
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
