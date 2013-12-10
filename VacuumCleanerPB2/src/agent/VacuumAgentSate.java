package agent;

import java.util.LinkedList;

import aima.core.agent.Action;

public interface VacuumAgentSate {
	
	public LinkedList<Action> generatePath();
	public boolean suck();

}
