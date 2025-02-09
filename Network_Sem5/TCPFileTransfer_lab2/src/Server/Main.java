package Server;

import Client.FTClient;

public class Main {
    public static void main(String[] args) {
        FTServer ftServer = new FTServer(args);
        ftServer.start();
    }
}
