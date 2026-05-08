import src.LFUCache;

public class Main {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("         LFU Cache Test Suite           ");
        System.out.println("========================================\n");

        testBasicGetAndPut();
        testEvictLeastFrequent();
        testFrequencyIncrementOnGet();
        testFrequencyIncrementOnPut();
        testTieBreakingByLRU();
        testUpdateExistingKey();
        testGetOnMissingKey();
        testGetOnEmptyCache();
        testMinFreqResetsOnNewKey();
        testMixedOperations();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    // -----------------------------------------------
    // Test 1: Basic put and get
    // -----------------------------------------------
    static void testBasicGetAndPut() {
        printHeader("Test 1: Basic Get and Put");
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(3, 30);
        assertEqual("get(1)", 10, cache.get(1));
        assertEqual("get(2)", 20, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    // -----------------------------------------------
    // Test 2: Evict least frequent key
    // -----------------------------------------------
    static void testEvictLeastFrequent() {
        printHeader("Test 2: Evict Least Frequent Key");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 10); // freq(1) = 1
        cache.put(2, 20); // freq(2) = 1
        cache.get(1);     // freq(1) = 2
        cache.put(3, 30); // evicts key 2 (freq=1, LFU)
        assertEqual("get(1) survives", 10, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    // -----------------------------------------------
    // Test 3: get() increments frequency
    // -----------------------------------------------
    static void testFrequencyIncrementOnGet() {
        printHeader("Test 3: Get Increments Frequency");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 10); // freq(1) = 1
        cache.put(2, 20); // freq(2) = 1
        cache.get(1);     // freq(1) = 2
        cache.get(1);     // freq(1) = 3
        cache.put(3, 30); // evicts key 2 (freq=1)
        assertEqual("get(1) high freq survives", 10, cache.get(1));
        assertEqual("get(2) low freq evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    // -----------------------------------------------
    // Test 4: put() on existing key increments frequency
    // -----------------------------------------------
    static void testFrequencyIncrementOnPut() {
        printHeader("Test 4: Put on Existing Key Increments Frequency");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 10); // freq(1) = 1
        cache.put(2, 20); // freq(2) = 1
        cache.put(1, 99); // freq(1) = 2, value updated
        cache.put(3, 30); // evicts key 2 (freq=1)
        assertEqual("get(1) updated value", 99, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    // -----------------------------------------------
    // Test 5: Tie-breaking by LRU
    // -----------------------------------------------
    static void testTieBreakingByLRU() {
        printHeader("Test 5: Tie-Breaking by LRU");
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);
        cache.put(1, 10); // freq(1) = 1
        cache.put(2, 20); // freq(2) = 1
        cache.put(3, 30); // freq(3) = 1
        // All freq=1, key 1 is LRU
        cache.put(4, 40); // evicts key 1 (LRU among freq=1)
        assertEqual("get(1) evicted (LRU)", null, cache.get(1));
        assertEqual("get(2) survives", 20, cache.get(2));
        assertEqual("get(3) survives", 30, cache.get(3));
        assertEqual("get(4)", 40, cache.get(4));
    }

    // -----------------------------------------------
    // Test 6: Update existing key
    // -----------------------------------------------
    static void testUpdateExistingKey() {
        printHeader("Test 6: Update Existing Key");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 10);
        cache.put(1, 99); // update
        assertEqual("get(1) returns updated value", 99, cache.get(1));
    }

    // -----------------------------------------------
    // Test 7: Get on missing key returns null
    // -----------------------------------------------
    static void testGetOnMissingKey() {
        printHeader("Test 7: Get on Missing Key Returns Null");
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);
        cache.put(1, 10);
        assertEqual("get(99) missing", null, cache.get(99));
        assertEqual("get(0) missing", null, cache.get(0));
    }

    // -----------------------------------------------
    // Test 8: Get on empty cache returns null
    // -----------------------------------------------
    static void testGetOnEmptyCache() {
        printHeader("Test 8: Get on Empty Cache Returns Null");
        LFUCache<Integer, Integer> cache = new LFUCache<>(3);
        assertEqual("get(1) empty cache", null, cache.get(1));
    }

    // -----------------------------------------------
    // Test 9: minFreq resets to 1 on new key insert
    // -----------------------------------------------
    static void testMinFreqResetsOnNewKey() {
        printHeader("Test 9: minFreq Resets to 1 on New Key Insert");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 10); // freq(1) = 1
        cache.get(1);     // freq(1) = 2
        cache.get(1);     // freq(1) = 3
        cache.put(2, 20); // freq(2) = 1, minFreq resets to 1
        cache.put(3, 30); // evicts key 2 (minFreq=1), not key 1 (freq=3)
        assertEqual("get(1) high freq survives", 10, cache.get(1));
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 30, cache.get(3));
    }

    // -----------------------------------------------
    // Test 10: Mixed operations (LeetCode classic)
    // -----------------------------------------------
    static void testMixedOperations() {
        printHeader("Test 10: Mixed Operations (LeetCode Classic)");
        LFUCache<Integer, Integer> cache = new LFUCache<>(2);
        cache.put(1, 1);  // freq(1) = 1
        cache.put(2, 2);  // freq(2) = 1
        assertEqual("get(1)", 1, cache.get(1));   // freq(1) = 2
        cache.put(3, 3);                           // evicts key 2 (LFU, freq=1)
        assertEqual("get(2) evicted", null, cache.get(2));
        assertEqual("get(3)", 3, cache.get(3));   // freq(3) = 2
        cache.put(4, 4);                           // evicts key 1 or 3 (both freq=2, key 1 is LRU)
        assertEqual("get(1) evicted", null, cache.get(1));
        assertEqual("get(3) survives", 3, cache.get(3));
        assertEqual("get(4)", 4, cache.get(4));
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