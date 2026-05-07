package src;

public interface ILRUCache<Key, Value>{

    // Get the value for specified key.
    public Value get(Key key);

    // Insert or update value for given key.
    public void put(Key key, Value value);

    // Support TTLs.
    public void put(Key key, Value value, long ttlInMs);

}