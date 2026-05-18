package src.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId){
        super(userId + " user does not exist");
    }
}