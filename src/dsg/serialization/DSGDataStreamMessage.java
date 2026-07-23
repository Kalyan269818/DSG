package dsg.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DSGDataStreamMessage {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* State */
	private boolean bool;
	private int integer;
	private String[] strings;

	/* Constructors */
	public DSGDataStreamMessage() {
		this(false, 0, null);
	}
	
	public DSGDataStreamMessage(boolean bool, int integer, String[] strings) {
		this.bool = bool;
		this.integer = integer;
		this.strings = strings;
	}

	// ##############
	// # COMPARISON #
	// ##############
	
	@Override
	public boolean equals(Object object) {
		if(this == object) return true;
		if(object == null) return false;
		if(getClass() != object.getClass()) return false;
		DSGDataStreamMessage other = (DSGDataStreamMessage) object;
		return (bool == other.bool) && (integer == other.integer) && Arrays.equals(strings, other.strings);
	}

	// #################
	// # SERIALIZATION #
	// #################
	
	public void serialize(DataOutputStream output) throws IOException {
		// TODO Implement method
	}
	
	public void deserialize(DataInputStream input) throws IOException {
		// TODO Implement method
	}

	// ##############
	// # CONVERSION #
	// ##############
	
	public static byte[] message2bytes(DSGDataStreamMessage message) {
		// TODO Implement method
		return null;
	}

	public static DSGDataStreamMessage bytes2message(byte[] array) {
		// TODO Implement method
		return null;
	}

}
