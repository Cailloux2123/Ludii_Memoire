package game.functions.booleans.deductionPuzzle.is;

/**
 * Defines the types of Is test for puzzle according to region and a specific
 * result to check.
 */
public enum IsPuzzleRegionResultType
{
	/** To check if the count of a region is equal to the result. */
	Count,

	/** To check if the sum of a region is equal to the result. */
	Sum,
	
	/** To check if all element are connected or not */
	Connex,
	
	/** To check if the value in the region is unique or not */
	Distinct,
	
	/** To check if the pattern match with the hint */
	Match,
	
	/** To check if the count of empty cell of a region is equal to the result. */
	CountEmpty
}
