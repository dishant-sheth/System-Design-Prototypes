package src.exceptions;

import src.models.ReservationState;

public class InvalidReservationActionException extends RuntimeException {
    public InvalidReservationActionException(ReservationState state){
        super("Cannot perform this action is current state " + state);
    }
}