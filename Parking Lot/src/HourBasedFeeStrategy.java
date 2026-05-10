package src;

import java.math.BigDecimal;
import java.util.Map;
import src.interfaces.IFeeCalculatorStrategy;
import src.models.ParkingSpotType;
import src.models.Ticket;

public class HourBasedFeeStrategy implements IFeeCalculatorStrategy {

    private final Map<ParkingSpotType,BigDecimal> costMap;
    public HourBasedFeeStrategy(Map<ParkingSpotType,BigDecimal> costMap) {
        this.costMap = costMap;
    }

    @Override
    public BigDecimal calculate(final Ticket ticket) {
        final int numOfMinutes = (int)Math.ceil((ticket.getExitTime() - ticket.getEntryTime())/(1000 * 60 * 1.0));
        final int numOfHours = (int)Math.ceil(numOfMinutes/60.0);
        return costMap.get(ticket.getParkingSpot().getParkingSpotType()).multiply(new BigDecimal(numOfHours));
    }
}