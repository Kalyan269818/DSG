package dsg.serialization;

import java.io.Serializable;
import java.util.Arrays;

// Note: To enable a fair comparison, the name of this class must have the same length as DSGExternalizableMessage  
public class DSGEvaluationBaseMessage implements Serializable {
	
	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* State */
	private boolean bool;
	private int integer;
	private String[] string;

	/* Constructors */
	public DSGEvaluationBaseMessage() {
		this(false, 0, null);
	}
	
	public DSGEvaluationBaseMessage(boolean bool, int integer, String[] string) {
		this.bool = bool;
		this.integer = integer;
		this.string = string;
	}

	// ##############
	// # COMPARISON #
	// ##############
	
	@Override
	public boolean equals(Object object) {
		if(this == object) return true;
		if(object == null) return false;
		if(getClass() != object.getClass()) return false;
		DSGEvaluationBaseMessage other = (DSGEvaluationBaseMessage) object;
		return (bool == other.bool) && (integer == other.integer) && Arrays.equals(string, other.string);
	}

	// ##############
	// # CONVERSION #
	// ##############
	
	public static byte[] message2bytes(DSGEvaluationBaseMessage message) {
		// TODO Implement method
		return null;
	}

	public static DSGEvaluationBaseMessage bytes2message(byte[] array) {
		// TODO Implement method
		return null;
	}

}
