package src.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(){
        super("User does not have sufficient funds to execute this order");
    }
}