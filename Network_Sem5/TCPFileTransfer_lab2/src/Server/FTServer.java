package Server;

import Exceptions.FileTransferException;

import java.io.IOException;
import java.net.Socket;

public class FTServer {

    public FTServer(String[] args) {
        InputChecker.check(args);
    }
}
