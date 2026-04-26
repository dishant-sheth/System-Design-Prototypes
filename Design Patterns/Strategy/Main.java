
import paymentmethods.CreditCardPayment;
import paymentmethods.IPaymentStrategy;

public class Main {

    public static void main(String[] args) {
        IPaymentStrategy defaulStrategy = new CreditCardPayment("", "", "");
        // Default payment is credit card.
        PaymentProcessor paymentProcessor = new PaymentProcessor(defaulStrategy);
        paymentProcessor.pay(100.98);
    }
}