package src.states;

import src.VendingMachine;
import src.exceptions.InvalidProductException;
import src.exceptions.NotEnoughInventoryException;
import src.interfaces.IMachineState;
import src.models.Product;

public class IdleMachineState implements IMachineState {

    private final VendingMachine vendingMachine;

    public IdleMachineState(VendingMachine vendingMachine){
        this.vendingMachine = vendingMachine;
    }

    @Override
    public void selectProduct(final String productId){
        if(!vendingMachine.inventory.containsProduct(productId)){
            throw new InvalidProductException();
        }
        if(!vendingMachine.inventory.hasInventory(productId)){
            throw new NotEnoughInventoryException();
        }
        final Product product = vendingMachine.inventory.getProduct(productId);
        vendingMachine.startTransaction(product);
        System.out.println("Selected Product - " + product.name + " costs " + product.cost);
        this.vendingMachine.setMachineState(new ProductSelectedMachineState(this.vendingMachine));
    }

    @Override
    public void insertMoney(final double amount){
        System.out.println("[INVALID ACTION] No product selected yet");
    }

    @Override
    public void dispense(){
        System.out.println("[INVALID ACTION] No product selected yet");
    }

    @Override
    public void cancel(){
        System.out.println("[INVALID ACTION] Machine is already idle");
    }
}