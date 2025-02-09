package Client;

import Exceptions.InputException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class InputChecker {

    private InputChecker() {}
    public static void check(String[] args) {
        if (args.length != 3) {
            throw new InputException("Need 3 parametrs: filepath, server-address, port");
        }
        String filename = Path.of(args[0]).getFileName().toString();
        filepathCheck(filename);
        addrCheck(args[1]);
        portCheck(args[2]);
    }

    private static void portCheck(String port) {
        int portInt = Integer.parseInt(port);
        if ((portInt < 1) || (portInt > 65536)) {
            throw new InputException("Invalid port number");
        }
    }

    private static void filepathCheck(String filename) {
        try {

            byte[] bytes = filename.getBytes("UTF-8");
            if (bytes.length > 4096) {
                throw new InputException("The file name length must not exceed 4096 bytes");
            }
        } catch (UnsupportedEncodingException e) {
            throw new InputException("Incorrect encoding");
        }
    }

    private static void addrCheck(String addrStr) {
        try {
            InetAddress addr = InetAddress.getByName(addrStr);
        } catch (UnknownHostException e) {
            throw new InputException("IP address/hostname is incorrect");
        }
    }


}
