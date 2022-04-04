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

	//@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
	}

	//@AfterAll
	static void tearDownAfterClass() throws Exception {
		
	}

	//BeforeEach
	void setUp() throws Exception {
		//this.centralServer = new CentralServer(6666);
		//System.out.println(this.centralServer.getState());
		//centralServer.run();
		//System.out.println(this.centralServer.getState());
	}

	//AfterEach
	void tearDown() throws Exception {
		//centralServer.interrupt();
	}
	
	@Test
	void testRun() {
		try {
			CentralServer c = new CentralServer(6666,1,1.01);
			
			clientConnect(USER_CLASS.FREE);
			clientConnect(USER_CLASS.PAID);
			clientConnect(USER_CLASS.FREE);
			clientConnect(USER_CLASS.FREE);
			clientConnect(USER_CLASS.PAID);
			clientConnect(USER_CLASS.FREE);
			clientConnect(USER_CLASS.FREE);
			
			
			//TimeUnit.SECONDS.sleep(10);
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
			System.out.println("CLIENT - Sent SYN");
			
			DataInputStream din = new DataInputStream(client.getInputStream());
			String synPlusAck = din.readUTF();
			
			if (c.equals(USER_CLASS.FREE)) {
				if (synPlusAck.equals(syn + "1")) {
					System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(syn + " " + "FREE");
				}
				else {
					System.out.println("CLIENT - ACK not accepted");
					
					dout.close();  
					din.close();
					
					client.close();
					
					fail("CLIENT - ACK not accepted");
				}
			}
			else if (c.equals(USER_CLASS.PAID)) {
				if (synPlusAck.equals(syn + "1")) {
					System.out.println("CLIENT - ACK accepted, sending final response with user class");
					dout.writeUTF(syn + " " + "PAID");
				}
				else {
					System.out.println("CLIENT - ACK not accepted");
					
					dout.close();  
					din.close();
					
					client.close();
					
					fail("CLIENT - ACK not accepted");
				}
			}
			else {
				System.out.println("client userclass unknown");
			}
			dout.close();  
			din.close();
			
			client.close();
			return;
			
		} catch (Exception e) {
			fail("CLIENT - Failed to open client socket");
		}
	}
	
	/*
	@Test
	void testRun2() {
		try {
			CentralServer centralServer = new CentralServer(6666,1,1.01);
			
			Socket client = new Socket("localhost",6666);
			
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());  
			
			String syn = Integer.toString((int) (Math.random() * 1000));
			
			dout.writeUTF(syn);  
			dout.flush(); 
			System.out.println("CLIENT - Sent SYN");
			
			DataInputStream din = new DataInputStream(client.getInputStream());
			String synPlusAck = din.readUTF();
			
			if (synPlusAck.equals(syn + "1")) {
				System.out.println("CLIENT - ACK accepted, sending final response with user class");
				dout.writeUTF(syn + " " + "FREE");
			}
			else {
				System.out.println("CLIENT - ACK not accepted");
				
				dout.close();  
				din.close();
				
				client.close();
				
				fail("CLIENT - ACK not accepted");
			}
			dout.close();  
			din.close();
			
			client.close();
			centralServer.interrupt();
			return;
			
		} catch (Exception e) {
			fail("CLIENT - Failed to open client socket");
		}
	}
	/*
	@Test
	void testCentralServer() {
		try {
			CentralServer centralServer = new CentralServer(6666);
			if (centralServer.getPortNumberTESTING() != 6666) {
				System.out.println("startwwww");
				fail("Central Server not listening on the correct port!");
			}
			centralServer.interrupt();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed to make central server");
		}
		
	}
	*/
	//Test
	void testAssignConnectionToSubServer() {
		return;
	}

}
