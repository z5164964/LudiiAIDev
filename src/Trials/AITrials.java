package Trials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import game.Game;
import game.types.state.GameType;
import graphics.svg.element.Style;
import main.FileHandling;
import main.collections.FastArrayList;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;
import other.trial.Trial;
import Random.RandomAI;

import UCT.UCTAI;
import UCTLGR.UCTLGRAI;
import MAST.MASTAI;
import MASTLGR.MASTLGRAI;
import NST.NSTAI;
import NSTLGR.NSTLGRAI;

/**
 * A simple tutorial that demonstrates a variety of useful methods provided
 * by the Ludii general game system.
 * Original File Here: https://github.com/Ludeme/LudiiExampleAI/blob/master/src/experiments/Tutorial.java
 * 
 * @author Dennis Soemers
 * Edited for our Thesis trials by Kiran Gupta
 */
public class AITrials
{

	public static void main(final String[] args)
	{	

		// Change Board Size here
		//final List<String> options = Arrays.asList("Board Size/6x6");

		// Change game HERE
		//Game game = GameLoader.loadGameFromFile(new File("src/Games/Checkers6.lud"));
		//Game game = GameLoader.loadGameFromName("Breakthrough.lud", options);    // For games with different board sizes
		Game game = GameLoader.loadGameFromName("Knightthrough.lud");
		
		// the game's "stateFlags" contain properties of the game that may be
		// important for some AI algorithms to know about
		final long gameFlags = game.gameFlags();

		System.out.println("\n--------- Running Trial ---------");
		System.out.println("--------- Game Data: ---------");
		// for example, we may like to know whether our game has stochastic elements
		System.out.println("Game chosen: " + game.name());
		final boolean isStochastic = ((gameFlags & GameType.Stochastic) != 0L);
		if (isStochastic)
			System.out.println(game.name() + " is stochastic.");
		else
			System.out.println(game.name() + " is not stochastic.");
		
		// figure out how many players are expected to play this game
		final int numPlayers = game.players().count();
		System.out.println(game.name() + " is a " + numPlayers + "-player game.");

		// Number of sites on board
		System.out.println("Num sites on board = " + game.board().numSites());

		// Change number of games we'd like to play HERE
		final int numGames = 150;
		System.out.println("Number of Games being played: " + numGames);

		// Change the time given to each AI agent per move
		final double givenTime = 5;
		System.out.println("Time given per move: " + givenTime + " seconds");

		final int moveLimit = Integer.MAX_VALUE;
		System.out.println("Limited Number of Moves: " + moveLimit);

		
		// to be able to play the game, we need to instantiate "Trial" and "Context" objects
		Trial trial = new Trial(game);
		Context context = new Context(game, trial);
		
		//---------------------------------------------------------------------

		// now we're going to have a look at playing a few full games, using AI
		
		// first, let's instantiate some agents
		final List<AI> agents = new ArrayList<AI>();
		agents.add(null);	// insert null at index 0, because player indices start at 1
	
		// Change Agents HERE
		// NOTE: in our following loop through number of games, the different
		// agents are always assigned the same player number.
		MASTLGRAI p1 = new MASTLGRAI();      // Player 1
	  UCTAI p2 = new UCTAI();     // Player 2
		agents.add(p1);   
		agents.add(p2);   
		
		System.out.println("\n--------- AI Data: ---------");
		System.out.println("AI #1: " + p1.friendlyName);
		if (p1.getEpsilon() != 0) {
			System.out.println("Epsilon value: " + p1.getEpsilon());
		}

		System.out.println("AI #2: " + p2.friendlyName);
		if (p2.getEpsilon() != 0) {
			System.out.println("Epsilon value: " + p2.getEpsilon());
		}
		
		// Score counters for wins and losses in games
    int p1Score = 0;
    int p2Score = 0;
    int otherScore = 0;

		long startTime = System.currentTimeMillis();

		System.out.println("\n--------- Running Trials (" + numGames + " Games) ---------");
		for (int i = 0; i < numGames; ++i)
		{
			// (re)start our game
			game.start(context);
			
			// (re)initialise our agents
			for (int p = 1; p < agents.size(); ++p)
			{
				agents.get(p).initAI(game, p);
      }

			int moveCount = 0;
			
			// keep going until the game is over
			while (!context.trial().over() && moveCount < moveLimit)
			{
				// figure out which player is to move
				final int mover = context.state().mover();
								
				// retrieve mover from list of agents
				final AI agent = agents.get(mover);
				
				// ask agent to select a move
				// we'll give them a search time limit of 0.2 seconds per decision
				// IMPORTANT: pass a copy of the context, not the context object directly
				final Move move = agent.selectAction
						(
							game, 
							new Context(context),
							givenTime,
							-1,
							-1
						);
								
				// apply the chosen move
				game.apply(context, move);

				moveCount++;
			}

			if (moveCount == moveLimit) {
				System.out.println("Move Limit Hit");
			}
			
			// let's see who won
			final int gameCount = i+1;
			System.out.println("Game " + gameCount + ": " + context.trial().status());
			if (context.trial().status() != null) {
				if (context.trial().status().winner() == 1) {
					p1Score++;
				} else if (context.trial().status().winner() == 2) {
					p2Score++;
				} else {
					otherScore++;
				}
			} else {
				otherScore++;
			}

      
		}

		long endTime = System.currentTimeMillis();

		System.out.println("\n--------- Trial Results (" + numGames + " Games) ---------");
    System.out.println("Times the " + p1.friendlyName + " won: " + p1Score);
    System.out.println("Times the " + p2.friendlyName + " won: " + p2Score);
    System.out.println("Times other results happened: " + otherScore);

		long timeElapsed = (endTime - startTime)/1000;
 
    System.out.println("Execution time in Seconds: " + timeElapsed);
	}
}

