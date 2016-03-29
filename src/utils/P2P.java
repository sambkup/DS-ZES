package utils;

import java.util.ArrayList;
import java.util.List;

public class P2P {

	String processName;
	List<Node> foundNodes;
	Node myNode;

	Object config_data;

	public P2P(String name,double lattitude, double longitude, int port) {
		this.processName = name;
		this.foundNodes = new ArrayList<Node>();
		this.myNode = new Node(name,lattitude, longitude, port);
		// TODO: call bootstrap() and block until at least one node is found.
	}

	public Node findNodeWithName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		synchronized (this.foundNodes) {
			for (Node node : this.foundNodes) {
				if (node.name.equals(name)) {
					return node;
				}
			}
		}
		return null;
	}

	public synchronized Node getNode(String name) {
		synchronized (this.foundNodes) {
			for (Node node : this.foundNodes) {
				if (node.name.equals(name)) {
					return node;
				}
			}
		}
		// TODO: Make more robust by throwing exceptions
		// throw new NodeNotFoundException();
		return null;
	}

	public synchronized int getNodeIndex(String name) {
		synchronized (this.foundNodes) {
			Node node;
			for (int i = 0; i < this.foundNodes.size(); i++) {
				node = this.foundNodes.get(i);
				if (node.name.equals(name)) {
					return i;
				}
			}
		}
		// TODO: Make more robust by throwing exceptions
		// throw new NodeNotFoundException();

		return -1;
	}

	/* Setters and Getters */

	public String getProcessName() {
		synchronized (this.processName) {
			return this.processName;
		}
	}

	public List<Node> getNodes() {
		List<Node> nodesList;
		synchronized (this.foundNodes) {
			nodesList = new ArrayList<Node>(this.foundNodes);
		}
		return nodesList;
	}
	public Node getMyNode(){
		return myNode;
	}

}
