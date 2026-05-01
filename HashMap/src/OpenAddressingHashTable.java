package src;

@SuppressWarnings("unchecked")
public class OpenAddressingHashTable<Key, Value> implements IMap<Key, Value> {
    class Slot {
        private final Key key;
        private Value value;
        private final int keyHash;
        private boolean isDeleted;

        Slot(final Key key, final Value value, final int keyHash){
            this.key = key;
            this.value = value;
            this.keyHash = keyHash;
            this.isDeleted = false;
        }

        void delete(){
            this.isDeleted = true;
        }

        boolean isDeleted(){
            return this.isDeleted;
        }

        void updateValue(Value v){
            this.value = v;
        }
    }

    private final int MINIMUM_CAPACITY = 8;
    private final double INCREASE_SIZE_LOAD_THRESHOLD = 0.5;
    private final double DECREASE_SIZE_LOAD_THRESHOLD = 0.125;
    private int capacity, size, used_slots;
    private Object[] slots;
    private final Probe probe;

    public OpenAddressingHashTable(){
        this(new LinearProbe());
    }

    public OpenAddressingHashTable(Probe probe){
        this.probe = probe;
        this.capacity = MINIMUM_CAPACITY;
        this.size = 0;
        this.used_slots = 0;
        this.slots = new Object[capacity];
    }

    @Override
    public int size(){
        return this.size;
    }

    @Override
    public boolean isEmpty(){
        return (this.size == 0);
    }

    @Override
    public boolean containsKey(Key k){
        final int keyHash = k.hashCode();
        int attempt = 0;
        while(attempt < this.capacity){
            final int getIdx = probe.probe(keyHash, attempt, this.capacity);
            final Slot currSlot = (Slot) slots[getIdx];
            if(currSlot == null) break;
            if(currSlot.isDeleted()){
                ++attempt;
                continue;
            }
            if(currSlot.key.equals(k)) return true;
            ++attempt;
        }

        return false;
    }

    @Override
    public void clear(){
        for(int i=0; i<this.capacity; ++i){
            slots[i] = null;
        }
        this.size = 0;
        this.used_slots = 0;
    }

    @Override
    public Value put(Key k, Value v){
        final int keyHash = k.hashCode();
        int attempt = 0;
        int firstTombstoneIndex = -1, validPutIdx = -1;
        Value returnVal = null;

        while(attempt < this.capacity){
            final int putIdx = probe.probe(keyHash, attempt, this.capacity);
            // Check if slot is free - if yes, straight insert.
            final Slot slot = (Slot) slots[putIdx];
            if(slot == null){ // No point probing more.
                validPutIdx = putIdx;
                break;
            }
            else if (slot.isDeleted()){
                if(firstTombstoneIndex == -1) firstTombstoneIndex = putIdx;
            }
            else if(slot.key.equals(k)){ // Key already exists, update value.
                returnVal = (Value) slot.value;
                slot.updateValue(v);
                return returnVal; // No change in size - return.
            }
            ++attempt;
        }

        //System.out.println("capacity=" + capacity + " size=" + size + " used_slots=" + used_slots + " attempt=" + attempt);
        if (firstTombstoneIndex == -1 && validPutIdx == -1) throw new IllegalStateException("Table full — resize threshold misconfigured");

        if(firstTombstoneIndex != -1){
            slots[firstTombstoneIndex] = new Slot(k, v, keyHash);
        } else {
            slots[validPutIdx] = new Slot(k, v, keyHash);
            ++used_slots;
        }

        ++size;

        final double loadFactor = used_slots/(capacity*1.0);
        if(loadFactor >= INCREASE_SIZE_LOAD_THRESHOLD){
            resize(capacity * 2);
        }

        assert used_slots >= size : "used_slots=" + used_slots + " size=" + size;
        return returnVal;
    }

    @Override
    public Value get(Key k){
        final int keyHash = k.hashCode();
        int attempt = 0;
        while(attempt < this.capacity){
            final int getIdx = probe.probe(keyHash, attempt, this.capacity);
            final Slot currSlot = (Slot) slots[getIdx];
            if(currSlot == null) break;
            if(currSlot.isDeleted()){
                ++attempt;
                continue;
            }
            if(currSlot.key.equals(k)) return currSlot.value;
            ++attempt;
        }

        return null;
    }

    @Override
    public Value delete(Key k){
        final int keyHash = k.hashCode();
        int attempt = 0;
        Value returnVal = null;
        while(attempt < this.capacity){
            final int delIdx = probe.probe(keyHash, attempt, this.capacity);
            final Slot currSlot = (Slot) slots[delIdx];
            if(currSlot == null) break;
            if(currSlot.isDeleted()){
                ++attempt;
                continue;
            }
            if(currSlot.key.equals(k)){
                currSlot.delete();
                returnVal = currSlot.value;
                break;
            }
            ++attempt;
        }

        if(returnVal == null) return null;

        --size;
        final double loadFactor = size/(this.capacity * 1.0);
        if(loadFactor <= DECREASE_SIZE_LOAD_THRESHOLD){
            resize(this.capacity/2);
        }

        assert used_slots >= size : "used_slots=" + used_slots + " size=" + size;
        return returnVal;
    }

    private void resize(int newCapacity){
        if(newCapacity < MINIMUM_CAPACITY){
            //System.out.println("Will not resize. Minimum capacity is " + MINIMUM_CAPACITY);
            return;
        }
        //System.out.println("Resize to " + newCapacity);

        Object[] newSlots = new Object[newCapacity];
        for(int i=0; i<this.capacity; ++i){
            Slot currSlot = (Slot) slots[i];
            if(currSlot == null) continue;
            if(currSlot.isDeleted()) continue;
            int attempt = 0;
            while(attempt < newCapacity){
                int newIdx = probe.probe(currSlot.keyHash, attempt, newCapacity);
                if(newSlots[newIdx] == null){
                    newSlots[newIdx] = new Slot(currSlot.key, currSlot.value, currSlot.keyHash);
                    break;
                }
                ++attempt;
            }
        }

        this.capacity = newCapacity;
        this.used_slots = this.size; // All old deleted slots have been ignored.
        this.slots = newSlots;
    }
}