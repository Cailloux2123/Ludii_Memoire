package game.functions.booleans.deductionPuzzle.is.regionResult;

import java.util.BitSet;
import java.util.LinkedList;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

import other.topology.*;

/**
 * Returns true if the graph containing only unresolved cells is connex.
 * 
 * @author Tom.Doumont & Pierre.Accou
 * 
 * @remarks This works only for deduction puzzles.
 */
@Hide
public class IsConnex extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	
	/** Which result. */
	private final IntFunction elementFn;
	
	/** Which number complete the board. */
	private final IntFunction whatFn;

	/** Which type. */
	private final SiteType type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type    The graph element of the region [Default SiteType of the board].
	 * @param element Not used in current version
	 */
	public IsConnex
	(
		@Opt final SiteType       type,
		@Opt final IntFunction    what,
			 final IntFunction    element
	)
	{

		elementFn = element;
		whatFn = (what == null) ? new IntConstant(1) : what;
		this.type = type;
		
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(Context context)
	{
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;

		int numberConnexComponents = 0;
		final ContainerState cs = context.state().containerStates()[0];
		BitSet marked = new BitSet(context.containers()[0].numSites());
		LinkedList<Integer> pending = new LinkedList<>();
		
		for (int cell =0; cell < context.containers()[0].numSites() ; cell++) {
			if (((!cs.isResolved(cell, realType)) && !marked.get(cell)) || ((cs.what(cell, realType) != whatFn.eval(context)) && !marked.get(cell))) {
				numberConnexComponents += 1;
				marked.set(cell);
				pending.add(cell);
				while (!pending.isEmpty()) {
					int currentNode = pending.poll();

					for (Cell neighbor : context.topology().cells().get(currentNode).orthogonal()) {
						if ((!cs.isResolved(neighbor.index(), realType) && !marked.get(neighbor.index())) || ((cs.what(neighbor.index(), realType) != whatFn.eval(context)) && !marked.get(neighbor.index())))  {
							marked.set(neighbor.index());
							pending.add(neighbor.index());
						}
					}
				}
			}
		}
		
		//Should it be != 1?
		if (numberConnexComponents > 1) {
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
		elementFn.preprocess(game);
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.DeductionPuzzle;
		gameFlags |= elementFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.DeductionPuzzle.id(), true);
		concepts.or(elementFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(elementFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(elementFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= elementFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		return false;
	}

	//-------------------------------------------------------------------------


	/**
	 * @return The result to check.
	 */
	public IntFunction result() 
	{
		return elementFn;
	}
	
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the number of " + " in "  + " equals ";
	}
	
	//-------------------------------------------------------------------------
}
