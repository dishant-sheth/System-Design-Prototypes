package src;

public class LinearProbe extends Probe {

    @Override
    public int probe(final int hashCode, final int attempt, final int capacity){
        return getIndex((hashCode + attempt), capacity);
    }
}