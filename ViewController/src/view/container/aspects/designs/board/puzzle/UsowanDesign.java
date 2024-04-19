package view.container.aspects.designs.board.puzzle;

import java.awt.Color;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.other.Regions;
import game.types.board.SiteType;
import metadata.graphics.util.PuzzleDrawHintType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class UsowanDesign extends PuzzleDesign
{
	
	protected ArrayList<ArrayList<Location>> regionsLocalisation= new ArrayList<>();
	
	public UsowanDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		drawHintType = PuzzleDrawHintType.Default;
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
		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);

		detectRegions(context);
		
		if (!regionsLocalisation.isEmpty()) {
			drawRegions(g2d, context, new Color(0,0,0), strokeThick, regionsLocalisation);
		}

		return g2d.getSVGDocument();
	}
	
	public void detectRegions(final Context context) {
		final Regions[] regions = context.game().equipment().regions();
		int allRegion = regions.length;
		for (int i=0; i<allRegion; i++) {
			final ArrayList<Location> regionLoc = new ArrayList<>();
			int[] sites = regions[i].sites();
			if (!regions[i].name().contains("Hints")) {
				for (int site : sites) {
					regionLoc.add(new FullLocation(site,0,SiteType.Cell));
				}
				regionsLocalisation.add(regionLoc);
			}
		}
	}
	
}
