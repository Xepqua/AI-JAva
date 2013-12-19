package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import aima.core.agent.Action;

public class CheckBeforeMovesLevelDirtyAgentState extends SearchSuckAgentState {

	public CheckBeforeMovesLevelDirtyAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public LinkedList<Action> generatePath() {

		double currentEnergy= agent.getCurrentEnergy();
		LinkedList<Action> nextDirection=null;
		
		if(agent.getDirtyTileFind().size()>0){
			
			HashMap<LinkedList<Action>, Integer> pointPath=new HashMap<LinkedList<Action>, Integer>();
			UndirectedGraph<TileNode, DefaultEdge> graphNoObstacle = cloneGraph();
			removeObstacleFromGraph(graphNoObstacle);
			for(Point dirtyPoint:agent.getDirtyTileFind()){
				
				LinkedList<Action> actionPoints=getNextDirectionFromPoint(dirtyPoint,graphNoObstacle);
//				System.out.println(agent.getEnergyToClean(dirtyPoint)+ " " + dirtyPoint);
				pointPath.put(actionPoints, actionPoints.size()+agent.getEnergyToClean(dirtyPoint));
				
			}
			
			ArrayList<LinkedList<Action>> bestNextDirections=new ArrayList<LinkedList<Action>>();
			int best=0;
			for(LinkedList<Action> path:pointPath.keySet()){
				int cost=pointPath.get(path);
				if(bestNextDirections.size()==0){
					best=cost;
//					System.out.println("bestNextDirections.size()==0 ::::::" + best);
					
					bestNextDirections.add(path);
				}else if (cost < best){
					best = cost;
					bestNextDirections.clear();
					bestNextDirections.add(path);
//					System.out.println("pointPath.get(path) < best ::::::" + best);
					
				}else if (cost == best){
					bestNextDirections.add(path);
//					System.out.println("pointPath.get(path) == best ::::::" + best);
				}
			}
			Random random = new Random();
			int rand = random.nextInt(bestNextDirections.size());
			nextDirection=bestNextDirections.get(rand);
		}else
			nextDirection = super.generatePath();

		if(nextDirection!=null){
			
			if(nextDirection.size()+movesToReturnBase()<currentEnergy){
				return nextDirection;
			}

		}
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		
		return returnBase.generatePath();
	}
	
	@Override
	public boolean suck() {
		double currentEnergy= agent.getCurrentEnergy();
		double suckCost=agent.getActionEnergyCosts().get(agent.getActionFromName("suck"));
		if( suckCost*agent.getEnergyToClean(agent.getCurrentPosition()) + movesToReturnBase() < currentEnergy)
			return true;
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		agent.setNextDirections(returnBase.generatePath());
		return false;
	}
	
	protected double movesToReturnBase() {
		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();

		removeObstacleFromGraph(graph_temp);

		
		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graph_temp, getTileFromPoint(agent.getCurrentPosition(), graph_temp), getTileFromPoint(agent.getBaseLocation(), graph_temp));
		
		return path.getPathLength();

	}
	
	
	
	protected LinkedList<Action> getNextDirectionFromPoint(Point nearestUnvisited,UndirectedGraph<TileNode, DefaultEdge> graphNoObstacle) {
		// Ritorna le prossime direzioni per arrivare al punto
		// nearestUnvisited
		/*
		 * Vecchio agente return to the base
		 * 
		 * deve calcolare djikstra dalla current position al punto
		 * nearestUnvisited Una volta calcolato il percorso deve calcolare la
		 * lista delle direzioni Trasforma la lista dei punti in lista di
		 * direzioni
		 */

		TileNode currentTileNode = getTileFromPoint(agent.getCurrentPosition(),
				graphNoObstacle);
		TileNode nearestUnvisitedTileNode = getTileFromPoint(nearestUnvisited,
				graphNoObstacle);
		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(
				graphNoObstacle, currentTileNode, nearestUnvisitedTileNode);
		LinkedList<Action> currentActionPath = new LinkedList<Action>();
		LinkedList<Point> currentPointPath = new LinkedList<Point>();

		// costruct array list path of point
		List<DefaultEdge> edgeList = path.getPathEdgeList();
		for (DefaultEdge e : edgeList) {
			TileNode t1 = new TileNode();
			TileNode t2 = new TileNode();
			getTileNodeFromEdge(e, t1, t2);
			if (currentActionPath.size() == 0) {
				if (t1.position.equals(currentTileNode.position)) {
					currentActionPath.add(actionToThePoint(t2.position,
							t1.position));
					currentPointPath.add(t2.position);
				} else {
					currentActionPath.add(actionToThePoint(t1.position,
							t2.position));
					currentPointPath.add(t1.position);
				}
			} else {
				if (currentPointPath.getLast().equals(t1.position)) {
					currentActionPath.add(actionToThePoint(t2.position,
							t1.position));
					currentPointPath.add(t2.position);
				} else {
					currentActionPath.add(actionToThePoint(t1.position,
							t2.position));
					currentPointPath.add(t1.position);
				}
			}
		}

		return currentActionPath;

	}

}
