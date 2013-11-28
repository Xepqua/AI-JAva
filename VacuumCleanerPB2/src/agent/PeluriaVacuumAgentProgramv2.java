package agent;

import java.awt.Point;
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

public class PeluriaVacuumAgentProgramv2 implements AgentProgram {

	private Action currentDirection;
	private Point baseLocation;
	private Point currentPosition;
	private Point nextPosition;

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
		if (!isMovedLastTime())
			changeDirection();

		// add in a graph a tile
		updateMap();

		return currentDirection;

	}

	private void InformationByEnvironment(
			LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept) {

		N = environmentPercept.getN();
		M = environmentPercept.getM();
		actionEnergyCosts = environmentPercept.getActionEnergyCosts();
		isMovedLastTime = environmentPercept.isMovedLastTime();

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
		TileNode nextTileNode = new TileNode(nextPosition, false);
		graphMap.addVertex(nextTileNode);
		graphMap.addEdge(getTileFromPoint(currentPosition), nextTileNode);
	}

	private void changeDirection() {
		Random rand = new Random();
		int rand_int = rand.nextInt(3);
		switch (rand_int) {
		case 0:
			currentDirection = getActionFromName("up");
			break;
		case 1:
			currentDirection = getActionFromName("down");
			break;
		case 2:
			currentDirection = getActionFromName("left");
			break;
		case 3:
			currentDirection = getActionFromName("right");
			break;

		default:
			currentDirection = getActionFromName("up");
			break;
		}
	}

	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName().equals(actionName))
				return a;

		return null;
	}

}

class TileNode {
	public TileNode(Point p, boolean isObstacle) {
		this.position = p;
		if (isObstacle) {
			TileType = LocationState.Obstacle;
		}
	}

	public Point position;
	public LocationState TileType;
}
