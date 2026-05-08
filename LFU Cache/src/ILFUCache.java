package src;

public interface ILFUCache<Key, Value> {

    Value get(final Key key);

    void put(final Key key, final Value value);

    int size();

}