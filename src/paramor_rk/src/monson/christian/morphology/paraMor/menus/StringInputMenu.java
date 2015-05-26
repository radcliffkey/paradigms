/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

public class StringInputMenu {

	private static final String MENU_NAME = "String Querry Menu";

	private String previousMenuName;
	private String instructions;

	BufferedReader stdin;
	
	public StringInputMenu(BufferedReader stdin, 
						 String previousMenuName, 
						 String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
	}

	public String present() {
		boolean continueLoop = true;
		String choice = "";
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			if (choice.matches("r|R")) {
				continueLoop = false;
				
			} else {
				return choice;
			}
			
		} while (continueLoop);
		
		return null;
	}
	
	private void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);
		System.err.println();
		System.err.println(instructions + ":");
		System.err.println("-----------------");
		System.err.println("  Enter a string");
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
