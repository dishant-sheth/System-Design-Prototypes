package src.states;

import src.VendingMachine;
import src.interfaces.IMachineState;
import src.models.Product;

public class MoneyInsertedMachineState implements IMachineState {

    private final VendingMachine vendingMachine;
    public MoneyInsertedMachineState(final VendingMachine vendingMachine){
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(final String productId){
        System.out.println("[INVALID ACTION] Product has already been selected. Please cancel to restart selection process");
    }

    @Override
    public void insertMoney(final double amount){
        System.out.println("[INVALID ACTION] Payment is already compeled.");
    }

    @Override
    public void dispense(){
        final Product product = this.vendingMachine.currentTransaction.getProduct();
        this.vendingMachine.inventory.reduceInventory(product, 1);
        System.out.println("Dispensing product - " + product.name);
        // Manage change
        final double changeToReturn = this.vendingMachine.currentTransaction.amountPaid - product.cost;
        this.vendingMachine.changeDispenser.returnChange(changeToReturn);
        System.out.println("Returning change = " + changeToReturn);
        this.vendingMachine.setMachineState(new DispensedMachineState(this.vendingMachine));
    }

    @Override
    public void cancel(){
        System.out.println("Returning " + this.vendingMachine.currentTransaction.amountPaid);
        // Modify in change dispenser also.
        this.vendingMachine.resetTransaction();
        this.vendingMachine.setMachineState(new IdleMachineState(this.vendingMachine));
    }
}