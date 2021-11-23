package UCT;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.RankUtils;
import other.move.Move;
import other.trial.*;
import game.rules.play.moves.*;

/*
 *  Base development of a MCTS-UCT AI agent for Ludii
 *  
 *  @author Dennis Soemers
 *  Base UCT AI based upon source code example provided at: https://github.com/Ludeme/LudiiExampleAI/blob/master/src/mcts/ExampleUCT.java
 * 
 *  Edits made by Kiran Gupta
 */
public class UCTAI extends AI 
{
	/** Our player index */
	protected int player = -1;
  
  protected String analysisReport = null;
	
  /*
   * Constructor 
   */
  public UCTAI() {
    this.friendlyName = "MCTS-UCT AI";
  }

  @Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
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
      Node selectedNode = selectionPhase(root);
      Context selectedContext = selectedNode.context;

      // Simulation
      if (!selectedContext.trial().over()) {
        selectedContext = new Context(selectedContext);
        playoutPhase(selectedContext);
      }

      // Backpropagation
      backpropagate(selectedContext, selectedNode, game);

      // Increment the counter
      i++;
    }

    // Return the move we wish to play
		return finalMoveSelection(root);
  }

  // Methods for the Selection Phase
  public static Node selectionPhase(final Node root) {
    Node current = root;

    while (true) {
      if (current.context.trial().over()) {
        break;
      }

      current = selectNode(current);
      
      if (current.visitCount == 0) {
        break;
      }
    }

    return current;
  }

  public static Node selectNode(final Node current) {

    // If there is a node that hasn't been chosen, choose that child.
    if (!current.unexpandedMoves.isEmpty()) {

			// randomly select an unexpanded move
			final Move move = current.unexpandedMoves.remove(
					ThreadLocalRandom.current().nextInt(current.unexpandedMoves.size()));
			
			// create a copy of context
			final Context context = new Context(current.context);
			
			// apply the move
			context.game().apply(context, move);
			
			// create new node and return it
			return new Node(current, move, context);
		}

    // Otherwise use UCT equation to select from all children
    Node bestChild = null;
    double bestValue = Double.NEGATIVE_INFINITY;
    int numBestFound = 0;

    final int childrenNum = current.children.size();
    final int mover = current.context.state().mover();

    for (int i = 0; i < childrenNum; i++) {

      // Get the first child and calculate their UCT score
      final Node child = current.children.get(i);

      final double vi = (child.scoreSums[mover] / child.visitCount);
      final double c = Math.sqrt(2.0);
      final double np = (double) current.visitCount;
      final double ni = (double) child.visitCount;
      final double uctValue = vi + c * Math.sqrt(Math.log(np)/ni);

      if (uctValue > bestValue) {
        bestValue = uctValue;
        bestChild = child;
        numBestFound = 1;
      } else if (uctValue == bestValue && ThreadLocalRandom.current().nextInt() % ++numBestFound == 0) {
        bestChild = child;
      }
    }
    
    return bestChild;
  }

  public static Trial playoutPhase(final Context context) {
    final Game game = context.game();
    final Trial trial = context.trial();

    while (!trial.over()) {
      final Moves legal = game.moves(context); 
      //final int mover = context.state().mover();

      // If there are no legal moves, we have reached the terminal state
      if (legal.moves().size() == 0) {
        game.apply(context, Game.createPassMove(context, true));
        continue;
      }

      final int r = ThreadLocalRandom.current().nextInt(legal.moves().size());
      final Move move = legal.moves().get(r);
      game.apply(context, move);
    }

    return trial;
  }

  // Method for the Back-Propagation Phase
  // This computes utilities for all players at the of the playout,
  // which will all be values in [-1.0, 1.0]
  public static Node backpropagate(final Context contextEnd, Node current, Game game) {
    final double[] utilities = RankUtils.utilities(contextEnd);
			
    // Backpropagate utilities through the tree
    while (current != null)
    {
      current.visitCount += 1;
      for (int p = 1; p <= game.players().count(); ++p)
      {
        current.scoreSums[p] += utilities[p];
      }
      current = current.parent;
    }
    return null;
  }


  	/**
	 * Selects the move we wish to play using the "Robust Child" strategy
	 * (meaning that we play the move leading to the child of the root node
	 * with the highest visit count).
	 * 
	 * @param rootNode
	 * @return
	 */
	public static Move finalMoveSelection(final Node rootNode) {
		Node bestChild = null;
    int bestVisitCount = Integer.MIN_VALUE;
    int numBestFound = 0;
    
    final int numChildren = rootNode.children.size();

    for (int i = 0; i < numChildren; ++i) {
      final Node child = rootNode.children.get(i);
      final int visitCount = child.visitCount;
        
        if (visitCount > bestVisitCount) {
            bestVisitCount = visitCount;
            bestChild = child;
            numBestFound = 1;
        }
        else if (
          visitCount == bestVisitCount && 
          ThreadLocalRandom.current().nextInt() % ++numBestFound == 0
        ) {
          // this case implements random tie-breaking
          bestChild = child;
        }
    }
    
    return bestChild.moveFromParent;
	}


  /**
    * Inner class for nodes used by example UCT
    * 
    * @author Dennis Soemers
    */
	private static class Node
	{
		/** Our parent node */
		private final Node parent;
		
		/** The move that led from parent to this node */
		private final Move moveFromParent;
		
		/** This objects contains the game state for this node (this is why we don't support stochastic games) */
		private final Context context;
		
		/** Visit count for this node */
		private int visitCount = 0;
		
		/** For every player, sum of utilities / scores backpropagated through this node */
		private final double[] scoreSums;
		
		/** Child nodes */
		private final List<Node> children = new ArrayList<Node>();
		
		/** List of moves for which we did not yet create a child node */
		private final FastArrayList<Move> unexpandedMoves;
		
		/**
		 * Constructor
		 * 
		 * @param parent
		 * @param moveFromParent
		 * @param context
		 */
		public Node(final Node parent, final Move moveFromParent, final Context context) {
			this.parent = parent;
			this.moveFromParent = moveFromParent;
			this.context = context;
			final Game game = context.game();
			scoreSums = new double[game.players().count() + 1];
			
			// For simplicity, we just take ALL legal moves. 
			// This means we do not support simultaneous-move games.
			unexpandedMoves = new FastArrayList<Move>(game.moves(context).moves());
			
			if (parent != null)
				parent.children.add(this);
		}
	}

  public double getEpsilon() {
    return 0;
  }
}
