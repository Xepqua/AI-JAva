package gui;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import aima.gui.framework.AgentAppFrame;

/**
 * Adds some selectors to the base class and adjusts its size.
 * 
 * @author Ruediger Lunde
 */
public class VacuumFrame extends AgentAppFrame {
	private static final long serialVersionUID = 1L;
	public static String ENV_SEL = "EnvSelection";
	public static String AGENT_SEL = "AgentSelection";
	
	public static Map<Integer,String> stringToFileMap;

	public VacuumFrame() {
		this.setTitle("Vacuum Agent Application");
		this.setSelectors(new String[] { VacuumFrame.ENV_SEL,
				VacuumFrame.AGENT_SEL }, new String[] { "Select Environment",
				"Select Agent" });
		
		//take all instance in the folder instance
		String instancesEnv []=new File("instance").list();
		//put the file in the map
		stringToFileMap=new HashMap<Integer, String>();
		for(int i=0;i<instancesEnv.length;i++)
			stringToFileMap.put(i, "instance\\"+instancesEnv[i]);
		
		this.setSelectorItems(VacuumFrame.ENV_SEL,
				instancesEnv, 0);
		this.setSelectorItems(VacuumFrame.AGENT_SEL, new String[] { "Peluria Agent" },
				0);
		this.setEnvView(new VacuumView());
		this.setSize(800, 400);
	}
}