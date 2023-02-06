import java.net.*;
import java.io.*;

public class clientOne {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    String recMessage;

    public void sendData(String filename) {
        try {
            BufferedReader inputData = new BufferedReader(new FileReader(filename));
            String dataLine;
            while ((dataLine = inputData.readLine()) != null)   {
                out.println (dataLine);
            }
            out.println("end");
            System.out.println("Data sent successfully!");
        } catch (IOException except) {
            System.err.println("Error while sending data!");
            except.printStackTrace();
        }
    }

    public void receive() {
        try {
            while (true) {
                for (String recMessage=in.readLine(); recMessage!=null; recMessage=in.readLine()) {
                    System.out.println("Client received on " + clientSocket.getPort() + ": "+recMessage);
                }
                recMessage = in.readLine();
            }
        } catch (IOException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
        }
    }


    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Connection Successful: " + clientSocket);
        } catch (IOException except) {
            System.err.println("Connection failed!");
            except.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // System.out.println(args[0]);
        String ipaddress = "dc03.utdallas.edu";
        int port = 9038;
        clientOne connect =new clientOne();
        connect.startConnection(ipaddress, port);
        String filename = "oneData.txt";
        if (args.length != 0) {
            if (args[0].equals("2")) {
                filename = "twoData.txt";
            }
        }
        connect.sendData(filename);
        connect.receive();
    }
}