package Exceptions;

public class AuthenticationErrorException extends Exception {

    public AuthenticationErrorException() {
        super();
    }

    public AuthenticationErrorException(String message) {
        super(message);
    }
}
