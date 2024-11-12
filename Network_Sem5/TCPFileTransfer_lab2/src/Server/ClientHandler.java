package Server;

import Exceptions.FileTransferException;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ClientHandler {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private ClientHandler() {}

    public static Runnable clientHandle(Socket clientSocket, String uploadDir, int timeout) {
        logger.info("Client connected: " +clientSocket.getRemoteSocketAddress());
        return () -> {
            try(DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
                getFile(in, out, uploadDir, timeout);

            } catch (IOException e) {
                throw new FileTransferException("Failed to receive file",e);
            }
        };
    }

    private static void getFile(DataInputStream in, DataOutputStream out, String uploadDir, int timeout) {
        String filename = getFilename(in);
        long fileSize = getFileSize(in);
        File file = new File(uploadDir, filename);
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            getFileData(in, fileOut, fileSize, timeout, filename);
        } catch (IOException e) {
            throw new FileTransferException("Failed to create output file");
        }
        sendResponse(out, filename, fileSize, fileSize);
    }

    private static void sendResponse(DataOutputStream out, String filename, long expectedSize, long receivedSize) {
            try {
                if (expectedSize == receivedSize) {
                    out.writeUTF("SUCCESS");
                    logger.info(filename + " is recevied!");
                } else {
                    out.writeUTF("FAILURE");
                    logger.info("Failed to received " + filename + "!");
                }
            } catch (IOException e) {
                throw new FileTransferException("Failes to send response");
            }
    }

    private static void getFileData(DataInputStream in, FileOutputStream out, long fileSize, int timeout, String filename) {
        byte[] buffer = new byte[8192];
        long receivedBytes = 0;
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        AtomicLong lastBytes = new AtomicLong(0);

        while(receivedBytes < fileSize) {
            try {
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
                receivedBytes += bytesRead;
                lastBytes.addAndGet(bytesRead);
                long time = System.currentTimeMillis();
                if (time - lastTime >= timeout) {
                    printSpeed(startTime, time, lastTime, receivedBytes, lastBytes, filename);
                    lastTime = time;
                    lastBytes.set(0);
                }

            } catch (IOException e) {
                throw new FileTransferException("Failed to receive data file");
            }
        }
        printSpeed(startTime, System.currentTimeMillis(), lastTime, receivedBytes, lastBytes, filename);
    }

    private static void printSpeed(long startTime, long time, long lastTime, long receivedBytes, AtomicLong lastBytes, String filename) {
        double instantSpeed = (lastBytes.get() / ((time - lastTime) / 1000.0)) / (1024 * 1024);
        double averageSpeed = (receivedBytes / ((time - startTime) / 1000.0)) / (1024 * 1024);
        System.out.printf("%s: Instant speed: %.2f MB/sec, Average speed: %.2f MB/sec\n", filename, instantSpeed, averageSpeed);
    }

    private static long getFileSize(DataInputStream in) {
        try {
            return in.readLong();
        } catch (IOException e) {
            throw new FileTransferException("Failed to receive file size");
        }
    }
    private static String getFilename(DataInputStream in) {
        try {
            String filename = Path.of(in.readUTF()).getFileName().toString();
            InputChecker.filepathCheck(filename);
            return filename;
        } catch (IOException e) {
            throw new FileTransferException("Failed to receive filename");
        }
    }


}
