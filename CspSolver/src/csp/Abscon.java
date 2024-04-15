package csp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import main.collections.FastArrayList;
import game.Game;
import game.types.board.SiteType;
import other.AI;
import other.action.puzzle.ActionSet;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import gnu.trove.list.array.TIntArrayList;


import org.xcsp.modeler.Compiler;

/**
 * Abscon Solver.
 * 
 * @author Eric.Piette
 */
public class Abscon extends AI
{
	
	//-------------------------------------------------------------------------
	
	/** Our player index */
	protected int player = -1;
	
	//-------------------------------------------------------------------------

	/**
	 * The game to solve.
	 */
	public static Game game;
	
	/** 
	 * The context of the game at the init state.
	 */
	public static Context context;
	
	//-------------------------------------------------------------------------
	
	/**
	 * The solution of the puzzle.
	 */
	protected List<Integer> solution;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public Abscon()
	{
		this.friendlyName = "Abscon";
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		
		//Convert the indice of an element to the indice of a Variable if all elements are not variables
		int numElem = game.board().graph().faces().size();
		int current = 0;
		int[] convert = new int[numElem];
		for (int i = 0; i< numElem ; i++ ) {
			convert[i] = current;
			if (game.constraintVariables().contains(i)) {
				current ++;
			}
		}
		
		Move move = null;
		final Trial trial = context.trial();

		// We got the variables in the game.
		final TIntArrayList vars = game.constraintVariables(); 
		
		//System.out.println("VARS: " + vars);
		
		TIntArrayList varsNotSet = new TIntArrayList();
		
		for(int i = 0 ; i < game.constraintVariables().size(); i++) {
			if(!context.state().containerStates()[0].isResolvedCell(vars.get(i)))
				varsNotSet.add(i);
		}

		final int varSelected = varsNotSet.get(context.rng().nextInt(varsNotSet.size()));
		
		// We create the action
		final ActionSet as = new ActionSet(SiteType.Cell, vars.get(varSelected), solution.get(convert[vars.get(varSelected)]));
		as.setDecision(true);
		move = new Move(as);

		
		return move;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void initAI(final Game game, final int playerID)
	{
		System.out.println("Game is " + game.name() + " "  /**game.getOptions().toStrings()**/);

		// Init of the necessary data for the solver.
		this.player = playerID;
		Data.game = game;
		Data.context = new Context(game, new Trial(game));
		
		// Compilation class.
		final Compiler compiler = new org.xcsp.modeler.Compiler();
		// Time values.
		long stopAt = 0;
		long start = System.nanoTime();
		DecimalFormat f = new DecimalFormat("###,###.###");
		
		// Traduction of Ludii to XCSP.
		compiler.generateXCSP();
		
		// Get the time.
		stopAt = System.nanoTime();
		double secs = (stopAt - start) / 1000000000.0;
		System.out.println("Test: ");
		System.out.println("LUDII to XCSP in: " + f.format(secs));
		stopAt = 0;
		start = System.nanoTime();
		
		// Run of the solver code.
	      try {
	    	String[] cmd = { "C:/Program Files/Git/bin/bash", "-c", "resources/runAbscon.sh"};
		    ProcessBuilder pb = new ProcessBuilder();
	    	pb.command(cmd);
	    	pb.directory(new File("../CspSolver"));
	    	Process process = pb.start();
	    	
	    	StringBuilder output = new StringBuilder();
	    	
	  		BufferedReader reader = new BufferedReader(
	  				new InputStreamReader(process.getInputStream()));

	  		String line;
	  		while ((line = reader.readLine()) != null) {
	  			output.append(line + "\n");
	  		}
	  		
	  		// Get the solution.
	  		int begin = output.indexOf("<values>");
	  		for(int index = begin +8 ; index < output.length(); index++)
	  		{
	  			if(Character.isDigit(output.charAt(index)))
	  			{
	  				begin = index;
	  				break;
	  			}
	  		}
	  		solution = new ArrayList<Integer>();	
  			String temp = "";	  	
	  		for(int index = begin ; index < output.length(); index++)
	  		{
	  			//System.out.println(index);
	  			if(Character.isDigit(output.charAt(index)))
	  				temp += output.charAt(index);
	  			else if(!temp.equals(""))
	  			{
	  				solution.add(Integer.valueOf(temp));
	  				temp="";
	  				if(output.charAt(index) == '<')
	  					break;
	  			}
	  		}
	  		System.out.print(solution.toString());

	       } catch (Exception ex) {
	          ex.printStackTrace();
	       }

	      
	        // Get the time.
			stopAt = System.nanoTime();
			secs = (stopAt - start) / 1000000000.0;

			System.out.println("Solved in calling external CSP Abscon in : " + f.format(secs));
		
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
		{
			return true;
		}
				return false;
	}

	//-------------------------------------------------------------------------

}
