/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 
 * @author cmonson
 *
 * @param <E> This class is parameterized by an Enum, one of whose values a user may select.
 */
public class SelectAnEnumValueMenu<E extends Enum<E>> {

	BufferedReader stdin;
	
	private E[] enumValues;

	private static final String MENU_NAME = "Select a Metric Menu";
	
	private String previousMenuName;
	
	private String instructions;

	
	public SelectAnEnumValueMenu(Class<E> enumClass, 
								 BufferedReader stdin, 
								 String previousMenuName,
								 String instructions) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
		this.instructions = instructions;
		
		this.enumValues = enumClass.getEnumConstants();
	}

	public E present() {
		E enumValue = null;
		boolean successfullySelected = false;
		while ( ! successfullySelected) {
			printOptions();
			
			String choice;
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			if (choice.matches("r|R")) {
				return null;
			}
			
			int enumValueIndex;
			try {
				enumValueIndex = Integer.parseInt(choice);
			}
			catch (NumberFormatException e) {
				System.err.println();
				System.err.println("Please Enter an integer index.  [Press Enter to continue...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
			

			try {
				enumValue = enumValues[enumValueIndex];
				successfullySelected = true;
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.err.println();
				System.err.println("Please select an entry between the indexes 0 and " +
						(enumValues.length-1));
				System.err.println();
				System.err.println("[Press Enter to continue...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
		}
		
		return enumValue;
	}

	public void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);		
		System.err.println();
		System.err.println(instructions + ":");
		System.err.println("------------------------------------------------------------------");
		for (int enumValueIndex = 0; 
			 enumValueIndex < enumValues.length; 
			 enumValueIndex++) {
			
			E enumValue = enumValues[enumValueIndex];
			
			System.err.println(" [" + enumValueIndex + "] " + enumValue);
		}
		System.err.println();
		System.err.println(" <R>eturn to the " + previousMenuName);
		System.err.println();
		System.err.print("> ");
	}
}
