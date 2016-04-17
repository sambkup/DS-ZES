package utils;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class Node implements Serializable {

	public enum SensorState {
		SAFE, DANGER
	}

	private static final long serialVersionUID = -6754243543644721809L;

	public String ip = null;
	public int port = 0;
	public String address = null;
	public NodePatrolArea myPatrolArea;
	public P2PRegion p2pPatrolArea;
	public NodeLocation myLocation;
	SensorState state;

	public Node(NodePatrolArea myPatrolArea, P2PRegion p2pPatrolArea, NodeLocation myLocation, int port, String ip) {

		this.myPatrolArea = myPatrolArea;
		this.p2pPatrolArea = p2pPatrolArea;
		this.myLocation = myLocation;
		this.port = port;
		this.ip = ip;
		this.state = SensorState.SAFE;
		this.address = String.format("/%s:%d", this.ip, this.port);
	}

	public Node() {
		// TODO Auto-generated constructor stub
	}

	public Node clone() {
		return new Node(this.myPatrolArea.clone(), this.p2pPatrolArea.clone(), this.myLocation.clone(), this.port,
				this.ip);

	}

	public boolean inMyArea(Node testNode) {
		return myPatrolArea.inMyArea(testNode.myLocation);
	}

	public Node splitPatrolArea(Node testNode) {
		if (!inMyArea(testNode)) {
			// I cannot split my area with this node
			// TODO: throw an error
		}
		testNode.myPatrolArea = myPatrolArea.splitPatrolArea(testNode.myLocation);
		return testNode;
	}

	/**
	 * @param testNode,
	 *            neighborNodes
	 * @return Given a node, determine the closest node from my neighbors and return the node
	 */
	public Node findClosestNode(double[] latLong, HashMap<String, Node> neighborNodes) {
		Node returnNode = null;
		double minDistance = 0;
		for (String key : neighborNodes.keySet()) {
			Node neighbor = neighborNodes.get(key);
			double dist = neighbor.myLocation.findDistance(latLong);
			if (minDistance == 0) {
				minDistance = dist;
				returnNode = neighbor;
				continue;
			}
			if (dist < minDistance) {
				returnNode = neighbor;
			}
		}
		return returnNode;
	}
	
	public boolean isNeighbor(Node testNode) {
		return myPatrolArea.isNeighbor(testNode.myPatrolArea);
	}

	public String getName() {
		return "(" + this.ip + ":" + this.port + ")";
	}

	public SensorState getState() {
		return state;
	}

	public void setUnsafe() {
		state = SensorState.DANGER;
	}

	public void setSafe() {
		state = SensorState.SAFE;
	}

	public JSONObject enterJSON(JSONObject route) throws JSONException {
		int count = route.length();
		count = count + 1;
		route.put(String.valueOf(count), myLocation);
		return route;
	}

	public String toString() {
		return String.format("(%s:%d), %s, %s", ip, port, myLocation.toString(), myPatrolArea.toString());
	}

}
