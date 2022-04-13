package server_and_client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import adaptive_loop.Loop;

public class CentralServer extends Thread {
	
	/** CURRENT PROGRESS - FOR ME WHEN I COME BACK TO DO MORE!
	 * THE FIRST TWO CONNECTIONS DO FINE AND SEEM LIKE THE
	 * TASKS ARE BEING HANDLED PROPERLY BY THE SUBSERVERS
	 * 
	 * AFTER THAT IT SEEMS LIKE THERE IS A ISSUE WITH 
	 * THREADING OR SOMETHING WHERE THE SERVER NO LONGER 
	 * IS RECIEVING THE REQUESTS AND THE CLIENTS
	 * ARE STUCK WAITING WITHOUT THEIRS GOING
	 * THROUGH!
	 * 
	 * -------
	 * SOCKETS STILL ALL CLOSED BY THE TIME THEY ARE IN THE 
	 * SUBSERVERS, POST LIKELY WRONG OR JAVA CHANGED
	 * 
	 * MIGHT WORK IF I MOVE HANDLING TCP TO SUBSERVERS,
	 * IN THE POST HE ASSIGNS TO SUBSERVER DIRECTLY AFTER ACCEPTING
	 * ON CENTRAL, POSSIBLE THAT OPENING AND CLOSING 
	 * READ/WRITE STREAMS ALSO CLOSES THE SOCKET,
	 * IF SO NEEDS TO BE DONE IN THE SUBSERVER SO I CAN USE THE OPEN CONNECTION
	 * TO HANDLE USER REQUESTS
	 * 
	 * [TESTING TO SEE IF ADDING A GPG KEY WILL LET MY COMMITS
	 * SHOW UP AS CONTRIBUTIONS ON MY PROFILE]
	 * 
	 * gpg testing - final
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * ADD SOMETHING TO SAVE THE RATIO OF SUBSERVERS FROM THE PREVIOUS LOOP OR SOMETHING,				!!!!!!!!!!
	 * CURRENTLY THE SUBSERVERS ARE FREQUENTLY EMPTY WHEN MONITOR CHECKS THE RATIO FOR ADAPTING			!!!!!!!!!!
	 * 
	 * 
	 * 
	 * 
	 * ALSO, EVERYTHING BROKE WHEN I ADDED A USER REQUEST AND SERVER RESPONSE AS THE 
	 * SUBSERVER FUNCTION, STALLING WAITING FOR USER INPUT PROBABLY
	 * 
	 * PROB THE TWO SUBSERVER PROCESSES I DIDNT COULDNT ACCOUNT FOR BEFORE
	 * FIGURE OUT WHERE THOSE ARE
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * NOPE!!! ITS BLOCKING AT THE ACCEPT METHOD IN handleIncomingConnections() 
	 * BECAUSE MY SHIT TEST METHOD IS ONLY ACTUALLY SENDING ONE CONNECTION REQUEST
	 * FOR SOME REASON IDK WHY YET
	 */
	
	public final int maxClients = 7;
	private final ServerSocket centralSocket;
	private final SubServer[] subServers = new SubServer[maxClients];
	
	public double[] relativeDelayFactors;
	
	private Queue<Socket> freeQueue;
	private Queue<Socket> paidQueue;
	
	private int requestsHandledCount;
	
	
	
	public CentralServer(int portNumber, double relativeDelayFactorFree, double relativeDelayFactorPaid) throws IOException {
		this.centralSocket = new ServerSocket(portNumber);
		this.relativeDelayFactors = new double[2];
		this.relativeDelayFactors[0] = relativeDelayFactorFree;
		this.relativeDelayFactors[1] = relativeDelayFactorPaid;
		this.freeQueue = new LinkedList<Socket>();
		this.paidQueue = new LinkedList<Socket>();
		
		this.requestsHandledCount = 0;
		
		start();
	}
	
	
	@Override
    public void run() {
		Loop mape_k = new Loop(this);
		double timeElapsed = 0;
        while ( !interrupted() ) {
        	System.out.println("-------------------------------------------------------");
        	double startTime = System.currentTimeMillis();
        	//Maybe just need to pull either the loop or the handling of incoming connections into 
        	//its own thread
        	while (timeElapsed < 50) {
        		System.out.println("testos2");//not even making it here?
        		handleIncomingConnections();
        		System.out.println("testos3");//not even making it here?
        		timeElapsed = System.currentTimeMillis() - startTime;
        		//System.out.println("---------------------");
        	}
        	System.out.println("testos4");//not even making it here?
        	mape_k.run();
        	
        	System.out.println("==========");
        	double[] ratios = mape_k.getLoopInstanceRatios();
        	int[] countsAndPlan = mape_k.getLoopInstanceCountsPlusPlan();
        	System.out.println("FQL: " + this.getFreeQueueLength() + "| PQL: " + this.getPaidQueueLength());
        	System.out.println("[DR: " + ratios[0] + "|AR: " + ratios[1] + "||C: (" + countsAndPlan[0] + "/" + countsAndPlan[1] + ")|P: " + countsAndPlan[2] + "]");
        	System.out.println("CServerClosedStatus: " + this.centralSocket.isClosed());
        	System.out.println("Total Requests Handled: " + this.requestsHandledCount);
        	System.out.println("==========");
        	
        	startTime = System.currentTimeMillis();
        	timeElapsed = 0;
        }
        System.out.println("QQQQQQQQQQ(CSERVER INTERRUPTED)QQQQQQQQQQ");
    }
	
	public void handleIncomingConnections() {
		Socket connection;
		try {
			//System.out.println(this.centralSocket.getLocalPort());
			connection = this.centralSocket.accept();
			
			//connection.setKeepAlive(true);
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
			System.out.println(ackFinalCheckSplit[1]);
			
			if (!ackFinalCheckSplit[0].equals(synCheck)) {
				//System.out.println("SERVER - Failed user ack check");
			}
			else {
				if (ackFinalCheckSplit[1].equals("FREE")) {
					System.out.println("SERVER - Connection to free user accepted, adding to service queue");
					if (addToFreeQueue(connection)) {
						System.out.println("Successfully added to free queue");
					}
				}
				else if (ackFinalCheckSplit[1].equals("PAID")) {
					System.out.println("SERVER - Connection to paid user accepted, adding to service queue");
					if (addToPaidQueue(connection)) {
						System.out.println("Successfully added to paid queue");
					}
				}
				else {
					System.out.println("SERVER - Failed user ack check");
				}
			}
			//dout.close(); 
			//din.close();
			//If I close these here the connection is closed entirely as well
			//connection.close();
            
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("CS - Connection Attempt Failed");
		}
	}
	
	public boolean setSubserverNull(int m_id) {
		if (this.subServers[m_id] != null) {
			this.subServers[m_id] = null;
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean addToFreeQueue(Socket connection) {
		return this.freeQueue.add(connection);
	}
	public boolean addToPaidQueue(Socket connection) {
		return this.paidQueue.add(connection);
	}
	
	public Socket getFreeUser() {
		if (!this.freeQueue.isEmpty()) {
			return this.freeQueue.poll();
		}
		else {
			return null;
		}
	}
	public Socket getPaidUser() {
		if (!this.paidQueue.isEmpty()) {
			return this.paidQueue.poll();
		}
		else {
			return null;
		}
	}
	
	public int getFreeQueueLength() {
		return this.freeQueue.size();
	}
	public int getPaidQueueLength() {
		return this.paidQueue.size();
	}
	
	/**
	 * 
	 * @return - an array of ints representing the subservers. 0 if free, 1 if paid, -1 if not in use
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
	 * Iterates over all available subservers and assigns the connection to one of them assuming at least one subserver is assigned a null value
	 * @param connection - Connection accepted at the central server socket
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

	public int getPortNumberTESTING () {
		return centralSocket.getLocalPort();
	}
	
	
	protected class SubServer extends Thread {

        final private int m_id;
        final private Socket subServerSocket;
        final private CentralServer reference;

        final private USER_CLASS userClass;

        public SubServer( Socket connection , int id , USER_CLASS u_class, CentralServer serverReference) {
            this.m_id = id;
            this.subServerSocket = connection;
            this.userClass = u_class;
            this.reference = serverReference;
            start();
        }

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
            	 }
            	 
            	 try {
            		 DataOutputStream dout = new DataOutputStream(subServerSocket.getOutputStream());  
            		 DataInputStream din = new DataInputStream(subServerSocket.getInputStream());
            		 
            		 //System.out.println("Testos");
            		 
            		 String request = din.readUTF();
            		 
            		 if (request.equals("GM")) {
            			 dout.writeUTF("Good Morning User-" + this.m_id + "!");
            			 dout.flush();
            		 }
            		 else if (request.equals("GN")) {
            			 dout.writeUTF("Good Night User-" + this.m_id + "!");
            			 dout.flush();
            		 }
            		 else {
            			 dout.writeUTF("Please use \"GM\" or \"GN\" User-" + this.m_id + "!");
            			 dout.flush();
            		 }
            	 } catch (Exception e) {
            		 System.out.println("Socket open but streams broken???");
            	 }
            	 
            	 /*
            	 try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
            	 this.reference.requestsHandledCount++;
            	 close();
            	 interrupt();
            	 //this.reference.setSubserverNull(this.m_id);
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
				this.subServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	this.reference.setSubserverNull(m_id);
        }
    }
}



