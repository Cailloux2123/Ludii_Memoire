package game.functions.booleans.deductionPuzzle.at.regionResult;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.StringRoutines;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;

/**
 * Returns true if the count of a region is equal to the result.
 * 
 * @author Pierre.Accou and Eric.Piette
 * TODO : Change documentation
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class AtMost extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;
	
	/** What. */
	private final IntFunction whatFn;

	/** Which result. */
	private final IntFunction resultFn;

	/** Which type. */
	private final SiteType type;
	
	/** The name of the region to check.. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type   The graph element of the region [Default SiteType of the board].
	 * @param region The region to count.
	 * @param what   The index of the piece to count [1].
	 * @param result The result to check.
	 */
	public AtMost
	(
		@Opt   final SiteType       type,
		@Opt   final RegionFunction region,
		@Opt   final IntFunction    what,
		@Opt   final String         nameRegion,
			   final IntFunction    result
	)
	{
		this.region = region;
		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = RegionTypeStatic.Regions;
		whatFn = (what == null) ? new IntConstant(1) : what;
		resultFn = result;
		this.type = type;
		name = (nameRegion == null) ? "" : nameRegion;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		if (region != null)
		{
			final int result = resultFn.eval(context);
			final int[] sites = region.eval(context).sites();
			int currentCount = 0;
		
			for (final int site : sites)
				if (ps.isResolved(site, type))
					currentCount += ps.what(site, type);
		
			if (currentCount > result)
				return false;
		}
		else
		{
			int result = resultFn.eval(context);
			final Regions[] regions = context.game().equipment().regions();

			Integer[] regionHint;
			if (type == SiteType.Cell)
				regionHint = context.game().equipment().cellHints();
			else if (type == SiteType.Vertex)
				regionHint = context.game().equipment().vertexHints();
			else
				regionHint = context.game().equipment().edgeHints();

			for (final Regions reg : regions)
			{
				if (reg.name().contains(name))
					if (reg.regionTypes() != null)
					{
						final RegionTypeStatic[] areas = reg.regionTypes();
						for (final RegionTypeStatic area : areas)
						{
							final Integer[][] regionsList = reg.convertStaticRegionOnLocs(area, context);
							int indexRegion = 0;
							for (final Integer[] locs : regionsList)
							{
								if (resultFn.isHint())
								{
									context.setHint(regionHint[indexRegion].intValue());
									result = resultFn.eval(context);
								}
								int currentCount = 0;
								for (final Integer loc : locs)
								{
									if (ps.isResolved(loc.intValue(), type))
										currentCount += ps.what(loc.intValue(), type);
								}
									
								if (currentCount > result)
									return false;
								indexRegion++;
							}
						}
					}
			}
		}
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "Sum(" + region + ") = " + resultFn;
		return str;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		resultFn.preprocess(game);
		if (region != null)
			region.preprocess(game);
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.DeductionPuzzle;
		gameFlags |= resultFn.gameFlags(game);

		if (region != null)
			gameFlags |= region.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);

		concepts.or(resultFn.concepts(game));

		if (region != null)
			concepts.or(region.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(resultFn.writesEvalContextRecursive());

		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Hint.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(resultFn.readsEvalContextRecursive());

		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= resultFn.missingRequirement(game);

		if (region != null)
			missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (is Sum ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);
		willCrash |= resultFn.willCrash(game);

		if (region != null)
			willCrash |= region.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------
	/**
	 * @return The region to sum.
	 */
	public RegionFunction region() 
	{
		return region;
	}

	/**
	 * @return The result to check.
	 */
	public IntFunction result() 
	{
		return resultFn;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the number of " + whatFn.toEnglish(game) + StringRoutines.getPlural(whatFn.toEnglish(game)) + " in " + region.toEnglish(game) + " equals " + resultFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}