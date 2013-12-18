package agent;

import java.awt.Point;
import java.util.LinkedList;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import aima.core.agent.Action;

public class CleanClusterAgentState extends SearchSuckAgentState {

	public CleanClusterAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public LinkedList<Action> generatePath() {
		LinkedList<Action> nextDirections=new LinkedList<Action>();
//		double energyToSpent=((PeluriaVacuumAgentProgramv3)agent).getEnergyToSpent();
//		
//		while(energyToSpent>0){
//			Point tileThatMaximizeDistance=getDirtyTileMaxSparsity();
//			LinkedList<Action> directionForArrive=getNextDirectionFromPoint(tileThatMaximizeDistance);
//			energyToSpent-=directionForArrive.size()+energyToClean(tileThatMaximizeDistance);
//			if(energyToSpent>0){
//				nextDirections.addAll(directionForArrive);
//				for(int i=energyToClean(tileThatMaximizeDistance);i>0;i--)
//				nextDirections.add(agent.getActionFromName("suck"));
//			}
//
//		}
		
		return nextDirections;
	}

	private int energyToClean(Point tileThatMaximizeDistance) {
		return ((PeluriaVacuumAgentProgramv3)agent).getEnergyToClean(tileThatMaximizeDistance);
	}

	private Point getDirtyTileMaxSparsity() {
		// TODO Auto-generated method stub
		return null;
	}
	

	protected double movesToReturnBase() {
		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();

		removeObstacleFromGraph(graph_temp);

		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graph_temp, getTileFromPoint(agent.getCurrentPosition(), graph_temp), getTileFromPoint(agent.getBaseLocation(), graph_temp));
		
		return path.getPathLength();

	}

}
