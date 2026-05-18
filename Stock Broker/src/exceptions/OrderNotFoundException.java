package src.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(final String orderId){
        super("Did not find order " + orderId);
    }
}