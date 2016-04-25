package communication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Node;

public class Message implements Serializable {
	private static final long serialVersionUID = 45;
	public String destIP;
	public int destPort;
	public messageKind kind;
	public Node node;
	public String destLoc; //lat,lng, to store the destination of user
	public String startNodeIP;
	public ArrayList<String> jsonRoute;
	public String phoneIP;
	public int phonePort;
	int seqNum;
	Node closestNode = null; //ip of my neighbour which is closest to the sender


	private HashMap<String, Node> neighborNodes;
	private Node SplitNode;
	private Node NewNode;
	
	


	public enum messageKind{
		REQ_UPDATED_PATROL,		
		UPDATE_PATROL_NACK,	
		UPDATE_PATROL_ACK,	
		NEIGHBOR_UPDATE,
		
		HEARTBEAT,
		
		STATE_TOGGLE,
		
		REQ_START, //message sent from phone to nodes to get a start node
		MY_AREA, //response sent from node to phone saying if it's in the node's patrol area
		NOT_MY_AREA, //response sent from node to phone saying the user is not in user's patrol area
		MSG_JSON //JSON Object passed around
	}

	
	public Message(String destIP, int port, messageKind kind, Node node) {
		this.destIP = destIP;
		this.destPort = port;
		this.kind = kind;
		this.node = node;
		this.seqNum = -1;
	}




	public void print() {
		System.out.println("------------------------------");
		System.out.println("Message To: " + this.destIP+":"+this.destPort);
		System.out.println("Message Kind: " + this.kind);
		System.out.println("Message Data: " + this.node.toString());
		System.out.println("Message SeqNum: " + this.seqNum);
		System.out.println("------------------------------");
	}

	@SuppressWarnings("unchecked")
	public Message clone() {
		//TODO: update this
		Message clone = new Message(destIP, destPort, kind, node.clone());
		clone.seqNum = seqNum;
		// TODO: skipped some parameters
//		public String destLoc; //lat,lng, to store the destination of user
//		public String startNodeIP;
//		public JSONObject jsonRoute;
//		public String phoneIP;
//		public int phonePort;
//		Node closestNode = null; //ip of my neighbour which is closest to the sender


		//TODO: check this supressed warning
		clone.neighborNodes = (HashMap<String, Node>) this.neighborNodes.clone(); // doesn't actually clone
		clone.SplitNode = this.SplitNode.clone();
		clone.NewNode = this.NewNode.clone();

		return clone;
	}

	public String toString() {
		return String.format("Message To: %s:%d, Kind: %s, Data: %s, SeqNum: %d", this.destIP, this.destPort, this.kind,
				this.node.toString(), this.seqNum);
	}
	
	
	/*** Getters and Setters ***/
	
	
	public String getDestIP() {
		return destIP;
	}
	
	public String getDestName() {
		return "("+this.destIP+":"+this.destPort+")";
	}

	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
		// set the sequence number for this message
	}

	public messageKind getKind() {
		return kind;
	}


	public void setKind(messageKind kind) {
		this.kind = kind;
	}
	
	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(Node node) {
		this.node = node;
	}
	
	public String getDestLoc() {
		return destLoc;
	}

	public void setDestLoc(String destLoc) {
		this.destLoc = destLoc;
	}

	public String getStartNodeIP() {
		return startNodeIP;
	}

	public void setStartNodeIP(String startNodeIP) {
		this.startNodeIP = startNodeIP;
	}


	public void setDestIP(String destIP) {
		this.destIP = destIP;
	}

	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}

	public ArrayList<String> getJsonRoute() {
		return jsonRoute;
	}

	public void setJsonRoute(ArrayList<String> jsonRoute) {
		this.jsonRoute = jsonRoute;
	}

	public void setPhoneIP(String phoneIP) { this.phoneIP = phoneIP; }

	public void setPhonePort(int phonePort) {this.phonePort = phonePort;}
	
	public Node getClosestNode() {
		return closestNode;
	}

	public void setClosestNode(Node closestNode) {
		this.closestNode = closestNode;
	}
	
	
	
	/**
	 * @return the neighborNodes
	 */
	public HashMap<String, Node> getNeighborNodes() {
		return neighborNodes;
	}

	/**
	 * @param neighborNodes the neighborNodes to set
	 */
	public void setNeighborNodes(HashMap<String, Node> neighborNodes) {
		this.neighborNodes = neighborNodes;
	}

	/**
	 * @return the splitNode
	 */
	public Node getSplitNode() {
		return SplitNode;
	}

	/**
	 * @param splitNode the splitNode to set
	 */
	public void setSplitNode(Node splitNode) {
		SplitNode = splitNode;
	}

	/**
	 * @return the newNode
	 */
	public Node getNewNode() {
		return NewNode;
	}

	/**
	 * @param newNode the newNode to set
	 */
	public void setNewNode(Node newNode) {
		NewNode = newNode;
	}
}
