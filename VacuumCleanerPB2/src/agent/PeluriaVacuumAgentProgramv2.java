package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.DynamicAction;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;

enum AgentStatus {
	FindBase, SuckAndSearch, ReturnedBase, CheckBefore
}

public class PeluriaVacuumAgentProgramv2 implements AgentProgram {

	private Action currentDirection;
	private LinkedList<Action> nextDirections = new LinkedList<>();

	private Point baseLocation;
	private Point currentPosition;
	private Point nextPosition;
	private double currentEnergy;
	private AgentStatus status = AgentStatus.SuckAndSearch;

	// dimension of enviroment
	private int N;
	private int M;

	private boolean tilesWhereImIsDirty = false;
	private UndirectedGraph<TileNode, DefaultEdge> graphMap = new SimpleGraph<>(
			DefaultEdge.class);

	private Map<Action, Double> actionEnergyCosts;
	private boolean isMovedLastTime;

	@Override
	public Action execute(Percept percept) {

		final LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept = (LocalVacuumEnvironmentPerceptTaskEnvironmentB) percept;

		InformationByEnvironment(environmentPercept);
		if (currentPosition == null) {
			currentDirection = getActionFromName("up");
			currentPosition = new Point(N / 2, M / 2);
			TileNode tileInitial = new TileNode(currentPosition, false);
			graphMap.addVertex(tileInitial);
		} else {
			if (!isMovedLastTime()) {
				updateVertexOnGraph(nextPosition, LocationState.Obstacle);
			} else {
				currentPosition = nextPosition;
			}
		}

		// return true if the agent is in a dirty tile
		if (tilesWhereImIsDirty) {
			return getActionFromName("suck");
		}

		// return true if in current direction there is an obstacle
		if (!isMovedLastTime() && nextDirections.size() == 0)
			changeDirection();
		else {
			currentDirection = nextDirections.pollFirst();
		}

		// add in a graph a tile
		updateMap();

		return currentDirection;

	}

	// Print the grahMap
	private void printGraph() {
		System.out.println("---------START-------");
		int maxN = N * 3, maxM = M * 3;
		int m[][] = new int[maxN][maxM];
		for (int i = 0; i < maxN; i++)
			for (int j = 0; j < maxM; j++)
				m[i][j] = 0;

		for (TileNode t : graphMap.vertexSet()) {

			if (t.TileType == LocationState.Obstacle)
				m[t.position.x + N][t.position.y + M] = 2;
			else
				m[t.position.x + N][t.position.y + M] = 1;

		}

		for (int i = 0; i < maxN; i++) {
			for (int j = 0; j < maxM; j++)
				if (i - N == currentPosition.x && j - M == currentPosition.y)
					System.out.print("@");
				else if (m[i][j] == 1)
					System.out.print("o");
				else if (m[i][j] == 0)
					System.out.print(" ");
				else
					System.out.print("#");
			System.out.println("");
		}
		System.out.println("-----------END--------");

	}

	// get TileNode from Edge
	private void getTileNodeFromEdge(DefaultEdge e, TileNode t1, TileNode t2) {
		String edgeStrings[] = e.toString().split(":");
		int x1 = Integer
				.valueOf(edgeStrings[0].substring(
						edgeStrings[0].indexOf("x= ") + 3,
						edgeStrings[0].indexOf(" y")));
		int y1 = Integer.valueOf(edgeStrings[0].substring(
				edgeStrings[0].indexOf("y= ") + 3,
				edgeStrings[0].indexOf(" type=")));
		String state1 = edgeStrings[0].substring(
				edgeStrings[0].indexOf("type= ") + 3, edgeStrings[0].length());

		t1.position.x = x1;
		t1.position.y = y1;
		t1.TileType = LocationState.Clean;
		if (state1.equals("Obstacle"))
			t1.TileType = LocationState.Obstacle;

		int x2 = Integer
				.valueOf(edgeStrings[1].substring(
						edgeStrings[1].indexOf("x= ") + 3,
						edgeStrings[1].indexOf(" y")));
		int y2 = Integer.valueOf(edgeStrings[1].substring(
				edgeStrings[1].indexOf("y= ") + 3,
				edgeStrings[1].indexOf(" type=")));
		String state2 = edgeStrings[1].substring(
				edgeStrings[1].indexOf("type= ") + 3, edgeStrings[1].length());
		t2.position.x = x2;
		t2.position.y = y2;
		t2.TileType = LocationState.Clean;
		if (state2.contains("Obstacle"))
			t2.TileType = LocationState.Obstacle;

	}

	private void InformationByEnvironment(
			LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept) {

		N = environmentPercept.getN();
		M = environmentPercept.getM();
		actionEnergyCosts = environmentPercept.getActionEnergyCosts();
		isMovedLastTime = environmentPercept.isMovedLastTime();

		if (environmentPercept.isOnBase()) {
			baseLocation = (Point) currentPosition.clone();
		}

		currentEnergy = environmentPercept.getCurrentEnergy();

		if (environmentPercept.getState().getLocState()
				.equals(LocationState.Dirty))
			tilesWhereImIsDirty = true;
		else
			tilesWhereImIsDirty = false;

	}

	private void updateVertexOnGraph(Point p, LocationState state) {
		for (TileNode tileNode : graphMap.vertexSet()) {
			if (tileNode.position.equals(p)) {
				tileNode.TileType = state;
			}
		}
	}

	public boolean isMovedLastTime() {
		return isMovedLastTime;
	}

	public void setMovedLastTime(boolean isMovedLastTime) {
		this.isMovedLastTime = isMovedLastTime;
	}

	private void updateMap() {
		nextPosition = (Point) currentPosition.clone();
		if (currentDirection.equals(getActionFromName("up"))) {
			nextPosition.x = currentPosition.x - 1;
		} else if (currentDirection.equals(getActionFromName("down"))) {
			nextPosition.x = currentPosition.x + 1;
		} else if (currentDirection.equals(getActionFromName("left"))) {
			nextPosition.y = currentPosition.y - 1;
		} else if (currentDirection.equals(getActionFromName("right"))) {
			nextPosition.y = currentPosition.y + 1;
		}

		if (getTileFromPoint(nextPosition,graphMap) == null) {
			TileNode nextTileNode = new TileNode(nextPosition, false);
			graphMap.addVertex(nextTileNode);
			graphMap.addEdge(getTileFromPoint(currentPosition,graphMap), nextTileNode);
		}

	}

	private TileNode getTileFromPoint(Point p, UndirectedGraph<TileNode, DefaultEdge> graph) {
		for (TileNode node : graph.vertexSet()) {
			if (node.position.equals(p))
				return node;
		}
		return null;
	}

	private void changeDirection() {

		List<Point> unvisitedPoint = neighborhoodUnvisited(getTileFromPoint(currentPosition,graphMap));
		if (unvisitedPoint.size() != 0) {
			Random r = new Random();
			currentDirection = pointToTheAction(unvisitedPoint.get(r
					.nextInt(unvisitedPoint.size())),currentPosition);
		} else {

			unvisitedPoint = getTotalUnvisitedPoint();
			Point nearestUnvisited = getNearestUnvisitedPoint(unvisitedPoint);
			nextDirections = getNextDirectionFromPoint(nearestUnvisited);

		}

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
		TileNode currentTileNode=getTileFromPoint(currentPosition,graphNoObstacle);
		TileNode nearestUnvisitedTileNode=getTileFromPoint(nearestUnvisited,graphNoObstacle);
		DijkstraShortestPath<TileNode, DefaultEdge> path=new DijkstraShortestPath<TileNode, DefaultEdge>(graphNoObstacle,currentTileNode ,nearestUnvisitedTileNode );
		LinkedList<Action> currentPath=new LinkedList<Action>();
			
		//costruct array list path of point 
		List<DefaultEdge> edgeList=path.getPathEdgeList();
		for(DefaultEdge e:edgeList){
			TileNode t1=new TileNode();
			TileNode t2=new TileNode();
			getTileNodeFromEdge(e, t1, t2);
			if(currentPath.size()==0){
						if(t1.equals(currentTileNode))
							currentPath.add(pointToTheAction(t2.position, t1.position));
						else
							currentPath.add(pointToTheAction(t1.position, t2.position));
					}else{
						if(currentPath.get(currentPath.size()-1).equals(t1))
							currentPath.add(pointToTheAction(t2.position, t1.position));
						else
							currentPath.add(pointToTheAction(t1.position, t2.position));
					}
				}
										
		return currentPath;

	}
	
	private UndirectedGraph<TileNode, DefaultEdge> cloneGraph(){
		UndirectedGraph<TileNode, DefaultEdge> graph_temp = new SimpleGraph<TileNode, DefaultEdge>(DefaultEdge.class);
		for(TileNode tileNode:graphMap.vertexSet())
			graph_temp.addVertex((TileNode) tileNode.clone());

		for(DefaultEdge e:graphMap.edgeSet()){
			TileNode t1=new TileNode();
			TileNode t2=new TileNode();
			getTileNodeFromEdge(e, t1, t2);
			graph_temp.addEdge(t1, t2);
		}
		
		return graph_temp;
	}
	
	private void removeObstacleFromGraph(UndirectedGraph<TileNode, DefaultEdge> graph_temp){
		for (TileNode tileNode : graph_temp.vertexSet()) {
			if (tileNode.TileType == LocationState.Obstacle) {
				ArrayList<TileNode> nodeNeigh = new ArrayList<>();
				for (TileNode tileNode2 : graph_temp.vertexSet()) {
					if (graph_temp.containsEdge(tileNode, tileNode2)) {
						nodeNeigh.add(tileNode2);
					}
				}
				// provare se senza questo for si rimuovono anche gli archi
				// eliminando solo il vertice
				for (int i = 0; i < nodeNeigh.size(); i++) {
					graph_temp.removeEdge(tileNode, nodeNeigh.get(i));
				}
				graph_temp.removeVertex(tileNode);
			}
		}
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
			TileNode pointToArrive = getTileFromPoint(unvisitedPoint.get(i),graph_temp);
			TileNode currPos = getTileFromPoint(currentPosition,graph_temp);
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

	

	// FIXME CARMELO
	private List<Point> getTotalUnvisitedPoint() {
		// TODO Ritorna una lista di punti non obstacle che hanno dei vicini non
		// visitati

		List<Point> pointWhereThereAreNeighborhoodUnvisited = new ArrayList<Point>();
		if (graphMap != null) {
			for (TileNode tileNode : graphMap.vertexSet()) {
				if (tileNode.TileType != LocationState.Obstacle) {
					if (neighborhoodUnvisited(tileNode).size() > 0) {
						pointWhereThereAreNeighborhoodUnvisited
								.add(tileNode.position);
					}
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
			for (TileNode node : graphMap.vertexSet()) {
				if (node.position.equals(point)) {
					unvisited = false;
					break;
				}
			}
			if(unvisited){
				returnList.add(point);
			}
		}
		return returnList;
	}


	private Action pointToTheAction(Point pollFirst,Point nearPoint) {
		if (pollFirst.x == nearPoint.x + 1)
			return getActionFromName("down");
		if (pollFirst.x == nearPoint.x - 1)
			return getActionFromName("up");
		if (pollFirst.y == nearPoint.y + 1)
			return getActionFromName("right");
		if (pollFirst.y == nearPoint.y - 1)
			return getActionFromName("left");
		return null;
	}

	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName().equals(actionName))
				return a;

		return null;
	}

}

class TileNode {
	public Point position;
	public LocationState TileType;

	public TileNode() {
		position = new Point();
		TileType = LocationState.Clean;
	}

	public TileNode(Point p, boolean isObstacle) {
		this.position = p;
		if (isObstacle) {
			TileType = LocationState.Obstacle;
		} else
			TileType = LocationState.Clean;
	}
	
	@Override
	protected Object clone()  {
		TileNode t=new TileNode();
		t.position=(Point) this.position.clone();
		t.TileType=this.TileType;
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((TileType == null) ? 0 : TileType.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TileNode other = (TileNode) obj;
		if (TileType != other.TileType)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "x= " + position.x + " y= " + position.y + " type= "
				+ TileType.name();
	}
}
