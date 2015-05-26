/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

public class ListOfData<D extends Number & Comparable<? super D>> {
	
	ArrayList<D> list = new ArrayList<D>();
	boolean listIsSorted = true;
	
	public enum SummaryStat {MEAN, STDEV, MIN, Q25, MEDIAN, Q75, MAX};
	
	public ListOfData(D... list) {
		add(list);
		listIsSorted = false;
	}
	
	@SuppressWarnings("unchecked")
	public ListOfData(Set<D> data) {
		this();
		for (D datum : data) {
			add(datum);
		}
	}
	
	public ListOfData(boolean listIsSorted, D... list) {
		this(list);
		this.listIsSorted = listIsSorted;
	}
	
	public void add(D... data) {
		for (D datum : data) {
			list.add(datum);
		}
		listIsSorted = false;
	}
	
	public void sort() {
		if ( ! listIsSorted) {
			Collections.sort(list);
			listIsSorted = true;
		}
	}
	
	public EnumMap<SummaryStat, Double> getSummary() {
		EnumMap<SummaryStat, Double> summary = 
			new EnumMap<SummaryStat, Double>(SummaryStat.class);
		
		sort();
		summary.put(SummaryStat.MEAN, getMean());
		summary.put(SummaryStat.STDEV, getStdev());
		summary.put(SummaryStat.MIN, getMin());
		
		try {
			summary.put(SummaryStat.Q25, getPercentile(0.25));
		} catch (PercentileUnderflowException e) {
			summary.put(SummaryStat.Q25, getMin());
		} catch (PercentileOverflowException e) { // This probably will never be thrown
			e.printStackTrace();
			System.exit(0);
		}
		
		summary.put(SummaryStat.MEDIAN, getMedian());
		
		try {
			summary.put(SummaryStat.Q75, getPercentile(0.75));
		} catch (PercentileOverflowException e) {
			summary.put(SummaryStat.Q75, getMax());
		} catch (PercentileUnderflowException e) { // This probably will never be thrown
			e.printStackTrace();
			System.exit(0);
		}
		
		summary.put(SummaryStat.MAX, getMax());
		
		return summary;
	}
	
	// Returns null if this ListOfData is empty
	private Double getMax() {
		if (list.size() == 0) {
			return null;
		}
		return list.get(list.size()-1).doubleValue();
	}

	// Returns null if this LisfOfData is empty
	private Double getMin() {
		if (list.size() == 0) {
			return null;
		}
		return list.get(0).doubleValue();
	}

	// returns null if this ListOfData is empty 
	private Double getMean() {
		if (list.size() == 0) {
			return null;
		}
		double sumOfData = 0;
		for (D datum : list) {
			sumOfData += datum.doubleValue();
		}
		double n = (double) list.size();
		double mean = sumOfData / n;
		
		return mean;
	}
	
	public Double getStdev() {
		if (list.size() == 0) {
			return null;
		}
		Double mean = getMean();
		double sumOfSquaresOfDeviationsFromMean = 0;
		for (D datum : list) {
			double deviationFromMean = datum.doubleValue() - mean;
			double squareOfDeviationFromMean = deviationFromMean * deviationFromMean;
			sumOfSquaresOfDeviationsFromMean += squareOfDeviationFromMean;
		}
		double n = (double) list.size();
		double meanOfDeviationsFromMean = sumOfSquaresOfDeviationsFromMean / n;
		double stdDev = Math.sqrt(meanOfDeviationsFromMean);
		
		return stdDev;
	}

	private static class PercentileUnderflowException extends Exception {
		private static final long serialVersionUID = 1L;
		PercentileUnderflowException(String message) { super(message); }
	}
	private static class PercentileOverflowException  extends Exception {
		private static final long serialVersionUID = 1L;
		PercentileOverflowException(String message) { super(message); }
	}
	/**
	 * Return the (100p)th percentile this ListOfData.  I use the official definition
	 * of percentile in "Statistical Inference" by Casella and Berger p226-227.  
	 * Specifically, "The notation {b} ... is defined to be the number b rounded to
	 * the nearest integer....  The (100p)th sample percentile is X_({np}) if
	 * (1 / 2n) < p < 0.5 and X_(n+1-{n(1-p)}) if 0.6 < p < (1 - (1 / 2n))....  There is a
	 * restriction on the range of p because the size of the sample limites the range
	 * of sample percentiles."  I 
	 * 
	 * @param p Must be between 0 and 1 inclusive.
	 * @return <code>null</code> if this ListOfData is empth
	 * @throws PercentileUnderflowException 
	 * @throws PercentileOverflowException 
	 */
	public Double getPercentile(double p) throws PercentileUnderflowException, 
												 PercentileOverflowException {
		if (list.size() == 0) {
			return null;
		}
		
		if ((p < 0) || (p > 1)) {
			throw new IllegalArgumentException(
					"A requested quartile must be between 0 and 1, but " 
					+ p + " was passed");
		}
		
		int n = list.size();
		
		// Percentiles are messy beasts.  And to define them so that "the sample
		// percentiles exhibit the following symmetry[:] If the (100p)th sample percentile 
		// is the ith smallest observation, then the (100(1-p))th sample percentile should 
		// be the ith largest observation" (Cassella and Berger, p 227), you do indeed
		// have the problem of too small or too large p values depending on the number
		// of samples/data in this ListOfData.

		
		double lowerLimit = 1 / (2 * (double)n);
		double upperLimit = 1 - lowerLimit;
		if (p <= lowerLimit) {
			throw new PercentileUnderflowException("p must be greater than " + lowerLimit
											   	   + ", but was in fact: " + p);
		}
		if (p >= upperLimit) {
			throw new PercentileOverflowException("p must be less than " + upperLimit
												  + ", but was in fact: " + p);
		}
		
		if (p == 0.5) {
			return getMedian();
		}
		
		
		// Begin the real work of finding the (100p)th percentile
		
		sort();
		
		int order;
		D percentile;
		
		if (p < 0.5) {
			order = (int) Math.round(n * p);
			order -= 1;  // adjust to index into an array in java
			percentile = list.get(order);
		} else { // p > 0.5
			order = (int) ((n + 1) - Math.round(n * (1 - p)));
			order -= 1;  // adjust to index into an array in java.  (kinda silly since
						 // the previous line added 1, but to make the math clear I
					     // do it this way anyway.)
			percentile = list.get(order);
		}
		
		double percentileAsDouble = percentile.doubleValue();
		return percentileAsDouble;
	}

	// Returns null if this ListOfData is empty
	public Double getMedian() {
		if (list.size() == 0) {
			return null;
		}

		sort();
		
		int n = list.size();
		
		boolean nIsOdd = false;
		if ((n % 2) == 1) {
			nIsOdd = true;
		}
		
		double median;
		
		if (nIsOdd) {  // just get the middle value
			int order;
			order = (n + 1) / 2;
			order--;  // adjust to index into an array in java
			D medianAsD = list.get(order);
			median = medianAsD.doubleValue();
			
		} else { // n is even, the median is the average of the 2 middle values
			int orderNOver2 = n / 2;
			orderNOver2--;  // adjust to index into an array in java
			int orderNOver2Plus1 = orderNOver2 + 1;
			D valueAtNOver2 = list.get(orderNOver2);
			D valueAtNOver2Plus1 = list.get(orderNOver2Plus1);
			median = ( valueAtNOver2.doubleValue() + valueAtNOver2Plus1.doubleValue() ) / 2;
		}
		
		return median;
	}
	
	/**
	 * From Manning and Sch�tze p61-63:
	 * 
	 * "Let p(x) be the probability mass function of a random variable X, over a discrete
	 * set of symbols (or alphabet X:
	 * 
	 * p(x) = P(X=x), x in X
	 * 
	 * ...
	 * 
	 * The entropy (or self-information) is the average uncertainty of a single random variable:
	 * 
	 * Entropy H(p) = H(X) = - SUM_x_in_X [ p(x) * log_2 p(x) ]
	 * 
	 * Entropy measures the amount of information in a random variable.  It is normally measured
	 * in bits (hence the log to the base 2), but using any other base yields only a linear
	 * scaling of results....  Also, for this definition to make sense, we define 
	 * 0 * log_2 0 = 0....
	 * 
	 * The minus sing at the start of the formula for entropy can be moved inside the
	 * logarithm, where it becomes a reciprocal:
	 * 
	 * H(X) = SUM_x_in_X [ p(x) * log_2 (1/p(x)) ]
	 * 
	 * People without any statistics background often think about a formula like this as a
	 * sum of the quantity p(x) log_2(1/p(x)) for each x.  While this is mathematically 
	 * impeccable, it is the wrong way to think about such equations.  Rather you should 
	 * think of SUM_x_in_X [ p(x)... ] as an idiom.  It says to take a weighted average of
	 * the rest of the formula (which will be a function of x), where the weighting depends
	 * on the probability of each x.  Technically, this idiom defines an expectation....
	 * Indeed,
	 * 
	 * H(X) = E [ log_2 (1/p(x)) ]
	 * 
	 */
	public double getEntropy() {
		double sum = 0;
		
		for (D listEntry : list) {
			double listEntryAsDouble = listEntry.doubleValue();
			double log2OfListEntry = Math.log(listEntryAsDouble) / Math.log(2.0);
			double term = listEntryAsDouble * log2OfListEntry;
			sum += term;
		}
		
		double entropy = -1 * sum;
		
		return entropy;
	}
	
	// TODO: cache the summary and only recompute it when necessary
	public String getPrettySummary() {
		EnumMap<SummaryStat, Double> summary = getSummary();
		
		String toReturn = "";
		toReturn += String.format(" ( Mean:   " + summary.get(SummaryStat.MEAN)   + "%n");
		toReturn += String.format("   Min:    " + summary.get(SummaryStat.MIN)    + "%n");
		toReturn += String.format("   Q25:    " + summary.get(SummaryStat.Q25)    + "%n");
		toReturn += String.format("   Median: " + summary.get(SummaryStat.MEDIAN) + "%n");
		toReturn += String.format("   Q75:    " + summary.get(SummaryStat.Q75)    + "%n");
		toReturn += String.format("   Max:    " + summary.get(SummaryStat.MAX)    + " )%n");
		
		return toReturn;
	}

}
