package src.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingSizeMapper {

    private static final Map<VehicleType, List<ParkingSpotType>> vehicleSizeToParkingSpotTypeMap = new HashMap<>();

    static {
        populateSizeMap();
    }

    private static void populateSizeMap(){
        for(VehicleType vehicleType: VehicleType.values()){
            List<ParkingSpotType> result = new ArrayList<>();
            for(ParkingSpotType parkingSpotType: ParkingSpotType.values()){
                if(vehicleType.ordinal() <= parkingSpotType.ordinal()){
                    result.add(parkingSpotType);
                }
            }
            Collections.sort(result);
            vehicleSizeToParkingSpotTypeMap.put(vehicleType, result);
        }
    }

    public static List<ParkingSpotType> getValidSpotTypes(final VehicleType vehicleType){
        return vehicleSizeToParkingSpotTypeMap.get(vehicleType);
    }
}