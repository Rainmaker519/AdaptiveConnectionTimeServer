package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server_and_client.CentralServer;
import server_and_client.USER_CLASS;

class Test_centralServer {
	
	@Test
	void testRun() {
		try {
			Date date = new Date();
		    //This method returns the time in millis
		    long start = date.getTime();
			CentralServer c = new CentralServer(6666,1,1.01);
			//should I add something to show the average rato
			int lSize = 20;
			int l = 0; 
			
			ArrayList<Socket> clients = new ArrayList<>();
			
			while (l < 20) {
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
			
			System.out.println("FreeQueueLength: " + c.getFreeQueueLength() + "||  PaidQueueLength: " + c.getPaidQueueLength());
			
			if (c.getFreeQueueLength() + c.getPaidQueueLength() != 0) {
				fail();
			}
			
			c.interrupt();
			
			date = new Date();
			long end = date.getTime();
			
			System.out.println("Total Time to Handle 20 Requests: " + (end - start) + " Milliseconds");
		}
		catch(Exception e) {
			fail();
		}
	}
	
	private Socket clientConnect(USER_CLASS c) {
		try {
			Socket client = new Socket("localhost",6666);
			
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			
			String syn = Integer.toString((int) (Math.random() * 1000));
			
			dout.writeUTF(syn);  
			dout.flush(); 
			//System.out.println("CLIENT - Sent SYN");
			
			DataInputStream din = new DataInputStream(client.getInputStream());
			String synPlusAck = din.readUTF();
			
			if (c.equals(USER_CLASS.FREE)) {
				if (synPlusAck.equals(syn + "1")) {
					//System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(syn + " " + "FREE");
				}
				else {
					//System.out.println("CLIENT - ACK not accepted");
					
					dout.close();  
					din.close();
					
					//client.close();
					
					fail("CLIENT - ACK not accepted");
				}
			}
			else if (c.equals(USER_CLASS.PAID)) {
				if (synPlusAck.equals(syn + "1")) {
					//System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(syn + " " + "PAID");
				}
				else {
					//System.out.println("CLIENT - ACK not accepted");
					
					dout.close();  
					din.close();
				}
			}
			else {
				System.out.println("client userclass unknown");
				dout.close();  
				din.close();
			}
			
			//the reason this shit is blocking is because I'm needing a response from
			//the server to move forward in the client connect method wtf
			
			//the connections dont even make it to the subserver until after a certain
			//amount of time passes and all the connections in that time are queued up
			
			//since it blocks in the connect method tho, the server never reaches the point
			//where connections are allocated to subservers at all
			
			return client;
			
			
			
		} catch (Exception e) {
			fail("CLIENT - Failed to open client socket");
			return null;
		}
	}
	
	private boolean clientRequest(Socket client) {
		//System.out.println("At rand g selection");
		
		try {
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			DataInputStream din = new DataInputStream(client.getInputStream());
			
			double r = Math.random() * 100;
			if (r >50) {
				dout.writeUTF("GM");
				dout.flush();
			}
			else {
				dout.writeUTF("GN");
				dout.flush();
			}
			
			System.out.println("read-utf: " + din.readUTF());
			din.close();
			dout.close();
			
			client.close();
			
			return true;
		}catch (Exception e) {
			System.out.println("request failed oops");
			return false;
		}
		
	}

}
