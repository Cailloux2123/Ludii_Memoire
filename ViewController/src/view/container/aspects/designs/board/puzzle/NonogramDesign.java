package view.container.aspects.designs.board.puzzle;

import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import metadata.graphics.util.PuzzleDrawHintType;
import other.context.Context;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class NonogramDesign extends PuzzleDesign
{
	public NonogramDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		drawHintType = PuzzleDrawHintType.NextTo;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(120, 190, 240),
			new Color(0, 0, 0),
			new Color(210, 230, 255),
			null,
			null,
			null,
			null,
			null,
			new Color(200,50,200),
			swThin,
			swThick
		);
		
		detectHints(context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);

		return g2d.getSVGDocument();
	}


	
	//-------------------------------------------------------------------------
	
}
