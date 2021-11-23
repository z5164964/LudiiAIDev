package MASTLGR;

import java.util.Map;
import java.util.HashMap;

import game.Game;
import other.AI;
import other.context.Context;
import other.move.Move;
import DataStructures.*;
import MASTLGR.backpropagation.BackPropagation;
import MASTLGR.finalmoveselection.FinalMoveSelection;
import MASTLGR.playout.Playout;
import MASTLGR.selection.Selection;

/*
 *  MAST-LGR AI agent for Ludii
 *  Implements the LGR policy with MAST as a fall-back
 *  
 *  Based on work by Dennis Soemers
 *  Base UCT AI based upon source code example provided at: https://github.com/Ludeme/LudiiExampleAI/blob/master/src/mcts/ExampleUCT.java
 * 
 *  @author Kiran Gupta
 */
public class MASTLGRAI extends AI 
{
	/** Our player index */
	protected int player = -1;

  protected int movesMade = 0;

  protected String analysisReport = null;
  
  protected double epsilon = 0.2;                                         // epsilon value used for e-greedy

  /** Used to store the data of actions **/
  protected final Map<MoveKey, ActionStatistics> globalActionStats;
  protected final Map<MoveKey, LGRNode> lastGoodReplies;
	
  /*
   * Constructor 
   */
  public MASTLGRAI() {
    this.friendlyName = "MAST-LGR AI";
    globalActionStats = new HashMap<MoveKey, ActionStatistics>();
    lastGoodReplies = new HashMap<MoveKey, LGRNode>();
  }

  @Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
    globalActionStats.clear();
    lastGoodReplies.clear();
    movesMade = 0;
	}

  @Override
	public void closeAI()
	{
    globalActionStats.clear();
    lastGoodReplies.clear();
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

    int i = 0;

    // Whilst we can still loop through
    while (
      i < iterationLimit &&                                       // Whilst we still have iterations left
      System.currentTimeMillis() < timeLimit &&                   // Whilst there is still time left
      !wantsInterrupt                                             // GUI user clicking the pause button
    ) {
      
      // Selection (and Expansion)
      Node selectedNode = Selection.selectionPhase(root, globalActionStats, lastGoodReplies, epsilon);
      Context selectedContext = selectedNode.context;

      // Simulation
      if (!selectedContext.trial().over()) {
        selectedContext = new Context(selectedContext);
        Playout.playoutPhase(selectedContext, globalActionStats, lastGoodReplies, epsilon);
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