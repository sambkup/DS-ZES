package utils;

public class P2PRegion {
	
	private double[] range = new double[4];
	
	public P2PRegion(double min_lat, double max_lat, double min_long, double max_long){
		range = new double[4];
		range[0]=min_lat;
		range[1]=max_lat;
		range[2]=min_long;
		range[3]=max_long;
		
		// TODO: do some bounds checking so the min<max.  If not throw error.
	}
	
	public P2PRegion(double[] range){
		if (range.length != 4){
			//TODO: throw error
			System.out.println("length is: "+range.length);
		}
		this.range[0]=range[0];
		this.range[1]=range[1];
		this.range[2]=range[2];
		this.range[3]=range[3];
	}

	

	/**
	 * @return the range
	 */
	public double[] getRange() {
		return range;
	}
	
	// No setter for range, since this should be unchanging.
	
	

}
