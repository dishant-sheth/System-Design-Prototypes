package src;

class KVNode {
    // only key is immutable.
    private final Object key;
    private Object value;
    private int frequency;
    // Use inside DLL - prev & next pointers.
    KVNode prev, next;

    KVNode(final Object key, final Object value){
        this.key = key;
        this.value = value;
        this.prev = null;
        this.next = null;
        this.frequency = 1;
    }

    Object getKey(){
        return this.key;
    }

    Object getValue(){
        return this.value;
    }

    void updateValue(final Object value){
        this.value = value;
    }

    void increaseFrequency(){
        ++this.frequency;
    }

    int getFrequency(){
        return this.frequency;
    }

    @Override
    public String toString(){
        return "(" + key.toString() + ", " + value.toString() + " -> " + this.frequency + ")";
    }
}