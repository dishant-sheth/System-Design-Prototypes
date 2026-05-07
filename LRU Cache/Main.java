import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import src.LRUCache;
import src.ShardedLRUCache;

public class Main {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========================================");
        System.out.println("   Sharded LRU Cache Test Suite         ");
        System.out.println("========================================\n");

        testShardedBasicPutAndGet();
        testShardedEviction();
        testShardedConcurrentPuts();
        testShardedConcurrentGetAndPut();
        testShardedThroughputVsSingleLock();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d Passed | %d Failed%n", passed, failed);
        System.out.println("========================================");
    }

    static void testShardedBasicPutAndGet() {
        printHeader("Test 1: Sharded Basic Put and Get");
        ShardedLRUCache<Integer, Integer> cache = new ShardedLRUCache<>(4, 25);

        for (int i = 0; i < 100; i++) cache.put(i, i * 10, 100000);

        int found = 0;
        for (int i = 0; i < 100; i++) {
            if (cache.get(i) != null) found++;
        }
        assertEqual("all 100 keys inserted across shards", true, found == 100);
    }

    static void testShardedEviction() {
        printHeader("Test 2: Sharded Eviction Respects Per-Shard Capacity");
        ShardedLRUCache<Integer, Integer> cache = new ShardedLRUCache<>(4, 5);

        for (int i = 0; i < 40; i++) cache.put(i, i, 100000);

        int found = 0;
        for (int i = 0; i < 40; i++) {
            if (cache.get(i) != null) found++;
        }
        assertEqual("at most 20 keys survive (4 shards x 5)", true, found <= 20);
    }

    static void testShardedConcurrentPuts() throws InterruptedException {
        printHeader("Test 3: Sharded Concurrent Puts - No Corruption");
        ShardedLRUCache<Integer, Integer> cache = new ShardedLRUCache<>(4, 25);
        AtomicBoolean failed = new AtomicBoolean(false);

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                try {
                    for (int i = base; i < base + 10; i++) cache.put(i, i, 100000);
                } catch (Exception e) {
                    failed.set(true);
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        assertEqual("no exceptions during concurrent puts", false, failed.get());
    }

    static void testShardedConcurrentGetAndPut() throws InterruptedException {
        printHeader("Test 4: Sharded Concurrent Get and Put - No Deadlock");
        ShardedLRUCache<Integer, Integer> cache = new ShardedLRUCache<>(4, 25);
        for (int i = 0; i < 50; i++) cache.put(i, i, 100000);

        AtomicBoolean failed = new AtomicBoolean(false);
        List<Thread> threads = new ArrayList<>();

        // Writers
        for (int t = 0; t < 4; t++) {
            int base = t * 10;
            threads.add(new Thread(() -> {
                try {
                    for (int i = base; i < base + 10; i++) cache.put(i, i * 2, 100000);
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

    static void testShardedThroughputVsSingleLock() throws InterruptedException {
        printHeader("Test 5: Sharded vs Single Lock - Throughput Comparison");
        int ops = 9000000;
        int threadCount = 32;

        LRUCache<Integer, Integer> single = new LRUCache<>(1000);
        long singleTime = measureThroughput(threadCount, ops, single);

        ShardedLRUCache<Integer, Integer> sharded = new ShardedLRUCache<>(8, 125);
        long shardedTime = measureThroughput(threadCount, ops, sharded);

        System.out.printf("   Single lock: %dms | Sharded: %dms%n", singleTime, shardedTime);
        assertEqual("sharded is faster than single lock", true, shardedTime < singleTime);
    }

    static long measureThroughput(int threadCount, int ops, Object cache) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        long start = System.currentTimeMillis();
        int perThread = ops / threadCount;

        for (int t = 0; t < threadCount; t++) {
            int base = t * perThread;
            threads.add(new Thread(() -> {
                for (int i = base; i < base + perThread; i++) {
                    if (cache instanceof ShardedLRUCache) {
                        ((ShardedLRUCache<Integer, Integer>) cache).put(i % 1000, i, 100000);
                        ((ShardedLRUCache<Integer, Integer>) cache).get(i % 1000);
                    } else {
                        ((LRUCache<Integer, Integer>) cache).put(i % 1000, i, 100000);
                        ((LRUCache<Integer, Integer>) cache).get(i % 1000);
                    }
                }
            }));
        }

        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();

        return System.currentTimeMillis() - start;
    }

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