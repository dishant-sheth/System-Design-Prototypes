package src;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ShardedLFUCache<Key, Value> implements ILFUCache<Key, Value> {

    private static final int DEFAULT_SHARDS = 5;
    private static final int DEFAULT_CAPACITY = 8;
    private final int numOfShards, capacity;
    private final ReentrantLock[] locks;
    private final LFUCache<Key, Value>[] caches;
    private AtomicInteger size;

    public ShardedLFUCache(){
        this(DEFAULT_SHARDS, DEFAULT_CAPACITY);
    }

    public ShardedLFUCache(final int numOfShards, final int capacity){
        this.numOfShards = numOfShards;
        this.capacity = capacity;
        this.locks = new ReentrantLock[numOfShards];
        this.caches = new LFUCache[numOfShards];
        for(int i=0; i<numOfShards; ++i){
            caches[i] = new LFUCache(capacity);
            locks[i] = new ReentrantLock();
        }
        this.size = new AtomicInteger(0);
    }

    @Override
    public int size(){
        return size.get();
    }
    
    @Override
    public Value get(final Key key){
        final int shardId = getShardId(key);
        try {
            locks[shardId].lock();
            return (Value) caches[shardId].get(key);
        }
        finally {
            locks[shardId].unlock();
        }
    }

    @Override
    public void put(final Key key, final Value value){
        final int shardId = getShardId(key);
        try {
            locks[shardId].lock();
            int sizeBeforePut = caches[shardId].size();
            caches[shardId].put(key, value);
            int sizeAfterPut = caches[shardId].size();
            size.addAndGet(sizeAfterPut - sizeBeforePut);
        }
        finally {
            locks[shardId].unlock();
        }
    }

    private int getShardId(Key key){
        return Math.abs(key.hashCode() % this.numOfShards);
    }
}