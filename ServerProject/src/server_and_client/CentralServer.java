package server_and_client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import adaptive_loop.Loop;

public class CentralServer extends Thread {
	
	public final int maxClients = 3;
	private final ServerSocket centralSocket;
	private final SubServer[] subServers = new SubServer[maxClients];
	
	public double[] relativeDelayFactors;
	
	private Queue<Socket> freeQueue;
	private Queue<Socket> paidQueue;
	
	private static int requestsHandledCount;
	
	
	/**
	 * The managing server of the system, manages incoming connections and serves to correct queues.
	 * @param portNumber - Port number of server.
	 * @param relativeDelayFactorFree - Free half of desired ratio.
	 * @param relativeDelayFactorPaid - Paid half of desired ratio.
	 * @throws IOException - Throws if server fails to start correctly.
	 */
	public CentralServer(int portNumber, double relativeDelayFactorFree, double relativeDelayFactorPaid) throws IOException {
		this.centralSocket = new ServerSocket(portNumber);
		this.relativeDelayFactors = new double[2];
		this.relativeDelayFactors[0] = relativeDelayFactorFree;
		this.relativeDelayFactors[1] = relativeDelayFactorPaid;
		this.freeQueue = new LinkedList<Socket>();
		this.paidQueue = new LinkedList<Socket>();
		
		requestsHandledCount = 0;
		
		start();
	}
	
	/**
	 * What runs when the server is started, which occurs at initialization in this case.
	 * Spends 300 milliseconds waiting for connections, then runs a loop of mape-k.
	 * This continues as long as the server is up.
	 */
	@Override
    public void run() {
		Loop mape_k = new Loop(this);
		double timeElapsed = 0;
		try {
			this.centralSocket.setSoTimeout(300);
		} catch (SocketException e) {

		}
        while ( !interrupted() ) {
        	System.out.println("-------------------------------------------------------");
        	double startTime = System.currentTimeMillis();
        	
        	while (timeElapsed < 50) {
        		handleIncomingConnections();
        		timeElapsed = System.currentTimeMillis() - startTime;
        	}
        	
        	mape_k.run();
        	
        	System.out.println("==========");
        	double[] ratios = mape_k.getLoopInstanceRatios();
        	int[] countsAndPlan = mape_k.getLoopInstanceCountsPlusPlan();
        	System.out.println("FQL: " + this.getFreeQueueLength() + "| PQL: " + this.getPaidQueueLength());
        	System.out.println("[DR: " + ratios[0] + "|AR: " + ratios[1] + "||C: (" + countsAndPlan[0] + "/" + countsAndPlan[1] + ")|P: " + countsAndPlan[2] + "]");
        	System.out.println("Total Requests Handled: " + requestsHandledCount);
        	System.out.println("==========");
        	
        	startTime = System.currentTimeMillis();
        	timeElapsed = 0;
        }
        System.out.println("QQQQQQQQQQ(CSERVER INTERRUPTED)QQQQQQQQQQ");
    }
	
	/**
	 * The server side of handling incoming connections, the TCP handshake is fulfilled.
	 * If everything works as intended, the verified client will be added to either the 
	 * paid or free queue.
	 */
	public void handleIncomingConnections() {
		Socket connection;
		try {
			connection = this.centralSocket.accept(); 	
			
			DataInputStream din = new DataInputStream(connection.getInputStream());
			String synCheck = din.readUTF();
			
			DataOutputStream dout = new DataOutputStream(connection.getOutputStream());  
			
			String syn = Integer.toString((int) (Math.random() * 1000));
			
			String ackResponse = synCheck + " " + syn;
			
			dout.writeUTF(ackResponse);  
			dout.flush();  
			
			String ackFinalCheckFull = din.readUTF();
			String[] ackFinalCheckSplit = ackFinalCheckFull.split(" ");
			
			
   		 	
			
			
			if (!ackFinalCheckSplit[0].equals(syn)) {
				//Failed user ack check.
			}
			else {
				if (ackFinalCheckSplit[1].equals("FREE")) {
					//Connection to free user accepted, adding to service queue.
					if (addToFreeQueue(connection)) {
						System.out.println("Successfully added to free queue");
					}
					else {
						System.out.println("SERVER - Failed user ack check - internal");
					}
				}
				else if (ackFinalCheckSplit[1].equals("PAID")) {
					//Connection to paid user accepted, adding to service queue.
					if (addToPaidQueue(connection)) {
						System.out.println("Successfully added to paid queue");
					}
					else {
						System.out.println("SERVER - Failed user ack check - internal");
					}
				}
				else {
					System.out.println("SERVER - Failed user ack check");
				}
			}
		} catch (IOException e) {
			System.out.println("CS - Connection Attempt Failed");
		}
	}
	
	/**
	 * Sets a subserver slot to null, used for dead subservers.
	 * @param m_id - Subserver ID.
	 * @return - True if subserver was NOT null, and now IS null.
	 */
	public boolean setSubserverNull(int m_id) {
		if (this.subServers[m_id] != null) {
			this.subServers[m_id] = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Adds given connection to the free connection queue.
	 * @param connection - Verified client socket.
	 * @return - True if queue add is successful.
	 */
	public boolean addToFreeQueue(Socket connection) {
		return this.freeQueue.add(connection);
	}
	/**
	 * Adds given connection to the paid connection queue.
	 * @param connection - Verified client socket.
	 * @return - True if queue add is successful.
	 */
	public boolean addToPaidQueue(Socket connection) {
		return this.paidQueue.add(connection);
	}
	
	/**
	 * Polls a verified free user from the free queue.
	 * @return - The socket of that user.
	 */
	public Socket getFreeUser() {
		if (!this.freeQueue.isEmpty()) {
			return this.freeQueue.poll();
		}
		else {
			return null;
		}
	}
	/**
	 * Polls a verified paid user from the free queue.
	 * @return - The socket of that user.
	 */
	public Socket getPaidUser() {
		if (!this.paidQueue.isEmpty()) {
			return this.paidQueue.poll();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets the current length of the free queue.
	 * @return - The current length of the free queue.
	 */
	public int getFreeQueueLength() {
		return this.freeQueue.size();
	}
	/**
	 * Gets the current length of the paid queue.
	 * @return - The current length of the paid queue.
	 */
	public int getPaidQueueLength() {
		return this.paidQueue.size();
	}
	
	/**
	 * Gets an array of values representing the classes of each subserver.
	 * @return - An array of ints representing the subservers. 0 if free, 1 if paid, -1 if not in use.
	 */
	public int[] getSubserverClasses() {
		int[] result = new int[this.maxClients];
		for (int i = 0; i < this.maxClients; i++) {
			if (subServers[i] == null) {
				result[i] = -1;
			}
			else if (subServers[i].userClass == USER_CLASS.FREE) {
				result[i] = 0;
			}
			else {
				result[i] = 1;
			}
		}
		return result;
	}
	
	/**
	 * Iterates over all available subservers and assigns the connection to one of them assuming at least one subserver is assigned a null value.
	 * @param connection - Connection accepted at the central server socket.
	 */
	 public void assignConnectionToSubServer(USER_CLASS uClass) {
         for ( int i = 0 ; i < maxClients ; i++ ) {
             if ( this.subServers[ i ] == null ) {
            	 if (uClass.equals(USER_CLASS.FREE)) {
            		 Socket socket = getFreeUser();//next free socket in queue
            		 this.subServers[ i ] = new SubServer(socket, i , USER_CLASS.FREE, this);
                     return;
            	 }
            	 else {
            		 Socket socket = getPaidUser();//next paid socket in queue
            		 this.subServers[ i ] = new SubServer(socket, i , USER_CLASS.PAID, this);
                     return;
            	 }
                  
             }
         }
    }
	 
	/**
	 * Returns port number of central server.
	 * @return - Port number of central server.
	 */
	public int getPortNumberTESTING () {
		return centralSocket.getLocalPort();
	}
	
	
	protected class SubServer extends Thread {

        final private int m_id;
        final private Socket subServerSocket;
        final private CentralServer reference;

        final private USER_CLASS userClass;

        /**
         * SubServer user to handle user requests once they've been verified by the handshake from
         * the central server and queued up.
         * @param connection - How we pass the user connection in from that queue.
         * @param id - The id of the subserver 0-(numSubservers).
         * @param u_class - The user class this subserver is serving.
         * @param serverReference - A ease of access reference to the central server.
         */
        public SubServer( Socket connection , int id , USER_CLASS u_class, CentralServer serverReference) {
            this.m_id = id;
            this.subServerSocket = connection;
            this.userClass = u_class;
            this.reference = serverReference;
            start();
        }

        /**
         * The code that will run if the SubServer is started or run. This checks if a subserver is not closed,
         * then handles the defined client request of asking for a good morning or good night message.
         */
        @Override
        public void run() {
             while( !interrupted() ) {
                 //process a client request
            	 if (subServerSocket.isClosed()) {
            		 System.out.println("SUBSERVER DOING NOTHING ON A CLOSED CONNECTION?");
            		 close();
                	 interrupt();
                	 return;
            	 }
            	 else {
            		 System.out.println("Server_Operation_" + this.m_id + "_" + this.userClass);
            		 requestsHandledCount = requestsHandledCount + 1;
            	 }
            	 
            	 try {
            		 DataOutputStream dout = new DataOutputStream(subServerSocket.getOutputStream());  
            		 DataInputStream din = new DataInputStream(subServerSocket.getInputStream());
            		 
            		 String request = din.readUTF();
            		 
            		 
            		 if (request.equals("GM")) {
            			 dout.writeUTF("[Subserver-" + this.m_id + "] Good Morning!");
            			 dout.flush();
            		 }
            		 else if (request.equals("GN")) {
            			 dout.writeUTF("[Subserver-" + this.m_id + "] Good Night!");
            			 dout.flush();
            		 }
            		 else {
            			 dout.writeUTF("[Subserver-" + this.m_id + "] Please use \"GM\" or \"GN\"");
            			 dout.flush();
            		 }
            		
            	 } catch (Exception e) {
            		 System.out.println("Socket open but streams broken???");
            	 }
            	 
            	 close();
            	 interrupt();
             }
        }

        /**
         * Terminates the connection with this client (i.e. stops serving him).
         */
        public void close() {
        	try {
				this.subServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	this.reference.setSubserverNull(m_id);
        }
        
        
    }
}




