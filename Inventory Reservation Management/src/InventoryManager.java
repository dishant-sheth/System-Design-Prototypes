package src;

import src.models.Inventory;

public class InventoryManager {

    private final Inventory inventory;
    private final ReservationManager reservationManager;

    public InventoryManager(){
        this.inventory = new Inventory();
        this.reservationManager = new ReservationManager(this.inventory);
    }

    public void addProduct(String productId, String name, int availableStock){
        inventory.addProduct(productId, name, availableStock);
    }

    public void addInventory(String productId, int refillStock){
        inventory.addInventory(productId, refillStock);
    }

    public String reserveInventory(String productId, int qty, long ttlSeconds){
        return reservationManager.reserve(productId, qty, ttlSeconds);
    }

    public void confirmReservation(String reservationId){
        reservationManager.confirmReservation(reservationId);
    }

    public void releaseReservation(String reservationId){
        reservationManager.releaseReservation(reservationId);
    }

    public Inventory.QuantitySnapshot getInventory(String productId){
        return inventory.getInventory(productId);
    }


    
}