package view.container.aspects.designs.board.puzzle;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import game.util.directions.CompassDirection;
import metadata.graphics.util.PuzzleHintLocationType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.topology.Edge;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class HexagonalNonogramDesign extends PuzzleDesign
{
	public HexagonalNonogramDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
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
			new Color(210, 0, 0),
			new Color(0, 230, 0),
			new Color(0, 0, 255),
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);

		if (hintValues == null)
			detectHints(context);
		
		drawGround(g2d, context, true);
		
		//final TIntArrayList blackLocations = new TIntArrayList();
		//final TIntArrayList varsConstraints = context.game().constraintVariables();
		//for (final Cell c : context.board().topology().cells())
			//if (varsConstraints.contains(c.index()))
				//blackLocations.add(c.index());
		
		//fillCells(g2d, boardStyle.placement().width, colorFillPhase0, colorEdgesInner, strokeThin, blackLocations, Color.BLACK, true);
		
		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);
		
		

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------
	
	@Override
	/**
	 * @param regionIndeces
	 * @param context
	 * @return
	 */
	protected Location findHintPosInRegion(final Integer[] regionIndeces, final SiteType siteType, final Context context)
	{
		if (regionIndeces.length == 1)
			return new FullLocation(regionIndeces[0].intValue(),0,siteType);
		
		double highestRow = -99999999;
		double lowestIndex = 99999999;
		Location bestLocationFound = null;
		
		int cnt = 0;

		for (final Integer cellIndex : regionIndeces)
		{
			
			final Point2D posn = context.topology().getGraphElements(context.board().defaultSite()).get(cellIndex.intValue()).centroid();
			
			final double cellX = posn.getX();
			final double cellY = posn.getY();
			
			if (hintLocationType == PuzzleHintLocationType.BetweenVertices)
			{
				final Point2D posnA = context.topology().getGraphElements(siteType).get(regionIndeces[0].intValue()).centroid();
				final Point2D posnB = context.topology().getGraphElements(siteType).get(regionIndeces[1].intValue()).centroid();
				final Point2D midPoint = new Point2D.Double((posnA.getX() + posnB.getX()) / 2, (posnA.getY() + posnB.getY()) / 2);
					
				double lowestDistance = 99999999;
				for (final Edge e : context.board().topology().edges())
				{
					final double edgeDistance = Math.hypot(midPoint.getX()-e.centroid().getX(), midPoint.getY()-e.centroid().getY());
					if (edgeDistance < lowestDistance)
					{
						lowestDistance = edgeDistance;
						bestLocationFound = new FullLocation(e.index(),0,SiteType.Edge);
					}
				}
				
				if (Math.abs(posnA.getX() - posnB.getX()) < Math.abs(posnA.getY() - posnB.getY()))
				{
					if (posnA.getY() < posnB.getY())
						hintDirections.add(CompassDirection.N);
					else
						hintDirections.add(CompassDirection.S);
				}
				else
				{
					if (posnA.getX() < posnB.getX())
						hintDirections.add(CompassDirection.W);
					else
						hintDirections.add(CompassDirection.E);
				}
			}
			else if (
				(cellX <= lowestIndex && cellY >= highestRow
				||
				cellX < lowestIndex)     // cellY > highestRow.intValue() if top is preferred over left
				&& cnt==0
			)
			{
				cnt ++;
				highestRow = posn.getY();
				lowestIndex = posn.getX();
				bestLocationFound = new FullLocation(cellIndex.intValue(),0,siteType);
				
				if (regionIndeces[0].equals(Integer.valueOf(regionIndeces[1].intValue() - 1))) {
					hintDirections.add(CompassDirection.W);
				}
				else {
					final Point2D posnA = context.topology().getGraphElements(siteType).get(regionIndeces[0].intValue()).centroid();
					final Point2D posnB = context.topology().getGraphElements(siteType).get(regionIndeces[1].intValue()).centroid();
					if (posnA.getX() == posnB.getX()) {
						hintDirections.add(CompassDirection.N);
					} else if (regionIndeces[0] > regionIndeces[1]) {
						hintDirections.add(CompassDirection.NE);
					} else {
						hintDirections.add(CompassDirection.SE);
					}
				}
			}
		}
		
        return bestLocationFound;
	}
	
}
