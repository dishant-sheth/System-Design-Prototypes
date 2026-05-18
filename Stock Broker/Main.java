import java.util.List;
import src.StockBroker;
import src.exceptions.*;
import src.models.*;

/**
 * Test suite for Stock Broker / Order Matching Engine.
 * Reservation-aware: balance() and holdings() return AVAILABLE amounts.
 * Run: javac Main.java && java Main
 */
public class Main {

    static int passed = 0, failed = 0;

    static void group(String name) {
        System.out.println("\n" + name);
        System.out.println("─".repeat(52));
    }

    static void test(String name, Runnable r) {
        try {
            r.run();
            System.out.println("  ✓  " + name);
            passed++;
        } catch (AssertionError e) {
            System.out.println("  ✗  " + name + "\n       → " + e.getMessage());
            failed++;
        } catch (Exception e) {
            System.out.println("  ✗  " + name + "\n       → "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            failed++;
        }
    }

    static void eq(Object exp, Object act, String msg) {
        if (!exp.equals(act))
            throw new AssertionError(msg + " | expected=" + exp + " actual=" + act);
    }

    static void eqDouble(double exp, double act, String msg) {
        if (Math.abs(exp - act) > 0.001)
            throw new AssertionError(msg + " | expected=" + exp + " actual=" + act);
    }

    static void isTrue(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    static void throws_(Class<? extends Exception> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("Expected " + type.getSimpleName() + " but nothing thrown — " + msg);
        } catch (AssertionError ae) { throw ae; }
        catch (Exception e) {
            if (!type.isInstance(e))
                throw new AssertionError("Expected " + type.getSimpleName()
                        + " but got " + e.getClass().getSimpleName() + " — " + msg);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    static StockBroker fresh() { return new StockBroker(); }

    static StockBroker withSetup() {
        StockBroker sb = fresh();
        sb.registerUser("buyer",  "Alice", 10000);
        sb.registerUser("seller", "Bob",   10000);
        sb.addStock("INFY", "Infosys", 100);
        sb.addStockToPortfolio("seller", "INFY", 100);
        return sb;
    }

    // ── user management ───────────────────────────────────────────────────────

    static void testUserManagement() {
        group("user management");

        test("register user — portfolio created with initial balance", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 5000);
            eqDouble(5000, sb.getPortfolio("U1").balance(), "initial balance");
        });

        test("duplicate user registration throws", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 1000);
            throws_(UserAlreadyExistsException.class,
                    () -> sb.registerUser("U1", "Alice2", 2000),
                    "duplicate userId");
        });

        test("get portfolio for unknown user throws", () -> {
            throws_(UserNotFoundException.class,
                    () -> fresh().getPortfolio("GHOST"),
                    "unknown user");
        });

        test("register multiple users — balances are independent", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 1000);
            sb.registerUser("U2", "Bob",   2000);
            eqDouble(1000, sb.getPortfolio("U1").balance(), "U1 balance");
            eqDouble(2000, sb.getPortfolio("U2").balance(), "U2 balance");
        });
    }

    // ── stock management ──────────────────────────────────────────────────────

    static void testStockManagement() {
        group("stock management");

        test("addStock registers stock successfully", () -> {
            StockBroker sb = fresh();
            sb.addStock("INFY", "Infosys", 100);
            sb.registerUser("U1", "Alice", 1000);
            sb.addStockToPortfolio("U1", "INFY", 10);
            eq(10, sb.getPortfolio("U1").holdings().get("INFY"), "holdings after seed");
        });

        test("duplicate addStock is silently ignored", () -> {
            StockBroker sb = fresh();
            sb.addStock("INFY", "Infosys", 100);
            sb.addStock("INFY", "Infosys v2", 200);
            sb.registerUser("U1", "Alice", 10000);
            String oid = sb.placeOrder("U1", "INFY", OrderType.BUY, 1, 100);
            isTrue(oid != null && !oid.isEmpty(), "order placed on deduped stock");
        });

        test("placeOrder on unknown stock throws", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 1000);
            throws_(StockNotFoundException.class,
                    () -> sb.placeOrder("U1", "GHOST", OrderType.BUY, 1, 100),
                    "unknown stock");
        });

        test("addStockToPortfolio on unknown stock throws", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 1000);
            throws_(StockNotFoundException.class,
                    () -> sb.addStockToPortfolio("U1", "GHOST", 10),
                    "unknown stock");
        });
    }

    // ── order validation ──────────────────────────────────────────────────────

    static void testOrderValidation() {
        group("order validation");

        test("buy order with insufficient total balance throws", () -> {
            StockBroker sb = withSetup();
            throws_(InsufficientFundsException.class,
                    () -> sb.placeOrder("buyer", "INFY", OrderType.BUY, 1000, 100),
                    "total balance too low");
        });

        test("sell order with insufficient holdings throws", () -> {
            StockBroker sb = withSetup();
            throws_(InsufficientHoldingsException.class,
                    () -> sb.placeOrder("buyer", "INFY", OrderType.SELL, 1, 100),
                    "buyer has no INFY holdings");
        });

        test("placeOrder for unknown user throws", () -> {
            StockBroker sb = fresh();
            sb.addStock("INFY", "Infosys", 100);
            throws_(UserNotFoundException.class,
                    () -> sb.placeOrder("GHOST", "INFY", OrderType.BUY, 1, 100),
                    "unknown user");
        });

        test("placeOrder returns non-empty orderId", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 1, 100);
            isTrue(oid != null && !oid.isEmpty(), "orderId must be non-empty");
        });

        test("second BUY order fails when available balance exhausted by reservation", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("buyer", "INFY", OrderType.BUY, 60, 100); // reserves 6000, no match
            throws_(InsufficientFundsException.class,
                    () -> sb.placeOrder("buyer", "INFY", OrderType.BUY, 60, 100),
                    "second order must fail — available balance exhausted");
        });

        test("second SELL order fails when available holdings exhausted by reservation", () -> {
            StockBroker sb = withSetup(); // seller has 100 INFY
            sb.placeOrder("seller", "INFY", OrderType.SELL, 70, 200); // reserves 70, no match at 200
            throws_(InsufficientHoldingsException.class,
                    () -> sb.placeOrder("seller", "INFY", OrderType.SELL, 40, 200),
                    "second sell must fail — available holdings exhausted");
        });
    }

    // ── reservation lifecycle ─────────────────────────────────────────────────

    static void testReservations() {
        group("reservation lifecycle");

        test("unmatched BUY order reduces available balance", () -> {
            StockBroker sb = withSetup(); // buyer has 10000
            sb.placeOrder("buyer", "INFY", OrderType.BUY, 30, 200); // reserves 6000, no match at 200
            eqDouble(4000, sb.getPortfolio("buyer").balance(),
                    "available balance = 4000 while order pending");
        });

        test("unmatched SELL order reduces available holdings", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 60, 200); // no match at 200
            eq(40, sb.getPortfolio("seller").holdings().get("INFY"),
                    "available holdings = 40 while order pending");
        });

        test("cancelled BUY order releases reserved balance", () -> {
            StockBroker sb = withSetup();
            eqDouble(10000, sb.getPortfolio("buyer").balance(), "full balance before order");
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 30, 200); // reserves 6000, no match
            eqDouble(4000, sb.getPortfolio("buyer").balance(), "available = 4000 after reservation");
            sb.cancelOrder(oid);
            eqDouble(10000, sb.getPortfolio("buyer").balance(), "full balance restored after cancel");
        });

        test("cancelled SELL order releases reserved holdings", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("seller", "INFY", OrderType.SELL, 60, 200); // no match
            eq(40, sb.getPortfolio("seller").holdings().get("INFY"),
                    "available holdings = 40 after reservation");
            sb.cancelOrder(oid);
            eq(100, sb.getPortfolio("seller").holdings().get("INFY"),
                    "full holdings restored after cancel");
        });

        test("partial fill — remaining reservation holds until remainder filled or cancelled", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100); // 5 match, 5 pending
            // reserved 1000, consumed 500, 500 still reserved
            eqDouble(9000, sb.getPortfolio("buyer").balance(),
                    "available = 9000 — 500 consumed + 500 still reserved");
        });

        test("partial fill — cancel of remainder releases remaining reservation", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 100);
            String buyOid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 10, 100); // 5 match, 5 pending
            sb.cancelOrder(buyOid);
            // 500 consumed, 500 reservation released → available = 9500
            eqDouble(9500, sb.getPortfolio("buyer").balance(),
                    "available = 9500 after cancel of partial remainder");
        });
    }

    // ── exact match ───────────────────────────────────────────────────────────

    static void testExactMatch() {
        group("exact match — buy qty == sell qty");

        test("trade executes when buy price >= sell price", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(1, sb.getTradeHistory("buyer").size(),  "buyer should have 1 trade");
            eq(1, sb.getTradeHistory("seller").size(), "seller should have 1 trade");
        });

        test("buyer available balance deducted after match", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eqDouble(9000, sb.getPortfolio("buyer").balance(), "buyer available balance after buy");
        });

        test("seller available balance credited after match", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eqDouble(11000, sb.getPortfolio("seller").balance(), "seller balance after sell");
        });

        test("buyer available holdings increase after match", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(10, sb.getPortfolio("buyer").holdings().get("INFY"), "buyer holdings");
        });

        test("seller available holdings decrease after match", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(90, sb.getPortfolio("seller").holdings().get("INFY"), "seller available holdings");
        });

        test("seller portfolio updated — not buyer's portfolio twice", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eqDouble(11000, sb.getPortfolio("seller").balance(), "seller balance must be 11000");
        });

        test("trade executes at sell price — buyer refunded price difference", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10,  95);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            // reserved 1000, consumed 950, 50 released → available = 9050
            eqDouble(9050, sb.getPortfolio("buyer").balance(),
                    "buyer pays sell price 95 — excess reservation refunded");
        });

        test("no trade when buy price < sell price", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 110);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(0, sb.getTradeHistory("buyer").size(),  "no trades — prices don't cross");
            eq(0, sb.getTradeHistory("seller").size(), "no trades — prices don't cross");
        });

        test("order book contains unmatched orders", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 110);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            var snapshot = sb.getOrderBook("INFY");
            eq(1, snapshot.buyOrders().size(),  "1 pending buy order");
            eq(1, snapshot.sellOrders().size(), "1 pending sell order");
        });
    }

    // ── partial fills ─────────────────────────────────────────────────────────

    static void testPartialFills() {
        group("partial fills");

        test("partial fill — trade qty is filled qty not original order qty", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 20, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            List<Trade> trades = sb.getTradeHistory("buyer");
            eq(1, trades.size(), "exactly 1 trade");
            eq(10, trades.get(0).quantity(), "trade qty must be 10 (filled qty)");
        });

        test("partial fill — buyer fully filled, seller has remainder", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 20, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eqDouble(9000, sb.getPortfolio("buyer").balance(), "buyer pays for 10");
            eq(10, sb.getPortfolio("buyer").holdings().get("INFY"), "buyer gets 10");
        });

        test("partial fill — remaining sell order stays in book", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 20, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(1, sb.getOrderBook("INFY").sellOrders().size(), "1 remaining sell order in book");
        });

        test("partial fill — seller available holdings reflect partial consumption", () -> {
            StockBroker sb = withSetup(); // seller has 100 INFY
            sb.placeOrder("seller", "INFY", OrderType.SELL, 20, 100); // reserves 20
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100); // 10 consumed
            // available = total(100) - reserved_remaining(10) - consumed(10) = 80
            eq(80, sb.getPortfolio("seller").holdings().get("INFY"),
                    "seller available = 80 (10 consumed, 10 still reserved)");
        });

        test("partial fill — second buyer clears remaining sell order", () -> {
            StockBroker sb = withSetup();
            sb.registerUser("buyer2", "Carol", 10000);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 20, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            sb.placeOrder("buyer2", "INFY", OrderType.BUY,  10, 100);
            eq(1, sb.getTradeHistory("buyer2").size(), "buyer2 should have 1 trade");
            eq(0, sb.getOrderBook("INFY").sellOrders().size(), "sell order fully cleared");
        });

        test("buy order partially filled — buyer qty > seller qty", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5,  100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            // reserved 1000, consumed 500, 500 still reserved for remaining 5
            eqDouble(9000, sb.getPortfolio("buyer").balance(),
                    "available = 9000 (500 consumed + 500 still reserved)");
            eq(5, sb.getPortfolio("buyer").holdings().get("INFY"), "buyer gets 5");
            eq(1, sb.getOrderBook("INFY").buyOrders().size(), "remaining buy in book");
        });

        test("cascading fills — one large buy matched by multiple sells", () -> {
            StockBroker sb = fresh();
            sb.registerUser("buyer",   "Alice", 100000);
            sb.registerUser("seller1", "Bob",   10000);
            sb.registerUser("seller2", "Carol", 10000);
            sb.addStock("INFY", "Infosys", 100);
            sb.addStockToPortfolio("seller1", "INFY", 50);
            sb.addStockToPortfolio("seller2", "INFY", 50);
            sb.placeOrder("seller1", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("seller2", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",   "INFY", OrderType.BUY,  20, 100);
            eq(2, sb.getTradeHistory("buyer").size(), "buyer has 2 trades");
            eq(20, sb.getPortfolio("buyer").holdings().get("INFY"), "buyer gets 20 total");
            eqDouble(98000, sb.getPortfolio("buyer").balance(), "buyer pays 2000 total");
        });
    }

    // ── trade history ─────────────────────────────────────────────────────────

    static void testTradeHistory() {
        group("trade history");

        test("trade history newest first", () -> {
            StockBroker sb = fresh();
            sb.registerUser("buyer",  "Alice", 100000);
            sb.registerUser("seller", "Bob",   10000);
            sb.addStock("INFY", "Infosys", 100);
            sb.addStockToPortfolio("seller", "INFY", 100);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5,  90);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 110);
            List<Trade> history = sb.getTradeHistory("buyer");
            eq(2, history.size(), "2 trades in history");
            isTrue(history.get(0).tradeExecutedAt() >= history.get(1).tradeExecutedAt(),
                    "newest trade must be first");
        });

        test("both buyer and seller appear in each other's trade history", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(1, sb.getTradeHistory("buyer").size(),  "buyer has 1 trade");
            eq(1, sb.getTradeHistory("seller").size(), "seller has 1 trade");
        });

        test("no trades if orders don't match", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 150);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(0, sb.getTradeHistory("buyer").size(),  "no trades");
            eq(0, sb.getTradeHistory("seller").size(), "no trades");
        });

        test("getTradeHistory for unknown user throws", () -> {
            throws_(UserNotFoundException.class,
                    () -> fresh().getTradeHistory("GHOST"),
                    "unknown user");
        });
    }

    // ── order cancellation ────────────────────────────────────────────────────

    static void testCancellation() {
        group("order cancellation");

        test("cancel pending order — does not execute in future match", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 10, 100);
            sb.cancelOrder(oid);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            eq(0, sb.getTradeHistory("seller").size(), "cancelled order must not match");
            eq(0, sb.getTradeHistory("buyer").size(),  "cancelled buyer order must not execute");
        });

        test("cancel unknown orderId throws", () -> {
            throws_(OrderNotFoundException.class,
                    () -> fresh().cancelOrder("FAKE-ID"),
                    "unknown orderId");
        });

        test("cancel already-cancelled order throws", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 10, 100);
            sb.cancelOrder(oid);
            throws_(CannotCancelOrderException.class,
                    () -> sb.cancelOrder(oid),
                    "double cancel");
        });

        test("cancel filled order throws", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 10, 100);
            throws_(CannotCancelOrderException.class,
                    () -> sb.cancelOrder(oid),
                    "cancel filled order");
        });

        test("cancelled order skipped during matching — lazy deletion", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 10, 150);
            sb.cancelOrder(oid);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            eq(0, sb.getTradeHistory("seller").size(), "cancelled order not matched");
        });
    }

    // ── order book ────────────────────────────────────────────────────────────

    static void testOrderBook() {
        group("order book");

        test("getOrderBook for unknown stock throws", () -> {
            throws_(StockNotFoundException.class,
                    () -> fresh().getOrderBook("GHOST"),
                    "unknown stock");
        });

        test("buy orders in book sorted highest price first", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("buyer", "INFY", OrderType.BUY, 5,  90);
            sb.placeOrder("buyer", "INFY", OrderType.BUY, 5, 100);
            sb.placeOrder("buyer", "INFY", OrderType.BUY, 5,  95);
            var bids = sb.getOrderBook("INFY").buyOrders();
            eq(3, bids.size(), "3 buy orders");
            isTrue(bids.get(0).price >= bids.get(1).price, "bids descending by price");
        });

        test("sell orders in book sorted lowest price first", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 110);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 100);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 5, 105);
            var asks = sb.getOrderBook("INFY").sellOrders();
            eq(3, asks.size(), "3 sell orders");
            isTrue(asks.get(0).price <= asks.get(1).price, "asks ascending by price");
        });

        test("matched orders removed from book", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            var snapshot = sb.getOrderBook("INFY");
            eq(0, snapshot.buyOrders().size(),  "no pending buys");
            eq(0, snapshot.sellOrders().size(), "no pending sells");
        });
    }

    // ── portfolio ─────────────────────────────────────────────────────────────

    static void testPortfolio() {
        group("portfolio consistency");

        test("seeded holdings do not affect balance", () -> {
            StockBroker sb = withSetup();
            eqDouble(10000, sb.getPortfolio("seller").balance(),
                    "addStockToPortfolio must not change balance");
        });

        test("available balance never goes negative", () -> {
            StockBroker sb = withSetup();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            isTrue(sb.getPortfolio("buyer").balance() >= 0, "available balance must not go negative");
        });

        test("holdings snapshot is independent of internal state", () -> {
            StockBroker sb = withSetup();
            var snap1 = sb.getPortfolio("seller").holdings();
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);
            sb.placeOrder("buyer",  "INFY", OrderType.BUY,  10, 100);
            eq(100, snap1.get("INFY"),
                    "snapshot taken before trade must not reflect later mutations");
        });

        test("duplicate addStock does not overwrite existing order book", () -> {
            StockBroker sb = withSetup();
            String oid = sb.placeOrder("buyer", "INFY", OrderType.BUY, 5, 100);
            isTrue(oid != null, "order placed");
            sb.addStock("INFY", "Infosys v2", 200);
            eq(1, sb.getOrderBook("INFY").buyOrders().size(),
                    "pending order must survive duplicate addStock");
        });
    }

    // ── UserRepository ────────────────────────────────────────────────────────

    static void testUserRepository() {
        group("UserRepository internal correctness");

        test("initial balance is set correctly on registration", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 5000);
            eqDouble(5000, sb.getPortfolio("U1").balance(), "balance must be 5000");
        });

        test("findUser does not throw NullPointerException", () -> {
            StockBroker sb = fresh();
            sb.registerUser("U1", "Alice", 1000);
            isTrue(sb.getTradeHistory("U1") != null, "findUser must not NPE");
        });
    }

    // ── concurrency ───────────────────────────────────────────────────────────

    static void testConcurrency() {
        group("concurrency — multiple threads placing orders");

        test("10 buyers racing for 10 units — no oversell", () -> {
            StockBroker sb = fresh();
            for (int i = 0; i < 10; i++) {
                sb.registerUser("buyer" + i, "Buyer" + i, 10000);
            }
            sb.registerUser("seller", "Seller", 10000);
            sb.addStock("INFY", "Infosys", 100);
            sb.addStockToPortfolio("seller", "INFY", 10);
            sb.placeOrder("seller", "INFY", OrderType.SELL, 10, 100);

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    try { sb.placeOrder("buyer" + idx, "INFY", OrderType.BUY, 1, 100); }
                    catch (Exception ignored) {}
                });
            }
            for (Thread t : threads) t.start();
            for (Thread t : threads) { try { t.join(); } catch (InterruptedException ignored) {} }

            int totalBought = 0;
            for (int i = 0; i < 10; i++) {
                Integer h = sb.getPortfolio("buyer" + i).holdings().get("INFY");
                if (h != null) totalBought += h;
            }
            isTrue(totalBought <= 10, "total bought must not exceed 10 — no oversell");
        });

        test("concurrent orders on different stocks don't interfere", () -> {
            StockBroker sb = fresh();
            sb.registerUser("buyer",   "Alice", 100000);
            sb.registerUser("sellerA", "Bob",   10000);
            sb.registerUser("sellerB", "Carol", 10000);
            sb.addStock("INFY", "Infosys", 100);
            sb.addStock("TCS",  "TCS",     200);
            sb.addStockToPortfolio("sellerA", "INFY", 50);
            sb.addStockToPortfolio("sellerB", "TCS",  50);

            Thread t1 = new Thread(() -> {
                sb.placeOrder("sellerA", "INFY", OrderType.SELL, 10, 100);
                sb.placeOrder("buyer",   "INFY", OrderType.BUY,  10, 100);
            });
            Thread t2 = new Thread(() -> {
                sb.placeOrder("sellerB", "TCS", OrderType.SELL, 10, 200);
                sb.placeOrder("buyer",   "TCS", OrderType.BUY,  10, 200);
            });

            t1.start(); t2.start();
            try { t1.join(); t2.join(); } catch (InterruptedException ignored) {}

            isTrue(sb.getTradeHistory("buyer").size() >= 1,
                    "at least one trade executed concurrently");
        });
    }

    // ── main ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═".repeat(54));
        System.out.println("  Stock Broker — Test Suite");
        System.out.println("═".repeat(54));

        testUserManagement();
        testStockManagement();
        testOrderValidation();
        testReservations();
        testExactMatch();
        testPartialFills();
        testTradeHistory();
        testCancellation();
        testOrderBook();
        testPortfolio();
        testUserRepository();
        testConcurrency();  // run last

        System.out.println("\n" + "═".repeat(54));
        System.out.printf("  %d passed   %d failed   %d total%n",
                passed, failed, passed + failed);
        System.out.println("═".repeat(54));
        System.exit(failed > 0 ? 1 : 0);
    }
}