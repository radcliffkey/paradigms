/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

public class BooleanInputMenu {

	private static final String MENU_NAME = "Positive Integer Querry Menu";

	private String previousMenuName;
	private String instructions;

	BufferedReader stdin;
	
	public BooleanInputMenu(BufferedReader stdin, 
									String previousMenuName, 
									String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
	}
	
	public Boolean present() {
		boolean continueLoop = true;
		String choice;
		do {
			printOptions();
			
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			choice = choice.toLowerCase();
			
			if (choice.matches("t")) {
				return true;
				
			} else if (choice.matches("f")) {
				return false;
				
			} else if (choice.matches("r")) {
				continueLoop = false;
				
			} else {
				InvalidMenuChoice invalidMenuChoice = new InvalidMenuChoice(stdin, choice);
				invalidMenuChoice.present();
				
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
		System.err.println("  <T>rue");
		System.err.println("  <F>alse");
		System.err.println();
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}
	
	
}
