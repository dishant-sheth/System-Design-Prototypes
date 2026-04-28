package stockpriceobservers;

import java.util.HashMap;
import java.util.Map;
import models.StockPrice;

public class TradingBotSvc implements IStockPriceObserver {

    private final Map<String,Double> stockPriceMap;

    public TradingBotSvc(){
        stockPriceMap = new HashMap<>();
    }

    @Override
    public void onPriceUpdate(StockPrice stockPrice){
        final String stockName = stockPrice.getLastUpdatedStock();
        final double currentPrice = stockPrice.getStockPrice(stockName);
        if(stockPriceMap.containsKey(stockName)){
            final double oldPrice = stockPriceMap.get(stockName);
            if(currentPrice >= oldPrice){
                System.out.println("HOLD | " + stockName + " | Old -> " + oldPrice + " | Current -> " + currentPrice);
            } else {
                System.out.println("TRACK STOP LOSS / BUY MORE | " + stockName + " | Old -> " + oldPrice + " | Current -> " + currentPrice);
            }
        }
        stockPriceMap.put(stockName, currentPrice);
    }

}