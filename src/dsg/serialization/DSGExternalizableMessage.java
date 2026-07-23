package dsg.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
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

	public static byte[] message2bytes(DSGExternalizableMessage message) {
		// TODO Implement method
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream objects = new ObjectOutputStream(bytes);
			objects.writeObject(message);
			objects.flush();
			return bytes.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static DSGExternalizableMessage bytes2message(byte[] array) {
		// TODO Implement method
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(array);
			ObjectInputStream objects = new ObjectInputStream(bytes);
			return (DSGExternalizableMessage) objects.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

}
