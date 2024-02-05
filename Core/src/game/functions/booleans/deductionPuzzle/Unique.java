package game.functions.booleans.deductionPuzzle;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
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
public class Unique extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;

	/** Which type of area. */
	private final RegionTypeStatic typeRegion;
	
	/** Values to ignore. */
	private final IntFunction[] exceptions;

	/** Graph element type. */
	private final SiteType type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return.
	 * @param region      The region to check [Regions].
	 * @param except      The exception on the test.
	 * @param excepts     The exceptions on the test.
	 */
	public Unique
	(
		@Opt           final SiteType       elementType,
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

		type = elementType;
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
		
		Long longSize = Long.valueOf(context.containers()[0].numSites());
		int sizeBoard = (int) Math.sqrt(longSize);    //Need size of the board.
		int[][] referenceMatrix = generateMatrix(sizeBoard);
		
		for (final IntFunction exception : exceptions)
			excepts.add(exception.eval(context));

		if (typeRegion == null)
		{
			final TIntArrayList history = new TIntArrayList();
			final int[] sites = region.eval(context).sites();
			if (sites.length == 0)
				return true;
			/*
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
			*/
			final int what = cs.what(sites[0], realType);
			if (what == 0 && !excepts.contains(what))
				return false;
			if (!excepts.contains(what))
			{
				if(history.contains(what))
					return false;
				history.add(what);
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
				            /*
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
							*/
							int[] locsArray = convertToIntArray(locs);
							int loc = FindCentralCell(referenceMatrix, locsArray);
							
							if (!cs.isResolved(loc, realType))              //je dois récupérer le bon loc car anciennement loc.intValue
								continue;
							final int what = cs.what(loc, realType);        //je dois récupérer le bon loc car anciennement loc.intValue
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
				else if (rgn.region() != null)
				{
					final RegionFunction[] regionsFunctions = rgn.region();
					for (final RegionFunction regionFunction : regionsFunctions)
					{
						final int[] locs = regionFunction.eval(context).sites();
						final TIntArrayList history = new TIntArrayList();
						/*
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
						*/
						
						int loc = FindCentralCell(referenceMatrix, locs);
						
						final int what = cs.what(loc, realType);   //Est-ce que les regions sont créés par ordre ou ? Donc est-ce que locs[0] = centre si non comment la récupérer
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
				else if (rgn.sites() != null)
				{
					final TIntArrayList history = new TIntArrayList();
					/*
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
					*/
					int[] sites = rgn.sites();
					
					int loc = FindCentralCell(referenceMatrix, sites);
					
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
		return true;
	}
	
	public int[][] generateMatrix(int size) {
        int[][] matrix = new int[size][size];
        int value = 1;

        for (int i = size - 1; i >= 0; i--) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = value++;
            }
        }

        return matrix;
    }
	
	public int FindCentralCell(int[][] matrix, int[] region) {
        int[] rows = new int[region.length];
        int[] columns = new int[region.length];

        for (int k = 0; k < region.length; k++) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == region[k]) {
                        rows[k] = i;
                        columns[k] = j;
                        break;
                    }
                }
            }
        }

        Arrays.sort(rows);
        Arrays.sort(columns);

        int ligneCentrale = rows[region.length / 2];
        int colonneCentrale = columns[region.length / 2];

        return matrix[ligneCentrale][colonneCentrale];
    }
	
	public int[] convertToIntArray(Integer[] IntTab) {
        int[] intArray = new int[IntTab.length];

        for (int i = 0; i < IntTab.length; i++) {
        	intArray[i] = IntTab[i];
        }

        return intArray;
    }

	//-------------------------------------------------------------------------

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
