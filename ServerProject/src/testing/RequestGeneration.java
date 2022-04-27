package testing;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import server_and_client.USER_CLASS;

public class RequestGeneration {

	public static void main(String[] args) {
		int numThreads = Integer.valueOf(args[0]);
		String hostName = args[1];
		int port = Integer.valueOf(args[2]);
		
		ArrayList<Long> totalResponseTimes = new ArrayList<>();
		ArrayList<Long> totalWaitTimes = new ArrayList<>();
		
		ArrayList<ClientThread> clientThreads = new ArrayList<>();
		
		System.out.println("Initializing Request Threads: " + numThreads + " Threads at " + hostName + ":" + port);
		
		for (int i = 0; i < numThreads; i++) {
			System.out.println("Creating client thread " + (i + 1));
			
			ClientThread thread = new ClientThread(numThreads,hostName,port);
			clientThreads.add(thread);
		}
		
		System.out.println("All clients successfully created");
		
		int cycles = 5;
		while (cycles >= 1) {
			for (ClientThread t : clientThreads) {
				t.run();
				totalResponseTimes.addAll(t.getResponseTimes());
				totalWaitTimes.addAll(t.getWaitTimes());
			}
			cycles -= 1;
		}
		
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
	
	
	
	
	
	/**
	 * while (l < 20) {
				System.out.println("RS");
				double ran = Math.random() * 100;
				if (ran >= 50) {
					clients.add(clientConnect(USER_CLASS.FREE));
				}
				else {
					clients.add(clientConnect(USER_CLASS.PAID));
				}
				l++;
				long interArrival = (long) (Math.random() * 500);
				TimeUnit.MILLISECONDS.sleep(interArrival);
			}
			
			while (!clients.isEmpty()) {
				clientRequest(clients.get(0));
				clients.remove(0);
			}
	 */

}
