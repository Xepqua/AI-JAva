package agent;

import java.awt.Point;
import java.util.LinkedList;

import aima.core.agent.Action;

public class CheckBeforeMovesLevelDirtyAgentState extends CheckBeforeMovesAgentState {

	public CheckBeforeMovesLevelDirtyAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public LinkedList<Action> generatePath() {
		double currentEnergy= agent.getCurrentEnergy();
		LinkedList<Action> nextDirection=null;
		

		nextDirection = super.generatePath();

		if(nextDirection!=null){
			
			if(nextDirection.size()+movesToReturnBase()<currentEnergy)
				return nextDirection;

		}
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		
		return returnBase.generatePath();
	}
	
	@Override
	public boolean suck() {
		double currentEnergy= agent.getCurrentEnergy();
		double suckCost=agent.getActionEnergyCosts().get(agent.getActionFromName("suck"));
		System.out.println(suckCost*agent.getEnergyToClean(agent.getCurrentPosition())+" "+currentEnergy);
		if( suckCost*agent.getEnergyToClean(agent.getCurrentPosition()) + movesToReturnBase() < currentEnergy)
			return true;
		VacuumAgentSate returnBase=new ReturnBaseAgentState(agent);
		agent.setState(returnBase);
		agent.setNextDirections(returnBase.generatePath());
		return false;
	}

}
