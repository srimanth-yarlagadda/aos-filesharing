import java.net.*;
import java.io.*;

public class Server {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    int runServer = 2;
    private String Data = "";

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: "+serverSocket+"\n");            
        } catch (IOException e) {
            System.err.println("Cannot create server");
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (runServer != 0) {
                    clientSocket = serverSocket.accept(); 
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    for (String recMessage=in.readLine(); recMessage!=null; recMessage=in.readLine()) {
                        Data = Data + recMessage;
                        System.out.println("Received on " + clientSocket.getPort() + ": "+recMessage);
                        if ("end".equals(recMessage)) {
                            runServer -= 1;
                        }
                    }
                    recMessage = in.readLine();
                }
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
        System.out.println(Data);
    }

    public static void main(String[] args) {
        Server server=new Server();
        server.start(9038);
        server.listen();
        System.out.println("Shutting down server and socket !");
        server.stop();
        System.out.println("Total data received:");
        server.print();
    }
}