package src.exceptions;

public class StockNotFoundException extends RuntimeException {
    public StockNotFoundException(String stockSymbol){
        super(stockSymbol + " stock does not exist");
    }
}