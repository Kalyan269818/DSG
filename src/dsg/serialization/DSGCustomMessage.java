package dsg.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class DSGCustomMessage {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* State */
	private boolean bool;
	private int integer;
	private String[] strings;

	/* Constructors */
	public DSGCustomMessage() {
		this(false, 0, null);
	}
	
	public DSGCustomMessage(boolean bool, int integer, String[] strings) {
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
		DSGCustomMessage other = (DSGCustomMessage) object;
		return (bool == other.bool) && (integer == other.integer) && Arrays.equals(strings, other.strings);
	}

	// #################
	// # SERIALIZATION #
	// #################
	
	public void serialize(OutputStream output) throws IOException {
		// TODO Implement method
	}
	
	public void deserialize(InputStream input) throws IOException {
		// TODO Implement method
	}

	// ##############
	// # CONVERSION #
	// ##############
	
	public static byte[] message2bytes(DSGCustomMessage message) {
		// TODO Implement method
		return null;
	}

	public static DSGCustomMessage bytes2message(byte[] array) {
		// TODO Implement method
		return null;
	}

}
