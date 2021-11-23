package MAST;

import java.util.Map;
import java.util.HashMap;

import game.Game;
import other.AI;
import other.context.Context;
import other.move.Move;
import DataStructures.*;
import MAST.backpropagation.BackPropagation;
import MAST.finalmoveselection.FinalMoveSelection;
import MAST.playout.Playout;
import MAST.selection.Selection;

/*
 *  Base development of a MAST AI agent for Ludii
 *  
 *  @author Dennis Soemers
 *  Base UCT AI based upon source code example provided at: https://github.com/Ludeme/LudiiExampleAI/blob/master/src/mcts/ExampleUCT.java
 * 
 *  Edits made by Kiran Gupta
 */
public class MASTAI extends AI 
{
	/** Our player index */
	protected int player = -1;

  protected int movesMade = 0;

  protected String analysisReport = null;

  protected double epsilon = 0.2;                                         // epsilon value used for e-greedy

  /** Used to store the data of actions **/
  protected final Map<MoveKey, ActionStatistics> globalActionStats;
	
  /*
   * Constructor 
   */
  public MASTAI() {
    this.friendlyName = "MAST AI";
    globalActionStats = new HashMap<MoveKey, ActionStatistics>();
  }

  @Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
    globalActionStats.clear();
    movesMade = 0;
	}

  @Override
	public void closeAI()
	{
    globalActionStats.clear();
    movesMade = 0;
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isStochasticGame())
			return false;
		
		if (!game.isAlternatingMoveGame())
			return false;
		
		return true;
	}

  /*
   * Analysis Report Functions
   * Allows for messages to be output in the analysis tab
   */
  @Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}

  /*
   * SelectAction Function
   * Takes the game and its context and returns a move that the AI chooses
   */
  @Override
  public Move selectAction
  (
    final Game game,
    final Context context,
    final double maxSeconds,
    final int maxIterations,
    final int maxDepth
  ) {
    // Create a root node for the tree
    final Node root = new Node(null, null, context);

    // Check and store all iterations and time limitations
    final int iterationLimit = 
      (maxIterations >= 0) ? 
        maxIterations : 
        Integer.MAX_VALUE;
    
    final long timeLimit = 
      (maxSeconds >= 0) ? 
        System.currentTimeMillis() + (long)(maxSeconds * 1000L) : 
        Long.MAX_VALUE;             

    // Number of iterations made in MCTS
    int i = 0;

    // Whilst we can still loop through
    while (
      i < iterationLimit &&                                       // Whilst we still have iterations left
      System.currentTimeMillis() < timeLimit &&                   // Whilst there is still time left
      !wantsInterrupt                                             // GUI user clicking the pause button
    ) {
      
      // Selection (and Expansion)
      Node selectedNode = Selection.selectionPhase(root, globalActionStats, epsilon);
      Context selectedContext = selectedNode.context;

      // Simulation
      if (!selectedContext.trial().over()) {
        selectedContext = new Context(selectedContext);
        Playout.playoutPhase(selectedContext, globalActionStats, epsilon);
      }

      // Backpropagation
      BackPropagation.backpropagate(selectedContext, selectedNode, game);

      // Increment the counter
      i++;
    }

    // Update variables for output (analysis tab)
    movesMade += 1;
    analysisReport = friendlyName + ": (i: " + i + ", m: " + movesMade + ", actions: " + globalActionStats.size() + ")";

    // Return the move we wish to play
		return FinalMoveSelection.finalMoveSelection(root); 
  }

  public double getEpsilon() {
    return epsilon;
  }
}