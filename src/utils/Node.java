package utils;

import java.io.Serializable;

public class Node implements Serializable {
	
	private static final long serialVersionUID = -6754243543644721809L;
	
	public String ip = null;
	public int port = 0;
	public String address = null;
	NodePatrolArea myPatrolArea;
	public P2PRegion p2pPatrolArea;
	public NodeLocation nodeLocation;

	public Node(NodePatrolArea myPatrolArea, P2PRegion p2pPatrolArea, NodeLocation nodeLocation,int port, String ip) {

		this.myPatrolArea = myPatrolArea;
		this.p2pPatrolArea = p2pPatrolArea;
		this.nodeLocation = nodeLocation;
		this.port = port;
		this.ip = ip;
		this.address = String.format("/%s:%d", this.ip, this.port);
	}
	
	public String getName(){
		return "("+this.ip+":"+this.port+")";
	}
	
	public String toString() {
		return String.format("(%s:%d)", ip, port);
	}
}
