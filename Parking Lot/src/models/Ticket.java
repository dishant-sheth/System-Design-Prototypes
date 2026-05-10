package src.models;

import java.util.concurrent.atomic.AtomicLong;

public class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;
    private boolean isActive;
    private final long entryTime;
    private long exitTime;

    private static AtomicLong ID = new AtomicLong(1);

    public static synchronized String getNextId(){
        return "T" + ID.addAndGet(1);
    }

    public Ticket(final String ticketId, final Vehicle vehicle, final ParkingSpot parkingSpot){
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
        this.isActive = true;
        this.entryTime = System.currentTimeMillis();
    }

    public void close(){
        this.exitTime = System.currentTimeMillis();
        this.isActive = false;
    }

    public boolean isActive(){
        return this.isActive;
    }

    public String getId(){
        return this.ticketId;
    }

    public ParkingSpot getParkingSpot(){
        return this.parkingSpot;
    }

    public long getEntryTime(){
        return this.entryTime;
    }

    public long getExitTime(){
        return this.exitTime;
    }

    public Vehicle getVehicle(){
        return this.vehicle;
    }
}