package src.interfaces;

public interface InventoryRepository {
    public boolean isValidInventory(String productId);
    public int getAvailableInventory(String productId);
    public void reserveInventory(String productId, int quantity);
    public void consumeInventory(String productId, int quantity);
    public void returnInventory(String productId, int quantity);
}