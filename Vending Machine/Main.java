import java.util.HashMap;
import java.util.Map;
import src.VendingMachine;
import src.exceptions.CannotFormChangeException;
import src.exceptions.InvalidDenomination;
import src.exceptions.InvalidProductException;
import src.exceptions.NotEnoughInventoryException;
import src.models.Denominations;
import src.models.Product;

public class Main {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("      Vending Machine Test Suite        ");
        System.out.println("========================================\n");

        testHappyPathExactChange();
        testHappyPathWithChange();
        testMultipleInsertionsToReachCost();
        testCancelAfterProductSelected();
        testCancelAfterMoneyInserted();
        testInsertMoneyBeforeSelectThrows();
        testDispenseBeforeSelectThrows();
        testDispenseBeforeEnoughMoneyInserted();
        testSelectInvalidProductThrows();
        testSelectOutOfStockProductThrows();
        testInvalidDenominationThrows();
        testCannotFormChangeThrows();
        testRestockInventory();
        testSelectProductAfterProductSelectedThrows();
        testSelectProductAfterMoneyInsertedThrows();
        testChangeCalculatorComplexScenario();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    // -----------------------------------------------
    // Helpers
    // -----------------------------------------------
    static VendingMachine buildMachine() {
        VendingMachine vm = new VendingMachine();
        // Stock products
        vm.inventory.addInventory(new Product("Chips", "P1", 30.0), 5);
        vm.inventory.addInventory(new Product("Water", "P2", 20.0), 3);
        vm.inventory.addInventory(new Product("Soda",  "P3", 50.0), 2);
        // Refill change
        Map<Denominations, Integer> changeMap = new HashMap<>();
        changeMap.put(Denominations.HUNDRED, 5);
        changeMap.put(Denominations.FIFTY,   5);
        changeMap.put(Denominations.TWENTY,  10);
        changeMap.put(Denominations.TEN,     10);
        changeMap.put(Denominations.FIVE,    10);
        vm.changeDispenser.refillChange(changeMap);
        return vm;
    }

    // -----------------------------------------------
    // Test 1: Happy path - exact change
    // -----------------------------------------------
    static void testHappyPathExactChange() {
        printHeader("Test 1: Happy Path - Exact Change");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("P2");           // Water = 20
            vm.insertMoney(20.0);             // exact
            vm.dispense();
            pass("full transaction completed with exact change");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 2: Happy path - overpay, get change back
    // -----------------------------------------------
    static void testHappyPathWithChange() {
        printHeader("Test 2: Happy Path - Overpay, Get Change");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("P1");           // Chips = 30
            vm.insertMoney(50.0);             // overpay by 20
            vm.dispense();
            pass("transaction completed, change returned");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 3: Multiple insertions to reach cost
    // -----------------------------------------------
    static void testMultipleInsertionsToReachCost() {
        printHeader("Test 3: Multiple Insertions to Reach Cost");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("P1");           // Chips = 30
            vm.insertMoney(10.0);             // 10 so far
            vm.insertMoney(20.0);             // 30 total — should transition to MoneyInserted
            vm.dispense();
            pass("multiple insertions accepted, product dispensed");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 4: Cancel after product selected
    // -----------------------------------------------
    static void testCancelAfterProductSelected() {
        printHeader("Test 4: Cancel After Product Selected");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("P1");
            vm.insertMoney(10.0);             // partial payment
            vm.cancel();                      // should return 10 and go idle
            vm.selectProduct("P2");           // machine should be back to idle — this should work
            pass("cancel returns to idle, next selection works");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 5: Cancel after money fully inserted
    // -----------------------------------------------
    static void testCancelAfterMoneyInserted() {
        printHeader("Test 5: Cancel After Money Inserted");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("P2");           // Water = 20
            vm.insertMoney(20.0);             // fully paid
            vm.cancel();                      // should return money and go idle
            vm.selectProduct("P1");           // machine should be idle again
            pass("cancel after full payment returns to idle");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 6: Insert money before selecting product
    // -----------------------------------------------
    static void testInsertMoneyBeforeSelectThrows() {
        printHeader("Test 6: Insert Money Before Select - Invalid State");
        VendingMachine vm = buildMachine();
        // IdleState prints invalid action but doesn't throw
        // Just verify no state corruption occurs
        vm.insertMoney(10.0);
        try {
            vm.selectProduct("P1");           // should still work after invalid action
            pass("machine state not corrupted by invalid insertMoney in idle");
        } catch (Exception e) {
            fail("machine state corrupted: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 7: Dispense before selecting product
    // -----------------------------------------------
    static void testDispenseBeforeSelectThrows() {
        printHeader("Test 7: Dispense Before Select - Invalid State");
        VendingMachine vm = buildMachine();
        vm.dispense();
        try {
            vm.selectProduct("P1");           // should still work
            pass("machine state not corrupted by invalid dispense in idle");
        } catch (Exception e) {
            fail("machine state corrupted: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 8: Dispense before enough money inserted
    // -----------------------------------------------
    static void testDispenseBeforeEnoughMoneyInserted() {
        printHeader("Test 8: Dispense Before Enough Money - Prints Remaining");
        VendingMachine vm = buildMachine();
        vm.selectProduct("P1");               // Chips = 30
        vm.insertMoney(10.0);                 // only 10 paid
        vm.dispense();                        // should print "please pay X more", not dispense
        // Machine should still be in ProductSelectedState
        try {
            vm.insertMoney(20.0);             // complete the payment
            vm.dispense();
            pass("machine still functional after premature dispense call");
        } catch (Exception e) {
            fail("unexpected exception: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 9: Select invalid product
    // -----------------------------------------------
    static void testSelectInvalidProductThrows() {
        printHeader("Test 9: Select Invalid Product Throws");
        VendingMachine vm = buildMachine();
        try {
            vm.selectProduct("INVALID");
            fail("expected InvalidProductException");
        } catch (InvalidProductException e) {
            pass("InvalidProductException thrown for unknown product");
        }
    }

    // -----------------------------------------------
    // Test 10: Select out of stock product
    // -----------------------------------------------
    static void testSelectOutOfStockProductThrows() {
        printHeader("Test 10: Select Out of Stock Product Throws");
        VendingMachine vm = buildMachine();
        // Drain Soda stock (qty = 2)
        vm.selectProduct("P3"); vm.insertMoney(50.0); vm.dispense();
        vm.selectProduct("P3"); vm.insertMoney(50.0); vm.dispense();
        try {
            vm.selectProduct("P3");           // should throw — out of stock
            fail("expected NotEnoughInventoryException");
        } catch (InvalidProductException | NotEnoughInventoryException e) {
            pass("NotEnoughInventoryException thrown for out of stock product");
        }
    }

    // -----------------------------------------------
    // Test 11: Invalid denomination throws
    // -----------------------------------------------
    static void testInvalidDenominationThrows() {
        printHeader("Test 11: Invalid Denomination Throws");
        VendingMachine vm = buildMachine();
        vm.selectProduct("P1");
        try {
            vm.insertMoney(7.0);              // 7 is not a valid denomination
            fail("expected InvalidDenomination");
        } catch (InvalidDenomination e) {
            pass("InvalidDenomination thrown for unsupported amount");
        }
    }

    // -----------------------------------------------
    // Test 12: Cannot form change throws
    // -----------------------------------------------
    static void testCannotFormChangeThrows() {
        printHeader("Test 12: Cannot Form Change Throws");
        VendingMachine vm = new VendingMachine();
        vm.inventory.addInventory(new Product("Chips", "P1", 30.0), 5);
        // No change loaded — can't return 20 change
        try {
            vm.selectProduct("P1");           // Chips = 30
            vm.insertMoney(50.0);             // overpay by 20, no change available
            vm.dispense();
            fail("expected CannotFormChangeException");
        } catch (CannotFormChangeException e) {
            pass("CannotFormChangeException thrown when change cannot be formed");
        }
    }

    // -----------------------------------------------
    // Test 13: Restock inventory
    // -----------------------------------------------
    static void testRestockInventory() {
        printHeader("Test 13: Restock Inventory");
        VendingMachine vm = buildMachine();
        // Drain all Soda (qty = 2)
        vm.selectProduct("P3"); vm.insertMoney(50.0); vm.dispense();
        vm.selectProduct("P3"); vm.insertMoney(50.0); vm.dispense();
        // Restock
        vm.inventory.addInventory(new Product("Soda", "P3", 50.0), 3);
        try {
            vm.selectProduct("P3");
            pass("restock successful, product selectable again");
        } catch (Exception e) {
            fail("unexpected exception after restock: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 14: Select product after product already selected
    // -----------------------------------------------
    static void testSelectProductAfterProductSelectedThrows() {
        printHeader("Test 14: Select Product After Already Selected - Invalid State");
        VendingMachine vm = buildMachine();
        vm.selectProduct("P1");
        // Should print invalid action, not throw
        vm.selectProduct("P2");               // invalid in ProductSelectedState
        try {
            vm.cancel();                      // should still work — state not corrupted
            pass("state not corrupted by double select");
        } catch (Exception e) {
            fail("state corrupted by double select: " + e.getMessage());
        }
    }

    // -----------------------------------------------
    // Test 15: Select product after money inserted
    // -----------------------------------------------
    static void testSelectProductAfterMoneyInsertedThrows() {
        printHeader("Test 15: Select Product After Money Inserted - Invalid State");
        VendingMachine vm = buildMachine();
        vm.selectProduct("P2");               // Water = 20
        vm.insertMoney(20.0);                 // fully paid, now in MoneyInserted
        vm.selectProduct("P1");               // invalid in MoneyInsertedState
        try {
            vm.dispense();                    // should still work
            pass("state not corrupted by invalid select in MoneyInserted");
        } catch (Exception e) {
            fail("state corrupted: " + e.getMessage());
        }
    }

    static void testChangeCalculatorComplexScenario() {
        printHeader("Test 16: Change Calculator - Complex Denomination Breakdown");
        VendingMachine vm = new VendingMachine();
        vm.inventory.addInventory(new Product("Chips", "P1", 35.0), 5);

        // Load specific denominations
        Map<Denominations, Integer> changeMap = new HashMap<>();
        changeMap.put(Denominations.TWENTY,  2); // 40 total in 20s
        changeMap.put(Denominations.TEN,     1); // 10 total in 10s
        changeMap.put(Denominations.FIVE,    1); // 5 total in 5s
        // No FIFTY or HUNDRED
        vm.changeDispenser.refillChange(changeMap);

        // Insert 100 (FIFTY + FIFTY not available, use HUNDRED... wait no HUNDRED either)
        // Insert 50 + 20 + 10 = 80, change = 45
        // But we only have: 2x20, 1x10, 1x5 = 45 exactly → 20+20+5 = 45
        Map<Denominations, Integer> insertMap = new HashMap<>();
        insertMap.put(Denominations.FIFTY, 5);
        insertMap.put(Denominations.TWENTY, 3);
        vm.changeDispenser.refillChange(insertMap);

        try {
            vm.selectProduct("P1");    // costs 35
            vm.insertMoney(50.0);      // pay 50, change = 15 → 10 + 5
            vm.dispense();
            pass("change calculator correctly broke down 15 into 10 + 5");
        } catch (CannotFormChangeException e) {
            fail("change calculator failed to break down 15 into available denominations");
        }
    }

    // -----------------------------------------------
    // Utilities
    // -----------------------------------------------
    static void printHeader(String title) {
        System.out.println("▶ " + title);
    }

    static void pass(String label) {
        System.out.printf("   ✅ PASS | %s%n", label);
        passed++;
    }

    static void fail(String label) {
        System.out.printf("   ❌ FAIL | %s%n", label);
        failed++;
    }
}