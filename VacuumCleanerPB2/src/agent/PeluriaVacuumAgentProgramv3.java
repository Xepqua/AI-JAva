package agent;


import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.DynamicAction;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;


public class PeluriaVacuumAgentProgramv3 extends PeluriaVacuumAgentProgramv2 implements AgentProgram {

	
	//Ricerca base
	//Sparserizza le celle usando un tot dell'energia
	//Esplore
	//vai passo 2
	
	//Ad ogni azione che fai controlla che poi puoi tornare alla base
	


	@Override
	public Action execute(Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

		InformationByEnvironment(environmentPercept);
		if (currentPosition == null) {
			state=new SearchSuckAgentState(this);
			currentDirection = getActionFromName("up");
			nextDirections.add(currentDirection);
			currentPosition = new Point(N / 2, M / 2);
			TileNode tileInitial = new TileNode(currentPosition, false);
			graphMap.addVertex(tileInitial);
		} else {
			if (!isMovedLastTime() && !suckLastTime) {
				updateVertexOnGraph(nextPosition, LocationState.Obstacle);
			} else if(!suckLastTime){
				currentPosition = nextPosition;
			}
		}
		
		changeState();


		// return true if the agent is in a dirty tile
		if(tilesWhereImIsDirty && state.suck()){
			suckLastTime=true;
			return getActionFromName("suck");
		}

		// return true if in current direction there is an obstacle
//		if (!isMovedLastTime() || nextDirections.size() == 0)
//			changeDirection();
//		else {
//			currentDirection = nextDirections.pollFirst();
//		}
		
		if(nextDirections.size()<=0)
			nextDirections=state.generatePath();
		
//		System.out.println("NEXT ACTIONS");
//		for(Action a:nextDirections)
//			System.out.println(a);
		
		suckLastTime=false;
			
		currentDirection = nextDirections.pollFirst();

		// add in a graph a tile
		updateMap();
//		printGraph(graphMap);
		System.out.println(currentEnergy);

		if(currentEnergy==0){
			return getNoOpAction();
		}
		
		return currentDirection;

	}
	

	private void changeState() {
		if (isOnTheBase && ! (state instanceof CheckBeforeMovesAgentState)) {
			if(baseLocation==null)
				baseLocation = (Point) currentPosition.clone();
			state=new CheckBeforeMovesAgentState(this);
			System.out.println("CHeck before moves");
		}

		
		if(! (state instanceof FindBaseAgentState) && ! (state instanceof CheckBeforeMovesAgentState) && ! (state instanceof ReturnBaseAgentState) && currentEnergy<20 ){
			state=new FindBaseAgentState(this);
		}
		
	}


	protected void InformationByEnvironment(
			LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept) {

		N = environmentPercept.getN();
		M = environmentPercept.getM();
		actionEnergyCosts = environmentPercept.getActionEnergyCosts();
		isMovedLastTime = environmentPercept.isMovedLastTime();
		isOnTheBase=environmentPercept.isOnBase();

		currentEnergy = environmentPercept.getCurrentEnergy();
		
		
		if (environmentPercept.getState().getLocState()
				.equals(LocationState.Dirty))
			tilesWhereImIsDirty = true;
		else
			tilesWhereImIsDirty = false;
		
		

	}

}

