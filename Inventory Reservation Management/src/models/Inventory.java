package src.models;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import src.exceptions.InsufficientInventory;
import src.exceptions.ProductNotFoundException;
import src.interfaces.InventoryRepository;

public class Inventory implements InventoryRepository {

    public record QuantitySnapshot(
        int total,
        int reserved,
        int consumed
    ){
        public int getAvailableStock(){
            return (total - reserved - consumed);
        } 
    }

    public class Quantity {
        private int total = 0, reserved = 0, consumed = 0;

        public void updateTotal(int qty){
            this.total += qty;
        }

        public void updateReserved(int qty){
            this.reserved += qty;
        }

        public void updateConsumed(int qty){
            this.consumed += qty;
        }

        public int getAvailableStock(){
            return (total - reserved - consumed);
        }

        public QuantitySnapshot snapshot(){
            return new QuantitySnapshot(this.total, this.reserved, this.consumed);
        }
        
    }

    private final Map<String, Product> productMap;
    private final Map<String, Quantity> inventoryMap;
    // Fine-grained per product ID locking?
    private final Map<String, ReadWriteLock> inventoryLocks;

    public Inventory(){
        this.productMap = new ConcurrentHashMap<>();
        this.inventoryMap = new ConcurrentHashMap<>();
        this.inventoryLocks = new ConcurrentHashMap<>();
    }

    public void addProduct(String productId, String name, int availableStock){
        final Product product = new Product(productId, name);
        // If 2 threads attempt to add same product at the same time, 
        if(productMap.putIfAbsent(productId, product) != null){
            return;
        }

        // Add lock per product.
        inventoryLocks.put(productId, new ReentrantReadWriteLock());
        
        // Add in inventory
        Quantity quantity = new Quantity();
        quantity.updateTotal(availableStock);
        inventoryMap.put(productId, quantity);
    }

    public void addInventory(String productId, int refillStock){
        if(!productMap.containsKey(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Get write lock for given product Id.
        Lock productWriteLock = inventoryLocks.get(productId).writeLock();
        try {
            productWriteLock.lock();
            inventoryMap.get(productId).updateTotal(refillStock);
        } finally {
            productWriteLock.unlock();
        }
    }

    public QuantitySnapshot getInventory(final String productId){
        if(!isValidInventory(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Get write lock for given product Id.
        Lock productReadLock = inventoryLocks.get(productId).readLock();
        try {
            productReadLock.lock();
            return inventoryMap.get(productId).snapshot();
        } finally {
            productReadLock.unlock();
        }

    }

    @Override
    public boolean isValidInventory(String productId){
        return productMap.containsKey(productId);
    }

    @Override
    public int getAvailableInventory(String productId){
        if(!productMap.containsKey(productId)){
            throw new ProductNotFoundException(productId);
        }

        Lock productReadLock = inventoryLocks.get(productId).readLock();
        try {
            productReadLock.lock();
            return inventoryMap.get(productId).getAvailableStock();
        } finally {
            productReadLock.unlock();
        }
    }

    @Override
    public void reserveInventory(String productId, int quantity){
        if(!productMap.containsKey(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Optimistic check.
        if(getAvailableInventory(productId) < quantity){
            throw new InsufficientInventory(productId);
        }

        // Get write lock for given product Id.
        Lock productWriteLock = inventoryLocks.get(productId).writeLock();
        try {
            productWriteLock.lock();
            // Check available quantity inside write lock
            if(inventoryMap.get(productId).getAvailableStock() < quantity){
                throw new InsufficientInventory(productId);
            }
            inventoryMap.get(productId).updateReserved(quantity);
        } finally {
            productWriteLock.unlock();
        }
    }

    @Override
    public void consumeInventory(String productId, int quantity){
        if(!productMap.containsKey(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Get write lock for given product Id.
        Lock productWriteLock = inventoryLocks.get(productId).writeLock();
        try {
            productWriteLock.lock();
            inventoryMap.get(productId).updateReserved(quantity * -1);
            inventoryMap.get(productId).updateConsumed(quantity);
        } finally {
            productWriteLock.unlock();
        }
    }

    @Override
    public void returnInventory(String productId, int quantity){
        if(!productMap.containsKey(productId)){
            throw new ProductNotFoundException(productId);
        }

        // Get write lock for given product Id.
        Lock productWriteLock = inventoryLocks.get(productId).writeLock();
        try {
            productWriteLock.lock();
            inventoryMap.get(productId).updateReserved(quantity * -1);
        } finally {
            productWriteLock.unlock();
        }
    }

    
}