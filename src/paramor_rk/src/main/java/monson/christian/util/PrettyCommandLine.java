/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PrettyCommandLine {

	private PrettyCommandLine() {
		// You can't create a PrettyCommandLine
	}
	
	/**
	 * Pretty print a Unix-style commandline execution. 
	 * 
	 * Blocks until the commandline command terminates execution.
	 * 
	 * Returns the exit status of the command. By convention 0 means success.
	 * 
	 * @param commandline
	 * @return
	 */

	public static int runCommandline(String commandline) {
		int processExitValue = 0;
		try {
			System.out.println();
			System.out.println("Running the command:");
			System.out.println();
			System.out.println("  " + commandline);
			System.out.println();
			
			// Use this syntax to allow commandline to contain unix redirection ('|' and '>', etc.)
			// This syntax explicitly invokes a unix shell
			String[] args = {"sh", "-c", commandline};
			
			Process process = Runtime.getRuntime().exec(args);
			
			process.waitFor();
			
			processExitValue = process.exitValue();
			if (processExitValue == 0) {
				System.out.println("Command Succeeded.");
				System.out.println();
			} else {
				System.out.println("Command Failed:");
				System.out.println();
			}
			String processStdout = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
			if (processStdout != null) {
				System.out.println("  Stdout says:");
				System.out.println();
				System.out.println("    " + processStdout);
				System.out.println();
			}
			String processStderr = new BufferedReader(new InputStreamReader(process.getErrorStream())).readLine();
			if (processStderr != null) {
				System.err.println("  Stderr says:");
				System.err.println();
				System.err.println("    " + processStderr);
				System.err.println();
			}
			if ((processExitValue != 0) && (processStdout == null) && (processStderr == null)) {
				System.out.println("  Failed for an unknown reason (stdout and stderr are both null).");
				System.out.println();
			}
			System.out.println();
				
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return processExitValue;
	}

}
