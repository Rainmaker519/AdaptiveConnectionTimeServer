package testing;

import server_and_client.CentralServer;

public class DevTesting {

	/**
	 * Just starts an instance of CentralServer on 6666 with a desired even enforced response time.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CentralServer c = new CentralServer(6666,1,1.01);
			System.out.println(c);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
