package agent;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	
	
	@Override
	public Action execute(Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

		InformationByEnvironment(environmentPercept);
		if (currentPosition == null) {
			state=new FindBaseAgentState(this);
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

		
		if(nextDirections.size()<=0)
			nextDirections=state.generatePath();
		
		
		suckLastTime=false;
			
		currentDirection = nextDirections.pollFirst();

		// add in a graph a tile
		updateMap();
//		printGraph(graphMap);
//		System.out.println(currentEnergy);

		if(currentEnergy==0){
			return getNoOpAction();
		}
		
		if(currentDirection.equals(getActionFromName("suck"))){
			suckLastTime=true;
			int levelCurrentTile=dirtyTile.get(currentPosition);
			if(levelCurrentTile>1)
				dirtyTile.put(currentPosition, levelCurrentTile-1);
			else
				dirtyTile.remove(currentPosition);
		}
		
		return currentDirection;

	}
	
	private void changeState() {

		if (isOnTheBase && state instanceof FindBaseAgentState) {
			if(baseLocation==null)
				baseLocation = (Point) currentPosition.clone();
			
			if(therIsNotCluster()){
				state=new CheckBeforeMovesAgentState(this);
			}else{
				state=new CleanClusterAgentState(this);
			}
		}
		
		if(!therIsNotCluster() && state instanceof CheckBeforeMovesAgentState)
			state=new CleanClusterAgentState(this);
		
	}


	private boolean therIsNotCluster() {
		return true;
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
				.equals(LocationState.Dirty)){
			int dirtyLevel=1;
			dirtyTile.put(currentPosition, dirtyLevel);
			
		}
		
		
		
		

	}
	
	

	

}

