package src.states;

import src.VendingMachine;
import src.exceptions.InvalidDenomination;
import src.interfaces.IMachineState;
import src.models.Denominations;

public class ProductSelectedMachineState implements IMachineState {

    private final VendingMachine vendingMachine;
    private double amountPaid;
    public ProductSelectedMachineState(final VendingMachine vendingMachine){
        this.vendingMachine = vendingMachine;
        this.amountPaid = 0;
    }

    @Override
    public void selectProduct(final String productId){
        System.out.println("[INVALID ACTION] Product has already been selected. Please cancel to restart selection process");
    }

    @Override
    public void insertMoney(final double amount){
        Denominations denomination = Denominations.getDenomination(amount);
        if(denomination == null){
            throw new InvalidDenomination();
        }
        amountPaid += amount;
        // Modify in change dispenser also.
        this.vendingMachine.changeDispenser.addChange(denomination, 1);
        final double productCost = vendingMachine.currentTransaction.getProduct().cost;
        if(amountPaid < productCost){
            System.out.println("Please pay " + (productCost - amountPaid) + " more");
            return;
        }
        System.out.println("Successfully inserted " + amountPaid);
        this.vendingMachine.currentTransaction.setAmountPaid(amountPaid);
        this.vendingMachine.setMachineState(new MoneyInsertedMachineState(this.vendingMachine));
    }

    @Override
    public void dispense(){
        final double productCost = vendingMachine.currentTransaction.getProduct().cost;
        System.out.println("Please pay " + (productCost - amountPaid) + " more");
    }

    @Override
    public void cancel(){
        System.out.println("Returning " + amountPaid);
        // Modify in change dispenser also.
        this.vendingMachine.changeDispenser.returnChange(this.amountPaid);
        this.amountPaid = 0;
        this.vendingMachine.resetTransaction();
        this.vendingMachine.setMachineState(new IdleMachineState(this.vendingMachine));
    }
}