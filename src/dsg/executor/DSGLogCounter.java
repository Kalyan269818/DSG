package dsg.executor;

public class DSGLogCounter implements DSGTask {

	// #########
	// # STATE #
	// #########


    /* Constructor */
    public DSGLogCounter(String filepath, DSGLogType type){
        // TODO implement constructor
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
    }



	// ##########
	// # GETTER #   /* Required for testing purposes */
	// ##########

    public DSGLogType getType() {
    	// TODO implement method
        return null;
    }
    
    public int getResult(){
    	// TODO implement method
        return 0;
    }
    
    
}