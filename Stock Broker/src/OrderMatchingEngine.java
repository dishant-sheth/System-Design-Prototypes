package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import src.models.Order;
import src.models.OrderStatus;
import src.models.OrderType;
import src.models.Trade;

public class OrderMatchingEngine {

    public record OrderBookSnapshot(
        String stockName,
        List<Order> buyOrders,
        List<Order> sellOrders
    ){}

    // Orders need to be matches per stock.
    final Map<String, PriorityQueue<Order>> buyOrderBook;
    final Map<String, PriorityQueue<Order>> sellOrderBook;
    final Map<String, ReentrantLock> stockLockMap;

    public OrderMatchingEngine(){
        this.buyOrderBook = new ConcurrentHashMap<>();
        this.sellOrderBook = new ConcurrentHashMap<>();
        this.stockLockMap = new ConcurrentHashMap<>();
    }

    public void addNewStock(String stockSymbol){

        PriorityQueue<Order> buyPq = new PriorityQueue<>((Order o1, Order o2) -> {
            if(o1.price == o2.price){
                return Long.compare(o1.orderPlacedAt, o2.orderPlacedAt);
            }
            return Double.compare(o2.price, o1.price);
        });

        PriorityQueue<Order> sellPq = new PriorityQueue<>((Order o1, Order o2) -> {
            if(o1.price == o2.price){
                return Long.compare(o1.orderPlacedAt, o2.orderPlacedAt);
            }
            return Double.compare(o1.price, o2.price);
        });

        this.buyOrderBook.putIfAbsent(stockSymbol, buyPq);
        this.sellOrderBook.putIfAbsent(stockSymbol, sellPq);
        this.stockLockMap.putIfAbsent(stockSymbol, new ReentrantLock());
    }

    public OrderBookSnapshot getStockOrderBookSnapshot(final String stockSymbol){
        final ReentrantLock stockLock = stockLockMap.get(stockSymbol);
        try {
            stockLock.lock();
            return new OrderBookSnapshot(stockSymbol, new ArrayList<>(buyOrderBook.get(stockSymbol)),  new ArrayList<>(sellOrderBook.get(stockSymbol)));
        } finally {
            stockLock.unlock();
        }
    }

    public List<Trade> placeOrder(Order order){
        List<Trade> trades = new ArrayList<>();

        ReentrantLock stockLock = stockLockMap.get(order.stock);

        try {
            stockLock.lock();
            if(order.type == OrderType.BUY){
                buyOrderBook.get(order.stock).offer(order);
            } else {
                sellOrderBook.get(order.stock).offer(order);
            }

            while(!buyOrderBook.get(order.stock).isEmpty() && !sellOrderBook.get(order.stock).isEmpty()){
                // Get top buy and sell order.
                Order topBuyOrder = buyOrderBook.get(order.stock).peek();
                Order topSellOrder = sellOrderBook.get(order.stock).peek();

                try {
                    topBuyOrder.writeLock.lock();
                    topSellOrder.writeLock.lock();

                    // Lazy remove cancelled orders.
                    if(topBuyOrder.status == OrderStatus.CANCELLED){
                        buyOrderBook.get(order.stock).poll();
                        continue;
                    }

                    if(topSellOrder.status == OrderStatus.CANCELLED){
                        sellOrderBook.get(order.stock).poll();
                        continue;
                    }

                    System.out.println(topBuyOrder);
                    System.out.println(topSellOrder);

                    // Price-match possible.
                    if(topBuyOrder.price >= topSellOrder.price){
                        // Check quantity match.
                        Trade trade = null;
                        if(topBuyOrder.quantity == topSellOrder.quantity){
                            // Execute trade.
                            topBuyOrder.status = OrderStatus.FILLED;
                            topSellOrder.status = OrderStatus.FILLED;
                            // Remove from queue.
                            buyOrderBook.get(order.stock).poll();
                            sellOrderBook.get(order.stock).poll();

                            trade = new Trade(
                                topBuyOrder.userId, topSellOrder.userId,
                                topBuyOrder.id, topSellOrder.id,
                                order.stock, topSellOrder.price, topBuyOrder.price, topBuyOrder.quantity,
                                System.currentTimeMillis());
                            //System.out.println(trade);

                        }
                        else if(topBuyOrder.quantity > topSellOrder.quantity){
                            // Sell order is completely fulfilled here.
                            topBuyOrder.status = OrderStatus.PARTIALLY_FILLED;
                            topSellOrder.status = OrderStatus.FILLED;
                            // Remove fully filled sell order.
                            sellOrderBook.get(order.stock).poll();

                            trade = new Trade(
                                topBuyOrder.userId, topSellOrder.userId,
                                topBuyOrder.id, topSellOrder.id,
                                order.stock, topSellOrder.price, topBuyOrder.price, topSellOrder.quantity,
                                System.currentTimeMillis()
                            );

                            topBuyOrder.quantity = topBuyOrder.quantity - topSellOrder.quantity;
                        }
                        else if(topSellOrder.quantity > topBuyOrder.quantity){
                            topBuyOrder.status = OrderStatus.FILLED;
                            topSellOrder.status = OrderStatus.PARTIALLY_FILLED;
                            // Remove fully filled buy order.
                            buyOrderBook.get(order.stock).poll();

                            trade = new Trade(
                                topBuyOrder.userId, topSellOrder.userId,
                                topBuyOrder.id, topSellOrder.id,
                                order.stock, topSellOrder.price, topBuyOrder.price, topBuyOrder.quantity,
                                System.currentTimeMillis()
                            );
                            topSellOrder.quantity = topSellOrder.quantity - topBuyOrder.quantity;
                        }

                        trades.add(trade);
                    }
                    else break;
                }
                finally {
                    topBuyOrder.writeLock.unlock();
                    topSellOrder.writeLock.unlock();
                }
                
            }
        } finally {
            stockLock.unlock();
        }
        return trades;
    }
    
}