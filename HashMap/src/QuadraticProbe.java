package src;

public class QuadraticProbe extends Probe {

    @Override
    public int probe(final int hashCode, final int attempt, final int capacity){
        return getIndex((hashCode + (attempt * (attempt+1))/2), capacity);
    }
}