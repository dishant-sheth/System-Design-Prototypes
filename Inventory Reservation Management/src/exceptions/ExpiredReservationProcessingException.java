package src.exceptions;

public class ExpiredReservationProcessingException extends RuntimeException {

    public ExpiredReservationProcessingException(String message){
        super("Error occurred when processing expired entries - " + message);
    }
}