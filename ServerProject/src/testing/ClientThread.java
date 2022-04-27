package testing;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import server_and_client.USER_CLASS;

public class ClientThread extends Thread {
	
	/**
	 * REMEMBER THAT YOU NEED TO RECORD THESE RESULTS SOMEWHERE
	 * def will make presenting my demo easier if after there is a txt
	 * file or graph that shows average response time, wait time, etc
	 */
	
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	
	
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	//How can I best record the average response time and wait time?
	
	
	private int numClients_;
	private String hostName_;
	private int port_;
	
	private ArrayList<Integer> responseTimesMilliseconds;
	private ArrayList<Integer> waitTimesMilliseconds;
	
	
	protected ClientThread(int numClients, String hostName, int port) {
		numClients_ = numClients;
		hostName_ = hostName;
		port_ = port;
		
		responseTimesMilliseconds = new ArrayList<>();
		waitTimesMilliseconds = new ArrayList<>();
	}
	
	public void run() {
		//make and connect client
		ArrayList<Socket> clients = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			clients.add(clientConnect());
		}
		
		while (!clients.isEmpty()) {
			clientRequest(clients.get(0));
			clients.remove(0);
		}
	}
	
	private boolean clientConnectNot() {
		Socket client;
		try {
			client = new Socket(this.hostName_,this.port_);
			
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			DataInputStream din = new DataInputStream(client.getInputStream());
			
			String syn = Integer.toString((int) (Math.random() * 1000));
			dout.writeUTF(syn);  
			dout.flush(); 
			
			String synPlusAck = din.readUTF();
			String[] synAckSplit = synPlusAck.split(" ");
			
			USER_CLASS c;
			double flip = Math.random();
			if (flip >= .5) {
				c = USER_CLASS.FREE;
			}
			else {
				c = USER_CLASS.PAID;
			}
			
			if (c.equals(USER_CLASS.FREE)) {
				if (synAckSplit[0].equals(syn)) {
					dout.writeUTF(synAckSplit[1] + " " + "FREE");
				}
				else {
					//failed handshake
					dout.close();  
					din.close();
				}
			}
			else if (c.equals(USER_CLASS.PAID)) {
				if (synAckSplit[0].equals(syn)) {
					dout.writeUTF(synAckSplit[1] + " " + "PAID");
				}
				else {
					//failed handshake
					dout.close();  
					din.close();
				}
			}
			else {
				System.out.println("client userclass unknown");
				dout.close();  
				din.close();
			}
			
			clientRequest(client);
			client.close();
			/*
			long interArrival = (long) (Math.random() * 500);
			try {
				TimeUnit.MILLISECONDS.sleep(interArrival);
			} catch (InterruptedException e) {
			}
			*/
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private Socket clientConnect() {
		USER_CLASS c;
		double flip = Math.random();
		if (flip >= .5) {
			c = USER_CLASS.FREE;
		}
		else {
			c = USER_CLASS.PAID;
		}
	
		try {
			Socket client = new Socket("localhost",6666);
			
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			
			String syn = Integer.toString((int) (Math.random() * 1000));
			
			dout.writeUTF(syn);  
			dout.flush(); 
			//System.out.println("CLIENT - Sent SYN");
			
			DataInputStream din = new DataInputStream(client.getInputStream());
			String synPlusAck = din.readUTF();
			String[] synAckSplit = synPlusAck.split(" ");
			
			if (c.equals(USER_CLASS.FREE)) {
				if (synAckSplit[0].equals(syn)) {
					//System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(synAckSplit[1] + " " + "FREE");
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
				if (synAckSplit[0].equals(syn)) {
					//System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(synAckSplit[1] + " " + "PAID");
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
