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

import communication.P2PNetwork;
import utils.Node;
import utils.NodeLocation;
import utils.NodePatrolArea;
import utils.P2PRegion;

public class SensorNode {
	
	protected static final String receive_block = new String();
	static P2PNetwork p2p;
	static P2PRegion region;
	static NodePatrolArea initial_patrol_area;
	static NodeLocation node_loc;
	static Node myNode;
	
	public static void main(String[] args) {
		
		// --------------------------------
		// initialize - get necessary parameter inputs
		int port = 4001;
		

		String IP = findMyIPaddr();
		double[] range = {40.442954,-79.94247,40.443112,-79.942191};
		double[] location = {40.443082,-79.942418}; // a second spot on campus
		

		// --------------------------------
		// construct the required objects

		region = new P2PRegion(range);
		initial_patrol_area = new NodePatrolArea(range);
		node_loc = new NodeLocation(location);
		myNode = new Node(initial_patrol_area,region,node_loc,port,IP);

		p2p = new P2PNetwork(myNode);

				
		// --------------------------------
		// Execute something here
		
		run_receiver(p2p);
	}
	private static void run_receiver(final P2PNetwork p2p) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					message_prompt(p2p);
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



	
	private static void message_prompt(P2PNetwork p2p) {
		Object[] options = {"Print Nodes"};

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

		int result = JOptionPane.showOptionDialog(null, myPanel, "Process: " + p2p.localNode.getName(), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		@SuppressWarnings("unused")
		String kind = zField.getText();
		
		if (result == -1) {
			System.exit(0);
		}  else if (options[result].equals("Print Nodes")) {
			p2p.printFoundNodes();
		} 

	}

}
