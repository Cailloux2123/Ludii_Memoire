package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.UsowanDesign;

public class UsowanStyle extends PuzzleStyle
{
	public UsowanStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		
		final UsowanDesign usowanDesign = new UsowanDesign(this, boardPlacement);
		containerDesign = usowanDesign;
	}

	//-------------------------------------------------------------------------
	
}
