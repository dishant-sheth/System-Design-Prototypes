package paymentmethods;

public interface IPaymentStrategy {
    
    public boolean pay(final double amount);
}