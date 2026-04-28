package stockpriceobservers;

import models.StockPrice;

public class PriceDisplaySvc implements IStockPriceObserver {

    @Override
    public void onPriceUpdate(StockPrice stockPrice){
        final String stockName = stockPrice.getLastUpdatedStock();
        System.out.println("Stock Price of " + stockName + " updated to $" + stockPrice.getStockPrice(stockName));
    }

}