package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.designs.board.puzzle.HexagonalNonogramDesign;

public class HexagonalNonogramStyle extends PuzzleStyle
{
	public HexagonalNonogramStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		containerDesign = new HexagonalNonogramDesign(this, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

}
