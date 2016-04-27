package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Node implements Serializable {

	public enum SensorState {
		SAFE, DANGER
	}

	private static final long serialVersionUID = -6754243543644721809L;

	public String ip = null;
	public int port = 0;
	public NodePatrolArea myPatrolArea;
	public P2PRegion p2pPatrolArea;
	public NodeLocation myLocation;
	public SensorState state;
	
	private int heartBeatCount = 0;

	public Node(NodePatrolArea myPatrolArea, P2PRegion p2pPatrolArea, NodeLocation myLocation, int port, String ip) {

		this.myPatrolArea = myPatrolArea;
		this.p2pPatrolArea = p2pPatrolArea;
		this.myLocation = myLocation;
		this.port = port;
		this.ip = ip;
		this.state = SensorState.SAFE;
	}

	public Node clone() {
		Node clone = new Node(this.myPatrolArea.clone(), this.p2pPatrolArea.clone(), this.myLocation.clone(), this.port,
				this.ip);
		clone.state = this.state;
		return clone;
	}
	
	public boolean heartBeat(){
		// increment the heatbeat counter
		this.heartBeatCount++;
		
		// check if the heartbeat counter is above a threshold
		if (this.heartBeatCount > 5){
			// above threshold mean this node is dead
			return false;
		}
		return true;
		
	}
	
	public void resetHeartBeat(){
		this.heartBeatCount = 0;
	}

	public boolean inMyArea(Node testNode) {
		return myPatrolArea.inMyArea(testNode.myLocation);
	}

	public Node splitPatrolArea(Node testNode) {
		if (!inMyArea(testNode)) {
			// I cannot split my area with this node
			// TODO: throw an error
		}
//		testNode.myPatrolArea = this.myPatrolArea.splitPatrolArea(testNode.myLocation);
		double[] myLoc = this.myLocation.getLocation();
		double a1 = myLoc[0];
		double b1 = myLoc[1];
		double[] testLoc = testNode.myLocation.getLocation();
		double a2 = testLoc[0];
		double b2 = testLoc[1];
		
		double[] patrolArea = this.myPatrolArea.getRange();
		double x1 = patrolArea[0];
		double y1 = patrolArea[1];
		double x2 = patrolArea[2];
		double y2 = patrolArea[3];
		double x3 = 0.0;
		double y3 = 0.0;
		double x4 = 0.0;
		double y4 = 0.0;
		
		
		/*
		 * 1. Determine the geometry of the area, and how to split it
		 * 	- If a square decide which axis to split
		 * 2. Split the regions
		 * 3. decide which region belongs to which node
		 */
		
		System.out.println("|x2-x1|="+Math.abs(x2-x1)+" |y2-y1|="+Math.abs(y2-y1)+" |x2-x1|==|y2-y1|="+(Math.abs(x2-x1) == Math.abs(y2-y1))+" ||x2-x1|-|y2-y1||<0.000001"+(Math.abs((Math.abs(x2-x1) - Math.abs(y2-y1))) < 0.000001));
		// approximately square
		if (Math.abs((Math.abs(x2-x1) - Math.abs(y2-y1))) < 0.000001   ){
			// square - now figure out which axis to split along
			System.out.println("Splitting a square region");
			if ( Math.abs(a2-a1) > Math.abs(b2-b1)){
				// further apart on x-axis, so split y
//				x1 = x1;
				x4 = x2;
				x2 = x2+Math.abs(((x2-x1)/2));
				x3 = x2;
				
//				y1 = y1;
//				y2 = y2;
				y3 = y1;
				y4 = y2;

			} else {

				// further apart on y-axis, so split x	
//				x1 = x1;
//				x2 = x2;
				x3 = x1;
				x4 = x2;
				
//				y1 = y1;
				y4 = y2;
				y2 = y1 + Math.abs(((y2-y1)/2));
				y3 = y2;


			}
		} else if (Math.abs(x2-x1) < Math.abs(y2-y1)) {
			// y2-y1 is longer, so split along y
//			x1 = x1;
//			x2 = x2;
			x3 = x1;
			x4 = x2;
			
//			y1 = y1;
			y4 = y2;
			y2 = y1 + Math.abs(((y2-y1)/2));
			y3 = y2;

			
			
			
			
		} else {
			// x2-x1 is longer, so split along x
//			x1 = x1;
			x4 = x2;
			x2 = x2+Math.abs(((x2-x1)/2));
			x3 = x2;
			
//			y1 = y1;
//			y2 = y2;
			y3 = y1;
			y4 = y2;
			
			
		}
		
		
		patrolArea[0] = x1;
		patrolArea[1] = y1;
		patrolArea[2] = x2;
		patrolArea[3] = y2;		
		NodePatrolArea patrolArea1 = new NodePatrolArea(patrolArea);
		
		double[] testPatrol = new double[4];
		testPatrol[0] = x3;
		testPatrol[1] = y3;
		testPatrol[2] = x4;
		testPatrol[3] = y4;	
		NodePatrolArea patrolArea2 = new NodePatrolArea(testPatrol);
		
		if (patrolArea1.inMyArea(testNode.myLocation)){
			//patrol area 1 belongs to testNode
			this.myPatrolArea = patrolArea2;
			testNode.myPatrolArea = patrolArea1;
		} else {
			this.myPatrolArea = patrolArea1;
			testNode.myPatrolArea = patrolArea2;
		}
		
		return testNode;
	}

	/**
	 * @param testNode,
	 *            neighborNodes
	 *            	senderNode - shouldnot compare with this
	 * @return Given a node, determine the closest node from my neighbors and return the node
	 */
	public Node findClosestNode(double[] latLong, HashMap<String, Node> neighborNodes,Node senderNode) {
		Node returnNode = null;
		double old_dist = Double.MAX_VALUE;
		for (String key : neighborNodes.keySet()) {
			
			Node neighbor = neighborNodes.get(key);
			if(senderNode!=null){ //will be null for REQ_UPDATED_PATROL. 
				if(neighbor.ip.equalsIgnoreCase(senderNode.ip)){
					continue;
				}
			}
			double dist = neighbor.myLocation.findDistance(latLong);
			System.out.println("Dist to "+neighbor.getName()+" is: "+dist);
			if (dist <= old_dist) {
				old_dist = dist;
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

	public ArrayList<String> enterLocation(ArrayList<String> route) {
		
		String coordinates = new String();
		coordinates = String.valueOf(myLocation.getLocation()[0])+","+String.valueOf(myLocation.getLocation()[1]);
		route.add(coordinates);
		return route;
	}

	public String toString() {
		String[] chunks = this.ip.split("[.]");
		int nodeNum = Integer.parseInt(chunks[3])-100;
		
		String result = "alive";
		if (this.heartBeatCount > 5){
			result = "dead";
		}
		
		
		return String.format("Node %d %s; \tLocation: %s; \tPatrol Area: %s \tHeartbeat: %d; \t%s", nodeNum, this.state, myLocation.toString(), myPatrolArea.toString(), this.heartBeatCount, result);
	}

}
