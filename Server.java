import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {
    
    private static ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    int runServer = 3;
    private static String Data = "";
    private static Hashtable<String, byte[]> messageDict = new Hashtable<String, byte[]>();
    private static Hashtable<String, String> msgCounter = new Hashtable<String, String>();
    String msgID;
    int newID;
    private Socket thisSocket;
    private static int totalClientReceipts = 0;
    private static boolean dataReady = false;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: "+serverSocket+"\n");            
        } catch (IOException e) {
            System.err.println("Cannot create server");
            e.printStackTrace();
        }
    }

    public void storeMsg(String port, byte[] message) {
        if (msgCounter.containsKey(""+port)) {
            msgID = msgCounter.get(""+port);
        } else {
            msgID = "0";
        }
        newID = Integer.parseInt(msgID);
        newID += 1;
        msgCounter.put(""+port, ""+newID);
        messageDict.put(""+port+msgID, message);
    }

    public Server assignSocket(Socket tsock) {
        thisSocket = tsock;
        return this;
    }

    // @override
    public void run() {
        
        while (thisSocket == null) {
            try {
                System.out.println("\nListening from thread on: "+serverSocket);
                Socket clinSock = serverSocket.accept();
                (new Thread(new Server().assignSocket(clinSock))).start();
                // System.out.println("\n\n\nNo longer listening from thread !\n\n\n");
            } catch (IOException except) {
                except.printStackTrace();
            }
        }

        BufferedReader inStr;
        System.out.println("\n\n\nListening...."+thisSocket);
        System.out.println("Current Thread ID: " + Thread.currentThread().getId());

        try {
            inStr = new BufferedReader(new InputStreamReader(thisSocket.getInputStream()));
            DataInputStream inStreamByte = new DataInputStream(thisSocket.getInputStream());
            boolean receiveData = true;
            


            // byte[] message = new byte[1];
            // inStreamByte.readFully(message, 0, 1);
            // // char[] charset = message;
            // System.out.println("Got byte: " + (char)message[0]);

            
            // inStreamByte.readFully(message, 0, 1);
            // char[] charset = message;
            int totlen = inStreamByte.readInt();
            System.out.println("Got byte: " + totlen);
            

            while (receiveData) {
                totlen = inStreamByte.readInt();
                System.out.println("Got byte inside: " + totlen);
                if (totlen < 0) {
                    receiveData = false;
                    break;
                } else {
                    byte[] byteData = new byte[totlen];
                    inStreamByte.readFully(byteData, 0, totlen);
                    storeMsg(""+thisSocket.getPort(), byteData);
                    String dummy = new String(byteData);
                    System.out.println("reced ==> " + dummy);
                }
            }
            
            
            for (String rec=inStr.readLine(); rec!="end"; rec=inStr.readLine()) {
                if ("end".equals(rec)) {
                    // runServer -= 1;
                    totalClientReceipts += 1;
                    System.out.println("ending....");
                    break;
                } else {
                // storeMsg(""+thisSocket.getPort(), rec);
                System.out.println("[" + thisSocket.getPort() + "]: "+rec);
                TimeUnit.SECONDS.sleep(1);
                }            
            } 
        } catch (IOException | InterruptedException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
            // inter.printStackTrace();
        }

        try {
            while (dataReady == false) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException except) {
                    except.printStackTrace();
                }
            }
            // System.out.println("Transmitting to " + thisSocket.getPort());
            PrintWriter outChannel = new PrintWriter(thisSocket.getOutputStream(), true);
            sendData(outChannel, thisSocket.getPort());
            // System.out.println("!! Transmitted to " + thisSocket.getPort());
        } catch (IOException  except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
        }

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
        //     // inter.printStackTrace();
        }
    }

    public void stop() {
        try {
            in.close();
            // out.close();
            clientSocket.close();
            serverSocket.close();
            System.out.println("Shut down: Success !");
        } catch (IOException e) {
            System.err.println("Shut down: ERROR X !");
            e.printStackTrace();
        }
    }

    public void print() {
        System.out.println("\n\n#################\n");
        System.out.println(Data);
        System.out.println("\n#################\n\n");
    }

    public void stitchMessages() {
        System.out.println("[Stitch] Current Thread ID: " + Thread.currentThread().getId());
        while (totalClientReceipts < 2) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException except) {
                except.printStackTrace();
            }
        }
        System.out.println("[Stitching]");
        String fullKey;
        byte[] m;
        for (String port: msgCounter.keySet()) {
            int totMessages = Integer.parseInt(msgCounter.get(port));
            String halfKey = port;
            for (int i = 0; i < totMessages; i++) {
                fullKey = halfKey + i;
                m = messageDict.get(fullKey);
                Data = Data + m;
            }
        }
        dataReady = true;
        System.out.println("[Stitching Done]");
    }

    public void sendData(PrintWriter outChannel, int port) {
        System.out.println("\n====== SENDING DATA over " + port);
        outChannel.println(Data);
        outChannel.println("end");
        System.out.println("Responded to " + port);
    }

    public static void main(String[] args) {
        Server server=new Server();
        server.start(9038);
        (new Thread(new Server() )).start();
        System.out.println("Total data received:");
        server.stitchMessages();
        // server.print();
        // server.sendData();
    }
}