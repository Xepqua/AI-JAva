package agent;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.DynamicAction;
import core.LocalVacuumEnvironmentPerceptTaskEnvironmentB;
import core.VacuumEnvironment.LocationState;


enum AgentStatus{
	FindBase, SuckAndSearch, ReturnedBase, CheckBefore
}
public class PeluriaVacuumAgentProgramv2 implements AgentProgram {

	private Action currentDirection;
	private LinkedList<Action> nextDirections=new LinkedList<>();
	
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
		if (!isMovedLastTime() && nextDirections.size()==0)
			changeDirection();
		else{
			currentDirection=nextDirections.pollFirst();
		}

		// add in a graph a tile
		updateMap();
		
		return currentDirection;

	}

//Print the grahMap
	private void printGraph() {
		System.out.println("---------START-------");
		int maxN=N*3,maxM=M*3;
		int m[][] = new int[maxN][maxM];
		for(int i=0;i<maxN;i++)
			for(int j=0;j<maxM;j++)
				m[i][j]=0;
		
		for(TileNode t:graphMap.vertexSet()){

			if(t.TileType==LocationState.Obstacle)
				m[t.position.x+N][t.position.y+M]=2;
			else
				m[t.position.x+N][t.position.y+M]=1;

			
		}
		
		for(int i=0;i<maxN;i++){
			for(int j=0;j<maxM;j++)
				if(i-N==currentPosition.x && j-M==currentPosition.y)
					System.out.print("@");
				else if(m[i][j]==1)
					System.out.print("o");
				else if(m[i][j]==0)
					System.out.print(" ");
				else
					System.out.print("#");
			System.out.println("");
		}
		System.out.println("-----------END--------");
		
	}
	
//get TileNode from Edge
	private void getTileNodeFromEdge(DefaultEdge e,TileNode t1,TileNode t2){
		String edgeStrings[]=e.toString().split(":");
		int x1=Integer.valueOf(edgeStrings[0].substring(edgeStrings[0].indexOf("x= ")+3,edgeStrings[0].indexOf(" y")));
		int y1=Integer.valueOf(edgeStrings[0].substring(edgeStrings[0].indexOf("y= ")+3,edgeStrings[0].indexOf(" type=")));
		String state1=edgeStrings[0].substring(edgeStrings[0].indexOf("type= ")+3,edgeStrings[0].length());

		t1.position.x=x1;
		t1.position.y=y1;
		t1.TileType=LocationState.Clean;
		if(state1.equals("Obstacle"))
			t1.TileType=LocationState.Obstacle;

		int x2=Integer.valueOf(edgeStrings[1].substring(edgeStrings[1].indexOf("x= ")+3,edgeStrings[1].indexOf(" y")));
		int y2=Integer.valueOf(edgeStrings[1].substring(edgeStrings[1].indexOf("y= ")+3,edgeStrings[1].indexOf(" type=")));
		String state2=edgeStrings[1].substring(edgeStrings[1].indexOf("type= ")+3,edgeStrings[1].length());
		t2.position.x=x2;
		t2.position.y=y2;
		t2.TileType=LocationState.Clean;
		if(state2.contains("Obstacle"))
			t2.TileType=LocationState.Obstacle;
		
	}

	private void InformationByEnvironment(
			LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept) {

		N = environmentPercept.getN();
		M = environmentPercept.getM();
		actionEnergyCosts = environmentPercept.getActionEnergyCosts();
		isMovedLastTime = environmentPercept.isMovedLastTime();
		
		if(environmentPercept.isOnBase()){
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
		
		if(getTileFromPoint(nextPosition)==null){
			TileNode nextTileNode = new TileNode(nextPosition, false);
			graphMap.addVertex(nextTileNode);			
			graphMap.addEdge(getTileFromPoint(currentPosition),nextTileNode);
		}
		
	}

	private TileNode getTileFromPoint(Point p) {
		for(TileNode node: graphMap.vertexSet()){
			if(node.position.equals(p))
				return node;
		}
		return null;
	}

	private void changeDirection() {
		
		List<Point> unvisitedPoint=getUnvisitedPoint(currentPosition);
		if(unvisitedPoint.size()!=0){
			Random r=new Random();
			currentDirection=pointToTheAction(unvisitedPoint.get(r.nextInt(unvisitedPoint.size())));
		}else{
			
			unvisitedPoint=getTotalUnvisitedPoint();
			Point nearestUnvisited=getNearestUnvisitedPoint(unvisitedPoint);
			nextDirections=getNextDirectionFromPoint(nearestUnvisited);
			
		}
		
	}

	private LinkedList<Action> getNextDirectionFromPoint(Point nearestUnvisited) {
		// TODO RItorna le prossime direzioni per arrivare al punto nearestUnvisited
		/*
		 * Vecchio agente return to the base
		 * 
		 * deve calcolare djikstra dalla current position al punto nearestUnvisited
		 * Una volta calcolato il percorso deve calcolare la lista delle direzioni
		 * Trasforma la lista dei punti in lista di direzioni
		 */
		return null;
	}

	private Point getNearestUnvisitedPoint(List<Point> unvisitedPoint) {
		// TODO Ritorna il punto con path minimo rispetto la posizione dell'agente
		//va calcolato un nuovo grafo con punti e eliminando gli ostacoli
		//findNearestDirtyTiles programma 1
		/*
		 * Si calcola il nuovo grafo eliminando gli ostacoli
		 * il Path è calcolato da currentPosition a ogni punto di univisitedPoint
		 * ritorna un punto di unvisitedPoint più vicino. con path più piccolo
		 */
		return null;
	}

	private List<Point> getTotalUnvisitedPoint() {
		// TODO Ritorna una lista di punti non obstacle che hanno dei vicini non visitati
		return null;
	}

	private List<Point> getUnvisitedPoint(Point point) {
		//TODO Ritorna una lista di punti non visitati vicini a point
		
		/*
		 * Questa serve in getTotalUnvisitedPoint()
		 */
		return null;
	}
	
	private Action pointToTheAction(Point pollFirst) {
		if(pollFirst.x == currentPosition.x +1)
			return getActionFromName("down");
		if(pollFirst.x == currentPosition.x -1)
			return getActionFromName("up");
		if(pollFirst.y == currentPosition.y +1)
			return getActionFromName("right");
		if(pollFirst.y == currentPosition.y -1)
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
		position=new Point();
		TileType=LocationState.Clean;
	}
	
	public TileNode(Point p, boolean isObstacle) {
		this.position = p;
		if (isObstacle) {
			TileType = LocationState.Obstacle;
		}else
			TileType=LocationState.Clean;
	}

	
	@Override
	public String toString() {
		return "x= "+position.x+" y= "+position.y+" type= "+TileType.name();
	}
}
