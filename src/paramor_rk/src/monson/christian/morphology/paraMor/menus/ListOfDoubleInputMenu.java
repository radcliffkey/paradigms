/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListOfDoubleInputMenu {
	private static final String MENU_NAME = "List of Double Input Menu";

	private String previousMenuName;
	private String instructions;

	BufferedReader stdin;
	
	public ListOfDoubleInputMenu(BufferedReader stdin, 
								 String previousMenuName, 
								 String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
	}
	
	public List<Double> present() {
		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			if (choice.matches(".*\\d.*")) {
				List<Double> toReturn = doListOfDouble(choice);
				if (toReturn != null) {
					return toReturn;
				}
				
			} else if (choice.matches("r|R")) {
				continueLoop = false;
				
			} else {
				InvalidMenuChoice invalidMenuChoice = new InvalidMenuChoice(stdin, choice);
				invalidMenuChoice.present();
			}
			
		} while (continueLoop);
		
		return null;
	}
	
	private List<Double> doListOfDouble(String fullList) {
		String[] doubleStrings = fullList.split("\\s*,\\s*");
		List<Double> doubles = new ArrayList<Double>();
	
		for (String doubleString : doubleStrings) {
			try {
				Double d = Double.parseDouble(doubleString);
				doubles.add(d);
				
			} catch (NumberFormatException e) {
				System.err.println("The entry: " + fullList);
				System.err.println("  is not a comma separated list of real numbers (doubles).");
				System.err.println("  Specifically \"" + doubleString + "\" is not a real number.");
				System.err.println("  Please try again.");
				System.err.println();
				System.err.println("  Press Enter to Continue...");
				
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen

				return null;
			}
		}
		
		return doubles;
	}

		
	private void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);
		System.err.println();
		System.err.println(instructions + ":");
		System.err.println("-----------------");
		System.err.println("  <#, #, #> Enter a comma separated list of real numbers (doubles)");
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}


}
