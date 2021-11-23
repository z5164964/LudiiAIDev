package LGR;
import java.util.Map;
import java.util.List;

import other.context.Context;
import other.move.Move;
import DataStructures.*;
import main.collections.FastArrayList;

public class LGRFunctions {

  public static Move lgrSelect(
    final FastArrayList<Move> moves, 
    Map<MoveKey, LGRNode> lastGoodReplies, 
    final Context currentContext
  ) {

    // First, get the last 2 moves from the context
    List<Move> movesList = currentContext.trial().generateCompleteMovesList();

    // If we have no moves in the context, return null
    if (movesList.size() == 0) {
      return null;
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
          return null;
        }

      // Otherwise return null - allow for fallback
      } else {
        return null;
      }
    }

    // Otherwise check our 2 last moves
    Move secondLastMove = movesList.get(movesList.size()-2);
    
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

    // Finally, if we have not found a LGR, return null
    return null;
  }

  public static boolean lgrStore(
    Move currentMove,
    MoveKey lastMoveKey,
    MoveKey penultimateMoveKey,
    double currentUtility,
    double highestUtility,
    Map<MoveKey, LGRNode> lastGoodReplies 
  ) {
    // If there is a previous move
    if (lastMoveKey != null) {
      LGRNode lastMoveNode = lastGoodReplies.get(lastMoveKey);

      // If the object doesn't exist yet, make a new object
      if (lastMoveNode == null) {
        lastMoveNode = new LGRNode();
        lastGoodReplies.put(lastMoveKey, lastMoveNode);
      }

      // Update LGR accordingly
      if (currentUtility == highestUtility) {
        lastMoveNode.reply = currentMove;
      } else {
        if (lastMoveNode.reply == currentMove) {
          lastMoveNode.reply = null;
        }
      }

      // Now check if there is a penultimate move
      if (penultimateMoveKey != null) {
        LGRNode penultimateMoveNode = lastMoveNode.lastGoodReplies.get(penultimateMoveKey);

        // If the object doesn't exist yet, make a new object
        if (penultimateMoveNode == null) {
          penultimateMoveNode = new LGRNode();
          lastMoveNode.lastGoodReplies.put(penultimateMoveKey, penultimateMoveNode);
        }

        // Update LGR accordingly
        if (currentUtility == highestUtility) {
          penultimateMoveNode.reply = currentMove;
        } else {
          if (penultimateMoveNode.reply == currentMove) {
            penultimateMoveNode.reply = null;
          }
        }
      }
    }
    return true;
  }
}
