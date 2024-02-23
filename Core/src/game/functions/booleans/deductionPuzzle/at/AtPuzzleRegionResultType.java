package game.functions.booleans.deductionPuzzle.at;

/**
 * Defines the types of At test for puzzle according to region and a specific
 * result to check.
 */
public enum AtPuzzleRegionResultType
{
	/** To check if the count of a region (or number in a cell) is less or equal to the result. */
	Most,

	/** To check if the sum of a region (or number in a cell) is Most or equal to the result. */
	Least,
}
