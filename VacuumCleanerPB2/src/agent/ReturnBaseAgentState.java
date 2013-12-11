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
		nextDirections.add(new Action() {
			
			@Override
			public boolean isNoOp() {
				return true;
			}
			
			@Override
			public String toString() {
				return "NO OP";
			}
			
		});
		return nextDirections;
	}
	
	@Override
	public boolean suck() {
		return false;
	}

}
