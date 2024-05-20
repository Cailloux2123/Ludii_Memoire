package game.functions.booleans.deductionPuzzle.is.simple;


import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;

import other.topology.*;

/**
 * Returns true if 2 edges intersect in a graph
 * 
 * @author Tom.Doumont & Pierre.Accou
 * 
 */
@Hide
public class IsCrossed extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

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
				for (Edge compareEdge: edges) {
					if (ps.isResolvedEdges(compareEdge.index()) && ps.whatEdge(compareEdge.index()) != 0) {
						if (!edge.equals(compareEdge)){
							if(edge.vA().row()== edge.vB().row()) {
								if(compareEdge.vA().col() == compareEdge.vB().col() ) {

									if(( Math.max(compareEdge.vA().row(), compareEdge.vB().row()) > edge.vA().row() && Math.min(compareEdge.vA().row(), compareEdge.vB().row()) < edge.vB().row()) 
											&& (Math.min(edge.vA().col(), edge.vB().col()) < compareEdge.vA().col() && Math.max(edge.vA().col(), edge.vB().col()) > compareEdge.vB().col())  ) {
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
	
	//-------------------------------------------------------------------------
}
