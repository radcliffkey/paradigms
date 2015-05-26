/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomSchemeSelector {

	private enum State { PARAMS, NUMBER_OF_SCHEMES, SCHEMES };
	
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
	
		int schemeCounter = 0;
		List<Integer> randomSample = new ArrayList<Integer>();
		String line;
		State state = State.PARAMS;
		while ((line = selectedSchemesReader.readLine()) != null) {
			switch(state) {
			case PARAMS: 
				if (line.matches("^----.*$")) {
					state = State.NUMBER_OF_SCHEMES;
				}
				break;
				
			case NUMBER_OF_SCHEMES:
				Pattern numOfSchemesPattern = Pattern.compile("  (\\d+) Schemes Selected");
				Matcher numOfSchemesMatcher = numOfSchemesPattern.matcher(line);
				if ( ! numOfSchemesMatcher.matches()) {
					state = State.PARAMS;
				} else {
					String numOfSchemesString = numOfSchemesMatcher.group(1);
					int numOfSchemes = Integer.valueOf(numOfSchemesString);
					for (int i=0; i<100; i++) {
						boolean foundNew = false;
						while ( ! foundNew) {
							int numberBetween0AndNumOfSchemes = getIntegerBetween0AndNumOfSchemes(numOfSchemes);
							if ( ! randomSample.contains(numberBetween0AndNumOfSchemes)) {
								foundNew = true;
								randomSample.add(numberBetween0AndNumOfSchemes);
							} else {
								@SuppressWarnings("unused") int j = 23;
							}
						}
					}
					Collections.sort(randomSample);
					state = State.SCHEMES;
				}
				break;
				
			case SCHEMES:
				if (line.matches(".*->.*")) {
					schemeCounter++;
					if (randomSample.contains(schemeCounter)) {
						System.out.println("Rank of Scheme: " + schemeCounter);
						System.out.println(line);
						line = selectedSchemesReader.readLine();
						System.out.println(line);
						line = selectedSchemesReader.readLine();
						System.out.println(line);
						line = selectedSchemesReader.readLine();
						System.out.println(line);
					}
				}
				break;
			}
			
		}
		
	}

	private static int getIntegerBetween0AndNumOfSchemes(int numOfSchemes) {
		double random = Math.random();
		random *= (double)numOfSchemes;
		int integerBetween0AndNumOfSchemes = (int)Math.round(random);
		return integerBetween0AndNumOfSchemes;
	}
}
