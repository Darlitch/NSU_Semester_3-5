import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class InputResolver {
    public static String resolve(String[] args) throws UnknownHostException {
        String ipVer = addrCheck(args[0]);
        portCheck(args[1]);
        return ipVer;
    }

    private static String addrCheck(String addrStr) throws UnknownHostException {
        try {
            InetAddress addr = InetAddress.getByName(addrStr);
            if (addr instanceof Inet4Address && addr.isMulticastAddress()) {
                return  "IPv4";
            } else if (addr instanceof Inet6Address && addr.isMulticastAddress()) {
                return "IPv6";
            } else {
                throw new UnknownHostException();
            }
        } catch (UnknownHostException ex) {
            throw new UnknownHostException();
        }
    }

    private static void portCheck(String port) throws UnknownHostException {
        if (port.length() != 4 && port.length() != 5) {
            throw new UnknownHostException();
        }
    }



}
