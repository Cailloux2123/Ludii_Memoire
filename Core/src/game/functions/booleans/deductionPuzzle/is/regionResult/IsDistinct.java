package game.functions.booleans.deductionPuzzle.is.regionResult;

import java.util.BitSet;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns true if the central value in the region appears only once.
 * 
 * @author Pierre Accou & Tom Doumont
 * 
 * @remarks This is used for the constraints of a deduction puzzle. This works
 *          only for deduction puzzles.
 */
@Hide
public class IsDistinct extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;

	/** Which type of area. */
	private final RegionTypeStatic typeRegion;

	/** Graph element type. */
	private final SiteType type;
	
	/** Integer to be unique. */
	private final IntFunction resultFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return.
	 * @param region      The region to check [Regions].
	 * @param result      The integer to be unique in this region. It can appear only once.
	 */
	public IsDistinct
	(
		@Opt           final SiteType       elementType,
		@Opt	       final RegionFunction region,
					   final IntFunction	result
	)
	{
		this.region = region;
		typeRegion = (region == null) ? RegionTypeStatic.Regions : null;
		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = typeRegion;
		
		type = elementType;
		resultFn = result;
	}
	
	//---------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "Every item within a region is different than the central integer";
	}
	
	//---------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final ContainerState cs = context.state().containerStates()[0];
		final int result = resultFn.eval(context);
		final int[] sites = region.eval(context).sites();
		
		
		if (region == null) 
			return false;
		for (final int site: sites) {
			if (cs.isResolved(site, realType) && cs.what(site, realType)!= 0) {
				final int what = cs.what(site, realType);
				if (what == result) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void addConstraint(ProblemAPI translator, Context context, Var[] x)
	{
		for (int place : region.eval(context).sites()) {
			if (resultFn.eval(context) != 0) {
				translator.different(x[place], resultFn.eval(context));
			}
			else {
				translator.different(x[place], x[context.from()]);
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		if(region != null)
			str += "is Distinct(" + region + ")";
		return str;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}
	

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.DeductionPuzzle;

		if (region != null)
			gameFlags |= region.gameFlags(game);


		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.DeductionPuzzle.id(), true);

		if (region != null)
			concepts.or(region.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (region != null)
			region.preprocess(game);

	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

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
			game.addCrashToReport("The ludeme (all Different ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);

		if (region != null)
			willCrash |= region.willCrash(game);


		return willCrash;
	}

	//--------------------------------------------------------------------
	
	/**
	 * @return The region to check.
	 */
	public RegionFunction region() 
	{
		return region;
	}
	
	/**
	 * @return The static region.
	 */
	public RegionTypeStatic area() 
	{
		return typeRegion;
	}

	/**
	 * The exceptions of the test.
	 * 
	 * @return The indices of all the exceptions sites.
	 */
	public IntFunction[] exceptions()
	{
		return null;
	}
	
	/**
	 * @return The result to check.
	 */
	public IntFunction result() 
	{
		return resultFn;
	}
}
