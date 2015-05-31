/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.IOException;

public abstract class AbstractGUIMenu {

	/**
	 * Runs the menu -- i.e. the menu is presented to the user.
	 * And the user is prompted to select an option.
	 * 
	 * @throws IOException
	 */
	public abstract void present() throws IOException;

	/**
	 * Prints all valid menu options.
	 */
	public abstract void printOptions();

}
