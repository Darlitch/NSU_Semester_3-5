package Server;

import Exceptions.InputException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

public class InputChecker {
    private InputChecker() {};
    public static void check(String[] args) {
        if (args.length != 1) {
            throw new InputException("Port number not specified");
        }
        portCheck(args[0]);
    }

    private static void portCheck(String port) {
        if (port.length() != 4 && port.length() != 5) {
            throw new InputException("Invalid port number");
        }
        String regex = "\\d+";
        if (!port.matches(regex)) {
            throw new InputException("Invalid port number");
        }
    }

    public static void filepathCheck(String filepath) {
        if (filepath.length() > 4096) {
            throw new InputException("The file name length must not exceed 4096 bytes");
        }
        byte[] bytes = filepath.getBytes();
        Charset charset = StandardCharsets.UTF_8;
        CharsetDecoder decoder = charset.newDecoder();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = CharBuffer.allocate(bytes.length);
        CoderResult result = decoder.decode(byteBuffer, charBuffer, true);
        if (result.isMalformed()) {
            throw new InputException("The file path must match the encoding \"UTF-8\"");
        }
//        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes));
//        if (charBuffer.remaining() != 0) {
//            throw new InputException("The file path must match the encoding \"UTF-8\"");
//        }
    }
}
