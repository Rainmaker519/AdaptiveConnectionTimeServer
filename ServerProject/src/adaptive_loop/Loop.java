package adaptive_loop;

import java.util.ArrayList;

import server_and_client.CentralServer;
import server_and_client.USER_CLASS;

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
		int plan = plan(analyzed);
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
	 * @return 0 if more free needed, 1 if more paid needed, -1 if either fine
	 */
	public int plan(double[] delaysByClass) {
		//flipping subservers is a bad idea given that they close and are not able
		//to be rebound to a different socket
		//this means the best option is to keep the ratio of free to paid servers
		//decided in plan, and assign empty subserver slots whichever class
		//is further away from its intended amount
		double desiredFreeFactor = this.server.relativeDelayFactors[0];
		double desiredPaidFactor = this.server.relativeDelayFactors[1];
		
		double desiredRatio = desiredFreeFactor / desiredPaidFactor;
		
		int[] actualClasses = this.server.getSubserverClasses();
		double freeNum = 0;
		double paidNum = 0;
		for (int i = 0; i < actualClasses.length; i++) {
			if (actualClasses[i] == 0) {
				freeNum++;
			}
			else if (actualClasses[i] == 1) {
				paidNum++;
			}
		}
		
		if (paidNum == 0) {
			paidNum = 0.0001;
		}
		double actualRatio = freeNum / paidNum;
		
		double error = desiredRatio - actualRatio;
		
		if (error >= .1) {
			return 0;
		}
		else if (error <= -.1) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * Based on the plan update the classes that each subserver will serve
	 * @param plan - The plan created in plan()
	 */
	public void execute(int plan) {
		int numUnassignedSubservers = 0;
		for (int i : this.server.getSubserverClasses()) {
			if (i == -1) {
				numUnassignedSubservers++;
			}
		}
		if (numUnassignedSubservers == 0) {
			//do nothing
		}
		else if (numUnassignedSubservers >= 2) {
			//if more than one free server to assign
			if (plan == 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.FREE);
			}
			else if (plan == 1) {
				this.server.assignConnectionToSubServer(USER_CLASS.PAID);
			}
			//do rest evenly
			if (numUnassignedSubservers != 1) {
				for (int i = 0; i < numUnassignedSubservers-1; i++) {
					if (i % 2 == 1) {
						this.server.assignConnectionToSubServer(USER_CLASS.FREE);
					}
					else {
						this.server.assignConnectionToSubServer(USER_CLASS.PAID);
					}
				}
			}
		}
		else {
			//if one free server to assign
			if (plan == 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.FREE);
			}
			else if (plan == 1) {
				this.server.assignConnectionToSubServer(USER_CLASS.PAID);
			}
		}
	}

}
