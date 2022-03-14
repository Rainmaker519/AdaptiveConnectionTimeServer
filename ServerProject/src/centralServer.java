import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class centralServer extends Thread {
	
	private final ServerSocket centralSocket;
	public final static int maxClients = 8;
	private final SubServer[] subServers = new SubServer[maxClients];
	
	public centralServer(int portNumber) throws IOException {
		this.centralSocket = new ServerSocket(portNumber);
		start();
	}
	
	@Override
    public void run() {
        while ( !interrupted() ) {
             //wait for clients
             Socket connection = this.centralSocket.accept();
             //should i add the TCP handshake here to verify user connections? TCP will definitely will make the rest of debugging significantly easier
             //could also be better to do the handshake with the subserver????? this would mean that the delay would be decided before it
             //might be better to do it here, could use the user class as the SYN packet of the TCP connection
             
             
             //to implement the adaptive system I need to queue these connections and serve connections to subservers with the timing I want
             assignConnectionToSubServer( connection );
        }
    }
	
	/**
	 * Assigns a client connection to one of the subservers, automatically assigns the FREE user class (need to have way to signaling premium somehow or simulating it for testing purposes)
	 * @param connection - Connection accepted at the central server socket
	 */
	 public void assignConnectionToSubServer( Socket connection ) {
         for ( int i = 0 ; i < maxClients ; i++ ) {

             //find an unassigned subserver (waiter)
             if ( this.subServers[ i ] == null ) {
                  this.subServers[ i ] = new SubServer( connection , i , USER_CLASS.FREE);
                  break;
             }
         }
    }

	public static void main(String[] args) {
		
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

}

