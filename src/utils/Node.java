package utils;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
/**
 * @author jayasudha
 *
 */
public class Node implements Serializable {

	public enum SensorState {
		SAFE, DANGER
	}

	private static final long serialVersionUID = -6754243543644721809L;

	public String ip = null;
	public int port = 0;
	public String address = null;
	NodePatrolArea myPatrolArea;
	public P2PRegion p2pPatrolArea;
	public NodeLocation myLocation;
	// public String state = "SAFE";

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
	 * @param testNode, otherNode
	 *            
	 * @return Given 2 nodes, return the distance between two
	 */

	
	public double distance(Node testNode, Node otherNode) {
		double testLat = testNode.myLocation.getLocation()[0];
		double testLon = testNode.myLocation.getLocation()[1];
		double lat = otherNode.myLocation.getLocation()[0];
		double lon = otherNode.myLocation.getLocation()[1];
		double dist = Math.pow((Math.pow((lat - testLat), 2) + Math.pow((lon - testLat), 2)), 1 / 2); //euclidean distance
		return dist;

	}

	/**
	 * @param testNode,
	 *            neighborNodes
	 * @return Given a node, determine the closest node from my neighbors and return it's IP
	 */
	public String findClosestNode(Node testNode, HashMap<String, Node> neighborNodes) {
		String returnIP = null;
		double minDistance = 0;
		for (String key : neighborNodes.keySet()) {
			Node neighbor = neighborNodes.get(key);
			double dist = this.distance(this, neighbor);
			if (minDistance == 0) {
				minDistance = dist;
				returnIP = neighbor.ip;
				continue;
			}
			if (dist < minDistance) {
				returnIP = neighbor.ip;
			}
		}
		return returnIP;
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
