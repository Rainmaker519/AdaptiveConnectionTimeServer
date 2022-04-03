package server_and_client;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UserConnectClient {

	public static void main(String[] args) {
		//Scanner scanner = new Scanner(System.in);
		//System.out.println("Type \"connect\" to connect to the service");
		System.out.println("Attempting to connect to the service");
		
		//long startTime = System.currentTimeMillis();
		
		try {
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
			
			TimeUnit.SECONDS.sleep(15);
			
			client.close();
			System.out.println("--------------------------------------------------------------");
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Connection Failed!");
			System.out.println("--------------------------------------------------------------");
		}
		
		
	}

}
