import java.net.*;
import java.io.*;

public class clientOne {
    private Socket clientSocket;
    private PrintWriter out;
    // private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            System.out.println(clientSocket);
            System.out.println("Connection Successful !");
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("Hello Server !\nHow are you doing today?\nGood to talk to you\nBye!");
            System.out.println("Message Sent!");
            // while (true) {
                out.println("end");
                System.out.println("Shutdown message sent!");
            // }


            // while (true){
            //     clientSocket = serverSocket.accept(); 
            //     // new TCPServerThread(clientSocket).start();
            // }
        // serverSocket.close();
        } catch (IOException e) {
            System.err.println("Cannot connect!");
            e.printStackTrace();
        }


    }


    public static void main(String[] args) {
        String ipaddress = "dc03.utdallas.edu";
        int port = 9038;
        clientOne connect =new clientOne();
        connect.startConnection(ipaddress, port);
    }


}