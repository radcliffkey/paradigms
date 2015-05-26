/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.initialization;

import java.lang.reflect.InvocationTargetException;

import monson.christian.morphology.paraMor.ParaMor;

abstract class AbstractInitFileCommand {
	
	/**
	 * Attempt to parse the line <code>lcLine</code> appropriately for the given
	 * type of command.  Most instantiations of the parseCommand() method have side
	 * effects that change the state of the parameter morphologyInducer.
	 * 
	 * @param morphologyInducer After a successful parse of any given command,
	 *                          parseCommand() may change the state of morphologyInducer.
	 * @param line The line from the initializations file we belive is a command of the
	 *               current type.
	 * @param pathToInitFile The path to the initializations file where the 'corpus'
	 *                       command occured. 
	 * @param lineNum The line number from the initializations file where <code>lcLine</code>
	 *                occured.
	 * @return <code>true&nbsp;</code> if parse succeeds
	 *     <br><code>false</code>      if parse fails
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	abstract public boolean parseCommand(ParaMor morphologyInducer,
										 String line, 
										 String pathToInitFile, 
										 int lineNum) /*throws SecurityException, 
										 					 IllegalArgumentException, 
										 					 NoSuchMethodException, 
										 					 InstantiationException, 
										 					 IllegalAccessException, 
										 					 InvocationTargetException*/;		
	
	protected static void handleParseError(String errorMsg, 
										   String pathToInitFile, 
										   int lineNum) {
		System.err.println();
		System.err.println("Syntax Error");
		System.err.println("  Line: " + lineNum + " of initFile:");
		System.err.println("    " + pathToInitFile);
		System.err.println();
		System.err.print("  ");
		System.err.println(errorMsg);
		System.err.println();
		InitFile.printHelp();
	}
}

