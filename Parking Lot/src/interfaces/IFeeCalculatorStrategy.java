package src.interfaces;

import java.math.BigDecimal;
import src.models.Ticket;

public interface IFeeCalculatorStrategy {
    public BigDecimal calculate(final Ticket ticket);
}