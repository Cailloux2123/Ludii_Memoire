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
 * Returns true if each sub region of a static region is different.
 * 
 * @author Eric.Piette
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsRightDirections extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param elementType The graph element type [Cell].
	 */
	public IsRightDirections
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
		
		Integer[][] vertexHints = context.equipment().vertexHints();
		Integer[][] verticesWithHints = context.equipment().verticesWithHints();
		
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
				if (vertexHints[i][0] == 0) {
					Edge edge1 = toAnalyse.get(0);
					Edge edge2 = toAnalyse.get(1);
					if (edge1.vA().row() == edge1.vB().row()) {
						if (edge1.vA().row() != edge2.vA().row() || edge1.vA().row() != edge2.vB().row())
							return false;
					}
					else if (edge1.vA().col() == edge1.vB().col()) {
						if (edge1.vA().col() != edge2.vA().col() || edge1.vA().col() != edge2.vB().col())
							return false;
					}
				} 
				else if (vertexHints[i][0] == 1) {
					Edge edge1 = toAnalyse.get(0);
					Edge edge2 = toAnalyse.get(1);
					if (edge1.vA().row() == edge1.vB().row()) {
						if (edge1.vA().row() == edge2.vA().row() && edge1.vA().row() == edge2.vB().row())
							return false;
					}
					else if (edge1.vA().col() == edge1.vB().col()) {
						if (edge1.vA().col() == edge2.vA().col() && edge1.vA().col() == edge2.vB().col())
							return false;
					}
				}
			}
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
			game.addCrashToReport("The ludeme (is Unique ...) is used but the number of players is not 1.");
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
		return "each sub-region of the board is different";
	}
	
	//-------------------------------------------------------------------------
}
