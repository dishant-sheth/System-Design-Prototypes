package src;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import src.exceptions.CannotCancelOrderException;
import src.exceptions.InsufficientFundsException;
import src.exceptions.InsufficientHoldingsException;
import src.exceptions.OrderNotFoundException;
import src.exceptions.StockNotFoundException;
import src.exceptions.UserNotFoundException;
import src.interfaces.IUserManager;
import src.interfaces.IUserPortfolioManager;
import src.models.Order;
import src.models.OrderStatus;
import src.models.OrderType;
import src.models.Stock;
import src.models.Trade;
import src.models.UserPortfolio;

public class StockBroker {
    
    final IUserManager userManager;
    final Map<String, Stock> stockMap;
    final Map<String, Order> orderMap;
    final OrderMatchingEngine orderMatchingEngine;
    final TradeExecutor tradeExecutor;
    
    public StockBroker() {
        this.userManager = new UserRepository();
        this.stockMap = new ConcurrentHashMap<>();
        this.orderMap = new ConcurrentHashMap<>();
        this.orderMatchingEngine = new OrderMatchingEngine();
        this.tradeExecutor = new TradeExecutor(this.userManager);
    }

    public void registerUser(String userId, String name, double initialBalance){
        this.userManager.registerUser(userId, name, initialBalance);
    }

    public void addStock(String stockSymbol, String companyName, double initialPrice) {
        final Stock stock = new Stock(stockSymbol, companyName, initialPrice);
        if(stockMap.putIfAbsent(stockSymbol, stock) != null){
            return;
        }

        this.orderMatchingEngine.addNewStock(stockSymbol);
    }

    public void addStockToPortfolio(String userId, String stockSymbol, Integer qty){
        if(!userManager.isValidUser(userId)){
            throw new UserNotFoundException(userId);
        }

        if(!stockMap.containsKey(stockSymbol)){
            throw new StockNotFoundException(stockSymbol);
        }

        userManager.getUserPortfolioManager(userId).updateHoldings(stockSymbol, qty);
    }

    public String placeOrder(String userId, String stockSymbol, OrderType orderType, Integer qty, double price){
        if(!userManager.isValidUser(userId)){
            throw new UserNotFoundException(userId);
        }

        if(!stockMap.containsKey(stockSymbol)){
            throw new StockNotFoundException(stockSymbol);
        }

        // Check if user can buy/sell
        final IUserPortfolioManager portfolioManager = userManager.getUserPortfolioManager(userId);
        final ReentrantLock userPortfolioLock = portfolioManager.getLock();
        try {
            userPortfolioLock.lock();
            if(orderType == OrderType.BUY){
                final double totalValue = price * qty;
                if(totalValue > portfolioManager.getBalance()){
                    throw new InsufficientFundsException();
                }
                // Reserve funds.
                portfolioManager.updateReserveBalance(totalValue);
                portfolioManager.updateBalance(totalValue * -1);
            }
            else if(orderType == OrderType.SELL){
                if(!portfolioManager.hasSufficientHoldings(stockSymbol, qty)){
                    throw new InsufficientHoldingsException();
                }
                portfolioManager.updateReserveHoldings(stockSymbol, qty);
                portfolioManager.updateHoldings(stockSymbol, qty * -1);
            }
        } finally {
            userPortfolioLock.unlock();
        }

        final String orderId = UUID.randomUUID().toString();
        final Order order = new Order(orderId, userId, orderType, stockSymbol, price, qty);
        orderMap.put(orderId, order);

        final List<Trade> trades = orderMatchingEngine.placeOrder(order);
        tradeExecutor.execute(trades);

        return orderId;
    }

    public void cancelOrder(final String orderId){
        if(!orderMap.containsKey(orderId)){
            throw new OrderNotFoundException(orderId);
        }

        final Order order = orderMap.get(orderId);
        try {
            order.writeLock.lock();
            if(order.status == OrderStatus.CANCELLED || order.status == OrderStatus.FILLED){
                throw new CannotCancelOrderException();
            }

            orderMap.get(orderId).status = OrderStatus.CANCELLED;
            System.out.println("Cancelled order " + orderId + "with pending quantity - " + order.quantity);
        } finally {
            order.writeLock.unlock();
        }

        // Return reserved funds/holdings
        final IUserPortfolioManager portfolioManager = userManager.getUserPortfolioManager(order.userId);
        try {
            portfolioManager.getLock().lock();
            if(order.type == OrderType.BUY){
                double totalOrderValue = order.price * order.quantity;
                portfolioManager.updateReserveBalance(totalOrderValue * -1);
                portfolioManager.updateBalance(totalOrderValue);
            } 
            else if(order.type == OrderType.SELL) {
                portfolioManager.updateReserveHoldings(order.stock, order.quantity * -1);
                portfolioManager.updateHoldings(order.stock, order.quantity);
            }
        } finally {
            portfolioManager.getLock().unlock();
        }
    }

    public UserPortfolio.UserPortfolioSnapshot getPortfolio(final String userId){
        if(!userManager.isValidUser(userId)){
            throw new UserNotFoundException(userId);
        }

        return userManager.getUserPortfolioManager(userId).getSnapshot();
    }

    public List<Trade> getTradeHistory(final String userId){
        if(!userManager.isValidUser(userId)){
            throw new UserNotFoundException(userId);
        }

        return userManager.getUserPortfolioManager(userId).getTradeList();
    }

    public OrderMatchingEngine.OrderBookSnapshot getOrderBook(final String stockSymbol){
        if(!stockMap.containsKey(stockSymbol)){
            throw new StockNotFoundException(stockSymbol);
        }

        return orderMatchingEngine.getStockOrderBookSnapshot(stockSymbol);
    }


    
}