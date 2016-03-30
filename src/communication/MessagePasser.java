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
import java.util.HashMap;
import java.util.List;
import communication.Connection.ConnectionStatus;
import services.ClockService;
import utils.P2P;
import utils.Node;

public class MessagePasser {

	ClockService clockService;
	P2P p2pNetwork;
	public Node localNode;

	List<Message> delay_receive_queue;
	List<Message> receive_queue;

	HashMap<String, Connection> connection_list;

	public MessagePasser(Node myself, ClockService newClockService) {
		this.clockService = newClockService;
		this.connection_list = new HashMap<String, Connection>();
		this.delay_receive_queue = new ArrayList<Message>();
		this.receive_queue = new ArrayList<Message>();

		this.localNode = myself;
		this.p2pNetwork = new P2P(this.localNode);

		if (this.localNode.ip == null || this.localNode.port == 0) {
			System.out.println("Unknown IP or Port!");
			return;
		}

		// run server
		Thread server = new Thread() {
			public void run() {
				listen_server();
			}
		};
		server.start();
	}

	public Connection open_connection(Node node) {

		if (node == null || node.ip == null || node.port == 0 || node.name == null) {
			return null;
		}
		Socket s = null;

		try {
			s = new Socket(node.ip, node.port);
			s.setKeepAlive(true);

			Connection c = new Connection(s, this);
			// c.print();
			synchronized (connection_list) {
				connection_list.put(node.name, c);
			}
			Message marco = new Message(node.name, "marco", "marco");
			marco.src = this.localNode.name;
			c.send_marco(marco);

			System.out.println("Sent Marco to node: " + node.toString());
			return c;
		} catch (UnknownHostException e) {
			System.out.println("Client Socket error:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("Client EOF error:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("Client readline error:" + e.getMessage());
		}

		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				System.out.println("close error:" + e.getMessage());
			}
		return null;
	}

	Connection get_connection(String name) {
		synchronized (connection_list) {
			// lookup open connection in hashmap
			if (connection_list.containsKey(name)) {
				// if there is one, return it
				return connection_list.get(name);
			}
		}
		// else, open a new TCP socket and turn it into a connection
		return open_connection(this.p2pNetwork.getNode(name));
	}

	public synchronized void send(Message message) {
		Connection connection_to_use = get_connection(message.dest);
		if (connection_to_use == null) {
			System.out.println("Failed to find or open connection");
			return;
		}

		// send message via TCP:
		synchronized (connection_to_use) {
			connection_to_use.seqNum++;
			message.set_seqNum(connection_to_use.seqNum);
		}
		message.set_source(this.localNode.name);

		send_tcp(message, connection_to_use);

	}

	private void send_tcp(Message message, Connection connection_to_use) {
		if (connection_to_use.status == ConnectionStatus.ready) {
			connection_to_use.write_object(message);
		} else {
			connection_to_use.enqueue(message);
		}

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
		//	System.out.println("Got " + message.kind + " from " + message.src);
		if (message.kind.equals("polo")) {
			c.set_status(ConnectionStatus.ready);
			return;
		} else if (message.kind.equals("marco")) {
			add_connection(message.src, c);
			return;
		}  

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
					listenSocket.close();
			} catch (IOException e) {
				System.out.println("Failed to close server.");
			}
		}

	}

	private Connection compare_connections(Connection existing, Connection remote) {
		int remotePort = remote.clientSocket.getPort();
		String remoteAddressStr = remote.clientSocket.getInetAddress().toString();
		remoteAddressStr = remoteAddressStr.replace(".", "");
		remoteAddressStr = remoteAddressStr.replace("/", "");
		String remoteValueStr = String.format("%d%s", remotePort, remoteAddressStr);
		long remoteValue = Long.parseLong(remoteValueStr);

		int localPort = existing.clientSocket.getLocalPort();
		String localAddressStr = existing.clientSocket.getLocalAddress().toString();
		localAddressStr = localAddressStr.replace(".", "");
		localAddressStr = localAddressStr.replace("/", "");
		String localValueStr = String.format("%d%s", localPort, localAddressStr);
		long localValue = Long.parseLong(localValueStr);

		return localValue > remoteValue ? existing : remote;
	}

	public void add_connection(String name, Connection connection) {
		Connection existing = connection_list.get(name);
		if (existing != null && existing.status != ConnectionStatus.ready && !name.equals(this.localNode.name)) {
			// compare
			Connection winner = compare_connections(existing, connection);
			if (winner == existing) {
				connection.close();
				return;
			} else {
				if (!existing.message_queue.isEmpty()) {
					connection.message_queue.addAll(existing.message_queue);
				}
				existing.close();
			}
		}

		synchronized (connection_list) {
			connection_list.put(name, connection);
		}

		Message polo = new Message(name, "polo", "polo");
		polo.src = this.localNode.name;
		connection.send_polo(polo);

		connection.set_status(ConnectionStatus.ready);
	}

	public void remove_connection(Connection connection) {
		synchronized (connection_list) {
			connection_list.remove(connection.clientSocket.getRemoteSocketAddress().toString());
		}
	}

}

class Connection extends Thread {
	public enum ConnectionStatus {
		none, open, ready, closed
	}

	DataInputStream in;
	DataOutputStream out;
	ObjectOutputStream outObj;
	ObjectInputStream inObj;

	volatile int seqNum = -1;
	ConnectionStatus status = ConnectionStatus.closed;
	Socket clientSocket;
	MessagePasser msg_passer;
	List<Message> message_queue = new ArrayList<Message>();

	public Connection(Socket aClientSocket, MessagePasser msg_passer) {
		this.msg_passer = msg_passer;
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			outObj = new ObjectOutputStream(clientSocket.getOutputStream());
			inObj = new ObjectInputStream(clientSocket.getInputStream());

			this.start();
			status = ConnectionStatus.open;
		} catch (IOException e) {
			System.out.println("Connection:" + e.getMessage());
		}
	}

	public void run() {
		try {
			while (true) {
				Message message = (Message) inObj.readObject();
				msg_passer.receive_message(message, this);
			}

		} catch (EOFException e) {
			System.out.println("Server EOF:" + e.getMessage());
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to convert received object!");
		} finally {
			try {
				if (clientSocket != null) {
					msg_passer.remove_connection(this);
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

	public void enqueue(Message message) {
		synchronized (message_queue) {
			this.message_queue.add(message);
		}
	}

	public void set_status(ConnectionStatus status) {
		synchronized (this.status){
			this.status = status;
			synchronized (message_queue) {
				if (status == ConnectionStatus.ready && !this.message_queue.isEmpty()) {
					for (Message msg : message_queue) {
						write_object(msg);
					}
					message_queue.clear();
				}
			}
		}
	}

	public void send_marco(Message marco) {
		this.write_object(marco);
	}

	public void send_polo(Message polo) {
		this.write_object(polo);
	}

	public void close() {
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close connection: ");
			this.print();
			e.printStackTrace();
		}
	}

	public static String statusToString(ConnectionStatus status) {
		switch (status) {
		case closed:
			return "closed";
		case open:
			return "open";
		case ready:
			return "ready";
		default:
			return "none";
		}
	}

	public void print() {
		System.out.println("Connection status: " + statusToString(this.status));
		System.out.println("Remote Address: " + this.clientSocket.getRemoteSocketAddress());
		System.out.println("Local Address: " + this.clientSocket.getLocalSocketAddress());
	}
}
