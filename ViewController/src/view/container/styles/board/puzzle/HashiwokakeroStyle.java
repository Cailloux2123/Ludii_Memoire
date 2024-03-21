package view.container.styles.board.puzzle;

import bridge.Bridge;
import game.equipment.container.Container;
import other.context.Context;
import view.container.aspects.components.board.HashiwokakeroComponents;
import view.container.aspects.designs.board.puzzle.HashiwokakeroDesign;
import view.container.styles.board.graph.GraphStyle;

public class HashiwokakeroStyle extends GraphStyle
{
	public HashiwokakeroStyle(final Bridge bridge, final Container container, final Context context) 
	{
		super(bridge, container, context);
		final HashiwokakeroDesign boardDesign = new HashiwokakeroDesign(bridge, this, boardPlacement);
		containerDesign = boardDesign;
		containerComponents = new HashiwokakeroComponents(bridge, this, boardDesign);
	}

	//-------------------------------------------------------------------------

}
