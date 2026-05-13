package src.interfaces;

public interface IMachineState {

    public void selectProduct(final String productId);

    public void insertMoney(final double amount);

    public void dispense();

    public void cancel();

}