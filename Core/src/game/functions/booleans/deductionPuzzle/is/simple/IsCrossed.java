package game.functions.booleans.deductionPuzzle.is.simple;


import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
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

import other.topology.*;

@Hide
public class IsCrossed extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------

	/**
	 * @param elementType Type of graph elements to return [Default SiteType of the board].
	 * @param region      The region to sum [Regions].
	 * @param nameRegion   The name of the region to check.
	 * @param result      The result to check.
	 */
	public IsCrossed()
	{

	}  
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final List<Edge> edges = context.topology().edges();
		final ContainerState ps = context.state().containerStates()[0];

		for (Edge edge : edges) {
			if (ps.isResolvedEdges(edge.index()) && ps.whatEdge(edge.index()) != 0) {
				//System.out.println("Now comparing Edge "+ edge);
				//System.out.println(ps.whatEdge(edge.index()));
				for (Edge compareEdge: edges) {
					if (ps.isResolvedEdges(compareEdge.index()) && ps.whatEdge(compareEdge.index()) != 0) {
						if (!edge.equals(compareEdge)){
							//System.out.println("Edge: " + edge);
							//System.out.println("compareEdge: " + compareEdge);
							//Do both edges intersects?
							//Horizontal
							if(edge.vA().row()== edge.vB().row()) {
								if(compareEdge.vA().col() == compareEdge.vB().col() ) {

									if(( Math.max(compareEdge.vA().row(), compareEdge.vB().row()) > edge.vA().row() && Math.min(compareEdge.vA().row(), compareEdge.vB().row()) < edge.vB().row()) 
											&& (Math.min(edge.vA().col(), edge.vB().col()) < compareEdge.vA().col() && Math.max(edge.vA().col(), edge.vB().col()) > compareEdge.vB().col())  ) {
										//System.out.println("Ligne 66");

										return true;
									}
								}
							}

						}
					}

				}
			}
			
		}
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";

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

	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.DeductionPuzzle;


		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);


		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
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

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

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

		return willCrash;
	}


	@Override
	public String toEnglish(final Game game) 
	{
		String regionName = "the board";
		
		return "the sum of "+  " in " + regionName + " is equal to ";
	}
	
	//-------------------------------------------------------------------------
}
