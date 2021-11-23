package NST.selection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;

import other.context.Context;
import other.move.Move;
import DataStructures.*;
import DataStructures.Node;
import main.collections.FastArrayList;

public class Selection {

  // Selection Phase
  // Returns the Node in which we simulate from
  public static Node selectionPhase(final Node root, Map<MoveKey, NgramNode> ngramStats, double epsilon) {
    Node current = root;

    while (true) {
      if (current.context.trial().over()) {
        break;
      }

      current = selectNode(current, ngramStats, epsilon);
      
      if (current.visitCount == 0) {
        break;
      }
    }

    return current;
  }

  // Select Function
  // Uses the ngramStats to select a child of the current node
  public static Node selectNode(final Node current, Map<MoveKey, NgramNode> ngramStats, double epsilon) {

    // If there is a node that hasn't been chosen, choose one of those child.
    if (!current.unexpandedMoves.isEmpty()) {

      Move moveFromParent = null;
      if (current.parent != null) {
        moveFromParent = current.parent.moveFromParent;
      }

      // Select the next move, using epsilon greedy and the previous two moves
      Move selectedMove = epsilonGreedySelect(
        current.unexpandedMoves, 
        ngramStats, 
        epsilon, 
        current.moveFromParent,
        moveFromParent
      );
      current.unexpandedMoves.remove(current.unexpandedMoves.indexOf(selectedMove));

			// create a copy of context
			final Context context = new Context(current.context);
			
			// apply the move
			context.game().apply(context, selectedMove);
			
			// create new node and return it
			return new Node(current, selectedMove, context);
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

  // epsilonGreedySelect Function
  // Takes a list of moves, the previous 2 moves and stats and performs an epsilon-greedy select on that list using the ngramStats
  // Returns a Move from the list of Moves
  // Used in both the selection and playout phases
  public static Move epsilonGreedySelect(
    FastArrayList<Move> moves, 
    Map<MoveKey, NgramNode> ngramStats, 
    double epsilon,
    Move previousMove,
    Move secondLastMove
    ) {
    
    // First we must find and record the move with the highest score average
    double highestScore = Double.NEGATIVE_INFINITY;
    Move selectedMove = null;

    MoveKey previousMoveKey = null;
    MoveKey secondLastMoveKey = null;

    if (previousMove != null) {
      previousMoveKey = new MoveKey(previousMove, 0);
    }

    if (secondLastMove != null) {
      secondLastMoveKey = new MoveKey(secondLastMove, 0);
    }
    
    // Loop through all the moves looking for the largest
    for (Move currentMove : moves) {
      MoveKey currentMoveKey = new MoveKey(currentMove, 0);
      NgramNode currentMoveNode = ngramStats.get(currentMoveKey);

      // If there are stats recorded for a move, check the score
      if (currentMoveNode != null) {

        // First set the score for the 1-gram
        double currentMoveScore = currentMoveNode.accumulatedScore / currentMoveNode.visitCount;
        
        // Next get the score for the 2-gram (if it exists)
        NgramNode ngram2Node = currentMoveNode.ngramStats.get(previousMoveKey);
        if (ngram2Node != null) {
          currentMoveScore = ngram2Node.accumulatedScore / ngram2Node.visitCount;
          
          // If we have a 2-gram, check for 3-gram
          NgramNode ngram3Node = ngram2Node.ngramStats.get(secondLastMoveKey);
          if (ngram3Node != null) {
            currentMoveScore = ngram3Node.accumulatedScore / ngram3Node.visitCount;
          }
        }


        
        // If it's higher than the previously recorded, replace both score + move
        if (currentMoveScore > highestScore) {
          highestScore = currentMoveScore;
          selectedMove = currentMove;

        // If it's equal to the previous recorded, give a 50% chance to replace both score + move
        } else if ((currentMoveScore == highestScore) && (Math.random() < 0.5)) {
          highestScore = currentMoveScore;
          selectedMove = currentMove;
        }
      }
    }

    // If we've found a move before, perform epsilon greedy
    // 2 cases where we need to change:
    //   1. If a random number generated is less than epsilon
    //   2. If there are no moves with scores
    if (selectedMove != null) {
      
      // Epsilon percent of the time we choose a random move
      // Otherwise we should just return the selected move
      double randNum = Math.random();
      if (randNum < epsilon) {
        final int r = ThreadLocalRandom.current().nextInt(moves.size());
        final Move move = moves.get(r);
        selectedMove = move;
      }

    } else {
      final int r = ThreadLocalRandom.current().nextInt(moves.size());
      final Move move = moves.get(r);
      selectedMove = move;
    }
    
    // Return the selected move
    return selectedMove;
  }
}
