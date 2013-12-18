package agent;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	private double energyToSpent;
	
	private HashMap<Point, Integer> dirtyTile=new HashMap<Point, Integer>();

	@Override
	public Action execute(Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

		InformationByEnvironment(environmentPercept);
		if (currentPosition == null) {
			energyToSpent=currentEnergy;
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
		printGraph(graphMap);
		System.out.println(currentEnergy);

		if(currentEnergy==0){
			return getNoOpAction();
		}
		
		if(currentDirection.equals(getActionFromName("suck"))){
			suckLastTime=true;
			energyToSpent-=actionEnergyCosts.get(currentDirection);
			int levelCurrentTile=dirtyTile.get(currentPosition);
			if(levelCurrentTile>1)
				dirtyTile.put(currentPosition, levelCurrentTile-1);
			else
				dirtyTile.remove(currentPosition);
		}else
			energyToSpent-=actionEnergyCosts.get(currentDirection);
		
		
		return currentDirection;

	}
	

	private void changeState() {
		if(isOnTheBase &&  state instanceof FindBaseAgentState){
			if(dirtyTile.size()>0){
				energyToSpent=getEnergyToSpentForClean();
				state=new CleanClusterAgentState(this);
			}else{
				energyToSpent=getEnergyToSpentForExplore();
				state=new CheckBeforeMovesAgentState(this);
			}
		}
		
		if (energyToSpent<=0 &&  state instanceof CheckBeforeMovesAgentState  && dirtyTile.size()>0 ){
			if(baseLocation==null)
				baseLocation = (Point) currentPosition.clone();
			state=new CleanClusterAgentState(this);
			energyToSpent=getEnergyToSpentForClean();
		}

		if(energyToSpent<=0 && state instanceof CleanClusterAgentState){
			energyToSpent=getEnergyToSpentForExplore();
			state=new CheckBeforeMovesAgentState(this);
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
				.equals(LocationState.Dirty)){
			int dirtyLevel=1;
			dirtyTile.put(currentPosition, dirtyLevel);
			
		}
		
		
		
		

	}


	public double getEnergyToSpent() {
		return energyToSpent;
	}

	public double getEnergyToSpentForExplore(){
		return 0;
	}
	
	public double getEnergyToSpentForClean(){
		return 0;
	}
	
	
	public int getEnergyToClean(Point p){
		return dirtyTile.get(p);
	}
	

}

