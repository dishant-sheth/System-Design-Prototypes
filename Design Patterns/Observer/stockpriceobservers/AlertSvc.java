package stockpriceobservers;

import java.util.HashMap;
import java.util.Map;
import models.StockPrice;

public class AlertSvc implements IStockPriceObserver {

    private final Map<String, Double> stockThresholds;

    public AlertSvc(){
        this.stockThresholds = new HashMap<>();
        // Dummy data
        stockThresholds.put("MSFT", 425.0);
    }

    @Override
    public void onPriceUpdate(StockPrice stockPrice){
        final String stock = stockPrice.getLastUpdatedStock();
        final double price = stockPrice.getStockPrice(stock);
        if(stockThresholds.containsKey(stock)){
            double thresholdPrice = stockThresholds.get(stock);
            if(price >= thresholdPrice){
                System.out.println("ALERT! Threshold breached for stock " + stock + ". Current price -> $" + price);
            }
        }
        
    }
}