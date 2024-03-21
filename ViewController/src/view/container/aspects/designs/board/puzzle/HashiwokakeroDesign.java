package view.container.aspects.designs.board.puzzle;

import bridge.Bridge;
import view.container.aspects.designs.board.graph.GraphDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

/**
 * Pen and Paper components properties.
 * 
 * @author Matthew.Stephenson
 */
public class HashiwokakeroDesign extends GraphDesign
{
	public HashiwokakeroDesign(final Bridge bridge, final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement, false, false);
		
		bridge.settingsVC().setNoAnimation(true);
	}
	
	//-------------------------------------------------------------------------
	
}
