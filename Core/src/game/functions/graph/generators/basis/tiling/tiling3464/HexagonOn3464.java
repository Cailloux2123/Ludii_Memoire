package game.functions.graph.generators.basis.tiling.tiling3464;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Graph;
import main.math.MathRoutines;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a hexagonal rhombotrihexahedral (semi-regular tiling 3.4.6.4) board.
 * 
 * @author cambolbro
 */
@Hide
public class HexagonOn3464 extends Basis
{
	private static final long serialVersionUID = 1L;

	private static final double[][] ref = 
	{
		// Store major hexagon point position
		{ -0.5 * Tiling3464.ux,  1.0 * Tiling3464.uy },
		{  0.5 * Tiling3464.ux,  1.0 * Tiling3464.uy },
		{  1.0 * Tiling3464.ux,  0.0 * Tiling3464.uy },
		{  0.5 * Tiling3464.ux, -1.0 * Tiling3464.uy },
		{ -0.5 * Tiling3464.ux, -1.0 * Tiling3464.uy },
		{ -1.0 * Tiling3464.ux,  0.0 * Tiling3464.uy },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
		{ 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 },
	};
	{		
		// Calculate outer square point positions
		final double a = unit + Math.sqrt(3) / 2.0;
		final double h = a / Math.cos(Math.toRadians(15));
		
		for (int n = 0; n < 12; n++)
		{
			final double theta = Math.toRadians(15 + n * 30);
			
			ref[6 + n][0] = h * Math.cos(theta);
			ref[6 + n][1] = h * Math.sin(theta);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Hidden constructor, is a helper for Tiling3464.
	 * 
	 * @param dim The dimension.
	 */
	public HexagonOn3464
	(
	   	 final DimFunction dim
	)
	{
		this.basis = BasisType.T3464;
		this.shape = ShapeType.Hexagon;
		
		this.dim = new int[] { dim.eval() };
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final int rows = 2 * dim[0] - 1;
		final int cols = 2 * dim[0] - 1;
		
		// Create vertices
		final List<double[]> vertexList = new ArrayList<double[]>();
		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
			{
				if (c > cols / 2 + r || r - c > cols / 2)
					continue;
				
				// Determine reference hexagon position
				final Point2D ptRef = xy(r, c);
				
				// Add satellite points (hexagon and extended square points) relative to reference hexagons
				for (int n = 0; n < Tiling3464.ref.length; n++)
				{
					final double x = ptRef.getX() + ref[n][0];
					final double y = ptRef.getY() + ref[n][1];
					
					// See if vertex already created
					int vid;
					for (vid = 0; vid < vertexList.size(); vid++)
					{
						final double[] ptV = vertexList.get(vid);
						final double dist = MathRoutines.distance(ptV[0], ptV[1], x, y);
						if (dist < 0.1)
							break;
					}
					
					if (vid >= vertexList.size())
						vertexList.add(new double[] { x, y });
				}
			}

		final Graph result = BaseGraphFunction.createGraphFromVertexList(vertexList, unit, basis, shape);
		
		result.reorder();
		
		return result;
	}

	//-------------------------------------------------------------------------

	static Point2D xy(final int row, final int col)
	{
		final double hx = unit * (1 + Math.sqrt(3));
		final double hy = unit * (3 + Math.sqrt(3)) / 2;

		return new Point2D.Double(hy * (col - row), hx * (row + col) * 0.5);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public long gameFlags(Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.SemiRegularTiling.id(), true);
		concepts.set(Concept.HexShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------

}
