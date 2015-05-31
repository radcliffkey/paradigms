/**
 * 
 */
package monson.christian.morphology.paraMor.searchAndProcessing;

public enum VerticalMetric {
	
	// Baselines
	
	BASELINE_NEVER_MOVE_UP(
		"Baseline: Never Move Up",
		false,  // this metric/test does NOT use the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
			
	BASELINE_ALWAYS_MOVE_UP(
		"Baseline: Always Move Up", 
		false,  // this metric/test does NOT use the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
			
	
	// Heuristics
			
	LEVELS_SKIPPED(
		"Levels Skipped",
		false,  // this metric/test does NOT use the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
			
	AVERAGE_LENGTH_OF_PARENT_STEMS(
		"Average Length of Parent Stems",
		false,  // this metric/test does NOT use the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
			
	MINIMUM_LENGTH_OF_PARENT_STEMS(
		"Minimum Length of Parent Stems",
		false,  // this metric/test does NOT use the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
					  
	
	// Ratio Variants
			
	RATIO(
		"Ratio", 
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
						          
	ORACLE_SLIDING_RATIO_MEAN(
		"Oracle Sliding Ratio: Mean",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does not correct for one sidedness
		true),  // this metric/test DOES use oracle information
			
	ORACLE_SLIDING_RATIO_MEDIAN(
		"Oracle Sliding Ratio: Median",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does not correct for one sidedness
		true),  // this metric/test DOES use oracle information
			
	LINEAR_SLIDING_RATIO(
		"Linear Sliding Ratio",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
						   
	RATIO_NO_ZEROS_IN_TABLE(
		"Ratio - No Zeros Allowed in Table",
		true,   // this metric/test USES the 2x2 stem grid
		true,   // this metric/test DOES require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
		
	RATIO_PARENT_LARGER_THAN_1 (
		"Ratio - Parent Larger than 1",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		true,   // this metric/test DOES require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
		
	RATIO_ONE_SIDED (
		"Ratio - One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test DOES correct for one sidedness
		false), // this metric/test does NOT use oracle information
			
		
	// Dice Variants
		
	DICE(
		"Dice",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
						   
	ORACLE_SCALED_DICE_MEAN(
		"Oracle Scaled Dice: Mean",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		true),  // this metric/test DOES use oracle information
						   
	ORACLE_SCALED_DICE_MEDIAN(
		"Oracle Scaled Dice: Median",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		true),  // this metric/test DOES use oracle information
				   		   
	SCALED_DICE_EXPONENTIAL(
		"Sclaed Dice: Exponential",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for onesidedness
		false), // this metric/test does NOT use oracle information
				   		   
	SCALED_DICE_FACTORIAL(
		"Sclaed Dice: Factorial",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for onesidedness
		false), // this metric/test does NOT use oracle information
		   		   		   
    SCALED_DICE_FACTORIAL_NO_ZEROS_IN_TABLE(
		"Scaled Dice Factorial - No Zeros Allowed in Table",
		true,   // this metric/test USES the 2x2 stem grid
		true,   // this metric/test DOES require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
					
	SCALED_DICE_FACTORIAL_PARENT_LARGER_THAN_1 (
		"Scaled Dice Factorial - Parent Larger than 1",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		true,   // this metric/test DOES require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
					
	SCALED_DICE_FACTORIAL_ONE_SIDED (
		"Scaled Dice Factorial - One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test DOES correct for one sidedness
		false), // this metric/test does NOT use oracle information
			
			
	// Large Sample Bernoulli Test Variants
			
	LARGE_SAMPLE_BERNOULLI_TEST(
		"Large Sample Bernoulli Test",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
			
			
	// Pearsons Chi^2 Test Variants
					      
	PEARSONS_CHI_SQUARED_TEST(
		"Pearson's Chi-Squared Test",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
			
	PEARSONS_CHI_SQUARED_TEST_ONE_SIDED(
		"Pearson's Chi-Squared Test--One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test CORRECTS for one sidedness
		false), // this metric does NOT use oracle information

	
	// Constrained Likelihood Ratio of Bernoulli Variants

	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI(
		"Constrained Likelihood Ratio of Bernoulli",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information

	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED(
		"Constrained Likelihood Ratio of Bernoulli--One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test CORRECTS for one sidedness
		false), // this metric does NOT use oracle information
			
	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_ONE_SIDED_NO_ZEROS_IN_TABLE(
		"Constrained LR of Bernoulli--One Sided--No Zeros Allowed In Table",
		true,   // this metric/test USES the 2x2 stem grid
		true,   // this metric/test DOES require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test CORRECTS for one sidedness
		false), // this metric does NOT use oracle information			
			
			
	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_CONDITION_ON_COMPLEMENT(
		"Constrained Likelihood Ratio of Bernoulli-Condition on Complement",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information

	CONSTRAINED_LIKELIHOOD_RATIO_OF_BERNOULLI_CONDITION_ON_COMPLEMENT_ONE_SIDED(
		"Constrained Likelihood Ratio of Bernoulli-Condition on Complement--One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test CORRECTS for one sidedness
		false), // this metric does NOT use oracle information
				
			
	// Pointwise Mutual Information Variants
			
	POINTWISE_MUTUAL_INFORMATION(
		"Pointwise Mutual Information",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric does NOT use oracle information
	
	POINTWISE_MUTUAL_INFORMATION_NO_ZEROS_IN_TABLE(
		"Pointwise Mutual Information - No Zeros Allowed in Table",
		true,   // this metric/test USES the 2x2 stem grid
		true,   // this metric/test DOES require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
			
	POINTWISE_MUTUAL_INFORMATION_PARENT_LARGER_THAN_1 (
		"Pointwise Mutual Information - Parent Larger than 1",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		true,   // this metric/test DOES require the parent to have more than 1 stem
		false,  // this metric/test does NOT correct for one sidedness
		false), // this metric/test does NOT use oracle information
					
	POINTWISE_MUTUAL_INFORMATION_ONE_SIDED (
		"Pointwise Mutual Information - One Sided",
		true,   // this metric/test USES the 2x2 stem grid
		false,  // this metric/test does NOT require no zeros in the grid
		false,  // this metric/test does NOT require the parent to have more than 1 stem
		true,   // this metric/test DOES correct for one sidedness
		false)  // this metric/test does NOT use oracle information
			
	; // End list of members of the VerticalMetricName enum
	
	private String prettyName;

	private boolean usesGrid;
	private boolean requiresNoZerosInGrid;
	private boolean parentMustHaveMoreThanOneStem;
	private boolean correctForLessLikelyThanChance;  // true if this VerticalMetric
													 // performs onesidedness correction
													 // (some tests that are already
													 // one sided leave this false.)
	private boolean oracle;  // true if this VerticalMetric uses information
							 // that can only be obtained by an oracle--and 
							 // that therefor cannot be used by a real
							 // search algorithm.
		
	private VerticalMetric(String prettyName,
						   boolean usesGrid,
						   boolean requiresNoZerosInGrid,
						   boolean parentMustHaveMoreThanOneStem,
						   boolean correctForLessLikelyThanChance,
						   boolean oracle) {
		
		this.prettyName = prettyName;
		this.usesGrid = usesGrid;
		this.requiresNoZerosInGrid = requiresNoZerosInGrid;
		this.parentMustHaveMoreThanOneStem = parentMustHaveMoreThanOneStem;
		this.correctForLessLikelyThanChance = correctForLessLikelyThanChance;
		this.oracle = oracle;
	}
	
	public boolean isGridUser() {
		return usesGrid;
	}
	
	public boolean requiresNoZerosInGrid() {
		return requiresNoZerosInGrid;
	}
	
	public boolean parentMustHaveMoreThanOneStem() {
		return parentMustHaveMoreThanOneStem;
	}
	
	public boolean isCorrectForLessLikelyThanChance() {
		return correctForLessLikelyThanChance;
	}
	
	public boolean isOracle() {
		return oracle;
	}
	
	
	@Override
	public String toString() {
		return prettyName;
	}
}