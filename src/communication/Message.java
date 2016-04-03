package communication;

import java.io.Serializable;

import utils.Node;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	String destIP;
	int destPort;
	String kind;
	Node node;

	int seqNum;

	public Message(String destIP,int port, String kind, Node node) {
		this.destIP = destIP;
		this.destPort = port;
		this.kind = kind;
		this.node = node;
		this.seqNum = -1;
	}

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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
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

	

	public void print() {
		System.out.println("------------------------------");
		System.out.println("Message To: " + this.destIP+":"+this.destPort);
		System.out.println("Message Kind: " + this.kind);
		System.out.println("Message Data: " + this.node.toString());
		System.out.println("Message SeqNum: " + this.seqNum);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(destIP, destPort, kind, node);
		clone.seqNum = seqNum;
		return clone;
	}

	public String toString() {
		return String.format("Message To: %s:%d, Kind: %s, Data: %s, SeqNum: %d", this.destIP, this.destPort, this.kind,
				this.node.toString(), this.seqNum);
	}
}
