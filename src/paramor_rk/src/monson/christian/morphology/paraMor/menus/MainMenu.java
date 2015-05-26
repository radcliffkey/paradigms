/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import monson.christian.morphology.paraMor.ParaMor;
import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.searchAndProcessing.BottomUpSearchInteractive;
import monson.christian.util.AbstractGUIMenu;

public class MainMenu extends AbstractGUIMenu {

	protected static final String MENU_NAME = "Root Menu";
	
	BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

	private ParaMor morphologyInducer;
	
	SearchAndProcessingMenu searchAndProcessingMenu;
	
	public MainMenu(ParaMor morphologyInducer) {
		this.morphologyInducer = morphologyInducer;
		
		searchAndProcessingMenu = 
			new SearchAndProcessingMenu(stdin, morphologyInducer, MENU_NAME);
	}
	
	@Override
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
			
			if (choice.matches("sc")) {
				CorpusMenu menu = new CorpusMenu(stdin, morphologyInducer.getCorpus(), MENU_NAME);
				menu.present("Set the corpus");
				
			} else if (choice.matches("cn")) {
				CreateNetworkMenu menu = new CreateNetworkMenu(stdin, morphologyInducer, MENU_NAME);
				menu.present();
				
			} else if (choice.matches("ine")) {
				doInteractiveNetworkExploration();
				
			} else if (choice.matches("sp|SP|Sp")) {
				searchAndProcessingMenu.present();
				
			} else if (choice.matches("q|Q")) {
				QuitMenu command = new QuitMenu(stdin);
				continueLoop = command.present();
				
			} else {
				InvalidMenuChoice command = new InvalidMenuChoice(stdin, choice);
				command.present();
		
			}
			
		} while (continueLoop);
	}


	
	private void doInteractiveNetworkExploration() {
		BottomUpSearchInteractive bottomUpSearchInteractive =
			new BottomUpSearchInteractive(
					(VirtualPartialOrderNetwork)morphologyInducer.getPartialOrderNetwork(),
					stdin);
		
		bottomUpSearchInteractive.search();
	}

	@Override
	public void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);
		System.err.println();
		System.err.println("Select an Action:");
		System.err.println("-----------------");
		System.err.println("  <SC> <S>et the <C>orpus.  Read in a corpus.  Set the corpus' settings.");
		System.err.println();
		System.err.println("  <CN> <C>ompute a Morphology Scheme <N>etwork");
		System.err.println();
		System.err.println("  <INE> <I>nteractive <N>etwork <E>xploration");
		System.err.println();
		System.err.println("  <SP>  <S>earch and <P>rocessing.  Search a Morphology Scheme Network");
		System.err.println("     And process any results of a search.");
		System.err.println();
		System.err.println("  <Q>uit");
		System.err.println();
		System.err.print("> ");
	}

}
