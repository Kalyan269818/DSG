package dsg.executor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.RepeatedTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import dsg.executor.DSGLogCounter.DSGLogType;

public class DSGLogCounterTests {
    
    @RepeatedTest(10)
    public void testParallelLogAnalyzer() throws InterruptedException {
        // Define number of LogFiles and LogFile entries
        Random random = new Random();
        int num_files = random.nextInt(5, 10);
        int num_info = 15*num_files + random.nextInt(100000);
        int num_warn = 10*num_files + random.nextInt(75000);
        int num_error = 7*num_files + random.nextInt(45000);
        long SEED = 123456L;

        // Prepare folder for LogFiles (directory exists and is empty before the LogFile creation)
        Path path = Paths.get(System.getProperty("user.dir"), "logfiles");
        try {
        	Files.createDirectories(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Create num_files many LogFiles
        String[] filepaths = DSGLogFileGenerator.generateLogFiles(path.toString(), num_files, SEED, num_info, num_warn, num_error);

        // Create an ExecutorService with num_files worker threads
		DSGExecutorService executor = new DSGExecutorService(num_files);

        // Create a new DSGLogAnalyzer (DSGTask) for each LogFile in the directory, ’path' is pointing to and for each type of log entry
        for (String filepath: filepaths){
                executor.dispatch(new DSGLogCounter(filepath, DSGLogType.INFO));
                executor.dispatch(new DSGLogCounter(filepath, DSGLogType.WARN));
                executor.dispatch(new DSGLogCounter(filepath, DSGLogType.ERROR));
        }
        

        // Shutdown the Executor Service
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Compute results
        int counter_info = 0;
        int counter_warn = 0;
        int counter_error = 0;
        
        for(int i = 0; i < 3 * num_files; i++) {
        	DSGLogCounter result = (DSGLogCounter) executor.collect();
        	switch (result.getType()) {
        	case INFO:
        		counter_info += result.getResult();
        		break;
        	case WARN:
        		counter_warn += result.getResult();
        		break;
        	case ERROR:
        		counter_error += result.getResult();
        		break;
        	}
        }

        // Compare results
        assertEquals(num_info, counter_info, "The number of INFO log entries from the executor service do not match");
        assertEquals(num_warn, counter_warn, "The number of WARN log entries from the executor service do not match");
        assertEquals(num_error, counter_error, "The number of ERROR log entries from the executor service do not match");
    }
}
