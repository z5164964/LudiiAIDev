package UCTLGR.selection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Map;
import java.util.List;

import other.context.Context;
import other.move.Move;
import DataStructures.*;
import DataStructures.Node;
import main.collections.FastArrayList;

public class Selection {

  // Selection Phase
  // Returns the Node in which we simulate from
  public static Node selectionPhase(final Node root, Map<MoveKey, LGRNode> lastGoodReplies) {
    Node current = root;

    while (true) {
      if (current.context.trial().over()) {
        break;
      }

      current = selectNode(current, lastGoodReplies);
      
      if (current.visitCount == 0) {
        break;
      }
    }

    return current;
  }

  // Select Function
  // Uses the LGRs to select a child of the current node
  public static Node selectNode(final Node current, Map<MoveKey, LGRNode> lastGoodReplies) {

    // If there is a node that hasn't been chosen, choose one of those child.
    if (!current.unexpandedMoves.isEmpty()) {

      Move selectedMove = lgrSelect(current.unexpandedMoves, lastGoodReplies, current.context);
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

  // lgrSelect Function
  // Takes a list of moves and finds the LGR based on the context
  // Returns a Move from the list of Moves if LGR is available, if not a random move is selected
  // Used in both the selection and playout phases
  public static Move lgrSelect(final FastArrayList<Move> moves, Map<MoveKey, LGRNode> lastGoodReplies, final Context currentContext) {

    // First, get the last 2 moves from the context
    List<Move> movesList = currentContext.trial().generateCompleteMovesList();

    // If we have no moves in the context, return a random move
    if (movesList.size() == 0) {
      final int r = ThreadLocalRandom.current().nextInt(moves.size());
      final Move move = moves.get(r);
      return move;
    }
    
    Move previousMove = movesList.get(movesList.size()-1);
    MoveKey moveKey = new MoveKey(previousMove, 0);
    LGRNode lgrNode1 = lastGoodReplies.get(moveKey);

    // If we have 1 move in the context, check for its LGR and if that exists in our
    // available moves, use that
    if (movesList.size() == 1) {
      if (lgrNode1 != null && lgrNode1.reply != null) {
        if (moves.contains(lgrNode1.reply)) {
          return lgrNode1.reply;
        } else {
          final int r = ThreadLocalRandom.current().nextInt(moves.size());
          final Move move = moves.get(r);
          return move;
        }

      // Otherwise choose a random move
      } else {
        final int r = ThreadLocalRandom.current().nextInt(moves.size());
        final Move move = moves.get(r);
        return move;
      }
    }

    // Otherwise check our 2 last moves
    Move secondLastMove = movesList.get(movesList.size()-2);

    // Now find the last good reply
    // First check for LGR with history length 2
    if (lgrNode1 != null) {
      moveKey = new MoveKey(secondLastMove, 0);
      LGRNode lgrNode2 = lgrNode1.lastGoodReplies.get(moveKey);

      // If we have a reply for the past two moves, check if that reply exists in the moves list
      if (lgrNode2 != null && lgrNode2.reply != null) {
        if (moves.contains(lgrNode2.reply)) {
          return lgrNode2.reply;
        }
      }

      // Next check for a reply for the past move and if it exists in the moves list
      if (lgrNode1.reply != null) {
        if (moves.contains(lgrNode1.reply)) {
          return lgrNode1.reply;
        }
      }
    }

    // Finally, if we have not found a LGR, return a random move
    final int r = ThreadLocalRandom.current().nextInt(moves.size());
    final Move move = moves.get(r);
    return move;
  }
}