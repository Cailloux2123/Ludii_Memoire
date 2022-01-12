package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ArrayUtils;
import main.collections.ListUtils;
import other.GameLoader;
import supplementary.experiments.analysis.RulesetConceptsUCT;
import utils.RulesetNames;

/**
 * Script to generate scripts for evaluation of training runs
 *
 * @author Dennis Soemers
 */
public class EvalDecisionTreesNormalGamesSnellius
{
	
	/** Memory to assign to JVM, in MB (2 GB per core --> we take 2 cores per job, 4GB per job, use 3GB for JVM) */
	private static final String JVM_MEM = "3072";
	
	/** Memory to assign per process (in GB) */
	private static final int MEM_PER_PROCESS = 4;
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1200;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 100;
	
	/** Num trials per matchup */
	private static final int NUM_TRIALS = 50;
	
	/** Memory available per node in GB (this is for Thin nodes on Snellius) */
	private static final int MEM_PER_NODE = 256;
	
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 234;
	
	/** Number of cores per node (this is for Thin nodes on Snellius) */
	private static final int CORES_PER_NODE = 128;
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 2;
	
	/** If we request more cores than this in a job, we get billed for the entire node anyway, so should request exclusive */
	private static final int EXCLUSIVE_CORES_THRESHOLD = 96;
	
	/** If we have more processes than this in a job, we get billed for the entire node anyway, so should request exclusive  */
	private static final int EXCLUSIVE_PROCESSES_THRESHOLD = EXCLUSIVE_CORES_THRESHOLD / CORES_PER_PROCESS;
	
	/** How many processes are we happy to run "simultaneously" on a single core? */
	private static final int PROCESSES_PER_CORE = 2;
	
	/** Number of processes we can put in a single job (on a single node) */
	private static final int PROCESSES_PER_JOB = (PROCESSES_PER_CORE * CORES_PER_NODE) / CORES_PER_PROCESS;
	
	/** Games we want to run */
	private static final String[] GAMES = 
			new String[]
			{
				"Alquerque.lud",
				"Amazons.lud",
				"ArdRi.lud",
				"Arimaa.lud",
				"Ataxx.lud",
				"Bao Ki Arabu (Zanzibar 1).lud",
				"Bizingo.lud",
				"Breakthrough.lud",
				"Chess.lud",
				//"Chinese Checkers.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Fox and Geese.lud",
				"Go.lud",
				"Gomoku.lud",
				"Gonnect.lud",
				"Havannah.lud",
				"Hex.lud",
				"Knightthrough.lud",
				"Konane.lud",
				//"Level Chess.lud",
				"Lines of Action.lud",
				"Omega.lud",
				"Pentalath.lud",
				"Pretwa.lud",
				"Reversi.lud",
				"Royal Game of Ur.lud",
				"Surakarta.lud",
				"Shobu.lud",
				"Tablut.lud",
				//"Triad.lud",
				"XII Scripta.lud",
				"Yavalath.lud"
			};
	
	private static final String[] TREE_TYPES =
			new String[]
			{
				"BinaryClassificationTree_Playout",
				"BinaryClassificationTree_TSPG",
				"ImbalancedBinaryClassificationTree_Playout",
				"ImbalancedBinaryClassificationTree_TSPG",
				"ImbalancedBinaryClassificationTree2_Playout",
				"ImbalancedBinaryClassificationTree2_TSPG",
				"IQRTree_Playout",
				"IQRTree_TSPG",
				"LogitRegressionTree_Playout",
				"LogitRegressionTree_TSPG"
			};
	
	private static final int[] TREE_DEPTHS = new int[] {1, 2, 3, 4, 5, 10};
	
	private static final String[] EXPERIMENT_TYPES = new String[] {"Greedy", "Sampling"};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private EvalDecisionTreesNormalGamesSnellius()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates our scripts
	 * @param argParse
	 */
	private static void generateScripts(final CommandLineArgParse argParse)
	{
		final List<String> jobScriptNames = new ArrayList<String>();

		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String userName = argParse.getValueString("--user-name");
		
		// Sort games in decreasing order of expected duration (in moves per trial)
		// This ensures that we start with the slow games, and that games of similar
		// durations are likely to end up in the same job script (and therefore run
		// on the same node at the same time).
		final Game[] compiledGames = new Game[GAMES.length];
		final double[] expectedTrialDurations = new double[GAMES.length];
		for (int i = 0; i < compiledGames.length; ++i)
		{
			final Game game = GameLoader.loadGameFromName(GAMES[i]);

			if (game == null)
				throw new IllegalArgumentException("Cannot load game: " + GAMES[i]);

			compiledGames[i] = game;
			expectedTrialDurations[i] = RulesetConceptsUCT.getValue(RulesetNames.gameRulesetName(game), "DurationMoves");

			System.out.println("expected duration per trial for " + GAMES[i] + " = " + expectedTrialDurations[i]);
		}

		final List<Integer> sortedGameIndices = ArrayUtils.sortedIndices(GAMES.length, new Comparator<Integer>()
		{

			@Override
			public int compare(final Integer i1, final Integer i2) 
			{
				final double delta = expectedTrialDurations[i2.intValue()] - expectedTrialDurations[i1.intValue()];
				if (delta < 0.0)
					return -1;
				if (delta > 0.0)
					return 1;
				return 0;
			}

		});
		
		// All the "algorithms" we want to evaluate
		final List<String> algorithms = new ArrayList<String>();
		algorithms.add("Random");
		algorithms.add("CE");
		algorithms.add("TSPG");
		//algorithms.add("UCT");
		
		for (final String treeType : TREE_TYPES)
		{
			for (final int treeDepth : TREE_DEPTHS)
			{
				algorithms.add(treeType + "_" + treeDepth);
			}
		}
		
		final List<Object[][]> matchupsPerPlayerCount = new ArrayList<Object[][]>();

		// First create list with data for every process we want to run
		final List<ProcessData> processDataList = new ArrayList<ProcessData>();
		final List<ProcessData> processDataListUCT = new ArrayList<ProcessData>();
		
		for (int idx : sortedGameIndices)
		{
			final Game game = compiledGames[idx];
			final String gameName = GAMES[idx];
			
			final int numPlayers = game.players().count();
			
			// Check if we already have a matrix of matchup-lists for this player count
			while (matchupsPerPlayerCount.size() <= numPlayers)
			{
				matchupsPerPlayerCount.add(null);
			}
			
			if (matchupsPerPlayerCount.get(numPlayers) == null)
				matchupsPerPlayerCount.set(numPlayers, ListUtils.generateCombinationsWithReplacement(algorithms.toArray(), numPlayers));
			
			for (final Object[] matchup : matchupsPerPlayerCount.get(numPlayers))
			{
				boolean includesUCT = false;
				for (final Object obj : matchup)
				{
					final String s = (String) obj;
					
					includesUCT = includesUCT || s.equals("UCT");
				}
				
				if (includesUCT)
				{
					for (final String experimentType : EXPERIMENT_TYPES)
					{
						processDataListUCT.add(new ProcessData(gameName, matchup, experimentType, numPlayers));
					}
				}
				else
				{
					for (final String experimentType : EXPERIMENT_TYPES)
					{
						processDataList.add(new ProcessData(gameName, matchup, experimentType, numPlayers));
					}
				}
			}
		}
		
		processDataList.addAll(0, processDataListUCT);
		
		int processIdx = 0;
		while (processIdx < processDataList.size())
		{
			// Start a new job script
			final String jobScriptFilename = "EvalDecisionTrees_" + jobScriptNames.size() + ".sh";
					
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
			{
				writer.println("#!/bin/bash");
				writer.println("#SBATCH -J EvalDecisionTrees");
				writer.println("#SBATCH -p thin");
				writer.println("#SBATCH -o /home/" + userName + "/EvalDecisionTrees/Out/Out_%J.out");
				writer.println("#SBATCH -e /home/" + userName + "/EvalDecisionTrees/Out/Err_%J.err");
				writer.println("#SBATCH -t " + MAX_WALL_TIME);
				writer.println("#SBATCH -N 1");		// 1 node, no MPI/OpenMP/etc
				
				// Compute memory and core requirements
				final int numProcessesThisJob = Math.min(processDataList.size() - processIdx, (PROCESSES_PER_JOB / PROCESSES_PER_CORE));
				final boolean exclusive = (numProcessesThisJob > EXCLUSIVE_PROCESSES_THRESHOLD);
				final int jobMemRequestGB;
				if (exclusive)
					jobMemRequestGB = Math.min(MEM_PER_NODE, MAX_REQUEST_MEM);	// We're requesting full node anyway, might as well take all the memory
				else
					jobMemRequestGB = Math.min(numProcessesThisJob * MEM_PER_PROCESS, MAX_REQUEST_MEM);
				
				if (exclusive)		// We're requesting full node anyway, might as well take all the cores
					writer.println("#SBATCH --cpus-per-task=" + CORES_PER_NODE);
				else
					writer.println("#SBATCH --cpus-per-task=" + numProcessesThisJob * CORES_PER_PROCESS);
				writer.println("#SBATCH --mem=" + jobMemRequestGB + "G");		// 1 node, no MPI/OpenMP/etc
								
				if (exclusive)
					writer.println("#SBATCH --exclusive");
				
				// load Java modules
				writer.println("module load 2021");
				writer.println("module load Java/11.0.2");
				
				// Put up to PROCESSES_PER_JOB processes in this job
				int numJobProcesses = 0;
				while (numJobProcesses < PROCESSES_PER_JOB && processIdx < processDataList.size())
				{
					final ProcessData processData = processDataList.get(processIdx);
					final String cleanGameName = StringRoutines.cleanGameName(processData.gameName.replaceAll(Pattern.quote(".lud"), ""));
					
					final List<String> agentStrings = new ArrayList<String>();
					for (final Object agent : processData.matchup)
					{
						final String s = (String) agent;
						
						final String agentStr;
						
						if (s.equals("Random"))
						{
							agentStr = "Random";
						}
						else if (s.equals("UCT"))
						{
							agentStr = "UCT";
						}
						else if (s.equals("CE") || s.equals("TSPG"))
						{
							final String algName = (processData.experimentType.equals("Greedy")) ? "Greedy" : "Softmax";
							final String weightsFileName = (s.equals("CE")) ? "PolicyWeightsPlayout_P" : "PolicyWeightsTSPG_P";
							
							final List<String> policyStrParts = new ArrayList<String>();
							policyStrParts.add("algorithm=" + algName);
							for (int p = 1; p <= processData.numPlayers; ++p)
							{
								policyStrParts.add
								(
									"policyweights" + 
									p + 
									"=/home/" + 
									userName + 
									"/TrainFeaturesSnellius4/Out/" + 
									cleanGameName + "_Baseline" +
									"/" + weightsFileName + p + "_00201.txt"
								);
							}
							policyStrParts.add("friendly_name=" + s);
							
							if (s.equals("TSPG"))
								policyStrParts.add("boosted=true");
							
							agentStr = 
									StringRoutines.join
									(
										";", 
										policyStrParts
									);
						}
						else if (s.startsWith("LogitRegressionTree"))
						{
							agentStr = 
									StringRoutines.join
									(
										";", 
										"algorithm=SoftmaxPolicyLogitTree",
										"policytrees=/" + 
										StringRoutines.join
										(
											"/", 
											"home",
											userName,
											"TrainFeaturesSnellius4",
											"Out",
											"Trees",
											cleanGameName,
											s + ".txt"
										),
										"friendly_name=" + s,
										"greedy=" + ((processData.experimentType.equals("Greedy")) ? "true" : "false")
									);
						}
						else
						{
							agentStr = 
									StringRoutines.join
									(
										";", 
										"algorithm=ProportionalPolicyClassificationTree",
										"policytrees=/" + 
										StringRoutines.join
										(
											"/", 
											"home",
											userName,
											"TrainFeaturesSnellius4",
											"Out",
											"Trees",
											cleanGameName,
											s + ".txt"
										),
										"friendly_name=" + s,
										"greedy=" + ((processData.experimentType.equals("Greedy")) ? "true" : "false")
									);
						}
	
						agentStrings.add(StringRoutines.quote(agentStr));
					}
					
					// Write Java call for this process
					final String javaCall = StringRoutines.join
							(
								" ", 
								"java",
								"-Xms" + JVM_MEM + "M",
								"-Xmx" + JVM_MEM + "M",
								"-XX:+HeapDumpOnOutOfMemoryError",
								"-da",
								"-dsa",
								"-XX:+UseStringDeduplication",
								"-jar",
								StringRoutines.quote("/home/" + userName + "/EvalDecisionTrees/Ludii.jar"),
								"--eval-agents",
								"--game",
								StringRoutines.quote("/" + processData.gameName),
								"-n " + NUM_TRIALS,
								"--thinking-time 1",
								"--agents",
								StringRoutines.join(" ", agentStrings),
								"--warming-up-secs",
								String.valueOf(0),
								"--game-length-cap",
								String.valueOf(1000),
								"--out-dir",
								StringRoutines.quote
								(
									"/home/" + 
									userName + 
									"/EvalDecisionTrees/Out/" + 
									processData.experimentType + "/" +
									cleanGameName +
									"/" +
									StringRoutines.join("_", processData.matchup)
								),
								"--output-summary",
								"--output-alpha-rank-data",
								"--max-wall-time",
								String.valueOf(MAX_WALL_TIME),
								">",
								"/home/" + userName + "/EvalDecisionTrees/Out/Out_${SLURM_JOB_ID}_" + numJobProcesses + ".out",
								"&"		// Run processes in parallel
							);
					
					writer.println(javaCall);
					
					++processIdx;
					++numJobProcesses;
				}
				
				writer.println("wait");		// Wait for all the parallel processes to finish

				jobScriptNames.add(jobScriptFilename);
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		final List<List<String>> jobScriptsLists = new ArrayList<List<String>>();
		List<String> remainingJobScriptNames = jobScriptNames;

		while (remainingJobScriptNames.size() > 0)
		{
			if (remainingJobScriptNames.size() > MAX_JOBS_PER_BATCH)
			{
				final List<String> subList = new ArrayList<String>();

				for (int i = 0; i < MAX_JOBS_PER_BATCH; ++i)
				{
					subList.add(remainingJobScriptNames.get(i));
				}

				jobScriptsLists.add(subList);
				remainingJobScriptNames = remainingJobScriptNames.subList(MAX_JOBS_PER_BATCH, remainingJobScriptNames.size());
			}
			else
			{
				jobScriptsLists.add(remainingJobScriptNames);
				remainingJobScriptNames = new ArrayList<String>();
			}
		}

		for (int i = 0; i < jobScriptsLists.size(); ++i)
		{
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "SubmitJobs_Part" + i + ".sh"), "UTF-8"))
			{
				for (final String jobScriptName : jobScriptsLists.get(i))
				{
					writer.println("sbatch " + jobScriptName);
				}
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper around data for a single process (multiple processes per job)
	 *
	 * @author Dennis Soemers
	 */
	private static class ProcessData
	{
		public final String gameName;
		public final Object[] matchup;
		public final String experimentType;
		public final int numPlayers;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param matchup
		 * @param experimentType
		 * @param numPlayers
		 */
		public ProcessData(final String gameName, final Object[] matchup, final String experimentType, final int numPlayers)
		{
			this.gameName = gameName;
			this.matchup = matchup;
			this.experimentType = experimentType;
			this.numPlayers = numPlayers;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Creating eval job scripts."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--user-name")
				.help("Username on the cluster.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--scripts-dir")
				.help("Directory in which to store generated scripts.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateScripts(argParse);
	}
	
	//-------------------------------------------------------------------------

}
