package csp.Solvers;

import java.util.List;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;

import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.deductionPuzzle.all.AllDifferent;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsCount;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsSum;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rules;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Satisfy;
import game.rules.start.StartRule;
import game.rules.start.deductionPuzzle.Set;
import game.types.board.RegionTypeStatic;
import game.util.equipment.Hint;
import other.context.Context;

/**
 * Translator from Ludii to XCSP.
 * 
 * @author Eric.Piette
 */

public class Translator implements ProblemAPI {

	@Override
	public void model() {
		// We get the game and his context from Ludii.
		final Game game = Data.game;
		final Context context = Data.context;
		final Rules rules = game.rules();

		final int domSize = game.board().cellRange().max(context);
		System.out.println("domSize: " + domSize);
		final int numberVariables = game.constraintVariables().size();
		System.out.println("numberVariables: " + numberVariables);

		if (numberVariables == 0) {
			return;
		}
		// We create a variable for each vertex.
		final Var x[];
		if (domSize != 1) {
			x = array("x", size(numberVariables), dom(range(1, domSize + 1)), "x[i] is the cell i");
		} else
			x = array("x", size(numberVariables), dom(range(0, domSize + 1)), "x[i] is the cell i");

		// We create the unary constraints from the starting rules.
		if (rules.start() != null) {
			final StartRule[] starts = rules.start().rules();
			for (final StartRule s : starts) {
				// Only from the set rules.
				if (s.isSet()) {
					final Set set = (Set) s;
					for (int index = 0; index < set.vars().length; index++) {
						final int var = set.vars()[index];
						final int value = set.values()[index];
						equal(x[var], value);
					}
				}
			}
		}

		// Translation of the constraints
		final Moves moves = game.rules().phases()[0].play().moves();
		if (moves.isConstraintsMoves()) {
			final Satisfy set = (Satisfy) game.rules().phases()[0].play().moves();
			final BooleanFunction[] constraintsToTranslate = set.constraints();
			for (final BooleanFunction constraint : constraintsToTranslate) {
				System.out.println("constraint: " + constraint.toString());

				// ------------------------------------ ALL DIFFERENT

				if (constraint instanceof AllDifferent)
				{
					final AllDifferent allDiff = (AllDifferent) (constraint);
					allDiff.addConstraint(this, context, x);
				}

				// ------------------------------------ SUM
				else if (constraint instanceof IsSum)
				{

					final IsSum sum = (IsSum) (constraint);
					sum.addConstraint(this, context, x);

				}
				////// ------------------------------------ Count
				else if (constraint instanceof IsCount) {
					System.out.println("ON vient bien dans la contrainte");
					final IsCount count = (IsCount) (constraint);
					final int result = count.result().eval(context);
					final int what = count.what().eval(context);
					// FOR A REGION
					if (count.region() != null) {
						System.out.println("Blabla pouki");
						final int[] variables = count.region().eval(context).sites();
						final Var[] vars = new Var[variables.length];
						for (int i = 0; i < variables.length; i++) {
							vars[i] = x[variables[i]];
						}

						count(vars, what, EQ, result);
					}
				}
			}

		}

	}

}

