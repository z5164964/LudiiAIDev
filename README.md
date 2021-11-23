# LudiiAIDev
This repository contains the implemented MAST, NST and LGR Strategies developed within Ludii across the course of my thesis. A link to the original Ludii repository can be found here: https://github.com/Ludeme/Ludii

## Agents Implemented
There are 6 different agents that have been implemented, each implemented within its own subfolder in the repository:
1. MCTS-UCT: Monte Carlo Tree Search w/ UCT as a selection method
2. UCT-LGR: Last Good Reply Policy w/ MCTS-UCT as a fallback strategy
3. MAST: Move-Average Sampling Technique
4. MAST-LGR: Last Good Reply Policy w/ MAST as a fallback strategy
5. NST: N-gram Selection Technique
6. NST-LGR: Last Good Reply Policy w/ NST as a fallback strategy

## How to run trials
1. Open project within using a Java IDE
2. Download [Ludii's Jar File](https://ludii.games/download.php) and add it as a library to the project
3. Go to the [AITrials.java](https://github.com/z5164964/LudiiAIDev/blob/main/src/Trials/AITrials.java) file
4. Edit which game you want to run (lines 43-48), you may want to edit the board size as an option
5. Edit the number of games you want to run (line 72) and the play time allowed (line 76)
6. Edit the agents you want to compete against each other by importing them (change imports on lines 98 and 99) 
7. Run the main function of the AITrials.java

## Results (vs MCTS-UCT)
These results were the final comparison that we were not able to make during our Thesis. As stated in the Thesis, the trials competing the MAST-LGR and NST-LGR agents against the MCTS-UCT agent were not able to be completed before the due date of the thesis. Therefore the results comparing all agents against MCTS-UCT will be updated on this repository. 300 games were played in each trial, with a epsilon value of 0.2 being used for all MAST and NST agents. Additionally, a play clock of 5 seconds was used in all simulations. All win rates are calculated with a 95% confidence interval.

### Win-rate(%) of all agents against MCTS-UCT, 5s play clock, epsilon = 0.2
| Game          | UCT-LGR           | MAST              | MAST-LGR     | NST               | NST-LGR |
|---------------|-------------------|-------------------|--------------|-------------------|---------|
| Breakthrough  | 70.7 (&#177;5.19) | 75.7 (&#177;4.86) | Not Finished | 89.0 (&#177;3.54) | Not Finished       |
| Knightthrough | 78.7 (&#177;4.64) | 87.0 (&#177;3.81) | Not Finished | 94.7 (&#177;2.54) | Not Finished        |
| Gomoku        | 50.2 (&#177;5.51) | 5.67 (&#177;2.62) | Not Finished | 37.5 (&#177;5.29) | Not Finished        |
| Othello       | 48.2 (&#177;5.45) | 89.3 (&#177;3.43) | Not Finished | 95.3 (&#177;2.25) | Not Finished        |
| Connect Four  | 22.2 (&#177;4.60) | 14.5 (&#177;3.66) | Not Finished | 32.8 (&#177;5.08) |  Not Finished        |
| Atari Go      | 48.3 (&#177;5.65) | 78.0 (&#177;4.69) | Not Finished | 69.7 (&#177;5.20) |  Not Finished        |
