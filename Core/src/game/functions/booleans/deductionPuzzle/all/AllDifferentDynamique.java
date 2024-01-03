package game.functions.booleans.deductionPuzzle.all;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeDynamic;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Returns true if every item is different in the specific region.
 * 
 * @author Eric.Piette
 * 
 * @remarks This is used for the constraints of a deduction puzzle. This works
 *          only for deduction puzzles.
 */
@Hide
public class AllDifferentDynamique extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;  // Ici la region que l'on crée alors ?

	/** Which type of area. */
	private final RegionTypeStatic typeRegion;
	
	/** Values to ignore. */
	private final IntFunction[] exceptions;

	/** Graph element type. */
	private final SiteType type;
	
	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	/** Region to check. */
	private final RegionFunction where;

	/** Loc to check. */
	private final IntFunction locWhere;

	/** Type of sites around (empty, enemy, all, ...). */
	private final RegionTypeDynamic typeDynamic;

	/** Distance around. */
	private final IntFunction distance;

	/** Choice of directions. */
	private final AbsoluteDirection directions;

	/** Condition to check by each site to be on the return region. */
	private final BooleanFunction cond;

	/** Origin included or not. */
	private final BooleanFunction originIncluded;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return.
	 * @param region      The region to check [Regions].
	 * @param except      The exception on the test.
	 * @param excepts     The exceptions on the test.
	 */
	public AllDifferentDynamique
	(
		@Opt           final SiteType       typeLoc,
			@Or        final IntFunction       where,
			@Or  	   final RegionFunction    regionWhere,
		@Opt 	       final RegionTypeDynamic type,
	    @Opt     @Name final IntFunction       distance,
	    @Opt 	       final AbsoluteDirection directions,
	    @Opt 	 @Name final BooleanFunction   If,
	    @Opt     @Name final BooleanFunction   includeSelf,
		@Opt	       final RegionFunction region,
		@Opt @Name @Or final IntFunction    except,
		@Opt @Name @Or final IntFunction[]  excepts
	)
	{
		this.region = region;
		typeRegion = (region == null) ? RegionTypeStatic.Regions : null;
		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = typeRegion;
		
		if (excepts != null)
			exceptions = excepts;
		else if (except != null)
		{
			exceptions = new IntFunction[1];
			exceptions[0] = except;
		}
		else
			exceptions = new IntFunction[0];

		this.where = regionWhere;
		locWhere = where;
		typeDynamic = type;
		this.distance = (distance == null) ? new IntConstant(1) : distance;
		this.directions = (directions == null) ? AbsoluteDirection.Adjacent : directions;
		cond = (If == null) ? null : If;
		originIncluded = (includeSelf == null) ? new BooleanConstant(false) : includeSelf;
		this.type = typeLoc;
	}
	
	//---------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "Every item within a region is different";
	}
	
	//---------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final ContainerState cs = context.state().containerStates()[0];
		final TIntArrayList excepts = new TIntArrayList();
		
		if (precomputedRegion != null)
			return false; //return precomputedRegion;

		// sites Around
		final TIntArrayList sitesAround = new TIntArrayList();

		// distance
		final int dist = cs.what(loc, realType); //Ici il faut trouver quoi mettre comme container state

		// Get the list of index of the regionTo
		final TIntArrayList typeRegionTo = convertRegion(context, typeDynamic);

		final int origFrom = context.from();
		final int origTo = context.to();

		// sites to check
		final int[] sites;
		if (where != null)
		{
			sites = where.eval(context).sites();
		}
		else
		{
			sites = new int[1];
			sites[0] = locWhere.eval(context);
		}
		
		final other.topology.Topology graph = context.topology();

		final ContainerState state = context.state().containerStates()[0];
		
		// if region empty & site not specify
		if (sites.length == 0)
			return false; //return new Region(sitesAround.toArray());

		if (sites[0] == Constants.UNDEFINED)
			return false; //return new Region();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		
		for (final int site : sites)
		{
			if (site >= graph.getGraphElements(realType).size())
				continue;
			
			context.setFrom(site);

			final List<Radial> radials = graph.trajectories().radials(realType, site, directions); //Ici pour directions si on met All on prends les diaonales sinon on fait juste N S E W
			for (final Radial radial : radials)
			{
				if (dist >= radial.steps().length)
					continue;
				
				for (int toIdx = 0; toIdx < radial.steps().length; toIdx++)
				{
					final int to = radial.steps()[dist].id();
					context.setTo(to);
					if ((cond == null || cond.eval(context))
							&& (typeDynamic == null || typeRegionTo.contains(state.what(to, realType))))
					{
						sitesAround.add(to);
					}
				}
			}
		}

		context.setFrom(origFrom);
		context.setTo(origTo);

		if (originIncluded.eval(context))
		{
			for(int site : sites)
				if (!sitesAround.contains(site))
					sitesAround.add(site);
		}
		else
		{
			for(int site : sites)
				while (sitesAround.contains(site))
					sitesAround.remove(site);
		}
		
		Region region = new Region(sitesAround.toArray());
		
		//Need to change the allDiff with the new 
		
		
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final ContainerState cs = context.state().containerStates()[0];
		final TIntArrayList excepts = new TIntArrayList();
		
		for (final IntFunction exception : exceptions)
			excepts.add(exception.eval(context));

		if (typeRegion == null)
		{
			final TIntArrayList history = new TIntArrayList();
			final int[] sites = region.eval(context).sites();
			if (sites.length == 0)
				return true;
			for (final int site : sites)
			{
				if (!cs.isResolved(site, realType))
					continue;
				final int what = cs.what(site, realType);
				if (what == 0 && !excepts.contains(what))
					return false;
				if (!excepts.contains(what))
				{
					if(history.contains(what))
						return false;
					history.add(what);
				}
			}
		}
		else if (typeRegion.equals(RegionTypeStatic.Regions))
		{
			final Regions[] regions = context.game().equipment().regions();

			for (final Regions rgn : regions)
			{
				if (rgn.regionTypes() != null)
				{
					final RegionTypeStatic[] areas = rgn.regionTypes();
					for (final RegionTypeStatic area : areas)
					{
						final Integer[][] regionsList = rgn.convertStaticRegionOnLocs(area, context);
						for (final Integer[] locs : regionsList)
						{
							final TIntArrayList history = new TIntArrayList();
							if (area.equals(RegionTypeStatic.AllDirections))
								if (cs.what(locs[0].intValue(), realType) == 0)
									continue;
				
							for (final Integer loc : locs)
							{
								if (loc != null)
								{
									if (!cs.isResolved(loc.intValue(), realType))
										continue;
									final int what = cs.what(loc.intValue(), realType);
									if (what == 0 && !excepts.contains(what))
										return false;
									
									if (!excepts.contains(what))
									{
										if (history.contains(what))
											return false;
										history.add(what);
									}
								}
							}
						}
					}
				}
				else if (rgn.region() != null)
				{
					final RegionFunction[] regionsFunctions = rgn.region();
					for (final RegionFunction regionFunction : regionsFunctions)
					{
						final int[] locs = regionFunction.eval(context).sites();
						final TIntArrayList history = new TIntArrayList();
						for (final int loc : locs)
						{
							if (!cs.isResolved(loc, realType))
								continue;
							final int what = cs.what(loc, realType);
							if (what == 0 && !excepts.contains(what))
								return false;
								if (!excepts.contains(what))
								{
									if (history.contains(what))
										return false;
									history.add(what);
								}
						}
					}
				}
				else if (rgn.sites() != null)
				{
					final TIntArrayList history = new TIntArrayList();
					for (final int loc : rgn.sites())
					{
						if (!cs.isResolved(loc, realType))
							continue;
						final int what = cs.what(loc, realType);
						if (what == 0 && !excepts.contains(what))
							return false;
						if (!excepts.contains(what))
						{
							if (history.contains(what))
								return false;
							history.add(what);
						}
					}
				}
				}
			}
		return true;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context       The context.
	 * @param dynamicRegion The dynamic region.
	 * @return A list of the index of the player for each kind of region.
	 */
	public static TIntArrayList convertRegion(final Context context, final RegionTypeDynamic dynamicRegion)
	{
		final int indexSharedPlayer = context.game().players().size();
		final TIntArrayList whatIndex = new TIntArrayList();
		final State state = context.state();

		if (dynamicRegion == RegionTypeDynamic.Empty)
		{
			whatIndex.add(0);
		}
		else if (dynamicRegion == RegionTypeDynamic.NotEmpty)
		{
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
				whatIndex.add(components[i].index());
		}
		else if (dynamicRegion == RegionTypeDynamic.Own)
		{
			final int moverId = state.mover();
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
			{
				final Component component = components[i];
				if (component.owner() == moverId || component.owner() == indexSharedPlayer)
					whatIndex.add(component.index());
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.Enemy)
		{
			final Component[] components = context.equipment().components();
			if (context.game().requiresTeams())
			{
				final TIntArrayList enemies = new TIntArrayList();
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
						enemies.add(pid);

				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (enemies.contains(component.owner()))
						whatIndex.add(component.index());
				}
			}
			else
			{
				final int moverId = state.mover();
				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (component.owner() != moverId && component.owner() > 0 && component.owner() < indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.NotEnemy)
		{
			final Component[] components = context.equipment().components();
			if (context.game().requiresTeams())
			{
				final TIntArrayList enemies = new TIntArrayList();
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
						enemies.add(pid);

				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (enemies.contains(component.owner()) || component.owner() == indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
			else
			{
				final int moverId = state.mover();
				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (component.owner() == moverId || component.owner() == indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.NotOwn)
		{
			final int moverId = state.mover();
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
			{
				final Component component = components[i];
				if (component.owner() != moverId && component.owner() != indexSharedPlayer)
					whatIndex.add(component.index());
			}
		}

		return whatIndex;
	}
	
	@Override
	public String toString()
	{
		String str = "";
		if(region != null)
			str += "AllDifferent(" + region + ")";
		else
			str += "AllDifferent(" + typeRegion.toString() + ")";
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

		for (final IntFunction fn : exceptions)
			gameFlags |= fn.gameFlags(game);

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

		for (final IntFunction fn : exceptions)
			concepts.or(fn.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		for (final IntFunction fn : exceptions)
			writeEvalContext.or(fn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		for (final IntFunction fn : exceptions)
			readEvalContext.or(fn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (region != null)
			region.preprocess(game);
		
		for(final IntFunction fn : exceptions)
			fn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (region != null)
			missingRequirement |= region.missingRequirement(game);

		for (final IntFunction fn : exceptions)
			missingRequirement |= fn.missingRequirement(game);

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

		for (final IntFunction fn : exceptions)
			willCrash |= fn.willCrash(game);

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
		return exceptions;
	}
}
