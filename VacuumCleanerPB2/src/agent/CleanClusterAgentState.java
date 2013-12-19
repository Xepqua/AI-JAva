package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
		
		ArrayList<Cluster> clusters=KMeansDirtyTile.getBestClusters(agent.getDirtyTileFind());
		
		SortedSet<Cluster> orderedClusters=new TreeSet<>(clusters);
		
		Point lastPoint=agent.getCurrentPosition();
		double energy=agent.getCurrentEnergy();
		System.out.println("CLESTER FIND "+orderedClusters.size());
		
		for(Cluster c:orderedClusters){
			ArrayList<Action> nextCleanClusterAction=new ArrayList<Action>();
			System.out.println("START "+lastPoint);
			if(canCleanCluster(c,nextCleanClusterAction,lastPoint,energy)){
				nextDirections.addAll(nextCleanClusterAction);
				energy-=nextCleanClusterAction.size();
				System.out.println("SIZE CLUSTER "+c.getPoints().size());
				break;
			}
				
		}
		
		agent.getDirtyTileFind().clear();
		
		
		return nextDirections;
		
	}
	
	@Override
	public boolean suck() {
		return false;
	}


	private boolean canCleanCluster(Cluster c, ArrayList<Action> nextCleanClusterAction, Point lastPoint,double energySpent) {
		
		ArrayList<Point> points=c.getPoints();
		Point currentPosition=(Point) lastPoint.clone();
		double currentEnergy=energySpent;
		
		while(points.size()>0){
			
			Point nextPoint=getNearestPoint(points, currentPosition);
			LinkedList<Action> nextDirectionsToCleanPoint=getNextDirectionFromPoint(nextPoint, currentPosition);
			double energyToSpent=nextDirectionsToCleanPoint.size()+agent.getEnergyToClean(nextPoint);
			if(energyToSpent<currentEnergy){
				
				currentEnergy-=energyToSpent;
				currentPosition=nextPoint;
				points.remove(nextPoint);
				nextCleanClusterAction.addAll(nextDirectionsToCleanPoint);
				for(int i=0;i<agent.getEnergyToClean(nextPoint);i++)
					nextCleanClusterAction.add(agent.getActionFromName("suck"));
				
				
			}else
				return false;
			
		}
		lastPoint.x=currentPosition.x;
		lastPoint.y=currentPosition.y;
		return true;
	}


	protected double movesToReturnBase(Point currentPosition) {
		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();

		removeObstacleFromGraph(graph_temp);
//		System.out.println("START "+currentPosition+" "+agent.getBaseLocation());
//		System.out.println(agent.getGraphMap().containsVertex(getTileFromPoint(currentPosition, graph_temp))+" "+agent.getGraphMap().containsVertex(getTileFromPoint(agent.getBaseLocation(), graph_temp)));

		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graph_temp, getTileFromPoint(currentPosition, graph_temp), getTileFromPoint(agent.getBaseLocation(), graph_temp));
		
		return path.getPathLength();

	}
	
	
	protected Point getNearestPoint(List<Point> unvisitedPoint,Point currentPosition) {
		/*
		 * Ritorna il punto con path minimo rispetto la posizione dell'agente va
		 * calcolato un nuovo grafo con punti e eliminando gli ostacoli
		 * findNearestDirtyTiles programma 1
		 * 
		 * Si calcola il nuovo grafo eliminando gli ostacoli il Path è calcolato
		 * da currentPosition a ogni punto di univisitedPoint ritorna un punto
		 * di unvisitedPoint più vicino. con path più piccolo
		 */

		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();

		removeObstacleFromGraph(graph_temp);

		Point pointToReturn = new Point();
		double lengthPath = Integer.MAX_VALUE;
		for (int i = 0; i < unvisitedPoint.size(); i++) {
			TileNode pointToArrive = getTileFromPoint(unvisitedPoint.get(i),
					graph_temp);
			TileNode currPos = getTileFromPoint(currentPosition,
					graph_temp);
			// controllare se funziona l'equals o == tra i tilenode del grafo
			DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(
					graph_temp, currPos, pointToArrive);
			if (path.getPathLength() < lengthPath) {
				lengthPath = path.getPathLength();
				pointToReturn = pointToArrive.position;
			}

		}
		return pointToReturn;
	}
	
	protected LinkedList<Action> getNextDirectionFromPoint(Point nearestUnvisited,Point currentPosition) {
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
		UndirectedGraph<TileNode, DefaultEdge> graphNoObstacle = cloneGraph();
		removeObstacleFromGraph(graphNoObstacle);
		TileNode currentTileNode = getTileFromPoint(currentPosition,
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
