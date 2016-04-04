package utils;

import java.io.Serializable;

public class NodePatrolArea implements Serializable{

	private static final long serialVersionUID = -7166993872659928457L;
	private double[] range = new double[4];
	
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
	
	public NodePatrolArea clone(){
		return new NodePatrolArea(this.range);
	}
	
	public NodePatrolArea splitPatrolArea(NodeLocation testLocation){
		if (!inMyArea(testLocation)){
			// I cannot split my area with this node
			// TODO: throw an error
		}
		
		double[] newRange = new double[4];
		
		/*
		 * determine whether to split latt or long
		 * take whichever, and divide in half
		 * Update my patrol area, and retun the next node's new patrol area
		 */
		
		if ((range[2]-range[0]) >= (range[3]-range[1])){
			// latt is longer, so split along latt
			
			double split = range[2]-range[0]/2;
			
			// same longitude
			newRange[3] = range[3];
			newRange[1] = range[1];
			// split the lats
			newRange[2] = range[2];
			newRange[0] = split;
			
			range[2] = split;
			
			return new NodePatrolArea(newRange);
			
		} else {
			// latt is longer, so split along latt
			
			double split = range[3]-range[1]/2;
			
			// same latitude
			newRange[2] = range[2];
			newRange[0] = range[0];
			// split the latts
			newRange[3] = range[3];
			newRange[1] = split;
			
			range[3] = split;

			return new NodePatrolArea(newRange);
		}
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
	
	public String toString() {
		return String.format("(%f,%f):(%f,%f)", range[0],range[1],range[2],range[3]);
	}

	
	
	

}
