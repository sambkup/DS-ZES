package process;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import communication.Message;
import communication.P2PNetwork;
import services.ClockService;
import utils.Node;
import utils.NodePatrolArea;
import utils.P2PRegion;

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
	 * - change code to use ports so I can start testing again
	 * 
	 */
	
	
	//private static String name;

	protected static final String receive_block = new String();
	private static int port;
	private static P2PNetwork p2p;
	private static ClockService clock;
	static Node myself;
	
	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameter inputs

		port = 4001;
		double[] range = {80.9000,-90.000,80.9000,-90.000};
		P2PRegion region = new P2PRegion(range);
		NodePatrolArea initial_patrol_area = new NodePatrolArea(range);
		myself = new Node(initial_patrol_area,region,port,findMyIPaddr());

		// --------------------------------
		// construct the required objects

		clock = ClockService.clockServiceFactory("logical");

		p2p = new P2PNetwork(myself, clock);
		
		System.out.println(p2p.localNode.toString());
		
		
		Message message = new Message(p2p.localNode.ip,p2p.localNode.port ,"generic",myself);
		p2p.send(message);
		
		// --------------------------------
		// Execute something here
		
		message_prompt();
		run_receiver(p2p);
		//prompt();
		//System.exit(0);
		
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
	

	private static void run_receiver(P2PNetwork msg_passer) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					synchronized (receive_block) {
						try {
							receive_block.wait();
						} catch (InterruptedException e) {
							System.out.println("failed to wait");
							e.printStackTrace();
						}
						Message rcved = (Message) msg_passer.receive();
						if (rcved != null) {
							rcved.print();
						}
					}
				}
			}
		};

		receiver_thread.start();
	}

	private static void message_prompt() {
		Object[] options = { "Send","Receive"};

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		JTextField xField = new JTextField(15);
		JTextField zField = new JTextField(15);
		JTextField groupField = new JTextField(15);

		JPanel myPanel = new JPanel();

		myPanel.setLayout(new GridBagLayout());
		
		String[] rowMessages = {"Fill in the Message:", "Message destination:", "Message Kind:"};
		JComponent[] components = {xField, zField, groupField};
		int rows = rowMessages.length;
		for (int row=0; row<rows; row++) {
			constraints.gridx = 0;
			constraints.gridy = row;
			myPanel.add(new JLabel(rowMessages[row]), constraints);
			
			if (row == 0) continue;
			
			constraints.gridx = 1;
			constraints.gridy = row;
			myPanel.add(components[row-1], constraints);
		}

		myPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Message"));

		int result = JOptionPane.showOptionDialog(null, myPanel, "Process: " + p2p.localNode.toString(), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		String dest = xField.getText();
		String kind = zField.getText();
		
		if (result == -1) {
			System.exit(0);
		} else if (options[result].equals("Send")) {
			Message message = new Message(dest, 4001, kind, myself);
			p2p.send(message);

		}  else if (options[result].equals("Receive")) {
			synchronized (receive_block) {
				receive_block.notify();
			}
		} 

	}

	
}
