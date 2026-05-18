package src.models;

import java.util.concurrent.locks.ReentrantLock;

public class Order {
    public final String id;
    public final String userId;
    public final OrderType type;
    public OrderStatus status;
    public final String stock;
    public final double price;
    public int quantity;
    public final long orderPlacedAt;
    public final ReentrantLock writeLock;

    public Order(String id, String userId, OrderType type, String stock, double price, int quantity){
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = OrderStatus.PLACED;
        this.stock = stock;
        this.price = price;
        this.quantity = quantity;
        this.orderPlacedAt = System.currentTimeMillis();
        this.writeLock = new ReentrantLock();
    }

    @Override
    public String toString(){
        return (this.type.toString() + " " + this.stock + " " + this.price + " " + this.quantity);
    }


}