package src.models;

public class Product {
    public final String name;
    public final String productId;
    public final double cost;

    public Product(final String name, final String productId, final double cost){
        this.name = name;
        this.productId = productId;
        this.cost = cost;
    }
}