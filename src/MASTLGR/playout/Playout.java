package MASTLGR.playout;

import java.util.Map;
import java.util.List;

import game.Game;
import game.rules.play.moves.*;
import other.context.Context;
import other.move.Move;
import other.trial.*;
import other.RankUtils;
import LGR.LGRFunctions;
import DataStructures.*;
import MASTLGR.playout.Playout;
import MASTLGR.selection.Selection;

// Playout (Simulation) Phase
// Takes the context that the game is in and simulates the game to the end
// Uses the GAS & epsilon in order to bias the choices in the simulation
// Updates the statistics and LGRs for all the moves in the trial after simulation
// Returns a trial object
public class Playout {
  public static Trial playoutPhase(
    final Context context, 
    Map<MoveKey, ActionStatistics> globalActionStats,
    Map<MoveKey, LGRNode> lastGoodReplies,
    double epsilon  
  ) {

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
      // First check if there is a last good reply
      Move selectedMove = LGRFunctions.lgrSelect(legal.moves(), lastGoodReplies, context);
      
      // If there isn't, choose a move using the epsilon-greedy method
      if (selectedMove == null) {
        selectedMove = Selection.epsilonGreedySelect(legal.moves(), globalActionStats, epsilon);
      }
      game.apply(context, selectedMove);
    }

    // *** Recording Stats from Trial *** //
    // Get the moves from the trial and record their statistics in actionStatistics
    final double[] utilities = RankUtils.utilities(context);
    final List<Move> trialMoves = trial.generateCompleteMovesList();

    // First we need to get the highest utility
    double highestUtility = Double.NEGATIVE_INFINITY;
    for (double utility : utilities) {
      if (utility > highestUtility) {
        highestUtility = utility;
      }
    }

    // Loop through each move, editing the stats stored in the mapping
    MoveKey lastMoveKey = null;
    MoveKey penultimateMoveKey = null;
    for (Move currentMove : trialMoves) {
      double currentUtility = utilities[currentMove.mover()];     // Ask about what stats should be stored.
      
      // Store the LGRs
      LGRFunctions.lgrStore(currentMove, lastMoveKey, penultimateMoveKey, currentUtility, highestUtility, lastGoodReplies);
      
      MoveKey currentMoveKey = new MoveKey(currentMove, 0);

      // Get the statistics object relating to the current action
      ActionStatistics currentStats = globalActionStats.get(currentMoveKey);

      // If the object doesn't exist yet, set the statistics and then input the stats into the mapping
      if (currentStats == null) {
        currentStats = new ActionStatistics();
        currentStats.visitCount = 1.0;
        currentStats.accumulatedScore = currentUtility;
        globalActionStats.put(currentMoveKey, currentStats);

      // If the object exists, increment the statistics
      } else {
        currentStats.visitCount += 1.0;
        currentStats.accumulatedScore += currentUtility;
      }

      // Shift the move keys to record next move
      penultimateMoveKey = lastMoveKey;
      lastMoveKey = new MoveKey(currentMove, 0);
    }
   
    return trial;
  }
}