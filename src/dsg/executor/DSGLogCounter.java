package dsg.executor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DSGLogCounter implements DSGTask {

	// #########
	// # STATE #
	// #########

    private String filepath;
    private DSGLogType type;
    private int result;

    /* Constructor */
    public DSGLogCounter(String filepath, DSGLogType type){
        // TODO implement constructor
        this.filepath = filepath;
        this.type = type;
        this.result = 0;
    }

	// ########
	// # TYPE #
	// ########

    public enum DSGLogType {
        INFO,
        WARN,
        ERROR
    }

	// ###########
	// # EXECUTE #
	// ###########

    @Override
    public void execute() {
        // TODO implement method
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            String prefix = type.name() + " ";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(prefix) || line.equals(type.name())) {
                    result++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



	// ##########
	// # GETTER #   /* Required for testing purposes */
	// ##########

    public DSGLogType getType() {
    	// TODO implement method
        return type;
    }

    public int getResult(){
    	// TODO implement method
        return result;
    }
    
    
}