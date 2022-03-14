package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server_and_client.CentralServer;

class Test_centralServer {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
	}

	@AfterAll
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
		//centralServer.start();
		
		try {
			CentralServer centralServer = new CentralServer(6666);
			
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

	//Test
	void testAssignConnectionToSubServer() {
		return;
	}

}
