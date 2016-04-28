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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import communication.Message.messageKind;
import utils.Node;
import utils.Node.SensorState;
import utils.NodeLocation;


public class P2PNetwork {

	private final ScheduledExecutorService heatBeatScheduler = Executors.newScheduledThreadPool(1);

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
		
		/* run server */
		Thread server = new Thread() {
			public void run() {
				listen_server();
			}
		};
		server.start();
		
		
		/* bootstrap */
		System.out.println("Starting bootstrapping...");
		if (!findFirstNodeByIP()){
			System.out.println("I am the first node");
		}
		
		
		/* Run the overlay for demo */
		
		run_display_overlay(this);
		
		/* run the heartbeat */
		
		try {
			runHeartBeat(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		/* Check for an update to the state */
//		
//		Thread reader = new Thread() {
//			public void run() {
//				while (true) {
//					try {
//						sleep(5000);
//						readfiles("resources/state.txt");
//					} catch (Exception e) {
//						System.out.println(e.getMessage());
//					}
//				}
//			}
//		};
//		reader.start();

		
		
		 
	}
	public void readfiles(String name) throws IOException{
		File file = new File(name);
		String line;
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		line = br.readLine();
		if(line.equalsIgnoreCase("0")){
			this.localNode.setSafe();
			
		}
		else if(line.equalsIgnoreCase("1")){
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
				
				/* found the first node, so ask to be placed */
				
				Message message = new Message(testIP, this.localNode.port,messageKind.REQ_UPDATED_PATROL,this.localNode);
				send(message);	
				
				return true;
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				socket did not successfully open
//				System.out.println(testIP+":"+this.localNode.port + " - Closed");
				continue;
			}
		}
		return false;

	}

	public void send(Message message) {
		String ip = message.destIP;
		int port = message.destPort;
		if (message.getKind() != messageKind.HEARTBEAT){
			System.out.println("Sending message "+message.getKind()+" to "+ip+":"+port);
		}


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
//			e.printStackTrace();
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
		
		switch (message.kind) {
			
		/* Node message handlers */
		
		//TODO: wrap the whole thing in synchronized, not just each step
		
		case REQ_UPDATED_PATROL:
			System.out.println("Recevied \"REQ_UPDATED_PATROL\"");
			

			synchronized(this.neighborNodes){

				// 1. Check if I can split -> if not, send next closest node
				
				if (!localNode.inMyArea(newNode)){
					// TODO: find which node the newNode should ask next
					Node nextNode = newNode.findClosestNode(newNode.myLocation.getLocation(), this.neighborNodes,null);
					System.out.printf("Next closest node is %s\n", nextNode.toString());
					this.send(new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_NACK, nextNode));
					return;
				}

				
				// 2. Split the patrol area, send update to NewNode
				Message newMessage = null;

				newNode = localNode.splitPatrolArea(newNode); //split if sender is not phone			
				newMessage = new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_ACK, newNode);
				newMessage.setNewNode(newNode);
				newMessage.setSplitNode(this.localNode);
				System.out.println("NewNode set to: "+newNode.getName() + " SplitNode set to: "+this.localNode.getName() );

				newMessage.setNeighborNodes(this.neighborNodes);
				// TODO: maybe need to syncronize this?
				this.send(newMessage);


				// 3. send NEIGHBOR_UPDATE to all my neighbors
				newMessage.setKind(messageKind.NEIGHBOR_UPDATE);
				for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
					// need to clone the message here just in case newMessage updates before sending
					newMessage.setDestIP(entry.getValue().ip);
					newMessage.setDestPort(entry.getValue().port);
					this.send(newMessage);
				}

				// 4. check if any of my neighbors are still neighbors
				for (Iterator<Map.Entry<String,Node>> it = this.neighborNodes.entrySet().iterator(); it.hasNext();){
					Map.Entry<String,Node> entry = it.next();
					if (!this.localNode.isNeighbor(entry.getValue())){
						// not a neighbor - drop it
						System.out.println(entry.getKey() + " is no longer a neighbor");
						it.remove();
					}
				}
				// 5. Add NewNode to my neighbors
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
			
			synchronized(this.neighborNodes){
				
				System.out.println("Checking Neighbor Nodes----------");
				for (Entry<String, Node> entry : message.getNeighborNodes().entrySet()) {
					System.out.println(entry.getValue().toString());
				}
				System.out.println("---------------------------------");
				
				// 2. Check if any of the neighbors are mine
				for (Entry<String, Node> entry : message.getNeighborNodes().entrySet()) {
					// need to clone the message here just in case newMessage updates before sending
					if (this.localNode.isNeighbor(entry.getValue())){
						// not a neighbor - drop it
						this.neighborNodes.put(entry.getKey(), entry.getValue());
					}
				}

				// 3. Add SplitNode to my neighbors
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
				} else {
					System.out.println("NewNode is not a neighbor");
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
			
		case HEARTBEAT:
//			System.out.println("Recevied \"HEARTBEAT\"");
			// when receive a heartbeat, reset the counter for this node.
			String key = newNode.getName();
			synchronized(this.neighborNodes){
				this.neighborNodes.get(key).resetHeartBeat();
				if (newNode.getState() == SensorState.SAFE){
					this.neighborNodes.get(key).setSafe();
				} else{
					this.neighborNodes.get(key).setUnsafe();
				}
			}

			return;
					
		/* Overlay message handlers */

			
		case STATE_TOGGLE:
			System.out.println("Recevied \"STATE_TOGGLE\"");
			synchronized (this.localNode){
				if (this.localNode.getState() == SensorState.SAFE){
					this.localNode.setUnsafe();
				} else{
					this.localNode.setSafe();
				}
			}
			return;
			
		/* App message handlers */
	
		case REQ_START:
			System.out.println("Received \"REQ_START\"");
			Node sourceNode = message.getSenderNode();

			/* check if myloc is within my patrol area */
//			System.out.println(message.toString());
			if(this.localNode.inMyArea(sourceNode))	{
				this.send((new Message(sourceNode.ip,sourceNode.port,messageKind.MY_AREA, this.localNode)));
			} else{
				Node closeNeighbour = this.localNode.findClosestNode(sourceNode.myLocation.getLocation(), this.neighborNodes,null);
				if (closeNeighbour != null){
					System.out.println(closeNeighbour.ip);
					Message returnMessage = new Message(sourceNode.ip,sourceNode.port,messageKind.NOT_MY_AREA, this.localNode);
					returnMessage.setClosestNode(closeNeighbour);
					this.send(returnMessage);
				} else {
					// I am the only node, so complete the loop
					
					ArrayList<String> newJSON = new ArrayList<String>();
					if (this.localNode.getState() == SensorState.SAFE) {
						try {
							newJSON = this.localNode.enterLocation(new ArrayList<String>());
						} catch (Exception ex) {
							System.out.println("error in entering my location to JSON");
							ex.printStackTrace();
						}
					}
					message.setJsonRoute(newJSON);
					message.setDestIP(message.phoneIP);  //setting as phone
					message.setDestPort(message.phonePort);
					message.setClosestNode(this.localNode);
					this.send(message);
				}
			}
			return;
	case MSG_JSON:
		System.out.println("Received \"MSG_JSON\"");


		/**********************new logic********************/
		//check if destloc is in my area. if yes, enter my location and send to phone
		//else enter my location and send to closest neighbour from destlocation
		//TODO: if my neighbours are unsafe, send back to the sender and ask to send to a different neighbour


	

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
			/*enter my location to json */
			//	JSONObject newJSON = message.getJsonRoute();
			ArrayList<String> newJSON = new ArrayList<String>();
			if (this.localNode.getState() == SensorState.SAFE) {
				try {
					newJSON = this.localNode.enterLocation(message.getJsonRoute());
				} catch (Exception ex) {
					System.out.println("error in entering my location to JSON");
					ex.printStackTrace();
				}
			}
			message.setJsonRoute(newJSON);
			System.out.println("Route is "+message.jsonRoute.toString());
		} else{
			Node closestNeighbor = localNode.findClosestNode(latLong, neighborNodes,message.senderNode);
			if(closestNeighbor!=null){
				System.out.println("Closest neighbor is: "+closestNeighbor.getName());
				message.setDestIP(closestNeighbor.ip); 
				message.setDestPort(closestNeighbor.port);	
				/*enter my location to json */
				//	JSONObject newJSON = message.getJsonRoute();
				ArrayList<String> newJSON = new ArrayList<String>();
				if (this.localNode.getState() == SensorState.SAFE) {
					try {
						newJSON = this.localNode.enterLocation(message.getJsonRoute());
					} catch (Exception ex) {
						System.out.println("error in entering my location to JSON");
						ex.printStackTrace();
					}
				}
				message.setJsonRoute(newJSON);
			} else{ //i dont have any safe neighbors other than the sender, send it to sender
				message.setKind(messageKind.NO_NEIGHBORS);
				message.setDestIP(message.senderNode.ip);
				message.setDestPort(message.senderNode.port);
			}
		}
		message.setSenderNode(localNode);
		this.send(message);	
		return;
		
		/* send to a different neighbor, my location already in route - so need not enter;*/
		
	case NO_NEIGHBORS:
		
		String destloc2 = message.getDestLoc();
		String latLng2[] = destloc2.split(",");
		double latLong2[] = new double[2];
		latLong2[0] = Double.parseDouble(latLng2[0]);
		latLong2[1] = Double.parseDouble(latLng2[1]);
//		NodeLocation destLoc2 = new NodeLocation(latLong2);
		Node closestNeighbor = localNode.findClosestNode(latLong2, neighborNodes,message.senderNode);
		if(closestNeighbor!=null){
			System.out.println(" Next Closest neighbor is: "+closestNeighbor.getName());
			message.setKind(messageKind.MSG_JSON);
			message.setDestIP(closestNeighbor.ip); 
			message.setDestPort(closestNeighbor.port);	
		} else{ //NOT SURE WHAT TO DO! send a game over message?
			message.setKind(messageKind.MSG_JSON);
			message.setDestIP(message.phoneIP);  //setting as phone
			message.setDestPort(message.phonePort);	
		}
		message.setSenderNode(localNode);
		this.send(message);	
		break;
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
//		int counter = 0;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(this.localNode.port);

			while (true) {
				Socket clientSocket = listenSocket.accept();				
				new Connection(clientSocket, this);
//				System.out.println("Server received a new connection: # " + counter + " ip: "+clientSocket.getRemoteSocketAddress());
//				counter++;
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
	
	
	private void run_display_overlay(final P2PNetwork p2p) {
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

		String serverName = "192.168.2.123";
		int port = 31337;
		try
		{
			InetAddress address = InetAddress.getByName(serverName);
			Socket client = new Socket(address, port);
			ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
			oos.writeObject(this.localNode);
//			System.out.println("Overlay updated");

			client.close();
		}catch(IOException e)
		{
			System.out.println("Overlay phone not found");
//			e.printStackTrace();
		}
		
		
		
	}

	
	private void runHeartBeat(final P2PNetwork p2p) throws IOException {
		Runnable update = new Runnable() {
			public void run() {
				p2p.sendHeartBeat();
			}
		};
		// update the config file every 'time_interval'
		this.heatBeatScheduler.scheduleAtFixedRate(update, 0, 1, TimeUnit.SECONDS);
	}

	
	
	
	private void sendHeartBeat(){
		// go through each neighbor and sent a heartbeat
		Message newMessage = new Message(null,0,messageKind.HEARTBEAT,this.localNode);

//		System.out.println("Sending Heartbeat");
		synchronized(this.neighborNodes){
			for (Entry<String, Node> entry : this.neighborNodes.entrySet()) {
				// need to clone the message here just in case newMessage updates before sending
				if (!entry.getValue().heartBeat() ){
					System.out.println("Node: " + entry.getValue().getName() + " is dead");
				}
				newMessage.setDestIP(entry.getValue().ip);
				newMessage.setDestPort(entry.getValue().port);
				this.send(newMessage);
			}			
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
				Message message = gson.fromJson(json, Message.class);				
				
				if (message.kind != messageKind.HEARTBEAT){
					System.out.println("Received message "+message.getKind()+" from "+this.clientSocket.getInetAddress()+":"+this.clientSocket.getPort());
				}
				p2p.receive_message(message, this);
			}

		} catch (EOFException e) {
//			System.out.println("Server EOF:" + e.getMessage());
			
		} catch (IOException e) {
//			System.out.println(e.getMessage());

			
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
