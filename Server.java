import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.Files;

public class Server implements Runnable {
    
    private static ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    int runServer = 3;
    private static byte[] Data = new byte[1200];
    private static Hashtable<String, byte[]> messageDict = new Hashtable<String, byte[]>();
    private static Hashtable<String, String> msgCounter = new Hashtable<String, String>();
    private static ArrayList<String> portOrder = new ArrayList<String>();
    String msgID;
    int newID;
    private Socket thisSocket;
    private static int totalClientReceipts = 0;
    private static boolean dataReady = false;
    private static int clientsDone = 0;
    private static int debug = 0;

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: "+serverSocket+"\n");            
        } catch (IOException e) {
            System.err.println("Cannot create server");
            e.printStackTrace();
        }
    }

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

    public Server assignSocket(Socket tsock) {
        thisSocket = tsock;
        return this;
    }

    public void run() {
        
        while (thisSocket == null & serverSocket != null) {
            try {
                Socket clinSock = serverSocket.accept();
                portOrder.add(String.valueOf(clinSock.getPort()));
                (new Thread(new Server().assignSocket(clinSock))).start();
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
        if (debug == 1) {
            System.out.println("Current Thread ID: " + Thread.currentThread().getId());
        }
        try {
            inStr = new BufferedReader(new InputStreamReader(thisSocket.getInputStream()));
            DataInputStream inStreamByte = new DataInputStream(thisSocket.getInputStream());
            boolean receiveData = true;
            int totlen;
            byte[] fidbuf = new byte[2];
            inStreamByte.readFully(fidbuf, 0, fidbuf.length);
            String fileID = new String(fidbuf);
            // System.out.println("got FID " + fileID);


            while (receiveData) {
                totlen = inStreamByte.readInt();
                if (totlen < 0) {
                    receiveData = false;
                    totalClientReceipts += 1;
                    System.out.println("ending....");
                    break;
                } else {
                    byte[] byteData = new byte[totlen];
                    inStreamByte.readFully(byteData, 0, totlen);
                    storeMsg(fileID, byteData);
                    String dummy = new String(byteData);
                    if (debug == 1) {
                        System.out.println("reced ==> " + dummy);
                        System.out.println("[" + thisSocket.getPort() + "]==>: "+dummy);
                    }
                }
            }

            TimeUnit.SECONDS.sleep(1);
        } catch (IOException | InterruptedException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
        }

        while (dataReady == false) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException except) {
                except.printStackTrace();
            }
        }
        
        sendData(thisSocket);
        clientsDone += 1;
        return;
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

    public void stopServer() {
        try {
            // in.close();
            // out.close();
            // clientSocket.close();
            serverSocket.close();
            System.out.println("\nShut down: Success !");
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
        while (totalClientReceipts < 2) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException except) {
                except.printStackTrace();
            }
        }
        System.out.println("[Processing Data]");
        String fullKey;
        byte[] m;
        int datit = 0;
        for (String port: new String[]{"f1", "f2"}) {
            int totMessages = Integer.parseInt(msgCounter.get(port));
            String halfKey = port;
            for (int i = 0; i < totMessages; i++) {
                fullKey = halfKey + "#" + i;
                m = messageDict.get(fullKey);
                for (int k = 0; k < m.length; k++) {
                    Data[datit] = m[k];
                    datit++;
                }
            }
        }
        // String dummy = new String(Data);
        // System.out.println("stitched ==> " + dummy);
        int endByte = Data.length;
        for (int i = 0; i < Data.length; i++) {
            if (Data[i] == 0) {
                endByte = i;
                break;
            }
        }
        dataReady = true;
        System.out.println("[Processed Data]");
        File serverCopy = new File("dirThree/f3.txt");
        try {
            if (serverCopy.delete()) {
                serverCopy.createNewFile();
            }
            FileOutputStream writeOp = new FileOutputStream(serverCopy);
            writeOp.write(Data,0,endByte);
        } catch(IOException except) {
            except.printStackTrace();
        }
        return;
    }

    public void sendData(Socket thisSocket) {
        int port = thisSocket.getPort();
        System.out.println("\n====== SENDING DATA over " + port);
        try {
            DataInputStream inByte = new DataInputStream(thisSocket.getInputStream());
            DataOutputStream outChannel = new DataOutputStream(thisSocket.getOutputStream());
            outChannel.writeInt(1200);
            int ack = inByte.readInt();
            outChannel.flush();
            // int runc = 2;
            int cut = 0;
            for (int r = 0; r < 6; r++) {
                // System.out.println("\n" + port + " " + r);
                outChannel.writeInt(100);
                outChannel.flush();
                ack = inByte.readInt();
                outChannel.write(Arrays.copyOfRange(Data,cut, cut+100));
                outChannel.flush();
                ack = inByte.readInt();
                String dummy = new String(Arrays.copyOfRange(Data,cut, cut+100));
                cut += 100;
            }
            outChannel.writeInt(-1);
        } catch (IOException except) {
            except.printStackTrace();
        }
        // outChannel.println(Data);
        // outChannel.println("end");
        System.out.println("Data sent to " + port);
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            if (args[0].equals("1")) {
                debug = 1;
            }
        }
        Server server=new Server();
        server.start(9038);
        Thread serveThread = (new Thread(new Server() ));
        serveThread.start();
        server.stitchMessages();
        
        try {
            while (clientsDone < 2) {
                TimeUnit.SECONDS.sleep(2);
            }
            server.stopServer();
        } catch (InterruptedException except) {
            except.printStackTrace();
        }
    }
}