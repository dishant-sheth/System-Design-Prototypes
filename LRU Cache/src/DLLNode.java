package src;

class DLLNode {
    private final Object key;
    private Object value;
    private long expiryTime;
    DLLNode next;
    DLLNode prev;

    public DLLNode(Object key, Object value, long expiryTime){
        this.key = key;
        this.value = value;
        this.next = null;
        this.prev = null;
        this.expiryTime = expiryTime;
    }

    Object getKey(){
        return this.key;
    }

    void setValue(Object value){
        this.value = value;
    }

    Object getValue(){
        return this.value;
    }

    long getExpiryTime(){
        return this.expiryTime;
    }

    void updateExpiryTime(long expiryTime){
        this.expiryTime = expiryTime;
    }

    @Override
    public String toString(){
        return this.key.toString() + " " + this.value.toString() + " " + this.expiryTime;
    }
}