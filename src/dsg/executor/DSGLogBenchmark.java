package dsg.executor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dsg.executor.DSGLogCounter.DSGLogType;

public class DSGLogBenchmark {

	// ##############
	// # PARAMETERS #
	// ##############

	private static final int NUM_FILES = 100;
	private static final int NUM_INFO = 20_000_000;
	private static final int NUM_WARN = 10_000_000;
	private static final int NUM_ERROR = 10_000_000;
	private static final long SEED = 987654321L;

	private static final int MIN_THREADS = 1;
	private static final int MAX_THREADS = 16;
	private static final int REPETITIONS = 5;

	// ##############
	// # GENERATION #
	// ##############

	/* Removes any previously generated log files and creates <NUM_FILES> new ones for the benchmark */
	private static String[] prepareLogFiles(Path directory) throws IOException {
		if (Files.exists(directory)) {
			try (DirectoryStream<Path> files = Files.newDirectoryStream(directory)) {
				for (Path file : files) {
					Files.deleteIfExists(file);
				}
			}
		} else {
			Files.createDirectories(directory);
		}

		return DSGLogFileGenerator.generateLogFiles(directory.toString(), NUM_FILES, SEED, NUM_INFO, NUM_WARN, NUM_ERROR);
	}

	// #############
	// # BENCHMARK #
	// #############

	/* Performs a full DSGLogCounter analysis of all log files with <threads> worker threads and returns the elapsed time in milliseconds */
	private static long runOnce(String[] filepaths, int threads) throws InterruptedException {
		DSGExecutorService executor = new DSGExecutorService(threads);

		long start = System.nanoTime();

		for (String filepath : filepaths) {
			executor.dispatch(new DSGLogCounter(filepath, DSGLogType.INFO));
		}

		for (int i = 0; i < filepaths.length; i++) {
			executor.collect();
		}

		long elapsed = System.nanoTime() - start;

		executor.shutdown();

		return elapsed / 1_000_000;
	}

	/* Repeats the benchmark <REPETITIONS> times for <threads> worker threads and returns the average execution time in milliseconds */
	private static double benchmarkThreadCount(String[] filepaths, int threads) throws InterruptedException {
		long total = 0;

		for (int r = 0; r < REPETITIONS; r++) {
			total += runOnce(filepaths, threads);
		}

		return total / (double) REPETITIONS;
	}

	// ########
	// # MAIN #
	// ########

	public static void main(String[] args) throws IOException, InterruptedException {
		Path directory = Paths.get(System.getProperty("user.dir"), "logfiles-benchmark");

		System.out.println("Generating " + NUM_FILES + " log files with " + NUM_INFO + " INFO, " + NUM_WARN
				+ " WARN and " + NUM_ERROR + " ERROR entries at " + directory + " ...");
		String[] filepaths = prepareLogFiles(directory);
		System.out.println("Done generating log files.");

		System.out.println("threads,avg_ms");
		for (int threads = MIN_THREADS; threads <= MAX_THREADS; threads++) {
			double avg = benchmarkThreadCount(filepaths, threads);
			System.out.println(threads + "," + avg);
		}
	}

}
