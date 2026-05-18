package src.models;

public class Stock {
    final String stockSymbol;
    final String companyName;
    final double initialPrice;

    public Stock(String stockSymbol, String companyName, double initialPrice){
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.initialPrice = initialPrice;
    }
}