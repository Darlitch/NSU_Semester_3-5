package Server;

import Exceptions.ConnectionException;
import Exceptions.FileTransferException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTServer {
    private final int timeout = 3000;
    private final String upload_dir = "R:\\SecondCourse\\Network_Sem5\\TCPFileTransfer_lab2\\src\\filesForTest\\upload";
    private ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public FTServer(String[] args) {
        InputChecker.check(args);
        startServerSocket(Integer.parseInt(args[0]));
        threadPool = Executors.newCachedThreadPool();
    }

    private void startServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new ConnectionException("Failed to start server socket", e);
        }
    }

    public void start() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(ClientHandler.clientHandle(clientSocket, upload_dir, timeout));
            } catch (IOException e) {
                throw new ConnectionException("Failed to accept connection", e);
            }
        }
    }
}
