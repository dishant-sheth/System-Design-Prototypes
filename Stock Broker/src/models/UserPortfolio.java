package src.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import src.interfaces.IUserPortfolioManager;

public class UserPortfolio implements IUserPortfolioManager {

    public record UserPortfolioSnapshot(
        double balance,
        Map<String, Integer> holdings
    ){}

    double balance, reservedBalance;
    final Map<String, Integer> holdings, reservedHoldings;
    final List<Trade> tradeHistory;
    final ReentrantLock rwLock;

    public UserPortfolio(){
        this.balance = 0;
        this.reservedBalance = 0;
        this.holdings = new HashMap<>();
        this.reservedHoldings = new HashMap<>();
        this.tradeHistory = new ArrayList<>();
        this.rwLock = new ReentrantLock();
    }

    @Override
    public ReentrantLock getLock(){
        return this.rwLock;
    }

    @Override
    public void updateBalance(double delta){
        this.balance += delta;
    }

    @Override
    public void updateReserveBalance(double delta){
        this.reservedBalance += delta;
    }

    @Override
    public void updateHoldings(String stockSymbol, Integer quantity){
        holdings.put(stockSymbol, holdings.getOrDefault(stockSymbol, 0) + quantity);
    }

    @Override
    public void updateReserveHoldings(String stockSymbol, Integer quantity){
        reservedHoldings.put(stockSymbol, holdings.getOrDefault(stockSymbol, 0) + quantity);
    }

    @Override
    public boolean hasSufficientHoldings(String stockSymbol, Integer quantity){
        if(holdings.containsKey(stockSymbol)){
            return (holdings.get(stockSymbol) >= quantity);
        }
        return false;
    }

    @Override
    public double getBalance(){
        return this.balance;
    }

    @Override
    public void recordTrade(final Trade trade){
        tradeHistory.addFirst(trade);
    }

    @Override
    public List<Trade> getTradeList(){
        return tradeHistory;
    }

    @Override
    public UserPortfolioSnapshot getSnapshot(){
        return new UserPortfolioSnapshot(this.balance, new HashMap<>(this.holdings));
    }
    
}