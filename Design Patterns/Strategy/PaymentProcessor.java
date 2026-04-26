import paymentmethods.IPaymentStrategy;

public class PaymentProcessor {

    private IPaymentStrategy paymentStrategy;

    public PaymentProcessor(IPaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }

    public void setPaymentStrategy(final IPaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }

    public boolean pay(final double amount){
        if(paymentStrategy == null){
            throw new IllegalStateException("No payment strategy set");
        }

        return paymentStrategy.pay(amount);
    }

}