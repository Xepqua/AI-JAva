package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

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

	protected Action currentDirection;
	protected LinkedList<Action> nextDirections = new LinkedList<>();

	protected Point baseLocation;
	protected Point currentPosition;
	protected Point nextPosition;
	protected double currentEnergy;
	protected VacuumAgentSate state;
	protected boolean suckLastTime; 
	protected boolean isOnTheBase=false;
	
	protected int thresholdForFindBase;
	
	protected HashMap<Point, Integer> dirtyTile=new HashMap<Point, Integer>();



	// dimension of enviroment
	protected int N;
	protected int M;

	protected boolean tilesWhereImIsDirty = false;
	protected UndirectedGraph<TileNode, DefaultEdge> graphMap = new SimpleGraph<>(
			DefaultEdge.class);

	protected Map<Action, Double> actionEnergyCosts;
	protected boolean isMovedLastTime;

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
			thresholdForFindBase=N*M;
		} else {
			if (!isMovedLastTime() && !suckLastTime) {
				updateVertexOnGraph(nextPosition, LocationState.Obstacle);
			} else if(!suckLastTime){
				currentPosition = nextPosition;
			}
		}
		
		changeState();


		// return true if the agent is in a dirty tile
		if(tilesWhereImIsDirty && state.suck()){
			if(dirtyTile.containsKey(currentPosition))
				dirtyTile.remove(currentPosition);
			
			suckLastTime=true;
			return getActionFromName("suck");
		}else if(tilesWhereImIsDirty)
			dirtyTile.put(currentPosition, 1);

		// return true if in current direction there is an obstacle
//		if (!isMovedLastTime() || nextDirections.size() == 0)
//			changeDirection();
//		else {
//			currentDirection = nextDirections.pollFirst();
//		}
		
		if(nextDirections.size()<=0)
			nextDirections=state.generatePath();
		
//		System.out.println("NEXT ACTIONS");
//		for(Action a:nextDirections)
//			System.out.println(a);
		
		suckLastTime=false;
			
		currentDirection = nextDirections.pollFirst();


		// add in a graph a tile
		updateMap();
//		printGraph(graphMap);
//		System.out.println(currentEnergy);
//

		
		return currentDirection;

	}
	

	protected void changeState() {
		if (isOnTheBase && ! (state instanceof CheckBeforeMovesAgentState)) {
			if(baseLocation==null)
				baseLocation = (Point) currentPosition.clone();
			state=new CheckBeforeMovesAgentState(this);
		}

		
		if(! (state instanceof FindBaseAgentState) && ! (state instanceof CheckBeforeMovesAgentState) && ! (state instanceof ReturnBaseAgentState) && currentEnergy<thresholdForFindBase ){
			state=new FindBaseAgentState(this);
		}
		
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



	protected void InformationByEnvironment(
			LocalVacuumEnvironmentPerceptTaskEnvironmentB environmentPercept) {

		N = environmentPercept.getN();
		M = environmentPercept.getM();
		actionEnergyCosts = environmentPercept.getActionEnergyCosts();
		isMovedLastTime = environmentPercept.isMovedLastTime();
		isOnTheBase=environmentPercept.isOnBase();

		currentEnergy = environmentPercept.getCurrentEnergy();
		
		
		if (environmentPercept.getState().getLocState()
				.equals(LocationState.Dirty))
			tilesWhereImIsDirty = true;
		else
			tilesWhereImIsDirty = false;

	}

	protected void updateVertexOnGraph(Point p, LocationState state) {
		for (TileNode tileNode : graphMap.vertexSet()) {
			if (tileNode.position.equals(p)) {
				tileNode.TileType = state;
			}
		}
	}


	protected void updateMap() {
		nextPosition=getPointToTheAction(currentPosition,currentDirection);

		TileNode nextTileNode=getTileFromPoint(nextPosition,graphMap);
		TileNode currentTileNode=getTileFromPoint(currentPosition, graphMap);
		
		if ( nextTileNode==null || (!graphMap.containsEdge(nextTileNode,currentTileNode) &&  !graphMap.containsEdge(currentTileNode,nextTileNode))) {
			if(nextTileNode==null){
				nextTileNode=new TileNode(nextPosition, false);
				graphMap.addVertex(nextTileNode);
				
				List<Point> tmp = new ArrayList<>();
				tmp.add(new Point(nextTileNode.position.x - 1, nextTileNode.position.y));
				tmp.add(new Point(nextTileNode.position.x, nextTileNode.position.y - 1));
				tmp.add(new Point(nextTileNode.position.x + 1, nextTileNode.position.y));
				tmp.add(new Point(nextTileNode.position.x, nextTileNode.position.y + 1));
				
				for(Point p:tmp){
					TileNode nearTileNode=getTileFromPoint(p, graphMap);
					if(nearTileNode!=null && graphMap.containsVertex(nearTileNode)){
						graphMap.addEdge(  nextTileNode, nearTileNode);
					}
				}
			}
			
			if(!currentTileNode.position.equals(nextTileNode.position)){
				graphMap.addEdge(currentTileNode, nextTileNode);
			}
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
			if (node.position.equals(p)){
				return node;
			}
		}
		return null;
	}


	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName().equals(actionName))
				return a;

		return null;
	}
	
	public Action getNoOpAction(){
		return new Action() {
			
			@Override
			public boolean isNoOp() {
				return true;
			}
			
			@Override
			public String toString() {
				return "NO OP";
			}
			
		};
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

	public boolean isSuckLastTime() {
		return suckLastTime;
	}

	public void setSuckLastTime(boolean suckLastTime) {
		this.suckLastTime = suckLastTime;
	}

	
	public int getEnergyToClean(Point p){
		return dirtyTile.get(p);
	}
	
	public Set<Point> getDirtyTileFind(){
		return dirtyTile.keySet();
	}
}

