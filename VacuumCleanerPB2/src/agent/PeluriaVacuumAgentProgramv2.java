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
	private Point baseLocation;
	
	//dimension of enviroment
	private int N;
	
	private UndirectedGraph<TileNode, DefaultEdge> graphMap;



	@Override
	public Action execute(Percept percept) {
		
		//return true if the agent is in a dirty tile 		
		if(isInDirtyTile())
			return getActionFromName("suck");
		
		
		//return true if in current direction there is an obstacle
		if(thereIsObstacle())
			changeDirection();
		
		//add in a graph a tile
		updateMap();
		
		return currentDirection; 
		
	}
	
	private void updateMap() {
		//CARMELO
	}

	private void changeDirection() {
		//FACCIAMO INSIEME
	}

	private boolean thereIsObstacle() {
		return previusPosition.equals(currentPosition);
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
