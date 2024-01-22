package ludiiToXcsp;

import java.util.List;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;

import csp.Data;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.deductionPuzzle.all.AllDifferent;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsSum;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rules;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Satisfy;
import game.rules.start.StartRule;
import game.rules.start.deductionPuzzle.Set;
import game.types.board.RegionTypeStatic;
import other.context.Context;

/**
 * Translator from Ludii to XCSP.
 * 
 * @author Eric.Piette
 */

public class Translator implements ProblemAPI{

	@Override
	public void model()
	{
		// We get the game and his context from Ludii.
		final Game game = Data.game;
		final Context context = Data.context;
		final Rules rules = game.rules();

		// WARNING: Works for puzzle with vertices for now. TODO for Face | Edge.
		
		// Domain size = number of components.
		final int domSize = game.numComponents();
		final int numberVariables = game.constraintVariables().size();
		if (numberVariables == 0) {
			return;
		}

		// We create a variable for each vertex.
		final Var x[];
		if(domSize != 1) {
			x = array("x", size(numberVariables), dom(range(1, domSize + 1)), "x[i] is the cell i");
		}
		else
			x = array("x", size(numberVariables), dom(range(0, domSize + 1)), "x[i] is the cell i");
		
		// We create the unary constraints from the starting rules.
		if (rules.start() != null)
		{
			final StartRule[] starts = rules.start().rules();
			for (final StartRule s : starts)
			{
				// Only from the set rules.
				if (s.isSet())
				{
					final Set set = (Set) s;
					for (int index = 0; index < set.vars().length; index++)
					{
						final int var = set.vars()[index];
						final int value = set.values()[index];
						equal(x[var], value);
					}
				}
			}
		}

		// Translation of the constraints
		final Moves moves = game.rules().phases()[0].play().moves();
		
		if (moves.isConstraintsMoves())
		{
			final Satisfy set = (Satisfy) game.rules().phases()[0].play().moves();
			final BooleanFunction[] constraintsToTranslate = set.constraints();
			for (final BooleanFunction constraint : constraintsToTranslate)
			{

//------------------------------------ ALL DIFFERENT -----------------------------------------------------					

				if (constraint instanceof AllDifferent) // TODO add in Ludii a boolean function to get each of them differently.
				{
						final AllDifferent allDiff = (AllDifferent) (constraint);
						final IntFunction[] exceptions = allDiff.exceptions();
						// FOR A REGION
						if (allDiff.region() != null)
						{
							final int[] variables = allDiff.region().eval(context).sites();
							final Var[] vars = new Var[variables.length];
							for (int i = 0; i < variables.length; i++)
								vars[i] = x[variables[i]];
							if(exceptions.length == 0)
								allDifferent(vars);
							else 
								allDifferent(vars, exceptions[0].eval(context));
						}
						else
						{
							final Regions[] regions = context.game().equipment().regions();
							
							for(final Regions region : regions) {
								if(region.regionTypes() != null) {
									final RegionTypeStatic[] areas = region.regionTypes();
									for(final RegionTypeStatic area : areas) {
										final Integer[][] regionsList = region.convertStaticRegionOnLocs(area, context);
										for(final Integer[] locs : regionsList) {
											final Var[] vars = new Var[locs.length];
											for (int i = 0; i < locs.length; i++)
												vars[i] = x[locs[i]];
											if(exceptions.length == 0)
												allDifferent(vars);
											else 
												allDifferent(vars, exceptions[0].eval(context));
										}
									}
								}
								else if(region.region() != null) {
									final RegionFunction[] regionsFunctions = region.region();
									for(final RegionFunction regionFunction : regionsFunctions) {
										final int[] locs = regionFunction.eval(context).sites();
										final Var[] vars = new Var[locs.length];
										for (int i = 0; i < locs.length; i++)
											vars[i] = x[locs[i]];
										if(exceptions.length == 0)
											allDifferent(vars);
										else 
											allDifferent(vars, exceptions[0].eval(context));
									}
								}
							}
						}
//					AllDiff allDifferent = new AllDiff();
//					allDifferent.translate(allDiff, context, x);
//					Var[] vars = allDifferent.vars;
//					if(allDifferent.exceptions.length == 0)
//						allDifferent(vars);
//					else 
//						allDifferent(vars, allDifferent.exceptions[0].eval(context));
				}

//------------------------------------ SUM -----------------------------------------------------	
/**
					else if (constraint instanceof IsSum) // TODO add in Ludii a boolean function to get each of them differently.
					{
						final IsSum sum = (IsSum) (constraint);
						int result = sum.result().eval(context);
						final String nameRegion = sum.nameRegion();
						final RegionFunction regionFn = sum.region();
					
						if(regionFn != null)
						{
							final int[] sites = regionFn.eval(context).sites();
							final Var[] vars = new Var[sites.length];
							for (int i = 0; i < sites.length; i++)
									vars[i] = x[sites[i]];
							sum(vars, EQ, result);
						}
						else {
							final List<Regions> regions = context.game().equipment().regions();
							final Integer[] vertexHint = context.game().equipment().vertexHints();
							for(final Regions region : regions) {
							if(nameRegion == null || (region.name() != null && region.name().equals(nameRegion)))
								if(region.regionTypes() != null) {
									final RegionTypeStatic[] areas = region.regionTypes();
									for(final RegionTypeStatic area : areas) {
										final Integer[][] regionsList = region.convertStaticRegionOnLocs(area, context);
										int indexRegion = 0;
										for(final Integer[] locs : regionsList) {
											if(sum.result() instanceof Hint) {
												context.setHint(vertexHint[indexRegion]);
												result = sum.result().eval(context);
											}
											final Var[] vars = new Var[locs.length];
											for(int i = 0 ; i < locs.length ; i++) 
													vars[i] = x[locs[i].intValue()];
											sum(vars, EQ, result);
												
											indexRegion++;
										}
									}
								}
						}
					}
					
					}
					**/
//					
////------------------------------------ Mult -----------------------------------------------------	
//
//					else if (constraint instanceof Mult)
//					{
//						final Mult mult = (Mult) (constraint);
//						int result = mult.result().eval(context);
//						final String nameRegion = mult.nameRegion();
//						final RegionFunction regionFn = mult.region();
//					
//						if(regionFn != null)
//						{
//							final int[] sites = regionFn.eval(context).sites();
//							final Var[] vars = new Var[sites.length];
//							for (int i = 0; i < sites.length; i++)
//									vars[i] = x[sites[i]];
//							if(vars.length == 1)
//								eq(mul(vars[0],1),result);
//							else if(vars.length == 2)
//								eq(mul(vars[0],vars[1]),result);
//							else if(vars.length == 3)
//								eq(mul(vars[0],vars[1],vars[2]),result);
//							else if(vars.length == 4)
//								eq(mul(vars[0],vars[1],vars[2],vars[3]),result);
//							else if(vars.length == 5)
//								eq(mul(vars[0],vars[1],vars[2],vars[3],vars[4]),result);
//							else if(vars.length == 6)
//								eq(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5]),result);
//							else if(vars.length == 7)
//								eq(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6]),result);
//							else if(vars.length == 8)
//								eq(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6],vars[7]),result);
//							else if(vars.length == 9)
//								eq(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6],vars[7],vars[8]),result);
//						}
//						else {
//							final List<Region> regions = context.game().equipment().regions();
//							final Integer[] vertexHint = context.game().vertexHints();
//							for(final Region region : regions) {
//							if(nameRegion == null || (region.name() != null && region.name().equals(nameRegion)))
//								if(region.areas() != null) {
//									final AreaType[] areas = region.areas();
//									for(final AreaType area : areas) {
//										final Integer[][] regionsList = region.convertAreaOnLocs(area, context);
//										int indexRegion = 0;
//										for(final Integer[] locs : regionsList) {
//											if(mult.result() instanceof Hint) {
//												context.setHint(vertexHint[indexRegion]);
//												result = mult.result().eval(context);
//											}
//											final Var[] vars = new Var[locs.length];
//											for(int i = 0 ; i < locs.length ; i++) 
//													vars[i] = x[locs[i].intValue()];
//
//											if(vars.length == 1)
//												equal(mul(vars[0],1),result);
//											else if(vars.length == 2)
//												equal(mul(vars[0],vars[1]),result);
//											else if(vars.length == 3)
//												equal(mul(vars[0],vars[1],vars[2]),result);
//											else if(vars.length == 4)
//												equal(mul(vars[0],vars[1],vars[2],vars[3]),result);
//											else if(vars.length == 5)
//												equal(mul(vars[0],vars[1],vars[2],vars[3],vars[4]),result);
//											else if(vars.length == 6)
//												equal(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5]),result);
//											else if(vars.length == 7)
//												equal(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6]),result);
//											else if(vars.length == 8)
//												equal(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6],vars[7]),result);
//											else if(vars.length == 9)
//												equal(mul(vars[0],vars[1],vars[2],vars[3],vars[4],vars[5],vars[6],vars[7],vars[8]),result);
//												
//												
//											indexRegion++;
//										}
//									}
//								}
//						}
//					}
//					
//					}
//////------------------------------------ Count -----------------------------------------------------	
//					else if (constraint instanceof Count)
//					{
//						final Count count = (Count) (constraint);
//						final int result = count.result().eval(context);
//						final int what = count.what().eval(context);
//						// FOR A REGION
//						if (count.region() != null)
//						{
//							final int[] variables = count.region().eval(context).sites();
//							final Var[] vars = new Var[variables.length];
//							for (int i = 0; i < variables.length; i++)
//								vars[i] = x[variables[i]];
//							count(vars, what, EQ, result);
//						}
//					}
////				
//////-------------------------------------------Unique----------------------------------------------------
//					else if (constraint instanceof Unique)
//					{
//						final List<Region> regions = context.game().equipment().regions();
//						for(final Region region : regions) {
//							if(region.areas() != null) {
//								final AreaType[] areas = region.areas();
//								for(final AreaType area : areas) {
//									final Integer[][] regionsList = region.convertAreaOnLocs(area, context);
//									
//									for(int i = 0 ; i < regionsList.length; i++)
//										for(int j = i+1 ; j < regionsList.length; j++)
//										{
//											final Integer[] set1 = regionsList[i];
//											final Integer[] set2 = regionsList[j];
//											
//											final Var[] vars1 = new Var[set1.length];
//											int i1 = 0;
//											for (final Integer var : set1)
//											{
//												vars1[i1] = x[var.intValue()];
//												i1++;
//											}
//											
//											final Var[] vars2 = new Var[set2.length];
//											int i2 = 0;
//											for (final Integer var : set2)
//											{
//												vars2[i2] = x[var.intValue()];
//												i2++;
//											}
//											
//											allDifferentList(vars1,vars2);
//										}
//								}
//							}
//						}
//					}
//						
////					
//////----------------------------------------------------- NOT Line -------------(TO IMPROVE to manage Not)-----------------------------
////
//					else if (constraint instanceof Not)
//					{
//						final Not not = (Not) (constraint);
//						
//						if(not.a() instanceof Line)
//						{
//							final Line line = (Line) (not.a());
//							final int size = line.length().eval(context);
//							final DirectionChoice dirn = line.dirn();
//							
//							final Transitions transitions = transitions();
//							final Function<Integer, String> q = i -> "q" + i;
//							int num = 1;
//							transitions.add(q.apply(num), 1, q.apply(num));
//							 for (int i = 0; i < size-1; i++) {
//								 transitions.add(q.apply(num), 2, q.apply(num+1));
//								 transitions.add(q.apply(num+1), 1, q.apply(1));
//								 num++;
//							 }
//							final Automaton automaton = automaton(q.apply(1), transitions, q.apply(1),q.apply(2),q.apply(3));
//							
//							final Transitions transitions2 = transitions();
//							final Function<Integer, String> q2 = i -> "q" + i;
//							int num2 = 1;
//							transitions2.add(q2.apply(num2), 2, q2.apply(num2));
//							 for (int i = 0; i < size-1; i++) {
//								 transitions2.add(q2.apply(num2), 1, q2.apply(num2+1));
//								 transitions2.add(q2.apply(num2+1), 2, q2.apply(1));
//								 num2++;
//							 }
//							final Automaton automaton2 = automaton(q.apply(1), transitions2, q.apply(1),q.apply(2),q.apply(3));
//							
//							
//							if(dirn.equals(DirectionChoice.Orthogonal))
//							{
//								for (final List<Vertex> row : context.graph().rows())
//								{
//									final Var[] vars = new Var[row.size()];
//									int i = 0;
//									for (final Vertex vertex : row)
//									{
//										vars[i] = x[vertex.index()];
//										i++;
//									}
//									regular(vars, automaton);
//									regular(vars, automaton2);
//								}
//								
//								for (final List<Vertex> col : context.graph().columns())
//								{
//									final Var[] vars = new Var[col.size()];
//									int i = 0;
//									for (final Vertex vertex : col)
//									{
//										vars[i] = x[vertex.index()];
//										i++;
//									}
//									regular(vars, automaton);
//									regular(vars, automaton2);
//								}
//							}
//							
//						}
//					}
//					
////					
//////----------------------------------------------------- For EACH -------------(TO IMPROVE to manage For Each)-----------------------------
////					
//					else if (constraint instanceof ForEach)
//					{
//						final ForEach forEach = (ForEach) (constraint);
//						final VariableType type = forEach.type();
//						final BooleanFunction constraintIntern = forEach.constraint();
//						
//						if(type.equals(VariableType.Vertex)) {
//							final Integer[][] regions = context.game().vertexWithHints();
//							final Integer[] hints = context.game().vertexHints();
//							
//							final int size = Math.min(regions.length, hints.length);
//							for(int i = 0; i < size; i++) {
//								for(int j = 0 ; j < regions[i].length; j++)
//								{
//									final int indexVertex = regions[i][j];
//									final int nbEdges = 0;
//									final PuzzleState ps = (PuzzleState) context.state().containerStates()[0];
//
//									for(int indexEdge = 0 ; indexEdge < context.game().board().graph().edges().size() ; indexEdge++)
//									{
//										final Edge edge = context.game().board().graph().edges().get(indexEdge);
//									}
//
//									if(hints[i] != null)
//										context.setHint(hints[i]);
//									context.setEdge(nbEdges);
//
//								}
//							}
//						}
//						
//						else if(type.equals(VariableType.Hint)) {
//							final Integer[][] regions = context.game().vertexWithHints();
//							final Integer[] hints = context.game().vertexHints();
//							
//							final int size = Math.min(regions.length, hints.length);
//							for(int i = 0; i < size; i++) {
//								if(constraintIntern instanceof Count) {
//									final Count count = (Count) (constraintIntern);
//									final int result = hints[i].intValue();
//									final int what = count.what().eval(context);
//									context.setFrom(regions[i][0]);
//									// FOR A REGION
//									if (count.region() != null)
//									{
//										final int[] variables = count.region().eval(context).sites();
//										final Var[] vars = new Var[variables.length];
//										for (int j = 0; j < variables.length; j++)
//											vars[j] = x[variables[j]];
//										count(vars, what, EQ, result);
//									}
//								}
//								else
//									if(constraintIntern instanceof Lt) {
//										final int locA = regions[i][0];
//										final int locB = regions[i][1];
//										final Var[] vars = new Var[2];
//										vars[0] = x[locA];
//										vars[1] = x[locB];
//										lessThan(vars[0],vars[1]);
//									}
//								
//							}
//						}
//						
//						else if(type.equals(VariableType.Face)) {
//							final Integer[][] regions = context.game().faceWithHints();
//							final Integer[] hints = context.game().faceHints();
//						
//							final int size = Math.min(regions.length, hints.length);
//							for(int i = 0; i < size; i++) {		
//								if(constraintIntern instanceof Count) {
//									final Count count = (Count) (constraintIntern);
//									final int result = hints[i].intValue();
//									final int what = count.what().eval(context);
//									context.setFrom(regions[i][0]);
//									// FOR A REGION
//									if (count.region() != null)
//									{
//										final int[] variables = count.region().eval(context).sites();
//										final Var[] vars = new Var[variables.length];
//										for (int j = 0; j < variables.length; j++)
//											vars[j] = x[variables[j]];
//										count(vars, what, EQ, result);
//									}
//								}
//							}
//						}
//					}
///////////////////////////////////////////////////// END //////////////////////
				}
			}
	
	}
	
}
