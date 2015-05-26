/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultClustering;
import monson.christian.morphology.paraMor.searchAndProcessing.SearchBatch;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster.CalculateSimilarityAs;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchResultCluster.ClusterWRT;

public class ClusteringMenu {

	private static final String MENU_NAME = "Clustering Menu";
	
	private BufferedReader stdin;

	private SearchBatch searchBatch;
	
	private BottomUpSearchResultClustering.Parameters clusteringParameters = 
		new BottomUpSearchResultClustering.Parameters();
	
	private String previousMenuName;
	
	
	public ClusteringMenu(BufferedReader stdin, 
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
			
			if (choice.matches("setcsa")) {
				doSetCalculateSimilarityAs();

			} else if (choice.matches("setcwrt")) {
				doSetClusterWithRespectTo();
				
			} else if (choice.matches("setfact")) {
				doSetFilterAtClusterTime();
				
			} else if (choice.matches("setdc")) {
				doSetDiscriminativeClustering();
				
			} else if (choice.matches("setnbdc")) {
				doSetNetworkBasedDiscriminativeClustering();
				
			} else if (choice.matches("setl2smhns")) {
				doSetLevel2SchemeMustHaveAtLeastNStems();
				
			} else if (choice.matches("setaicr")) {
				doSetAffixesInCommonRequired();
				
			} else if (choice.matches("setaicf")) {
				doSetAffixesInCommonForbidden();
				
			} else if (choice.matches("setctcc")) {
				doSetChildTypesCoveredCutoff();
				
			} else if (choice.matches("setumc")) {
				doSetUseMergeCredits();
				
			} else if (choice.matches("setpmc")) {
				doSetPositiveMergeCredits();
				
			} else if (choice.matches("settitcf")) {
				doSetTieInTypeCoveredFilter();
				
			} else if (choice.matches("c")) {
				continueLoop = cluster();
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}


	private void doSetCalculateSimilarityAs() {
		String instructions = "Please choose a clustering similarity measure";
		SelectAnEnumValueMenu<CalculateSimilarityAs> selectASimilarityMeasureMenu =
			new SelectAnEnumValueMenu<CalculateSimilarityAs>(CalculateSimilarityAs.class,
					stdin,
					MENU_NAME,
					instructions);
		BottomUpSearchResultCluster.CalculateSimilarityAs value = selectASimilarityMeasureMenu.present();
		if (value != null) {
			clusteringParameters.setCalculateSimilarityAs(value);
		}
	}

	private void doSetClusterWithRespectTo() {
		String instructions = "Please choose what to cluster with respect to";
		SelectAnEnumValueMenu<ClusterWRT> selectAClusteringRestrictionMenu =
			new SelectAnEnumValueMenu<ClusterWRT>(ClusterWRT.class,
					stdin,
					MENU_NAME,
					instructions);
		BottomUpSearchResultCluster.ClusterWRT value = selectAClusteringRestrictionMenu.present();
		if (value != null) {
			clusteringParameters.setClusterWRT(value);
		}
	}	
	
	
	private void doSetFilterAtClusterTime() {
		String instructions = "'true' to filter at cluster time: " + String.format("%n") +
							  "'false' to postpone filtering and not throw any scheme away during clustering" +
							  String.format("%n");
		
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean filterAtClusterTime = booleanInputMenu.present();
		if (filterAtClusterTime != null) {
			clusteringParameters.setFilterAtClusterTime(filterAtClusterTime);
		}
	}

	
	private void doSetDiscriminativeClustering() {
		String instructions = "'true' to perform discriminative clustering: i.e. only merge clusters" + 
							  String.format("%n") +
							  "  if every pair of affixes in the resulting cluster occured in some" +
							  String.format("%n") +
							  "  original leaf cluster/scheme." + String.format("%n") +
							  "'false' to *not* perform discriminative clustering";
				
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean useDiscriminativeClustering = booleanInputMenu.present();
		if (useDiscriminativeClustering != null) {
			clusteringParameters.setDiscriminativeClustering(useDiscriminativeClustering);
		}
	}

	private void doSetNetworkBasedDiscriminativeClustering() {
		String instructions = "'true' to perform network based discriminative clustering: " + 
							  String.format("%n") +
							  "  i.e. only merge clusters if every pair of affixes in the " +
							  String.format("%n") +
							  "  resulting cluster occurs in a level 2 scheme in the network " + 
							  String.format("%n") +
							  "  with at least 1 context." + String.format("%n") +
							  "'false' to *not* perform network based discriminative clustering";
		
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean useNetworkBasedDiscriminativeClustering = booleanInputMenu.present();
		if (useNetworkBasedDiscriminativeClustering != null) {
			clusteringParameters.setNetworkBasedDiscriminativeClustering(
					useNetworkBasedDiscriminativeClustering);
		}	
	}
	
	private void doSetLevel2SchemeMustHaveAtLeastNStems() {
		NonNegativeIntegerInputMenu positiveIntegerInputMenu =
			new NonNegativeIntegerInputMenu(
					stdin,
					MENU_NAME,
					"Enter the number of stems that each level 2 scheme must (at least) contain when " +
					"network based discriminative clustering is in force");
		Integer level2SchemeMustContainAtLeastNStems = positiveIntegerInputMenu.present();
		if (level2SchemeMustContainAtLeastNStems != null) {
			clusteringParameters.setLevel2SchemeMustHaveAtLeastNStems(level2SchemeMustContainAtLeastNStems);
		}
	}

	private void doSetAffixesInCommonRequired() {
		String instructions = "'true' to only merge 2 clusters if they share at least one affix: " + 
							  String.format("%n") +
							  "'false' to *not* require merged clusters to share an affix";
		
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean affixesInCommonRequired = booleanInputMenu.present();
		if (affixesInCommonRequired != null) {
			clusteringParameters.setAffixesInCommonRequired(affixesInCommonRequired);
		}	
	}


	private void doSetAffixesInCommonForbidden() {
		String instructions = "'true' to only merge 2 clusters if they share NO affixes in common: " + 
							String.format("%n") +
							"'false' to *not* require that merged clusters be disjoint";
		
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean affixesInCommonForbidden = booleanInputMenu.present();
		if (affixesInCommonForbidden != null) {
			clusteringParameters.setAffixesInCommonForbidden(affixesInCommonForbidden);
		}	
	}
	


	private void doSetChildTypesCoveredCutoff() {
		ListOfIntegerInputMenu listOfIntegerInputMenu =
			new ListOfIntegerInputMenu(
					stdin,
					MENU_NAME,
					"Enter the number of types that at least 1 child scheme (or cluster) must cover " +
					"to merge any pair of schemes (or clusters)");
		List<Integer> childTypeCoveredCutoffs = listOfIntegerInputMenu.present();
		if (childTypeCoveredCutoffs != null) {
			clusteringParameters.setChildTypesCoveredCutoffs(childTypeCoveredCutoffs);
		}
	}

	private void doSetUseMergeCredits() {
		String instructions = "'true' to use merge credits during clustering, used with the " +
							  "'child types covered cutoff'" +
							  String.format("%n") +
							  "'false' to *not* use merge credits during clustering";

		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean useMergeCredits = booleanInputMenu.present();
		if (useMergeCredits != null) {
			clusteringParameters.setUseMergeCredits(useMergeCredits);
		}	
	}


	private void doSetPositiveMergeCredits() {
		NonNegativeIntegerInputMenu positiveIntegerInputMenu =
			new NonNegativeIntegerInputMenu(
					stdin,
					MENU_NAME,
					"Enter the number of positive merge credits to award clusters that cover " +
					"at least as many types as the 'child types covered cutoff'");
		Integer numOfPositiveMergerCredits = positiveIntegerInputMenu.present();
		if (numOfPositiveMergerCredits != null) {
			clusteringParameters.setNumOfPositiveMergerCredits(numOfPositiveMergerCredits);
		}
	}
	
	private void doSetTieInTypeCoveredFilter() {
		String instructions = "'true' to use the Child Type Covered Cutoff to remove any " +
		"schemes that are smaller than this threshold after clustering is complete." +
		String.format("%n") +
		"'false' to *not* do any type covered filtering after clustering is done.";

		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean tieInTypeCoveredFilter = booleanInputMenu.present();
		if (tieInTypeCoveredFilter != null) {
			clusteringParameters.setTieInTypeCoveredFiltering(tieInTypeCoveredFilter);
		}	
	}
	
	// Returns false if we should completely stop doing this clustering and go back to the entirely
	// previous menu.
	private boolean cluster() {
		
		System.err.println();
		System.err.println("Do you wish to cluster the selected schemes of the current");
		System.err.println("  bottom-up search following parameter settings?");
		System.err.println("------------------------------------------------------------");
		System.err.println();
		System.err.println(clusteringParameters.toString());
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
			System.err.println("  Aborting this clustering round.  Press Enter to continue...");
			try {
				stdin.readLine();
			}
			catch (IOException e) {}  // This should never happen
			
			return true;  // yes, continue at the current menu level
		}
		
		
		searchBatch.cluster(clusteringParameters);		
		
		System.err.println();
		System.err.println("Clustering complete.  Returning to the " + previousMenuName);
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
		System.err.println("  Cluster the current Bottom Up Search Results with the Parameters");
		System.err.println("------------------------------------------------------------------");
		System.err.println(clusteringParameters.toString());
		System.err.println();
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SetCSA>      <Set> <C>alculate <S>imilarity <A>s");
		System.err.println("  <SetCWRT>     <Set> <C>luster <W>ith <R>espect <T>o");
		System.err.println("  <SetFACT>     <Set> <F>ilter <A>t <C>luster <T>ime - An attempt to ");
		System.err.println("                        to consider each scheme as a cluster");
		System.err.println("                        candidate and as a candidate for");
		System.err.println("                        discard simultaneously.");
		System.err.println("  <SetDC>       <Set> <D>iscriminative <C>lustering");
		System.err.println("  <SetNBDC>     <Set> <N>etwork <B>ased <D>iscriminative <C>lustering");
		System.err.println("    <SetL2SMHNS <Set> <L>evel <2> <S>cheme <M>ust <H>ave at least <N> <S>tems");
		System.err.println("  <SetAICR>     <Set> <A>ffixes <I>n <C>ommon <R>equired");
		System.err.println("  <SetAICF>     <Set> <A>ffixes <I>n <C>ommon <F>orbidden");
		System.err.println("  <SetCTCC>     <Set> <C>hild <T>ype <C>overed <C>utoff");
		System.err.println("    <SetUMC>    <Set> <U>se <M>erge <C>redits");
		System.err.println("      <SetPMC>  <Set> <P>ositive <M>erge <C>redits");
		System.err.println("  <SetTITCF>    <Set> <T>ie <I>n <T>ype <C>overed <F>ilter");
		System.err.println("                        'true' to use the Child Type Covered");
		System.err.println("                        Cutoff to remove any schemes that are");
		System.err.println("                        smaller than this threshold after");
		System.err.println("                        clustering is complete");
		System.err.println();
		System.err.println("  <C>luster");		
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}	
}
