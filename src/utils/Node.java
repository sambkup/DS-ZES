package utils;

public class Node {
	
	public String name = null;
	public String ip = null;
	public int port = 0;
	public String address = null;
	public double lattitude = 0.0;
	public double longitude = 0.0;

	public Node(String name, double lattitude, double longitude, int port, String ip) {

		this.lattitude = lattitude;
		this.longitude = longitude;

		this.name = name;
		this.ip = ip;
		this.port = port;
		this.address = String.format("/%s:%d", this.ip, this.port);
	}
	
	
	public String toString() {
		return String.format("%s(%s:%d)", name, ip, port);
	}
}
