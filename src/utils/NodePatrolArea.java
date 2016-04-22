package utils;

import java.io.Serializable;

public class NodePatrolArea implements Serializable{

	
	private static final long serialVersionUID = -7166993872659928457L;
	private double[] range = new double[4];
	// {min_lattitude, min_longitute, max_latitude, max_longitude}
	
	public NodePatrolArea(double[] range ){
		if (range.length != 4){
			//TODO: throw exception
		}
		this.setRange(range);
	}
	
	/**
	 * @param testLocation
	 * @return
	 * Returns true if given location is within my region
	 * Returns false if given location is not within my region
	 */
	public boolean inMyArea(NodeLocation testLocation){
		double[] testCoordinates = testLocation.getLocation();
		
		// if out of range in anything
//		if (testCoordinates[0] > range[2] || testCoordinates[0] < range[0] ||
//			testCoordinates[1] > range[3] || testCoordinates[1] < range[1] ){
//				return false;
//		}
//		return true;

		double x1 = range[0];
		double y1 = range[1];
		double x2 = range[2];
		double y2 = range[3];
		double a = testCoordinates[0];
		double b = testCoordinates[1];
		if ( (x2 < a) && (a < x1) && 
			 (y1 < b) && (b < y2)){
			return true;
		}
		return false;
	}
	
	public boolean isNeighbor(NodePatrolArea testPatrolArea){
		// check if there are matching coordinates
		
		double[] testRange = testPatrolArea.getRange();

		double x1 = this.range[0];
		double y1 = this.range[1];
		double x2 = this.range[2];
		double y2 = this.range[3];

		double x3 = testRange[0];
		double y3 = testRange[1];
		double x4 = testRange[2];
		double y4 = testRange[3];

		if (x2==x3 || x1==x4){
			
			// if 2nd edge is smaller
			if ( Math.abs(y4-y3) <= Math.abs(y2-y1)){
				if ( ( y2>=y3) && (y3>=y1) && ( y2>=y4) && (y4>=y1)){
					return true;
				}
			// if 1st edge is smaller
			} else if ( Math.abs(y4-y3) >= Math.abs(y2-y1))	{
				if ( ( y4>=y1) && (y1>=y3) && ( y4>=y2) && (y2>=y3) ){
					return true;
				}
			}
			
		} else if (y1==y4 || y2==y3){
			
			// if 2nd edge is smaller
			if ( Math.abs(x4-x3) <= Math.abs(x2-x1)){
				if ( (x1 >= x3) && (x3 >= x2) && (x1 >= x4) && (x4 >= x2) ){
					return true;
				}
			} else if  ( Math.abs(x4-x3) >= Math.abs(x2-x1)){

				if ( (x3 >= x2) && ( x2 >= x4) && (x3 >= x1) && ( x1 >= x4) ){
					return true;
				}
				
			}

		}
		return false;
	}
	
	public NodePatrolArea clone(){
		return new NodePatrolArea(this.range);
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
		if(range[0]>=range[2]){
			this.range[0]=range[0];
			this.range[2]=range[2];
		}else{
			this.range[2]=range[0];
			this.range[0]=range[2];
		}
		
		if(range[3]>=range[1]){
			this.range[1]=range[1];
			this.range[3]=range[3];
		}else{
			this.range[3]=range[1];
			this.range[1]=range[3];
		}
	}
	
	public String toString() {
		return String.format("(%f,%f):(%f,%f)", range[0],range[1],range[2],range[3]);
	}


}
