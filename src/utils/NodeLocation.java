package utils;

import java.io.Serializable;

public class NodeLocation implements Serializable{
	
	private static final long serialVersionUID = -5422474327179902439L;
	private double[] location = new double[2];
	
	public NodeLocation(double[] location ){
		if (location.length != 2){
			//TODO: throw error
		}
		
		this.location[0]=location[0];
		this.location[1]=location[1];
	}

	public NodeLocation clone(){
		return new NodeLocation(this.location);
	}

	
	/**
	 * @return the location
	 */
	public double[] getLocation() {
		return location;
	}
	
	public String toString() {
		return String.format("(%f,%f)", location[0],location[1]);
	}
	
	/**
	 * @param loc1 
	 * @param loc2
	 * @return distance between the two coordinates
	 */
	public double findDistance(double[] loc){
		double deltaLat = loc[0]-this.location[0];
		double deltaLon = loc[1]-this.location[1];
		double dist = Math.pow(deltaLat,2)+Math.pow(deltaLon,2);
		dist = Math.pow(dist, 0.5);	
		return dist;
	}



}
