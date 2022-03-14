package server_and_client;
import java.io.*;  
import java.net.*;  

public class serverTesting {

	public static void main(String[] args) {
		System.out.println("Starting Server");
		
		boolean result = start();
		
		if (result) {
			System.out.println("Successful");
		}
		else {
			System.out.println("Unsuccesful");
		}
	}
	
	public static boolean start() {
		try {
			ServerSocket server = new ServerSocket(6666);
			Socket client = server.accept();
			long endTime = System.currentTimeMillis();
			System.out.println("Client Connected");
			DataInputStream stream = new DataInputStream(client.getInputStream());
			String testString = (String) stream.readUTF();
			long startTime = Long.parseLong(testString.split(" ")[1]);
			System.out.println("Message Recieved: " + testString.split(" ")[0]);
			server.close();
			System.out.println(endTime-startTime);
			return true;
		} catch (Exception e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
			return false;
		}
		
	}

}
