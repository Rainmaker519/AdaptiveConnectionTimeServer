package adaptive_loop;

import java.util.ArrayList;

import server_and_client.CentralServer;

public class Loop {
	
	/*
	 * Only uses connection delay as a metric for user delay, as processing delay is heavily based 
	 * on the response time of the user making it a poor metric for average connection delay.
	 */
	
	public CentralServer server;
	
	private ArrayList<Double> freeConnections;
	private ArrayList<Double> paidConnections;

	public Loop (CentralServer server) {
		this.server = server;
		this.freeConnections = new ArrayList<Double>();
		this.paidConnections = new ArrayList<Double>();
	}
	
	/**
	 * Runs the entirety of the MAPE-K Loop, call this every S (where S is the length of time intervals used)
	 * @return - returns true if the loop runs without passing an error
	 */
	public boolean run() {
		ArrayList<Double>[] monitored = monitor();
		double[] analyzed = analyze(monitored);
		boolean[] plan = plan(analyzed);
		execute(plan);
		return true;
	}
	
	/**
	 * Monitor part of MAPE-K
	 * @return - Returns a two dimensional double array, 0 being free and 1 being paid, containing the connection times of all the users within the current time step
	 */
	public ArrayList<Double>[] monitor() { 
		@SuppressWarnings("unchecked")
		ArrayList<Double>[] information = (ArrayList<Double>[])new ArrayList[2];
		
		information[0] = this.freeConnections;
		information[1] = this.paidConnections;
		
		this.freeConnections.clear();
		this.paidConnections.clear();
		
		return information;
	}
	
	/**
	 * Analyze part of MAPE-K
	 * @return - Returns the average connection delay for free [0] and paid [1] users
	 */
	public double[] analyze(ArrayList<Double>[] delays) {
		double[] averageConnectionDelays = new double[2];
		double total = 0;
		for (int i = 0; i < delays.length; i++) {
			for (double delay : delays[i]) {
				total += delay;
			}
			total = total / delays[i].size();
			averageConnectionDelays[i] = total;
			total = 0;
		}
		return averageConnectionDelays;
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
