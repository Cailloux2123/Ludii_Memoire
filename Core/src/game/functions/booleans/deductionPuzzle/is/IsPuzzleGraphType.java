package game.functions.booleans.deductionPuzzle.is;

/**
 * Defines the types of Is test for puzzle according to a graph element.
 */
public enum IsPuzzleGraphType
{
	/**
	 * To check if each sub region of a static region is different.
	 */
	Unique,
	
	/**
	 * To check if edges cross in a puzzle
	 */
	Crossed,
	
	/**
	 * To check if edges takes the right directions in case of some hints
	 */
	RightDirections,
	
	/**
	 * To check if all hint are selected with edges
	 */
	AllHintVisited
}
