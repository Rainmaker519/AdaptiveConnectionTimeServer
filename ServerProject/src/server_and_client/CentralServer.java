package server_and_client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer extends Thread {
	
	private final ServerSocket centralSocket;
	public final static int maxClients = 8;
	private final SubServer[] subServers = new SubServer[maxClients];
	
	public CentralServer(int portNumber) throws IOException {
		this.centralSocket = new ServerSocket(portNumber);
		start();
	}
	
	
	/**
	 THINGS I NEED TO KEEP TRACK OF THAT I ONLY HAVE ACCESS TO DURING run()
	 
	 1. TIME THAT THE USER CONNECTION IS accept()'ed, TOGETHER WITH THE USER RECORDING THEIR CONNECTION ATTEMPT TIME I CAN GET THE FULL CONNECTION DELAY
	  
	 
	 2. NEED TO SOMEHOW RECORD TIME USED FOR THIS TCP HANDSHAKE AND HAVE A REFERENCE TO THE SPECIFIC USER INCLUDING THEIR CLASS, MAKES UP FIRST PART OF COMMUNICATION DELAY
	 (THE OTHER HALF IS THE TIME FOR TRANSMITTING THE HTTP REQUEST AND RESPONSE)
	 
	 ~processing delay will need to be recorded in the process() method in subserver
	 **/
	
	
	@Override
    public void run() {
		boolean notRunYet = true;
        while ( !interrupted() && notRunYet) {
        	//wait for clients
        	notRunYet = false;
        	Socket connection;
			try {
				connection = this.centralSocket.accept();
				System.out.println("SERVER - Client Connection Request Recieved");
				
				DataInputStream din = new DataInputStream(connection.getInputStream());
				String synCheck = din.readUTF();
				
				DataOutputStream dout = new DataOutputStream(connection.getOutputStream());  
				
				String ackResponse = synCheck + "1";
				
				dout.writeUTF(ackResponse);  
				dout.flush();  
				
				
				String ackFinalCheckFull = din.readUTF();
				String[] ackFinalCheckSplit = ackFinalCheckFull.split(" ");
				
				System.out.println(synCheck);
				System.out.println(ackFinalCheckFull);
				
				if (!ackFinalCheckSplit[0].equals(synCheck)) {
					System.out.println("SERVER - Failed user ack check");
				}
				else {
					if (ackFinalCheckSplit[1].equals("FREE")) {
						System.out.println("SERVER - Connection to user verified and accepted");
						assignConnectionToSubServer( connection , USER_CLASS.FREE);
					}
					else if (ackFinalCheckSplit[1].equals("PAID")) {
						System.out.println("SERVER - Connection to user verified and accepted");
						assignConnectionToSubServer( connection , USER_CLASS.PAID);
					}
					else {
						System.out.println("SERVER - Failed user ack check");
					}
				}
				dout.close(); 
				din.close();
				/**
				if (testString.equals("FREE") || testString.equals("PAID")) {
					System.out.println("Client Connection SYN Accepted");
				}
				**/
				//should i add the TCP handshake here to verify user connections? TCP will definitely will make the rest of debugging significantly easier
	            //could also be better to do the handshake with the subserver????? this would mean that the delay would be decided before it
	            //might be better to do it here, could use the user class as the SYN packet of the TCP connection
	            
	            //btw the central serverSocket is where the TCP queue is in case I forget
	            
	            
	            //to implement the adaptive system I need to queue these connections and serve connections to subservers with the timing I want
	            
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("CS - Connection Attempt Failed");
			}
        }
    }
	
	/**
	 * Assigns a client connection to one of the subservers, automatically assigns the FREE user class (need to have way to signaling premium somehow or simulating it for testing purposes)
	 * @param connection - Connection accepted at the central server socket
	 */
	 public void assignConnectionToSubServer( Socket connection , USER_CLASS uClass) {
         for ( int i = 0 ; i < maxClients ; i++ ) {
             //find an unassigned subserver (waiter)
             if ( this.subServers[ i ] == null ) {
                  this.subServers[ i ] = new SubServer( connection , i , uClass);
                  break;
             }
         }
    }

	public int getPortNumberTESTING () {
		return centralSocket.getLocalPort();
	}
	
	
	protected class SubServer extends Thread {

        final private int m_id;
        final private Socket subServer;

        private USER_CLASS userClass;

        public SubServer( Socket connection , int id , USER_CLASS u_class) {
            this.m_id = id;
            this.subServer = connection;
            this.userClass = u_class;
            start();
        }

        @Override
        public void run() {
             while( !interrupted() ) {
                 //process a client request
                 //this is for you to implement
             }
        }

        //as an example, if you read String messages from your client,
        //just call this method from the run() method to process the client request
        public void process( String message ) {

        }

        /**
         * terminates the connection with this client (i.e. stops serving him)
         */
        public void close() {
            try {
                 this.subServer.close();
            } catch ( IOException e ) {
                 //ignore
            }
        }
    }
}



