/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClusterTypeCoveredCounter {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		File corpus = new File(args[0]);
		
		BufferedReader selectedSchemesReader;
		
		try {
		selectedSchemesReader = new BufferedReader(
						new InputStreamReader(
							new FileInputStream(corpus),
							"utf8"));
		}
		catch(FileNotFoundException e) {	
			System.err.println();
			System.err.println("  Sorry.  The file: " + corpus.getAbsolutePath());
			System.err.println("    could not be read.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println("  Did NOT successfully set the corpus path.");
			return;
		}
		catch(Exception e) {
			System.err.println();
			System.err.println("  Sorry.  While opening the file: " + corpus.getAbsolutePath());
			System.err.println("    an error was encountered.  Here is the full Java error:");
			System.err.println();
			System.err.println(e.getMessage());
			System.err.println();
			System.err.println("  Did NOT successfully set the corpus path.");
			return;
		}	
	
		PrintWriter outResults = new PrintWriter(new BufferedWriter(new FileWriter("./junk.txt")),
				  true); // true to autoflush
		
		//Map<Integer, Integer> histogramOfTypesCovered = new TreeMap<Integer, Integer>();
		String line;
		while ((line = selectedSchemesReader.readLine()) != null) {
			Pattern numOfTypesPattern = Pattern.compile("^ \\| Types.*\\((\\d+)\\).*$");
			Matcher numOfTypesMatcher = numOfTypesPattern.matcher(line);
			boolean matches = numOfTypesMatcher.matches();
			if ( ! matches) {
				//System.err.println("YOW");
			} else {
				String numOfTypesString = numOfTypesMatcher.group(1);
				outResults.println(numOfTypesString);
			}
				/*
				int numOfTypes = Integer.valueOf(numOfTypesString);
				if ( ! histogramOfTypesCovered.containsKey(numOfTypes)) {
					histogramOfTypesCovered.put(numOfTypes, 0);
				}
				int numOfClustersThatCoverExactlyNumOfTypes =
					histogramOfTypesCovered.get(numOfTypes);
				histogramOfTypesCovered.put(numOfTypes, numOfClustersThatCoverExactlyNumOfTypes + 1 );
				*/
					
		}
		
		
		
	}
}
