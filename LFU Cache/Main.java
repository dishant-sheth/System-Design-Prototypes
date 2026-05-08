import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import src.LFUCache;
import src.ShardedLFUCache;

public class Main {

    static int passed = 0;
    static int failed = 0;
  
    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("      Sharded LFU Cache Test Suite      ");
        System.out.println("========================================\n");

        testBasicGetAndPut();
        testEvictLeastFrequent();
        testTieBreakingByLRU();
        testUpdateExistingKey();
        testGetOnMissingKey();
        testConcurrentPuts();
        testConcurrentGetAndPut();
        testConcurrentEviction();
        testThroughputVsSingleLock();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    // -----------------------------------------------
    // Test 1: Basic put and get across shards
    // -----------------------------------------------
    static void testBasicGetAndPut() {
        printHeader("Test 1: Basic Get and Put Across Shards");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 25);

        for (int i = 0; i < 100; i++) cache.put(i, i * 10);

        int found = 0;
        for (int i = 0; i < 100; i++) {
            if (cache.get(i) != null) found++;
        }
        assertEqual("all 100 keys inserted across shards", true, found == 100);
    }

    // -----------------------------------------------
    // Test 2: Eviction respects LFU per shard
    // -----------------------------------------------
    static void testEvictLeastFrequent() {
        printHeader("Test 2: Evict Least Frequent Per Shard");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 5);

        for (int i = 0; i < 40; i++) cache.put(i, i); // overfill each shard

        int found = 0;
        for (int i = 0; i < 40; i++) {
            if (cache.get(i) != null) found++;
        }
        assertEqual("at most 20 keys survive (4 shards x 5)", true, found <= 20);
    }

    // -----------------------------------------------
    // Test 3: Tie-breaking by LRU within a shard
    // -----------------------------------------------
    static void testTieBreakingByLRU() {
        printHeader("Test 3: Tie-Breaking by LRU Within Shard");
        // Use keys that hash to the same shard
        // With 1 shard we guarantee all keys are in same shard
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(1, 3);
        cache.put(1, 10); // freq(1) = 1
        cache.put(2, 20); // freq(2) = 1
        cache.put(3, 30); // freq(3) = 1
        cache.put(4, 40); // evicts key 1 (LRU among freq=1)
        assertEqual("get(1) evicted (LRU)", null, cache.get(1));
        assertEqual("get(2) survives", 20, cache.get(2));
        assertEqual("get(3) survives", 30, cache.get(3));
        assertEqual("get(4)", 40, cache.get(4));
    }

    // -----------------------------------------------
    // Test 4: Update existing key
    // -----------------------------------------------
    static void testUpdateExistingKey() {
        printHeader("Test 4: Update Existing Key");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 25);
        cache.put(1, 10);
        cache.put(1, 99);
        assertEqual("get(1) returns updated value", 99, cache.get(1));
    }

    // -----------------------------------------------
    // Test 5: Get on missing key returns null
    // -----------------------------------------------
    static void testGetOnMissingKey() {
        printHeader("Test 5: Get on Missing Key Returns Null");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 25);
        cache.put(1, 10);
        assertEqual("get(99) missing", null, cache.get(99));
        assertEqual("get(0) missing", null, cache.get(0));
    }

    // -----------------------------------------------
    // Test 6: Concurrent puts - no corruption
    // -----------------------------------------------
    static void testConcurrentPuts() throws InterruptedException {
        printHeader("Test 6: Concurrent Puts - No Corruption");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 25);
        AtomicBoolean failed = new AtomicBoolean(false);

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                try {
                    for (int i = base; i < base + 10; i++) cache.put(i, i);
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        assertEqual("no exceptions during concurrent puts", false, failed.get());
    }

    // -----------------------------------------------
    // Test 7: Concurrent get and put - no deadlock
    // -----------------------------------------------
    static void testConcurrentGetAndPut() throws InterruptedException {
        printHeader("Test 7: Concurrent Get and Put - No Deadlock");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(4, 25);
        for (int i = 0; i < 50; i++) cache.put(i, i);

        AtomicBoolean failed = new AtomicBoolean(false);
        List<Thread> threads = new ArrayList<>();

        // Writers
        for (int t = 0; t < 4; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                try {
                    for (int i = base; i < base + 10; i++) cache.put(i, i * 2);
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        // Readers
        for (int t = 0; t < 4; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                try {
                    for (int i = base; i < base + 10; i++) cache.get(i);
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        assertEqual("no exceptions during concurrent get/put", false, failed.get());
    }

    // -----------------------------------------------
    // Test 8: Concurrent eviction - never exceeds capacity
    // -----------------------------------------------
    static void testConcurrentEviction() throws InterruptedException {
        printHeader("Test 8: Concurrent Eviction - Never Exceeds Per-Shard Capacity");
        ShardedLFUCache<Integer, Integer> cache = new ShardedLFUCache<>(1, 10);
        AtomicBoolean exceeded = new AtomicBoolean(false);

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                for (int i = base; i < base + 10; i++) {
                    cache.put(i, i);
                    int size = cache.size();
                    if (size > 10) exceeded.set(true);
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        assertEqual("cache never exceeded capacity", false, exceeded.get());
    }

    // -----------------------------------------------
    // Test 9: Sharded vs single lock throughput
    // -----------------------------------------------
    static void testThroughputVsSingleLock() throws InterruptedException {
        printHeader("Test 9: Sharded vs Single Lock - Throughput Comparison");
        int ops = 10000000;
        int threadCount = 8;

        LFUCache<Integer, Integer> single = new LFUCache<>(1000);
        long singleTime = measureThroughput(threadCount, ops, single);

        ShardedLFUCache<Integer, Integer> sharded = new ShardedLFUCache<>(8, 125);
        long shardedTime = measureThroughput(threadCount, ops, sharded);

        System.out.printf("   Single lock: %dms | Sharded: %dms%n", singleTime, shardedTime);
        assertEqual("sharded is faster than single lock", true, shardedTime < singleTime);
    }

    static long measureThroughput(int threadCount, int ops, Object cache) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        int perThread = ops / threadCount;
        long start = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            int base = t * perThread;
            threads.add(new Thread(() -> {
                for (int i = base; i < base + perThread; i++) {
                    if (cache instanceof ShardedLFUCache) {
                        ((ShardedLFUCache<Integer, Integer>) cache).put(i % 1000, i);
                        ((ShardedLFUCache<Integer, Integer>) cache).get(i % 1000);
                    } else {
                        ((LFUCache<Integer, Integer>) cache).put(i % 1000, i);
                        ((LFUCache<Integer, Integer>) cache).get(i % 1000);
                    }
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        return System.currentTimeMillis() - start;
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
}