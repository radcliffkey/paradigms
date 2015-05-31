/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.util;

import java.io.Serializable;
import java.util.Comparator;

public class ComparablePair<L extends Comparable<? super L>, R extends Comparable<? super R>> extends Pair<L, R> implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public ComparablePair() {
		super();

	}

	public ComparablePair(L left, R right) {
		super(left, right);
	}

	/**
	 * Compares two Pair<L, R> instances, first by their Lefts and then by their Rights.
	 * 
	 * @author cmonson
	 *
	 */
	public static class 
	ByLeft<LEFT extends Comparable<? super LEFT>, RIGHT extends Comparable<? super RIGHT>> 
	implements Comparator<ComparablePair<LEFT, RIGHT>> {

		public int compare(ComparablePair<LEFT, RIGHT> a, ComparablePair<LEFT, RIGHT> b) {

			int aComparedToB = a.left.compareTo(b.left);
			if (aComparedToB != 0) {
				return aComparedToB;
			}
			
			aComparedToB = a.right.compareTo(b.right);
			return aComparedToB;
		}
	}

	/**
	 * Compares two Pair<L, R> instances, a and b, first by their Lefts and then by their Rights.
	 * a < b if <b>a.left > b.left</b> (i.e. sorts into dereasing order).
	 *                 
	 * 
	 * @author cmonson
	 *
	 */
	public static class 
	ByLeftDecreasing<LEFT extends Comparable<? super LEFT>, RIGHT extends Comparable<? super RIGHT>> 
	implements Comparator<ComparablePair<LEFT, RIGHT>> {

		public int compare(ComparablePair<LEFT, RIGHT> a, ComparablePair<LEFT, RIGHT> b) {

			int bComparedToA = b.left.compareTo(a.left);
			if (bComparedToA != 0) {
				return bComparedToA;
			}
			
			bComparedToA = b.right.compareTo(a.right);
			return bComparedToA;
		}
	}
	
	/**
	 * Compares two Pair<L, R> instances, first by their Rights and then by their Lefts.
	 * 
	 * @author cmonson
	 *
	 */
	public static class 
	ByRight<LEFT extends Comparable<? super LEFT>, RIGHT extends Comparable<? super RIGHT>> 
	implements Comparator<ComparablePair<LEFT, RIGHT>> {

		public int compare(ComparablePair<LEFT, RIGHT> a, ComparablePair<LEFT, RIGHT> b) {

			int aComparedToB = a.right.compareTo(b.right);
			if (aComparedToB != 0) {
				return aComparedToB;
			}
			
			aComparedToB = a.left.compareTo(b.left);
			return aComparedToB;
		}
	}
	
	/**
	 * Compares two Pair<L, R> instances, a and b, first by their Rights and then by their Lefts.
	 * a < b if <b>a.right > b.right</b> (i.e. sorts into dereasing order).
	 *                 
	 * 
	 * @author cmonson
	 *
	 */
	public static class 
	ByRightDecreasing<LEFT extends Comparable<? super LEFT>, RIGHT extends Comparable<? super RIGHT>> 
	implements Comparator<ComparablePair<LEFT, RIGHT>> {

		public int compare(ComparablePair<LEFT, RIGHT> a, ComparablePair<LEFT, RIGHT> b) {

			int bComparedToA = b.right.compareTo(a.right);
			if (bComparedToA != 0) {
				return bComparedToA;
			}
			
			bComparedToA = b.left.compareTo(a.left);
			return bComparedToA;
		}
	}


}
