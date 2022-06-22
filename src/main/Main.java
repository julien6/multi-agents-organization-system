package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

	public static void main(String[] args) {
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.GUI, "true");
		ContainerController containerController = runtime.createMainContainer(profile);

		AgentController agentController1, agentController2, agentController3, agentController4, agentController5;

		Object[] argsAgent1 = { 50, 25 };
		Object[] argsAgent2 = { 70, 140 };
		Object[] argsAgent3 = { 100, 80 };
		Object[] argsAgent4 = { 141, 60 };
		Object[] argsAgent5 = { 20, 60 };

		try {
			agentController1 = containerController.createNewAgent("Agent1", "main.NodeAgent", argsAgent1);
			agentController2 = containerController.createNewAgent("Agent2", "main.NodeAgent", argsAgent2);
			agentController3 = containerController.createNewAgent("Agent3", "main.NodeAgent", argsAgent3);
			agentController4 = containerController.createNewAgent("Agent4", "main.NodeAgent", argsAgent4);
			agentController5 = containerController.createNewAgent("Agent5", "main.NodeAgent", argsAgent5);

			agentController1.start();
			agentController2.start();
			agentController3.start();
			agentController4.start();
			agentController5.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
		}

	}

}