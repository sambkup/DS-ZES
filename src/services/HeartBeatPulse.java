package services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import communication.Message;
import communication.Message.messageKind;
import communication.P2PNetwork;
import utils.Node;

public class HeartBeatPulse extends Thread{
	/*
	 * TODO: Implement the HeartBeat Service
	 * This provides some of the D.S. characteristic of fault tolerance to the application
	 * Functionality:
	 * 1. Needs to notify user if neighboring node has died
	 */
	private P2PNetwork p2p;
	
	public HeartBeatPulse(P2PNetwork p){
		this.p2p = p;
	}
	
	public void run(){
		// get list of neighbors
		List<Node> neighbors = p2p.getListOfNeighbors();

		// sleep until neighbors are available
		while(neighbors == null){
			try {
				System.out.println("No neighbors, no heart beat");
				sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		while(neighbors != null){
			// for each neighbor
			for(Node neighbor : neighbors){
				// String destIP, int port, messageKind kind, Node node
				if(neighbor != null){
					// craft and send heart beat
					Message HBMessage = new Message(neighbor.getIP(), neighbor.getPort(), messageKind.HEART_BEAT, p2p.getLocalNode());
					p2p.send(HBMessage);
				}
			}
			
			// sleep 10s until next pulse
			try {
				sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// loop for next 'pulse'
		}
	}
}
