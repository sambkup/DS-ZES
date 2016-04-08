package utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.Timer;

public class Node implements Serializable {
	
	private static final long serialVersionUID = -6754243543644721809L;
	
	public String ip = null;
	public int port = 0;
	public String address = null;
	NodePatrolArea myPatrolArea;
	public P2PRegion p2pPatrolArea;
	public NodeLocation myLocation;
	public Timer timer; // used for heart beat code
	
	public Node(NodePatrolArea myPatrolArea, P2PRegion p2pPatrolArea, NodeLocation myLocation,int port, String ip) {

		this.myPatrolArea = myPatrolArea;
		this.p2pPatrolArea = p2pPatrolArea;
		this.myLocation = myLocation;
		this.port = port;
		this.ip = ip;
		this.address = String.format("/%s:%d", this.ip, this.port);
	}
	
	public Node clone(){
		return new Node(this.myPatrolArea.clone(), 
				this.p2pPatrolArea.clone(), 
				this.myLocation.clone(), 
				this.port, 
				this.ip);
		
	}
	
	public boolean inMyArea(Node testNode){
		return myPatrolArea.inMyArea(testNode.myLocation);		
	}
	
	public Node splitPatrolArea(Node testNode){
		if (!inMyArea(testNode)){
			// I cannot split my area with this node
			// TODO: throw an error
		}
		testNode.myPatrolArea = myPatrolArea.splitPatrolArea(testNode.myLocation);
		return testNode;
	}
	
	
	public String getName(){
		return "("+this.ip+":"+this.port+")";
	}
	
	// heartbeat uses this getter
	public String getIP(){
		return this.ip;
	}
	
	// heartbeat uses this getter
	public int getPort(){
		return this.port;
	}
	
	public String toString() {
		return String.format("(%s:%d), %s, %s", ip, port,myLocation.toString() , myPatrolArea.toString());
	}
	
}
