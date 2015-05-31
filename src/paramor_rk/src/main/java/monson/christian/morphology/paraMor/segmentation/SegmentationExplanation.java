/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.segmentation;

import java.io.Serializable;

abstract public class SegmentationExplanation implements Serializable {

	private static final long serialVersionUID = 1L;

	
	protected String word;    // The word being segmented
	
	public SegmentationExplanation(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}
}
