package NST.playout;

import java.util.Map;
import java.util.List;

import game.Game;
import game.rules.play.moves.*;
import other.context.Context;
import other.move.Move;
import other.trial.*;
import other.RankUtils;
import DataStructures.*;
import NST.playout.Playout;
import NST.selection.Selection;

// Playout (Simulation) Phase
// Takes the context that the game is in and simulates the game to the end
// Uses the ngramStats & epsilon in order to bias the choices in the simulation
// Updates the statistics for all the moves in the trial after simulation
// Returns a trial object
public class Playout {
  public static Trial playoutPhase(final Context context, Map<MoveKey,  NgramNode> ngramStats, double epsilon) {

    final Game game = context.game();
    final Trial trial = context.trial();
    final List<Move> movesList = trial.generateCompleteMovesList();
    Move previousMove = null;
    Move secondLastMove = null;
    
    if (movesList.size() > 0) {
      previousMove = movesList.get(movesList.size()-1);
    }

    if (movesList.size() > 1) {
      secondLastMove = movesList.get(movesList.size()-2);
    }

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
      Move selectedMove = Selection.epsilonGreedySelect(legal.moves(), ngramStats, epsilon, previousMove, secondLastMove);
      game.apply(context, selectedMove);

      secondLastMove = previousMove;
      previousMove = selectedMove;
    }

    // *** Recording Stats from Trial *** //
    // Get the moves from the trial and record their statistics in actionStatistics
    final double[] utilities = RankUtils.utilities(context);
    final List<Move> trialMoves = trial.generateCompleteMovesList();
    previousMove = null;
    secondLastMove = null;

    // Loop through each move, editing the stats stored in the mapping
    for (Move currentMove : trialMoves) {
      double utility = utilities[currentMove.mover()];     // Ask about what stats should be stored.
      
      // Set the 1-gram stats
      NgramNode node1 = recordStats(ngramStats, utility, currentMove);
      
      // Set the 2-gram stats if previousMove exists
      if (previousMove != null) {
        NgramNode node2 = recordStats(node1.ngramStats, utility, previousMove);
      
        // Set the 3-gram stats if secondLastMove exists
        if (secondLastMove != null) {
          recordStats(node2.ngramStats, utility, secondLastMove);
        }
      }

      secondLastMove = previousMove;
      previousMove = currentMove;
    }   
    return trial;
  }

  // Records the stats of the node in the map provided
  // Returns the NgramNode relating to the move
  public static NgramNode recordStats(
    Map<MoveKey,  NgramNode> statsMap,
    double utility,
    Move move
  ) {
    // First get the move from the map
    MoveKey moveKey = new MoveKey(move, 0);
    NgramNode moveNode = statsMap.get(moveKey); 

    if (moveNode == null) {
      moveNode = new NgramNode();
      moveNode.visitCount = 1.0;
      moveNode.accumulatedScore = utility;
      statsMap.put(moveKey, moveNode);
    } else {
      moveNode.visitCount += 1.0;
      moveNode.accumulatedScore += utility;
    }
    return moveNode;
  }
}
