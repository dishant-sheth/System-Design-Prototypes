R1
registerUser(userId, name, initialBalance) — create a user with a cash balance and empty portfolio.
Throw if userId already exists.
R2
addStock(stockSymbol, companyName, initialPrice) — register a tradeable stock on the exchange.
R3
addStockToPortfolio(userId, stockSymbol, qty) — seed a user's holdings directly (for setup/testing).
This is not a trade — no balance deducted, no order book involved.
R4
placeOrder(userId, stockSymbol, orderType, qty, price) → orderId
orderType = BUY or SELL. Validate: user exists, stock exists, sufficient balance for BUY, sufficient holdings for SELL. Add to order book. Attempt matching immediately.
R5
Matching logic — execute a trade when a new order arrives:
BUY price ≥ best SELL price → match. Execute at the SELL order's price (price-time priority). Update both portfolios and balances. Record the trade. Handle partial fills — an order can match in multiple smaller trades.
R6
cancelOrder(orderId) — remove a PENDING order from the order book.
Throw if order not found, already executed, or already cancelled.
R7
getPortfolio(userId) → { balance, holdings: Map<symbol, qty> }
R8
getTradeHistory(userId) → List<Trade> — all trades the user participated in, newest first.
R9
getOrderBook(stockSymbol) → { bids: List<Order>, asks: List<Order> } — current pending orders, bids descending by price, asks ascending.