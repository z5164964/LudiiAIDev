package MAST.playout;

import java.util.Map;
import java.util.List;

import game.Game;
import game.rules.play.moves.*;
import other.context.Context;
import other.move.Move;
import other.trial.*;
import other.RankUtils;
import DataStructures.*;
import MAST.playout.Playout;
import MAST.selection.Selection;

public class Playout {

  // Playout (Simulation) Phase
  // Takes the context that the game is in and simulates the game to the end
  // Uses the GAS & epsilon in order to bias the choices in the simulation
  // Updates the statistics for all the moves in the trial after simulation
  // Returns a trial object
  public static Trial playoutPhase(final Context context, Map<MoveKey, ActionStatistics> globalActionStats, double epsilon) {

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
      Move selectedMove = Selection.epsilonGreedySelect(legal.moves(), globalActionStats, epsilon);
      game.apply(context, selectedMove);
    }

    // *** Recording Stats from Trial *** //
    // Get the moves from the trial and record their statistics in actionStatistics
    final double[] utilities = RankUtils.utilities(context);
    final List<Move> trialMoves = trial.generateCompleteMovesList();

    // Loop through each move, editing the stats stored in the mapping
    for (Move currentMove : trialMoves) {
      double currentUtility = utilities[currentMove.mover()];     // Ask about what stats should be stored.
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
    }
   
    return trial;
  }
}