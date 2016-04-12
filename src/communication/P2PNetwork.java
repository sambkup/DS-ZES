package communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.google.gson.Gson;
import communication.Message.messageKind;
import utils.Node;
import org.json.*;


public class P2PNetwork {

	public Node localNode;

	List<Message> delay_receive_queue;
	List<Message> receive_queue;
	
	HashMap<String, Node> foundNodes;
	List<Node> neighborNodes;

	public P2PNetwork(Node myself) {
		/* Initiate the fields */
		this.delay_receive_queue = new ArrayList<Message>();
		this.receive_queue = new ArrayList<Message>();
		this.foundNodes = new HashMap<String, Node>();

		
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
		
		/* bootstrap */
		System.out.println("Starting bootstrapping...");
		if (!findFirstNodeByIP()){
			System.out.println("I am the first node");
		}
		
	}
	
	private boolean findFirstNodeByIP(){
		String myIP = this.localNode.ip;
		String delims = "[.]";
		String[] chunks = myIP.split(delims);

		int maxIP = 256;
		String testIP;
		for (int i = 1; i<maxIP; i++){
			if (Integer.toString(i).equals(chunks[3])){
				continue;
			}

			testIP = chunks[0]+"."+chunks[1]+"."+chunks[2]+"."+i;
			Socket s = null;
			try {
				InetSocketAddress endpoint = new InetSocketAddress(testIP, this.localNode.port);
				s = new Socket();
				s.connect(endpoint, 100);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				//System.out.println(testIP + " - Closed");
				continue;
			}
			if (s != null){
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println(testIP + " - Found first node");
			return true;
		}
		return false;

	}


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
				Message message = new Message(testIP, port,messageKind.GET_PARAM,this.localNode);
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
			System.out.println("connection created");
			s.setKeepAlive(true);

			connection_to_use = new Connection(s, this);
			System.out.println("Connection created");
			
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

		System.out.println("Sending message");
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
		case GET_PARAM:
			System.out.println("Recevied \"GET_PARAM\"");
			this.send(new Message(newNode.ip,newNode.port,messageKind.GET_PARAM_RESPONSE, this.localNode));
			return;
			
		case GET_PARAM_RESPONSE:
			System.out.println("Recevied \"GET_PARAM_RESPONSE\"");
			this.foundNodes.put(newNode.getName(), newNode);
			// request to update patrol region
			this.send(new Message(newNode.ip,newNode.port,messageKind.REQ_UPDATED_PATROL, this.localNode));
			return;
			
		case REQ_UPDATED_PATROL:
			System.out.println("Recevied \"REQ_UPDATED_PATROL\"");
			if (!localNode.inMyArea(newNode)){
				// node not in my region
				// TODO: find which node the newNode should ask next
				this.send(new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_NACK, this.localNode));
			}
			else{
				// send newNode's location
		
		
					newNode = localNode.splitPatrolArea(newNode); //split if sender is not phone
				
				this.send(new Message(newNode.ip,newNode.port,messageKind.UPDATE_PATROL_ACK, newNode));

				// store new Node:
				this.foundNodes.put(newNode.getName(), newNode);
				
				// send my updated location
				this.send(new Message(newNode.ip,newNode.port,messageKind.SEND_UPDATED_PARAM, this.localNode));
			}
			return;
			
		case SEND_UPDATED_PARAM:
			this.foundNodes.put(newNode.getName(), newNode);
			return;
			
		case SEND_PARAM:
			System.out.println("Recevied \"SEND_PARAM\"");
			this.foundNodes.put(newNode.getName(), newNode);
			return;
		case REQ_START:
				System.out.println("Received \"REQ_START\"");
				/* check if myloc is within my patrol area */
				System.out.println(message.toString());
				if(this.localNode.inMyArea(newNode))	{
					this.send((new Message(newNode.ip,newNode.port,messageKind.MY_AREA, this.localNode)));
				}
				else{
					this.send(new Message(newNode.ip,newNode.port,messageKind.NOT_MY_AREA, this.localNode));
				}
				return;
		case MSG_JSON:
				System.out.println("Received \"MSG_JSON\"");
			/*	//if final JSON, send it to the phone. if I am the phone, myip = phoneip. then pass it back to clientActivtiy
				if((localNode.ip.equalsIgnoreCase(message.phoneIP))&(localNode.port==message.phonePort)) {
					ClientActivity.getInstance().setJSONObjectToSendInstance(message.getJsonRoute());
					return;
				}
*/


				//if start node = myself,I am the device and  it is the final JSON, send it to the phone
				if(message.getStartNodeIP().equalsIgnoreCase(localNode.ip)){
					message.setDestIP(message.phoneIP);  //setting as phone
					message.setDestPort(message.phonePort);

				}
				else{
					message.setDestIP(this.foundNodes.get(1).ip);  //setting as first neighbour, should change to nearest node
					message.setDestPort(this.foundNodes.get(1).port);

				}



				//if destLoc is in my patrol area, add my location to json and send to start node
				JSONObject newJSON = message.getJsonRoute();
					//Add my location to the json Object if I'm safe, send it to my nearest neighbour
				if(this.localNode.getState().equalsIgnoreCase("SAFE")){
					try {
						newJSON = this.localNode.enterJSON(message.getJsonRoute());
					}catch (Exception ex){
						System.out.println("error in entering my location to JSON");
					}
				}
				//send JSON and message to next node
				message.setJsonRoute(newJSON);

				this.send(message);
				return;
			case MY_AREA:
				System.out.println("Received \"MY_AREA\"");
				//establish a permanent connection with it and wait for JSON
				this.send(new Message(newNode.ip,newNode.port,messageKind.MSG_JSON, this.localNode));
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
		System.out.println("Starting MessagePasser server with address = " + this.localNode.address);
		int counter = 0;
		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(this.localNode.port);

			while (true) {
				Socket clientSocket = listenSocket.accept();				
				new Connection(clientSocket, this);
				System.out.println("Server received a new connection: # " + counter);
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

	public void printFoundNodes(){
		System.out.println("Printing Found Nodes----------");
		System.out.println(this.localNode.getName()+" : "+this.localNode.toString());
		
		for (Entry<String, Node> entry : this.foundNodes.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}

//		int numFoundNodes = this.foundNodes.size();
//		for (int i = 0; i<numFoundNodes; i++){
////			System.out.println(" "+i+": "+this.foundNodes.get(i).toString());
//			System.out.println(" "+i+": "+this.foundNodes..get(i).toString());
//		}
		System.out.println("------------------------------");
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
				System.out.println("calling receive message function");
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
