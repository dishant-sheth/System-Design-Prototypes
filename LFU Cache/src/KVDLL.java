package src;

class KVDLL {
    // Track head and tail of DLL.
    private KVNode head = null, tail = null;
    private int size = 0;

    void insertAtHead(KVNode node){
        size += 1;
        if(head == null && tail == null){
            head = node;
            tail = node;
            return;
        }

        // Make new node head.
        node.next = head;
        head.prev = node;
        head = node;
        head.prev = null;
    }

    void removeNode(KVNode node){
        size -= 1;
        if(node == null) return;
        if(node == head && node == tail){
            head = null;
            tail = null;
        }
        else if(node == head){
            head = head.next;
            head.prev = null;
            node.next = null;
        } 
        else if(node == tail){
            tail = tail.prev;
            tail.next = null;
            node.prev = null;
        }
        else {
            if(node.prev != null) node.prev.next = node.next;
            if(node.next != null) node.next.prev = node.prev;
            node.prev = null;
            node.next = null;
        }
    }

    KVNode removeTail(){
        this.size -= 1;
        if(this.tail == null) return null;
        if(head == tail){
            KVNode returnNode = tail;
            head = null;
            tail = null;
            return returnNode;
        }
        KVNode returnNode = tail;
        tail = tail.prev;
        tail.next = null;
        return returnNode;
    }

    int getSize(){
        return this.size;
    }
    
}