package Client;

import Exceptions.AddressExceptions;
import Exceptions.FileTransferException;
import Exceptions.InputException;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class FTClient {
    String filepath;
    int port;
    String serverAddr;
    public FTClient(String[] args) throws AddressExceptions {
        InputChecker.check(args);
        filepath = args[0];
        serverAddr = args[1];
        port = Integer.parseInt(args[2]);
    }

    public void fileTransfer() {
        try {
            Socket socket = new Socket(serverAddr, port);
            File file = new File(filepath);
        } catch (IOException e) {
            throw new FileTransferException("Failed to send file", e);
        }
    }
}
