package src;

import java.util.HashMap;
import java.util.Map;

public class LFUCache<Key, Value> implements ILFUCache<Key, Value> {

    // Cache map.
    private final Map<Key, KVNode> cacheMap;
    // Frequency map
    private final Map<Integer, KVDLL> frequencyLRUMap;

    private final int capacity;
    private int currSize;
    private int minFrequency;

    public LFUCache(int capacity){
        this.capacity = capacity;
        this.currSize = 0;
        this.minFrequency = 1;
        this.cacheMap = new HashMap<>();
        this.frequencyLRUMap = new HashMap<>();
    }

    @Override
    public Value get(final Key key){
        if(cacheMap.containsKey(key)){
            KVNode node = cacheMap.get(key);

            this.adjustFrequency(node);

            return (Value) node.getValue();
        }
        return null;
    }

    @Override
    public void put(final Key key, final Value value) {
        // Check for capacity
        if(this.currSize == this.capacity){
            // Remove node with min frequency.
            KVNode nodeToRemove = frequencyLRUMap.get(this.minFrequency).removeTail();
            cacheMap.remove((Key) nodeToRemove.getKey());

            // Check if min freq needs to be adjusted.
            if(frequencyLRUMap.get(minFrequency).getSize() == 0){
                this.minFrequency += 1;
            }

            this.currSize -= 1;
        }
        // Insert.
        if(!cacheMap.containsKey(key)){
            final KVNode node = new KVNode(key, value);
            cacheMap.put(key, node);
            int currFreq = node.getFrequency(); // Always 1.
            if(!frequencyLRUMap.containsKey(currFreq)){
                frequencyLRUMap.put(currFreq, new KVDLL());
            }
            frequencyLRUMap.get(currFreq).insertAtHead(node);
            ++this.currSize;
            this.minFrequency = currFreq;
        }
        else { // update.
            final KVNode node = cacheMap.get(key);
            node.updateValue(value);
            this.adjustFrequency(node);
        }
        System.out.println(cacheMap);
    }

    private void adjustFrequency(KVNode node){
        int currFreq = node.getFrequency();
        frequencyLRUMap.get(currFreq).removeNode(node);

        node.increaseFrequency();
        int newFreq = node.getFrequency();
        if(!frequencyLRUMap.containsKey(newFreq)){
            frequencyLRUMap.put(newFreq, new KVDLL());
        }
        frequencyLRUMap.get(newFreq).insertAtHead(node);

        // Check if min freq needs to be adjusted.
        if(frequencyLRUMap.get(minFrequency).getSize() == 0){
            this.minFrequency += 1;
        }
    }
}