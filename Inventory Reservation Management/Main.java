import src.InventoryManager;
import src.exceptions.*;
import src.models.Inventory;

/**
 * Test suite for Inventory Reservation Management system.
 * Covers: happy paths, error paths, edge cases, concurrency, and expiry.
 *
 * Run: javac InventoryTest.java && java InventoryTest
 */
public class Main {

    // ── tiny inline test runner (no external deps) ──────────────────────────
    static int passed = 0, failed = 0;

    static void group(String name) {
        System.out.println("\n" + name);
        System.out.println("─".repeat(50));
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

    static void isTrue(boolean c, String msg) {
        if (!c) throw new AssertionError(msg);
    }

    static void throws_(Class<? extends Exception> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("Expected " + type.getSimpleName() + " but nothing thrown — " + msg);
        } catch (AssertionError ae) {
            throw ae;
        } catch (Exception e) {
            if (!type.isInstance(e))
                throw new AssertionError("Expected " + type.getSimpleName()
                        + " but got " + e.getClass().getSimpleName() + " — " + msg);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    static InventoryManager fresh() {
        return new InventoryManager();
    }

    static InventoryManager withProduct(String id, String name, int qty) {
        InventoryManager im = fresh();
        im.addProduct(id, name, qty);
        return im;
    }

    // ── test groups ──────────────────────────────────────────────────────────

    static void testAddProduct() {
        group("addProduct");

        test("inventory reflects initial stock after addProduct", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            Inventory.QuantitySnapshot q = im.getInventory("P1");
            eq(10, q.getAvailableStock(), "available stock");
        });

        test("duplicate addProduct is silently ignored — stock unchanged", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            im.addProduct("P1", "Shirt v2", 50); // duplicate
            eq(10, im.getInventory("P1").getAvailableStock(), "stock should remain 10");
        });

        test("getInventory on unknown product throws ProductNotFoundException", () -> {
            throws_(ProductNotFoundException.class,
                    () -> fresh().getInventory("GHOST"),
                    "unknown product");
        });
    }

    static void testAddInventory() {
        group("addInventory (restock)");

        test("addInventory increases available stock", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            im.addInventory("P1", 10);
            eq(15, im.getInventory("P1").getAvailableStock(), "after restock");
        });

        test("addInventory on unknown product throws ProductNotFoundException", () -> {
            throws_(ProductNotFoundException.class,
                    () -> fresh().addInventory("GHOST", 5),
                    "unknown product");
        });
    }

    static void testReserve() {
        group("reserveInventory");

        test("reserve reduces available stock", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            im.reserveInventory("P1", 3, 60);
            eq(7, im.getInventory("P1").getAvailableStock(), "available after reserve");
        });

        test("reserve on unknown product throws", () -> {
            throws_(Exception.class,
                    () -> fresh().reserveInventory("GHOST", 1, 60),
                    "unknown product");
        });

        test("reserve more than available throws InsufficientInventory", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            throws_(Exception.class,
                    () -> im.reserveInventory("P1", 10, 60),
                    "over-reserve");
        });

        test("reserve exactly all available stock succeeds", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            String id = im.reserveInventory("P1", 5, 60);
            isTrue(id != null && !id.isEmpty(), "reservationId must be non-empty");
            eq(0, im.getInventory("P1").getAvailableStock(), "zero available after full reserve");
        });

        test("reserve returns unique IDs for successive calls", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String id1 = im.reserveInventory("P1", 1, 60);
            String id2 = im.reserveInventory("P1", 1, 60);
            isTrue(!id1.equals(id2), "reservation IDs must be unique");
        });

        // ── BUG #1 ──────────────────────────────────────────────────────────
        // InventoryManager passes `new Inventory()` to ReservationManager
        // instead of the same `inventory` instance. So reserveInventory()
        // checks & deducts from a DIFFERENT Inventory than getInventory() reads.
        // This test exposes the split-brain: available stock stays at 10
        // even after reservation.
        test("[BUG#1] reserve actually deducts from the inventory getInventory() reads", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            im.reserveInventory("P1", 3, 60);
            int avail = im.getInventory("P1").getAvailableStock();
            eq(7, avail, "available must drop to 7 after reserving 3 — BUG: two separate Inventory instances");
        });
    }

    static void testConfirm() {
        group("confirmReservation");

        test("confirm reduces reserved and increases consumed", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 3, 60);
            im.confirmReservation(rid);
            Inventory.QuantitySnapshot q = im.getInventory("P1");
            eq(7, q.getAvailableStock(), "available after confirm");
        });

        test("confirm with unknown reservationId throws", () -> {
            throws_(Exception.class,
                    () -> fresh().confirmReservation("FAKE-ID"),
                    "unknown reservationId");
        });

        test("confirm already-consumed reservation throws", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 1, 60);
            im.confirmReservation(rid);
            // ── BUG #3 ──────────────────────────────────────────────────────
            // confirmReservation catches ALL exceptions (including typed ones)
            // and re-wraps in CannotProcessReservationException, swallowing
            // the InvalidReservationActionException. The outer exception type
            // differs from what callers expect.
            throws_(Exception.class,
                    () -> im.confirmReservation(rid),
                    "double-confirm must throw");
        });

        test("confirm already-released reservation throws", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 1, 60);
            im.releaseReservation(rid);
            throws_(Exception.class,
                    () -> im.confirmReservation(rid),
                    "confirm after release must throw");
        });
    }

    static void testRelease() {
        group("releaseReservation");

        test("release restores available stock", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 3, 60);
            im.releaseReservation(rid);
            eq(10, im.getInventory("P1").getAvailableStock(), "available restored after release");
        });

        test("release with unknown reservationId throws", () -> {
            throws_(Exception.class,
                    () -> fresh().releaseReservation("FAKE-ID"),
                    "unknown reservationId");
        });

        test("release already-confirmed reservation throws", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 1, 60);
            im.confirmReservation(rid);
            throws_(Exception.class,
                    () -> im.releaseReservation(rid),
                    "release after confirm must throw");
        });

        test("double-release throws", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 1, 60);
            im.releaseReservation(rid);
            throws_(Exception.class,
                    () -> im.releaseReservation(rid),
                    "double-release must throw");
        });
    }

    static void testInventoryConsistency() {
        group("inventory consistency — available + reserved + consumed = total");

        test("invariant holds after reserve", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            im.reserveInventory("P1", 4, 60);
            Inventory.QuantitySnapshot q = im.getInventory("P1");
            eq(6, q.getAvailableStock(), "available=6");
        });

        test("invariant holds after confirm", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 4, 60);
            im.confirmReservation(rid);
            Inventory.QuantitySnapshot q = im.getInventory("P1");
            // 4 consumed, 0 reserved → 6 available
            eq(6, q.getAvailableStock(), "available=6 after confirm");
        });

        test("multiple independent reservations on same product", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String r1 = im.reserveInventory("P1", 3, 60);
            String r2 = im.reserveInventory("P1", 3, 60);
            eq(4, im.getInventory("P1").getAvailableStock(), "4 remaining after two reserves");
            im.confirmReservation(r1);
            im.releaseReservation(r2);
            eq(7, im.getInventory("P1").getAvailableStock(), "7 after confirm+release");
        });

        test("restock after reservations works correctly", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            im.reserveInventory("P1", 5, 60);
            eq(0, im.getInventory("P1").getAvailableStock(), "0 after full reserve");
            im.addInventory("P1", 10);
            eq(10, im.getInventory("P1").getAvailableStock(), "10 after restock");
        });
    }

    static void testExpiry() {
        group("reservation expiry (TTL)");

        // ── BUG #2 ──────────────────────────────────────────────────────────
        // ReservationExpiryJob has two problems:
        // 1. `kill` AtomicBoolean is declared but never initialized → NPE on construction
        // 2. The while(!reservationExpiryMap.isEmpty()) loop never removes processed
        //    entries from the TreeMap → infinite loop, Thread.sleep never reached
        // 3. When expiry does run, it sets state=EXPIRED but never calls
        //    inventoryRepository.returnInventory() → stock never released
        test("[BUG#2] expired reservation releases stock back to available", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            im.reserveInventory("P1", 5, 1); // 1-second TTL

            // Wait for BG thread to expire it
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

            int avail = im.getInventory("P1").getAvailableStock();
            eq(5, avail, "stock must be returned after TTL expiry — BUG: expiry thread has NPE + infinite loop + missing returnInventory()");
        });
    }

    static void testConcurrency() {
        group("concurrency — 10 threads racing for last 1 unit");

        test("at most 1 reservation succeeds when 10 threads race for qty=1", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 1);
            int[] successCount = {0};
            int[] failCount = {0};
            Object lock = new Object();

            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        im.reserveInventory("P1", 1, 60);
                        synchronized (lock) { successCount[0]++; }
                    } catch (Exception e) {
                        synchronized (lock) { failCount[0]++; }
                    }
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            eq(1, successCount[0], "exactly 1 thread should succeed");
            eq(9, failCount[0],    "exactly 9 threads should fail");
        });

        test("concurrent reservations for DIFFERENT products don't interfere", () -> {
            InventoryManager im = fresh();
            im.addProduct("P1", "Shirt", 5);
            im.addProduct("P2", "Pants", 5);

            int[] p1success = {0}, p2success = {0};
            Object lock = new Object();
            Thread[] threads = new Thread[10];

            for (int i = 0; i < 10; i++) {
                final int idx = i;
                threads[i] = new Thread(() -> {
                    try {
                        if (idx % 2 == 0) {
                            im.reserveInventory("P1", 1, 60);
                            synchronized (lock) { p1success[0]++; }
                        } else {
                            im.reserveInventory("P2", 1, 60);
                            synchronized (lock) { p2success[0]++; }
                        }
                    } catch (Exception ignored) {}
                });
            }

            for (Thread t : threads) t.start();
            for (Thread t : threads) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }

            isTrue(p1success[0] <= 5, "P1 success count must not exceed stock of 5");
            isTrue(p2success[0] <= 5, "P2 success count must not exceed stock of 5");
        });
    }

    static void testMultiProduct() {
        group("multi-product isolation");

        test("operations on P1 do not affect P2", () -> {
            InventoryManager im = fresh();
            im.addProduct("P1", "Shirt", 10);
            im.addProduct("P2", "Pants", 8);
            im.reserveInventory("P1", 5, 60);
            eq(5,  im.getInventory("P1").getAvailableStock(), "P1 reduced");
            eq(8,  im.getInventory("P2").getAvailableStock(), "P2 untouched");
        });
    }

    static void testConcurrencyDeep() {
        group("concurrency — simultaneous reserve + confirm + release + restock");

        // Scenario: 5 units, 20 threads each trying to reserve 1.
        // Exactly 5 should succeed, 15 should fail. No negative stock.
        test("20 threads racing for 5 units — exactly 5 succeed, stock never negative", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            int[] success = {0}, fail = {0};
            Object lock = new Object();
            Thread[] threads = new Thread[20];

            for (int i = 0; i < 20; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        im.reserveInventory("P1", 1, 60);
                        synchronized (lock) { success[0]++; }
                    } catch (Exception e) {
                        synchronized (lock) { fail[0]++; }
                    }
                });
            }
            for (Thread t : threads) t.start();
            for (Thread t : threads) { try { t.join(); } catch (InterruptedException ignored) {} }

            eq(5,  success[0], "exactly 5 reservations must succeed");
            eq(15, fail[0],    "exactly 15 must fail");
            eq(0,  im.getInventory("P1").getAvailableStock(), "available must be 0 — not negative");
        });

        // Scenario: reserve then concurrently confirm AND release the same reservation.
        // Exactly one of confirm/release must win; the other must throw.
        // After the race, stock must be consistent (either 9 consumed or 10 available).
        test("concurrent confirm + release on same reservation — only one wins", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 10);
            String rid = im.reserveInventory("P1", 1, 60);

            int[] confirmWin = {0}, releaseWin = {0}, exceptionCount = {0};
            Object lock = new Object();

            Thread confirmer = new Thread(() -> {
                try {
                    im.confirmReservation(rid);
                    synchronized (lock) { confirmWin[0]++; }
                } catch (Exception e) {
                    synchronized (lock) { exceptionCount[0]++; }
                }
            });

            Thread releaser = new Thread(() -> {
                try {
                    im.releaseReservation(rid);
                    synchronized (lock) { releaseWin[0]++; }
                } catch (Exception e) {
                    synchronized (lock) { exceptionCount[0]++; }
                }
            });

            confirmer.start(); releaser.start();
            try { confirmer.join(); releaser.join(); } catch (InterruptedException ignored) {}

            int totalWins = confirmWin[0] + releaseWin[0];
            eq(1, totalWins,       "exactly one of confirm/release must win");
            eq(1, exceptionCount[0], "the other must throw");

            // Stock must be coherent regardless of who won
            int avail = im.getInventory("P1").getAvailableStock();
            isTrue(avail == 9 || avail == 10,
                "available must be 9 (confirm won) or 10 (release won), got " + avail);
        });

        // Scenario: concurrent restock while threads are reserving.
        // Total stock added = 5 initial + 5 restocked = 10.
        // 10 threads each try to reserve 1. All 10 should succeed.
        test("restock mid-flight — concurrent addInventory + reserve", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);
            int[] success = {0}, fail = {0};
            Object lock = new Object();
            Thread[] threads = new Thread[10];

            // Restock thread fires at the same time as reservations
            Thread restockThread = new Thread(() -> im.addInventory("P1", 5));

            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        im.reserveInventory("P1", 1, 60);
                        synchronized (lock) { success[0]++; }
                    } catch (Exception e) {
                        synchronized (lock) { fail[0]++; }
                    }
                });
            }

            restockThread.start();
            for (Thread t : threads) t.start();
            try { restockThread.join(); } catch (InterruptedException ignored) {}
            for (Thread t : threads) { try { t.join(); } catch (InterruptedException ignored) {} }

            // Total stock = 10, each thread wants 1 → all 10 should succeed
            eq(10, success[0], "all 10 reservations must succeed with restocked inventory");
            eq(0,  fail[0],    "no failures expected");
            eq(0,  im.getInventory("P1").getAvailableStock(), "available must be 0 after all reserved");
        });

        // Scenario: reserve → confirm frees nothing (consumed), but
        // reserve → release returns stock. 5 threads reserve, 5 release,
        // 5 more threads then try to reserve the released stock — all should succeed.
        test("released stock is immediately available for re-reservation", () -> {
            InventoryManager im = withProduct("P1", "Shirt", 5);

            // Phase 1: 5 threads reserve
            String[] ids = new String[5];
            for (int i = 0; i < 5; i++) {
                ids[i] = im.reserveInventory("P1", 1, 60);
            }
            eq(0, im.getInventory("P1").getAvailableStock(), "0 available after 5 reserves");

            // Phase 2: all 5 release concurrently
            Thread[] releasers = new Thread[5];
            for (int i = 0; i < 5; i++) {
                final String rid = ids[i];
                releasers[i] = new Thread(() -> im.releaseReservation(rid));
            }
            for (Thread t : releasers) t.start();
            for (Thread t : releasers) { try { t.join(); } catch (InterruptedException ignored) {} }

            eq(5, im.getInventory("P1").getAvailableStock(), "5 available after all released");

            // Phase 3: 5 new threads re-reserve — all should succeed
            int[] success = {0};
            Object lock = new Object();
            Thread[] reReservers = new Thread[5];
            for (int i = 0; i < 5; i++) {
                reReservers[i] = new Thread(() -> {
                    try {
                        im.reserveInventory("P1", 1, 60);
                        synchronized (lock) { success[0]++; }
                    } catch (Exception ignored) {}
                });
            }
            for (Thread t : reReservers) t.start();
            for (Thread t : reReservers) { try { t.join(); } catch (InterruptedException ignored) {} }

            eq(5, success[0], "all 5 re-reservations must succeed on released stock");
        });
    }

    // ── main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═".repeat(52));
        System.out.println("  Inventory Reservation Management — Test Suite");
        System.out.println("═".repeat(52));

        testAddProduct();
        testAddInventory();
        testReserve();
        testConfirm();
        testRelease();
        testInventoryConsistency();
        testConcurrency();
        testConcurrencyDeep();
        testMultiProduct();
        testExpiry(); // run last — has a Thread.sleep(3s)

        System.out.println("\n" + "═".repeat(52));
        System.out.printf("  %d passed   %d failed   %d total%n", passed, failed, passed + failed);
        System.out.println("═".repeat(52));
        System.exit(failed > 0 ? 1 : 0);
    }
}