package src.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String userId){
        super(userId + " user already exists.");
    }
}