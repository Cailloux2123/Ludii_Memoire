package view.container.aspects.designs.board.puzzle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import game.util.directions.CompassDirection;
import gnu.trove.list.array.TIntArrayList;
import metadata.graphics.util.PuzzleDrawHintType;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.TopologyElement;
import other.topology.Vertex;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class AkariDesign extends PuzzleDesign
{
	public AkariDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		drawHintType = PuzzleDrawHintType.TopLeft;
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
				new Color(200,50,200),
				swThin,
				swThick
			);
			
			if (hintValues == null)
				detectHints(context);
			
			final TIntArrayList blackLocations = new TIntArrayList();
			final TIntArrayList varsConstraints = context.game().constraintVariables();
			for (final Cell c : context.board().topology().cells())
				if (varsConstraints.contains(c.index()))
					blackLocations.add(c.index());
			
			fillCells(g2d, boardStyle.placement().width, colorFillPhase0, colorEdgesInner, strokeThin, blackLocations, Color.BLACK, true);
			
			drawInnerCellEdges(g2d, context);
			drawOuterCellEdges(bridge, g2d, context);

			return g2d.getSVGDocument();
		}

		//-------------------------------------------------------------------------
		
		@Override
		public void drawPuzzleHints(final Graphics2D g2d, final Context context)
		{
			if (hintValues == null)
				detectHints(context);
			
			final Font valueFont = new Font("Arial", Font.BOLD, (boardStyle.cellRadiusPixels()));
			g2d.setColor(Color.WHITE);
			g2d.setFont(valueFont);
			
			for (final TopologyElement graphElement : topology().getAllGraphElements())
			{
				final SiteType type = graphElement.elementType();
				final int site = graphElement.index();

				final Point2D posn = graphElement.centroid();

				final Point drawnPosn = screenPosn(posn);

				for (int i = 0; i < hintValues.size(); i++)
				{
					if (locationValues.get(i).site() == site && locationValues.get(i).siteType() == type)
					{
						int maxHintvalue = 0;
						for (int j = 0; j < hintValues.size(); j++)
						{
							if (hintValues.get(i) != null)
							{
								if (hintValues.get(i).intValue() > maxHintvalue)
								{
									maxHintvalue = hintValues.get(i).intValue();
								}
							}
						}
						
						if (maxHintvalue > 9)
							g2d.setFont(new Font("Arial", Font.BOLD, (int) (boardStyle.cellRadiusPixels()/1.5)));
						
						final Rectangle2D rect = g2d.getFont().getStringBounds(Integer.toString(hintValues.get(i).intValue()), g2d.getFontRenderContext());
						
						if (hintDirections.get(i) == CompassDirection.W)
							g2d.drawString(hintValues.get(i).toString(), (int)(drawnPosn.x - rect.getWidth()/2 - cellRadiusPixels()*1.5), (int)(drawnPosn.y + rect.getHeight()/4 - cellRadiusPixels()*0.3));
						else if (hintDirections.get(i) == CompassDirection.N)
							g2d.drawString(hintValues.get(i).toString(), (int)(drawnPosn.x - rect.getWidth()/2 - cellRadiusPixels()*0.5), (int)(drawnPosn.y + rect.getHeight()/4 - cellRadiusPixels()*1.5));
					}
				}
			}
		}

		/**
		 * Draws the edges of the board grid.
		 */
		protected void drawGridEdges(final Graphics2D g2d, final Color borderColor, final BasicStroke stroke)
		{
			final List<Cell> cells = topology().cells();
			
			g2d.setColor(borderColor);
			g2d.setStroke(stroke);
			final List<Edge> sudokuEdges = new ArrayList<>();
			final GeneralPath path = new GeneralPath();
			final double boardDimension = Math.sqrt(cells.size());
			final int lineInterval = (int) Math.sqrt(boardDimension);

			for (final Cell cell : cells)
			{
				for (final Edge edge : cell.edges())
				{
					// vertical lines
					final int columnValue = (cell.index()+1);
					if ( (columnValue%lineInterval == 0) && (columnValue%boardDimension != 0) )
					{
						if (edge.vA().centroid().getX() > cell.centroid().getX() && edge.vB().centroid().getX() > cell.centroid().getX())
						{
							sudokuEdges.add(edge);
						}
					}
					
					// horizontal lines
					final int rowLength = (int) Math.sqrt(cells.size());
					final int rowValue = ((cell.index())/rowLength);
					if ( (rowValue%lineInterval == (lineInterval-1)) && (rowValue%(boardDimension-1) != 0) )
					{
						if (edge.vA().centroid().getY() > cell.centroid().getY() && edge.vB().centroid().getY() > cell.centroid().getY())
						{
							sudokuEdges.add(edge);
						}
					}
				}
			}

			while (sudokuEdges.size() > 0)
			{
				Edge currentEdge = sudokuEdges.get(0);
				boolean nextEdgeFound = true;

				final Point2D va = currentEdge.vA().centroid();
				Point2D vb = currentEdge.vB().centroid();
				final Point vAPosn = screenPosn(va);
				Point vBPosn = screenPosn(vb);

				path.moveTo(vAPosn.x, vAPosn.y);

				while (nextEdgeFound == true)
				{
					nextEdgeFound = false;
					path.lineTo(vBPosn.x, vBPosn.y);
					sudokuEdges.remove(currentEdge);

					for (final Edge nextEdge : sudokuEdges)
					{
						if (Math.abs(vb.getX() - nextEdge.vA().centroid().getX()) < 0.0001 && Math.abs(vb.getY() - nextEdge.vA().centroid().getY()) < 0.0001)
						{
							nextEdgeFound = true;

							currentEdge = nextEdge;
							vb = currentEdge.vB().centroid();
							vBPosn = screenPosn(vb);

							break;
						}
						else if (Math.abs(vb.getX() - nextEdge.vB().centroid().getX()) < 0.0001 && Math.abs(vb.getY() - nextEdge.vB().centroid().getY()) < 0.0001)
						{
							nextEdgeFound = true;

							currentEdge = nextEdge;
							vb = currentEdge.vA().centroid();
							vBPosn = screenPosn(vb);

							break;
						}
					}
				}
			}
			g2d.draw(path);
		}
		
		//-------------------------------------------------------------------------
		
		/**
		 * @param g2d
		 * @param pixels
		 * @param fillColor
		 * @param borderColor
		 * @param stroke
		 * @param validLocations - null if all locations are valid
		 * @param colorInvalid - color for the invalid cells
		 * @param addDiagonal - draws a diagonal if true
		 */
		protected void fillCells
		(
			final Graphics2D g2d, final int pixels,
			final Color fillColor, final Color borderColor, final BasicStroke stroke,
			final TIntArrayList validLocations,
			final Color colorInvalid,
			final boolean addDiagonal
		)
		{
			final List<Cell> cells = topology().cells();
			
			g2d.setStroke(stroke);
			for (final Cell cell : cells)
			{
				final GeneralPath path = new GeneralPath();
				g2d.setColor(fillColor);
				for (int v = 0; v < cell.vertices().size(); v++)
				{
					if (path.getCurrentPoint() == null)
					{
						final Vertex prev = cell.vertices().get(cell.vertices().size() - 1);
						final Point prevPosn = boardStyle.screenPosn(prev.centroid());
						path.moveTo(prevPosn.x, prevPosn.y);
					}
					final Vertex corner = cell.vertices().get(v);
					final Point cornerPosn = boardStyle.screenPosn(corner.centroid());
					path.lineTo(cornerPosn.x, cornerPosn.y);
				}

				if (validLocations != null)
				{
				// if cell is not a valid region, then color it differently
					if (!validLocations.contains(cell.index()))
					{
						g2d.setColor(colorInvalid);
					}
				}
				g2d.fill(path);

				if (addDiagonal && validLocations != null)
				{
					// add diagonal lines to invalid cells
					if (!validLocations.contains(cell.index()))
					{
						g2d.setColor(borderColor);
						
						final Point firstCorner = boardStyle.screenPosn(cell.vertices().get(1).centroid());
						final Point secondCorner = boardStyle.screenPosn(cell.vertices().get(3).centroid());
						path.moveTo(firstCorner.getX(), firstCorner.y);
						path.lineTo(secondCorner.x, secondCorner.y);
						g2d.draw(path);
					}
				}
			}
		}
		
	}
