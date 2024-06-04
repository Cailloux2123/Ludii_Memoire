package game.functions.booleans.deductionPuzzle.is.regionResult;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import csp.Solvers.Translator;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;

/**
 * Returns true if the sum of a region is equal to the result.
 * 
 * @author Lianne.Hufkens and Eric.Piette
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsSum extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;
	
	/** Which result. */
	private final IntFunction resultFn;

	/** Graph element type. */
	private final SiteType type;

	/** The name of the region to check.. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return [Default SiteType of the board].
	 * @param region      The region to sum [Regions].
	 * @param nameRegion   The name of the region to check.
	 * @param result      The result to check.
	 */
	public IsSum
	(
		@Opt final SiteType       elementType,
		@Opt final RegionFunction region,
		@Opt final String         nameRegion,
			 final IntFunction    result
	)
	{
		this.region = region;
		resultFn = result;

		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = RegionTypeStatic.Regions;

		type = (elementType == null) ? SiteType.Cell : elementType;
		name = (nameRegion == null) ? "" : nameRegion;
	}  
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		
		if (region != null)
		{
			final int result = resultFn.eval(context);
			final int[] sites = region.eval(context).sites();
			boolean allAssigned = true;
			int currentSum = 0;
		
			for (final int site : sites)
				if (ps.isResolved(site, type))
					currentSum += ps.what(site, type);
				else
					allAssigned = false;
		
			if ((allAssigned && currentSum != result) || (currentSum > result)) {
				return false;
			}
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
				if (reg.name().contains(name)) {
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
								boolean allAssigned = true;
								int currentSum = 0;
								for (final Integer loc : locs)
								{
									if (ps.isResolved(loc.intValue(), type))
										currentSum += ps.what(loc.intValue(), type);
									else
										allAssigned = false;
								}
									
								if ((allAssigned && currentSum != result) || (currentSum > result))
									return false;
								indexRegion++;
							}
						}
					}
					else {
						boolean allAssigned = true;
						int currentSum = 0;

						for (final Integer loc : reg.sites())
						{
							if (ps.isResolved(loc.intValue(), type))
								currentSum += ps.what(loc.intValue(), type);
							else
								allAssigned = false;
						}
							
							if ((allAssigned && currentSum != result) || (currentSum > result))
								return false;
					}
						
					}
				}
			}
		return true;

	}
	
	@Override
	public void addConstraint(Translator translator, Context context, Var[] x)
	{
		//TODO improve readibility of this code
		int result = result().eval(context);
		if (region() != null) {
			final int[] sites = region.eval(context).sites();
			final Var[] vars = new Var[sites.length];
			for (int i = 0; i < sites.length; i++)
				vars[i] = x[sites[i]];
			translator.sum(vars, translator.EQ , result);
		} else {
			final Regions[] regions = context.game().equipment().regions();
			for (final Regions region : regions) {
				System.out.println("Name: " + name);
				if( name!= null && name.equalsIgnoreCase(region.name())) {
					System.out.println("Ceci devrait nous en empecher");
					final Var[] vars = new Var[region.sites().length];
					for (int i = 0; i < region.sites().length; i++) {
						vars[i] = x[region.sites()[i]];
					}
					translator.sum(vars, translator.EQ, result);
				}
				else if (region.regionTypes() != null && name.equals("")) {
						final RegionTypeStatic[] areas = region.regionTypes();
						for (final RegionTypeStatic area : areas) {
							final Integer[][] regionsList = region.convertStaticRegionOnLocs(area, context);
							for (final Integer[] locs : regionsList) {
								if (result().isHint()) {
									System.out.println("Not implemented yet");
								}
								final Var[] vars = new Var[locs.length];
								for (int i = 0; i < locs.length; i++) {
									vars[i] = x[locs[i].intValue()];
								}
								translator.sum(vars, translator.EQ, result);
							}
						}
					}
					else if (name == null) {
						System.out.println("Magic square");
						final Var[] vars = new Var[region.sites().length];
						for (int i = 0; i < region.sites().length; i++) {
							vars[i] = x[region.sites()[i]];
						}
						translator.sum(vars, translator.EQ, result);
					}

			}
		}
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
	
	
	/**
	 * @return the Name of the region
	 */
	public String nameRegion() {
		return name;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String regionName = "the board";
		if (name.length() > 0)
			regionName = name;
		else if (region != null)
			regionName = region.toEnglish(game);
		
		return "the sum of " + type.name().toLowerCase() + " in " + regionName + " is equal to " + resultFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
