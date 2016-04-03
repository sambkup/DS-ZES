package utils;

import java.io.Serializable;

public class NodePatrolArea implements Serializable{

	private static final long serialVersionUID = -7166993872659928457L;
	private double[] range = new double[4];
	
	public NodePatrolArea(double min_lat, double max_lat, double min_long, double max_long){
		range = new double[4];
		range[0]=min_lat;
		range[1]=max_lat;
		range[2]=min_long;
		range[3]=max_long;
	}
	public NodePatrolArea(double[] range ){
		if (range.length != 4){
			//TODO: throw exception
		}
		
		this.range[0]=range[0];
		this.range[1]=range[1];
		this.range[2]=range[2];
		this.range[3]=range[3];
	}
	
	/**
	 * @param testLocation
	 * @return
	 * Returns true if given location is within my region
	 * Returns false if given location is not within my region
	 */
	public boolean inMyArea(NodeLocation testLocation){
		double[] testCoordinates = testLocation.getLocation();
		if (testCoordinates[0] > range[2] || testCoordinates[0] < range[0]){
			if(testCoordinates[1] > range[3] || testCoordinates[1] < range[1] ){
				return false;
			}
		}
		return true;
	}


	/**
	 * @return the range
	 */
	public double[] getRange() {
		return range;
	}

	/**
	 * @param range the range to set
	 */
	public void setRange(double[] range) {
		this.range = range;
	}
	
	
	

}
