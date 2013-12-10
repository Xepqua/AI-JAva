package agent;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Map;

import org.jgrapht.UndirectedGraph;
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
	private VacuumAgentSate state;

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
			state=new SearchSuckAgentState(this);
			currentDirection = getActionFromName("up");
			nextDirections.add(currentDirection);
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
		if(false && state.suck()){
//		if (tilesWhereImIsDirty) {
			return getActionFromName("suck");
		}

		// return true if in current direction there is an obstacle
//		if (!isMovedLastTime() || nextDirections.size() == 0)
//			changeDirection();
//		else {
//			currentDirection = nextDirections.pollFirst();
//		}
		
		if(nextDirections.size()<=0)
			nextDirections=state.generatePath();
			
		currentDirection = nextDirections.pollFirst();

		// add in a graph a tile
		printGraph(graphMap);
		updateMap();

		return currentDirection;

	}

	// Print the grahMap
	public void printGraph(UndirectedGraph<TileNode,DefaultEdge> graphMap) {
		System.out.println("---------START-------");
		System.out.println("NEXT SIZE: "+nextDirections.size());
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


	private void updateMap() {
		nextPosition=getPointToTheAction(currentPosition,currentDirection);

		if (getTileFromPoint(nextPosition,graphMap) == null) {
			TileNode nextTileNode = new TileNode(nextPosition, false);
			graphMap.addVertex(nextTileNode);
			graphMap.addEdge(getTileFromPoint(currentPosition,graphMap), nextTileNode);
		}

	}

	public Point getPointToTheAction(Point currentPosition,Action currentDirection) {
		Point nextPosition = (Point) currentPosition.clone();

		if (currentDirection.equals(getActionFromName("up"))) {
			nextPosition.x = currentPosition.x - 1;
		} else if (currentDirection.equals(getActionFromName("down"))) {
			nextPosition.x = currentPosition.x + 1;
		} else if (currentDirection.equals(getActionFromName("left"))) {
			nextPosition.y = currentPosition.y - 1;
		} else if (currentDirection.equals(getActionFromName("right"))) {
			nextPosition.y = currentPosition.y + 1;
		}
		
		return nextPosition;
	}
	
	public TileNode getTileFromPoint(Point p, UndirectedGraph<TileNode, DefaultEdge> graph) {
		for (TileNode node : graph.vertexSet()) {
			if (node.position.equals(p))
				return node;
		}
		return null;
	}


	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName().equals(actionName))
				return a;

		return null;
	}
	
	

	public Action getCurrentDirection() {
		return currentDirection;
	}

	public void setCurrentDirection(Action currentDirection) {
		this.currentDirection = currentDirection;
	}

	public LinkedList<Action> getNextDirections() {
		return nextDirections;
	}

	public void setNextDirections(LinkedList<Action> nextDirections) {
		this.nextDirections = nextDirections;
	}

	public Point getBaseLocation() {
		return baseLocation;
	}

	public void setBaseLocation(Point baseLocation) {
		this.baseLocation = baseLocation;
	}

	public Point getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Point currentPosition) {
		this.currentPosition = currentPosition;
	}

	public Point getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(Point nextPosition) {
		this.nextPosition = nextPosition;
	}

	public double getCurrentEnergy() {
		return currentEnergy;
	}

	public void setCurrentEnergy(double currentEnergy) {
		this.currentEnergy = currentEnergy;
	}

	public VacuumAgentSate getState() {
		return state;
	}

	public void setState(VacuumAgentSate state) {
		this.state = state;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public int getM() {
		return M;
	}

	public void setM(int m) {
		M = m;
	}

	public boolean isTilesWhereImIsDirty() {
		return tilesWhereImIsDirty;
	}

	public void setTilesWhereImIsDirty(boolean tilesWhereImIsDirty) {
		this.tilesWhereImIsDirty = tilesWhereImIsDirty;
	}

	public UndirectedGraph<TileNode, DefaultEdge> getGraphMap() {
		return graphMap;
	}

	public void setGraphMap(UndirectedGraph<TileNode, DefaultEdge> graphMap) {
		this.graphMap = graphMap;
	}

	public Map<Action, Double> getActionEnergyCosts() {
		return actionEnergyCosts;
	}

	public void setActionEnergyCosts(Map<Action, Double> actionEnergyCosts) {
		this.actionEnergyCosts = actionEnergyCosts;
	}
	
	public boolean isMovedLastTime() {
		return isMovedLastTime;
	}

	public void setMovedLastTime(boolean isMovedLastTime) {
		this.isMovedLastTime = isMovedLastTime;
	}

}

