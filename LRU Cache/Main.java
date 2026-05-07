import src.LRUCache;

public class Main {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("     LRU Cache Test Suite (Generic)     ");
        System.out.println("========================================\n");

        testBasicGetAndPut();
        testEvictionOnCapacityExceeded();
        testGetUpdatesRecency();
        testUpdateExistingKey();
        testCapacityTwo();
        testGetOnMissingKey();
        testAllKeysEvicted();
        testReInsertAfterEviction();
        testMixedOperations();
        testStringKeyStringValue();
        testStringKeyIntegerValue();
        testNullReturnOnEmptyCache();

        testExpiredKeyReturnsNull();
        testNonExpiredKeySurvives();
        testExpiredKeyEvictedBeforeLiveKey();
        testLiveKeyNotEvictedWhenExpiredExists();
        testExpiredKeyGetDoesNotUpdateRecency();
        testUpdateExistingKeyResetsExpiry();
        testMultipleExpiredKeysAllEvictedBeforeLive();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    static void testBasicGetAndPut() {
        printHeader("Test 1: Basic Get and Put");
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        assertEqual("get(1)", 10, cache.get(1));
        assertEqual("get(2)", 20, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    static void testEvictionOnCapacityExceeded() {
        printHeader("Test 2: Eviction on Capacity Exceeded");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3); // evicts key 1
        assertEqual("get(1) evicted", null, cache.get(1));
        assertEqual("get(2)", 2, cache.get(2));
        assertEqual("get(3)", 3, cache.get(3));
    }

    static void testGetUpdatesRecency() {
        printHeader("Test 3: Get Updates Recency");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);    // key 1 is now MRU, key 2 is LRU
        cache.put(3, 3); // evicts key 2
        assertEqual("get(1) survives", 1, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 3, cache.get(3));
    }

    static void testUpdateExistingKey() {
        printHeader("Test 4: Update Existing Key");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 100);
        cache.put(2, 200);
        cache.put(1, 999); // update key 1 → key 2 becomes LRU
        cache.put(3, 300); // evicts key 2
        assertEqual("get(1) updated", 999, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 300, cache.get(3));
    }

    static void testCapacityTwo() {
        printHeader("Test 5: Capacity = 2 (Minimum Meaningful)");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.get(1);    // key 1 is MRU, key 2 is LRU
        cache.put(3, 30); // evicts key 2
        assertEqual("get(1) survives", 10, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    static void testGetOnMissingKey() {
        printHeader("Test 6: Get on Missing Key Returns Null");
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        assertEqual("get(99)", null, cache.get(99));
        assertEqual("get(0)", null, cache.get(0));
    }

    static void testAllKeysEvicted() {
        printHeader("Test 7: All Keys Evicted Over Time");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3); // evicts 1
        cache.put(4, 4); // evicts 2
        assertEqual("get(1)", null, cache.get(1));
        assertEqual("get(2)", null, cache.get(2));
        assertEqual("get(3)", 3, cache.get(3));
        assertEqual("get(4)", 4, cache.get(4));
    }

    static void testReInsertAfterEviction() {
        printHeader("Test 8: Re-Insert After Eviction");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);  // evicts key 1
        cache.put(1, 100); // re-insert key 1, evicts key 2
        assertEqual("get(1) re-inserted", 100, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    static void testMixedOperations() {
        printHeader("Test 9: Mixed Operations (LeetCode Classic)");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 1);
        cache.put(2, 2);
        assertEqual("get(1)", 1, cache.get(1));
        cache.put(3, 3);                            // evicts key 2
        assertEqual("get(2)", null, cache.get(2));
        cache.put(4, 4);                            // evicts key 1
        assertEqual("get(1)", null, cache.get(1));
        assertEqual("get(3)", 3, cache.get(3));
        assertEqual("get(4)", 4, cache.get(4));
    }

    static void testStringKeyStringValue() {
        printHeader("Test 10: Generic <String, String>");
        LRUCache<String, String> cache = new LRUCache<>(2);
        cache.put("name", "Alice");
        cache.put("city", "Hosur");
        cache.get("name");               // name becomes MRU, city is now LRU
        cache.put("country", "India");   // evicts "city"
        assertEqual("get(city) evicted", null, cache.get("city"));
        assertEqual("get(name) survives", "Alice", cache.get("name"));
        assertEqual("get(country)", "India", cache.get("country"));
    }

    static void testStringKeyIntegerValue() {
        printHeader("Test 11: Generic <String, Integer>");
        LRUCache<String, Integer> cache = new LRUCache<>(2);
        cache.put("score", 95);
        cache.put("level", 3);
        assertEqual("get(score)", 95, cache.get("score"));
        cache.put("lives", 5); // evicts "level"
        assertEqual("get(level) evicted", null, cache.get("level"));
        assertEqual("get(lives)", 5, cache.get("lives"));
    }

    static void testNullReturnOnEmptyCache() {
        printHeader("Test 12: Get on Empty Cache Returns Null");
        LRUCache<Integer, String> cache = new LRUCache<>(5);
        assertEqual("get(1) empty cache", null, cache.get(1));
        assertEqual("get(0) empty cache", null, cache.get(0));
    }

    static void testExpiredKeyReturnsNull() throws InterruptedException {
        printHeader("Test 1: Expired Key Returns Null");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10, 100); // expires in 100ms
        assertEqual("get(1) before expiry", 10, cache.get(1));
        Thread.sleep(150);
        assertEqual("get(1) after expiry", null, cache.get(1));
    }

    static void testNonExpiredKeySurvives() throws InterruptedException {
        printHeader("Test 2: Non-Expired Key Survives");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10, 300); // expires in 300ms
        Thread.sleep(150);
        assertEqual("get(1) still alive", 10, cache.get(1));
    }

    static void testExpiredKeyEvictedBeforeLiveKey() throws InterruptedException {
        printHeader("Test 3: Expired Key Evicted Before Live LRU Key");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10, 100); // expires in 100ms
        cache.put(2, 20, 10000); // long lived
        Thread.sleep(150);      // key 1 is now expired
        cache.put(3, 30, 10000); // should evict key 1 (expired), not key 2 (live LRU)
        assertEqual("get(1) evicted (expired)", null, cache.get(1));
        assertEqual("get(2) survives", 20, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    static void testLiveKeyNotEvictedWhenExpiredExists() throws InterruptedException {
        printHeader("Test 4: Live LRU Key Not Evicted When Expired Key Exists");
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 10, 10000); // live
        cache.put(2, 20, 100);   // expires in 100ms
        cache.put(3, 30, 10000); // live
        Thread.sleep(150);       // key 2 expired
        cache.put(4, 40, 10000); // should evict key 2 (expired), not key 1 (live LRU)
        assertEqual("get(1) survives", 10, cache.get(1));
        assertEqual("get(2) evicted (expired)", null, cache.get(2));
        assertEqual("get(3) survives", 30, cache.get(3));
        assertEqual("get(4)", 40, cache.get(4));
    }

    static void testExpiredKeyGetDoesNotUpdateRecency() throws InterruptedException {
        printHeader("Test 5: Get on Expired Key Does Not Update Recency");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10, 100);   // expires in 100ms
        cache.put(2, 20, 10000); // long lived
        Thread.sleep(150);
        cache.get(1);            // expired — should return null and NOT move to MRU
        cache.put(3, 30, 10000); // key 2 should still be LRU, key 1 slot is freed
        assertEqual("get(1) expired", null, cache.get(1));
        assertEqual("get(2) survives", 20, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    static void testUpdateExistingKeyResetsExpiry() throws InterruptedException {
        printHeader("Test 6: Updating Key Resets TTL");
        LRUCache<Integer, Integer> cache = new LRUCache<>(2);
        cache.put(1, 10, 100);   // expires in 100ms
        Thread.sleep(50);
        cache.put(1, 99, 500);   // reset with new TTL of 500ms
        Thread.sleep(150);       // original TTL would have expired by now
        assertEqual("get(1) still alive after TTL reset", 99, cache.get(1));
    }

    static void testMultipleExpiredKeysAllEvictedBeforeLive() throws InterruptedException {
        printHeader("Test 7: Multiple Expired Keys Evicted Before Any Live Key");
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 10, 100);   // expires
        cache.put(2, 20, 100);   // expires
        cache.put(3, 30, 10000); // live
        Thread.sleep(500);
        cache.put(4, 40, 10000); // evicts key 1 (earliest expired)
        cache.put(5, 50, 10000); // evicts key 2 (next expired)
        assertEqual("get(1) evicted", null, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3) survives", 30, cache.get(3));
        assertEqual("get(4)", 40, cache.get(4));
        assertEqual("get(5)", 50, cache.get(5));
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
            System.out.printf("   ✅ PASS | %-40s → %s%n", label, actual);
            passed++;
        } else {
            System.out.printf("   ❌ FAIL | %-40s → Expected: %s, Got: %s%n", label, expected, actual);
            failed++;
        }
    }
}