package process;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import communication.P2PNetwork2;
import utils.Node;
import utils.NodeLocation;
import utils.NodePatrolArea;
import utils.P2PRegion;

public class MockAndroid {
	
	protected static final String receive_block = new String();
	static P2PNetwork2 p2p2;
	static P2PRegion region;
	static NodePatrolArea initial_patrol_area;
	static NodeLocation node_loc;
	static Node myNode;
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		// --------------------------------
		// initialize - get necessary parameter inputs
		int port = 4001;
		

		String IP = getIPAddress();
		System.out.println("IP IS "+IP);
	//	IP  = "192.168.2.118"; //hardcoding it to get messags from android
		
		double[] range = {0,0,0,0};
		double[] location = {2,2}; // node 5
		

		// --------------------------------
		// construct the required objects

		region = new P2PRegion(range);
		initial_patrol_area = new NodePatrolArea(range);
		node_loc = new NodeLocation(location);
		myNode = new Node(initial_patrol_area,region,node_loc,port,IP);

		p2p2 = new P2PNetwork2(myNode);

				
		// --------------------------------
		// Execute something here
		
		run_receiver(p2p2);
	}
	private static void run_receiver(final P2PNetwork2 p2p) {
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
	/*to get device's IP address*/
	public static String getIPAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while(interfaces.hasMoreElements()) {
				NetworkInterface intf = interfaces.nextElement();
				Enumeration<InetAddress> addrs = intf.getInetAddresses();
				while(addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						boolean isIPv4 = sAddr.indexOf(':')<0;
						if (isIPv4) {
							return sAddr;
						}
					}
				}
			}
		} catch (Exception ex) {
		System.out.println(ex.getMessage());}
		return null;
	}

	
	private static void message_prompt(P2PNetwork2 p2p) {
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
