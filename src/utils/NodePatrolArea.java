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
		double a1 = testCoordinates[0];
		double b1 = testCoordinates[1];
		if ( (x1 < a1) && (a1 < x2) && 
			 (y2 < b1) && (b1 < y1)){
			return true;
		}
		
		
		return false;
	}
	
	public boolean isNeighbor(NodePatrolArea testPatrolArea){
		// check if there are matching coordinates
		
		double[] testRange = testPatrolArea.getRange();

		double a1 = this.range[0];
		double b1 = this.range[1];
		double a2 = this.range[2];
		double b2 = this.range[3];

		double a3 = testRange[0];
		double b3 = testRange[1];
		double a4 = testRange[2];
		double b4 = testRange[3];

//		System.out.printf("(a1,b1);(a2,b2);(a3,b3);(a4,b4)\n");
//		System.out.printf("(%f,%f);(%f,%f);(%f,%f);(%f,%f)\n", a1,b1,a2,b2,a3,b3,a4,b4);
		if (a2==a3 || a1==a4){
			System.out.println("1");
			if ( (b4-b3) <= (b2-b1)){
				System.out.println("2");
				if ( ( b3>=b2) && (b3<=b1) ){
					System.out.println("3");
					return true;
				}
			} else {
				System.out.println("4");
				if ( ( b1<=b3) && (b1>=b4) ){
					System.out.println("5");
					return true;
				}
			}
			
		} else if (b1==b4 || b2==b3){
			System.out.println("6");
			if ( (a2-a1) >= (a4-a3) ){
				System.out.println("7");
				if ( (a3 <= a2) && (a3 >= a1) ){
					System.out.println("8");
					return true;
				}
			} else{
				System.out.println("9");
				if ( (a2 >= a3) && ( a2 <= a4) ){
					System.out.println("10");
					return true;
				}
				
			}

		}
		System.out.println("11");
		return false;
	}
	
	public NodePatrolArea clone(){
		return new NodePatrolArea(this.range);
	}
//	
//	public NodePatrolArea splitPatrolArea(NodeLocation testLocation){
//		if (!inMyArea(testLocation)){
//			// I cannot split my area with this node
//			// TODO: throw an error
//		}
//		
//		double[] newRange = new double[4];		
//		
//		/*
//		 * determine whether to split latt or long
//		 * take whichever, and divide in half
//		 * Update my patrol area, and retun the next node's new patrol area
//		 * 
//		 */
//		
//		
//		if ((range[2]-range[0]) >= (range[3]-range[1])){
//			// latt is longer, so split along latt
//			
//			double split = (range[2]-range[0])/2 + range[0];
//			
//			// same longitude
//			newRange[3] = range[3];
//			newRange[1] = range[1];
//			
//			// split the lats
//			newRange[2] = range[2];
//			newRange[0] = split;
//			
//			NodePatrolArea newPatrol = new NodePatrolArea(newRange);
//			if (!newPatrol.inMyArea(testLocation)){
//				range[2] = split;
//				return newPatrol;
//			} else{
//				double split1 = (range[3]-range[1])/2 + range[1];
//				
//				// same latitude
//				newRange[2] = range[2];
//				newRange[0] = range[0];
//				
//				// split the latts
//				newRange[3] = range[3];
//				newRange[1] = split1;
//				
//				range[3] = split1;
//
//				return new NodePatrolArea(newRange);
//				
//			}
//			
//			
//		} else {
//			// latt is longer, so split along latt
//			
//			double split = (range[3]-range[1])/2 + range[1];
//			
//			// same latitude
//			newRange[2] = range[2];
//			newRange[0] = range[0];
//			
//			// split the latts
//			newRange[3] = range[3];
//			newRange[1] = split;
//			
//			NodePatrolArea newPatrol = new NodePatrolArea(newRange);
//			if (!newPatrol.inMyArea(testLocation)){
//				range[3] = split;
//				return newPatrol;
//			} else{
//				double split1 = (range[2]-range[0])/2 + range[0];
//				
//				// same longitude
//				newRange[3] = range[3];
//				newRange[1] = range[1];
//				
//				// split the lats
//				newRange[2] = range[2];
//				newRange[0] = split1;
//				range[2] = split1;
//
//				return new NodePatrolArea(newRange);
//
//				
//			}
//
//			
//		}
//	}


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
		if(range[2]>=range[0]){
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
