package agent;

import java.awt.Point;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import core.VacuumEnvironment.LocationState;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;

public class PeluriaVacuumAgentProgramv2 implements AgentProgram {
	
	
	private Action currentDirection;
	private Point previusPosition;
	private Point currentPosition;
	
	private UndirectedGraph<TileNode, DefaultEdge> graphMap;



	@Override
	public Action execute(Percept percept) {
		
		if(isInDirtyTile())
			return getActionFromName("suck");
		
		if(thereIsObstacle())
			changeDirection();
		
		updateMap();
		
		return currentDirection; 
		
	}
	
	private void updateMap() {
		
	}

	private void changeDirection() {
		
	}

	private boolean thereIsObstacle() {
		return false;
	}

	private boolean isInDirtyTile() {
		return false;
	}

	public Action getActionFromName(final String actionName) {


		return null;

	}
	


}

class TileNode{
	public Point position;
	public LocationState TileType;
}
