package services;

public class HeartBeat {
	/*
	 * TODO: Implement the HeartBeat Service
	 * This provides some of the D.S. characteristic of fault tolerance to the application
	 * Functionality:
	 * 1. Needs to notify user if neighboring node has died
	 * 
	 * Steps:
	 * 1. Create a client/server for each neighbor
	 * 		a. Server sends neighbors a message every set period
	 * 		b. Neighbors have clients that update the server every so often
	 * 		c. May need future functionality of Nodes leaving the network
	 * 2. Hold warning state, throw to user if current node is in escape path
	 * 3. Be able to recover nodes that come back online
	 */
}
