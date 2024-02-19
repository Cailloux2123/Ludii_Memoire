package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.RippleEffectDesign;

public class RippleEffectStyle extends PuzzleStyle
{
	public RippleEffectStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		
		final RippleEffectDesign sudokuDesign = new RippleEffectDesign(this, boardPlacement);
		containerDesign = sudokuDesign;
	}

	//-------------------------------------------------------------------------
	
}
