package paymentmethods;

public class CreditCardPayment implements IPaymentStrategy{

    private final String cardNumber;
    private final String cardExpiry;
    private final String cvv;

    public CreditCardPayment(final String cardNumber, final String cardExpiry, final String cvv){
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
        this.cvv = cvv;
    }

    @Override
    public boolean pay(final double amount){
        System.out.println("Paying " + amount + " on credit card ending" + cardNumber.substring(cardNumber.length() - 4));
        return true;
    }
}