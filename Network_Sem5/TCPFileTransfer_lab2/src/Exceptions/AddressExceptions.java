package Exceptions;

import java.io.IOException;
import java.net.UnknownHostException;

public class AddressExceptions extends RuntimeException {
        public AddressExceptions(String message) {
            super(message);
        }


}
