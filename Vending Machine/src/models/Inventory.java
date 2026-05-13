package src.models;

import java.util.HashMap;
import java.util.Map;
import src.exceptions.InvalidProductException;
import src.exceptions.NotEnoughInventoryException;

public class Inventory {

    private final Map<String, Integer> inventoryMap;
    private final Map<String, Product> productIdMap;

    public Inventory(){
        this.inventoryMap = new HashMap<>();
        this.productIdMap = new HashMap<>();
    }

    public void addInventory(Product product, Integer quantity){
        if(!inventoryMap.containsKey(product.productId)){
            productIdMap.put(product.productId, product);
            inventoryMap.put(product.productId, 0);
        }
        inventoryMap.put(product.productId, inventoryMap.get(product.productId) + quantity);
    }

    public void reduceInventory(Product product, Integer quantity){
        if(!inventoryMap.containsKey(product.productId)){
            throw new InvalidProductException();
        }
        int newQuantity = inventoryMap.get(product.productId) - quantity;
        if(newQuantity > 0) inventoryMap.put(product.productId, newQuantity);
        else if(newQuantity == 0){
            inventoryMap.remove(product.productId);
            productIdMap.remove(product.productId);
        }
        else if(newQuantity < 0) throw new NotEnoughInventoryException();
    }

    public boolean containsProduct(final String productId){
        return productIdMap.containsKey(productId);
    }

    public boolean hasInventory(final String productId){
        if(!inventoryMap.containsKey(productId)){
            return false;
        }
        return (inventoryMap.get(productId) > 0);
    }

    public Product getProduct(final String productId){
        if(!inventoryMap.containsKey(productId)){
            throw new InvalidProductException();
        }
        return productIdMap.get(productId);
    }

}