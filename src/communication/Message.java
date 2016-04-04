package communication;

import java.io.Serializable;

import utils.Node;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	String destIP;
	int destPort;
	messageKind kind;
	Node node;

	int seqNum;

	public enum messageKind{
		GET_PARAM, 				// request parameters of the foreign node, send myself
		GET_PARAM_RESPONSE,  		// response to getParam - send my parameters
		SEND_PARAM, 			// send my parameters
		SEND_UPDATED_PARAM,		// send my updated parameters (assume my parameters are already stored)
		REQ_UPDATED_PATROL,		// request node to divide region with me.
		UPDATE_PATROL_NACK,	// - NACK if not in my region & NODE = next node to ask
		UPDATE_PATROL_ACK	// - ACK if my region is split & NODE = what I should be (will also receive an updateParam message)
	}

	
	public Message(String destIP, int port, messageKind kind, Node node) {
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
