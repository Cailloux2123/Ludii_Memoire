package game.functions.booleans;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.api.ProblemAPI;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;

import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import other.BaseLudeme;
import other.context.Context;
import other.location.Location;
import csp.Solvers.Translator;

/**
 * Is a org.xcsp.common functionality for boolean functions to avoid lots of cookie-cutter
 * code.
 * 
 * @author mrraow and Eric.Piette
 */
public abstract class BaseBooleanFunction extends BaseLudeme implements BooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Only used for Puzzles to detect the region constraint*/
	public RegionFunction regionConstraint;
	
	/** Only used for Puzzles to detect the locs constraint*/
	public IntFunction[] locsConstraint;
	
	/** Only used for Puzzles to detect the area constraint*/
	public RegionTypeStatic areaConstraint;
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public boolean autoFails()
	{
		return false;
	}
	
	@Override
	public boolean autoSucceeds()
	{
		return false;
	}

	@Override
	public RegionFunction regionConstraint() 
	{
		return regionConstraint;
	}
	
	@Override
	public IntFunction[] locsConstraint() 
	{
		return locsConstraint;
	}
	
	@Override
	public RegionTypeStatic staticRegion() 
	{
		return areaConstraint;
	}

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		return new ArrayList<Location>();
	}
	
	public void addConstraint(Translator translator, Context context, Var[] x) {
		return;
	}
	
	public void addDirectConstraint(Translator translator,Context context, Integer[] region, Integer[] result, Var[] x) {
		return;	
	}

	@Override
	public BitSet stateConcepts(final Context context)
	{
		if (eval(context))
			return this.concepts(context.game());

		return new BitSet();
	}



}
