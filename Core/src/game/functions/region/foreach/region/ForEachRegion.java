package game.functions.region.foreach.region;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BooleanFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the sites satisfying a constraint from a given region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachRegion extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Condition to check. */
	private final BooleanFunction condition;

	//-------------------------------------------------------------------------

	/**
	 * @param region The original region.
	 * @param If     The condition to satisfy.
	 * @example (forEach (sites Occupied by:P1) if:(= (what at:(site)) (id
	 *          "Pawn1")))
	 */
	public ForEachRegion
	(
		@Name final BooleanFunction If
	)
	{
		condition = If;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final Regions[] regions = context.game().equipment().regions();
		final Region resetRegion = context.region();
		final TIntArrayList returnSites = new TIntArrayList();
		
		for (int i = 0; i < regions.length; i++)
		{
			final Regions region = regions[i];
			RegionFunction[] regionFct = region.region();
			for (RegionFunction fct : regionFct) {
				Region eval = fct.eval(context);
				context.setRegion(eval);
				if (condition.eval(context)) {
					int[] sites = eval.sites();
					for (int site : sites) {
						returnSites.add(site);
					}
				}
			}
		}

		context.setRegion(resetRegion);
		return new Region(returnSites.toArray());
	}

	@Override
	public boolean isStatic()
	{
		return condition.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return condition.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(condition.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(condition.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= condition.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return condition.toEnglish(game);
	}
}
