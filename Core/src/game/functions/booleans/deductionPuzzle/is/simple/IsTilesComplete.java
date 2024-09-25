package game.functions.booleans.deductionPuzzle.is.simple;

import java.util.BitSet;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;

import annotations.Hide;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns true if all the variables of a deduction puzzle are set to value
 * satisfying all the constraints.
 * 
 * @author Eric.Piette
 * 
 * @remarks Works only for the ending condition of a deduction puzzle.
 */
@Hide
public final class IsTilesComplete extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public IsTilesComplete()
	{
		// Nothing to do.
	}

	@Override
	public boolean eval(final Context context)
	{
		final ContainerState ps = context.state().containerStates()[0];
		final SiteType type = context.board().defaultSite();
		
		final Regions[] regions = context.game().equipment().regions();
		for (int i=0; i<regions.length; i++) {
			if (!regions[i].name().contains("Hints")){
				int count = 0;
				int[] sites = regions[i].sites();
				for (int site : sites) {
					if (ps.isResolved(site, type)) {
						if (ps.what(site, type) == 1) {
							count++;
						}
					}
				}
				if (count != 0 && count != sites.length) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void addConstraint(ProblemAPI translator, Context context, Var[] x) {
		final ContainerState ps = context.state().containerStates()[0];
		final SiteType type = context.board().defaultSite();
		
		final Regions[] regions = context.game().equipment().regions();
		for (int i=0; i<regions.length; i++) {
			if (!regions[i].name().contains("Hints")){
				final Var[] vars = new Var[regions[i].sites().length];
				int[] sites = regions[i].sites();
				System.out.println("What sites");
				for (int site : sites)
					System.out.println(site);
				for (int j = 0; j < sites.length; j++)
					vars[j] = x[context.game().idToVar(sites[j])];
				System.out.println(vars[0]);
				//Object[] test = {translator.sum(vars, translator.EQ, 0), translator.sum(vars, translator.EQ, sites.length)};
				//translator.intension(translator.or(test));
				//translator.intension(translator.or(translator.cou));
			}
		}
		System.out.println("Finished");
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.DeductionPuzzle;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (is Solved) is used but the number of players is not 1.");
			willCrash = true;
		}
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.DeductionPuzzle.id(), true);
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.SolvedEnd.id(), true);
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
	public void preprocess(final Game game)
	{
		// Do nothing
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the puzzle is solved";
	}
}
