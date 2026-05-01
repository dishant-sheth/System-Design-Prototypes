package src;

public class DoubleHashingProbe extends Probe {

    @Override
    public int probe(final int hashCode, final int attempt, final int capacity){
        int h1 = getIndex(hashCode, capacity);
        int h2 = (hashCode & 0x7fffffff) | 1;
        return ((h1 + attempt * h2) & 0x7fffffff) % capacity;
    }

}