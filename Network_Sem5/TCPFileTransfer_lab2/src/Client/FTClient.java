package Client;

import Exceptions.FileTransferException;

import java.io.*;
import java.net.Socket;

public class FTClient {
    private final String filepath;
    private final int port;
    private final String serverAddr;
    public FTClient(String[] args) {
        InputChecker.check(args);
        filepath = args[0];
        serverAddr = args[1];
        port = Integer.parseInt(args[2]);
    }
    public void fileTransfer() {
        try (Socket socket = new Socket(serverAddr, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())){
            File file = new File(filepath);
            sendFileInfo(file, out);
            sendFileData(file, out);
            getServerResponse(in);
        } catch (IOException e) {
            throw new FileTransferException("Failed to send file", e);
        }
    }


    private void sendFileInfo(File file, DataOutputStream out) {
        try {
            out.writeUTF(file.getName());
            out.writeLong(file.length());
        } catch (IOException e) {
            throw new FileTransferException("Failed to send file info");
        }
    }

    private void sendFileData(File file, DataOutputStream out) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[9192];
            int bytesRead;
            while((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new FileTransferException("File not found", e);
        }
    }

    private void getServerResponse(DataInputStream in) {
        try {
            String serverResponse = in.readUTF();
            if (serverResponse.equals("SUCCESS")) {
                System.out.println("File transfer completed successfully");
            } else {
                System.out.println("File transfer failed");
            }
        } catch (IOException e) {
            throw new FileTransferException("Failed to get server response");
        }
    }
}
