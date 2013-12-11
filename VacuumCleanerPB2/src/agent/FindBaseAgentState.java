package agent;

public class FindBaseAgentState extends SearchSuckAgentState {

	public FindBaseAgentState(PeluriaVacuumAgentProgramv2 agent) {
		super(agent);
	}
	
	@Override
	public boolean suck() {
		return false;
	}

}
