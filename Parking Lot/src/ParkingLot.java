package src;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import src.interfaces.IFeeCalculatorStrategy;
import src.models.ParkingFloor;
import src.models.ParkingSizeMapper;
import src.models.ParkingSpot;
import src.models.ParkingSpotType;
import src.models.Ticket;
import src.models.Vehicle;
import src.models.VehicleType;

public class ParkingLot {
    private final List<ParkingFloor> floors;
    private final FeeCalculator feeCalculator;
    private final Map<String, ParkingSpot> parkingSpotMap;
    private final Map<ParkingSpotType, Queue<String>> availabilityMap;
    private final Map<ParkingSpotType, ReentrantLock> lockMap;

    public ParkingLot(Builder builder){
        this.floors = builder.floors;
        this.feeCalculator = builder.feeCalculator;
        this.parkingSpotMap = new HashMap<>();
        this.availabilityMap = new HashMap<>();
        buildAvailabilityMap();
        this.lockMap = new HashMap<>();
        for(ParkingSpotType spotType: ParkingSpotType.values()){
            lockMap.put(spotType, new ReentrantLock());
        }
    }

    private void buildAvailabilityMap(){
        for(ParkingSpotType spotType: ParkingSpotType.values()){
            availabilityMap.put(spotType, new ArrayDeque<>());
        }

        for(final ParkingFloor floor: this.floors){
            // Iterate over each parking spot and add to queue.
            for(final ParkingSpot parkingSpot: floor.getParkingSpots()){
                parkingSpotMap.put(parkingSpot.getId(), parkingSpot);
                availabilityMap.get(parkingSpot.getParkingSpotType()).add(parkingSpot.getId());
            }
        }
    }

    public Ticket park(Vehicle vehicle){
        final List<ParkingSpotType> validSpotTypes = ParkingSizeMapper.getValidSpotTypes(vehicle.vehicleType());
        String parkingSpotId = "";
        for(ParkingSpotType spotType: validSpotTypes){
            ReentrantLock lock = lockMap.get(spotType);
            try {
                lock.lock();
                while(!availabilityMap.get(spotType).isEmpty()){
                    parkingSpotId = availabilityMap.get(spotType).poll();
                    if(parkingSpotMap.get(parkingSpotId).isAvailable()) break;
                    else { // Add at the end again. May be available for use later.
                        availabilityMap.get(spotType).add(parkingSpotId);
                        parkingSpotId = "";
                    }
                }   
            }
            finally {
                lock.unlock();
            }
            if(!parkingSpotId.isEmpty()) break;
        }

        if(parkingSpotId.isEmpty()){
            throw new ParkingLotFullException(vehicle.vehicleType());
        }

        // Generate ticket.
        final Ticket ticket = new Ticket(Ticket.getNextId(), vehicle, parkingSpotMap.get(parkingSpotId));
        System.out.println("Parking vehicle " + vehicle.vehicleId() + " at spot " + parkingSpotId);
        return ticket;
    }

    public BigDecimal unpark(final Ticket ticket){
        if(!ticket.isActive()){
            throw new InvalidTicketException(ticket);
        }

        // Mark ticket as closed.
        ticket.close();
        // Return parking spot to queue.
        ParkingSpotType spotType = ticket.getParkingSpot().getParkingSpotType();
        ReentrantLock lock = lockMap.get(spotType);
        try {
            lock.lock();
            availabilityMap.get(spotType).add(ticket.getParkingSpot().getId());
        } finally {
            lock.unlock();
        }

        final BigDecimal fee = feeCalculator.calculate(ticket);
        System.out.println("Unparking vehicle " + ticket.getVehicle().vehicleId() + " from spot " + ticket.getParkingSpot().getId() + ". Total fee - " + fee.toPlainString());
        return fee;
    }

    public class ParkingLotFullException extends RuntimeException {
        public ParkingLotFullException(VehicleType vehicleType) {
            super("No available spot for vehicle type: " + vehicleType);
        }
    }

    public class InvalidTicketException extends RuntimeException {
        public InvalidTicketException(Ticket ticket) {
            super("Ticket is invalid " + ticket.getId());
        }
    }   

    public static class Builder {
        private List<ParkingFloor> floors;
        private FeeCalculator feeCalculator;

        public static Builder getInstance(){
            return new Builder();
        }

        public Builder withFloors(List<ParkingFloor> floors){
            this.floors = floors;
            return (this);
        }

        public Builder withFeeCalculatorStrategy(IFeeCalculatorStrategy strategy){
            this.feeCalculator = new FeeCalculator(strategy);
            return (this);
        }

        public ParkingLot build(){
            return new ParkingLot(this);
        }
    }


}