package communication;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.gson.Gson;
import communication.Message.messageKind;
import utils.Node;
import utils.Node.SensorState;
import utils.NodeLocation;

import org.json.*;


public class P2PNetwork {

	public Node localNode;

	List<Message> delay_receive_queue;
	List<Message> receive_queue;
	
	HashMap<String, Node> neighborNodes;

	public P2PNetwork(Node myself) {
		/* Initiate the fields */
		this.delay_receive_queue = new ArrayList<Message>();
		this.receive_queue = new ArrayList<Message>();
		this.neighborNodes = new HashMap<String, Node>();

		
		this.localNode = myself;

		if (this.localNode.ip == null || this.localNode.port == 0) {
			System.out.println("Unknown IP or Port!");
			// TODO: throw an exception - maybe IncorrectPortIP
			return;
		}
		
		// run server
		Thread server = new Thread() {
			public void run() {
				listen_server();
			}
		};
		server.start();
		
		Thread reader = new Thread() {
			public void run() {
				while (true) {
					try {
						sleep(5000);
						readfiles("state.txt");
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
		};
		reader.start();
		
		/* bootstrap */
		System.out.println("Starting bootstrapping...");
		if (!findFirstNodeByIP()){
			System.out.println("I am the first node");
		}
		
		run_display_overlay(this);
		
		 
	}
	public void readfiles(String name) throws IOException{
		File file = new File(name);
		String line;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		line = br.readLine();
		if(line.equalsIgnoreCase("safe")){
			this.localNode.setSafe();
			
		}
		else if(line.equalsIgnoreCase("danger")){
			this.localNode.setUnsafe();
		}
		fr.close();
		br.close();
		return;			
	}

	private boolean findFirstNodeByIP(){
		String myIP = this.localNode.ip;
		String delims = "[.]";
		String[] chunks = myIP.split(delims);

		int maxIP = 150;
		String testIP;
		for (int i = 100; i<maxIP; i++){
			if (Integer.toString(i).equals(chunks[3])){ 
				continue;
			}

			testIP = chunks[0]+"."+chunks[1]+"."+chunks[2]+"."+i;
			Socket s = null;
			try {
				InetSocketAddress endpoint = new InetSocketAddress(testIP, this.localNode.port);
				s = new Socket();
				s.connect(endpoint, 101);
				
				s.close();
				System.out.println(testIP+":"+this.localNode.port + " - Found first node");	
				Message message = new Message(testIP, this.localNode.port,messageKind.REQ_UPDATED_PATROL,this.localNode);
				send(message);	
				return true;
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				socket did not successfully open
				System.out.println(testIP+":"+this.localNode.port + " - Closed");
				continue;
			}
		}
		return false;

	}


	@SuppressWarnings("unused")
	private boolean findFirstNodeByPort(){
		String testIP = this.localNode.ip;
		int localport = this.localNode.port;
		int startport = 4000;
		int endport = 4200;
		
		// TODO: randomize the start point, so the earlier ports aren't overwhelmed
		for (int port = startport; port <= endport; port++){
			// Ignore myself
			if (port == localport){
				continue;
			}
			
			Socket s = null;
			try {
				s = new Socket(testIP, port);
				
				// if a socket successfully opened
				s.close();
				System.out.println(testIP+":"+port + " - Found first node");	
				Message message = new Message(testIP, port,messageKind.REQ_UPDATED_PATROL,this.localNode);
				send(message);	
				return true;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				socket did not successfully open
//				System.out.println(testIP+":"+port + " - Closed");
			}
		}
		return false;
	}

	public void send(Message message) {
		String ip = message.destIP;
		int port = message.destPort;
		
		System.out.println("Sending message to "+ip+":"+port);

		Socket s = null;
		Connection connection_to_use = null;

		try {
			s = new Socket(ip, port);
			s.setKeepAlive(true);
			connection_to_use = new Connection(s, this);
			
		} catch (UnknownHostException e) {
			System.out.println("Client Socket error:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("Client EOF error:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Client readline error:" + e.getMessage());
			e.printStackTrace();
		}
		
		if (connection_to_use == null) {
			System.out.println("Failed to find or open connection");
			return;
		}

		connection_to_use.write_object(message);
		connection_to_use.close();

	}

	public Message receive() {
		int size = 0;
		synchronized (receive_queue) {
			size = receive_queue.size();
		}
		if (size < 1)
			return null;

		Message retreive = null;
		synchronized (receive_queue) {
			retreive = receive_queue.remove(0);
		}

		return retreive;
	}

	public synchronized void receive_message(Message message, Connection c) {
		
		Node newNode = message.getNode();
	
//		System.out.println("new node's ip is "+newNode.ip);
	
		switch (message.kind) {
			
		case REQ_UPDATED_PATROL:
			System.out.println("Recevied \"REQ_UPDATED_PATROL\"");
			// 1. Check if I can split, if not, send next closest node
			if (!localNode.inMyArea(newNode)){
				// TODO: find which node the newNode should ask next
				Node nextNode = newNode.findClosestNode(newNode.myLocation.getLocation(), this.neighborNodes);
				System.out.printf("Next closest node is %s\n", nextNode.toString());
				this.send(new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_NACK, nextNode));
				return;
			}
			
			// 2. Split the patrol area, send update to NewNode
			Message newMessage = null;
			synchronized(this.neighborNodes){
				newNode = localNode.splitPatrolArea(newNode); //split if sender is not phone			
				newMessage = new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_ACK, newNode);
				newMessage.setNewNode(newNode);
				System.out.println("NewNode set to: "+newNode.getName());
				newMessage.setSplitNode(this.localNode);
				newMessage.setNeighborNodes(this.neighborNodes);
				this.send(newMessage);
			}

			
			// 3. send NEIGHBOR_UPDATE to all my neighbors
			newMessage.setKind(messageKind.NEIGHBOR_UPDATE);
			synchronized(this.neighborNodes){
				for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
					// need to clone the message here just in case newMessage updates before sending
					newMessage.setDestIP(entry.getValue().ip);
					newMessage.setDestPort(entry.getValue().port);
					this.send(newMessage.clone());
				}
			}


			
			// 4. check if any of my neighbors are still neighbors
			synchronized(this.neighborNodes){
				for (Iterator<Map.Entry<String,Node>> it = this.neighborNodes.entrySet().iterator(); it.hasNext();){
					Map.Entry<String,Node> entry = it.next();
					if (!this.localNode.isNeighbor(entry.getValue())){
						// not a neighbor - drop it
						it.remove();
					}
				}
				
				
				
//				for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
//					// need to clone the message here just in case newMessage updates before sending
//					if (!this.localNode.isNeighbor(entry.getValue())){
//						// not a neighbor - drop it
//						this.neighborNodes.remove(entry.getKey());
//					}
//				}
			}
			// 5. Add NewNode to my neighbors
			synchronized(this.neighborNodes){
				this.neighborNodes.put(newNode.getName(), newNode.clone());
			}

			
			return;
			
		case UPDATE_PATROL_NACK:
			System.out.println("Recevied \"UPDATE_PATROL_NACK\"");
			this.send(new Message(newNode.ip,newNode.port,messageKind.REQ_UPDATED_PATROL, this.localNode));
			return;
			
		case UPDATE_PATROL_ACK:
			System.out.println("Recevied \"UPDATE_PATROL_ACK\"");
			// 1. Update my node's information
			this.localNode = message.getNewNode();
			
			// 2. Check if any of the neighbors are mine
			synchronized(this.neighborNodes){
				for (Entry<String, Node> entry : message.getNeighborNodes().entrySet()) {
					// need to clone the message here just in case newMessage updates before sending
					if (this.localNode.isNeighbor(entry.getValue())){
						// not a neighbor - drop it
						this.neighborNodes.put(entry.getKey(), entry.getValue());
					}
				}
			}

			
			// 3. Add SplitNode to my neighbors
			synchronized(this.neighborNodes){
				this.neighborNodes.put(message.getSplitNode().getName(), message.getSplitNode().clone());
			}
			
			
			return;
			
		case NEIGHBOR_UPDATE:
			System.out.println("Recevied \"NEIGHBOR_UPDATE\"");

			System.out.println("NewNode: "+message.getNewNode().getName());
			System.out.println("SplitNode: "+message.getSplitNode().getName());
			
			synchronized(this.neighborNodes){

			
			
				// 1. check if newnode is a neighbor
				if (this.localNode.isNeighbor(message.getNewNode())){
					System.out.println("NewNode is a neighbor");
					this.neighborNodes.put(message.getNewNode().getName(), message.getNewNode().clone());
				}


			
				// 2. check if SplitNode is still a neighbor
				if (!this.localNode.isNeighbor(message.getSplitNode())){
					// not a neighbor - drop it
					System.out.println("SplitNode is no longer a neighbor");
					this.neighborNodes.remove(message.getSplitNode().getName());
				} else {
					// is a neighbor, update it
					System.out.println("SplitNode is still a neighbor");
					this.neighborNodes.put(message.getSplitNode().getName(),message.getSplitNode());
				}
				
			}


			return;
					
					
		case REQ_START:
				System.out.println("Received \"REQ_START\"");
				/* check if myloc is within my patrol area */
//				System.out.println(message.toString());
				if(this.localNode.inMyArea(newNode))	{
					this.send((new Message(newNode.ip,newNode.port,messageKind.MY_AREA, this.localNode)));
				}
				else{
					Node closeNeighbour = this.localNode.findClosestNode(newNode.myLocation.getLocation(), this.neighborNodes);
					System.out.println(closeNeighbour.ip);
					Message returnMessage = new Message(newNode.ip,newNode.port,messageKind.NOT_MY_AREA, this.localNode);
					returnMessage.setClosestNode(closeNeighbour);
					this.send(returnMessage);
				}
				return;
		case MSG_JSON:
				System.out.println("Received \"MSG_JSON\"");

				
				/**********************new logic********************/
				//check if destloc is in my area. if yes, enter my location and send to phone
				//else enter my location and send to closest neighbour from destlocation
				//TODO: if my neighbours are unsafe, send back to the sender and ask to send to a different neighbour
				
				
				/*enter my location to json */
			JSONObject newJSON = message.getJsonRoute();
			if (this.localNode.getState() == SensorState.SAFE) {
				try {
					newJSON = this.localNode.enterJSON(message.getJsonRoute());
				} catch (Exception ex) {
					System.out.println("error in entering my location to JSON");
					ex.printStackTrace();
				}
			}
			message.setJsonRoute(newJSON);
			
				/*get destination of user*/
				String destloc = message.getDestLoc();
				String latLng[] = destloc.split(",");
				double latLong[] = new double[2];
				latLong[0] = Double.parseDouble(latLng[0]);
				latLong[1] = Double.parseDouble(latLng[1]);
				NodeLocation destLoc = new NodeLocation(latLong);
				
				/*if my area, send to phone else to closest neighbour*/
				if(this.localNode.myPatrolArea.inMyArea(destLoc)){
					System.out.println("I am the last node in the chain");
					message.setDestIP(message.phoneIP);  //setting as phone
					message.setDestPort(message.phonePort);
				}
				else{
					Node closestNeighbor = localNode.findClosestNode(latLong, neighborNodes);
					message.setDestIP(closestNeighbor.ip); 
					message.setDestPort(closestNeighbor.port);	
				}
				this.send(message);	
				return;
		default:
			break;
		}
		
		// the receive buffer from the Homeworks
		synchronized (receive_queue) {
			receive_queue.add(message);
		}

		// put delayed messages into receive queue
		for (Message msg : new ArrayList<Message>(this.delay_receive_queue)) {
			synchronized (receive_queue) {
				receive_queue.add(msg);
			}
		}
		delay_receive_queue.clear();
	}

	private void listen_server() {
		System.out.println("Starting MessagePasser server with address = " + this.localNode.getName());
		int counter = 0;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(this.localNode.port);

			while (true) {
				Socket clientSocket = listenSocket.accept();				
				new Connection(clientSocket, this);
				System.out.println("Server received a new connection: # " + counter + " ip: "+clientSocket.getRemoteSocketAddress());
				counter++;
			}
		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		} finally {
			try {
				if (listenSocket != null)
					System.out.println("Closing socket");
					listenSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close server.");
			}
		}

	}
	
	
	private void run_display_overlay(P2PNetwork p2p) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					p2p.sendOverlay();
				}
			}
		};

		receiver_thread.start();
	}

	private void sendOverlay(){
		/*
		 * code here to send to tablet
		 */

		String serverName = "192.168.2.121";
		int port = 31337;
		try
		{
			InetAddress address = InetAddress.getByName(serverName);
			System.out.println("Connecting to " + serverName +" on port " + port);
			Socket client = new Socket(address, port);
			System.out.println("Just connected to " + client.getRemoteSocketAddress());
			ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

			//create node object
			//public Node(NodePatrolArea myPatrolArea, P2PRegion p2pPatrolArea, NodeLocation myLocation, int port, String ip) {
			//NodePatrolArea range --> {min_lattitude, min_longitute, max_latitude, max_longitude}
//			double[] range = {40.44294, -79.94242, 40.44316, -79.9422};
//			NodePatrolArea sampleNPA = new NodePatrolArea(range);
//			double[] nodePlacement = {40.4431325, -79.9423925};
//			P2PRegion p2preg = new P2PRegion(range);
//			NodeLocation nodeLocation = new NodeLocation(nodePlacement);
//			Node sampleNode = new Node(sampleNPA, p2preg, nodeLocation, 123, "hehehe");


			oos.writeObject(this.localNode);


			client.close();
		}catch(IOException e)
		{
			System.out.println("Overlay phone not found");
//			e.printStackTrace();
		}
		
		
		
	}
	
	
	@SuppressWarnings("unused")
	private Node findNextNeighbor(Node testNode){
		/*
		 * Look through all the neighbors. 
		 * If any neighbor contains the new node, then return that neighbor
		 * If no neighbor contains it, figure out which neighbor should be checked next.
		 */
		for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
		    if (entry.getValue().inMyArea(testNode)){
		    	return entry.getValue();
		    }
		}
		
		
 
		return null;
	}

	public void printNeighborNodes(){
		System.out.println("Printing Neighbor Nodes----------");

		synchronized(this.neighborNodes){
			for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
				System.out.println(entry.getValue().toString());
			}
		}
		System.out.println("---------------------------------");
	}
	
}

class Connection extends Thread {

	DataInputStream in;
	DataOutputStream out;

	Socket clientSocket;
	P2PNetwork p2p;

	public Connection(Socket aClientSocket, P2PNetwork p2p) {
		this.p2p = p2p;
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());

			this.start();
		} catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	public void run() {
		Gson gson = new Gson();
		try {
			while (true) {
								
				String json = in.readUTF();
//				System.out.println(json);
				Message message = gson.fromJson(json, Message.class);
//				System.out.println(message.toString());
//				System.out.println("calling receive message function");
				p2p.receive_message(message, this);
			}

		} catch (EOFException e) {
			System.out.println("Server EOF:" + e.getMessage());
			
		} catch (IOException e) {
			System.out.println(e.getMessage());

			
		} finally {
			try {
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println("Failed to close connection!");
			}
		}

	}

	public void write_object(Object object) {

		Gson gson = new Gson();

		String json = gson.toJson(object);

		try {
			synchronized (out) {
				this.out.writeUTF(json);
			}
		} catch (IOException e) {
			System.out.println("error sending message - client side:" + e.getMessage());
			e.printStackTrace();
		}


	}

	public void close() {
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close connection: ");
			e.printStackTrace();
		}
	}

}
