package src.models;

public class Reservation {
    private final String id;
    public final String productId;
    public final int quantity;
    private final long createdAt;
    private final long expiresAt;
    private ReservationState state;

    public Reservation(String id, String productId, int quantity, long ttlInSeconds){
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + (ttlInSeconds * 1000);
        this.state = ReservationState.RESERVED;
    }

    public ReservationState getState(){
        return this.state;
    }

    public void setState(final ReservationState state){
        this.state = state;
    }

    public long getExpiry(){
        return expiresAt;
    }
}