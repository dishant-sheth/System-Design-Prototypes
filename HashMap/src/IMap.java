package src;

public interface IMap<Key, Value> {

    public int size();

    public boolean isEmpty();

    public boolean containsKey(Key k);

    public void clear();

    public Value put(Key k, Value v);

    public Value get(Key k);

    public Value delete(Key k);
}