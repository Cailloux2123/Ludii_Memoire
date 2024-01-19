package ludiiToXcsp;

import java.util.List;

import org.xcsp.common.IVar.Var;

import game.equipment.other.Regions;
import game.functions.booleans.puzzleConstraints.AllDifferent;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.RegionTypeStatic;
import util.Context;

/**
 * To generate a all Different constraint in the XCSP.
 * 
 * @author Eric.Piette
 */

public class AllDiff{

	Var[] vars;
	IntFunction[] exceptions;
	
	void translate(final AllDifferent allDiff, final Context context, final Var[] x) {
		exceptions = allDiff.exceptions();
		// FOR A REGION
		if (allDiff.region() != null)
		{
			final int[] variables = allDiff.region().eval(context).sites();
			vars = new Var[variables.length];
			for (int i = 0; i < variables.length; i++)
				vars[i] = x[variables[i]];
		}
		else
		{
			final List<Regions> regions = context.game().equipment().regions();
			
			for(final Regions region : regions) {
				if(region.regionTypes() != null) {
					final RegionTypeStatic[] areas = region.regionTypes();
					for(final RegionTypeStatic area : areas) {
						final Integer[][] regionsList = region.convertAreaOnLocs(area, context);
						for(final Integer[] locs : regionsList) {
							final Var[] vars = new Var[locs.length];
							for (int i = 0; i < locs.length; i++)
								vars[i] = x[locs[i]];
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
					}
				}
			}
		}
		
	}

}
