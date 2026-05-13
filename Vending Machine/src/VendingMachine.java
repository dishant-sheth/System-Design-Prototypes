package src;

import java.util.ArrayList;
import java.util.List;
import src.interfaces.IMachineState;
import src.models.Inventory;
import src.models.Product;
import src.models.Transaction;
import src.states.IdleMachineState;

public class VendingMachine {
    private IMachineState machineState;
    public final Inventory inventory;
    public final ChangeDispenser changeDispenser;
    private List<Transaction> historicalTransactions;
    public Transaction currentTransaction;

    public VendingMachine(){
        this.machineState = new IdleMachineState(this);
        this.inventory = new Inventory();
        this.historicalTransactions = new ArrayList<>();
        this.changeDispenser = new ChangeDispenser();
    }

    public void selectProduct(final String productId){
        this.machineState.selectProduct(productId);
    }

    public void insertMoney(final double amount){
        this.machineState.insertMoney(amount);
    }

    public void dispense(){
        this.machineState.dispense();
    }

    public void cancel(){
        this.machineState.cancel();
    }

    public void setMachineState(IMachineState state){
        this.machineState = state;
    } 

    public void startTransaction(final Product product){
        this.currentTransaction = new Transaction(product);
    }

    public void resetTransaction(){
        this.currentTransaction = null;
    }

    public void completeTransaction(){
        this.historicalTransactions.add(currentTransaction);
        System.out.println(currentTransaction);
        this.currentTransaction = null;
    }
}