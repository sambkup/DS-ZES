package communication;

import java.io.Serializable;

import utils.Node;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	String destIP;
	int port;
	String kind;
	Node node;
	String src;

	int seqNum;

	public Message(String destIP,int port, String kind, Node node) {
		this.destIP = destIP;
		this.port = port;
		this.kind = kind;
		this.node = node;
		this.seqNum = -1;
	}

	public String getSource() {
		return src;
	}

	public String getDestIP() {
		return destIP;
	}
	
	public String getDestName() {
		return "("+this.destIP+":"+this.port+")";
	}

	public void set_source(String source) {
		this.src = source;
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
		System.out.println("Message From: " + this.src);
		System.out.println("Message To: " + this.destIP+":"+this.port);
		System.out.println("Message Kind: " + this.kind);
		System.out.println("Message Data: " + this.node.toString());
		System.out.println("Message SeqNum: " + this.seqNum);
		System.out.println("------------------------------");
	}

	public Message clone() {
		Message clone = new Message(destIP, port, kind, node);
		clone.src = src;
		clone.seqNum = seqNum;
		return clone;
	}

	public String toString() {
		return String.format("Message From: %s, To: %s:%d, Kind: %s, Data: %s, SeqNum: %d", this.src, this.destIP, this.port, this.kind,
				this.node.toString(), this.seqNum);
	}
}
