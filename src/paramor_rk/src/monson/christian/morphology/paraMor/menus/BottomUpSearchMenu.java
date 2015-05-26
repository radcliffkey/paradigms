/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import monson.christian.morphology.paraMor.networks.PartialOrderNetwork;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearch;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.VerticalMetric;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearch.BottomUpParameters;

public class BottomUpSearchMenu {

	private static final String MENU_NAME = "Bottom Up Search Menu";
	
	private BufferedReader stdin;

	private SearchBatch searchBatch;
	private BottomUpParameters bottomUpParameters = new BottomUpParameters();
	
	private String previousMenuName;
	
	
	public BottomUpSearchMenu(BufferedReader stdin, 
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
			
			if (choice.matches("sf")) {
				doSetStartFrom();
				
			} else if (choice.matches("setvm")) {
				doSetVerticalMetric();
				
			} else if (choice.matches("setbottommax")) {
				doSetBottomMax();
				
			} else if (choice.matches("setusees@l1")) {
				doSetUseExhaustiveSearchAtLevel1();
								
			} else if (choice.matches("setusecovered")) {
				doSetUseCovered();
				
			} else if (choice.matches("setselectwhich")) {
				doSetSelectWhich();
				
			} else if (choice.matches("setrmsta")) {
				doSetRequireMoreStemsThanAffixes();
				
			} else if (choice.matches("setvmc")) {
				doSetVerticalMetricCutoffs();
				
			} else if (choice.matches("settop")) {	
				doSetTopCutoffs();
				
			} else if (choice.matches("setbottom")) {
				doSetBottomCutoffs();
				
			} else if (choice.matches("setmaxl")) {
				doSetMaxLevelCutoffs();
				
			} else if (choice.matches("s")) {
				continueLoop = doBottomUpSearch();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetStartFrom() { 
		
		System.err.println("With the new (slot) way of doing morphemes " +
				"only starting from level 1 is implemented");
		
		BottomUpSearch.StartFrom startFrom = BottomUpSearch.StartFrom.ALL_LEVEL_1;
		
		bottomUpParameters.setStartFrom(startFrom);
	}

	private void doSetVerticalMetric() {
		
		String instructions = "Please select either the vertical metric " + 
							  VerticalMetric.RATIO +
							  " OR the vertical metric " + 
							  VerticalMetric.
							  	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED;
		SelectAnEnumValueMenu<VerticalMetric> selectAMetricMenu = 
			new SelectAnEnumValueMenu<VerticalMetric>(
					VerticalMetric.class, 
					stdin, 
					MENU_NAME, 
					instructions);
		VerticalMetric verticalMetric = selectAMetricMenu.present();
		if (verticalMetric == null) {
			return;
		}
		if ((verticalMetric != VerticalMetric.RATIO) &&
			(verticalMetric != VerticalMetric.LARGE_SAMPLE_BERNOULLI_TEST) &&
			(verticalMetric != 
				VerticalMetric.CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED)) {
			System.err.println();
			System.err.println("  Sorry, Bottom Up Search is only implemented using:");
			System.err.println("    \"Ratio,");
			System.err.println("    \"Large Sample Bernoulli Test\", or");
			System.err.println("    \"Constrained Likelihood Ratio of Bernoulli Trials - One Sided\"");
			System.err.println();
			System.err.println("  Please try again.");
			System.err.println();
			
			System.err.println("  Press Enter to continue...");
			System.err.print("> ");
			try { stdin.readLine();	} catch (IOException e) {}  // This should never happen

			return;
		}
		bottomUpParameters.setVerticalMetric(verticalMetric);
	}

	private void doSetBottomMax() {
		// Here I don't let you interactively RESET the BottomMax to NONE or null.  But I don't
		// want to deal with it today.
		NonNegativeIntegerInputMenu positiveIntegerInputMenu =
			new NonNegativeIntegerInputMenu(stdin,
										 MENU_NAME,
										 "Bottom Max: Please enter a Bottom Max size--Bottom Up Search" +
										 " will not start from any scheme with MORE than Bottom Max" +
										 " stems.");
		Integer bottomMax = positiveIntegerInputMenu.present();
		if (bottomMax != null) {
			bottomUpParameters.setBottomMax(bottomMax);
		}
	}
	
	private void doSetUseExhaustiveSearchAtLevel1() {
		String instructions = "'true' to perform exhaustive search at Level 1, disregarding the beam width" +
							  String.format("%n") +
							  "'false' to stick to the beam width, even at Level 1";
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
								 MENU_NAME,
								 instructions);
		Boolean useExhaustiveSearchAtLevel1 = booleanInputMenu.present();
		if (useExhaustiveSearchAtLevel1 != null) {
			bottomUpParameters.setExhaustiveSearchAtLevel1(useExhaustiveSearchAtLevel1);
		}
	}

	private void doSetUseCovered() {
		BooleanInputMenu booleanInputMenu = 
			new BooleanInputMenu(stdin,
								 MENU_NAME,
								 "Enter true to restrict search by using Alon's idea of " +
								 "covered Level 1 Schemes");
		Boolean useCovered = booleanInputMenu.present();
		if (useCovered != null) {
			bottomUpParameters.setUseCovered(useCovered);
		}
	}


	private void doSetSelectWhich() {
		SelectAnEnumValueMenu<BottomUpSearch.SelectWhich> selectAnEnumValueMenu =
			new SelectAnEnumValueMenu<BottomUpSearch.SelectWhich>(
					BottomUpSearch.SelectWhich.class,
					stdin,
					MENU_NAME,
					"Please choose whether to select only terminal schemes in paths or " +
					"ALL Schemes at level 2 or higher along any search path");
		BottomUpSearch.SelectWhich selectWhich = selectAnEnumValueMenu.present();
		if (selectWhich != null) {
			bottomUpParameters.setSelectWhich(selectWhich);
		}
	}

	private void doSetRequireMoreStemsThanAffixes() {
		String instructions = "'true' to not move to any scheme at which |Stems| <= |Affixes|" +
							  String.format("%n") +
							  "'false' to not look |Stems| vs. |Affixes| in this fashion.";
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean requireMoreStemsThanAffixes = booleanInputMenu.present();
		if (requireMoreStemsThanAffixes != null) {
			bottomUpParameters.setRequireMoreStemsThanAffixes(requireMoreStemsThanAffixes);
		}
	}
	
	private void doSetVerticalMetricCutoffs() {
		ListOfDoubleInputMenu listOfDoubleInputMenu = 
			new ListOfDoubleInputMenu(stdin, 
									  MENU_NAME, 
									  "Vertical Metric Cutoff: Please enter a list of cutoffs " +
									  "(real numbers) to search over with the " + 
									  bottomUpParameters.getVerticalMetric() + " metric.");
		List<Double> verticalMetricCutoffs = listOfDoubleInputMenu.present();
		if (verticalMetricCutoffs != null) {
			bottomUpParameters.setVerticalMetricCutoffs(verticalMetricCutoffs);
		}
	}
	
	private void doSetTopCutoffs() {
		ListOfIntegerInputMenu listOfIntegerInputMenu = 
			new ListOfIntegerInputMenu(stdin, 
									   MENU_NAME, 
									   "Top Cutoff: Please enter Top Cutoffs to search over, as integers.");
		List<Integer> topCutoffs = listOfIntegerInputMenu.present();
		if (topCutoffs != null) {
			bottomUpParameters.setTopCutoffs(topCutoffs);
		}
	}
	
	private void doSetBottomCutoffs() {
		ListOfIntegerInputMenu listOfIntegerInputMenu = 
			new ListOfIntegerInputMenu(stdin, 
									   MENU_NAME, 
									   "Bottom Cutoff: Please enter Bottom Cutoffs to search over, " +
									   "as integers.  Bottom Up Search will not start from any scheme" +
									   " with FEWER than Bottom Cutoff stems.");
		List<Integer> bottomCutoffs = listOfIntegerInputMenu.present();
		if (bottomCutoffs != null) {
			bottomUpParameters.setBottomCutoffs(bottomCutoffs);
		}
	}
	private void doSetMaxLevelCutoffs() {
		String instructions = "";
		instructions += String.format("Top Cutoff: Please enter the Maximum Level Cutoffs%n");
		instructions += String.format("to search over, as integers.%n");
		instructions += String.format("Type 'null' to search with NO Maximum Level");
			
		ListOfIntegerInputMenu listOfIntegerInputMenu = 
			new ListOfIntegerInputMenu(stdin, 
									   MENU_NAME, 
									   instructions);
		List<Integer> maxLevelCutoffs = listOfIntegerInputMenu.present();
		if (maxLevelCutoffs != null) {
			bottomUpParameters.setMaxLevelCutoffs(maxLevelCutoffs);
		}
	}

	
	// Returns false if we should completely stop doing this search and go back to the entirely
	// previous menu.
	private boolean doBottomUpSearch() {
		
		PartialOrderNetwork partialOrderNetwork = searchBatch.getSearchNetwork();
		if ( ! (partialOrderNetwork instanceof VirtualPartialOrderNetwork)) {
			System.err.println();
			System.err.println("Sorry, you must first build a Dynamic Dense or Virtual ");
			System.err.println("network before you may perform a seed growing oracle ");
			System.err.println("greedy search");
			System.err.println();
			System.err.println("  Press Enter to Continue...");
			try {stdin.readLine();}	catch (IOException e) {}  // This should never happen
			
			return false;  // go back to the previous menu to build a network
		}


		System.err.println();
		System.err.println("Do you wish to perform a series of bottom-up searches with the");
		System.err.println("  following parameter settings?");
		System.err.println("------------------------------------------------------------");
		System.err.println();
		System.err.println(bottomUpParameters.toString());
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
			System.err.println("  Aborting this search round.  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
			
			return true;  // continue at the current menu level
		}
		
		
		searchBatch.performSearchStep(bottomUpParameters);		
		
		System.err.println();
		System.err.println("Bottom-Up Search complete.  Returning to the " + previousMenuName);
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
		System.err.println("  Run the current Bottom-Up Search with the Parameters");
		System.err.println("--------------------------------------------------------");
		System.err.println(bottomUpParameters.toString());
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SF>             <S>earch <F>rom -- Select which level 1 nodes to start the search from");
		System.err.println("  <SetVM>          <Set> <V>ertical <M>etric");
		System.err.println("  <SetBottomMax>   Do not search from bottom schemes that contain MORE than Bottom Max stems.");
		System.err.println("  <SetUseES@L1>    <Set> <Use>ing <E>xhaustive <S>earch <@>(at) <L>evel <1>(one)");
		System.err.println("                     'True' will do exhaustive search from Level 1 schemes");
		System.err.println("                     'False' will stick to the specified Beam Width at Level 1");
		System.err.println("                             (as elsewhere).");
		System.err.println("  <SetUseCovered>  Turn Alon's Covering Search Restriction on and off");
		System.err.println("  <SetSelectWhich> Select only terminal Schemes, or all Schemes along a path");
		System.err.println("  <SetRMSTA>       <Set> <R>equire <M>ore <S>tems <T>han <A>ffixes");
		System.err.println();
		System.err.println("  <SetVMC>         <Set> <V>ertical <M>etric <C>utoff parameters to search over");
		System.err.println("  <SetTop>         <Set> <Top> cutoff parameters to search over -- No scheme");
		System.err.println("                     will be selected that has fewer than Top Cutoff stems.");
		System.err.println("  <SetBottom>      <Set> <Bottom> cutoff parameters to search over -- Do not");
		System.err.println("                     search from bottom schemes that contain FEWER than");
		System.err.println("                     Bottom Cutoff stems.");
		System.err.println("  <SetMaxL>        <Set> <Max>imum <L>evel cutoff parameters to search over");
		System.err.println();
		System.err.println("  <S>earch");		
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
