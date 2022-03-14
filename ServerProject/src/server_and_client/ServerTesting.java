package server_and_client;
import java.io.*;  
import java.net.*;  

public class ServerTesting {

	public static void main(String[] args) {
		try {
			CentralServer centralServer = new CentralServer(6666);
			centralServer.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
