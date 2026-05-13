package src.exceptions;

public class InvalidDenomination extends RuntimeException {
    public InvalidDenomination(){
        super("This denomination is not accepted");
    }
}