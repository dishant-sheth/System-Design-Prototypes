package src;

import java.util.List;
import src.interfaces.IUserManager;
import src.interfaces.IUserPortfolioManager;
import src.models.Trade;

public class TradeExecutor {

    private final IUserManager userManager;

    public TradeExecutor(IUserManager userManager){
        this.userManager = userManager;
    }
    
    public void execute(final List<Trade> trades){
        for(final Trade trade: trades){
            final String buyerId = trade.buyerUserId();
            final String sellerId = trade.sellerUserId();
            if(userManager.isValidUser(buyerId) && userManager.isValidUser(sellerId)){
                System.out.println("Executing trade with buyer & seller ID -> " + trade.buyerUserId() + ", " + trade.sellerUserId());
                final IUserPortfolioManager buyerPortfolioManager = userManager.getUserPortfolioManager(buyerId);
                final IUserPortfolioManager sellerPortfolioManager = userManager.getUserPortfolioManager(sellerId);

                try {
                    buyerPortfolioManager.getLock().lock();
                    sellerPortfolioManager.getLock().lock();
                    // Process balances.
                    final double reservedCost = trade.buyerOfferedPrice() * trade.quantity();
                    final double tradeCost = trade.price() * trade.quantity();
                    System.out.println("Trade Total Consideration -> " + tradeCost);
                    buyerPortfolioManager.updateReserveBalance(tradeCost * -1);
                    buyerPortfolioManager.updateBalance(reservedCost - tradeCost);
                    sellerPortfolioManager.updateBalance(tradeCost);

                    // Process holdings.
                    buyerPortfolioManager.updateHoldings(trade.stock(), trade.quantity());
                    sellerPortfolioManager.updateReserveHoldings(trade.stock(), trade.quantity() * -1);

                    // Updates records.
                    buyerPortfolioManager.recordTrade(trade);
                    sellerPortfolioManager.recordTrade(trade);
                } finally {
                    buyerPortfolioManager.getLock().unlock();
                    sellerPortfolioManager.getLock().unlock();
                }
            }
        }
    }

}