import src.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<Integer, Integer> map = new HashMap<>();
        int NUM_KEYS = 100_000;

        // PUT
        long start = System.nanoTime();
        for (int i = 0; i < NUM_KEYS; i++) {
            map.put(i, i);
        }
        long end = System.nanoTime();
        System.out.println("Put " + NUM_KEYS + " keys: " + (end - start) / 1_000_000 + "ms");

        // GET - hits
        start = System.nanoTime();
        for (int i = 0; i < NUM_KEYS; i++) {
            int val = map.get(i);
            if (val != i) System.out.println("GET MISMATCH at key " + i);
        }
        end = System.nanoTime();
        System.out.println("Get " + NUM_KEYS + " keys: " + (end - start) / 1_000_000 + "ms");

        // GET - misses
        start = System.nanoTime();
        for (int i = NUM_KEYS; i < NUM_KEYS * 2; i++) {
            if (map.get(i) != null) System.out.println("GET MISS FAILED at key " + i);
        }
        end = System.nanoTime();
        System.out.println("Get " + NUM_KEYS + " misses: " + (end - start) / 1_000_000 + "ms");

        // DELETE
        start = System.nanoTime();
        for (int i = 0; i < NUM_KEYS; i++) {
            int val = map.delete(i);
            if (val != i) System.out.println("DELETE MISMATCH at key " + i);
        }
        end = System.nanoTime();
        System.out.println("Delete " + NUM_KEYS + " keys: " + (end - start) / 1_000_000 + "ms");

        // Verify empty
        System.out.println("Size after delete: " + map.size() + " (expected 0)");
        System.out.println("isEmpty: " + map.isEmpty() + " (expected true)");
    }
}