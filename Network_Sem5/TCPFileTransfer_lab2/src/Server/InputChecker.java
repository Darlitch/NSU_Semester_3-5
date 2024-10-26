package Server;

import Exceptions.InputException;

public class InputChecker {
    private InputChecker() {};
    public static void check(String[] args) {
        if (args.length != 1) {
            throw new InputException("Port number not specified");
        }
        portCheck(args[0]);
    }

    public static void portCheck(String port) {
        if (port.length() != 4 && port.length() != 5) {
            throw new InputException("Invalid port number");
        }
        String regex = "\\d+";
        if (!port.matches(regex)) {
            throw new InputException("Invalid port number");
        }
    }
}
