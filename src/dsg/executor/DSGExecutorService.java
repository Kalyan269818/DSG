package dsg.executor;

public class DSGExecutorService {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	
	/* Constructor - creates a new executor service with <threads> number of worker threads*/
	public DSGExecutorService(int threads) {
		// TODO implement constructor
	}

	
	// ##########
	// # QUEUES #
	// ##########

	
	/* Dispatch new task for execution */
	public void dispatch(DSGTask task) {
		// TODO implement method
	}

	/* Collect results from a finished task */
	public DSGTask collect() {
		// TODO implement method
		return null;
	}
	
	// ############
	// # SHUTDOWN #
	// ############

	/* Terminate worker threads and await their termination */
	public void shutdown() throws InterruptedException {
		// TODO implement method
	}
	
}
