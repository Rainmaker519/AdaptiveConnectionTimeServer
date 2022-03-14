package server_and_client;
import java.io.*;  
import java.net.*;
import java.util.Scanner;  

public class clientTesting {

	public static void main(String[] args) {
		try{    
			long startTime = System.currentTimeMillis();
			
			Socket s=new Socket("localhost",6666);  
			DataOutputStream dout=new DataOutputStream(s.getOutputStream());  
			
			System.out.println("Type one word to send!");
			Scanner scanner = new Scanner(System.in);
			String userTyped = scanner.nextLine();
			scanner.close();
			
			dout.writeUTF(userTyped + " " + startTime);  
			System.out.println("Sent!");
			
			dout.flush();  
			dout.close();  
			
			s.close();  
		} 
		catch(Exception e) {
			System.out.println(e);
			System.out.println("Connection Failed!");
		}  
	}  
}
