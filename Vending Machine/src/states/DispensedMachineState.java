package src.states;

import src.VendingMachine;
import src.interfaces.IMachineState;

public class DispensedMachineState implements IMachineState {

    private final VendingMachine vendingMachine;
    public DispensedMachineState(final VendingMachine vendingMachine){
        this.vendingMachine = vendingMachine;
        validation();
        this.vendingMachine.completeTransaction();
        this.vendingMachine.setMachineState(new IdleMachineState(this.vendingMachine));
    }

    private void validation(){
        System.out.println("Checking in inventory & change dispenser are functional");
    }

    @Override
    public void selectProduct(final String productId){
        System.out.println("[INVALID ACTION] Internal state - not for use");
    }

    @Override
    public void insertMoney(final double amount){
        System.out.println("[INVALID ACTION] Internal state - not for use");
    }

    @Override
    public void dispense(){
        System.out.println("[INVALID ACTION] Internal state - not for use");
    }

    @Override
    public void cancel(){
        System.out.println("[INVALID ACTION] Internal state - not for use");
    }
}