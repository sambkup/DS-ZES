package utils;

import java.io.Serializable;

public class P2PRegion implements Serializable{
	
	private static final long serialVersionUID = -5261576759484517052L;
	private double[] range = new double[4];
	// range = [lat_min,long_min, lat_max,long_max]
	
	public P2PRegion(double[] range){
		if (range.length != 4){
			//TODO: throw error
			System.out.println("length is: "+range.length);
		}
		// reorder values if necessary
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
		}	}
	
	public P2PRegion clone(){
		return new P2PRegion(this.range);
	}

	
	/**
	 * @param testRegion
	 * @return
	 * Returns true if same region
	 * Returns false if not same region
	 */
	public boolean equals(P2PRegion testRegion){
		double[] testRange = testRegion.getRange();
		for (int i = 0; i< range.length; i++){
			if(testRange[i] != range[i]){
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
	
	// No setter for range, since this should be unchanging.
	
	

}
