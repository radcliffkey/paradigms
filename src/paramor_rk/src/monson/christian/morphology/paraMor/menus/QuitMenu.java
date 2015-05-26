/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

public class QuitMenu {

	private BufferedReader stdin;

	public QuitMenu(BufferedReader stdin) {
		this.stdin = stdin;
	}
	
	/**
	 * 
	 * @param stdin
	 * @return false if the user truely does wish to quit
	 */
	public boolean present() {
		System.err.println("Are you certain you wish to quit? (y/n)");
		System.err.println();
		System.err.print("> ");

		String choice = "";
		try {
			choice = stdin.readLine();
		} catch (IOException e) {
			e.printStackTrace();  // This should never happen
		}
		
		if (choice.matches("y|Y")) {
			return false;
		}
		
		return true;
	}

}
