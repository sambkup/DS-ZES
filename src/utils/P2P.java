package utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class P2P {

	String processName;
	List<Node> foundNodes;
	Node myNode;
	int nodecount = 0;

	Object config_data;

	public P2P(Node myself) {
		this.myNode = myself;

		this.processName = myNode.name;
		this.foundNodes = new ArrayList<Node>();
		System.out.println("Starting bootstrapping...");
		findFirstNode();
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

	public boolean findFirstNode(){
		String myIP = this.myNode.ip;
		String delims = "[.]";
		String[] chunks = myIP.split(delims);

		int maxIP = 256;
		String testIP;
		for (int i = 1; i<maxIP; i++){
			if (Integer.toString(i).equals(chunks[3])){
				continue;
			}

			testIP = chunks[0]+"."+chunks[1]+"."+chunks[2]+"."+i;
			Socket s = null;
			try {
				InetSocketAddress endpoint = new InetSocketAddress(testIP, this.myNode.port);
				s = new Socket();
				s.connect(endpoint, 100);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				//System.out.println(testIP + " - Closed");
				continue;
			}
			if (s != null){
				try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(testIP + " - Found first node");
			addFoundNode(testIP);
			return true;
		}
		return false;
	}
	
	private void addFoundNode(String IP){
		String name = "Node"+this.nodecount++;
		// TODO: Request details from this node - latt, long

		double lattitude = 0.0;
		double longitude = 0.0;
		int port = this.myNode.port;
		Node newNode = new Node(name,lattitude, longitude, port,IP);
		synchronized (foundNodes){
			foundNodes.add(newNode);
		}
		
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
