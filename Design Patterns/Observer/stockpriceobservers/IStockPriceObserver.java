package stockpriceobservers;

import models.StockPrice;

public interface IStockPriceObserver {
    public void onPriceUpdate(StockPrice stockPrice);
}