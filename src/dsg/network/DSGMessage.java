package dsg.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DSGMessage {

	/** Writes this message to the output stream */
	public void serialize(OutputStream stream) throws IOException;
	
	/** Reads data from the input stream into this message */
	public void deserialize(InputStream stream) throws IOException;
	
}
