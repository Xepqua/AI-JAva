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
		
		// return true if the agent is in a dirty tile
		if(tilesWhereImIsDirty){
			int dirtyLevel=environmentPercept.getState().getDirtyAmount();
			dirtyTile.put(currentPosition, dirtyLevel);
		}
		if(tilesWhereImIsDirty && state.suck()){

			
			if(dirtyTile.containsKey(currentPosition)){
				if(dirtyTile.get(currentPosition)==1)
					dirtyTile.remove(currentPosition);
			}
			
			suckLastTime=true;
			return getActionFromName("suck");
		}
			
		System.out.println(nextDirections.size());
		
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
		
		if(currentDirection.equals(getActionFromName("suck")))
			suckLastTime=true;
		
		
		return currentDirection;

	}
	
	@Override
	protected void updateMap() {
		nextPosition=getPointToTheAction(currentPosition,currentDirection);

		TileNode nextTileNode=getTileFromPoint(nextPosition,graphMap);
		TileNode currentTileNode=getTileFromPoint(currentPosition, graphMap);
		
		if ( nextTileNode==null || (!graphMap.containsEdge(nextTileNode,currentTileNode) &&  !graphMap.containsEdge(currentTileNode,nextTileNode))) {
			if(nextTileNode==null){
				nextTileNode=new TileNode(nextPosition, false);
				graphMap.addVertex(nextTileNode);
				
				List<Point> tmp = new ArrayList<>();
				tmp.add(new Point(nextTileNode.position.x - 1, nextTileNode.position.y));
				tmp.add(new Point(nextTileNode.position.x, nextTileNode.position.y - 1));
				tmp.add(new Point(nextTileNode.position.x + 1, nextTileNode.position.y));
				tmp.add(new Point(nextTileNode.position.x, nextTileNode.position.y + 1));
				
				for(Point p:tmp){
					TileNode nearTileNode=getTileFromPoint(p, graphMap);
					if(nearTileNode!=null && graphMap.containsVertex(nearTileNode)){
						graphMap.addEdge(  nextTileNode, nearTileNode);
					}
				}
			}
			
			if(!currentTileNode.position.equals(nextTileNode.position)){
				graphMap.addEdge(currentTileNode, nextTileNode);
			}
		}

	}
	
	@Override
	protected void changeState() {

		if (isOnTheBase && state instanceof FindBaseAgentState) {
			if(baseLocation==null)
				baseLocation = (Point) currentPosition.clone();
			
			if(therIsNotCluster()){
				state=new CheckBeforeMovesLevelDirtyAgentState(this);
			}else{
				state=new CleanClusterAgentState(this);
				return ;
			}
		}
		
		if(state instanceof CleanClusterAgentState && nextDirections.size()==0){
			state=new CheckBeforeMovesLevelDirtyAgentState(this);
		}
	}


	private boolean therIsNotCluster() {
//		if(dirtyTile.size()==0)
//			return true;
		
		
//		for(Cluster c:clusters){
//			System.out.println("CLUSTER "+c.getDistance());
//			for(Point p:c.getPoints())
//				System.out.println(p);
//			System.out.println();
//		}
		
		return true;
	}

	@Override
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
			tilesWhereImIsDirty=true;
		}else{
			tilesWhereImIsDirty=false;
		}
		
		
		
		

	}
	
	

	

}

