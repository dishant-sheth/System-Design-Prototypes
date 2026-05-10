package src;

import java.math.BigDecimal;
import src.interfaces.IFeeCalculatorStrategy;
import src.models.Ticket;

public class FeeCalculator {
    
    private final IFeeCalculatorStrategy feeCalculatorStrategy;

    public FeeCalculator(final IFeeCalculatorStrategy feeCalculatorStrategy){
        this.feeCalculatorStrategy = feeCalculatorStrategy;
    }

    public BigDecimal calculate(final Ticket ticket){
        return feeCalculatorStrategy.calculate(ticket);
    }

}