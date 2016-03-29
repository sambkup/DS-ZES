package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Node {
	
	public String name = null;
	public String ip = null;
	public int port = 0;
	public String address = null;
	public double lattitude = 0.0;
	public double longitude = 0.0;

	public Node(String name, double lattitude, double longitude, int port) {

		this.lattitude = lattitude;
		this.longitude = longitude;

		this.name = name;
		this.ip = findIPaddr();
		this.port = port;
		this.address = String.format("/%s:%d", this.ip, this.port);
	}
	
	private static String findIPaddr(){
		// TODO: make this failure tolerant
		InetAddress x = null;
		try {
			x = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return x.getHostAddress();
	}
	
	public String toString() {
		return String.format("%s(%s:%d)", name, ip, port);
	}
}
