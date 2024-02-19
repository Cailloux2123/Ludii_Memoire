package view.container.aspects.designs.board.puzzle;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import metadata.graphics.util.PuzzleDrawHintType;
import other.context.Context;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class RippleEffectDesign extends PuzzleDesign
{
	public RippleEffectDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		drawHintType = PuzzleDrawHintType.None;
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
			new Color(120, 190, 240),
			new Color(210, 230, 255),
			null,
			null,
			null,
			null,
			null,
			new Color(0,0,0),
			swThin,
			swThick
		);
		
		detectHints(context);
		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		//drawOuterCellEdges(bridge, g2d, context);

		// draw inner sudoku regions (for killer sudoko)
		final float dash1[] = {1.0f};
		final BasicStroke dashed = new BasicStroke(strokeThin.getLineWidth(),BasicStroke.CAP_ROUND,BasicStroke.CAP_ROUND,0.0f, dash1, 0.0f);
		
		drawRegions(g2d, context, colorSymbol(), dashed, hintRegions);

		return g2d.getSVGDocument();
	}
	

}