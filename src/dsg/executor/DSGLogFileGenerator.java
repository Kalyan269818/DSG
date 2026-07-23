package dsg.executor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class DSGLogFileGenerator {

    // Constant for pseudorandom generator
    private final static long SEED = 127493;


	// ####################
	// # LogFileGenerator #
	// ####################

    public static String[] generateLogFiles (String path, int num_files, long seed, int num_info, int num_warn, int num_error){

        // Init counter for each log category
        int remainingInfo = num_info;
        int remainingWarn = num_warn;
        int remainingError = num_error;
        int remainingTotal = remainingInfo + remainingWarn + remainingError;
        int entriesPerFile = remainingTotal / num_files;
        
        // Init random with seed
        Random random = new Random(seed);

        // Init num_files many LogFile writers
        BufferedWriter[] writers = new BufferedWriter[num_files];
        
        // Init result Array containing all newly created LogFilePaths as Strings
        String[] filepaths = new String[num_files];

        // Write each LogFile sequentially
        for (int i = 0; i < writers.length; i++){
            // Adjust entriesPerFile so that the last file shall contain all remaining entries
            if (i == (writers.length-1)) entriesPerFile += ((num_info + num_warn + num_error) % num_files);

            try {
                // Create a new empty LogFile
                Path filepath = Paths.get(path, "DSGLogFile" + i + ".txt");
                writers[i] = Files.newBufferedWriter(filepath);
                filepaths[i] = filepath.toString();
            
                String entry = "";

                // Write entriesPerFile many lines to the current LogFile
                for (int e = 0; e < entriesPerFile; e++){

                    // Randomly generate a value in range [0, remainingTotal[
                    int r = random.nextInt(remainingTotal);

                    // Selection criteria for log entry category
                    if (r < remainingInfo){
                        // Select a random INFO log entry
                        entry = DSGLogFileEntries.info.get(random.nextInt(DSGLogFileEntries.info.size()));
                        remainingInfo--;

                    } else if (r < (remainingInfo + remainingWarn)){
                        // Select a random WARN log entry
                        entry = DSGLogFileEntries.warn.get(random.nextInt(DSGLogFileEntries.warn.size()));
                        remainingWarn--;

                    } else {
                        // Select a random ERROR log entry
                        entry = DSGLogFileEntries.error.get(random.nextInt(DSGLogFileEntries.error.size()));
                        remainingError--;
                    }
                    remainingTotal--;
                    
                    // Write the entry to the LogFile
                    writers[i].write(entry);  
                    if (e < entriesPerFile-1) writers[i].newLine();
                }

                writers[i].close();              

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        assert remainingInfo == 0: "remainingInfo should be 0 but was " + remainingInfo;
        assert remainingWarn == 0: "remainingWarn should be 0 but was " + remainingWarn;
        assert remainingError == 0: "remainingError should be 0 but was " + remainingError;
        assert remainingTotal == 0: "remainingTotal should be 0 but was " + remainingTotal;
        
        return filepaths;
    }
    
  
	// ########
	// # MAIN #
	// ########
    
    public static void main(String[] args){

        // Print help output if requested
        System.out.println();
        if (args.length == 1 && (args[0].equals("-help")) || (args[0].equals("--help"))) {
            printUsage();
            return;
        }

        // Check correct number of arguments
        if (args.length != 4){
            System.out.println("Error: Wrong number of arguments.");
            printUsage();
            System.exit(1);
        }
         
        try {
            // Parse and check arguments 
            int num_files = Integer.parseInt(args[0]);
            int num_info = Integer.parseInt(args[1]);
            int num_warn = Integer.parseInt(args[2]);
            int num_error = Integer.parseInt(args[3]);

            if ((num_info/num_files <= 0) || (num_warn/num_files <= 0) || (num_error/num_files <= 0)){
                System.out.println("Warning: Make sure the number of info/warn/error log entries >= the number of files - otherwise some files can be empty");
                printUsage();
                System.exit(1);
            }

            // Set path where the LogFiles will be saved
            Path path = Paths.get(System.getProperty("user.dir"), "logfiles");
            Files.createDirectories(path);

            // Generate LogFiles
            String[] filepaths = generateLogFiles(path.toString(), num_files, DSGLogFileGenerator.SEED,  num_info, num_warn, num_error);

            // Print Result
            System.out.println("Successfully created the following " + num_files + " LogFiles at " + path.toString() + ".");
            for (String filepath: filepaths) System.out.println("\t" + filepath);
            System.out.println("Together all files contain " + num_info + " INFO log entries " + num_warn + " WARN entries and " + num_error + " ERROR entries.");


        } catch (NumberFormatException e) {
            System.out.println("Error: Arguments must all be integers.");
            printUsage();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    
    private static void printUsage() {
        System.out.println("Usage: java DSGLogFileGenerator <num_files> <num_info> <num_warn> <num_error>");
        System.out.println("Example: java DSGLogFileGenerator 5 170 45 27");
        System.out.println("This will create 5 logfiles with 170 INFO log entries, 45 WARN log entries and 27 ERROR log entries in total.");
        System.out.println("How the log entries are distributed in the logfiles is done at random but the same parameters will result in the same logfiles.");
        System.out.println("All logfiles will be created in the current working directory in the (potentially new) subfolder 'logfiles'");
        System.out.println("Note: Make sure to clean the subfolder where you want to create your logfiles in as there might be logfiles from previous runs already stored");
    }
    
    
    public class DSGLogFileEntries {
        
        static List<String> info = List.of(
            "INFO User registered",
            "INFO User logged in",
            "INFO User logged out", 
            "INFO Password changed", 
            "INFO Session expired",
            "INFO User profile updated", 
            "INFO Request received", 
            "INFO Request processed", 
            "INFO Database connection established", 
            "INFO Query executed successfully", 
            "INFO Service started", 
            "INFO Service stopped", 
            "INFO Message sent", 
            "INFO Message received", 
            "INFO Job started"
        );
            
        static List<String> warn = List.of(
            "WARN Failed login attempt",
            "WARN Invalid request parameters",
            "WARN Slow query detected",
            "WARN Cache miss",
            "WARN High memory usage detected",
            "WARN Disk space running low",
            "WARN Unauthorized access attempt",
            "WARN Retry attempt initiated",
            "WARN Partial failure detected",
            "WARN Task execution delayed"           
        );
                

        static List<String> error = List.of(
            "ERROR Account locked due to multiple failures",
            "ERROR Failed to process request",
            "ERROR Endpoint not found",
            "ERROR Database connection lost",
            "ERROR Failed to write to database",
            "ERROR Failed to reach external service",
            "ERROR Connection timeout",
            "ERROR Out of memory",
            "ERROR Failed to load configuration",
            "ERROR Access denied",
            "ERROR Invalid token",
            "ERROR Failed to process event",
            "ERROR Task execution failed",
            "ERROR Timeout occurred"
        );
    }
}
