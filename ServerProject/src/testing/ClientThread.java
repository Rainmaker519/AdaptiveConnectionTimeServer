package testing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import server_and_client.USER_CLASS;

public class ClientThread extends Thread {
	private int numClients_;
	private String hostName_;
	private int port_;
	
	private ArrayList<Long> responseTimesMilliseconds;
	private ArrayList<Long> waitTimesMilliseconds;
	
	/**
	 * Thread for individual client connection, used entirely for load and stability testing purposes.
	 * @param numClients - Number of clients run by each ClientThread
	 * @param hostName - Host name of server to connect to.
	 * @param port - Port of server to connect to.
	 */
	protected ClientThread(int numClients, String hostName, int port) {
		numClients_ = numClients;
		hostName_ = hostName;
		port_ = port;
		
		responseTimesMilliseconds = new ArrayList<>();
		waitTimesMilliseconds = new ArrayList<>();
	}
	
	/**
	 * The code which the client thread will execute when started.
	 * Handles n clients connecting, being validated, and executing a simple request.
	 */
	public void run() {
		//make and connect client
		ArrayList<Socket> clients = new ArrayList<>();
		long start = 0;
		for (int i = 0; i < numClients_; i++) {
			clients.add(clientConnect(.5));
			Date date = new Date();
		    start = date.getTime();
		}
		while (!clients.isEmpty()) {
			Date date = new Date();
		    long end = date.getTime();
		    if (end-start > 0) {
		    	waitTimesMilliseconds.add(end-start);
		    }
			clientRequest(clients.get(0));
			clients.remove(0);
			date = new Date();
		    end = date.getTime();
		    if (end-start > 0) {
		    	responseTimesMilliseconds.add(end-start);
		    }
		}
	}
	
	/**
	 * Performs TCP handshake, if client accepted and validated the return val will be their socket.
	 * @param cutoff - decimal value between 0 and 1 deciding ratio of free to paid users (above cutoff is free, below is paid)
	 * @return - Accepted and validated user socket or null
	 */
	private Socket clientConnect(double cutoff) {
		//choose user class randomly based on a cutoff which acts as the class ratio [1-cutoff is % free] 
		USER_CLASS c;
		double flip = Math.random();
		if (flip >= cutoff) {
			c = USER_CLASS.FREE;
		}
		else {
			c = USER_CLASS.PAID;
		}
		//client attempts to connect
		try {
			Socket client = new Socket(hostName_,port_);
			
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			
			//synchronizing packet generated
			String syn = Integer.toString((int) (Math.random() * 1000));
			//and sent to server
			dout.writeUTF(syn);  
			dout.flush(); 
			
			//if connection accepted
			//server send back its own syn packet and the client sent syn as an acknowledgement of the client
			DataInputStream din = new DataInputStream(client.getInputStream());
			String synPlusAck = din.readUTF();
			String[] synAckSplit = synPlusAck.split(" ");
			
			if (c.equals(USER_CLASS.FREE)) {
				//if server ack is correct send back the server's syn (as an ack) plus the user's class
				if (synAckSplit[0].equals(syn)) {
					dout.writeUTF(synAckSplit[1] + " " + "FREE");
				}
				else {
					dout.close();  
					din.close();
				}
			}
			else if (c.equals(USER_CLASS.PAID)) {
				//if server ack is correct send back the server's syn (as an ack) plus the user's class
				if (synAckSplit[0].equals(syn)) {
					dout.writeUTF(synAckSplit[1] + " " + "PAID");
				}
				else {
					dout.close();  
					din.close();
				}
			}
			else {
				System.out.println("client userclass unknown");
				dout.close();  
				din.close();
			}
			//return socket of validated client
			return client;
			
			
			
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Handles a simple client request asking for a good morning or good night message.
	 * @param client - Socket of an accepted and validated client.
	 * @return - True if request succeeds, false if not.
	 */
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
	
	/**
	 * Returns a list of response times accumulated from all clients on this client thread.
	 * @return - Accumulated response times as ArrayList<Long>
	 */
	public ArrayList<Long> getResponseTimes() {
		return this.responseTimesMilliseconds;
	}
	/**
	 * Returns a list of wait times accumulated from all clients on this client thread.
	 * @return - Accumulated wait times as ArrayList<Long>
	 */
	public ArrayList<Long> getWaitTimes() {
		return this.waitTimesMilliseconds;
	}
}
