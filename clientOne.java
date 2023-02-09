import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class clientOne {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    DataOutputStream outStreamByte;
    String recMessage;

    public void sendData(String filename) {
        try {
            outStreamByte.writeInt(1);
            BufferedReader inputData = new BufferedReader(new FileReader(filename));
            String dataLine;
            
            // Path filepath = filename;
            File file = new File(filename);
            byte[] fileData = Files.readAllBytes(file.toPath());
            // System.out.println("PrintData: "+Arrays.toString(fileData));

            
            // while ((dataLine = inputData.readLine()) != null)   {
            //     out.println (dataLine);
            // }
            int cut = 0;
            for (int i = 0; i < 3; i ++) {
                outStreamByte.writeInt(100);
                outStreamByte.write( Arrays.copyOfRange(fileData,cut, cut+100) ); 
                cut += 100;
            }

            outStreamByte.writeInt(-1);
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
                    if ("end".equals(recMessage)) {
                        break;
                    }
                }
                break;
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
            outStreamByte = new DataOutputStream(clientSocket.getOutputStream());
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
        System.out.println("END");

    }
}