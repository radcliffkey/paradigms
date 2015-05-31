/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.io.Serializable;


/**
 * Marker class.  BottomUpSearch is a SearchStep, any search filters are SearchSteps, etc.
 */
public interface SearchStep extends Serializable {

	/**
	 * A marker class.  Each SearchStep must subclass SearchStepParameters.  The subclass can then hold
	 * any particular set of ParameterSettings for that SearchStep. 
	 * 
	 * @author cmonson
	 *
	 */
	public static abstract
	class SearchStepParameters implements Iterable<SearchStepParameterSetting>, Serializable {

		private static final long serialVersionUID = 1L;
		
		public abstract String getAssociatedSearchStepName();
		protected abstract String getParametersString(); 
		protected abstract String getParametersStringAsComment();
		
		/**
		 * @return a String that is the titles of the columns in the evaluation results.
		 * 			<code>null</code> if this SearchStep has no printable parameters.
		 */
		public abstract String getColumnTitleStringForGlobalScoreForSpreadsheet();
		
		@Override
		public String toString() {
			String toReturn = "";
			toReturn += getAssociatedSearchStepName();
			toReturn += String.format("%n-----------------------------------------------%n");
			toReturn += getParametersString();
			return toReturn;
		}

		public String toStringAsComment() {
			String toReturn = "";
			toReturn += "# " + getAssociatedSearchStepName();
			toReturn += String.format("# %n# -----------------------------------------------%n");
			toReturn += getParametersStringAsComment();
			return toReturn;
		}

	}

	
	/** 
	 * Marker class.  Any SearchStep must contain a subclass of SearchStepParameterSetting.  This
	 * subclass contains one setting of parameters for that SearchStep.
	 * 
	 * @author cmonson
	 *
	 */
	public static abstract 
	class SearchStepParameterSetting implements Comparable<SearchStepParameterSetting>, Serializable {

		private static final long serialVersionUID = 1L;
		
		public abstract String getStringForSpreadsheet();
		
		public abstract String getFilenameUniqueifier();

		
		protected Class associatedSearchStep;
		
		/** 
		 * Each subclass of SearchStepParameterSetting should @Override this compareTo function.
		 * This compareTo handles the case when this subClass and that subClass are different.
		 * 
		 * @param that
		 * @return
		 */
		public int compareTo(SearchStepParameterSetting that) {
			Class<? extends SearchStepParameterSetting> thisClass = this.getClass();
			Class<? extends SearchStepParameterSetting> thatClass = that.getClass();
			
			String thisClassCononicalName = thisClass.getCanonicalName();
			String thatClassCononicalName = thatClass.getCanonicalName();
			
			return thisClassCononicalName.compareTo(thatClassCononicalName);
		}
		
		public Class getAssocatedSearchStep() {
			return associatedSearchStep;
		}
		
		public abstract String getAssociatedSearchStepName();
		protected abstract String getParameterString(); 
		
		@Override
		public String toString() {
			String toReturn = "";
			toReturn += getAssociatedSearchStepName();
			toReturn += String.format("%n-----------------------------------------------%n");
			toReturn += getParameterString();
			return toReturn;
		}

	}

	
	/**
	 * The <code>perform()</code> method executes the meat of the <code>SearchStep</code>.
	 * A list of <code>SearchPath</code>'s is returned that is the result of performing
	 * this <code>SearchStep</code>.
	 * 
	 * @return
	 */
	public abstract SearchPathList performSearchStep();
	
	public abstract String getName();
}
