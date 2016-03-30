package process;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import communication.MessagePasser;
import services.ClockService;
import utils.Node;

public class TestBench {

	
	/*
	 * TODO:
	 * Refactor:
	 * - Remove config file stuff
	 * - Remove need for only 1 socket
	 * - Remove send/receive rules
	 * - Add a bootstrapping skeleton to start looking for nodes in a network
	 * - Configureation.java should hold details about the P2P network  
	 * -- neighboring nodes
	 * -- my location
	 * -- my data
	 * -- 
	 * 
	 * Bootstrapping:
	 * - Broadcast to all IPs similar to mine
	 * - All 'servers' should have a fixed port
	 * - serially ping server/ports
	 * - maybe a small timeout?
	 * - Just need to find one, and that will help you get to the next one
	 * - For demo, serially check every port in localhost
	 * 
	 * 
	 */
	
	
	//private static String name;

	private static String name;
	private static int port;
	private static MessagePasser messagePasser;
	private static ClockService clock;
	
	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameter inputs

		name = "sammy";
		port = 4001;
		// config_file_address =
		// "http://www.andrew.cmu.edu/user/skupfer/config.txt";

		// --------------------------------
		// construct the required objects

		clock = ClockService.clockServiceFactory("logical");
		Node myself = new Node(name,80.9000,-90.000,port, findMyIPaddr());

		messagePasser = new MessagePasser(myself, clock);
		
		System.out.println(messagePasser.localNode.toString());
		
		
		// --------------------------------
		// Execute something here
		
		
		//prompt();
		System.exit(0);
		
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


	@SuppressWarnings("unused")
	private static void prompt() {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					Object[] options = { "Quit" };
					int n = JOptionPane.showOptionDialog(null, null, "TestBench", JOptionPane.CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == -1 || n == 0) {
						System.exit(0);

					}
				}
			}
		};

		receiver_thread.start();
	}
}
