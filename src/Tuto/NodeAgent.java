package Tuto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class NodeAgent extends Agent {

	// the node vector description
	private List<Integer> nodeVector;

	private Role currentRole;

	private List<List> roleOwnDemands;

	private Map<String, Integer> votes;

	// Put agent initializations here
	@Override
	protected void setup() {

		System.out.println("[" + getAID().getLocalName() + "] : ready");

		NodeAgent _this = this;

		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						Object[] obj = (Object[]) msg.getContentObject();

						String messageType = (String) obj[0];

						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);

						if (messageType.compareTo("DemandAnswerNotReady") == 0) {
							System.out.println("[" + getAID().getLocalName() + "] : Received a DemandAnswerNotReady"
									+ " from " + msg.getSender().getLocalName());

							Object[] content = new Object[2];
							content[0] = "DemandAnswer";
							content[1] = obj[1];

							reply.setContentObject(content);
							send(reply);
						}

						if (messageType.compareTo("DemandRequest") == 0) {
							List<List> demandsRequest = (List<List>) obj[1];
							System.out.println("[" + getAID().getLocalName() + "] : Received a DemandRequest : "
									+ demandsRequest + " from " + msg.getSender().getLocalName());

							Object[] content = new Object[2];

							if (_this.roleOwnDemands != null) {
								content[0] = "DemandAnswer";
								Map<String, String> demandsAnswer = _this.compareDemandRequestWithOwn(demandsRequest);
								content[1] = demandsAnswer;
							} else {
								content[0] = "DemandAnswerNotReady";
								content[1] = demandsRequest;
							}

							reply.setContentObject(content);
							send(reply);

						}

						if (messageType.compareTo("DemandAnswer") == 0) {
							Map<String, String> demandsAnswer = (HashMap<String, String>) obj[1];

							ArrayList<String> listOfRoles = new ArrayList<String>(demandsAnswer.keySet());
							for (String role : listOfRoles) {
								if (demandsAnswer.get(role).compareTo("favorable") == 0) {
									_this.votes.put(role, _this.votes.get(role) + 1);
								}
								if (demandsAnswer.get(role).compareTo("unfavorable") == 0) {
									_this.votes.put(role, _this.votes.get(role) - 1);
								}
							}

							String bestRole = _this.processVote(_this.votes);
							System.out.println("[" + getAID().getLocalName() + "] : Received a DemandAnswer"
									+ demandsAnswer + " from " + msg.getSender().getLocalName() + "; Votes : "
									+ _this.votes + "; Proposed best role : " + bestRole);

							if (bestRole.compareTo("r0") != 0) {
								_this.currentRole = Role.roles.get(Integer.parseInt(bestRole.substring(1)));
							}
						}

					} catch (UnreadableException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				block();
			}
		});

		this.currentRole = Role.r0;
		// System.out.println("Initial nodeAgent's current role is " +
		// this.currentRole);

		// initializing with pretended gathered node informations
		this.nodeVector = Arrays.asList(getArguments()).stream().map((element) -> (Integer) element)
				.collect(Collectors.toList());
		// System.out.println("Initializing node vector description with : " +
		// this.nodeVector.toString());

		// computing adequation vector for each role
		// and store it in a map
		Map<String, List<Double>> adequationVectors = new HashMap();
		for (int i = 1; i < Role.roles.size(); i++) {
			Role currentRole = Role.roles.get(i);
			List<Integer> roleVector = currentRole.roleVector;
			// System.out.println("NodeAgent " + getAID().getLocalName() + " whose
			// nodeVector is "
			// + this.nodeVector.toString() + " is computing adequation vector with " +
			// currentRole);
			List<Double> adequationVector = this.computeAdequation(nodeVector, roleVector);
			// System.out.println("\t -> adequation vector for " + currentRole + " : " +
			// adequationVector.toString());
			adequationVectors.put(currentRole.roleName, adequationVector);
		}

		// System.out.println("For " + getAID().getLocalName() + " -> adequations : " +
		// adequationVectors.toString());

		this.roleOwnDemands = this.computeDemands(adequationVectors);

		this.votes = new HashMap<String, Integer>();

		for (List roleAndVector : roleOwnDemands) {
			this.votes.put((String) roleAndVector.get(0), 0);
		}

		System.out.println("[" + getAID().getLocalName() + "] : own demands : " + this.roleOwnDemands);

		this.sendDemandsToNeighbourgs(this.roleOwnDemands);

	}

	public String processVote(Map<String, Integer> votes) {
		String roleMax = "r0";
		int voteMax = -1000;
		ArrayList<String> listOfRoles = new ArrayList<String>(votes.keySet());
		for (String role : listOfRoles) {
			if ((votes.get(role) > voteMax) && (votes.get(role) > 0)) {
				voteMax = votes.get(role);
				roleMax = role;
			}
		}
		return roleMax;
	}

	public Map<String, String> compareDemandRequestWithOwn(List<List> demands) {

		Map<String, String> response = new HashMap<String, String>();

		for (List ownRoleVectorCouple : this.roleOwnDemands) {

			String ownTargetedRole = (String) ownRoleVectorCouple.get(0);
			List<Double> ownAdequationVector = (List<Double>) ownRoleVectorCouple.get(1);

			for (List demandRoleVectorCouple : demands) {
				String demandTargetedRole = (String) demandRoleVectorCouple.get(0);
				List<Double> demandAdequationVector = (List<Double>) demandRoleVectorCouple.get(1);

				if (demandTargetedRole.compareTo(ownTargetedRole) == 0) {
					double res = ownAdequationVector.stream().reduce(0., (acc, e) -> acc + e)
							- demandAdequationVector.stream().reduce(0., (acc, e) -> acc + e);
					if (res < 0) {
						response.put(demandTargetedRole, "favorable");
					} else {
						response.put(demandTargetedRole, "unfavorable");
					}

				}
			}

		}

		return response;
	}

	public void sendDemandsToNeighbourgs(List<List> demands) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		Object[] content = new Object[2];
		content[0] = "DemandRequest";
		content[1] = demands;
		try {
			msg.setContentObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String agentName : Topology.INSTANCE.getNeighbourgs(getAID().getLocalName())) {
			System.out.println("[" + getAID().getLocalName() + "] : sending out a DemandRequest to " + agentName);
			msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
		}
		send(msg);
	}

	// function to compute an adequation vector
	public List<Double> computeAdequation(List<Integer> nodeVector, List<Integer> roleVector) {
		List<Double> adequationVector = new ArrayList<Double>(
				nodeVector.stream().map((element) -> (double) element).collect(Collectors.toList()));
		for (int i = 0; i < nodeVector.size(); i++) {
			adequationVector.set(i, adequationVector.get(i) / roleVector.get(i));
		}
		return adequationVector;
	}

	// function to create a role change demand
	public List<List> computeDemands(Map<String, List<Double>> adequationVectors) {

		List<List> listCouple = new ArrayList();

		ArrayList<String> listOfKeys = new ArrayList<String>(adequationVectors.keySet());
		for (String k : listOfKeys) {
			if (adequationVectors.get(k).stream().allMatch((e) -> e >= 1.0)) {
				listCouple.add(Arrays.asList(k, adequationVectors.get(k)));
			}
		}

		Collections.sort(listCouple, (e1, e2) -> {
			List<Double> adequation1 = (List<Double>) e1.get(1);
			List<Double> adequation2 = (List<Double>) e2.get(1);
			double res = adequation1.stream().reduce(0., (acc, e) -> acc + e)
					- adequation2.stream().reduce(0., (acc, e) -> acc + e);
			return (int) (Math.ceil(res) * Math.signum(res));
		});

		return listCouple;
	}

	// Put agent clean-up operations here
	@Override
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("NodeAgent " + getAID().getLocalName() + " terminating.");
	}
}
