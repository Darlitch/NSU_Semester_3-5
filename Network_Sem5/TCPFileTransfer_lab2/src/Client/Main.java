package Client;

public class Main {
    public static void main(String[] args) {
        FTClient ftclient = new FTClient(args);
        ftclient.fileTransfer();
    }
}
