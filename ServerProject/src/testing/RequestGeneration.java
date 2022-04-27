package testing;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class RequestGeneration {

	/**
	 * Used from the command line to generate user requests for demo purposes.
	 * @param args[0] - Number of ClientThreads to use for generation.
	 * @param args[1] - Host name of server which is being tested.
	 * @param args[2] - Port of server which is being tested.
	 */
	public static void main(String[] args) {
		//Parsing and making arg references clearer by giving local names.
		int numThreads = Integer.valueOf(args[0]);
		String hostName = args[1];
		int port = Integer.valueOf(args[2]);
		
		//Initializing local data structures.
		ArrayList<Long> totalResponseTimes = new ArrayList<>();
		ArrayList<Long> totalWaitTimes = new ArrayList<>();
		
		ArrayList<ClientThread> clientThreads = new ArrayList<>();
		
		System.out.println("Initializing Request Threads: " + numThreads + " Threads at " + hostName + ":" + port);
		
		//Creating numThreads client threads and adding to the clientThread ArrayList.
		for (int i = 0; i < numThreads; i++) {
			System.out.println("Creating client thread " + (i + 1));
			
			ClientThread thread = new ClientThread(numThreads,hostName,port);
			clientThreads.add(thread);
		}
		
		System.out.println("All clients successfully created");
		
		//For n cycles run each of the threads and record the response and wait times.
		int cycles = 5;
		while (cycles >= 1) {
			for (ClientThread t : clientThreads) {
				t.run();
				totalResponseTimes.addAll(t.getResponseTimes());
				totalWaitTimes.addAll(t.getWaitTimes());
			}
			cycles -= 1;
		}
		
		//Recording both response and wait times along with their averages,
		//and printing to a local text file.
		try {
			long avgResponse = 0;
			long avgWait = 0;
			for (long i : totalResponseTimes) {
				avgResponse += i;
			}
			for (long i : totalWaitTimes) {
				avgWait += i;
			}
			
			avgResponse = avgResponse / totalResponseTimes.size();
			avgWait = avgWait / totalWaitTimes.size();
			
			PrintWriter out = new PrintWriter("C:\\Users\\Charlie\\Desktop\\RESULTS.txt");
			
			out.println(totalResponseTimes);
			out.println(totalWaitTimes);
			out.println("AvgResponse: " + avgResponse);
			out.println("AvgWait: " + avgWait);
			out.println("-");
			out.flush();
			out.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
