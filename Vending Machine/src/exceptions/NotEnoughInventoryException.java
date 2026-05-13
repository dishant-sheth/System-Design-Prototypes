package src.exceptions;

public class NotEnoughInventoryException extends RuntimeException {
    public NotEnoughInventoryException(){
        super("Not enough inventory is available");
    }
}