package adaptive_loop;

import server_and_client.CentralServer;

public class Loop {
	
	/*
	 * Only handles connection delay, as processing delay is heavily based on the response time of the user making it a poor metric for average connection delay
	 */
	
	public CentralServer server;

	public Loop (CentralServer server) {
		this.server = server;
	}
	
	/**
	 * Monitor part of MAPE-K
	 * @return - Returns a two dimensional double array, 0 being free and 1 being paid, containing the connection times of all the users within the current time step
	 */
	public double[][] monitor() { 
		
	}
	
	/**
	 * Analyze part of MAPE-K
	 * @return - Returns the average connection delay for free [0] and paid [1] users
	 */
	public double[] analyze() {
		
	}
	
	/**
	 * Plan part of MAPE-K, takes average connection delays per class from analyze() as well as the desired relative delays (should be instance var of centralserver)
	 * @param delaysByClass - Output from analyze() containing average connection delays by user class
	 * @return An array of booleans representing all of the subservers, if true then means that the subserver should be flipped to serve the other type of user class
	 */
	public boolean[] plan(double[] delaysByClass) {
		
	}
	
	/**
	 * Based on the plan update the classes that each subserver will serve
	 * @param plan - The plan created in plan()
	 */
	public void execute(boolean[] plan) {
		
	}

}
