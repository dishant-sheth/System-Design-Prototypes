package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import stockpriceobservers.IStockPriceObserver;

public class StockPrice {
    private final Map<String, Double> pricesMap;
    private final List<IStockPriceObserver> observers;
    private String lastUpdatedStock = "";

    public StockPrice(){
        this.pricesMap = new HashMap<>();
        this.observers = new ArrayList<>();
    }

    public void registerObserver(IStockPriceObserver observer){
        observers.add(observer);
    }

    public void unregisterObserver(IStockPriceObserver observer){
        observers.remove(observer);
    }

    public void notifyObservers(){
        for(IStockPriceObserver observer: observers){
            observer.onPriceUpdate(this);
        }
    }

    public void updateStockPrice(String stock, double price){
        pricesMap.put(stock, price);
        lastUpdatedStock = stock;
        notifyObservers();
    }

    public String getLastUpdatedStock(){
        return lastUpdatedStock;
    }

    public double getStockPrice(String stock){
        return pricesMap.getOrDefault(stock, 0.0);
    }
}