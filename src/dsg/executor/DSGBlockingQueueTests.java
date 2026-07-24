package dsg.executor;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class DSGBlockingQueueTests {

    // #########
	// # TESTS #
	// #########
    
    @Test
    public void testDataTypes(){
        Class<?> queue = DSGBlockingQueue.class;

        for(Field field: queue.getDeclaredFields()){
            if(Collection.class.isAssignableFrom(field.getType())){
                fail("Usage of collection data types is not allowed for the blocking queue solution which was used in variable: " + field.getName() + " of type: " + field.getType());
            }
        }
    }


    @Test
    public void testSingleThreadedQueueAccess(){
        // In case queue is not implemented correctly and blocks indefinitely, timeout after 10 seconds
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {

            DSGBlockingQueue<Integer> queue = new DSGBlockingQueue<Integer>();

            int[] values = createRandomIntArray(100);

            for (int val: values){
                queue.insert(val);
            }

            for (int val: values){
                try {
                    assertEquals(val, queue.retrieve());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Test execution exceeded 10 seconds ");    
    }

    @RepeatedTest(10)
    public void testMultiThreadedQueueAccess(){
        // In case queue is not implemented correctly and blocks indefinitely, timeout after 10 seconds
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {

            // Init queue, thread pools and input arrays
            DSGBlockingQueue<Integer> queue = new DSGBlockingQueue<Integer>();
            int num_providers = 2;
            int num_consumers = 2;
            int array_length = 100;
            int[][] values = new int[num_providers][];
            ExecutorService executor = Executors.newFixedThreadPool(num_providers + num_consumers);
            

            // Create and provide input for queue in parallel
            for (int i = 0; i < num_providers; i++){
                int[] vals = createRandomIntArray(array_length);
                values[i] = vals;

                executor.execute(() -> {
                    for (int v = 0; v < vals.length; v++){
                        queue.insert(vals[v]);
                    }
                });
            }
            
            // Compute the sum of the values in parallel by collecting the results from the queue
            AtomicInteger parallel_sum = new AtomicInteger(0);
            
            for (int i = 0; i < num_consumers; i++){
                executor.execute(() -> {
                    try {
                    for (int v = 0; v < array_length; v++){
                            parallel_sum.addAndGet(queue.retrieve());
                        }
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                });
            }

            // Compute sum of the values sequentially for comparison
            int sequential_sum = 0;
            for (int i = 0; i < values.length; i++){
                for (int j = 0; j < values[i].length; j++){
                    sequential_sum += values[i][j];
                }
            }

            // Shutdown executor
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }

            // Compare results
            assertEquals(sequential_sum, parallel_sum.get());

        }, "Test execution exceeded 10 seconds ");
    }


    // ##########
	// # RANDOM #
	// ##########

    public int[] createRandomIntArray(int length){
        int[] result = new int[length];
        Random random = new Random();

        for (int i = 0; i < length; i++){
            result[i] = random.nextInt();
        }

        return result;
    }
}
