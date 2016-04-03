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
import utils.Node;
import utils.NodeLocation;
import utils.NodePatrolArea;
import utils.P2PRegion;

public class TestBench {

	
	/*
	 * TODO:
	 * Refactor:
	 * - Add a bootstrapping skeleton to start looking for nodes in a network
	 * - P2PNetwork.java should hold details about the P2P network  
	 * -- neighboring nodes
	 * 
	 * Bootstrapping:
	 * - Just need to find one, and that will help you get to the next one
	 * - Figure out how to divide up the network
	 * 
	 */
	
	
	//private static String name;

	protected static final String receive_block = new String();
	private static int port;
	private static P2PNetwork p2p;
	static Node myself;
	
	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameter inputs

		// Ports can be in the range 4000 - 4200 (can be increased if needed)
		port = 4011;
		
		/* CMU is not perfectly alightned north/south
		 * Southwest corner: 40.441713 | Longitude: -79.947789
		 * Southeast corner: 40.440309 | Longitude: -79.941298 (roughly)
		 * Northwest corner: 40.444963 | Longitude: -79.946405 (roughly)
		 * Northeast corner: 40.443844 | Longitude: -79.947789 (roughly)
		 * 
		 * Position 1: 40.443052 | Longitude: -79.944806
		 * Position 2: 40.442546 | Longitude: -79.941759
		 */
		
		
		
		double[] range = {40.441713,-79.947789,40.443844,-79.947789};
		double[] location = {40.443052,-79.944806};
//		double[] location = {40.442546,-79.941759};
		P2PRegion region = new P2PRegion(range);
		NodePatrolArea initial_patrol_area = new NodePatrolArea(range);
		NodeLocation node_loc = new NodeLocation(location);
//		String IP = findMyIPaddr();
//		String IP = "10.0.0.183";
		String IP = "127.0.0.1";
		myself = new Node(initial_patrol_area,region,node_loc,port,IP);

		// --------------------------------
		// construct the required objects

		p2p = new P2PNetwork(myself);
				
		// --------------------------------
		// Execute something here
			
		while (true){
			message_prompt();
		}		
	}
	

	@SuppressWarnings("unused")
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
	
	private static void message_prompt() {
		Object[] options = { "Send","Print Nodes"};

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

		}  else if (options[result].equals("Print Nodes")) {
			p2p.printFoundNodes();
		} 

	}

	
}
