package MASTLGR.finalmoveselection;
import java.util.concurrent.ThreadLocalRandom;

import other.move.Move;
import DataStructures.Node;

public class FinalMoveSelection {
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
}
