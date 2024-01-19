package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.AkariDesign;

public class AkariStyle extends PuzzleStyle
{
	public AkariStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new AkariDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
