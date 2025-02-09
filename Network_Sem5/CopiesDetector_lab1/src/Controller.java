import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Controller {
    private Datagram datagram;
    private Observer observer;
    public Controller(String[] args) throws IOException {
        datagram = new Datagram();
        setupDatagram(datagram, args, InputResolver.resolve(args));
        observer = new Observer();
    }

    private void setupDatagram(Datagram datagram, String[] args, String ipVer) throws IOException {
        datagram.addr = InetAddress.getByName(args[0]);
        datagram.ipVer = ipVer;
        datagram.port = Integer.parseInt(args[1]);
        datagram.msg = args[2].getBytes();
        datagram.socket = new MulticastSocket(datagram.port);
    }
    public void start() throws IOException, InterruptedException {
//        datagram.socket.joinGroup(datagram.addr); // через нетворк интерфейс сделать
//        datagram.socket.setNetworkInterface(?); // через нетворк интерфейс сделать
        InetAddress networkAddr = InetAddress.getByName("26.253.65.210");
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(networkAddr);
        datagram.socket.setNetworkInterface(networkInterface);
//        if (networkInterface != null) {
//        } else {
//            System.err.println("Network interface not found.");
//        }
        datagram.socket.joinGroup(new InetSocketAddress(datagram.addr, datagram.port), networkInterface);

        Sender sender = new Sender();
        Listener listener = new Listener();
        Updater updater = new Updater();

        Thread senderThread = new Thread(sender);
        Thread listenerThread = new Thread(listener);
        Thread updaterThread = new Thread(updater);

        senderThread.start();
        listenerThread.start();
        updaterThread.start();

        senderThread.join();
        listenerThread.join();
        updaterThread.join();

        datagram.socket.leaveGroup(new InetSocketAddress(datagram.addr, datagram.port), networkInterface);
        datagram.socket.close();
//        datagram.socket.leaveGroup(datagram.addr);
//        datagram.socket.close();
    }

    class Sender implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    MessageHandler.SendMessage(datagram);
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class Listener implements Runnable {
        @Override
        public void run() {
            while(true) {
                try {
                    Datagram recivedDatagram = MessageHandler.handleMessage(datagram);
                    observer.updateLiveCopies(recivedDatagram);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class Updater implements Runnable {
        @Override
        public void run() {
            while(true) {
                observer.removeDeadCopies();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
