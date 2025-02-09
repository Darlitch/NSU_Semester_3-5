import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class MessageHandler {
    public static Datagram handleMessage(Datagram datagram) throws IOException {
        Datagram recivedDatagram = new Datagram();
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        datagram.socket.receive(packet);
        recivedDatagram.addr = packet.getAddress();
        recivedDatagram.port = packet.getPort();
        recivedDatagram.msg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        return recivedDatagram;
    }

    public static void SendMessage(Datagram datagram) throws IOException {
        DatagramPacket packet = new DatagramPacket(datagram.msg, datagram.msg.length, datagram.addr, datagram.port);
        datagram.socket.send(packet);
    }
}
