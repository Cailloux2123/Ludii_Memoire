package csp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import util.AI;
import util.Context;
import util.Move;
import util.Trial;
import util.action.ActionSet;
import org.xcsp.modeler.Compiler;

/**
 * Choco solver.
 * 
 * @author Eric.Piette
 */
public class Choco extends AI
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
	public Choco()
	{
		this.friendlyName = "Choco";
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
		Move move = null;
		final Trial trial = context.trial();

		// We got the variables in the game.
		final TIntArrayList vars = game.constraintVariables(); 
		
		//System.out.println("VARS: " + vars);
		
		TIntArrayList varsNotSet = new TIntArrayList();
		
		for(int i = 0 ; i < game.board().numSites(); i++)
			if(trial.state().containerStates()[0].what(vars.get(i)) == 0)
				varsNotSet.add(i);
		
		final int varSelected = varsNotSet.get(context.rng().nextInt(varsNotSet.size()));
		
		// We create the action
		final ActionSet as = new ActionSet(vars.get(varSelected), solution.get(vars.get(varSelected)));
		as.setDecision(true);
		move = new Move(as);
		move.setFrom(vars.get(varSelected));
		move.setTo(vars.get(varSelected));
		
		return move;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void initAI(final Game game, final int playerID)
	{
		System.out.println("Game is " + game.name() + " " + game.gameOptions().toStrings());
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
		System.out.println("LUDII to XCSP in: " + f.format(secs));
		stopAt = 0;
		start = System.nanoTime();
		
		// Run of the solver code.
	      try {
	    	String[] cmd = { "C:\\Program Files\\Git\\bin\\bash.exe", "-c", "/c/Users/eric.piette/runChoco.sh"};
		    ProcessBuilder pb = new ProcessBuilder();

	    	pb.command(cmd);
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

	       } catch (Exception ex) {
	          ex.printStackTrace();
	       }

	      
	        // Get the time.
			stopAt = System.nanoTime();
			secs = (stopAt - start) / 1000000000.0;

			System.out.println("Solved in calling external CSP Choco in : " + f.format(secs));
		
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
		{
			System.out.println("GAME supported");
			return true;
		}
		
		System.out.println("GAME not supported");
		return false;
	}

	//-------------------------------------------------------------------------

}
