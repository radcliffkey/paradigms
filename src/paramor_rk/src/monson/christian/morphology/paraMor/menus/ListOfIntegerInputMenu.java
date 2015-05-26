/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListOfIntegerInputMenu {
	private static final String MENU_NAME = "List of Integer Input Menu";

	private String previousMenuName;
	private String instructions;

	BufferedReader stdin;
	
	public ListOfIntegerInputMenu(BufferedReader stdin, 
								  String previousMenuName, 
								  String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
	}
	
	public List<Integer> present() {
		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			if (choice.matches(".*((\\d)|(null)).*")) {
				List<Integer> toReturn = doListOfInteger(choice);
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
	
	private List<Integer> doListOfInteger(String fullList) {
		String[] integerStrings = fullList.split("\\s*,\\s*");
		ArrayList<Integer> integers = new ArrayList<Integer>();
	
		for (String integerString : integerStrings) {
			try {
				Integer integer = null;
				
				// Sometimes a user may actually want to enter a vaule of 'null' as one of the Integers
				if ( ! integerString.equals("null")) {
					integer = Integer.parseInt(integerString);	
				}
				integers.add(integer);
				
			} catch (NumberFormatException e) {
				System.err.println("The entry: " + fullList);
				System.err.println("  is not a comma separated list of integers.");
				System.err.println("  Specifically \"" + integerString + "\" is not an integer.");
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
		
		return integers;
	}

		
	private void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);
		System.err.println();
		System.err.println(instructions + ":");
		System.err.println("-----------------");
		System.err.println("  <#, #, #> Enter a comma separated list of integers.");
		System.err.println("              Enter NULL for the null Integer.");
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
