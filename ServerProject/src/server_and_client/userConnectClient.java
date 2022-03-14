package server_and_client;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class userConnectClient {

	public static void main(String[] args) {
		//Scanner scanner = new Scanner(System.in);
		//System.out.println("Type \"connect\" to connect to the service");
		System.out.println("Attempting to connect to the service");
		
		long startTime = System.currentTimeMillis();
		
		try {
			Socket s = new Socket("localhost",6666);  
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Connection Failed!");
		}
	}

}
