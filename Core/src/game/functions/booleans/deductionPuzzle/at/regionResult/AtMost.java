package game.functions.booleans.deductionPuzzle.at.regionResult;

import java.util.Arrays;
import java.util.BitSet;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
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
 * Returns true if the count of a region is less or equal to the result .
 * Or returns true if the number is less or equal to the result.
 * 
 * @author Pierre.Accou and Tom.Doumont
 *
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
	public final String name;
	
	private final BooleanFunction individualFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType   The graph element of the region [Default SiteType of the board].
	 * @param region 		The region to count.
	 * @param what   		The index of the piece to count [1].
	 * @param nameRegion	The name of the region where we work
	 * @param individual    The boolean to check if we work on a region or a specific cell[False]
	 * @param result 		The result to check.
	 */
	public AtMost
	(
		@Opt        final SiteType         elementType,
		@Opt        final RegionFunction   region,
		@Opt        final IntFunction      what,
		@Opt        final String           nameRegion,
		@Opt @Name  final BooleanFunction  individual,
			        final IntFunction      result
	)
	{
		this.region = region;
		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = RegionTypeStatic.Regions;
		whatFn = (what == null) ? new IntConstant(1) : what;
		resultFn = result;
		type = (elementType == null) ? SiteType.Cell : elementType;
		name = (nameRegion == null) ? "" : nameRegion;
		individualFn = (individual == null) ? new BooleanConstant(false) : individual;
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
					if (individualFn.eval(context)) {
						currentCount = 0;
						currentCount += ps.what(site, type);
						if (currentCount > result)
							return false;
					}
					else 
						currentCount += ps.what(site, type);
		
			if (currentCount > result)
				return false;
		}
		else
		{
			int result = resultFn.eval(context);
			final Regions[] regions = context.game().equipment().regions();

			Integer[][] regionHint;
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
									context.setHint(Arrays.stream(regionHint[indexRegion]).mapToInt(Integer::intValue).toArray());
									result = resultFn.eval(context);
								}
								int currentCount = 0;
								for (final Integer loc : locs)
								{
									if (ps.isResolved(loc.intValue(), type))
										if (individualFn.eval(context)) {
											currentCount = 0;
											currentCount += ps.what(loc.intValue(), type);
											if (currentCount > result)
												return false;
										}
										else 
											currentCount += ps.what(loc.intValue(), type);
								}
									
								if (currentCount > result)
									return false;
								indexRegion++;
							}
						}
					}
					else {
						int currentSum = 0;

						for (final Integer loc : reg.sites())
						{
							if (ps.isResolved(loc.intValue(), type))
								if (individualFn.eval(context)) {
									currentSum = 0;
									currentSum += ps.what(loc.intValue(), type);
									if (currentSum > result)
										return false;
								}
								else 
									currentSum += ps.what(loc.intValue(), type);

						}
							
							if (currentSum > result)
								return false;
						}
						
					}
			}
		
		
		return true;
	}

	
	@Override
	public void addConstraint(ProblemAPI translator, Context context, Var[] x)
	{
		int result = result().eval(context);
		if (region() != null) {
			final int[] sites = region.eval(context).sites();
			final Var[] vars = new Var[sites.length];
			for (int i = 0; i < sites.length; i++)
				vars[i] = x[sites[i]];
			translator.sum(vars, translator.LE, resultFn.eval(context));
		}
		else {
			final Regions[] regions = context.game().equipment().regions();
			for (final Regions region : regions) {
				if (region.name().equalsIgnoreCase(this.name)){
					final int[] sites = region.eval(context);
					final Var[] vars = new Var[sites.length];
					for (int i = 0; i < sites.length; i++)
						vars[i] = x[sites[i]];
					translator.sum(vars, translator.LE, resultFn.eval(context));
				}
			}
		}
		
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "At Most(" + region + ") = " + resultFn;
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
			game.addCrashToReport("The ludeme (at Most ...) is used but the number of players is not 1.");
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