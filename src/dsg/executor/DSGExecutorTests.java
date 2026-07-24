package dsg.executor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;


public class DSGExecutorTests {

    @Test
    public void testDataTypes(){
        Class<?> executor = DSGExecutorService.class;

        for(Field field: executor.getDeclaredFields()){
            if(Executor.class.isAssignableFrom(field.getType())){
                fail("Usage of Executor data types is not allowed for the executor service solution which was used in variable: " + field.getName() + " of type: " + field.getType());
            }
        }
    }
	
	@RepeatedTest(10)
    public void testParallelCounter() throws InterruptedException{ 
        // Create an instance of Random and create number values with it
        Random random = new Random();
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            numbers.add(random.nextInt(2000));
        }

        // Create an ExecutorService
		DSGExecutorService executor = new DSGExecutorService(10);

        // Reset the DSGParallelCounter counter to ensure no old test can influence the results
        DSGParallelCounter.reset_counter();

        // Submit all numbers as DSGParallelCounter tasks to be added concurrently
        for (Integer n: numbers){
            executor.dispatch(new DSGParallelCounter(n));
        }

        // Add all numbers sequentially
        int sum = 0;
        for (Integer n: numbers){
            sum += n;
        }

        // Shutdown Executor Service
        executor.shutdown();

        // Compare result of concurrent and sequential addition
        assertEquals(sum, DSGParallelCounter.get_counter());
    }
	
	
	public class DSGParallelCounter implements DSGTask {

		// #########
		// # STATE #
		// #########

	    /* Shared thread-safe counter */
	    private static AtomicInteger counter = new AtomicInteger(0);

	    /* Task property */
	    private int value;

	    /* Constructor */
	    public DSGParallelCounter(int value){
	        this.value = value;
	    }

		// ###########
		// # EXECUTE #
		// ###########

	    @Override
	    public void execute() {
	        counter.addAndGet(this.value);
	    }
	    
		// ##########
		// # GETTER #   /* Required for testing purposes */
		// ##########

	    public static int get_counter(){
	        return counter.get();
	    }

		// #########
		// # RESET #   /* Required for testing purposes */
		// #########

	    public static void reset_counter(){
	        counter = new AtomicInteger(0);
	    }
	}
}