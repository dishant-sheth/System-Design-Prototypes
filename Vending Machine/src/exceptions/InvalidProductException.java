package src.exceptions;

public class InvalidProductException extends RuntimeException {
    public InvalidProductException(){
        super("Specified product is invalid");
    }
}