package src.exceptions;

public class CannotFormChangeException extends RuntimeException {

    public CannotFormChangeException(){
        super("Cannot form change with given denominations");
    }
}