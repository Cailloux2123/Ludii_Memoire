package game.functions.booleans.deductionPuzzle.at;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.deductionPuzzle.at.regionResult.AtLeast;
import game.functions.booleans.deductionPuzzle.at.regionResult.AtMost;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Whether the specified query is true for a deduction puzzle.
 * 
 * @author Pierre.Accou and Eric.Piette
 * TODO : Change documentation
 * 
 */
@SuppressWarnings("javadoc")
public class At extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For a constraint related to count or sum.
	 * 
	 * @param isType     The query type to perform.
	 * @param type       The graph element of the region [Default SiteType of the board].
	 * @param region     The region [Regions].
	 * @param of         The index of the piece [1].
	 * @param nameRegion The name of the region to check.
	 * @param result     The result to check.
	 * 
	 * @example (is Count (sites All) of:1 8)
	 * @example (is Sum 5)
	 */
	public static BooleanFunction construct
	(
		           final AtPuzzleRegionResultType isType,
		@Opt       final SiteType                 type,
		@Opt       final RegionFunction           region,
		@Opt @Name final IntFunction              of,
		@Opt       final String                   nameRegion,
			       final IntFunction              result
	)
	{
		switch (isType)
		{
		case Most:
			return new AtMost(type, region, of, result);
		case Least:
			return new AtLeast(type, region, of, result);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPuzzleRegionResultType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private At()
	{
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public boolean eval(Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Is.eval(): Should never be called directly.");

		// return false;
	}
}
