package services;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.swing.Timer;

import communication.P2PNetwork;
import utils.Node;

public class HeartBeatListen extends Thread{
	
	private P2PNetwork p2p;
	
	public HeartBeatListen(P2PNetwork p){
		this.p2p = p;
	}
	
	public void run(){
		// get list of neighbors
		List<Node> neighbors = p2p.getListOfNeighbors();
		
		do{
			// create code to watch for changes in List of Neighbors
			try {
				MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
				md5.update(neighbors.toString().getBytes());
				// System.out.println("MD5 of neighbors: " + md5.digest());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// create timers
			for(Node neighbor : neighbors){
				
				// timer action listener
				ActionListener deadNodeAlert = new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						System.out.println("Node Died!" + e.getSource().toString());
					}
				};
				
				neighbor.timer = new Timer(15000, deadNodeAlert);
				neighbor.timer.start();
			}
			
		}while(true);
	}
}
