package agent;

import java.util.LinkedList;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import aima.core.agent.Action;

public class CheckBeforeMovesAgentState extends SearchSuckAgentState {

	public CheckBeforeMovesAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public LinkedList<Action> generatePath() {
		double currentEnergy= agent.getCurrentEnergy();
		LinkedList<Action> nextDirection = super.generatePath();
		if(nextDirection.size()+movesToReturnBase()<=currentEnergy)
			return nextDirection;
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		
		for(Action action:returnBase.generatePath()){
			System.out.println(action);
		}
		
		return returnBase.generatePath();
	}
	
	@Override
	public boolean suck() {
		double currentEnergy= agent.getCurrentEnergy();
		double suckCost=agent.getActionEnergyCosts().get(agent.getActionFromName("suck"));
		if( suckCost + movesToReturnBase() <= currentEnergy)
			return true;
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		agent.setNextDirections(returnBase.generatePath());
		return false;
	}

	private double movesToReturnBase() {
		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();

		removeObstacleFromGraph(graph_temp);

		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graph_temp, getTileFromPoint(agent.getCurrentPosition(), graph_temp), getTileFromPoint(agent.getBaseLocation(), graph_temp));
		
		return path.getPathLength();

	}

}
