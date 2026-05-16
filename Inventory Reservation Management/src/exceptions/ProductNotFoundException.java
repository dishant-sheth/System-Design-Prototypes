package src.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId){
        super("Did not find product with ID" + productId );
    }
}