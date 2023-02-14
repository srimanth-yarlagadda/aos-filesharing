import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;

public class Server implements Runnable {
    
    private static ServerSocket serverSocket;
    private Socket clientSocket;
    String recMessage;
    int runServer = 3;
    private static byte[] Data;
    private static Hashtable<String, byte[]> messageDict = new Hashtable<String, byte[]>();
    private static Hashtable<String, String> msgCounter = new Hashtable<String, String>();
    private static ArrayList<String> portOrder = new ArrayList<String>();
    String msgID;
    int newID;
    private Socket thisSocket;
    private static int totalClientReceipts = 0;
    private static int fullDataSizeBytes = 0;
    private static boolean dataReady = false;
    private static int clientsDone = 0;
    private static int debug = 0;

    // Create ServerSocket on the given port
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: "+serverSocket+"\n");            
        } catch (IOException e) {
            System.err.println("Cannot create server");
            e.printStackTrace();
        }
    }

    // After receiving data chunks from the clients, it is stored
    // in Hash Map with proper seuqence number. The key used here
    // is a combination of fileID (f1 or f2) and data sequence
    public void storeMsg(String source, byte[] message) {
        if (msgCounter.containsKey(""+source)) {
            msgID = msgCounter.get(""+source);
        } else {
            msgID = "0";
        }
        newID = Integer.parseInt(msgID);
        newID += 1;
        msgCounter.put(""+source, ""+newID);
        messageDict.put(""+source+"#"+msgID, message);
    }

    /* After accepting a new connection, a new thread is spawned
    and the connection socket is assigned for the instance in this
    new thread. If this is null (which only happens to one thread
    spawned from main process by design), then run() method listens 
    and accepts new client connections.
    */
    public Server assignSocket(Socket tsock) {
        thisSocket = tsock;
        return this;
    }


    /* This method is executed directly by newly spawned threads, since the Server class
    implements Runnable. One thread (spawned by main) - always keeps on listening for new 
    connection requests, if any received, it is accepted and a new thread is spawned to 
    handle it. Same method does both this operations (listening-accepting; processing client 
    request) depending on the instance */
    public void run() {
        
        /* See if there is a client socket assigned, if not keep listening for clients */
        while (thisSocket == null & serverSocket != null) {
            try {
                Socket clinSock = serverSocket.accept();
                portOrder.add(String.valueOf(clinSock.getPort()));
                (new Thread(new Server().assignSocket(clinSock))).start(); // Create new thread for the client
            } catch (IOException except) {
                // except.printStackTrace();
                System.out.println("\nConnection Closed !! \n");
                break;
            }
        }

        if (thisSocket == null) {
            return;
        }

        BufferedReader inStr;
        System.out.println("\nListening...."+thisSocket);
        if (debug == 1) {   // Debug line to identify thread
            System.out.println("Receiving Client on Thread: " + Thread.currentThread().getId());
        }
        try {
            inStr = new BufferedReader(new InputStreamReader(thisSocket.getInputStream()));
            DataInputStream inStreamByte = new DataInputStream(thisSocket.getInputStream());
            boolean receiveData = true;
            int totlen;
            byte[] fidbuf = new byte[2];            // Read the File ID in byte type
            inStreamByte.readFully(fidbuf, 0, fidbuf.length);   // Store FileID
            String fileID = new String(fidbuf);                 // Conver FileID to String

            /* Loop receives data continously, until end flag is sent.
            End of the data is indicated by -1, handled by if condition within
            this loop, which breaks out of the loop, and marks reception from one client complete */
            while (receiveData) {
                totlen = inStreamByte.readInt();
                if (totlen < 0) {
                    receiveData = false;
                    totalClientReceipts += 1; // Mark that reception from this client is done
                    System.out.println("ending....");
                    break;
                } else {
                    byte[] byteData = new byte[totlen];
                    inStreamByte.readFully(byteData, 0, totlen); // Read message into a buffer
                    storeMsg(fileID, byteData); // Store message with proper identification
                    fullDataSizeBytes += totlen; // Add current message size to total data size
                    String dummy = new String(byteData);
                    if (debug == 1) {
                        System.out.println("received ==> " + dummy);
                        System.out.println("[" + thisSocket.getPort() + "]==>: "+dummy);
                    }
                }
            }
        } catch (IOException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
        }

        /* Wait till the other client completes */
        while (dataReady == false) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException except) {
                except.printStackTrace();
            }
        }
        
        sendData(thisSocket); // Send full data to the client through this method 
        clientsDone += 1;   // Mark that, all the actions regarding this client are done
        return; // Close thread
    }

    public void listen() {
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                (new Thread(new Server().assignSocket(clientSocket) )).start();
            }
            
        } catch (IOException except) {
            System.err.println("Accept failed !");
            except.printStackTrace();
        }
    }

    /* Stop listening for new client connections */
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Shut down: ERROR X !");
            e.printStackTrace();
        }
    }

    public void stitchMessages() {
        while (totalClientReceipts < 2) {   // Wait till both clients send their data
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException except) {
                except.printStackTrace();
            }
        }
        System.out.println("[Processing Data]");
        String fullKey;
        byte[] m;   // Variables to retrieve data pieces from Hash map
        int datit = 0;  // Iterator for iterating over full buffer: Data
        Data = new byte[fullDataSizeBytes]; // Create final buffer with total Data size
        for (String fileID: new String[]{"f1", "f2"}) {
            int totMessages = Integer.parseInt(msgCounter.get(fileID));   // Total messages of a client
            String halfKey = fileID;  // Make key out of fileID + # + msgID
            for (int i = 0; i < totMessages; i++) {
                fullKey = halfKey + "#" + i; // Make key out of fileID + # + msgID
                m = messageDict.get(fullKey);
                for (int k = 0; k < m.length; k++) {    // Add each byte to full buffer: Data
                    Data[datit] = m[k];
                    datit++;
                }
            }
        }
        int endByte = Data.length;
        for (int i = 0; i < Data.length; i++) {
            if (Data[i] == 0) {
                endByte = i;    // Identify index, where the null bytes begin
                break;
            }
        }
        dataReady = true;       // Used by client handling threads to begin sending f3 to clients
        System.out.println("[Processed Data]");
        File serverCopy = new File("dirThree/f3.txt");  // Write to file in Server directory
        try {   // If such file already exists, delete it
            if (serverCopy.delete()) {
                serverCopy.createNewFile(); // If deleted, create it
            }
            FileOutputStream writeOp = new FileOutputStream(serverCopy);
            writeOp.write(Data,0,endByte);
        } catch(IOException except) {
            except.printStackTrace();
        }
        return;
    }

    /* Send data after collating. Data is sent in 3 messages of 200 bytes
    each, to both the clients. */
    public void sendData(Socket thisSocket) {
        int port = thisSocket.getPort();
        System.out.println("\n====== SENDING DATA over " + port);
        try {
            DataInputStream inByte = new DataInputStream(thisSocket.getInputStream());
            DataOutputStream outChannel = new DataOutputStream(thisSocket.getOutputStream());
            int ack; 
            int cut = 0;
            int increment = Data.length / 3;
            for (int r = 0; r < 3; r++) {
                outChannel.writeInt(increment);
                ack = inByte.readInt();
                outChannel.write(Arrays.copyOfRange(Data,cut, cut+increment));
                ack = inByte.readInt();
                String dummy = new String(Arrays.copyOfRange(Data,cut, cut+increment));
                cut += increment;
            }
            outChannel.writeInt(-1);
        } catch (IOException except) {
            except.printStackTrace();
        }
        System.out.println("Data sent to " + port);
    }

    public static void main(String[] args) {
        if (args.length != 0) { /* If debug argument proved, change flag */
            if (args[0].equals("1")) {
                debug = 1;
            }
        }
        Server server=new Server();
        server.start(9038); /* Port: arbitrarily chosen based on my NetID */
        Thread serveThread = (new Thread(new Server() )); /* New thread to listen and accept connections */
        serveThread.start();
        server.stitchMessages(); /* Collate data */
        /* See if both clients are sent the final file (f3) by server */
        try {
            while (clientsDone < 2) {
                TimeUnit.SECONDS.sleep(2);
            }
            server.stopServer(); /* Close connection, after f3 sent */
        } catch (InterruptedException except) {
            except.printStackTrace();
        }
    }
}