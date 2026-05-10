package src.models;

public class ParkingSpot {
    private final String parkingSpotId;
    private final ParkingSpotType parkingSpotType;
    private final String floorId;
    private boolean isAvailable;

    public ParkingSpot(final String parkingSpotId, final ParkingSpotType parkingSpotType, final String floorId){
        this.parkingSpotId = parkingSpotId;
        this.parkingSpotType = parkingSpotType;
        this.floorId = floorId;
        this.isAvailable = true;
    }

    public void setParkingSpotAvailability(boolean isAvailable){
        this.isAvailable = isAvailable;
    }
    
    public String getId(){
        return this.parkingSpotId;
    }

    public ParkingSpotType getParkingSpotType(){
        return this.parkingSpotType;
    }

    public boolean isAvailable(){
        return this.isAvailable;
    }
}