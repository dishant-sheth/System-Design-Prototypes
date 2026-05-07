package src;

import java.util.concurrent.locks.ReentrantLock;

public class ShardedLRUCache<Key, Value> implements ILRUCache<Key, Value> {

    private static final int DEFAULT_SHARDS = 5;
    private static final int DEFAULT_CAPACITY = 8;
    private final int numShards, capacity;
    private final LRUCache[] caches;
    private final ReentrantLock[] locks;

    public ShardedLRUCache(){
        this(DEFAULT_SHARDS, DEFAULT_CAPACITY);
    }

    public ShardedLRUCache(final int numShards, final int capacity){
        this.numShards = numShards;
        this.caches = new LRUCache[numShards];
        this.locks = new ReentrantLock[numShards];
        this.capacity = capacity;
        for(int i=0; i<numShards; ++i){
            LRUCache<Key, Value> cache = new LRUCache<>(capacity);
            caches[i] = cache;
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public Value get(Key key){
        final int shardId = getShardId(key);
        try {
            locks[shardId].lock();
            return (Value) caches[shardId].get(key);
        } finally {
            locks[shardId].unlock();
        }
    }

    @Override
    public void put(Key key, Value value){
        final int shardId = getShardId(key);
        try {
            locks[shardId].lock();
            caches[shardId].put(key, value);
        } finally {
            locks[shardId].unlock();
        }
    }

    @Override
    public void put(Key key, Value value, long ttlInMs){
        final int shardId = getShardId(key);
        try {
            locks[shardId].lock();
            caches[shardId].put(key, value, ttlInMs);
        } finally {
            locks[shardId].unlock();
        }
    }


    private int getShardId(Key key){
        return Math.abs(key.hashCode() % numShards);
    }
}