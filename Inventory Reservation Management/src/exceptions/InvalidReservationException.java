package src.exceptions;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(final String reservationId){
        super("Could not find valid reservation with ID " + reservationId);
    }
}