package adaptive_loop;

import java.util.ArrayList;

import server_and_client.CentralServer;
import server_and_client.USER_CLASS;

public class Loop {
	
	/*
	 * Only uses connection delay as a metric for user delay, as processing delay is heavily based 
	 * on the response time of the user making it a poor metric for average connection delay.
	 */
	
	//is the loop allocating all the possible free subservers each loop?
	
	//should the loop be running on its own rather than as part of the centralserver loop?
	
	//try lowering the time waiting for new clients to make more loop runs
	
	
	
	public CentralServer server;
	
	private ArrayList<Double> freeConnections;
	private ArrayList<Double> paidConnections;
	
	
	private double desiredRatio_;
	private double actualRatio_;
	private int[] actualCounts_;
	private int plan_;

	public Loop (CentralServer server) {
		this.server = server;
		this.freeConnections = new ArrayList<Double>();
		this.paidConnections = new ArrayList<Double>();
		this.desiredRatio_ = -1;
		this.actualRatio_ = -1;
		this.actualCounts_ = new int[] {-1,-1};
		this.plan_ = -5;
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
	
	public double[] getLoopInstanceRatios() {
		if (this.desiredRatio_ == -1 || this.actualRatio_ == -1) {
			return new double[] {-1,-1};
		}
		else {
			return new double[] {this.desiredRatio_,this.actualRatio_};
		}
		
	}
	
	public int[] getLoopInstanceCountsPlusPlan() {
		if (this.actualCounts_[0] == -1 || this.actualCounts_[1] == -1 || this.plan_ == -5) {
			return new int[] {this.actualCounts_[0], this.actualCounts_[1], this.plan_};
		}
		else {
			return new int[] {this.actualCounts_[0], this.actualCounts_[1], this.plan_};
		}
		
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
	 * Plan part of MAPE-K, takes average connection delays per class from analyze() 
	 * as well as the desired relative delays (should be instance var of centralserver)
	 * @param delaysByClass - Output from analyze() containing average connection delays by user class
	 * @return 0 if more free needed, 1 if more paid needed, -1 if either fine
	 */
	public int plan(double[] delaysByClass) {
		double desiredFreeFactor = this.server.relativeDelayFactors[0];
		double desiredPaidFactor = this.server.relativeDelayFactors[1];
		
		double desiredRatio = desiredFreeFactor / desiredPaidFactor;
		this.desiredRatio_ = desiredRatio;
		
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
		this.actualCounts_[0] = (int)freeNum;
		this.actualCounts_[1] = (int)paidNum;
		
		if (paidNum == 0) {
			paidNum = 0.0001;
		}
		double actualRatio = freeNum / paidNum;
		this.actualRatio_ = actualRatio;
		
		double error = desiredRatio - actualRatio;
		
		if (error >= .5) {
			return 0;
		}
		else if (error <= -.5) {
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
		this.plan_ = plan;
		
		int numUnassignedSubservers = 0;
		for (int i : this.server.getSubserverClasses()) {
			if (i == -1) {
				numUnassignedSubservers++;
			}
		}
		//System.out.println("Execute: " + numUnassignedSubservers);
		if (numUnassignedSubservers == 0) {
			//do nothing
		}
		else if (numUnassignedSubservers >= 2) {
			//if more than one free server to assign
			if (plan == 0 && this.server.getFreeQueueLength() > 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.FREE);
			}
			else if (plan == 1 && this.server.getPaidQueueLength() > 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.PAID);
			}
			//do rest evenly
			if (numUnassignedSubservers != 1) {
				for (int i = 0; i < numUnassignedSubservers-1; i++) {
					if (i % 2 == 1 && this.server.getFreeQueueLength() > 0) {
						this.server.assignConnectionToSubServer(USER_CLASS.FREE);
					}
					else {
						if (this.server.getPaidQueueLength() > 0) {
							this.server.assignConnectionToSubServer(USER_CLASS.PAID);
						}
					}
				}
			}
			else {
				if (plan == 0 && this.server.getFreeQueueLength() > 0) {
					this.server.assignConnectionToSubServer(USER_CLASS.FREE);
				}
				else if (plan == 1 && this.server.getPaidQueueLength() > 0) {
					this.server.assignConnectionToSubServer(USER_CLASS.PAID);
				}
			}
		}
		else {
			//if one free server to assign
			if (plan == 0 && this.server.getFreeQueueLength() > 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.FREE);
			}
			else if (plan == 1 && this.server.getPaidQueueLength() > 0) {
				this.server.assignConnectionToSubServer(USER_CLASS.PAID);
			}
		}
	}

}
