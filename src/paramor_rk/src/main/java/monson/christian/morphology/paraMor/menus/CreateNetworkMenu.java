/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

import monson.christian.morphology.paraMor.ParaMor;
import monson.christian.morphology.paraMor.networks.PartialOrderNetwork;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;

public class CreateNetworkMenu {

	private static final String MENU_NAME = "Create Network Menu";
	
	private BufferedReader stdin;

	private ParaMor morphologyInducer;
	private PartialOrderNetwork.Identifier networkIdentifier;

	private String previousMenuName;
	
	public 
	CreateNetworkMenu(
			BufferedReader stdin, 
			ParaMor morphologyInducer, 
			String previousMenuName) {
		
		this.stdin = stdin;
		this.morphologyInducer = morphologyInducer;
		this.previousMenuName = previousMenuName;
		
		networkIdentifier =
			new PartialOrderNetwork.Identifier(
					VirtualPartialOrderNetwork.class,
					morphologyInducer.getCorpus(),
					PartialOrderNetwork.MorphemicAnalysis.SUFFIX,
					false);
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
			
			if (choice.matches("v")) {
				networkIdentifier.setTheNetworkClass(VirtualPartialOrderNetwork.class);
				
			} else if (choice.matches("setma")) {
				doSetMorphemicAnalysis();
				
			} else if (choice.matches("setaes")) {
				doSetAllowEmptyStems();
				
			} else if (choice.matches("cn")) {
				PartialOrderNetwork partialOrderNetwork = 
					PartialOrderNetwork.factory(networkIdentifier);
				morphologyInducer.setPartialOrderNetwork(partialOrderNetwork);
				continueLoop = false;
					
			} else if (choice.matches("r|R")) {
				continueLoop = false;
				
			} else {
				new InvalidMenuChoice(stdin, choice).present();
			}
			
		} while (continueLoop);
	}

	private void doSetMorphemicAnalysis() {
		SelectAnEnumValueMenu<PartialOrderNetwork.MorphemicAnalysis> menu = 
			new SelectAnEnumValueMenu<PartialOrderNetwork.MorphemicAnalysis>(
					PartialOrderNetwork.MorphemicAnalysis.class, 
					stdin, 
					MENU_NAME, 
					"Select the type of morphemic analysis to perform");
		
		PartialOrderNetwork.MorphemicAnalysis morphemicAnalysis = menu.present();
		if ( morphemicAnalysis != null) {
			networkIdentifier.setMorphemicAnalysis(morphemicAnalysis);
		}
	}
	
	private void doSetAllowEmptyStems() {
		String instructions = "'true' to allow empty stems, i.e. *null* or _ stems. " + 
							  String.format("%n") +
							  "'false' to *not* allow empty stems";
		
		BooleanInputMenu booleanInputMenu =
			new BooleanInputMenu(stdin,
					MENU_NAME,
					instructions);
		Boolean allowEmptyStems = booleanInputMenu.present();
		if (allowEmptyStems != null) {
			networkIdentifier.setAllowEmptyStems(allowEmptyStems);
		}	
	}

	public void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);
		System.err.println();
		System.err.println("Current Network Creation Settings");
		System.err.println("---------------------------");
		System.err.println(networkIdentifier);
		System.err.println();
		System.err.println("Change the Network Creation Settings or Create a Network:");
		System.err.println("  To change corpus settings, go to the corpus creation menu (located elsewhere)");
		System.err.println("-----------------");
		System.err.println("  <V>    Prepare to create a <V>irtual Network");
		System.err.println();
		System.err.println("  <setMA> <Set> the <M>orphemic <A>analysis to be used when creating the network");
		System.err.println("  <setAES> <Set> <A>llow <E>mpty <S>tems");
		System.err.println();
		System.err.println("  <CN> <C>reate a <N>etwork with the current Network Creation Settings");
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
