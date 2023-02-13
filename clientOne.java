import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class clientOne {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    DataOutputStream outStreamByte;
    DataInputStream inByte;
    String recMessage;
    private static String filename, outputFilename;
    byte[] recFullFile;
    List<byte[]> partialFile = new ArrayList<byte[]>();

    public void sendData() {
        try {
            BufferedReader inputData = new BufferedReader(new FileReader(filename));
            String dataLine;
            
            File file = new File(filename);
            byte[] fileData = Files.readAllBytes(file.toPath());

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
            int fullLength = inByte.readInt();
            outStreamByte.writeInt(-2);
            System.out.println("Buffer created with " + fullLength);
            fullLength = 0;
            // recFullFile = new byte[fullLength];
            
            int byteInd = 0;
            int ct = 0;
            int increment = 0;
            boolean runboool = true;
            while (runboool) {
                increment = inByte.readInt();
                outStreamByte.writeInt(-2);
                System.out.println(ct + " Incre===> " + increment + " byteind " + byteInd);
                ct += 1;
                if (increment == -1) {
                    runboool = false;
                    System.out.println("\n***END\n");
                    break;
                } else {
                    byte[] tempBuff = new byte[increment];
                    fullLength += increment;
                    inByte.readFully(tempBuff, 0, tempBuff.length);
                    partialFile.add(tempBuff);
                    System.out.println("buffered\n");
                    outStreamByte.writeInt(-3);
                    String dummy = new String(Arrays.copyOfRange(tempBuff,0, tempBuff.length));
                    System.out.println("\nreced ==> " + dummy + "]\n");
                    // byteInd += increment;
                }
            }
            System.out.println("\n\nOut of loop");
            recFullFile = new byte[fullLength];
            int buffIt = 0;
            for (byte[] piece: partialFile) {
                for (int k = 0; k < piece.length; k++) {
                    recFullFile[buffIt] = piece[k];
                    buffIt++;
                }
            }
            String dummy = new String(Arrays.copyOfRange(recFullFile,0, recFullFile.length));
            System.out.println("\n\n OUT ==>> " + dummy);
            File clientFullFile = new File(outputFilename);
            if (clientFullFile.delete()) {
                clientFullFile.createNewFile();
            }
            FileOutputStream writeOp = new FileOutputStream(clientFullFile);
            writeOp.write(recFullFile,0,recFullFile.length);
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
            inByte = new DataInputStream(clientSocket.getInputStream());
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
        filename = "dirOne/oneData.txt";
        outputFilename = "dirOne/fullData.txt";
        if (args.length != 0) {
            if (args[0].equals("2")) {
                filename = "dirTwo/twoData.txt";
                outputFilename = "dirTwo/fullData.txt";
            }
        }
        connect.sendData();
        connect.receive();
        System.out.println("END");

    }
}