package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import core.VacuumEnvironment.LocationState;
import aima.core.agent.Action;

public class SearchSuckAgentState implements VacuumAgentSate {

	private PeluriaVacuumAgentProgramv2 agent;

	public SearchSuckAgentState(PeluriaVacuumAgentProgramv2 agent) {
		this.agent = agent;
	}

	@Override
	public LinkedList<Action> generatePath() {
		LinkedList<Action> nextDirections = new LinkedList<Action>();

		Action currentDirection = agent.getCurrentDirection();
		Point currentPosition = agent.getCurrentPosition();

		Point nextPoint = agent.getPointToTheAction(currentPosition, currentDirection);

		if (agent.isMovedLastTime() && agent.getTileFromPoint(nextPoint, agent.getGraphMap()) == null)
			nextDirections.add(currentDirection);
		else {

			List<Point> unvisitedPoint = neighborhoodUnvisited(getTileFromPoint(currentPosition, agent.getGraphMap()));
			if (unvisitedPoint.size() != 0) {
				Random r = new Random();
				currentDirection = actionToThePoint(unvisitedPoint.get(r.nextInt(unvisitedPoint.size())), currentPosition);
				nextDirections.add(currentDirection);
			} else {

				unvisitedPoint = getTotalUnvisitedPoint();
				Point nearestUnvisited = getNearestUnvisitedPoint(unvisitedPoint);
				nextDirections = getNextDirectionFromPoint(nearestUnvisited);

			}

		}

		return nextDirections;
	}

	@Override
	public boolean suck() {
		return true;
	}

	private TileNode getTileFromPoint(Point p, UndirectedGraph<TileNode, DefaultEdge> graph) {
		for (TileNode node : graph.vertexSet()) {
			if (node.position.equals(p))
				return node;
		}
		return null;
	}

	// FIXME ALESSANDRA
	private LinkedList<Action> getNextDirectionFromPoint(Point nearestUnvisited) {
		// TODO RItorna le prossime direzioni per arrivare al punto
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
		TileNode currentTileNode = getTileFromPoint(agent.getCurrentPosition(), graphNoObstacle);
		TileNode nearestUnvisitedTileNode = getTileFromPoint(nearestUnvisited, graphNoObstacle);
		DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graphNoObstacle, currentTileNode, nearestUnvisitedTileNode);
		LinkedList<Action> currentActionPath = new LinkedList<Action>();
		LinkedList<Point> currentPointPath = new LinkedList<Point>();

		// costruct array list path of point
		List<DefaultEdge> edgeList = path.getPathEdgeList();
		for (DefaultEdge e : edgeList) {
			TileNode t1 = new TileNode();
			TileNode t2 = new TileNode();
			getTileNodeFromEdge(e, t1, t2);
			if (currentActionPath.size() == 0) {
				if (t1.position.equals(currentTileNode.position)){
					currentActionPath.add(actionToThePoint(t2.position, t1.position));
					currentPointPath.add(t2.position);
				}else{
					currentActionPath.add(actionToThePoint(t1.position, t2.position));
					currentPointPath.add(t1.position);
				}
			} else {
				if (currentPointPath.getLast().equals(t1.position)){
					currentActionPath.add(actionToThePoint(t2.position, t1.position));
					currentPointPath.add(t2.position);
				}else{
					currentActionPath.add(actionToThePoint(t1.position, t2.position));
					currentPointPath.add(t1.position);
				}
			}
		}


		return currentActionPath;

	}

	private UndirectedGraph<TileNode, DefaultEdge> cloneGraph() {
// 		metodo visto da internet per copiare		
//		UndirectedGraph<TileNode, DefaultEdge> graph_temp = (UndirectedGraph<TileNode, DefaultEdge>) ((AbstractBaseGraph)agent.getGraphMap()).clone();

		UndirectedGraph<TileNode, DefaultEdge> graph_temp = new SimpleGraph<>(DefaultEdge.class);
		for (TileNode tileNode : agent.getGraphMap().vertexSet())
			graph_temp.addVertex((TileNode) tileNode.clone());

		for (DefaultEdge e : agent.getGraphMap().edgeSet()) {
			TileNode t1 = new TileNode();
			TileNode t2 = new TileNode();
			getTileNodeFromEdge(e, t1, t2);
			graph_temp.addEdge(getTileFromPoint(t1.position, graph_temp),getTileFromPoint(t2.position, graph_temp));

		}

		return graph_temp;
	}

	private void removeObstacleFromGraph(UndirectedGraph<TileNode, DefaultEdge> graph_temp) {
		ArrayList<TileNode> tileNodeToRemove = new ArrayList<TileNode>();
		for (TileNode tileNode : graph_temp.vertexSet()) {
			if (tileNode.TileType == LocationState.Obstacle) {
				for (TileNode tileNode2 : graph_temp.vertexSet()) {
					if (graph_temp.containsEdge(tileNode, tileNode2)) {
						graph_temp.removeEdge(tileNode,tileNode2);
					}else if(graph_temp.containsEdge(tileNode2, tileNode)){
						graph_temp.removeEdge(tileNode2,tileNode);
					}
				}
				// provare se senza questo for si rimuovono anche gli archi
				// eliminando solo il vertice

				tileNodeToRemove.add(tileNode);
			}
		}
		for (TileNode tileRemoved : tileNodeToRemove)
			graph_temp.removeVertex(tileRemoved);
	}

	// FIXME CARMELO
	private Point getNearestUnvisitedPoint(List<Point> unvisitedPoint) {
		// TODO Ritorna il punto con path minimo rispetto la posizione
		// dell'agente
		// va calcolato un nuovo grafo con punti e eliminando gli ostacoli
		// findNearestDirtyTiles programma 1
		/*
		 * Si calcola il nuovo grafo eliminando gli ostacoli il Path è calcolato
		 * da currentPosition a ogni punto di univisitedPoint ritorna un punto
		 * di unvisitedPoint più vicino. con path più piccolo
		 */

		UndirectedGraph<TileNode, DefaultEdge> graph_temp = cloneGraph();
		
		removeObstacleFromGraph(graph_temp);

		Point pointToReturn = new Point();
		double lengthPath = Integer.MAX_VALUE;
		for (int i = 0; i < unvisitedPoint.size(); i++) {
			TileNode pointToArrive = getTileFromPoint(unvisitedPoint.get(i), graph_temp);
			TileNode currPos = getTileFromPoint(agent.getCurrentPosition(), graph_temp);
			// controllare se funziona l'equals o == tra i tilenode del grafo
			DijkstraShortestPath<TileNode, DefaultEdge> path = new DijkstraShortestPath<TileNode, DefaultEdge>(graph_temp, currPos, pointToArrive);
			if (path.getPathLength() < lengthPath) {
				lengthPath = path.getPathLength();
				pointToReturn = pointToArrive.position;
			}

		}
		return pointToReturn;
	}

	// FIXME CARMELO
	private List<Point> getTotalUnvisitedPoint() {
		// TODO Ritorna una lista di punti non obstacle che hanno dei vicini non
		// visitati

		List<Point> pointWhereThereAreNeighborhoodUnvisited = new ArrayList<Point>();
		for (TileNode tileNode : agent.getGraphMap().vertexSet()) {
			if (tileNode.TileType != LocationState.Obstacle) {
				if (neighborhoodUnvisited(tileNode).size() > 0) {
					pointWhereThereAreNeighborhoodUnvisited.add(tileNode.position);
				}
			}
		}

		return pointWhereThereAreNeighborhoodUnvisited;
	}

	private List<Point> neighborhoodUnvisited(TileNode tileNode) {
		List<Point> returnList = new ArrayList<Point>();
		boolean unvisited = false;
		List<Point> tmp = new ArrayList<>();
		tmp.add(new Point(tileNode.position.x - 1, tileNode.position.y));
		tmp.add(new Point(tileNode.position.x, tileNode.position.y - 1));
		tmp.add(new Point(tileNode.position.x + 1, tileNode.position.y));
		tmp.add(new Point(tileNode.position.x, tileNode.position.y + 1));
		for (Point point : tmp) {
			unvisited = true;
			for (TileNode node : agent.getGraphMap().vertexSet()) {
				if (node.position.equals(point)) {
					unvisited = false;
					break;
				}
			}
			if (unvisited) {
				returnList.add(point);
			}
		}
		return returnList;
	}

	private Action actionToThePoint(Point nextPosition, Point currentPosition) {
		if (nextPosition.x == currentPosition.x + 1)
			return agent.getActionFromName("down");
		if (nextPosition.x == currentPosition.x - 1)
			return agent.getActionFromName("up");
		if (nextPosition.y == currentPosition.y + 1)
			return agent.getActionFromName("right");
		if (nextPosition.y == currentPosition.y - 1)
			return agent.getActionFromName("left");
		return null;
	}

	// get TileNode from Edge
	private void getTileNodeFromEdge(DefaultEdge e, TileNode t1, TileNode t2) {
		String edgeStrings[] = e.toString().split(":");
		int x1 = Integer.valueOf(edgeStrings[0].substring(edgeStrings[0].indexOf("x= ") + 3, edgeStrings[0].indexOf(" y")));
		int y1 = Integer.valueOf(edgeStrings[0].substring(edgeStrings[0].indexOf("y= ") + 3, edgeStrings[0].indexOf(" type=")));
		String state1 = edgeStrings[0].substring(edgeStrings[0].indexOf("type= ") + 3, edgeStrings[0].length());

		t1.position.x = x1;
		t1.position.y = y1;
		t1.TileType = LocationState.Clean;
		if (state1.equals("Obstacle"))
			t1.TileType = LocationState.Obstacle;

		int x2 = Integer.valueOf(edgeStrings[1].substring(edgeStrings[1].indexOf("x= ") + 3, edgeStrings[1].indexOf(" y")));
		int y2 = Integer.valueOf(edgeStrings[1].substring(edgeStrings[1].indexOf("y= ") + 3, edgeStrings[1].indexOf(" type=")));
		String state2 = edgeStrings[1].substring(edgeStrings[1].indexOf("type= ") + 3, edgeStrings[1].length());
		t2.position.x = x2;
		t2.position.y = y2;
		t2.TileType = LocationState.Clean;
		if (state2.contains("Obstacle"))
			t2.TileType = LocationState.Obstacle;

	}

}
