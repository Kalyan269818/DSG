package dsg.executor;

public class DSGBlockingQueue<V>{

    private Object[] elements;
    private int head;
    private int count;

	// ##################
	// # INITIALIZATION #
	// ##################

    /* Constructor */
    public DSGBlockingQueue(){
        // TODO implement constructor
        elements = new Object[16];
        head = 0;
        count = 0;
    }

	// ##########
	// # ACCESS #
	// ##########


    /* Inserts element to queue (non blocking operation) */
    public void insert(V value) {
    	// TODO implement method
        synchronized (this) {
            if (count == elements.length) {
                Object[] resized = new Object[elements.length * 2];
                for (int i = 0; i < count; i++) {
                    resized[i] = elements[(head + i) % elements.length];
                }
                elements = resized;
                head = 0;
            }

            elements[(head + count) % elements.length] = value;
            count++;

            notify();
        }
    }

    /* Retrieves element from head of the queue - wait if the queue is empty (blocking operation) */
    public V retrieve() throws InterruptedException {
    	// TODO implement method
        synchronized (this) {
            while (count == 0) {
                wait();
            }

            @SuppressWarnings("unchecked")
            V value = (V) elements[head];
            elements[head] = null;
            head = (head + 1) % elements.length;
            count--;

            return value;
        }
    }

    /* Size of the BlockingQueue */
    public int size() {
    	// TODO implement method
        synchronized (this) {
            return count;
        }
    }
}