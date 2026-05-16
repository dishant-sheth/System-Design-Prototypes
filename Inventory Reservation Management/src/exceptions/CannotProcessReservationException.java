package src.exceptions;

public class CannotProcessReservationException extends RuntimeException {
    public CannotProcessReservationException(final String otherErrorMsg){
        super("Failed to process reservation due to " + otherErrorMsg);
    }
}