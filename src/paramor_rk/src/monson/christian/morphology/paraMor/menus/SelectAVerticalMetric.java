/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.menus;

import java.io.BufferedReader;
import java.io.IOException;

import monson.christian.morphology.paraMor.searchAndProcessing.VerticalMetric;


// TODO: Remove this class once SelectAnEnumValueMenu is working
//
//
//is it worth it to try to generalize this class to be a selectAnEnumValue method?

/**
 * 
 * @author cmonson
 *
 * @param <E> This class is parameterized by an Enum, one of whose values a user may select.
 */
public class SelectAVerticalMetric {

	BufferedReader stdin;
	
	static final VerticalMetric[] verticalMetrics = VerticalMetric.values();

	private static final String MENU_NAME = "Select a Metric Menu";
	
	private String previousMenuName;
	
	public SelectAVerticalMetric(BufferedReader stdin, String previousMenuName) {
		this.stdin = stdin;
		this.previousMenuName = previousMenuName;
	}

	public VerticalMetric present() {
		VerticalMetric verticalMetric = null;
		boolean successfullySelected = false;
		while ( ! successfullySelected) {
			printOptions();
			
			String choice;
			try {
				choice = stdin.readLine();
			}
			catch (IOException e) {choice = "<" + e + ">";}  // This should never happen
			
			int verticalMetricIndex;
			try {
				verticalMetricIndex = Integer.parseInt(choice);
			}
			catch (NumberFormatException e) {
				System.err.println();
				System.err.println("Please Enter an integer index.  [Press Enter...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
			

			try {
				verticalMetric = verticalMetrics[verticalMetricIndex];
				successfullySelected = true;
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.err.println();
				System.err.println("Please select a vertical metric between the indexes 0 and " +
						(verticalMetrics.length-1));
				System.err.println("[Press Enter...]");
				try {
					stdin.readLine();
				}
				catch (IOException e2) {e2.printStackTrace();}  // This should never happen
				continue;
			}
		}
		
		return verticalMetric;
	}

	public void printOptions() {
		System.err.println();
		System.err.println(MENU_NAME);		
		System.err.println();
		System.err.println("Select a Metric to return to the " + previousMenuName + ":");
		System.err.println("------------------------------------------------------------------");
		for (int verticalMetricIndex = 0; 
			 verticalMetricIndex < verticalMetrics.length; 
			 verticalMetricIndex++) {
			
			VerticalMetric verticalMetric = verticalMetrics[verticalMetricIndex];
			
			System.err.println("[" + verticalMetricIndex + "] " + verticalMetric);
		}
		System.err.println();
		System.err.print("> ");
	}
}
