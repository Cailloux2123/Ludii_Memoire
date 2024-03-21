package game.functions.booleans.deductionPuzzle.is.regionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

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
import other.context.EvalContextData;
import other.state.container.ContainerState;

/**
 * Returns true if the sum of a region is equal to the result.
 * 
 * @author Lianne.Hufkens and Eric.Piette
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsMatch extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final RegionFunction region;
	
	/** Which type of area. */
	private final RegionTypeStatic typeRegion;

	/** Graph element type. */
	private final SiteType type;

	/** The name of the region to check.. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return [Default SiteType of the board].
	 * @param region      The region to sum [Regions].
	 * @param nameRegion   The name of the region to check.
	 */
	public IsMatch
	(
		@Opt final SiteType            elementType,
		@Opt final RegionFunction      region,
		@Opt final String              nameRegion,
			 final IntFunction    	   result
	)
	{
		this.region = region;
		typeRegion = (region == null) ? RegionTypeStatic.Regions : null;

		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = typeRegion;

		type = (elementType == null) ? SiteType.Cell : elementType;
		name = (nameRegion == null) ? "" : nameRegion;
	}  
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		
		final Integer[][] hints;
		final Integer[][] position;
		
		

		if (type.equals(SiteType.Cell)) {
			hints = context.game().equipment().cellHints();
			position = context.game().equipment().cellsWithHints();
		} else if (type.equals(SiteType.Edge)) {
			hints = context.game().equipment().edgeHints();
			position = context.game().equipment().edgesWithHints();
		} else {
			hints = context.game().equipment().vertexHints();
			position = context.game().equipment().verticesWithHints();
		}
		
		//System.out.println("Hints : "+ Arrays.deepToString(hints));
		//System.out.println("Position : "+Arrays.deepToString(position));
		
		for (int i=0; i<position.length; i++) {
			
			System.out.println("Region colonne 2 : " + Arrays.deepToString(position[6]));
			System.out.println("Hint colonne 2 : " + Arrays.deepToString(hints[6]));
			
			
			//recup le pattern avec les hints associé
			Integer[] hint = hints[i];
			
			//recup de ma region + voir si la case est coloriée ou non
			List<Integer> region = new ArrayList<>();
			
			for (int loc : position[i]) {
				if (ps.isResolved(loc, realType)) {
					region.add(1);
				}
				else {
					region.add(0);
				}
			}
			
			List<Integer> groupe = new ArrayList<>();
			int count = 0;
			
			for (int r : region) {
				if (r == 1) {
					count ++;
				}
				if (r == 0 && count != 0) {
					groupe.add(count);
					count = 0;
				}
			}
			if (count != 0) {
				groupe.add(count);
			}
			
			if (groupe.size() > hint.length) {
				return false;
			}
			
			for (int idx=0; idx < groupe.size(); idx++) {
				if (groupe.get(idx) > hint[idx]) {
					return false;
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
		str += "Sum(" + region + ") = ";
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
		if (region != null)
			region.preprocess(game);
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
		concepts.set(Concept.DeductionPuzzle.id(), true);


		if (region != null)
			concepts.or(region.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();

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
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
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
			game.addCrashToReport("The ludeme (is Sum ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);

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


	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String regionName = "the board";
		if (name.length() > 0)
			regionName = name;
		else if (region != null)
			regionName = region.toEnglish(game);
		
		return "the sum of " + type.name().toLowerCase() + " in " + regionName + " is equal to ";
	}
	
	//-------------------------------------------------------------------------
}
