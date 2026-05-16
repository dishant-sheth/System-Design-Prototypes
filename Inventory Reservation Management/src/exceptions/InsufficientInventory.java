package src.exceptions;

public class InsufficientInventory extends RuntimeException {
    public InsufficientInventory(final String productId){
        super("Insufficient inventory for product - " + productId);
    }
}