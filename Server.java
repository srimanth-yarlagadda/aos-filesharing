import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    int runServer = 3;
    private String Data = "";
    Hashtable<String, String> messageDict = new Hashtable<String, String>();
    Hashtable<String, String> msgCounter = new Hashtable<String, String>();
    String msgID;
    int newID;
    private Socket thisSocket;

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

    public Server assignSocket(Socket tsock) {
        thisSocket = tsock;
        return this;
    }

    // @override
    public void run() {
        
        // while (true) {
        //     clientSocket = serverSocket.accept();
        //     (new Thread(new Server().assignSocket(clientSocket) )).start();
        // }


        BufferedReader inStr;
        System.out.println("\n\n\nListening...."+thisSocket);
        System.out.println("Current Thread ID: " + Thread.currentThread().getId());

        try {
        inStr = new BufferedReader(new InputStreamReader(thisSocket.getInputStream()));
        // out = new PrintWriter(clientSocket.getOutputStream(), true);
        for (String rec=inStr.readLine(); rec!=null; rec=inStr.readLine()) {
            if ("end".equals(rec)) {
                // runServer -= 1;
                System.out.println("ending....");
                // break;
            } else {
            storeMsg(""+thisSocket.getPort(), rec);
            System.out.println("Received on " + thisSocket.getPort() + ": "+rec);
            TimeUnit.SECONDS.sleep(7);
            System.out.println("\n=========\n");
            }
        } 
        } catch (IOException | InterruptedException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
            // inter.printStackTrace();
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

    public void sendData() {
        out.println(Data);
        System.out.println("Responded!");
    }

    public static void main(String[] args) {
        Server server=new Server();
        // // (new Thread(server.star() )).start();
        // // new Thread(() -> star()).start();

        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        //     star();
        // }
        // }).start();
        server.start(9038);
        server.listen();
        // System.out.println("Shutting down server and socket !");
        // server.stop();
        System.out.println("Total data received:");
        server.stitchMessages();
        server.print();
        server.sendData();
    }
}