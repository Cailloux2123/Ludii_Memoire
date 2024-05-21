package game.functions.booleans.deductionPuzzle.is.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;

/**
 * Returns true if each hint are visited by edges.
 * 
 * @author Pierre.Accou
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsAllHintVisited extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param elementType The graph element type [Cell].
	 */
	public IsAllHintVisited
	(
		@Opt final SiteType elementType
	)
	{
		areaConstraint = RegionTypeStatic.Regions;
	}

	//--------------------------------------------------------------------------

	@Override
	public boolean eval(Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		final List<Edge> edges = context.topology().edges();
		
		Integer[][] verticesWithHints = context.equipment().verticesWithHints();
		
		int cnt = 0;
		
		for (int i=0; i<verticesWithHints.length; i++) {
			int vertex = verticesWithHints[i][0];
			List<Edge> toAnalyse = new ArrayList<>();
			for (Edge e : edges) {
				if (ps.isResolvedEdges(e.index())) {
					if (e.vA().index() == vertex || e.vB().index() == vertex) {
						toAnalyse.add(e);
					}
				}
			}
			
			if (toAnalyse.size() == 2) {
				cnt ++;
			}
		}
		
		if (cnt != verticesWithHints.length) {
			return false;
		}
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// Do nothing.
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.DeductionPuzzle;
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
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (is AllHintVisited ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "Unique()";
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "each hint are visited by edges";
	}
	
	//-------------------------------------------------------------------------
}
