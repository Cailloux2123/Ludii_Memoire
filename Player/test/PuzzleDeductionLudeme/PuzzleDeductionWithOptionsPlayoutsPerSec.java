package PuzzleDeductionLudeme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import game.rules.play.moves.Moves;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ListUtils;
import main.options.Option;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * Experiment for measuring playouts per 40 second for puzzle deduction.
 * 
 * @author Pierre.Accou
 */
public final class PuzzleDeductionWithOptionsPlayoutsPerSec
{
	/** The name of the csv to export with the results. */
	private String exportCSV;
	
	/** If true, suppress standard out print messages (will still write CSV with results at the end) */
	private boolean suppressPrints;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private PuzzleDeductionWithOptionsPlayoutsPerSec()
	{
		// Nothing to do here
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Start the experiment
	 */
	public void startExperiment()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);
		
		final List<File> entries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					
					if (path.equals("../Common/res/lud/board"))
						continue;

					if (path.equals("../Common/res/lud/dominoes"))
						continue;

					if (path.equals("../Common/res/lud/experimental"))
						continue;

					if (path.equals("../Common/res/lud/math"))
						continue;

					if (path.equals("../Common/res/lud/reconstruction"))
						continue;
					
					if (path.equals("../Common/res/lud/simulation"))
						continue;
					
					if (path.equals("../Common/res/lud/subgame"))
						continue;
					
					if (path.equals("../Common/res/lud/test"))
						continue;
					
					if (path.equals("../Common/res/lud/wip"))
						continue;
					
					if (path.equals("../Common/res/lud/wishlist"))
						continue;
					
					if (path.equals("../Common/res/lud/WishlistDLP"))
						continue;
					
					if (path.equals("../Common/res/lud/puzzle/planning"))
						continue;
					
					if (path.equals("../Common/res/lud/puzzle/deduction/Test"))
						continue;
					
					
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}

		final List<String> results = new ArrayList<String>();
		results.add(StringRoutines.join(",", new String[]{ "Name", "p/s", "m/s", "TotalPlayouts" }));

		for (final File fileEntry : entries)
		{
			final String fileName = fileEntry.getPath();

			System.out.println("File: " + fileName);

			final Game game = GameLoader.loadGameFromFile(fileEntry);
			
			assert (game != null);
			
			final List<List<String>> optionCategories = new ArrayList<List<String>>();
				
			for (int o = 0; o < game.description().gameOptions().numCategories(); o++)
			{
				final List<Option> options = game.description().gameOptions().categories().get(o).options();
				final List<String> optionCategory = new ArrayList<String>();

				for (int i = 0; i < options.size(); i++)
				{
					final Option option = options.get(i);
					optionCategory.add(StringRoutines.join("/", option.menuHeadings().toArray(new String[0])));
				}

				if (optionCategory.size() > 0)
					optionCategories.add(optionCategory);
			}

			final List<List<String>> optionCombinations = ListUtils.generateTuples(optionCategories);

			if (optionCombinations.size() > 1)
			{
				for (final List<String> optionCombination : optionCombinations)
				{
					System.out.println("Compiling with options: " + optionCombination);
					final Game toLaunch = GameLoader.loadGameFromFile(fileEntry, optionCombination);
					final String[] result = new String[4];
					if (game != null && !suppressPrints)
						System.out.println("Run: " + game.name());

					result[0] = toLaunch.name();
					

					final Trial trial = new Trial(toLaunch);
					final Context context = new Context(toLaunch, trial);

					// The Test
					long stopAt = 0L;
					long start = System.nanoTime();
					double abortAt = start + 40 * 1000000000.0;
					int solutionFind = 0;
					int moveDone = 0;
					while (stopAt < abortAt)
					{
						toLaunch.start(context);
						Moves moves = context.game().moves(context);
						int randomIndex = (int) (Math.random()*moves.count());
						context.game().apply(context, moves.get(randomIndex));
						if (context.trial().over() && context.game().requiresAllPass() && context.allPass()) {
							++solutionFind;
							context.game().start(context);
						}
						moveDone += 1;
						stopAt = System.nanoTime();
					}

					final double secs = (stopAt - start) / 1000000000.0;
					final double rate = (solutionFind / secs);
					final double rateMove = moveDone;

					result[1] = String.valueOf(rate);
					result[2] = String.valueOf(rateMove);
					result[3] = String.valueOf(solutionFind);
					results.add(StringRoutines.join(",", result));
					
					if (!suppressPrints)
						System.out.println(game.name() + "\t-\t" + secs + "secondes\t-\t" + rate + " Solution by second\t-\t" + rateMove + " mooves\n");
				}
			}
		}
		
		try (final PrintWriter writer = new UnixPrintWriter(new File(exportCSV), "UTF-8"))
		{
			for (final String toWrite : results)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{		
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Measure playouts per second for one or more games."
				);

		argParse.addOption(new ArgOption()
				.withNames("--export-csv")
				.help("Filename (or filepath) to write results to. By default writes to ./results.csv")
				.withDefault("results.csv")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final PuzzleDeductionWithOptionsPlayoutsPerSec experiment = new PuzzleDeductionWithOptionsPlayoutsPerSec();
		
		experiment.exportCSV = argParse.getValueString("--export-csv");


		experiment.startExperiment();
	}

}
