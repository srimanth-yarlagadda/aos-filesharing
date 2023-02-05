import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    int runServer = 2;
    private String Data = "";
    Hashtable<String, String> messageDict = new Hashtable<String, String>();
    Hashtable<String, String> msgCounter = new Hashtable<String, String>();
    String msgID;
    int newID;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: "+serverSocket+"\n");            
        } catch (IOException e) {
            System.err.println("Cannot create server");
            e.printStackTrace();
        }
    }

    public void storeMsg(String port, String message) {
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

    public void listen() {
        try {
            while (runServer != 0) {
                    clientSocket = serverSocket.accept(); 
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    for (String recMessage=in.readLine(); recMessage!=null; recMessage=in.readLine()) {
                        if ("end".equals(recMessage)) {
                            runServer -= 1;
                        } else {
                        storeMsg(""+clientSocket.getPort(), recMessage);
                        System.out.println("Received on " + clientSocket.getPort() + ": "+recMessage);
                        }
                    }
                    recMessage = in.readLine();
                }
            System.out.println("\n\n\n");
            System.out.println(msgCounter);
            System.out.println("\n\n\n");
            System.out.println(messageDict);
        } catch (IOException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
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
        String fullKey,m;
        for (String port: msgCounter.keySet()) {
            int totMessages = Integer.parseInt(msgCounter.get(port));
            String halfKey = port;
            for (int i = 0; i < totMessages; i++) {
                fullKey = halfKey + i;
                m = messageDict.get(fullKey);
                Data = Data + m;
            }
        }

    }

    public static void main(String[] args) {
        Server server=new Server();
        server.start(9038);
        server.listen();
        System.out.println("Shutting down server and socket !");
        server.stop();
        System.out.println("Total data received:");
        server.stitchMessages();
        server.print();
    }
}