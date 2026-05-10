
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import src.HourBasedFeeStrategy;
import src.ParkingLot;
import src.models.ParkingFloor;
import src.models.ParkingSpotType;



public class Main {
    public static void main(String[] args) {
        final int numFloors = 5;
        // Build common spot type to count map.
        final Map<ParkingSpotType, Integer> spotTypeCountMap = new HashMap<>();
        spotTypeCountMap.put(ParkingSpotType.SMALL, 20);
        spotTypeCountMap.put(ParkingSpotType.MEDIUM, 10);
        spotTypeCountMap.put(ParkingSpotType.LARGE, 5);
        // Builld cost map
        Map<ParkingSpotType,BigDecimal> costMap = new HashMap<>();
        costMap.put(ParkingSpotType.SMALL, new BigDecimal(10));
        costMap.put(ParkingSpotType.MEDIUM, new BigDecimal(25));
        costMap.put(ParkingSpotType.LARGE, new BigDecimal(50));

        //
        List<ParkingFloor> floors = new ArrayList<>();
        for(int i=0; i<numFloors; ++i){
            final ParkingFloor floor = ParkingFloor.Builder.getInstance()
                .withFloorNum(i)
                .withSpots(spotTypeCountMap)
                .build();
            floors.add(floor);
        }
        ParkingLot parkingLot = ParkingLot.Builder.getInstance()
            .withFloors(floors)
            .withFeeCalculatorStrategy(new HourBasedFeeStrategy(costMap))
            .build();
        
    }
}