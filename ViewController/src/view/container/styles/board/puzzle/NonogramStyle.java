package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.NonogramDesign;

public class NonogramStyle extends PuzzleStyle
{
	public NonogramStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		
		final NonogramDesign nonogramDesign = new NonogramDesign(this, boardPlacement);
		containerDesign = nonogramDesign;
	}

	//-------------------------------------------------------------------------
	
}
