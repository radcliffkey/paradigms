/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

public class InvalidMenuChoice {

	BufferedReader stdin;
	String invalidString;
	
	public InvalidMenuChoice(BufferedReader stdin, String invalidString) {
		this.stdin = stdin;
		this.invalidString = invalidString;
	}

	public void present() {
		System.err.println();
		System.err.println("I'm Sorry, I did not understand the command '" + invalidString + "'.");
		System.err.println("  Please press Enter.  And then supply a new command...");
		
		try {
			stdin.readLine();
		}
		catch (IOException e) {e.printStackTrace();}  // This should never happen
	}

}
