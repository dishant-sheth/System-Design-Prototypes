package src;

public abstract class Probe {

    public abstract int probe(final int hashCode, final int attempt, final int capacity);

    int getIndex(int hashCode, int capacity){
        return (hashCode & 0x7fffffff) & (capacity - 1);
    }
}