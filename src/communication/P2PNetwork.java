package communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import utils.Node;

public class P2PNetwork {

	public Node localNode;

	List<Message> delay_receive_queue;
	List<Message> receive_queue;
	
	List<Node> foundNodes;
	List<Node> neighborNodes;

	public P2PNetwork(Node myself) {
		/* Initiate the fields */
		this.delay_receive_queue = new ArrayList<Message>();
		this.receive_queue = new ArrayList<Message>();
		this.foundNodes = new ArrayList<Node>();
		
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
		if (!findFirstNodeByPort()){
			System.out.println("I am the first node");
		}
		
	}

	public boolean findFirstNodeByPort(){
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
				Message message = new Message(testIP, port,"getParam",this.localNode);
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
			System.out.println("Connection created");
			
		} catch (UnknownHostException e) {
			System.out.println("Client Socket error:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("Client EOF error:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Client readline error:" + e.getMessage());
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
		if (message.kind.equals("getParam")) {
			System.out.println("Recevied \"getParam\"");
			Node newNode = message.getNode();
			this.foundNodes.add(newNode);
			Message msg = new Message(newNode.ip,newNode.port,"parameters", this.localNode);
			this.send(msg);
			return;
		}  else if (message.kind.equals("parameters")){
			// add this node's parameters to myself
			System.out.println("Recevied \"parameters\"");
			Node newNode = message.getNode();
			this.foundNodes.add(newNode);
			// TODO: take some action to place myself
			return;
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
		int numFoundNodes = this.foundNodes.size();
		for (int i = 0; i<numFoundNodes; i++){
			System.out.println(i+": "+this.foundNodes.get(i).toString());
		}
		System.out.println("------------------------------");
	}
	
}

class Connection extends Thread {

	DataInputStream in;
	DataOutputStream out;
	ObjectOutputStream outObj;
	ObjectInputStream inObj;

	Socket clientSocket;
	P2PNetwork p2p;

	public Connection(Socket aClientSocket, P2PNetwork p2p) {
		this.p2p = p2p;
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			outObj = new ObjectOutputStream(clientSocket.getOutputStream());
			inObj = new ObjectInputStream(clientSocket.getInputStream());

			this.start();
		} catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	public void run() {
		try {
			while (true) {
				Message message = (Message) inObj.readObject();
				p2p.receive_message(message, this);
			}

		} catch (EOFException e) {
			System.out.println("Server EOF:" + e.getMessage());
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to convert received object!");
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
		try {
			synchronized (outObj) {
				this.outObj.writeObject(object);
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
