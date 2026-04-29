package src;

@SuppressWarnings("unchecked")
class ChainingHashTable<Key, Value> implements IMap<Key, Value> {

    private class Node {

        private final Key key;
        private final Value value;
        private final int keyHash;
        public Node next;

        public Node(Key key, Value value, final int keyHash){
            this.key = key;
            this.value = value;
            this.keyHash = keyHash;
        }

        public Key getKey(){
            return this.key;
        }

        public Value getValue(){
            return this.value;
        }

        public int getKeyHash(){
            return this.keyHash;
        }
    }

    private final double INCREASE_SIZE_LOAD_THRESHOLD = 0.5;
    private final double DECREASE_SIZE_LOAD_THRESHOLD = 0.125;
    private final int DEFAULT_CAPACITY = 8;
    private int capacity, size;
    private Object[] hashTable;

    public ChainingHashTable(){
        initMap(DEFAULT_CAPACITY);
    }

    public ChainingHashTable(int capacity){
        initMap(capacity);
    }

    private void initMap(final int capacity){
        this.capacity = capacity;
        this.size = 0;
        this.hashTable = new Object[capacity];
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
        final int getIdx = getIdxFromHash(keyHash);
        // Not found.
        if(hashTable[getIdx] == null) return false;
        // Iterate through LL at get index.
        Node headNode = (Node) hashTable[getIdx];
        while(headNode != null){
            if(k.equals(headNode.key)) return true;
            headNode = headNode.next;
        }
        return false;
    }

    @Override
    public void clear(){
        for(int i=0; i<this.capacity; ++i){
            this.hashTable[i] = null;
        }
    }

    @Override
    public Value put(Key k, Value v){
        final int keyHash = k.hashCode();
        // Get index to place at.
        final int putIdx = getIdxFromHash(keyHash);
        if(hashTable[putIdx] == null){
            hashTable[putIdx] = new Node(k, v, keyHash);
        }
        else {
            // Prepend to the head of the array.
            Node newNode = new Node(k, v, keyHash);
            newNode.next = (Node) hashTable[putIdx];
            hashTable[putIdx] = newNode;
        }

        size += 1;

        // Check if resize is needed.
        final double load = size/(capacity*1.0);
        if(load >= INCREASE_SIZE_LOAD_THRESHOLD){
            resize(this.capacity * 2);
        }

        return v;
    }

    @Override
    public Value get(Key k){    
        final int keyHash = k.hashCode();
        final int getIdx = getIdxFromHash(keyHash);
        // Not found.
        if(hashTable[getIdx] == null) return null;
        // Iterate through LL at get index.
        Node headNode = (Node) hashTable[getIdx];
        while(headNode != null){
            if(k.equals(headNode.key)) return headNode.value;
            headNode = headNode.next;
        }
        return null;
    }

    @Override
    public Value delete(Key k){
        final int keyHash = k.hashCode();
        final int deleteIdx = getIdxFromHash(keyHash);
        // Not found.
        if(hashTable[deleteIdx] == null) return null;
        // Iterate through LL to try & find it.
        Value returnVal = null;
        Node headNode = (Node) hashTable[deleteIdx];
        Node prev = null;
        while(headNode != null){
            // Found it, delete.
            if(k.equals(headNode.key)){
                returnVal = headNode.value;
                // Is head node.
                if(prev == null){
                    hashTable[deleteIdx] = headNode.next;
                } else {
                    prev.next = headNode.next;
                }
                break;
            }
            prev = headNode;
            headNode = headNode.next;
        }

        // Calculate load factors and check if we need to resize.
        this.size -= 1;
        final double load = size/(capacity*1.0);
        if(load <= DECREASE_SIZE_LOAD_THRESHOLD){
            resize(this.capacity/2);
        }

        return returnVal;
    }

    private void resize(int newCapacity){
        // 
        System.out.println("Resize to " + newCapacity);
        Object[] resizedHashTable = new Object[newCapacity];
        for(int i=0; i<this.capacity; ++i){
            if(hashTable[i] == null) continue;

            Node headNode = (Node) hashTable[i];
            while(headNode != null){
                Node next = headNode.next;

                final int keyHash = headNode.getKeyHash();
                final int newInsertIdx = getIdxFromHash(keyHash, newCapacity);
                // Insert each at relavant location
                if(resizedHashTable[newInsertIdx] == null){
                    resizedHashTable[newInsertIdx] = headNode;
                    headNode.next = null;
                } else {
                    Node existingHead = (Node) resizedHashTable[newInsertIdx];
                    headNode.next = existingHead;
                    resizedHashTable[newInsertIdx] = headNode;
                }

                headNode = next;
            }
            
        }

        this.hashTable = resizedHashTable;
        this.capacity = newCapacity;
    }

    private int getIdxFromHash(int hash){
        // size is guaranteed to be a power of 2.
        return hash & (this.capacity - 1);
    }

    private int getIdxFromHash(int hash, int size){
        // size is guaranteed to be a power of 2.
        return hash & (size - 1);
    }
    
}