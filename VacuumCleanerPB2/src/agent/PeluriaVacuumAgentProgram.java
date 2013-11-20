package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import core.LocalVacuumEnvironmentPercept;
import core.VacuumEnvironment.LocationState;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.DynamicAction;
import aima.core.agent.impl.NoOpAction;

public class PeluriaVacuumAgentProgram implements AgentProgram {
	
	//A rappresentation of a map in a graph
	private UndirectedGraph<Point, DefaultEdge> graphMap;
	
	//Important information
	private Point agentLocation;
	private Point baseLocation;
	private Double currentEnergy;
	private Map<Action, Double> actionEnergyCosts;
	private boolean noSuck;
	
	//A path that the vacuum have do, if null a vacuum have to take a decision
	private LinkedList<Point> path=new LinkedList<Point>();

	@Override
	public Action execute(Percept percept) {
		
		
		final LocalVacuumEnvironmentPercept environmentPercept = (LocalVacuumEnvironmentPercept) percept;
		
		agentLocation=environmentPercept.getAgentLocation();
		baseLocation=environmentPercept.getBaseLocation();
		currentEnergy=environmentPercept.getCurrentEnergy();
		actionEnergyCosts=environmentPercept.getActionEnergyCosts();
		
		
		if(graphMap==null)
			loadGraph(environmentPercept.getState());

		if(environmentPercept.getInitialEnergy()==currentEnergy && environmentPercept.getState().get(agentLocation)==LocationState.Dirty){
			
			int returnBaseCost = returnToTheBase(agentLocation).size();
			double costToClean = actionEnergyCosts.get(getActionFromName("suck"));
			if(returnBaseCost+costToClean>currentEnergy){
				noSuck=true;
				path=returnToTheBase(agentLocation);
				return pointToTheAction(path.pollFirst());
			}

		}
		
		if(!noSuck && environmentPercept.getState().get(agentLocation)==LocationState.Dirty)
			return getActionFromName("suck");
		
		if(path.size()==0){
			
		
			//calculate the dirty tiles
			ArrayList<Point> dirtyTiles=getDirtyTilesFromMap(environmentPercept.getState());
			//calculate the nearest dirty tile path
			ArrayList<Point> pathToReachNearestDirtyTile=findNearestDirtyTiles(dirtyTiles);
			//if there are no dirty tiles return to the base
			noSuck=false;
			if(dirtyTiles.size()==0 || energyToSpent(pathToReachNearestDirtyTile)>currentEnergy){
				if(!agentLocation.equals(baseLocation)){
					noSuck=true;
					path=returnToTheBase(agentLocation);
					System.out.println("torna");
				}else
					return NoOpAction.NO_OP;
			}else
				path.addAll(pathToReachNearestDirtyTile);
			
		}
		
		
		
		return pointToTheAction(path.pollFirst());
		
	}
	
	private double energyToSpent(ArrayList<Point> pathToReachNearestDirtyTile) {
		int returnBaseCost = returnToTheBase(pathToReachNearestDirtyTile.get(pathToReachNearestDirtyTile.size()-1)).size();
		double costToClean = actionEnergyCosts.get(getActionFromName("suck"));

		return pathToReachNearestDirtyTile.size()+costToClean+returnBaseCost;
	}

	private Action pointToTheAction(Point pollFirst) {
		if(pollFirst.x == agentLocation.x +1)
			return getActionFromName("down");
		if(pollFirst.x == agentLocation.x -1)
			return getActionFromName("up");
		if(pollFirst.y == agentLocation.y +1)
			return getActionFromName("right");
		if(pollFirst.y == agentLocation.y -1)
			return getActionFromName("left");
		return null;
	}


	private void loadGraph(Map<Point, LocationState> map){
		graphMap=new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
		double max_dimension = Math.sqrt(map.size());
		// Create vertex into our graph
		for (Point point : map.keySet()) {
			if (!map.get(point).equals(LocationState.Obstacle)) {
				graphMap.addVertex(point);
			}
		}
		// Create edge from graphMap
		for (Point point : graphMap.vertexSet()) {
			// Set neighbours
			ArrayList<Point> nb = neighbours(point, max_dimension);
			for (Point p_neigh : nb) {
				if (!map.get(p_neigh).equals(LocationState.Obstacle)) {
					graphMap.addEdge(point, p_neigh);
				}
			}
		}
	}
	
	private ArrayList<Point> neighbours(Point p, double max_dimension) {
		ArrayList<Point> tmp = new ArrayList<Point>();
		if (p.x > 0) {
			tmp.add(new Point(p.x - 1, p.y));
		}
		if (p.y > 0) {
			tmp.add(new Point(p.x, p.y - 1));
		}
		if (p.x < max_dimension-1) {
			tmp.add(new Point(p.x + 1, p.y));
		}
		if (p.y < max_dimension-1) {
			tmp.add(new Point(p.x, p.y + 1));
		}
		return tmp;
	}
	
	
	private ArrayList<Point> findNearestDirtyTiles(ArrayList<Point> dirtyTiles){
		//if there isn't dirty tiles return empty path
		if(dirtyTiles.size()==0)
			return new ArrayList<Point>();
		
		ArrayList<ArrayList<Point>> pathNearestDirtyTiles=new ArrayList<ArrayList<Point>>();
		
		//for each dirty tiles calculate minimum path
		for(Point dirtyTile:dirtyTiles){
			
			DijkstraShortestPath<Point, DefaultEdge> path=new DijkstraShortestPath<Point, DefaultEdge>(graphMap, agentLocation, dirtyTile);
			
			//if there is a path to reach dirty tile
			if(path!=null){
				ArrayList<Point> currentPath=new ArrayList<Point>();
				
				//costruct array list path of point 
				List<DefaultEdge> edgeList=path.getPathEdgeList();
				for(DefaultEdge e:edgeList){
					Point p1 = new Point(),p2 = new Point();
					getPointsFromEdge(e, p1, p2);
					if(currentPath.size()==0){
						if(p1.equals(agentLocation))
							currentPath.add(p2);
						else
							currentPath.add(p1);
					}else{
						if(currentPath.get(currentPath.size()-1).equals(p1))
							currentPath.add(p2);
						else
							currentPath.add(p1);
					}
				}
				
				if(pathNearestDirtyTiles.size()==0)
					pathNearestDirtyTiles.add(currentPath);
				else if(currentPath.size() < pathNearestDirtyTiles.get(0).size()){
					pathNearestDirtyTiles.clear();
					pathNearestDirtyTiles.add(currentPath);
				} else if(currentPath.size() == pathNearestDirtyTiles.get(0).size()){
					pathNearestDirtyTiles.add(currentPath);
				}
				
				
			}
			
		}
		
		//return a random nearest path
		return pathNearestDirtyTiles.get(new Random().nextInt(pathNearestDirtyTiles.size()));
	}
	
	private void getPointsFromEdge(DefaultEdge e,Point p1,Point p2){
		
		String edgeString=e.toString();
		String pointsString[]=edgeString.split(":");
		String number=pointsString[0].substring(pointsString[0].indexOf("x=")+2,pointsString[0].indexOf(",")) ;
		p1.x=Integer.parseInt(number);
		number=pointsString[0].substring(pointsString[0].indexOf("y=")+2,pointsString[0].indexOf("]")) ;
		p1.y=Integer.parseInt(number);
		number=pointsString[1].substring(pointsString[1].indexOf("x=")+2,pointsString[1].indexOf(",")) ;
		p2.x=Integer.parseInt(number);
		number=pointsString[1].substring(pointsString[1].indexOf("y=")+2,pointsString[1].indexOf("]")) ;
		p2.y=Integer.parseInt(number);
		
		
				
	}
	
	private LinkedList<Point> returnToTheBase(Point tiles){
		DijkstraShortestPath<Point, DefaultEdge> path=new DijkstraShortestPath<Point, DefaultEdge>(graphMap, tiles, baseLocation);
		LinkedList<Point> currentPath=new LinkedList<Point>();
		
		//costruct array list path of point 
		List<DefaultEdge> edgeList=path.getPathEdgeList();
		for(DefaultEdge e:edgeList){
			Point p1 = new Point(),p2 = new Point();
			getPointsFromEdge(e, p1, p2);
			if(currentPath.size()==0){
				if(p1.equals(agentLocation))
					currentPath.add(p2);
				else
					currentPath.add(p1);
			}else{
				if(currentPath.getLast().equals(p1))
					currentPath.add(p2);
				else
					currentPath.add(p1);
			}
		}
		return currentPath;
	}
	
	private ArrayList<Point> getDirtyTilesFromMap(Map<Point,LocationState> map){
		
		ArrayList<Point> dirtyTiles=new ArrayList<Point>();
		
		for(Point p : map.keySet()){
			if(map.get(p)==LocationState.Dirty){
				DijkstraShortestPath<Point, DefaultEdge> path=new DijkstraShortestPath<Point, DefaultEdge>(graphMap, agentLocation, p);
				if(path.getPathEdgeList()!=null)
					dirtyTiles.add(p);
			}
			
		}
		
		return dirtyTiles;
	}
	
	public Action getActionFromName(final String actionName) {

		for (final Action a : this.actionEnergyCosts.keySet())
			if (((DynamicAction) a).getName() == actionName)
				return a;

		return null;

	}

}
