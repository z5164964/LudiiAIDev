package UCTLGR.playout;

import java.util.Map;
import java.util.List;

import game.Game;
import game.rules.play.moves.*;
import other.context.Context;
import other.move.Move;
import other.trial.*;
import other.RankUtils;
import DataStructures.*;
import UCTLGR.playout.Playout;
import UCTLGR.selection.Selection;

// Playout (Simulation) Phase
// Takes the context that the game is in and simulates the game to the end
// Updates the LGRs for all the moves in the trial after simulation
// Returns a trial object
public class Playout {
  public static Trial playoutPhase(final Context context, Map<MoveKey, LGRNode> lastGoodReplies) {

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

      // *** Choosing move using stats *** //
      // Find an appropriate move using epsilon greedy & apply it
      Move selectedMove = Selection.lgrSelect(legal.moves(), lastGoodReplies, context);
      game.apply(context, selectedMove);
    }

    // *** Recording Stats from Trial *** //
    // Store the LGRs after the trial
    final double[] utilities = RankUtils.utilities(context);
    final List<Move> trialMoves = trial.generateCompleteMovesList();

    // First we need to get the highest utility
    double highestUtility = Double.NEGATIVE_INFINITY;
    for (double utility : utilities) {
      if (utility > highestUtility) {
        highestUtility = utility;
      }
    }

    // Next loop through the moves and store LGRs
    MoveKey lastMoveKey = null;
    MoveKey penultimateMoveKey = null;
    for (Move currentMove : trialMoves) {
      double currentUtility = utilities[currentMove.mover()];

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

      // Shift the move keys to record next move
      penultimateMoveKey = lastMoveKey;
      lastMoveKey = new MoveKey(currentMove, 0);
    }

    return trial;
  }
}