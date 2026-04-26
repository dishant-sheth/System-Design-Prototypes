package paymentmethods;

public class PayPalPayment implements IPaymentStrategy {
    private final String email;

    public PayPalPayment(final String email){
        this.email = email;
    }

    @Override
    public boolean pay(final double amount){
        System.out.println("Raising payment request for " + amount + " on PayPal account " + this.email);
        return true;
    }
}