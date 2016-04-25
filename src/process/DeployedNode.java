package process;

import java.net.InetAddress;
import java.net.UnknownHostException;

import communication.P2PNetwork;
import utils.Node;
import utils.NodeLocation;
import utils.NodePatrolArea;
import utils.P2PRegion;

public class DeployedNode {

	protected static final String receive_block = new String();
	static P2PNetwork p2p;
	static P2PRegion region;
	static NodePatrolArea initial_patrol_area;
	static NodeLocation node_loc;
	static Node myNode;
	static double[] location = new double[2];
	static String IP;

	public static void main(String[] args) {

		// --------------------------------
		// initialize - get necessary parameter inputs

		if (args.length == 0) {
			// go to a default value
			location[0] = 40.442546;
			location[1] = -79.941759;
			IP = findMyIPaddr(); 
		} else if (args.length != 3) {
			System.out.println("Usage: IP lattitude longitude");
			System.exit(0);
		} else {
			IP = args[0];
			location[0] = Double.parseDouble(args[1]);
			location[1] = Double.parseDouble(args[2]);		
		}

		// --------------------------------
		// initialize - Hard-coded parameters
		double[] range = {40.44294,-79.94242,40.44316,-79.94220};
		int port = 4001; 


		// --------------------------------
		// construct the required objects

		region = new P2PRegion(range);
		initial_patrol_area = new NodePatrolArea(range);
		node_loc = new NodeLocation(location);
		myNode = new Node(initial_patrol_area,region,node_loc,port,IP);
	    System.out.println("Starting DeployedNode.jar");
	    System.out.println("MyNode: "+myNode.toString());

		p2p = new P2PNetwork(myNode);


		// --------------------------------
		// Execute something here

		run_receiver(p2p);
	}
	private static void run_receiver(final P2PNetwork p2p) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					p2p.printNeighborNodes();
				}
			}
		};

		receiver_thread.start();
	}

	private static String findMyIPaddr(){
		// TODO: make this failure tolerant
		InetAddress x = null;
		try {
			x = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return x.getHostAddress();
	}

}
