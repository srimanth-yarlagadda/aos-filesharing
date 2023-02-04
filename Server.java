import java.net.*;
import java.io.*;


public class Server {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;
    boolean listening = true;

    public void start(int port) {
        System.out.println("Start member called");
        try {
            serverSocket = new ServerSocket(port);
            while (listening){
                clientSocket = serverSocket.accept(); 
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // String greeting = in.readLine();
                for (String recMessage=in.readLine(); recMessage!=null; recMessage=in.readLine()) {
                    System.out.println("Received on " + clientSocket.getPort() + ": "+recMessage);
                    if ("end".equals(recMessage)) {
                        listening = false;
                    }
                }
                recMessage = in.readLine();
            }
        // serverSocket.close();
        } catch (IOException e) {
            System.err.println("Cannot listen on given port.");
            e.printStackTrace();
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

    public static void main(String[] args) {
        Server server=new Server();
        server.start(9038);
        System.out.println("Shutting down server and socket !");
        server.stop();
    }
}