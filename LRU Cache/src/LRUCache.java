package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LRUCache<Key, Value> implements ILRUCache<Key, Value>{

    private static final long NO_EXPIRY = Long.MAX_VALUE;
    private static final int DEFAULT_CAPACITY = 8;
    private final int capacity;
    private int size;
    private final Map<Object, DLLNode> cacheMap;
    private final TreeMap<Long, List<Key>> expiryMap;
    private DLLNode head, tail;

    public LRUCache(){
        this(DEFAULT_CAPACITY);
    }

    public LRUCache(int capacity){
        this.capacity = capacity;
        this.size = 0;
        this.cacheMap = new HashMap<>();
        this.expiryMap = new TreeMap<>();
        this.head = null;
        this.tail = null;
    }
    
    @Override
    public synchronized Value get(Key key){
        if(cacheMap.containsKey(key)){
            final DLLNode node = cacheMap.get(key);

            //Check expiry
            // System.out.println("Get At " + System.currentTimeMillis() + " -> ");
            // System.out.println("Node expiry is at -> " + node.getExpiryTime());
            if(node.getExpiryTime() < System.currentTimeMillis()){
                // Remove from cache map
                DLLNode nodeToDelete = cacheMap.remove(key);
                // Remove from DLL
                deleteNode(nodeToDelete);
                return null;
            }
            final Value returnVal = (Value) node.getValue();

            // Adjust recency if not already head.
            moveNodeToHead(node);

            return returnVal;
        }
        return null;
    }

    @Override
    public synchronized void put(Key key, Value value, long ttlInMs){
        
        // 1. Is there sufficient capacity for this put?
        if(this.size == this.capacity){
            boolean hasExpiredKeys = false;
            // Check if there are keys that have already expired.
            final long currTime = System.currentTimeMillis();
            while(!expiryMap.isEmpty() && expiryMap.firstKey() < currTime){
                // Delete the expired key.
                // (1) Expiry Map
                final List<Key> keysToBeDeleted = expiryMap.remove(expiryMap.firstKey());
                for(Key keyToDelete: keysToBeDeleted){
                    // (2) Cache map
                    DLLNode nodeToDelete = cacheMap.remove(keyToDelete);
                    // (3) DLL
                    deleteNode(nodeToDelete);
                    hasExpiredKeys = true;
                    --this.size;
                }
            }

            if(!hasExpiredKeys & tail != null){
                final Key keyToDelete = (Key) tail.getKey();
                DLLNode nodeToDelete = cacheMap.remove(keyToDelete);
                expiryMap.get(nodeToDelete.getExpiryTime()).remove(keyToDelete);
                deleteNode(nodeToDelete);
                --this.size;
            }
        }

        // 2. Add new node or update existing node.
        final long expiryTime = (ttlInMs != NO_EXPIRY) ? System.currentTimeMillis() + ttlInMs : NO_EXPIRY;
        if(!cacheMap.containsKey(key)){
            final DLLNode node = new DLLNode(key, value, expiryTime);
            // (1) Insert in Cache map.
            cacheMap.put(key, node);
            // (2) Insert in DLL
            if(head == null && tail == null){
                head = node;
                tail = node;   
            }
            else {
                node.next = head;
                head.prev = node;
                head = node;
            }
        } else {
            // (1) Update node.
            final DLLNode recentlyUpdatedNode = cacheMap.get(key);
            recentlyUpdatedNode.setValue(value);
            moveNodeToHead(recentlyUpdatedNode);
            // (2) Modify expiry
            long oldExpiryTime = recentlyUpdatedNode.getExpiryTime();
            expiryMap.get(oldExpiryTime).remove(key);
            recentlyUpdatedNode.updateExpiryTime(expiryTime);
        }

        // (3) Insert in expiry map.
        if(!expiryMap.containsKey(expiryTime)){
            expiryMap.put(expiryTime, new ArrayList<>());
        }
        expiryMap.get(expiryTime).add(key);

        // System.out.println("At " + System.currentTimeMillis() + " -> ");
        // System.out.println(cacheMap);
        
        ++this.size;
    }

    @Override
    public synchronized void put(Key key, Value value){
        this.put(key, value, NO_EXPIRY);
    }

    public synchronized int size() {
        return this.size;
    }

    private void moveNodeToHead(DLLNode node){
        // Adjust recency if not already head.
        if(tail == node){
            tail = tail.prev;
            if(tail != null) tail.next = null;
        }
        if(head != node){
            if(node.prev != null) node.prev.next = node.next;
            if(node.next != null) node.next.prev = node.prev;
            node.next = head;
            head.prev = node;
            node.prev = null;
            head = node;
        }
    }

    private void deleteNode(DLLNode node){
        if(node == null) return;
        if(node.prev != null) node.prev.next = node.next;
        if(node.next != null) node.next.prev = node.prev;
        if(head == node){
            head = head.next;
        }
        if(tail == node){
            tail = tail.prev;
        }
    }
}