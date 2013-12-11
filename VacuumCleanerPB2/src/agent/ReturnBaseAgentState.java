package agent;

import java.util.LinkedList;

import aima.core.agent.Action;

public class ReturnBaseAgentState extends SearchSuckAgentState {

	public ReturnBaseAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public LinkedList<Action> generatePath() {	
		
		LinkedList<Action> nextDirections=getNextDirectionFromPoint(agent.getBaseLocation());
		nextDirections.add(agent.getNoOpAction());
		return nextDirections;
	}
	
	@Override
	public boolean suck() {
		return false;
	}

}
