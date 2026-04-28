import models.StockPrice;
import stockpriceobservers.AlertSvc;
import stockpriceobservers.PriceDisplaySvc;
import stockpriceobservers.TradingBotSvc;

public class Main {

    public static void main(String[] args) {
        StockPrice stockPrice = new StockPrice();
        stockPrice.registerObserver(new AlertSvc());
        stockPrice.registerObserver(new PriceDisplaySvc());
        stockPrice.registerObserver(new TradingBotSvc());

        stockPrice.updateStockPrice("MSFT", 420.55);
        stockPrice.updateStockPrice("MSFT", 422.45);
        stockPrice.updateStockPrice("MSFT", 424.25);
        stockPrice.updateStockPrice("MSFT", 425.75);
        stockPrice.updateStockPrice("MSFT", 423.54);
        stockPrice.updateStockPrice("MSFT", 480.59);
    }
}