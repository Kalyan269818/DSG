package dsg.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
		output.writeBoolean(bool);
		output.writeInt(integer);

		if (strings == null) {
			output.writeInt(-1);
		} else {
			output.writeInt(strings.length);
			for (String s : strings) {
				output.writeBoolean(s != null);
				if (s != null) output.writeUTF(s);
			}
		}
	}

	public void deserialize(DataInputStream input) throws IOException {
		// TODO Implement method
		bool = input.readBoolean();
		integer = input.readInt();

		int length = input.readInt();
		if (length < 0) {
			strings = null;
		} else {
			strings = new String[length];
			for (int i = 0; i < length; i++) {
				if (input.readBoolean()) strings[i] = input.readUTF();
			}
		}
	}

	// ##############
	// # CONVERSION #
	// ##############

	public static byte[] message2bytes(DSGDataStreamMessage message) {
		// TODO Implement method
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			message.serialize(data);
			data.flush();
			return bytes.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static DSGDataStreamMessage bytes2message(byte[] array) {
		// TODO Implement method
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(array);
			DataInputStream data = new DataInputStream(bytes);
			DSGDataStreamMessage message = new DSGDataStreamMessage();
			message.deserialize(data);
			return message;
		} catch (IOException e) {
			return null;
		}
	}

}
