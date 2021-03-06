package process;

import communication.P2PNetwork;
import utils.Node;
import utils.NodeLocation;
import utils.NodePatrolArea;
import utils.P2PRegion;

public class TestBench {

	
	/*
	 * 
	 * TODO:
	 * Refactor:
	 * -- P2PNetwork.java should hold details about the P2P network  
	 * -- neighboring nodes
	 * 
	 * Bootstrapping:
	 * - Just need to find one, and that will help you get to the next one
	 * - Figure out how to divide up the network
	 * 
	 * Coordinates:
	 * - CMU is not perfectly aligned north/south
	 * - Southwest corner: 40.441713 | Longitude: -79.947789
	 * - Southeast corner: 40.440309 | Longitude: -79.941298 (roughly)
	 * - Northwest corner: 40.444963 | Longitude: -79.946405 (roughly)
	 * - Northeast corner: 40.443844 | Longitude: -79.947789 (roughly)
	 * 
	 * - Position 1: 40.443052 | Longitude: -79.944806
	 * - Position 2: 40.442546 | Longitude: -79.941759
	 * 
	 */
	
	protected static final String receive_block = new String();
	static P2PNetwork p2p;
	static P2PRegion region;
	static NodePatrolArea initial_patrol_area;
	static NodeLocation node_loc;
	static Node myNode;
	
	public static void main(String[] args) {
		// --------------------------------
		// initialize - get necessary parameter inputs
//		int port = Integer.parseInt(args[0]);
		

		int port = 4050; // Ports can be in the range 4000 - 4200
		String IP = "172.29.92.26"; // localhost
		double[] range = {40.441713,-79.947789,40.443844,-79.947789};
//		double[] location = {40.443052,-79.944806};
		double[] location = {40.442546,-79.941759}; // a second spot on campus
		

		// --------------------------------
		// construct the required objects

		region = new P2PRegion(range);
		initial_patrol_area = new NodePatrolArea(range);
		node_loc = new NodeLocation(location);
		myNode = new Node(initial_patrol_area,region,node_loc,port,IP);

		p2p = new P2PNetwork(myNode);

				
		// --------------------------------
		// Execute something here
		
	//	run_receiver(p2p);

	}
	
	
	/*private static void run_receiver(final P2PNetwork p2p) {
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					message_prompt(p2p);
				}
			}
		};

		receiver_thread.start();
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
*/
	
}
