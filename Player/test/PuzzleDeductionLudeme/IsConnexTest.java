package PuzzleDeductionLudeme;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import game.functions.booleans.deductionPuzzle.is.regionResult.IsConnex;
import game.functions.ints.IntConstant;
import main.FileHandling;
import main.grammar.Description;
import manager.utils.game_logs.MatchRecord;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * For Puzzle: A Unit Test to test the is Connex ludeme.
 * 
 * @author Pierre.Accou
 */
@SuppressWarnings("static-method")
public class IsConnexTest
{
	/**
	 * The test to run
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void test() throws FileNotFoundException, IOException
	{
		System.out.println(
				"\n=========================================\nIs Connex Ludeme Deduction Puzzle Test\n=========================================\n");

		final long startAt = System.nanoTime();
		final File startFolder = new File("../Common/res/lud/puzzle/deduction/Test/IsConnex");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
					gameDirs.add(fileEntry);
				else
					entries.add(fileEntry);
			}
		}

		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

				final File trialsDir = new File("../Player/res/random_trials/puzzle/deduction/Test/IsConnex");
				
				if (!trialsDir.exists())
				{
					System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());
					continue;
				}

				final File[] trialFiles = trialsDir.listFiles();

				if (trialFiles.length == 0)
				{
					System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());
					continue;
				}

				// Load the string from lud file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(ludPath);
				}
				catch (final FileNotFoundException ex)
				{
					fail("Unable to open file '" + ludPath + "'");
				}
				catch (final IOException ex)
				{
					fail("Error reading file '" + ludPath + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				if (game == null)
					fail("COMPILATION FAILED for the file : " + ludPath);

				if (game.hasSubgames())
					continue;

				for (final File trialFile : trialFiles)
				{
					final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					context.rng().restoreState(loadedRecord.rngState());
					
					game.start(context);
					
					for (int loadedMovesIdx = 0; loadedMovesIdx<loadedMoves.size(); loadedMovesIdx++) {
						game.apply(context, loadedMoves.get(loadedMovesIdx));
					}
					
					IntConstant intconstante = new IntConstant(0);
					IsConnex isConnex = new IsConnex(null, null, intconstante);
					
					if (trialFile.getName().contains("True")) {
						assert(isConnex.eval(context) == true) : "The ludeme AtLeast will return true for this case but return false";
					}
					else {
						assert(isConnex.eval(context) != true) : "The ludeme AtLeast will return false for this case but return true";
					}

				}
			}
		}

		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");
	}

}
