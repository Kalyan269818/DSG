package dsg.serialization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class DSGExternalizableMessage implements Externalizable {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* State */
	private boolean bool;
	private int integer;
	private String[] strings;

	/* Constructors */
	public DSGExternalizableMessage() {
		this(false, 0, null);
	}
	
	public DSGExternalizableMessage(boolean bool, int integer, String[] strings) {
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
		DSGExternalizableMessage other = (DSGExternalizableMessage) object;
		return (bool == other.bool) && (integer == other.integer) && Arrays.equals(strings, other.strings);
	}

	// #################
	// # SERIALIZATION #
	// #################
	
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		// TODO Implement method
	}
	
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		// TODO Implement method
	}
	
	// ##############
	// # CONVERSION #
	// ##############
	
	public static byte[] message2bytes(DSGExternalizableMessage message) {
		// TODO Implement method
		return null;
	}

	public static DSGExternalizableMessage bytes2message(byte[] array) {
		// TODO Implement method
		return null;
	}

}
