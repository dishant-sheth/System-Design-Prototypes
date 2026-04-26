package paymentmethods;

public class CryptoPayment implements IPaymentStrategy {
    private final String walletId;

    public CryptoPayment(final String walletId){
        this.walletId = walletId;
    }

    @Override
    public boolean pay(final double amount){
        System.out.println("Paying " + amount + " from crypto wallet " + walletId);
        return true;
    }

}