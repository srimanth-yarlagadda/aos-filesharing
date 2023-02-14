import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class client {
    private Socket serverConnection;
    DataOutputStream outStreamByte;
    DataInputStream inByte;
    String recMessage;
    private static String filename, outputFilename;
    byte[] recFullFile;
    List<byte[]> partialFile = new ArrayList<byte[]>();
    private static int debug = 0;
    private static String fid = "f1";   // Set a default fileID

    /* Read the input directory of the client and send data to the Server */
    public void sendData() {
        try {
            BufferedReader inputData = new BufferedReader(new FileReader(filename));
            String dataLine;
            // Read the file from clients own directory, to be sent to server
            File file = new File(filename);
            byte[] fileData = Files.readAllBytes(file.toPath());

            // Inform server, ID of the file i.e., f1 or f2
            outStreamByte.write(fid.getBytes());
            int cutIncrement = fileData.length / 3; // dividing into three pieces
            int cut = 0;
            for (int i = 0; i < 3; i ++) {
                outStreamByte.writeInt(cutIncrement); // Inform server: data size to be sent
                outStreamByte.write( Arrays.copyOfRange(fileData,cut, cut+cutIncrement) ); 
                cut += cutIncrement;
            }
            outStreamByte.writeInt(-1); // Signal the end of send operation
            System.out.println("Data sent successfully!");
        } catch (IOException except) {
            System.err.println("Error while sending data!");
            except.printStackTrace();
        }
    }

    /* Receive method, receive messages sent by Server, collate and save them in a local directory */
    public void receive() {
        try {
            // Declare variables
            int fullLength;
            fullLength = 0;
            int byteInd = 0;
            int increment = 0;
            boolean runBool = true;

            /* Loop while, get data from server. Continues till -1 is received indicating end.
            -2 & -3 are used to send acknowledgement to Server. Receive size of the message first,
            create a buffer of that size, get the data and store it.*/
            while (runBool) {
                increment = inByte.readInt();   // Get message size
                outStreamByte.writeInt(-2);
                if (debug == 1) {
                    System.out.println("Increment==> " + increment + " byteind " + byteInd);
                }
                if (increment == -1) {
                    runBool = false;
                    break;
                } else {
                    byte[] tempBuff = new byte[increment];  // Create buffer of message size
                    fullLength += increment; // Update the total size of the file (create buffer later).
                    inByte.readFully(tempBuff, 0, tempBuff.length); // Read message
                    partialFile.add(tempBuff); // Save message
                    outStreamByte.writeInt(-3); // Send acknowledgement
                    if (debug == 1) {
                        String dummy = new String(Arrays.copyOfRange(tempBuff,0, tempBuff.length));
                        System.out.println("\nReceived ==> " + dummy + "]\n");
                    }
                }
            }
            // System.out.println("\n\nOut of loop");
            recFullFile = new byte[fullLength]; // Buffer of full file size
            int buffIt = 0; // Iterator
            for (byte[] piece: partialFile) { // Put all messages into single buffer
                for (int k = 0; k < piece.length; k++) {
                    recFullFile[buffIt] = piece[k];
                    buffIt++;
                }
            }
            if (debug == 1) {
                String dummy = new String(Arrays.copyOfRange(recFullFile,0, recFullFile.length));
                System.out.println("\n\n OUT ==>> " + dummy);
            }
            // Create and write a new file containing full data, f3 - clients copy
            File clientFullFile = new File(outputFilename);
            if (clientFullFile.delete()) {  // Delete if file already exists
                clientFullFile.createNewFile();
            }
            FileOutputStream writeOp = new FileOutputStream(clientFullFile); // Write operation
            writeOp.write(recFullFile,0,recFullFile.length);
        } catch (IOException except) {
            System.err.println("Cannot listen on given port.");
            except.printStackTrace();
        }
    }

    // Establish connection with Server & create input & output stream objects
    public void startConnection(String serverIP, int port) {
        try {
            serverConnection = new Socket(serverIP, port);
            outStreamByte = new DataOutputStream(serverConnection.getOutputStream());
            inByte = new DataInputStream(serverConnection.getInputStream());
            System.out.println("Connection Successful: " + serverConnection);
        } catch (IOException except) {
            System.err.println("Connection failed!");
            except.printStackTrace();
        }
    }

    public static void main(String[] args) {
        /* IP address and Port declarations. Start connection */
        String ipaddress = "dc03.utdallas.edu";
        int port = 9038;
        client connect =new client();
        connect.startConnection(ipaddress, port);

        /* Default is set to directory One (i.e., client one). If any 
        arguments given while invoking client, ID is changed accordingly*/
        filename = "dirOne/f1.txt";
        outputFilename = "dirOne/f3.txt";
        if (args.length != 0) {
            if (args[0].equals("2")) {
                filename = "dirTwo/f2.txt";
                outputFilename = "dirTwo/f3.txt";
                fid = "f2";
            }
            if (args.length == 2) {
                if (args[1].equals("1")) {
                    debug = 1;
                }
            }
        }
        connect.sendData();
        connect.receive();
        System.out.println("\n***END\n");

    }
}