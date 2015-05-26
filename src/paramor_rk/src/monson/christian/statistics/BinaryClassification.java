/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.statistics;

import java.io.Serializable;
import java.util.EnumMap;

public class BinaryClassification implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public enum Outcome {TRUE_POSITIVE, TRUE_NEGATIVE, FALSE_POSITIVE, FALSE_NEGATIVE};
	
	private EnumMap<Outcome, Integer> outcomes;
	private int total;
	private int marginalTruthPositive;
	private int marginalTruthNegative;
	private int marginalDecidedPositive;
	private int marginalDecidedNegative;
	
	public enum Measure {PRECISION, RECALL, FMEASURE, ACCURACY};

	
	/**
	 * Create and initialize to zeros a new <code>BinaryClassification</code>.
	 */
	public BinaryClassification() {
		outcomes = new EnumMap<Outcome, Integer>(Outcome.class);
		for(Outcome outcome : Outcome.values()) {
			outcomes.put(outcome, 0);
		}
	}
	
	/**
	 * Create a new <code>BinaryClassification</code> with the true positive,
	 * true negative, false positive, and false negative counts initialized as
	 * passed in.
	 * 
	 * @param truePositive
	 * @param trueNegative
	 * @param falsePositive
	 * @param falseNegative
	 */
	public BinaryClassification(int truePositive, int trueNegative, 
								int falsePositive, int falseNegative) {
		this();
		setAllOutcomes(truePositive, trueNegative, falsePositive, falseNegative);
	}
	
	/**
	 * WARNING: Destroys the previous counts of true positives, true negatives,
	 *          fasle positives, and false negatives.  And resets them to the
	 *          passed in values
	 * 
	 * @param truePositive
	 * @param trueNegative
	 * @param falsePositive
	 * @param falseNegative
	 */
	public void setAllOutcomes(int truePositive, int trueNegative, 
							   int falsePositive, int falseNegative) {
		
		outcomes.put(Outcome.TRUE_POSITIVE, truePositive);
		outcomes.put(Outcome.TRUE_NEGATIVE, trueNegative);
		outcomes.put(Outcome.FALSE_POSITIVE, falsePositive);
		outcomes.put(Outcome.FALSE_NEGATIVE, falseNegative);
		
		recomputeMarginals();		
	}

	/**
	 * Set the <code>Outcome</code> of this <code>BinaryClassification</code> 
	 * to be <code>count</code>.  Keeps track of marginals and totals automatically.
	 * 
	 * WARNING: Destroys the previous count of the specified <code>Outcome</code>.
	 * 
	 * @param outcome The outcome to increment by <code>1</code>.
	 */
	public void set(Outcome outcome, int count) {
		outcomes.put(outcome, count);
		recomputeMarginals();
	}

	private void recomputeMarginals() {
		marginalTruthPositive = outcomes.get(Outcome.TRUE_POSITIVE) +
								outcomes.get(Outcome.FALSE_NEGATIVE);
		marginalTruthNegative = outcomes.get(Outcome.TRUE_NEGATIVE) +
								outcomes.get(Outcome.FALSE_POSITIVE);
		marginalDecidedPositive = outcomes.get(Outcome.TRUE_POSITIVE) +
								  outcomes.get(Outcome.FALSE_POSITIVE);
		marginalDecidedNegative = outcomes.get(Outcome.TRUE_NEGATIVE) +
								  outcomes.get(Outcome.FALSE_NEGATIVE);
		
		total = outcomes.get(Outcome.TRUE_POSITIVE) +
				outcomes.get(Outcome.TRUE_NEGATIVE) +
				outcomes.get(Outcome.FALSE_POSITIVE) +
				outcomes.get(Outcome.FALSE_NEGATIVE);
	}
	
	/**
	 * Increment the correct outcome of this <code>BinaryClassification</code> 
	 * by <code>1</code>.  Keeps track of marginals and totals automatically.
	 * @param outcome The outcome to increment by <code>1</code>.
	 */
	public void increment(Outcome outcome) {
		
		Integer outcomeTotal = outcomes.get(outcome);
		outcomeTotal++;
		outcomes.put(outcome, outcomeTotal);
		
		total++;
		
		switch (outcome) {
		case TRUE_POSITIVE:
			marginalTruthPositive++;
			marginalDecidedPositive++;
			break;
		case TRUE_NEGATIVE:
			marginalTruthNegative++;
			marginalDecidedNegative++;
			break;
		case FALSE_POSITIVE:
			marginalTruthNegative++;
			marginalDecidedPositive++;
			break;
		case FALSE_NEGATIVE:
			marginalTruthPositive++;
			marginalDecidedNegative++;
			break;
		}
	}
		
	/**
	 * @return An <code>EnumMap&lt;Outcome, Integer&gt;</code> that holds the totals
	 * 		   for each possible <code>Outcome</code>, namely true positive, true negative,
	 *		   true negative, false positive, and false negative.  
	 */
	public EnumMap<Outcome, Integer> getTotalsByOutcome() {
		return outcomes;
	}
	
	/**
	 * @return the sum of the trials of all <code>Outcomes</code> in this 
	 * 		   <code>BinaryClassification</code>.
	 */
	public int getTotalTrials() {
		return total;
	}
	
	/**
	 * @return The MLE (Maximum Likelihood Estimate) of the probability of each 
	 * 		   <code>Outcome</code> in this <code>BinaryClassification</code>.
	 * 		   The MLE is just the fraction of trials found in each <code>Outcome</code>.
	 */
	public EnumMap<Outcome, Double> getFractionalTotalsByOutcome() {
		EnumMap<Outcome, Double> outcomesFraction 
				= new EnumMap<Outcome, Double>(Outcome.class);
		for (Outcome outcome : Outcome.values()) {
			double outcomeValue = (double)outcomes.get(outcome); 
			outcomesFraction.put(outcome, outcomeValue / total);
		}		
		return outcomesFraction;
	}
	
	/**
	 * Calculates the precision, recall, F1-Measure and Accuracy of the trials in this
	 * <code>BinaryClassification</code>.  
	 * 
	 * <p>Let <code>tp = true positives, tn = true negatives, fp = false negatives,
	 * fn = false negatives.</code>  
	 * 
	 * <p>The formulas used for each of these performance measures are:
	 * 
	 * <p><code>Precision&nbsp; = P = ( tp + fp ) / total</code>
	 * <p><code>Recall&nbsp;&nbsp;&nbsp;&nbsp; = R = ( tp + fn ) / total</code>
	 * <p><code>F1-Measure&nbsp;&nbsp;&nbsp;&nbsp; = ( 2 * P * R ) / ( P + R)</code>
	 * <p><code>Accuracy&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; = ( tp + tn ) / total</code>
	 * 
	 * @return The precision, recall, F1-Measure, and Accuracy of the trials in this
	 * 		   <code>BinaryClassification</code>.
	 */
	public EnumMap<Measure, Double> getMeasures() {
		// First convert all the integer outcomes to doubles
		EnumMap<Outcome, Double> outcomesDouble
				= new EnumMap<Outcome, Double>(Outcome.class);
		for (Outcome outcome : Outcome.values()) {
			outcomesDouble.put(outcome, outcomes.get(outcome).doubleValue());
		}
		
		EnumMap<Measure, Double> measures = new EnumMap<Measure, Double>(Measure.class);
		
		// Be careful of zero denominators
		double precision = (marginalDecidedPositive == 0) 
						   ? 0 : (outcomesDouble.get(Outcome.TRUE_POSITIVE) 
						          / marginalDecidedPositive);
		measures.put(Measure.PRECISION, precision);
		
		double recall = (marginalTruthPositive == 0)
						? 0 : (outcomesDouble.get(Outcome.TRUE_POSITIVE) 
							   / marginalTruthPositive);
		measures.put(Measure.RECALL, recall);
		
		double fmeasure = ((precision == 0) || (recall == 0))
						  ? 0 : (( 2 * measures.get(Measure.PRECISION) 
								     * measures.get(Measure.RECALL))
								          /
								 (  measures.get(Measure.PRECISION) 
								  + measures.get(Measure.RECALL)));
		measures.put(Measure.FMEASURE, fmeasure);
		
		double accuracy = (total == 0) ? 0 : ((  outcomesDouble.get(Outcome.TRUE_POSITIVE) 
				  							   + outcomesDouble.get(Outcome.TRUE_NEGATIVE))
					                          / (double)total);
		measures.put(Measure.ACCURACY, accuracy);
		
		return measures;
	}
	
	/**
	 * Add to this <code>BinaryClassification</code> the 
	 * <code>BinaryClassification</code>(s) passed in.  Adding a 
	 * <code>BinaryClassification</code> B to another <code>BinaryClassification</code>
	 * A means incrementing each <code>Outcome</code> in A by the corrosponding 
	 * <code>Outcome</code> in B as well as incrementing the marginals and total
	 * trials in this <code>BinaryClassification</code>. 
	 * 
	 * @param binaryClassifications The <code>BinaryClassifications</code> to increment
	 * 		  this <code>BinaryClassification</code> by.
	 */
	public void sumInPlace(BinaryClassification... binaryClassifications) {
		for (BinaryClassification binaryClassification : binaryClassifications) {
			for (Outcome outcome : Outcome.values()) {
				int thisOutcomeTotal = outcomes.get(outcome);
				int thatOutcomeTotal = binaryClassification.outcomes.get(outcome);
				outcomes.put(outcome, thisOutcomeTotal + thatOutcomeTotal);
			}
			total += binaryClassification.total;
			marginalTruthPositive   += binaryClassification.marginalTruthPositive;
			marginalTruthNegative   += binaryClassification.marginalTruthNegative;
			marginalDecidedPositive += binaryClassification.marginalDecidedPositive;
			marginalDecidedNegative += binaryClassification.marginalDecidedNegative;
		}
	}
		
	/**
	 * 
	 * @param binaryClassifications The <code>BinaryClassifications</code> to sum.
	 * @return The sum of <code>binaryClassifications</code> defined by:
	 *         <code>new BinaryClassification().sumInPlace(binaryClassifications);</code>
	 * @see <code>sumInPlace(BinaryClassification...)</code>
	 */
	public static BinaryClassification sum(BinaryClassification... binaryClassifications) {
		BinaryClassification toReturn = new BinaryClassification();
		toReturn.sumInPlace(binaryClassifications);
		return toReturn;
	}
	
	/**
	 * @return A pretty printed text String of the 2<code>x</code>2 contingency table that
	 * 		   summarizes this <code>BinaryClassification</code> together with the
	 * 		   accuracy, precision, recall, and F1-measure of this 
	 * 		   <code>BinaryClassification</code>.
	 */
	@Override
	public String toString() {
		// I don't bother caching measures because in real use the toString() or
		// getMeasures() methods will only be called once or twice ever on each 
		// BinaryClassification instance.
		EnumMap<Measure, Double> measures = getMeasures();
		
		String toReturn = "";
		toReturn += getOutcomeTableString();
		toReturn += String.format(" Accuracy:  %5.3f%n",   measures.get(Measure.ACCURACY));
		toReturn += String.format(" Precision: %5.3f%n",   measures.get(Measure.PRECISION));
		toReturn += String.format(" Recall:    %5.3f%n",   measures.get(Measure.RECALL));
		toReturn += String.format(" F1:        %5.3f%n", measures.get(Measure.FMEASURE));
		return toReturn;
	}
	
	/**
	 * @return A pretty printed text String of the 2<code>x</code>2 contingency table that
	 * 		   summarizes this <code>BinaryClassification</code>.
	 */
	public String getOutcomeTableString() {
		String outcomeTable = "";
		outcomeTable += String.format("%n      | Decision    |      %n");
		outcomeTable += String.format("      | +     -     |      %n");
		outcomeTable += String.format(" -----+-------------+------%n");
		outcomeTable += String.format("  T   |             |      %n");
		outcomeTable += String.format("  r + | %-5d %-5d | %-5d%n", outcomes.get(Outcome.TRUE_POSITIVE), outcomes.get(Outcome.FALSE_NEGATIVE), marginalTruthPositive);
		outcomeTable += String.format("  u   |             |      %n");
		outcomeTable += String.format("  t - | %-5d %-5d | %-5d%n", outcomes.get(Outcome.FALSE_POSITIVE), outcomes.get(Outcome.TRUE_NEGATIVE), marginalTruthNegative);
		outcomeTable += String.format("  h   |             |      %n");
		outcomeTable += String.format(" -----+-------------+------%n");
		outcomeTable += String.format("      | %-5d %-5d | %-5d%n%n", marginalDecidedPositive, marginalDecidedNegative, total);
		return outcomeTable;
	}

	/**
	 * Returns a comma separated string with the following format:
	 * <p>truePositive, falseNegative, falsePositive, trueNegative, Total, 
	 * 	  Accuracy, Precision, Recall, F1,  
	 */
	public String getStringForSpreadsheet() {
		// I don't bother caching measures because in real use the toString() or
		// getMeasures() methods will only be called once or twice ever on each 
		// BinaryClassification instance.
		EnumMap<Measure, Double> measures = getMeasures();
		String toReturn = "";
		toReturn += String.format("%d, %d, %d, %d, %d, %.3f, %.3f, %.3f, %.3f",
								  outcomes.get(Outcome.TRUE_POSITIVE),
								  outcomes.get(Outcome.FALSE_NEGATIVE),
								  outcomes.get(Outcome.FALSE_POSITIVE),
								  outcomes.get(Outcome.TRUE_NEGATIVE),
								  total,
								  measures.get(Measure.ACCURACY),
								  measures.get(Measure.PRECISION),
								  measures.get(Measure.RECALL),
								  measures.get(Measure.FMEASURE));
		return toReturn;
	}

	/**
	 * @return A pretty comma delimited String listing the order of fields returned
	 *         by getStringForSpreadsheet().  
	 */
	public static String getColumnTitleStringForSpreadsheet() {
		return "True +, False -, False +, True -, Total, Accuracy, Precision, Recall, F1";
	}
}
