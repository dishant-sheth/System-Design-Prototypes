package src.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParkingFloor {
    private final String floorId;
    private List<ParkingSpot> parkingSpots;
    private boolean isUsable;

    public ParkingFloor(final String floorId, final List<ParkingSpot> parkingSpots){
        this.floorId = floorId;
        this.parkingSpots = parkingSpots;
        this.isUsable = true;
    }

    public void setFloorUsability(boolean isUsable){
        this.isUsable = isUsable;
    }

    public List<ParkingSpot> getParkingSpots(){
        return this.parkingSpots;
    }

    public static class Builder {

        String floorId;
        List<ParkingSpot> parkingSpots;

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder withFloorNum(final int floorNum){
            this.floorId = "F" + floorNum;
            return (this);
        }

        public Builder withSpots(Map<ParkingSpotType, Integer> spotTypeCountMap){
            parkingSpots = new ArrayList<>();
            for(final ParkingSpotType parkingSpotType: spotTypeCountMap.keySet()){
                final int count = spotTypeCountMap.get(parkingSpotType);
                for(int i=0; i<count; ++i){
                    final String parkingSpotId = floorId + "-" + parkingSpotType.toString().charAt(0) + String.valueOf(i);
                    parkingSpots.add(new ParkingSpot(parkingSpotId, parkingSpotType, this.floorId));
                }
            }
            return (this);
        }

        public ParkingFloor build(){
            return new ParkingFloor(this.floorId, this.parkingSpots);
        }
    }
}