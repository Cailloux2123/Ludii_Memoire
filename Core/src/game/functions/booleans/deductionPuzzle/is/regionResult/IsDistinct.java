package game.functions.booleans.deductionPuzzle.is.regionResult;

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
	
	private final IntFunction resultFn;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return.
	 * @param region      The region to check [Regions].
	 * @param except      The exception on the test.
	 * @param excepts     The exceptions on the test.
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
		return "Every item within a region is different";
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
