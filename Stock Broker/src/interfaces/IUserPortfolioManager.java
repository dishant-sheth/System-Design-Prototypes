package src.interfaces;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import src.models.Trade;
import src.models.UserPortfolio.UserPortfolioSnapshot;

public interface IUserPortfolioManager {

    public ReentrantLock getLock();

    public void updateBalance(double delta);

    public void updateReserveBalance(double delta);

    public void updateHoldings(String stockSymbol, Integer quantity);

    public void updateReserveHoldings(String stockSymbol, Integer quantity);

    public boolean hasSufficientHoldings(String stockSymbol, Integer quantity);

    public double getBalance();

    public void recordTrade(final Trade trade);

    public List<Trade> getTradeList();

    public UserPortfolioSnapshot getSnapshot();
}