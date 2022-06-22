package Tuto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Topology {

	public static Topology INSTANCE = new Topology();

	private Map<String, List<String>> connectionMap = new HashMap<String, List<String>>();

	public Topology() {
		this.connectionMap.put("Agent1", Arrays.asList("Agent2", "Agent3"));
		this.connectionMap.put("Agent2", Arrays.asList("Agent1", "Agent3"));
		this.connectionMap.put("Agent3", Arrays.asList("Agent1", "Agent2", "Agent4"));
		this.connectionMap.put("Agent4", Arrays.asList("Agent3", "Agent5"));
		this.connectionMap.put("Agent5", Arrays.asList("Agent4"));
	}

	public List<String> getNeighbourgs(String agentName) {
		return this.connectionMap.get(agentName);
	}
}
