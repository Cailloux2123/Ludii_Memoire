package game.functions.booleans.deductionPuzzle.is.regionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
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
 * Returns true if the array match with the hint.
 * 
 * @author Pierre.Accou
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
	
	/** if we have different color in the puzzle. */
	private final BooleanFunction colorFn;

	/** The name of the region to check.. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType  Type of graph elements to return [Default SiteType of the board].
	 * @param region       The region to sum [Regions].
	 * @param nameRegion   The name of the region to check.
	 * @param colorHint    True if we have different color in the puzzle
	 * @param result       
	 */
	public IsMatch
	(
		@Opt       final SiteType              elementType,
		@Opt       final RegionFunction        region,
		@Opt       final String                nameRegion,
		@Opt @Name final BooleanFunction	   colorHint,	
			       final IntFunction    	   result
	)
	{
		this.region = region;
		typeRegion = (region == null) ? RegionTypeStatic.Regions : null;

		if(region != null)
			regionConstraint = region;
		else
			areaConstraint = typeRegion;

		colorFn = (colorHint == null) ? new BooleanConstant(false) : colorHint;
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
		final Integer[][] colors;

		if (type.equals(SiteType.Cell)) {
			hints = context.game().equipment().cellHints();
			position = context.game().equipment().cellsWithHints();
			colors = context.game().equipment().cellColors();
		} else if (type.equals(SiteType.Edge)) {
			hints = context.game().equipment().edgeHints();
			position = context.game().equipment().edgesWithHints();
			colors = context.game().equipment().edgeColors();
		} else {
			hints = context.game().equipment().vertexHints();
			position = context.game().equipment().verticesWithHints();
			colors = context.game().equipment().vertexColors();
		}
		
		for (int i=0; i<position.length; i++) {
			
			Integer[] hint = hints[i];
			
			int[] cases = new int[position[i].length];
			List<Integer> toComplete = new ArrayList<>();
			
			int index = 0;
			for (int loc : position[i]) {
				if (ps.isResolved(loc, realType)) {
					cases[index] = ps.what(loc, realType);
				}
				else {
					toComplete.add(index);
				}
				index ++;
			}
			
			List<int[]> allCases = new ArrayList<>();
			
			if (colorFn.eval(context)) {
				
				List<Integer> possibleValue = new ArrayList<>();
				possibleValue.add(0);
				
				for (int c : colors[i]) {
					possibleValue.add(c);
				}
				
				generateCasesColor(allCases, cases, 0, toComplete, possibleValue);
				
				if (!isPossibleSolutionColor(allCases, hint, colors[i])) {
					return false;
				}
			} else {
				boolean moreThanOne;
				if (hint.length==1) {
					moreThanOne = false;
				}
				else {
					moreThanOne = true;
				}
				StringBuilder regexPatternBuilder = new StringBuilder();
				regexPatternBuilder.append("0*1{");
				for (int r=0; r<hint.length; r++) {
					regexPatternBuilder.append(hint[r]);
					if (r == hint.length-1) {
						regexPatternBuilder.append("}0*");
					} else {
						regexPatternBuilder.append("}0+1{");
					}
				}
				String regexPattern = regexPatternBuilder.toString();
				Pattern patternCompile = Pattern.compile(regexPattern);
				int start = 0;
				if (moreThanOne) {
					start=hint[0];
				}
				
				generateCases(allCases, cases, 0, toComplete, 0, start, moreThanOne);
				
				if (!isPossibleSolution(allCases, patternCompile)) {
					return false;
				}
			}
			
		}
		
		return true;
		
	}

	//-------------------------------------------------------------------------
	
	public void generateCases(List<int[]> allCases, int[] array, int idx, List<Integer> unknown, int cnt, int start, boolean moreThanOne){

		if (array.length == idx) {
			allCases.add(array.clone());
			return;
		}
		 
		if (moreThanOne) {
			if (cnt>start) {
				return;
			}
		}
		
		if (unknown.contains(idx)){
			array[idx] = 1;
			generateCases(allCases, array, idx+1, unknown, cnt+1, start, moreThanOne);
			array[idx] = 0;
			if (cnt!=0)
				generateCases(allCases, array, idx+1, unknown, 0, start, false);
			else
				generateCases(allCases, array, idx+1, unknown, 0, start, moreThanOne);
		}
		else {
			generateCases(allCases, array, idx+1, unknown, 0, start, moreThanOne);
		}
	}
	
	public boolean isPossibleSolution(List<int[]> allCases, Pattern patternCompile){
		
		for (int[] cases : allCases) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int j : cases) {
				stringBuilder.append(j);
			}
			Matcher matcher = patternCompile.matcher(stringBuilder.toString());
			if (matcher.matches()) {
				return true;
			}
		}
        return false;
    }
	
	public void generateCasesColor(List<int[]> allCases, int[] array, int idx, List<Integer> unknown, List<Integer> possibleValue){

		if (array.length == idx) {
			if (!allCases.contains(array)) {
				allCases.add(array.clone());
			}
			return;
		} 
		if (unknown.contains(idx)){
			for (int i:possibleValue) {
				array[idx] = i;
				generateCasesColor(allCases, array, idx+1, unknown, possibleValue);
			}
		}
		else {
			generateCasesColor(allCases, array, idx+1, unknown, possibleValue);
		}
	}
	
	public boolean isPossibleSolutionColor(List<int[]> allCases, Integer[] hints, Integer[] color){
		
		StringBuilder regexPatternBuilder = new StringBuilder();
		regexPatternBuilder.append("^0*");
		for (int i=0; i<hints.length; i++) {
			regexPatternBuilder.append(color[i]);
			regexPatternBuilder.append("{");
			regexPatternBuilder.append(hints[i]);
			if (i == hints.length-1) {
				regexPatternBuilder.append("}0*$");
			} else {
				regexPatternBuilder.append("}0*");
			}
		}
		String regexPattern = regexPatternBuilder.toString();
		//System.out.println("Pattern : " + regexPattern);
		Pattern patternCompile = Pattern.compile(regexPattern);
		
		for (int[] cases : allCases) {
			if (regexPattern.equals("^0*7{3}0+2{1}0+7{1}0*$")) {
				System.out.println(Arrays.toString(cases));
				System.out.println("Pattern : " + regexPattern);
			}
			//System.out.println(Arrays.toString(cases));
			StringBuilder stringBuilder = new StringBuilder();
			for (int j : cases) {
				stringBuilder.append(j);
			}
			Matcher matcher = patternCompile.matcher(stringBuilder.toString());
			if (matcher.matches()) {
				return true;
			}
		}
		//System.out.println("hint : "+hints+" color : "+color);
        return false;
    }

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
			game.addCrashToReport("The ludeme (is Match ...) is used but the number of players is not 1.");
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
