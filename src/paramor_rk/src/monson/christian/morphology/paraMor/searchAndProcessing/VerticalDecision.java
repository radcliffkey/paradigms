/*******************************************************************************
 * Copyright (c) 2010 -- Christian Monson.
 ******************************************************************************/
package monson.christian.morphology.paraMor.searchAndProcessing;

import java.util.Comparator;
import java.util.EnumMap;

import monson.christian.morphology.paraMor.networks.VirtualPartialOrderNetwork;
import monson.christian.morphology.paraMor.schemes.Scheme;

//
// All of a class of vertical decision metrics that I am exploring in the
// BottomUp_Oracle_Instrumentation_Search class are based on some or all of the cells of a 2x2 grid
// that contains the counts of the numbers of stems that occur in:
//
// 1) the current scheme,
// 2) the complement scheme--that scheme that contains all the 
//    suffixes that do not occur in the current scheme but that 
//    do occur in the parent scheme, 
// 3) the parent scheme--the scheme that we are looking at moving 
//    to from the current scheme.  The parent scheme can also be viewed as the
//    conjunction of the current scheme and the complement scheme.  AND
// 4) all schemes that are not the current scheme, the complement scheme, or
//    the parent scheme.  OR equivalently (not current AND not complement).
// This 2x2 table can be augmented with marginal and total counts. 
// Cell 4) is indirectly estimated by first estimating the TRUE total number of 
// stems that occured in the corpus that the network was built from simply by the 
// number of types in the corpus that the network was built from.
//
// This class is intended for use by NetworkSearchProcedure and its descendents--
// so it is default (package) accessible as are all its members.
//
// NOTE: If VerticalDecisionGrid is ever made NOT immutable then the "observed" 
//       field needs to be reset.
public class VerticalDecision {
		
	protected VirtualPartialOrderNetwork partialOrderNetwork = null;
	protected Scheme current = null;
	protected Scheme parent = null;
	
	public enum GridValues {
		CURRENT_COMPLEMENT,     NOT_CURRENT_BUT_COMPLEMENT,     COMPLEMENT,
		CURRENT_NOT_COMPLEMENT, NOT_CURRENT_NOT_COMPLEMENT, NOT_COMPLEMENT,
		CURRENT,                NOT_CURRENT,                TOTAL
	};

	protected EnumMap<GridValues, Integer> grid = null;

	
	// A few factorial values:                   0, 1, 2, 3,  4,   5,   6,    7,     8       9       10
	private final int[] factorials = 
		new int[] { 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800 }; 
	
	// Cached value of the metric if it has been calculated
	// NOTE: If VerticalDecisionGrid is ever made NOT immutable then the "observed" field
	//       needs to be reset.
	protected EnumMap<VerticalMetric, Double> observed = 
					new EnumMap<VerticalMetric, Double>(VerticalMetric.class);

	
	/**
	 * Prerequisite parent must be a parent of current in sparsePartialOrderNetwork.
	 * 
	 * @param partialOrderNetwork
	 * @param current
	 * @param parent
	 */
	public VerticalDecision(VirtualPartialOrderNetwork partialOrderNetwork, 
							Scheme current, 
							Scheme parent) {
		
		this.partialOrderNetwork = partialOrderNetwork;
		this.current = current;
		this.parent  = parent;
	}
	
	// When calling this function think of the following:
	//
	// 2x2 table that the vertical metrics are based on.
	//
	// |------------------------|----------------------------|------------------------
	// |  current & complement  |   ~current & complement    |      complement
	// |------------------------|----------------------------|------------------------
	// | current & ~complement  |   ~current & ~complement   |     ~complement
	// |------------------------|----------------------------|------------------------
	// |      current           |        ~current            |    totalTrueStems
	//
	//
	// 2x2 table that the vertical metrics are based on using GridValues names.
	//
	// |------------------------|----------------------------|------------------------
	// |   CURRENT_COMPLEMENT   | NOT_CURRENT_BUT_COMPLEMENT |      COMPLEMENT
	// |------------------------|----------------------------|------------------------
	// | CURRENT_NOT_COMPLEMENT | NOT_CURRENT_NOT_COMPLEMENT |     NOT_COMPLEMENT
	// |------------------------|----------------------------|------------------------
	// |      CURRENT           |         NOT_CURRENT        |        TOTAL
	//
	//
	// Function Parameters as they map to the 2x2 table
	//
	// -------------------------------------------------------------------------------
	// |       parentSize       |                            |     complementSize     
	// |------------------------|----------------------------|------------------------
	// |                        |                            |
	// |------------------------|----------------------------|------------------------
	// |       currentSize      |                            | totalTrueStemsEstimate
	//
	//
	// The argument names all end with 'Estimate' to emphasize that this function does
	// NOT care if some other estimate of the parentSize (etc.) beside the
	// simple parent.size() were passed in.  This function will build a 2x2 table with
	// any valid arguments.
	//
	// Throws an IllegalArgumentException if while calculating the 2x2 table we find that
	// the interior cells parentSize + (currentSize-parentSize) + (complementSize-currentSize)
	// is greater than the totalTrueStemsEstimate.
	VerticalDecision(Integer parentSizeEstimate, 
					 Integer currentSizeEstimate,
					 Integer complementSizeEstimate,
					 Integer totalTrueStemsEstimate) {
		
		initializeGrid(parentSizeEstimate, 
					   currentSizeEstimate, 
					   complementSizeEstimate, 
					   totalTrueStemsEstimate);
	}
	
	// A quicker constructor if you already know all the grid values.
	public VerticalDecision(EnumMap<GridValues, Integer> grid) {
		this.grid = grid;
	}
	

	// Assumes 
	// 1) this.current, this.parent, and this.sparsePartialOrderNetwork 
	//    have been initialized.
	//    OR
	// 2) grid has been initialized
	//
	// All public functions that use this.grid, the 2x2 table
	// of stem counts in current, parent, and complement, should call initializeGrid()
	// before the do anything else, because there is no way to know if the grid has
	// been initialized yet or not.
	protected void initializeGrid() {
		
		// return if the grid has already been initialized.
		if (grid != null) {
			return;
		}
		
		int parentSizeEstimate  =  parent.adherentSize();
		int currentSizeEstimate = current.adherentSize();
		int complementSizeEstimate = 
			partialOrderNetwork.getSizeOfComplementaryNode(current, parent);

		// instrumentation needs an estimate on the number of true stems in the corpus
		// so here we estimate the number of true stems by the number of words in
		// the vocabulary.
		int totalTrueStemsEstimate = 
			partialOrderNetwork.getIdentifier().getCorpus().getVocabularySize();

		// Next an important (long) comment:
		//
		// It so happens that the number of words in the vocabulary is exactly the same
		// number as the number of stems in the *null-suffix* scheme--by definition.
		// Hence, if current (or complement) turns out to be the *null-suffix* scheme,
		// then when we fill in the 2x2 grid, if parentSize is > complementSize
		// (or parentSize is > current size) then the sum of the three interior
		// cells parentSize + (currentSize-parentSize) + (complementSize-currentSize)
		// will be greater than totalTrueStemsEstimate--An illegal state which if allowed
		// to proceed would give a negative value for the fourth interior cell.
		//
		// Since it is very likely that the number of stems in the *null-suffix* scheme
		// is a severe over-estimate of the number of true stems that can take a 
		// *null-suffix*, one reasonable way to address this problem would seem to be to
		// reduce the estimate of the size of the *null-suffix* scheme.  Notice that by 
		// moving up the network we are proposing to merge the *null-suffix* scheme with 
		// some other scheme C = ({suf1, suf2, ..., sufN}, {stem1, stem2, ..., stemM}) 
		// into a single sub-class, in other words we are proposing that both 
		// *null-suffix* and each of suf1, suf2, ..., sufN can attach to the same set of 
		// stems.  But the *null-suffix* scheme has as stem adherents each of the strings 
		// stem_i.suf_j, formed by concatenating one of the suffixes from C onto one of 
		// the stems from C.  Now, (unless there is a fairly unlikely scenario as outlined 
		// below) to make it more plausible that the *null-suffix* scheme and C could be 
		// part of the same sub-class it makes sense to remove all the adherent stems of 
		// form stem_i.suf_j from the *null-suffix* scheme.  Or equivalently to re-estimate
		// the number of stems in the *null-suffix* scheme by subtracting
		// (|stems in C| * |suffixes in C|) from the original estimate.
		//
		// It is possible that removing some of the stem_i.suf_j stems from the 
		// *null-scheme* is the wrong thing to do.  Two such occasions would be:
		// 1) there is some other stem in the language that has surface form stem_i.suf_j 
		//     but was formed in some way that did not involve a suffix from C
		// 2) the sub-class formed by combining the *null-scheme* with scheme C is 
		//    repeatable on the same word.
		// But both of these scenarios are rare and completely overwhelmed by the near 
		// certanty that the original estimate of the number of stems in the *null-suffix*
		// scheme is too high.
		//
		// Another important point is that in general you do not want to go around removing
		// stems from one scheme C' just because C' contains a stem that can be formed by
		// concatenating a stem and suffix from C, the scheme you are considering merging 
		// with C'.  Consider the Spanish verbal suffix ar and the clitic se.  These two 
		// suffixes belong to separate sub-classes, and in particular the se clitic 
		// sub-class attaches after suffixes from the ar verbal sub-class attach to stems.  
		// Hence, when considering merging the ar scheme with the se scheme, many stems in 
		// the se scheme can be formed by concatenating ar to a stem in the ar scheme.  But 
		// if we removed these suffixes from the se scheme we would increase the scores 
		// that suggest that se and ar are part of the same sub-class.  Put another way, 
		// removing stems fromt he *null-suffix* scheme makes sense precisely because we 
		// want to increase the scores suggesting *null-suffix* forms sub-classes with 
		// other suffixes, because we believe we have over-estimated the number of stems 
		// in the *null-suffix* scheme.
		
		
		
		if (currentSizeEstimate == totalTrueStemsEstimate) {
			// parentLevel - currentLevel.  Which is NOT necssarily the same
			// thing as the level of the lowest scheme in the SparsePartialOrderNetwork
			// that contains all the suffixes that are in parent but that are not in current,
			// but it is what we want to use when adjusting the current size estimate.
			
			int complementLevel_absolute = parent.level() - current.level();
			
			int adjustCurrentSizeEstimateBy = complementLevel_absolute * complementSizeEstimate;
			currentSizeEstimate -= adjustCurrentSizeEstimateBy;
			
			/*
			THIS IS BROKEN, FIX IT
			
			currentSizeEstimate = 8754;
			*/
		}
		// Since we don't actually know the name of the complementScheme (and in general the
		// complementScheme may not directly exist in the SparsePartialOrderNetwork) we
		// have to back off to looking at just the original extimated size of the complement
		// scheme.
		if (complementSizeEstimate == totalTrueStemsEstimate) {
			
			int adjustComplementSizeEstimateBy = current.level() * currentSizeEstimate;
			complementSizeEstimate -= adjustComplementSizeEstimateBy;
			/*
			complementSizeEstimate = 8754;
			*/
		}
		
		initializeGrid(parentSizeEstimate, 
					   currentSizeEstimate, 
					   complementSizeEstimate, 
					   totalTrueStemsEstimate);
	}
	
	// Corretly initialize Grid as:
	//
	// -------------------------------------------------------------------------------
	// |       parentSize       |                            |     complementSize     
	// |------------------------|----------------------------|------------------------
	// |                        |                            |
	// |------------------------|----------------------------|------------------------
	// |       currentSize      |                            | totalTrueStemsEstimate
	//
	//
	// The argument names all end with 'Estimate' to emphasize that this function does
	// NOT care if some other estimate of the parentSize (etc.) beside the
	// simple parent.size() were passed in.  This function will build a 2x2 table with
	// any valid arguments.
	//
	// Throws an IllegalArgumentException if while calculating the 2x2 table we find that
	// the interior cells parentSize + (currentSize-parentSize) + (complementSize-currentSize)
	// is greater than the totalTrueStemsEstimate.
	private void initializeGrid(int parentSizeEstimate, 
								int currentSizeEstimate, 
								int complementSizeEstimate, 
								int totalTrueStemsEstimate) {
		
		grid = new EnumMap<GridValues, Integer>(GridValues.class);
		
		// Fill in the passed in grid values
		grid.put(GridValues.CURRENT_COMPLEMENT, parentSizeEstimate);
		grid.put(GridValues.CURRENT, currentSizeEstimate);
		grid.put(GridValues.COMPLEMENT, complementSizeEstimate);
		grid.put(GridValues.TOTAL, totalTrueStemsEstimate);
		
		
		// Fill in the 3 missing 2x2 grid values
		Integer notCurrentButComplement = complementSizeEstimate - parentSizeEstimate;
		grid.put(GridValues.NOT_CURRENT_BUT_COMPLEMENT, notCurrentButComplement);
		
		Integer currentNotComplement = currentSizeEstimate - parentSizeEstimate;
		grid.put(GridValues.CURRENT_NOT_COMPLEMENT, currentNotComplement);
		
		Integer interiorSum = parentSizeEstimate + notCurrentButComplement + currentNotComplement;
		
		if (interiorSum > totalTrueStemsEstimate) {
			String msg = "A parentSizeEstimate of: " + parentSizeEstimate +
						 ", a currentSizeEstimate of: " + currentSizeEstimate +
						 ", and a complementSizeEstimate of: " + complementSizeEstimate +
						 " yield an interior table sum of: " + interiorSum +
						 ".  Which is larger than the totalTrueStemEstimate of: " + 
						 totalTrueStemsEstimate;
			throw new IllegalArgumentException(msg);
		}
		
		Integer notCurrentNotComplement = totalTrueStemsEstimate - interiorSum;
		grid.put(GridValues.NOT_CURRENT_NOT_COMPLEMENT, notCurrentNotComplement);
		
		// Fill in the 2 missing marginals
		Integer notComplement = totalTrueStemsEstimate - complementSizeEstimate;
		grid.put(GridValues.NOT_COMPLEMENT, notComplement);
		
		Integer notCurrent = totalTrueStemsEstimate - currentSizeEstimate;
		grid.put(GridValues.NOT_CURRENT, notCurrent);
	}
	
	// initializeGrid (if it has not already been initialized) and return it.
	public EnumMap<GridValues, Integer> getGrid() {
		initializeGrid();
		return grid;
	}
	
	/**
	 * If 
	 * 
	 * @param cutoff the value to check if 
	 * @return
	 */
	public boolean passes(Double cutoff, VerticalMetric verticalMetric) {
		if (verticalMetric.isGridUser()) {
			initializeGrid();
			if (verticalMetric.isCorrectForLessLikelyThanChance()) {
				if (coocurrenceIsLessLikelyThanChance()) {
					return false;
				}	
			}
			if (verticalMetric.requiresNoZerosInGrid()) {
				if (gridHasZeros()) {
					return false;
				}
			}
			if (verticalMetric.parentMustHaveMoreThanOneStem()) {
				if (parentHasOnlyOneStem()) {
					return false;
				}
			}
		} else {
			if (verticalMetric.isCorrectForLessLikelyThanChance()) {
				throw new VerticalMetricException("The VerticalMetric: " + verticalMetric +
												  " has isGridUser: false but " +
												  "isCorrectForLessLikelyThanChance: true." +
												  "  An invalid setting.");
			}
		}
		
		switch (verticalMetric) {
		case BASELINE_ALWAYS_MOVE_UP:
			return true;
		case BASELINE_NEVER_MOVE_UP:
			return false;
		
		// Levels skipped is opposite from most of the other
		// metrics.  The metric passes if the calculated value
		// is LESS THAN OR EQUAL TO the cutoff.
		case LEVELS_SKIPPED:
			if (calculate(verticalMetric) <= cutoff) {
				return true;
			}
			return false;
			
		// Unlike the other Metrics, LINEAR_SLIDING_RATIO actually adjusts
		// the cutoff that is used depending on the level of the current
		// node in the network.  Specifically, the cutoff that is passed 
		// to passes() is interpreted here as the cutoff when the current
		// node is at the highest level in the network that has a scheme 
		// with at least 2 stems (since you can never search from a network
		// node with only 1 stem anyway).  The adjusted cutoff is a linear
		// function with intercept 0 at level 0, and equal to
		// the passed in cutoff at that maximum 2-stem level.
		case LINEAR_SLIDING_RATIO:
			
			System.err.println();
			System.err.println("Calculating Linear-Sliding-Ratio has not yet been");
			System.err.println("  implemented in this version of ParaMor!!!!!!!!!");
			System.err.println();
			System.err.println();
			
			/*
			double maxCutoff = cutoff;
			Integer maxLevelInNetworkWith2Stems = 
				partialOrderNetwork.getMaxLevelWithNStems(2);
			double slope = maxCutoff / (double) maxLevelInNetworkWith2Stems;
			cutoff = current.level() * slope;
			*/
			break;
		}
		
		if (calculate(verticalMetric) > cutoff) {
			return true;
		}
		return false;
	}
	
	private boolean parentHasOnlyOneStem() {
		if ( grid.get(GridValues.CURRENT_COMPLEMENT) <= 1 ) {
			return true;
		}
		return false;
	}

	public boolean gridHasZeros() {
		initializeGrid();
		if ((grid.get(GridValues.CURRENT_COMPLEMENT) == 0) ||
			(grid.get(GridValues.CURRENT_NOT_COMPLEMENT) == 0) ||
			(grid.get(GridValues.NOT_CURRENT_BUT_COMPLEMENT) == 0) ||
			(grid.get(GridValues.NOT_CURRENT_NOT_COMPLEMENT) == 0)) {
			return true;
		}
		return false;
	}

	public boolean coocurrenceIsLessLikelyThanChance() {
		initializeGrid();
		
		// no cast necessary from int to double
		double currentAndComplement = grid.get(GridValues.CURRENT_COMPLEMENT);
		double current      		= grid.get(GridValues.CURRENT); 
		double complement   		= grid.get(GridValues.COMPLEMENT);  
		double total                = grid.get(GridValues.TOTAL);
		
		double expected = (current * complement) / total;
		
		if (currentAndComplement < expected) {
			return true;
		}
		return false;
	}
	
	public Double calculate(VerticalMetric verticalMetric) {
		if (verticalMetric.isGridUser()) {
			initializeGrid();
		}
		
		// just return cached value if available.
		if (observed.get(verticalMetric) == null) {
			
			Double thisObserved = null;
			
			switch (verticalMetric) {
				
			case BASELINE_ALWAYS_MOVE_UP:
				thisObserved = null;
				break;
				
			case BASELINE_NEVER_MOVE_UP:
				thisObserved = null;
				break;
				
			case LEVELS_SKIPPED:
				thisObserved = levelsSkipped();
				break;
				
			case AVERAGE_LENGTH_OF_PARENT_STEMS:
				thisObserved = averageLengthOfParentStems();
				break;
				
			case MINIMUM_LENGTH_OF_PARENT_STEMS:
				thisObserved = minimumLengthOfParentStems();
				break;
			
			case RATIO:
				thisObserved = ratio();
				break;
				
			case LINEAR_SLIDING_RATIO:
				// The difference in the sliding ratio is what ratio passes a cutoff, 
				// not in the actual value of the ratio.  (This is a different solution
				// than what I did for ORACLE_SLIDING_RATIO_MEAN and 
				// ORACLE_SLIDING_RATIO_MEDAIN.
				thisObserved = ratio();
				break;
				
			// All of these next three VerticalMetrics just call ratio()
			// The difference between them is what specific criteria they
			// must posses to return true in passes().
			case RATIO_NO_ZEROS_IN_TABLE:
			case RATIO_PARENT_LARGER_THAN_1:
			case RATIO_ONE_SIDED:
				thisObserved = ratio();
				break;
				
			case DICE:
				thisObserved = dice();
				break;

			case SCALED_DICE_EXPONENTIAL:
				thisObserved = scaledDiceExponential();
				break;
				
			case SCALED_DICE_FACTORIAL:
				thisObserved = scaledDiceFactorial();
				break;
				
			// All of these next three VerticalMetrics just call scaledDiceFactorial()
			// The difference between them is what specific criteria they
			// must posses to return true in passes().
			case SCALED_DICE_FACTORIAL_NO_ZEROS_IN_TABLE:
			case SCALED_DICE_FACTORIAL_PARENT_LARGER_THAN_1:
			case SCALED_DICE_FACTORIAL_ONE_SIDED:
				thisObserved = scaledDiceFactorial();
				break;

			case LARGE_SAMPLE_BERNOULLI_TEST:
				thisObserved = largeSampleBernoulliTest();
				break;
				
			case PEARSONS_CHI_SQUARED_TEST:
			case PEARSONS_CHI_SQUARED_TEST_ONE_SIDED:
				thisObserved = pearsonsChiSquaredTest();
				break;
				
			case CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI:
			case CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED:
			case CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED_NO_ZEROS_IN_TABLE:
				thisObserved = constrainedLikelihoodRatioOfBernoulli();
				break;
				
			case CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_CONDITION_ON_COMPLEMENT:
			case CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_CONDITION_ON_COMPLEMENT_ONE_SIDED:
				thisObserved = constrainedLikelihoodRatioOfBernoulli_conditionOnComplement();
				break;
				
			case POINTWISE_MUTUAL_INFORMATION:
				thisObserved = pointwiseMutualInformation();
				break;
				
			// All of these next three VerticalMetrics just call pointwiseMutualInformation()
			// The difference between them is what specific criteria they
			// must posses to return true in passes().
			case POINTWISE_MUTUAL_INFORMATION_NO_ZEROS_IN_TABLE:
			case POINTWISE_MUTUAL_INFORMATION_PARENT_LARGER_THAN_1:
			case POINTWISE_MUTUAL_INFORMATION_ONE_SIDED:
				thisObserved = pointwiseMutualInformation();
				break;
			
			default:
				throw new VerticalMetricException("The VerticalMetric " + 
						  									  verticalMetric + " is not " +
						  									  "compatible with " +
						  									  "VerticalDecision");
			}
			
			observed.put(verticalMetric, thisObserved);
		}
		
		return observed.get(verticalMetric);
	}

	private Double levelsSkipped() {
		// Minus one because no levels are skipped if, for example, 
		// you move from level 3 to level 4.
		return (double) ((parent.level() - current.level()) - 1);
	}
	
	private Double averageLengthOfParentStems() {
		double averageLengthOfParentStems = parent.getAverageLengthOfStems();
		return averageLengthOfParentStems;
	}

	private Double minimumLengthOfParentStems() {
		int minimumLengthOfParentStems = parent.getMinimumLengthOfStems();
		return ((double) minimumLengthOfParentStems);
	}
	
	protected double ratio() {
		// no cast necessary from int to double
		double parentSize  = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentSize = grid.get(GridValues.CURRENT); 
		double ratio = parentSize / currentSize;
		return ratio;
	}
	
	// Dice is the harmonic mean of the ratios:
	// 1) parentSize / currentSize
	// 2) parentSize / complementSize
	//
	// but it is implemented as the computationally somewhat simpler formula below
	private double dice() {
		// no cast necessary from int to double
		double parentSize     = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentSize    = grid.get(GridValues.CURRENT); 
		double complementSize = grid.get(GridValues.COMPLEMENT);  
		double dice = dice(parentSize, currentSize, complementSize);
		
		return dice;
	}
	
	private double scaledDiceExponential() {
		// no cast necessary from int to double
		double parentSize     = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentSize    = grid.get(GridValues.CURRENT); 
		double complementSize = grid.get(GridValues.COMPLEMENT);  

		// parentLevel - currentLevel.  Which is NOT necssarily the same
		// thing as the level of the lowest scheme in the SparsePartialOrderNetwork
		// that contains all the suffixes that are in parent but that are not in current
		int complementLevel_absolute = parent.level() - current.level();
		
		// It is possible for levelDifference to be negative!
		// But I'm not going to worry about that here.  I'll just let
		// the math do what it does.  I believe what will happen if 
		// levelDifference is negative is that where a positive 
		// levelDifference effectively increases the dice score (by 
		// increasing the parent:complement ratio) a negative 
		// levelDifference effectively decreases the dice score by 
		// decreasing the parent:complement ratio.  A second alternative 
		// would be to scale currentSize down when levelDifference is 
		// negative.  But I like the first option better because the first 
		// option is making complement look more like current, whereas the 
		// second option is making current look more like complement.  Since 
		// you suspect that current is a likely good scheme whereas you 
		// don't know anything about complement, it make more sense to keep 
		// current as is and adjust other thigs (such as complement).
		int levelDifference = current.level() - complementLevel_absolute;
		
		double scaleFactor = Math.pow(2, levelDifference);
		scaleFactor = 1 / scaleFactor;
		complementSize *= scaleFactor;
		
		double scaledDice = dice(parentSize, currentSize, complementSize);
		
		return scaledDice;

	}
	
	private double scaledDiceFactorial() {
		// no cast necessary from int to double
		double parentSize     = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentSize    = grid.get(GridValues.CURRENT); 
		double complementSize = grid.get(GridValues.COMPLEMENT);  
		
		// parentLevel - currentLevel.  Which is NOT necssarily the same
		// thing as the level of the lowest scheme in the SparsePartialOrderNetwork
		// that contains all the suffixes that are in parent but that are not in current
		int complementLevel_absolute = parent.level() - current.level();
		
		// It is possible for levelDifference to be negative!
		// and so to bring the size of complement onto a comparable scale to
		// the size of current, sometimes we make complementSize smaller
		// and sometimes we make it larger.  (unlike when scaling by an 
		// exponential, the math doesn't magically do this for us.)
		int levelDifference = current.level() - complementLevel_absolute;
		
		// if no scaling is needed then just multiply by 1
		double scaleFactor = 1.0;
		int adjustedLevelDifference;
		if (levelDifference > 0) {
			// +1 onto levelDifference because 1! is just 1
			// but really we want to adjust complement size
			// if there is any level diff.
			adjustedLevelDifference = levelDifference + 1;
			if (adjustedLevelDifference >= factorials.length) {
				adjustedLevelDifference = factorials.length-1;
			}
			scaleFactor = factorials[adjustedLevelDifference];  
			scaleFactor = 1 / scaleFactor;
		} else if (levelDifference < 0) {
			adjustedLevelDifference = -1 * levelDifference;
			// +1 onto levelDifference because 1! is just 1
			// but really we want to adjust complement size
			// if there is any level diff.
			adjustedLevelDifference = adjustedLevelDifference + 1;
			if (adjustedLevelDifference >= factorials.length) {
				adjustedLevelDifference = factorials.length-1;
			}			
			scaleFactor = factorials[adjustedLevelDifference];
		}
		complementSize *= scaleFactor;
		
		
		double scaledDice = dice(parentSize, currentSize, complementSize);
		
		return scaledDice;

	}

	
	protected double dice(double parentSize, double currentSize, double complementSize) {
		double dice = ( 2 * parentSize ) / ( currentSize + complementSize );

		return dice;
	}
	
	// Bassed on Casella and Berger "Statistical Inference" Example 10.3.5 (Large-sample
	// binomial tests) p 493.
	//
	// Let X1,...Xn be a random sample from a Bernoulli(p) population [(or equivalently
	// a single sample from a Binomial(n,p)].  Consider testing
	// H0: p <= p0
	// H1: p >  p0
	// where 0 < p0 < 1 is a specified value.  The MLE of p, based on a sample of size
	// n is p^ = SUM{i=1 to n}Xi/n.  Since p^ is just a sample mean, the Central Limit
	// Theorem applies and states that for any p, 0<p<1, (p^-p)/sigma_n converges to a
	// standard normal random variable.  Here sigma_n = sqrt(p(1-p)/n), a value that 
	// depends on the unknown parameter p.  A reasonable estimate of sigma_n is
	// Sn=sqrt(p^(1-p^)/n), and it can be shown (see Exercise 5.32) that sigma_n/Sn
	// converges in probability to 1.  Thus, for any p, 0<p<1,
	//
	//       p^ - p           d
	// --------------------  --->  N(0,1)
	//  sqrt(p^(1-p^) / n)  
	// 
	// The Wald test statistic Zn is defined by replacing p by p0, and the large-sample
	// Wald test rejects H0 if Zn > z_alpha.  As an alternative estimate of sigma_n, it
	// is easily checked that 1/I_n(p^) = p^(1-p^)/n.  So, the same statistic Zn obtains
	// if we use the information number to derive a standard error for p^.
	//
	// All right.  With the generic test in mind, this test is applicable because if we
	// think of each stem as a bernoulli trial of the variable "current & complement"
	// we can test if "current & complement" occur more often than chance, where chance
	// is defined as assuming current and complement are independent so that p0 is
	// P(current)*P(complement).
	//
	private double largeSampleBernoulliTest() {
		// no cast necessary from int to double
		double parentSize = grid.get(GridValues.CURRENT_COMPLEMENT);
		double current    = grid.get(GridValues.CURRENT); 
		double complement = grid.get(GridValues.COMPLEMENT);  
		double total      = grid.get(GridValues.TOTAL);
		
		double p_hat     = parentSize / total;
		double p_naught  = (current / total) * (complement / total);
		double numerator = p_hat - p_naught;
		
		double variance      = p_hat * (1 - p_hat);
		double standardError = Math.sqrt(variance);
		double sqrtTotal     = Math.sqrt(total);
		double denominator   = standardError / sqrtTotal;
		
		double largeSampleBinomialTestStatistic = numerator / denominator;
		return largeSampleBinomialTestStatistic;
	}
	
	
	// The following documentation is rather long and involved but it is thurough.
	//
	// (from deGroot "Probability and Statistics" section 9.1-9.3)
	// "Suppose that a large population consists of items of k different types,
	// and let p_i denote the probability that an item selected at random will 
	// be of type i (i=1,...,k)....  Let p_1_0,...,p_k_0 be specific [probabilities
	// that sum to 1], and suppose that the following hypothesis are to be tested:
	//
	// H0: p_i  = p_i_0  for i=1,...,k
	// H1: p_i != p_i_0  for at least one value of i.
	//
	// "We shall assume that a random sample of size n is to be taken from the given
	// population.  That is, n independent observations are to be taken, and there is
	// probability p_i that any particular observation will be of type i.  ...  On the
	// bases of these n observations, the hypotheses [H0 vs. H1] are to be tested.
	//
	// "For i=1,...,k, we shall let Ni denote the number of observations in the random
	// sample which are of type i.  Thus, N1,...,Nk are nonnegative integers such that
	// SUM{i=1..k}Ni = n.  ...
	//                                         
	// "In 1900, Karl Pearson... showed that if the hypothesis H0 is true, then:" 
	// (end of explicit quote from section 9.1)
	//
	//    d
	// Q ---> chi^2 with k-1 degrees of freedom.  Where Q is defined as:
	//
	//                  (Ni - (n * p_i_0))^2
	// Q = SUM{i=1..k} ---------------------- 
	//                      (n * p_i_0)
	// 
	// Building on Pearson's result, "We shall suppose now... that instead of testing
	// the simple null hypothesis that the parameters p_1,...,p_k have specific values,
	// we are interested in testing the composite null hypothesis that the values of
	// p_1,...,p_k belong to some specified subset of possible values.  In particular,
	// we shall consider problems in which the null hypothesis specifies that the 
	// parameters p_1,...,p_k can actually be represented as functions of a smaller
	// number of parameters....
	//
	// "In formal terms, in a problem of the type being considered, we are interested
	// in testing the hypothesis that for i=1,...,k, each probability p_i can be 
	// represented as a particular function pi_i(theta) of a vector of parameters
	// theta = (theta_1,...,theta_s).  It is assumed that s < k-1 and that no
	// component of the vector theta can be expressed as a function of the other s-1
	// components.  We shall let OMEGA denote the s-dimentional parameter space of all
	// possible values of theta.  Furthermore, we shall assume that the functions
	// pi_1(theta),...,pi_k(theta) always form a feasible set of values of p_1,...,p_k
	// in the sense that for every value of theta in OMEGA, pi_i(theta)>0 for i=1,...,k
	// and SUM{i=1..k}pi_i(theta) = 1.
	//
	// "The hypotheses to be tested can be written in the following form:
	//
	// H0: There exists a value of theta in OMEGA such that
	//       p_i = pi_i(theta) for i=1,...,k
	// H1: The hypothesis H0 is not true
	//
	// "...In order to carry out a [Pearson's] chi^2 test... of [these new H0 and H1],
	// the statistic Q defined [in Section] 9.1 must be modified because the expected
	// number of n*p_i_0 of observations of type i in a random sample of n observations
	// is no longer completely specified by the null hypothesis H0.  The modification
	// that is used is simply to replace n*p_i_0 by the M.L.E. of this expected number
	// under the assumption that H0 is true.  In other words, if theta-hat denotes the
	// M.L.E. of the parameter vector theta based on the observed numbers N1,...,Nk,
	// then the statistic Q is defined as follows:
	//
	//                  [Ni - n*pi_i(theta-hat)]^2
	// Q = SUM{i=1..k} ----------------------------
	//                      n*pi_i(theta-hat)
	//
	// "...In 1924, R. A. Fisher showed that if the null hypothesis H0 is true and 
	// certain regularity conditions are satisfied, then as the sample size 
	// n ---> infinity, Q --d--> chi^2 with k-1-s degrees of freedom. (end of quote from
	// section 9.2).
	//
	// And in particular if you have a contingency table, such as is the case with the
	// 2x2 table considered in a VerticalDecision. "Consider a two-way contingency table
	// containing R rows and C columns.  For i=1,...,R and j=1,...,C, we shall let 
	// p_i_j denote the probability that an individual selected at random from a given
	// population will be classified in the ith row and the jth column of the table.
	// Furthermore, we shall let p_i_+ denote the marginal probability that the 
	// individual will be classified in the ith row of the table, and we shall let p_+_j
	// denote the marginal probability that the individual will be classified in the jth
	// column of the table....  Suppose now that a random sample of n individuals is taken
	// from the given population.  For i=1,...,R and j=1,...,C, we shall let Nij denote
	// the number of individuals who are classified in the ith row and the jth column of
	// the table.  Furthermore, we shall let Ni+ denote the total number of individuals
	// classified in the ith row, and we shall let N+j denote the total number of
	// individuals classified in the jth column....  On the basis of these observations,
	// the following hypotheses are to be tested:
	//
	// H0: p_i_j = p_i_+ * p_+_j for i=1,...,R and j=1,...,C
	// H1: H0 is not true
	//
	// "Each individual in the population from which the smaple is taken must belong in 
	// one of the RC cells of the contingency table.  Under the null hypothesis H0,
	// the unknown probabilities p_i_j of these cells have been expressed as functions of
	// the unknown parameters p_i_+ and p_+_j.  Since SUM{i..R}p_i_+ = 1 and
	// SUM{j..C}p_+_j = 1, the actual number of unknown parameters to be estimated when
	// H0 is true is (R-1)+(C-1), or R+C-2.
	//
	// "For i=1,...,R and j=1,...,C, let E-hat_i_j denote the M.L.E., when H0 is true, of
	// the expected number of observations that will be classified in the ith row and the
	// jth column of the table.  In this problem, the statistic Q defined [in Section] 9.2
	// will have the following form:
	//
	//                              (Nij - E-hat_i_j)^2
	// Q = SUM{i=1..R} SUM{j=1..C} ---------------------
	//                                   E-hat_i_j
	//
	// "Furthermore, since the contingency table contains RC cells, ans since R+C-2
	// parameters are to be estimated when H0 is true, it follows that when H0 is true
	// and n-->infinity, Q --d--> chi^2 for which the number of degrees of freedom is
	// RC - 1 - (R+C-2) = (R-1)(C-1).
	//
	// "Next, we shall consider the form of the estimator E-hat_i_j.  The expected number
	// of observations in the ith row and the jth column is simply n*p_i_j.  When H0 is
	// true, p_i_j = p_i_+ * p_+_j.  Therefore, if p-hat_i_+ and p-hat_+_j denote the
	// M.L.E.'s of p_i_+ and p_+_j, then it follows that 
	// E-hat_i_j = n * p-hat_i_+ * p-hat_+_j.  Next, since p_i_+ is the probability that
	// an observation will be classified in the ith row, p-hat_i_+ is simply the proportion
	// of observations in the smaple that are classified in the ith row; that is, 
	// p-hat_i_+ = Ni+/n.  Similarly, p-hat_+_j = N+j/n, and it follows that
	//
	// E-hat_i_j = n * Ni+/n * N+j/n = (Ni+ * N+j) / n
	// (End of quote from Section 9.3).
	//
	// So the end of all this is that
	//                                   
	//                              (Nij - (Ni+ * N+j) / n)^2    
	// Q = SUM{i=1..R} SUM{j=1..C} ---------------------------  
	//                                    (Ni+ * N+j) / n
	//
	// AND
	//
	//    d
	// Q ---> chi^2 with 1 degree of freedom
	//
	// for a 2x2 table.  In Manning and Schuetze "Foundtations of Statistical Natural
	// Language Processing" p170 section 5.3.3 the formula for Q for a 2x2 is simplified 
	// to:
	//      n * ((N11 * N22) - (N12 * N21)) ^ 2
	// Q = -------------------------------------
	//             N1+ * N+1 * N2+ * N+2
	// 
	// and I (Christian Monson) verified this formula, which only involves a single
	// floating point operation (the final division), as correct with some algebra.
	// (The trick in this derivation is to note that 
	//  (Nij - (Ni+ * N+j) / n)^2 = ((N11 * N22) - (N12 * N21)) ^ 2 for each i and j, 
	//  by expanding Ni+ = Ni1 + Ni2 (similarly N+j) and also noting that 
	//  n-N11-N12-N21 = N22 etc.)
	//
	// Also note, that whereas the "large sample binomial test" is testing if
	// p > P(current) * P(complement), Pearson's chi^2 test is a two sided test asking if
	// p = P(current) * P(complement).
	// 
	private double pearsonsChiSquaredTest() {
		// Everything must be longs and not ints or else the integer arithmetic gets
		// too big and int values wrap around!!
		long parentSize              = grid.get(GridValues.CURRENT_COMPLEMENT);
		long currentNotComplement    = grid.get(GridValues.CURRENT_NOT_COMPLEMENT); 
		long notCurrentButComplement = grid.get(GridValues.NOT_CURRENT_BUT_COMPLEMENT);  
		long notCurrentNotComplement = grid.get(GridValues.NOT_CURRENT_NOT_COMPLEMENT);
		
		long currentMarginal       = grid.get(GridValues.CURRENT); 
		long complementMarginal    = grid.get(GridValues.COMPLEMENT);
		long notCurrentMarginal    = grid.get(GridValues.NOT_CURRENT);
		long notComplementMarginal = grid.get(GridValues.NOT_COMPLEMENT);
		
		long total                 = grid.get(GridValues.TOTAL);
		
		long numerator = (parentSize * notCurrentNotComplement) 
					   - (currentNotComplement * notCurrentButComplement);
		numerator *= numerator;  // square the numerator
		numerator *= total;
		
		long denominator =   currentMarginal 
						   * complementMarginal 
						   * notCurrentMarginal 
						   * notComplementMarginal;
		
		double numeratorAsDouble = numerator;
		double denominatorAsDouble = denominator;
		double pearsonsChiSquaredTestStatistic = numeratorAsDouble / denominatorAsDouble;
		
		return pearsonsChiSquaredTestStatistic;
	}
	
	
	// Definition 8.2.1 from "Statistical Inference" Cassella and Berger 2nd Edition
	// 
	// "The likelihood ratio test statistic for testing H0: theta in THETA-0 versus
	// H1: theta in ~THETA-0 is:
	//
	//                sup{THETA-0} L(theta | _x_) 
	// lambda(_x_) = -----------------------------
	//                sup{THETA} L(theta | _x_)
	//
	// [_x_ is the vector of (iid) data]
	//
	// A liklihood ratio test (LRT) is any test that has a rejection region of the form
	// {_x_: lambda(_x_) <= c}, where c is any number satisfying 0 <= c <= 1."
	//
	//
	// Theorem 10.3.3 from "Statistical Inference" Cassella and Berger 2nd Edition
	//
	// "Let X1,...,Xn be a random sample from a pdf or pmf f(x|theta).  Under the
	// regularity conditions in Miscellanea 10.6.2, if theta is an element of THETA-0,
	// then the distribution of the statistic -2*log(lambda(_X_)) converges to a chi
	// squared distribution as the sample size n --> infinity.  The degrees of freedom
	// of the limiting distribution is the difference between the number of free
	// parameters specified by theta in THETA-0 and the number of free parameters
	// specified by theta in THETA.
	//
	// "Rejection of H0: theta in THETA-0 for small values of lambda(_X_) is equivalent
	// to rejection for large values of -2*log(lambda(_X_)).  Thus,
	//
	// H0 is rejected iff -2*log(lambda(_X_)) >= chi^2_nu,alpha, 
	//
	// "where nu is the degrees of freedom specified in Theorem 10.3.3.  The Type I 
	// Error probability will be approximately alpha if theta is an element of 
	// THETA-0 and the sample size is large....
	//
	// "The computation of the degrees of freedom for the test statistic is usually
	// straightforward.  Most often, THETA can be represented as a subset of q-dimentional
	// Euclidian space that contains an open subset in R^q, and THETA-0 can be 
	// represented as a subset of p-dimensional Euclidian space that contains an open
	// subset in R^p, where p<q.  Then q-p=nu is the degrees of freedom for the test
	// statistic."
	//
	// Alrighty, with this background in mind, we can now formalize the informal
	// hypothesis test given in Manning and Schuetze "Foundations of Statistical Language
	// Processing" section 5.3.4 as the following.
	// 
	// Let X1,...,Xn iid Bernoulli(p1)
	//     Y1,...,Ym iid Bernoulli(p2)
	//
	// H0: p1  = p2
	// H1: p1 != p2
	//
	// The Likihood function of the joint density of all observed data is:
	//
	// L(p1, p2 | X1,...,Xn, Y1,...,Ym) =
	//
	//                   x_i      1-x_i                  y_j      1-y_j
	//  = PROD{i=1..n} p1   (1-p1)      * PROD{j=1..m} p2   (1-p2)
	//
	//      SUM{i=1..n}x_i     SUM{i=1..n}(1-x_i)   SUM{j=1..m}y_j     SUM{j=1..m}(1-y_j)
	//  = p1              (1-p1)                * p2              (1-p2)
	//
	// When maximized this unconstrained joint desity yields the MLE's
	//
	// p1-hat = X-bar = (SUM{i=1..n} x_i) / n and 
	// p2-hat = Y-bar = (SUM{j=1..m} y_j) / m
	// 
	//
	// Under the null hypothesis H0, p=p1=p2 the Liklihood function becomes
	//
	//                  x_i     1-x_i                 y_j     1-y_j
	//  = PROD{i=1..n} p   (1-p)      * PROD{j=1..m} p   (1-p)
	//
	//     SUM{i=1..n}x_i    SUM{i=1..n}(1-x_i)   SUM{j=1..m}y_j    SUM{j=1..m}(1-y_j)
	//  = p              (1-p)                 * p              (1-p)
	// 
	// When maximized under H0 the constrained MLE of p is:
	//
	//          SUM{i=1..n} x_i + SUM{j=1..m} y_j
	// p-hat = -----------------------------------
	//                        n + m
	//
	//
	//                       k      n-k
	// Now let L(k,n,pr) = pr (1-pr)
	//
	// be the kernal liklihood function of a binomial, then 
	//
	//                                 L(SUM x_i, n, p-hat) * L(SUM y_j, m, p-hat)
	// lambda(X1,...,Xn,Y1,...,Ym) = -----------------------------------------------
	//                                L(SUM x_i, n, p1-hat) * L(SUM y_j, m, p2-hat)
	//
	// And -2*log(lambda(X1,...,Xn,Y1,...,Ym) --d--> Chi^2 with 1 degree of freedom--
	// since H0 has 1 degree of freedom and H1 has 2 degrees of freedom and hence the
	// difference in the number of degrees of freedom is 1.
	//
	// Now if we let X1,...,Xn be the distribution of "complement | current" that is,
	// n is the number of stems that occur in the current scheme, and 
	// "complement | current" is the distribution of the occurence in the "complement"
	// scheme of each of those stems that occur in the current scheme.  Similarly,
	// we can let Y1,...,Yn be the distribution of "complement | ~current".  So,
	// m is the number of stems that do NOT occur in the current scheme, and
	// "complement | ~current" is the distribution of the occurence in the "complement"
	// scheme of each of those stems that do not occur in the current scheme.  Then
	//
	// p-hat  = complement / total
	// p1-hat = complement&current / current
	// p2-hat = complement&~current / ~current
	//          
	// SUM x_i = complement & current
	// SUM y_i = complement & ~current
	//
	// And lambda is as deliniated above. 
	//
	// I believe that the fact that "current" i.e. n may not be too terribly large is
	// not too much of a problem because it is n+m or current + ~current = total that
	// needs to be large, because it is the size of ALL the data, n+m that needs to go to
	// infinity.  At least I hope this is not a problem.
	//

	// Like pearsonsChiSquaredTest this constrained liklihood ratio test for binomials
	// tests p = P(current) * P(complement) and not the thing we are actually interested 
	// in, namely: p > P(current) * P(complement).  So combine this test with a sanity
	// check on complementSize * currentSize, and if 
	// parentSize < complementSize * currentSize, then even if 
	// constrainedLikelihoodRatioOfBernoulli() is large you should still not move up the
	// network.  A large liklihood ratio when parentSize < complementSize * currentSize
	// would actually be evidence that P(current & complement) is much *less* likely than 
	// chance, so we definitely do *not* want to put them in the same morphological 
	// sub-class
	private double constrainedLikelihoodRatioOfBernoulli() {
		// no cast necessary to convert from int to double
		double parentSize              = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentNotComplement    = grid.get(GridValues.CURRENT_NOT_COMPLEMENT); 
		double notCurrentButComplement = grid.get(GridValues.NOT_CURRENT_BUT_COMPLEMENT);  
		double notCurrentNotComplement = grid.get(GridValues.NOT_CURRENT_NOT_COMPLEMENT);
		
		double current       = grid.get(GridValues.CURRENT); 
		double complement    = grid.get(GridValues.COMPLEMENT);
		double notCurrent    = grid.get(GridValues.NOT_CURRENT);
		double notComplement = grid.get(GridValues.NOT_COMPLEMENT);
		
		double total         = grid.get(GridValues.TOTAL);
		
		
		// calculate the log of the numerator
		// where the numerator is:        p-hat  ^ (current & complement)
		//                         * (1 - p-hat) ^ (current - (current & complement))
		//                         *      p-hat  ^ (~current & complement)
		//                         * (1 - p-hat) ^ (~current - (~current & complement))
		// and notice that:
		// p-hat                            = complement / total    = probOfComplement
		// 1 - p-hat                        = ~complement / total   = probOfNotComplement
		// current - (current & complement) = current & ~complement 
		// ~current - (~current & complement) = ~current & ~complement
		
		double probOfComplement = complement / total;       // p-hat
		double probOfNotComplement = notComplement / total; // 1 - p-hat
		
		double numeratorFactor1 = Math.log(probOfComplement);
		double numeratorFactor2 = Math.log(probOfNotComplement);
		double numeratorFactor3 = numeratorFactor1;
		double numeratorFactor4 = numeratorFactor2;
		
		numeratorFactor1 *= parentSize;
		numeratorFactor2 *= currentNotComplement;
		numeratorFactor3 *= notCurrentButComplement;
		numeratorFactor4 *= notCurrentNotComplement;
		
		double logOfLiklihoodRatio =   numeratorFactor1
						       		 + numeratorFactor2
						       		 + numeratorFactor3
						       		 + numeratorFactor4;
		
		
		// calculate the log of the denominator
		// where the denominator is:        p1-hat  ^ (current & complement)
		//                           * (1 - p1-hat) ^ (current - (current & complement)
		//                           *      p2-hat  ^ (~current & complement)
		//                           * (1 - p2-hat) ^ (~current - (~current & complement)
		// and notice that
		//     p1-hat = ( current &  complement) /  current
		// 1 - p1-hat = ( current & ~complement) /  current
		//     p2-hat = (~current &  complement) / ~current
		// 1 - p2-hat = (~current & ~complement) / ~current
		// current - (current & complement) = current & ~complement 
		// ~current - (~current & complement) = ~current & ~complement
	
		// p1-hat
		double probOfComplementGivenCurrent = parentSize / current;
		
		// 1 - p1-hat
		double probOfNotComplementGivenCurrent = currentNotComplement / current;
		
		// p2-hat
		double probOfComplementGivenNotCurrent = notCurrentButComplement / notCurrent;
		
		// 1 - p2-hat
		double probOfNotComplementGivenNotCurrent = notCurrentNotComplement / notCurrent;
		
		double denominatorFactor1 = Math.log(probOfComplementGivenCurrent);
		double denominatorFactor2 = Math.log(probOfNotComplementGivenCurrent);
		double denominatorFactor3 = Math.log(probOfComplementGivenNotCurrent);
		double denominatorFactor4 = Math.log(probOfNotComplementGivenNotCurrent);
		
		denominatorFactor1 *= parentSize;
		denominatorFactor2 *= currentNotComplement;
		denominatorFactor3 *= notCurrentButComplement;
		denominatorFactor4 *= notCurrentNotComplement;
		
		logOfLiklihoodRatio = logOfLiklihoodRatio - denominatorFactor1
							  					  - denominatorFactor2
							  					  - denominatorFactor3
							  					  - denominatorFactor4;
		
		double chiSquaredStat = -2 * logOfLiklihoodRatio;
		
		return chiSquaredStat;
	}
	
	// The likelihood ratio test implemented as constrainedLikelihoodRatioOfBernoulli()
	// has an interesting property compared to all the other statistical tests in that
	// it treats current and complement asymetrically.  Since the test may be sensitive
	// to the absolute size of current and complement it seems like a good idea to 
	// emirically observe the behavior when the roles of current and complement are
	// swapped.  Namely, X1,...,Xn are now "current | complement" and Y1,...,Yn are
	// "current | ~complement".
	private double constrainedLikelihoodRatioOfBernoulli_conditionOnComplement() {
		int parentSize            = grid.get(GridValues.CURRENT_COMPLEMENT);
		int currentSize           = grid.get(GridValues.CURRENT); 
		int complementSize        = grid.get(GridValues.COMPLEMENT);  
		int total                 = grid.get(GridValues.TOTAL);
 
		VerticalDecision swapped = new VerticalDecision(parentSize, 
														complementSize, 
														currentSize, 
														total);
		return swapped.constrainedLikelihoodRatioOfBernoulli();
	}
	
	// Pointwise mutual information is an information-theoretically motivated measure
	// of how much one event informs us about another event.  Manning and Schuetze 
	// "Foundations of Statistical Language Processing" section 5.4, point
	// out several difficulties that pointwise mutual information runs into when
	// looking for collocations in text.  But just as a comprison it is implemented
	// here.
	//
	// The formula is straightforward:
	//
	//                 P(x,y)     
	// I(x,y) = log  ----------
	//             2  P(x)P(y)
	//
	// Substituting in the MLE's to estimate P(current), P(complement),
	// and P(current & complement) and doing some algebra:
	//
	//                  (current & complement) * total    /
	// I(x,y)-hat = ln --------------------------------  / ln 2
	//                       current * complement       /
	//
	// It is interesting to compare pointwise mutual information with the other metrics
	// and statistical tests that I have implemented in VerticalDecisionGrid.  It seems
	// that pointwise mutual information is most similar to the largeSampleBernoulliTest().
	// In particular where pointwise mutual information treats P(x,y) and (P(x)P(y))
	// multiplicatively, the large sample bernoulli test treats them additively.
	private double pointwiseMutualInformation() {
		// no cast necessary from int to double
		double parentSize         = grid.get(GridValues.CURRENT_COMPLEMENT);
		double currentMarginal    = grid.get(GridValues.CURRENT); 
		double complementMarginal = grid.get(GridValues.COMPLEMENT);  
		double total              = grid.get(GridValues.TOTAL);

		double lnOf2 = Math.log(2);
		
		double numerator   = parentSize * total;
		double denominator = currentMarginal * complementMarginal;
		double operand = numerator / denominator;
		double mutualInformation = Math.log(operand) / lnOf2;
		
		return mutualInformation;
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		toReturn += String.format("%ncurrent: ");
		toReturn += current.toPrettyString(10);
		toReturn += String.format("%nparent:  ");
		toReturn += parent.toPrettyString(10);
		if (grid != null) {
			toReturn += String.format("%n%n             | current | ~current |%n");
			toReturn += String.format("-------------+---------+----------+--------%n");
			toReturn += String.format("  complement | %-7d | %-8d | %-6d%n", 
									  grid.get(GridValues.CURRENT_COMPLEMENT), 
									  grid.get(GridValues.NOT_CURRENT_BUT_COMPLEMENT), 
									  grid.get(GridValues.COMPLEMENT));
			toReturn += String.format("-------------+---------+----------+--------%n");
			toReturn += String.format(" ~complement | %-7d | %-8d | %-6d%n",
									  grid.get(GridValues.CURRENT_NOT_COMPLEMENT),
									  grid.get(GridValues.NOT_CURRENT_NOT_COMPLEMENT),
									  grid.get(GridValues.NOT_COMPLEMENT));
			toReturn += String.format("-------------+---------+----------+--------%n");
			toReturn += String.format("             | %-7d | %-8d | %-6d",
									  grid.get(GridValues.CURRENT),
									  grid.get(GridValues.NOT_CURRENT),
									  grid.get(GridValues.TOTAL));
		}
		return toReturn;
	}

	
	/**
	 * Sorts VerticalDecisions's first lexicographically on their parent and then
	 *   lexicographically on their current.
	 * 
	 * @see PartialOrderNode.Lexicographically
	 * 
	 * @author cmonson
	 *
	 */
	public static class ByParentLexicographically implements Comparator<VerticalDecision> {
		/**
		 * @param verticalDecision1 The first <code>VerticalDecision</code> to be compared 
		 * @param verticalDecision2 The second <code>VerticalDecision</code> to be compared
		 * @return <code>-1</code> if <code>verticalDecision1</code>'s parent is less than 
		 * 				<code>verticalDecision2</code>'s parent lexicographically.
		 * 	   <br><code>&nbsp1</code> if <code>verticalDecision2</code>'s parent is less than
		 * 			    <code>verticalDecision1</code>'s parent lexicographically.
		 * 	   <br>Backs off to ByCurrentLexicographically if <code>verticalDecision1</code>'s 
		 * 				parent equals 
		 *     			<code>verticalDecision2</code>'s parent lexicographically
		 *     <br><code>&nbsp0</code> if <code>verticalDecision1</code>'s parent equals 
		 *     			<code>verticalDecision2</code>'s parent lexicographically, AND
		 *     <code>verticalDecision1</code>'s current equals 
		 *     			<code>verticalDecision2</code>'s current lexicographically.
		 */
		public int compare(VerticalDecision verticalDecision1, 
						   VerticalDecision verticalDecision2) {
			int parentToParent = compareStatic(verticalDecision1, verticalDecision2);
			if (parentToParent != 0) {
				return parentToParent;
			}
			return ByCurrentLexicographically.compareStatic(verticalDecision1, 
															verticalDecision2);
		}
		
		private static int 
		compareStatic(VerticalDecision verticalDecision1, 
					  VerticalDecision verticalDecision2) {
			
			// check for null parents
			Scheme parent1 = verticalDecision1.parent;
			Scheme parent2 = verticalDecision2.parent;
			if ((parent1 == null) || (parent2 == null)) {
				return 0;
			}
			
			return parent1.toString().compareTo(parent2.toString());
		}
		
		@Override
		public String toString() {
			return "Sort Vertical Decisions by Parent Lexicographically";
		}
	}
	
	/**
	 * Sorts VerticalDecisions's first lexicographically on their child and then
	 *   lexicographically on their parent.
	 * 
	 * @see PartialOrderNode.Lexicographically
	 * 
	 * @author cmonson
	 *
	 */
	public static class ByCurrentLexicographically implements Comparator<VerticalDecision> {
		/**
		 * @param verticalDecision1 The first <code>VerticalDecision</code> to be compared 
		 * @param verticalDecision2 The second <code>VerticalDecision</code> to be compared
		 * @return <code>-1</code> if <code>verticalDecision1</code>'s current is less than 
		 * 				<code>verticalDecision2</code>'s child lexicographically.
		 * 	   <br><code>&nbsp1</code> if <code>verticalDecision2</code>'s current is less than
		 * 			    <code>verticalDecision1</code>'s current lexicographically.
		 * 	   <br>Backs off to ByParentLexicographically if <code>verticalDecision1</code>'s 
		 * 				current equals 
		 *     			<code>verticalDecision2</code>'s current lexicographically
		 *     <br><code>&nbsp0</code> if <code>verticalDecision1</code>'s current equals 
		 *     			<code>verticalDecision2</code>'s current lexicographically, AND
		 *     							  <code>verticalDecision1</code>'s parent equals 
		 *     			<code>verticalDecision2</code>'s parent lexicographically.
		 */
		public int compare(VerticalDecision verticalDecision1, VerticalDecision verticalDecision2) {
			int childToChild = compareStatic(verticalDecision1, verticalDecision2);
				
			if (childToChild != 0) {
				return childToChild;
			}
			return ByParentLexicographically.compareStatic(verticalDecision1, verticalDecision2);
		}
		
		private static int 
		compareStatic(VerticalDecision verticalDecision1, 
					  VerticalDecision verticalDecision2) {

			// check for null currents
			Scheme current1 = verticalDecision1.current;
			Scheme current2 = verticalDecision2.current;
			if ((current1 == null) || (current2 == null)) {
				return 0;
			}			
			
			return current1.toString().compareTo(current2.toString());
		}
		
		@Override
		public String toString() {
			return "Sort Vertical Decisions by Current Lexicographically";
		}
	}
	
	/**
	 * Sorts VerticalDecisions's first on their parent size and then
	 *   lexicographically on their parent.
	 * 
	 * @see PartialOrderNode.Lexicographically
	 * 
	 * @author cmonson
	 *
	 */
	public static class ByParentSize implements Comparator<VerticalDecision> {
		/**
		 * @param verticalDecision1 The first <code>VerticalDecision</code> to be compared 
		 * @param verticalDecision2 The second <code>VerticalDecision</code> to be compared
		 * @return <code>-1</code> if <code>verticalDecision1</code>'s parent size is less than 
		 * 				<code>verticalDecision2</code>'s parent size.
		 * 	   <br><code>&nbsp1</code> if <code>verticalDecision2</code>'s parent size is less than
		 * 			    <code>verticalDecision1</code>'s parent size.
		 * 	   <br>Backs off to ByParentLexicographically if <code>verticalDecision1</code>'s 
		 * 				parent equals 
		 *     			<code>verticalDecision2</code>'s parent lexicographically
		 *     <br><code>&nbsp0</code> if <code>verticalDecision1</code>'s parent size equals
		 *     			<code>verticalDecision2</code>'s parent size, AND
		 *     							  <code>verticalDecision1</code>'s parent equals 
		 *     			<code>verticalDecision2</code>'s parent lexicographically, AND
		 *     <code>verticalDecision1</code>'s current equals 
		 *     			<code>verticalDecision2</code>'s current lexicographically.
		 */
		public int compare(VerticalDecision verticalDecision1, 
						   VerticalDecision verticalDecision2) {
			
			Integer parent1Size, parent2Size;
			
			// If possible, get the parent size right out of the grid
			// otherwise get it out of the parent.
			if (verticalDecision1.grid != null) {
				parent1Size = 
					verticalDecision1.grid.get(GridValues.CURRENT_COMPLEMENT);
			} else if (verticalDecision1.parent != null) {
				parent1Size = verticalDecision1.parent.adherentSize();
			} else {
				return 0;
			}
			
			// If possible, get the parent size right out of the grid
			if (verticalDecision2.grid != null) {
				parent2Size = 
					verticalDecision2.grid.get(GridValues.CURRENT_COMPLEMENT);
			} else if (verticalDecision2.parent != null) {
				parent2Size = verticalDecision2.parent.adherentSize();
			} else {
				return 0;
			}
			
			// subtracting the size of the second from the size of the first
			// will produced the correct positive or negative return value.
			int parentToParent = parent1Size - parent2Size;
				
			if (parentToParent != 0) {
				return parentToParent;
			}
			return new ByParentLexicographically().compare(verticalDecision1, verticalDecision2);
		}
		
		@Override
		public String toString() {
			return "Sort Vertical Decisions by Parent Size";
		}
	}

}


