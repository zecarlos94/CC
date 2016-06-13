package Exceptions;

public class UserAlreadyRegisteredException extends Exception {
     public UserAlreadyRegisteredException () {
        super();
    }
    public UserAlreadyRegisteredException (String message) {
        super(message);
    }
}
