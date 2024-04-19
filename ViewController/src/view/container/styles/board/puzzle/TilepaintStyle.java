package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.KakuroDesign;
import view.container.aspects.designs.board.puzzle.TilepaintDesign;

public class TilepaintStyle extends PuzzleStyle
{
	public TilepaintStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new TilepaintDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
