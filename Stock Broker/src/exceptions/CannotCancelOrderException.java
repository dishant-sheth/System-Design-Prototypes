package src.exceptions;

public class CannotCancelOrderException extends RuntimeException {
    public CannotCancelOrderException(){
        super("Cannot cancel order in current state");
    }
}