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

	/**
	 * @return the location
	 */
	public double[] getLocation() {
		return location;
	}


}
