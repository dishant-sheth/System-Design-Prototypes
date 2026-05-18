package src.exceptions;

public class InsufficientHoldingsException extends RuntimeException {
    public InsufficientHoldingsException(){
        super("User does not have sufficient holdings to execute this order");
    }
}