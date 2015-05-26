/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.List;


public abstract class NetworkSearchProcedure {
	
	/**
	 * A marker interface.  Any class that can be the end result of a network search 
	 * (such as a <code>Scheme</code>) must implement this interface.
	 * 
	 * @author cmonson
	 *
	 */
	public static interface Result {}
	
	public abstract List<? extends NetworkSearchProcedure.Result> search();
	 
}
