import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import src.HourBasedFeeStrategy;
import src.ParkingLot;
import src.models.ParkingFloor;
import src.models.ParkingSpotType;
import src.models.Ticket;
import src.models.Vehicle;
import src.models.VehicleType;

public class ParkingLotTest {

    static int passed = 0;
    static int failed = 0;

    public void main() throws InterruptedException {
        System.out.println("========================================");
        System.out.println("       Parking Lot Test Suite           ");
        System.out.println("========================================\n");

        testBikeParkesInSmallSpot();
        testCarParkesInMediumSpot();
        testTruckParkesInLargeSpot();
        testBikeFallsBackToMediumSpot();
        testBikeFallsBackToLargeSpot();
        testUnparkReturnsSpotToPool();
        testUnparkOnInvalidTicketThrows();
        testFeeCalculation();
        testParkingLotFull();
        testMultipleVehiclesSameType();
        testTicketIsActiveOnPark();
        testTicketIsInactiveAfterUnpark();
        System.out.println("==== CONCURRENCY TEST ====");
        testConcurrentParkSameSpotType();
        testConcurrentParkDifferentSpotTypes();
        testConcurrentParkAndUnpark();
        testConcurrentUnparkSameTicketThrows();
        testConcurrentLotFullException();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    // -----------------------------------------------
    // Helpers
    // -----------------------------------------------
    static ParkingLot buildLot(int numFloors, int small, int medium, int large) {
        Map<ParkingSpotType, Integer> spotMap = new HashMap<>();
        if (small > 0)  spotMap.put(ParkingSpotType.SMALL, small);
        if (medium > 0) spotMap.put(ParkingSpotType.MEDIUM, medium);
        if (large > 0)  spotMap.put(ParkingSpotType.LARGE, large);

        Map<ParkingSpotType, BigDecimal> costMap = new HashMap<>();
        costMap.put(ParkingSpotType.SMALL,  new BigDecimal(10));
        costMap.put(ParkingSpotType.MEDIUM, new BigDecimal(25));
        costMap.put(ParkingSpotType.LARGE,  new BigDecimal(50));

        List<ParkingFloor> floors = new ArrayList<>();
        for (int i = 0; i < numFloors; i++) {
            floors.add(ParkingFloor.Builder.getInstance()
                .withFloorNum(i)
                .withSpots(spotMap)
                .build());
        }
        return ParkingLot.Builder.getInstance()
            .withFloors(floors)
            .withFeeCalculatorStrategy(new HourBasedFeeStrategy(costMap))
            .build();
    }

    static Vehicle bike(String id)  { return new Vehicle(id, VehicleType.BIKE); }
    static Vehicle car(String id)   { return new Vehicle(id, VehicleType.CAR); }
    static Vehicle truck(String id) { return new Vehicle(id, VehicleType.TRUCK); }

    // -----------------------------------------------
    // Test 1: Bike parks in small spot
    // -----------------------------------------------
    static void testBikeParkesInSmallSpot() {
        printHeader("Test 1: Bike Parks in Small Spot");
        ParkingLot lot = buildLot(1, 5, 5, 5);
        Ticket ticket = lot.park(bike("B1"));
        assertEqual("spot type is SMALL", ParkingSpotType.SMALL, ticket.getParkingSpot().getParkingSpotType());
    }

    // -----------------------------------------------
    // Test 2: Car parks in medium spot
    // -----------------------------------------------
    static void testCarParkesInMediumSpot() {
        printHeader("Test 2: Car Parks in Medium Spot");
        ParkingLot lot = buildLot(1, 0, 5, 5);
        Ticket ticket = lot.park(car("C1"));
        assertEqual("spot type is MEDIUM", ParkingSpotType.MEDIUM, ticket.getParkingSpot().getParkingSpotType());
    }

    // -----------------------------------------------
    // Test 3: Truck parks in large spot
    // -----------------------------------------------
    static void testTruckParkesInLargeSpot() {
        printHeader("Test 3: Truck Parks in Large Spot");
        ParkingLot lot = buildLot(1, 0, 0, 5);
        Ticket ticket = lot.park(truck("T1"));
        assertEqual("spot type is LARGE", ParkingSpotType.LARGE, ticket.getParkingSpot().getParkingSpotType());
    }

    // -----------------------------------------------
    // Test 4: Bike falls back to medium when no small available
    // -----------------------------------------------
    static void testBikeFallsBackToMediumSpot() {
        printHeader("Test 4: Bike Falls Back to Medium Spot");
        ParkingLot lot = buildLot(1, 0, 5, 5); // no small spots
        Ticket ticket = lot.park(bike("B2"));
        assertEqual("spot type is MEDIUM (fallback)", ParkingSpotType.MEDIUM, ticket.getParkingSpot().getParkingSpotType());
    }

    // -----------------------------------------------
    // Test 5: Bike falls back to large when no small/medium available
    // -----------------------------------------------
    static void testBikeFallsBackToLargeSpot() {
        printHeader("Test 5: Bike Falls Back to Large Spot");
        ParkingLot lot = buildLot(1, 0, 0, 5); // only large spots
        Ticket ticket = lot.park(bike("B3"));
        assertEqual("spot type is LARGE (fallback)", ParkingSpotType.LARGE, ticket.getParkingSpot().getParkingSpotType());
    }

    // -----------------------------------------------
    // Test 6: Unpark returns spot to pool
    // -----------------------------------------------
    static void testUnparkReturnsSpotToPool() {
        printHeader("Test 6: Unpark Returns Spot to Pool");
        ParkingLot lot = buildLot(1, 1, 0, 0); // only 1 small spot
        Ticket t1 = lot.park(bike("B4"));
        lot.unpark(t1);
        // Spot should be back — park again without exception
        try {
            Ticket t2 = lot.park(bike("B5"));
            assertEqual("spot reused after unpark", ParkingSpotType.SMALL, t2.getParkingSpot().getParkingSpotType());
        } catch (Exception e) {
            fail("spot not returned to pool after unpark");
        }
    }

    // -----------------------------------------------
    // Test 7: Unpark on already-closed ticket throws
    // -----------------------------------------------
    static void testUnparkOnInvalidTicketThrows() {
        printHeader("Test 7: Unpark on Invalid Ticket Throws");
        ParkingLot lot = buildLot(1, 5, 5, 5);
        Ticket ticket = lot.park(car("C2"));
        lot.unpark(ticket);
        try {
            lot.unpark(ticket); // second unpark should throw
            fail("expected InvalidTicketException");
        } catch (ParkingLot.InvalidTicketException e) {
            pass("InvalidTicketException thrown on double unpark");
        }
    }

    // -----------------------------------------------
    // Test 8: Fee calculation (minute based)
    // -----------------------------------------------
    static void testFeeCalculation() throws InterruptedException {
        printHeader("Test 8: Fee Calculation - Ceiling Rounds Up to 1 Hour");
        ParkingLot lot = buildLot(1, 0, 5, 0); // medium spots, 25/hr
        Ticket ticket = lot.park(car("C3"));
        Thread.sleep(10000);
        lot.unpark(ticket); // immediate unpark — ceil gives 1 hour
        BigDecimal fee = ticket.getParkingSpot().getParkingSpotType() == ParkingSpotType.MEDIUM
            ? new BigDecimal(25) : null;
        // Directly test the strategy instead
        Map<ParkingSpotType, BigDecimal> costMap = new HashMap<>();
        costMap.put(ParkingSpotType.SMALL,  new BigDecimal(10));
        costMap.put(ParkingSpotType.MEDIUM, new BigDecimal(25));
        costMap.put(ParkingSpotType.LARGE,  new BigDecimal(50));
        HourBasedFeeStrategy strategy = new HourBasedFeeStrategy(costMap);
        BigDecimal calculatedFee = strategy.calculate(ticket);
        assertEqual("immediate unpark charges minimum 1 hour = 25", new BigDecimal(25), calculatedFee);
    }

    // -----------------------------------------------
    // Test 9: Parking lot full throws exception
    // -----------------------------------------------
    static void testParkingLotFull() {
        printHeader("Test 9: Parking Lot Full Throws Exception");
        ParkingLot lot = buildLot(1, 1, 0, 0); // only 1 spot
        lot.park(bike("B6")); // fills the only spot
        try {
            lot.park(bike("B7")); // should throw
            fail("expected ParkingLotFullException");
        } catch (ParkingLot.ParkingLotFullException e) {
            pass("ParkingLotFullException thrown when lot is full");
        }
    }

    // -----------------------------------------------
    // Test 10: Multiple vehicles of same type
    // -----------------------------------------------
    static void testMultipleVehiclesSameType() {
        printHeader("Test 10: Multiple Vehicles Same Type");
        ParkingLot lot = buildLot(1, 3, 0, 0);
        Ticket t1 = lot.park(bike("B8"));
        Ticket t2 = lot.park(bike("B9"));
        Ticket t3 = lot.park(bike("B10"));
        assertEqual("t1 spot is SMALL", ParkingSpotType.SMALL, t1.getParkingSpot().getParkingSpotType());
        assertEqual("t2 spot is SMALL", ParkingSpotType.SMALL, t2.getParkingSpot().getParkingSpotType());
        assertEqual("t3 spot is SMALL", ParkingSpotType.SMALL, t3.getParkingSpot().getParkingSpotType());
        assertEqual("all spots are different", true,
            !t1.getParkingSpot().getId().equals(t2.getParkingSpot().getId()) &&
            !t2.getParkingSpot().getId().equals(t3.getParkingSpot().getId()));
    }

    // -----------------------------------------------
    // Test 11: Ticket is active after park
    // -----------------------------------------------
    static void testTicketIsActiveOnPark() {
        printHeader("Test 11: Ticket is Active After Park");
        ParkingLot lot = buildLot(1, 5, 5, 5);
        Ticket ticket = lot.park(car("C4"));
        assertEqual("ticket is active", true, ticket.isActive());
    }

    // -----------------------------------------------
    // Test 12: Ticket is inactive after unpark
    // -----------------------------------------------
    static void testTicketIsInactiveAfterUnpark() {
        printHeader("Test 12: Ticket is Inactive After Unpark");
        ParkingLot lot = buildLot(1, 5, 5, 5);
        Ticket ticket = lot.park(car("C5"));
        lot.unpark(ticket);
        assertEqual("ticket is inactive", false, ticket.isActive());
    }

    static void testConcurrentParkSameSpotType() throws InterruptedException {
        printHeader("Test 13: Concurrent Park - Same Spot Type No Double Booking");
        ParkingLot lot = buildLot(1, 5, 0, 0); // 5 small spots
        int threadCount = 10; // more threads than spots
        List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
    
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            String id = "B" + i;
            threads.add(new Thread(() -> {
                try {
                    Ticket t = lot.park(bike(id));
                    tickets.add(t);
                } catch (ParkingLot.ParkingLotFullException e) {
                    // expected for threads beyond capacity
                } catch (Exception e) {
                    exceptionThrown.set(true);
                }
            }));
        }
    
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    
        // Exactly 5 tickets issued, all different spots
        assertEqual("exactly 5 tickets issued", 5, tickets.size());
        long uniqueSpots = tickets.stream().map(t -> t.getParkingSpot().getId()).distinct().count();
        assertEqual("all spots are unique (no double booking)", 5L, uniqueSpots);
        assertEqual("no unexpected exceptions", false, exceptionThrown.get());
    }
    
    static void testConcurrentParkDifferentSpotTypes() throws InterruptedException {
        printHeader("Test 14: Concurrent Park - Different Spot Types Run in Parallel");
        ParkingLot lot = buildLot(1, 5, 5, 5);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String bid = "B" + i, cid = "C" + i, tid = "T" + i;
            threads.add(new Thread(() -> {
                try { tickets.add(lot.park(bike(bid))); } catch (Exception e) { exceptionThrown.set(true); }
            }));
            threads.add(new Thread(() -> {
                try { tickets.add(lot.park(car(cid))); } catch (Exception e) { exceptionThrown.set(true); }
            }));
            threads.add(new Thread(() -> {
                try { tickets.add(lot.park(truck(tid))); } catch (Exception e) { exceptionThrown.set(true); }
            }));
        }
    
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    
        assertEqual("all 15 vehicles parked", 15, tickets.size());
        long uniqueSpots = tickets.stream().map(t -> t.getParkingSpot().getId()).distinct().count();
        assertEqual("all spots unique across types", 15L, uniqueSpots);
        assertEqual("no unexpected exceptions", false, exceptionThrown.get());
    }
    
    static void testConcurrentParkAndUnpark() throws InterruptedException {
        printHeader("Test 15: Concurrent Park and Unpark - No Deadlock or Corruption");
        ParkingLot lot = buildLot(1, 10, 0, 0);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        List<Ticket> tickets = Collections.synchronizedList(new ArrayList<>());
    
        // First fill the lot
        for (int i = 0; i < 10; i++) tickets.add(lot.park(bike("B" + i)));
    
        // Concurrent unparks and parks
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Ticket ticket = tickets.get(i);
            String newId = "NB" + i;
            threads.add(new Thread(() -> {
                try {
                    lot.unpark(ticket);
                    lot.park(bike(newId));
                } catch (Exception e) {
                    exceptionThrown.set(true);
                }
            }));
        }
    
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    
        assertEqual("no exceptions during concurrent park/unpark", false, exceptionThrown.get());
    }
    
    static void testConcurrentUnparkSameTicketThrows() throws InterruptedException {
        printHeader("Test 16: Concurrent Unpark Same Ticket - Only One Succeeds");
        ParkingLot lot = buildLot(1, 5, 0, 0);
        Ticket ticket = lot.park(bike("B_RACE"));
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
    
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            threads.add(new Thread(() -> {
                try {
                    lot.unpark(ticket);
                    successCount.incrementAndGet();
                } catch (ParkingLot.InvalidTicketException e) {
                    failCount.incrementAndGet();
                }
            }));
        }
    
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    
        assertEqual("exactly one unpark succeeds", 1, successCount.get());
        assertEqual("remaining 4 get InvalidTicketException", 4, failCount.get());
    }
    
    static void testConcurrentLotFullException() throws InterruptedException {
        printHeader("Test 17: Concurrent Park - Lot Full Exception Thrown Correctly");
        ParkingLot lot = buildLot(1, 3, 0, 0); // only 3 spots
        AtomicInteger parked = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);
    
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = "B" + i;
            threads.add(new Thread(() -> {
                try {
                    lot.park(bike(id));
                    parked.incrementAndGet();
                } catch (ParkingLot.ParkingLotFullException e) {
                    rejected.incrementAndGet();
                }
            }));
        }
    
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    
        assertEqual("exactly 3 parked", 3, parked.get());
        assertEqual("exactly 7 rejected", 7, rejected.get());
    }

    // -----------------------------------------------
    // Utilities
    // -----------------------------------------------
    static void printHeader(String title) {
        System.out.println("▶ " + title);
    }

    static <V> void assertEqual(String label, V expected, V actual) {
        boolean match = (expected == null) ? (actual == null) : expected.equals(actual);
        if (match) {
            System.out.printf("   ✅ PASS | %-45s → %s%n", label, actual);
            passed++;
        } else {
            System.out.printf("   ❌ FAIL | %-45s → Expected: %s, Got: %s%n", label, expected, actual);
            failed++;
        }
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