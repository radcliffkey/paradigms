/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import monson.christian.util.FileUtils.Encoding;


/**
 * Written for Ning-Ning and Evan (HS Students) in Jan. 2010.
 * 
 * This program reads in lines of a file and prints each line backward to a new file.
 * 
 * @author Christian Monson
 *
 */
public class LineReverser {
	
	BufferedReader inputFileReader = null;
	PrintWriter outputFileWriter = null;
	
	Encoding encoding = Encoding.UTF8;
	
	public LineReverser(File inputFile, File outputFile) {
		
		inputFileReader = 
			FileUtils.openFileForReading(inputFile, encoding);
		
		outputFileWriter = 
			FileUtils.openFileForWriting(outputFile, encoding);
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("The command line must look like:");
			System.out.println(
			 "    java LineReverser " + String.format("%n") +
			 "        <path-to-input-file> " + String.format("%n") +
			 "        <path-to-output-file>" + String.format("%n%n"));
			System.out.println("    Exiting...");
			System.out.println();
			System.out.println();
			System.exit(0);
		}
		
		LineReverser reverser = 
			new LineReverser(
					new File(args[0]), 
					new File(args[1]));
		
		reverser.reverse();
	}

	private void reverse() throws IOException {
		int lineCounter = 0;
		String line;
		while ((line = inputFileReader.readLine()) != null) {
			
			// skip blank lines
			//if (line.matches("^\\s*$")) {
			//	continue;
			//}
			
			// skip comment lines
			if (line.matches("^\\s*#.*$")) {
				continue;
			}
			
			lineCounter++;
			if ((lineCounter%1000) == 0) {
				System.err.println("  " + lineCounter + " " + line);
				System.err.flush();
			}
			
			for (int i=line.length()-1; i>=0; i--) {
				outputFileWriter.print(line.charAt(i));
			}
			outputFileWriter.println();
		}
	}
}
