package src;

@SuppressWarnings("unchecked")
public class HashMap<Key, Value> implements IMap<Key, Value> {
    
    private IMap mapStrategy;
    public HashMap(){
        this.mapStrategy = new ChainingHashTable();
    }

    public void setMapType(IMap map){
        this.mapStrategy = map;
    }

    @Override
    public int size(){
        return this.mapStrategy.size();
    }

    @Override
    public boolean isEmpty(){
        return this.mapStrategy.isEmpty();
    }

    @Override
    public boolean containsKey(Key k){
        return this.mapStrategy.containsKey(k);
    }

    @Override
    public void clear(){
        this.mapStrategy.clear();
    }

    @Override
    public Value put(Key k, Value v){
        return (Value) this.mapStrategy.put(k, v);
    }

    @Override
    public Value get(Key k){
        return (Value) this.mapStrategy.get(k);
    }

    @Override
    public Value delete(Key k){
        return (Value) this.mapStrategy.delete(k);
    }
}