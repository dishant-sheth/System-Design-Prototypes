# LRU Cache

A generic LRU Cache in Java, built progressively from a basic cache to a concurrent solution.

## Implementations

### 1. LRUCache<K, V>
Generic, thread-safe LRU cache using a **HashMap + Doubly Linked List**.

- `get(key)` — O(1), returns `null` if not found, updates recency
- `put(key, value, ttlMs)` — O(log n) due to TTL tracking, O(1) without TTL
- Expired keys always evicted before live LRU keys
- Thread safety via `synchronized` on `get` and `put`

**Data Structures:**
- `HashMap<K, Node>` — O(1) key lookup
- Doubly Linked List — O(1) recency ordering (LRU at head, MRU at tail)
- `TreeMap<Long, Set<K>>` — O(log n) sorted expiry tracking; `Set<K>` handles same-millisecond collisions

---

### 2. ShardedLRUCache<K, V>
Scalable, concurrent LRU cache using **lock striping** across N independent shards.

- Each shard is an independent `LRUCache` with its own `ReentrantLock`
- Threads on different shards never block each other
- ~2x throughput over single-lock cache at 32 threads, 5M ops
- Shard routing via `Math.abs(key.hashCode() % numShards)`

---

## Key Design Decisions

**Why not ReentrantReadWriteLock?**
`get` moves a node to tail — making it a write operation. Readers can't hold a read lock, so `synchronized` is the correct and simpler choice.

**Why TreeMap for TTL?**
TTL introduces a second ordering (expiry time) on top of recency. Strict O(1) expiry-first eviction is not achievable without approximation — TreeMap is the honest O(log n) tradeoff.

**Why sharding?**
A single lock serializes all threads. Sharding routes keys to independent shards — threads on different shards run fully in parallel, reducing contention proportionally to shard count.

---

## Future
- **Bucket sampling TTL** — approximate O(1) expiry used by Redis/Caffeine
- **Lock-free implementation** — ring buffer approach used by Caffeine
- **Distributed LRU** — consistent hashing across nodes