package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
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
			CentralServer c = new CentralServer(6666,1,1.01);
			int l = 0; 
			
			while (l < 500) {
				double ran = Math.random() * 100;
				if (ran >= 50) {
					clientConnect(USER_CLASS.FREE);
				}
				else {
					clientConnect(USER_CLASS.PAID);
				}
				l++;
			}
			
			
			TimeUnit.SECONDS.sleep(25);
			c.interrupt();
		}
		catch(Exception e) {
			fail();
		}
	}
	
	private void clientConnect(USER_CLASS c) {
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
			}
			return;
			
		} catch (Exception e) {
			fail("CLIENT - Failed to open client socket");
		}
	}

}
