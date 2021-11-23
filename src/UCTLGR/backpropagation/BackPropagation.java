package UCTLGR.backpropagation;

import other.context.Context;
import DataStructures.Node;
import other.RankUtils;
import game.Game;

public class BackPropagation {
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
}
