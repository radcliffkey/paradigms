/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileInputMenu {

	private static final String MENU_NAME = "Filename Querry Menu";

	private String previousMenuName;
	private String instructions;

	BufferedReader stdin;
	
	public FileInputMenu(BufferedReader stdin, 
						 String previousMenuName, 
						 String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
	}

	public File present() {
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
				File file = new File(choice);
				return file;
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
		System.err.println("  Enter a fully qualified file name");
		System.err.println("  <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}

}
