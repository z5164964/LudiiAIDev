package Trials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import NST.NSTAI;
import MASTLGR.MASTLGRAI;
import NSTLGR.NSTLGRAI;

/**
 * A simple tutorial that demonstrates a variety of useful methods provided
 * by the Ludii general game system.
 * 
 * @author Dennis Soemers
 * 
 * Class modified by Kiran Gupta, to run a tester trial so test print statements (white box testing)
 */
public class Tester
{

	public static void main(final String[] args)
	{	

		// Change Board Size here
		final List<String> options = Arrays.asList("Board Size/6x6");

		// Change game HERE
		Game game = GameLoader.loadGameFromName("Breakthrough.lud", options);

		System.out.println("\n--------- Testing Game ---------");
		System.out.println("--------- Game Data: ---------");
		// for example, we may like to know whether our game has stochastic elements
		System.out.println("Game chosen: " + game.name());
		/*
		final boolean isStochastic = ((gameFlags & GameType.Stochastic) != 0L);
		if (isStochastic)
			System.out.println(game.name() + " is stochastic.");
		else
			System.out.println(game.name() + " is not stochastic.");
		*/
		// figure out how many players are expected to play this game
		//CCfinal int numPlayers = game.players().count();
		//System.out.println(game.name() + " is a " + numPlayers + "-player game.");

		// Number of sites on board
		//System.out.println("Num sites on board = " + game.board().numSites());

		// Change the time given to each AI agent per move
		final double givenTime = 5;
		//System.out.println("Time given per move: " + givenTime + " seconds");

		
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
		AI p1 = new MASTLGRAI();      // Player 1
		AI p2 = new UCTAI();     // Player 2
		agents.add(p1);   
		agents.add(p2);   
		
		System.out.println("\n--------- AI Data: ---------");
		System.out.println("AI #1: " + p1.friendlyName);
		System.out.println("AI #2: " + p2.friendlyName);
	

		System.out.println("\n--------- Running Test ---------");

		game.start(context);
			
		// (re)initialise our agents
		for (int p = 1; p < agents.size(); ++p)
		{
			agents.get(p).initAI(game, p);
		}
		
		// keep going until the game is over
		while (!context.trial().over())
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
		}
	}
}

