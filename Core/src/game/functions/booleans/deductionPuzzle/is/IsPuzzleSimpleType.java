package game.functions.booleans.deductionPuzzle.is;

/**
 * Defines the types of Is test for puzzle with no parameter.
 */
public enum IsPuzzleSimpleType
{
	/**
	 * To check if all the variables of a deduction puzzle are set to values
	 * satisfying all the constraints.
	 */
	Solved, 
	
	/**
	 * To check if edges cross in a puzzle
	 */
	Crossed, 
	
	/**
	 * To check if all tiles in a puzzle are complete or empty
	 */
	TilesComplete

}
