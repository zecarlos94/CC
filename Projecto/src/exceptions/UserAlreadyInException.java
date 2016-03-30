package Exceptions;

public class UserAlreadyInException extends Exception {
     public UserAlreadyInException () {
        super();
    }
    public UserAlreadyInException (String message) {
        super(message);
    }
}
