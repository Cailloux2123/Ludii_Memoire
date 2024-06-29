package csp.Solvers;

import java.util.List;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;

import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.all.sites.AllSites;
import game.functions.booleans.deductionPuzzle.ForAll;
import game.functions.booleans.deductionPuzzle.all.AllDifferent;
import game.functions.booleans.deductionPuzzle.at.regionResult.AtLeast;
import game.functions.booleans.deductionPuzzle.at.regionResult.AtMost;
import game.functions.booleans.deductionPuzzle.is.graph.IsUnique;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsCount;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsMatch;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsSum;
import game.functions.booleans.math.Not;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rules;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Satisfy;
import game.rules.start.StartRule;
import game.rules.start.deductionPuzzle.Set;
import game.types.board.PuzzleElementType;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.util.equipment.Hint;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.TopologyElement;

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
		

		final SiteType type = game.board().defaultSite();
		final int maxElem = game.board().getRange(type).max(context);
		final int minElem = game.board().getRange(type).min(context);
		final int domSize = maxElem - minElem;
		System.out.println("The size of the domain for each element is: " + (domSize+1)); 
		final int numberVariables = game.constraintVariables().size()+1; //+1 Is a temporary fix for Sohei Sudoku, where there are more id then Cells due to weird board geometry
		System.out.println("numberVariables: " + numberVariables);
		

		if (numberVariables == 0) {
			return;
		}
		// We create a variable for each vertex.
		final Var x[];
		if (domSize != 1) {
			x = array("x", size(numberVariables), dom(range(minElem, maxElem + 1)), "x[i] is the cell i");
		} else
			x = array("x", size(numberVariables), dom(range(0, maxElem + 1)), "x[i] is the cell i");

		
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
//		// We add constraint for the moves already played by the human player
//		final ContainerState cs = context.state().containerStates()[0];
//		for (int elem = 0; elem < numberVariables; elem ++) {
//			System.out.println(elem);
//			if (cs.isResolved(elem, type)) {
//				System.out.println(elem);
//				equal(x[elem], cs.what(elem, type));
//				System.out.println(elem + " " + cs.what(elem, type));
//			}
//		}

		// Translation of the constraints
		final Moves moves = game.rules().phases()[0].play().moves();
		if (moves.isConstraintsMoves()) {
			final Satisfy set = (Satisfy) game.rules().phases()[0].play().moves();
			final BooleanFunction[] constraintsToTranslate = set.constraints();
			for (final BooleanFunction constraint : constraintsToTranslate) {
				System.out.println("constraint: " + constraint.toString());
				
				
				// ------------------------------------ NOT
				if (constraint instanceof Not) {
					final Not not = (Not) (constraint);
					
				}
				// ------------------------------------ FOR ALL
				
				if (constraint instanceof ForAll) {
					final ForAll forAll = (ForAll) (constraint);
					final int saveTo = context.to();
					final int saveFrom = context.from();
					final int[] saveHint = context.hint();
					final int saveEdge = context.edge();
					
					BaseBooleanFunction localConstraint = (BaseBooleanFunction) forAll.constraint();

					if (!forAll.type.equals(PuzzleElementType.Hint))
					{
						final List<? extends TopologyElement> elements = context.topology()
								.getGraphElements(PuzzleElementType.convert(forAll.type));
						System.out.println(elements.size());
						for (int i = 0; i < elements.size(); i++)
						{
							final TopologyElement element = elements.get(i);
							context.setFrom(element.index());
							localConstraint.addConstraint(this, context, x);
						}
					}
					else {
						final Integer[][] regions = context.game().equipment().withHints(context.board().defaultSite());
						final Integer[][] hints = context.game().equipment().hints(context.board().defaultSite());
						System.out.println("Number of hints: " + hints.length);
						System.out.println("Number of regions: "+ regions.length);
						for (int i = 0; i < hints.length; i++) {
							localConstraint.addDirectConstraint(this, context,  regions[i], hints[i],x);
						}
					} 
					context.setHint(saveHint);
					context.setEdge(saveEdge);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
				}
				
				// ------------------------------------ ALL SITES
				
				else if (constraint instanceof AllSites) {
					final AllSites allSites = (AllSites) (constraint);
					final int[] locs = allSites.region().eval(context).sites();
					for (int loc : locs) {
						context.setSite(loc);
						allSites.condition().addConstraint(this, context, x);
					}
					
				}

				// ------------------------------------ ALL DIFFERENT

				
				else if (constraint instanceof AllDifferent)
				{
					final AllDifferent allDiff = (AllDifferent) (constraint);
					allDiff.addConstraint(this, context, x);
				}
				
				
				// ------------------------------------ Is UNIQUE
				else if (constraint instanceof IsUnique)
				{
					final IsUnique isUnique = (IsUnique) (constraint);
					isUnique.addConstraint(this, context, x);
				}

				// ------------------------------------ SUM
				else if (constraint instanceof IsSum)
				{

					final IsSum sum = (IsSum) (constraint);
					sum.addConstraint(this, context, x);

				}
				// ------------------------------------ At Most
				else if (constraint instanceof AtMost)
				{
					final AtMost atMost = (AtMost) (constraint);
					atMost.addConstraint(this, context, x);

				}
				// ------------------------------------ At Least
				
				else if (constraint instanceof AtLeast)
				{
					final AtLeast atLeast = (AtLeast) (constraint);
					atLeast.addConstraint(this, context, x);

				}
				
				////// ------------------------------------ Is Count
				else if (constraint instanceof IsCount) {
					final IsCount count = (IsCount) (constraint);
					final int result = count.result().eval(context);
					final int what = count.what().eval(context);
					// FOR A REGION
					if (count.region() != null) {
						final int[] variables = count.region().eval(context).sites();
						final Var[] vars = new Var[variables.length];
						for (int i = 0; i < variables.length; i++) {
							vars[i] = x[variables[i]];
						}

						count(vars, what, EQ, result);
					}
				}
				
				////// ------------------------------------ Is Count

				else if (constraint instanceof IsMatch) {
					final IsMatch match = (IsMatch) constraint;
					match.addConstraint(this, context, x);
				}
				else {
					System.out.println("La contrainte " + constraint.toString() + " n'est pas encore implémentée");
				}
			}

		}

	}
	

}

