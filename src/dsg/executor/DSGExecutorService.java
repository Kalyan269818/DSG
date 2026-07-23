package dsg.executor;

public class DSGExecutorService {

	private static final DSGTask INPUT_POISON_PILL = new DSGTask() {
		public void execute() {}
	};

	private static final DSGTask OUTPUT_SHUTDOWN_SIGNAL = new DSGTask() {
		public void execute() {}
	};

	private final DSGBlockingQueue<DSGTask> inputQueue;
	private final DSGBlockingQueue<DSGTask> outputQueue;
	private final Thread[] workers;

	// ##################
	// # INITIALIZATION #
	// ##################


	/* Constructor - creates a new executor service with <threads> number of worker threads*/
	public DSGExecutorService(int threads) {
		// TODO implement constructor
		inputQueue = new DSGBlockingQueue<DSGTask>();
		outputQueue = new DSGBlockingQueue<DSGTask>();
		workers = new Thread[threads];

		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Thread(() -> {
				while (true) {
					DSGTask task;
					try {
						task = inputQueue.retrieve();
					} catch (InterruptedException e) {
						break;
					}

					if (task == INPUT_POISON_PILL) {
						break;
					}

					task.execute();
					outputQueue.insert(task);
				}
			});
			workers[i].start();
		}
	}


	// ##########
	// # QUEUES #
	// ##########


	/* Dispatch new task for execution */
	public void dispatch(DSGTask task) {
		// TODO implement method
		inputQueue.insert(task);
	}

	/* Collect results from a finished task */
	public DSGTask collect() {
		// TODO implement method
		try {
			DSGTask task = outputQueue.retrieve();

			if (task == OUTPUT_SHUTDOWN_SIGNAL) {
				outputQueue.insert(OUTPUT_SHUTDOWN_SIGNAL);
				return null;
			}

			return task;
		} catch (InterruptedException e) {
			return null;
		}
	}

	// ############
	// # SHUTDOWN #
	// ############

	/* Terminate worker threads and await their termination */
	public void shutdown() throws InterruptedException {
		// TODO implement method
		for (int i = 0; i < workers.length; i++) {
			inputQueue.insert(INPUT_POISON_PILL);
		}

		for (Thread worker : workers) {
			worker.join();
		}

		outputQueue.insert(OUTPUT_SHUTDOWN_SIGNAL);
	}

}
