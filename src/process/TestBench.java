package process;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

import communication.MessagePasser;
import services.ClockService;
import utils.P2P;

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
	 * - 
	 * 
	 * 
	 */
	
	
	//private static String name;

	private static P2P p2p_network;
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
		p2p_network = new P2P(name,80.9000,-90.000,port);

		messagePasser = new MessagePasser(p2p_network,clock);
		
		System.out.println(p2p_network.getMyNode().toString());
		
		System.out.println(findFirstNode());
		
		// --------------------------------
		// Execute something here
		
		
		
		

		
		
		//prompt();
		System.exit(0);
		
	}
	
	public static boolean findFirstNode(){
		String myIP = "10.0.0.183";
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
				//InetAddress inetAddress = InetAddress.getByName(testIP);
				InetSocketAddress endpoint = new InetSocketAddress(testIP, port);

				s = new Socket();
				s.connect(endpoint, 100);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(testIP + " - Closed");
				continue;

				//e.printStackTrace();
			}finally{
				try {
					s.close();
				} catch (IOException e) {
				}
			}
			System.out.println(testIP + " - Open");
			return true;
		}
		
		return false;
	}

	

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
