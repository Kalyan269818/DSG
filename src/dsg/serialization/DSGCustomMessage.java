package dsg.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
	
	/* Written in place of the array length to indicate that "strings" itself is null; safely distinguishable from any real length since at most 10 elements are ever expected */
	private static final int NULL_ARRAY_MARKER = 0xFF;

	public void serialize(OutputStream output) throws IOException {
		// TODO Implement method
		DataOutputStream data = new DataOutputStream(output);

		// "integer" is guaranteed to be in [0, 10000], which fits into 14 bits; pack it together with "bool" into a single 16-bit value
		int packed = (integer & 0x3FFF) | (bool ? 0x4000 : 0);
		data.writeShort(packed);

		if (strings == null) {
			data.writeByte(NULL_ARRAY_MARKER);
		} else {
			int length = strings.length;
			data.writeByte(length);

			// One presence bit per element instead of a full byte, indicating which elements are non-null
			byte[] presence = new byte[(length + 7) / 8];
			for (int i = 0; i < length; i++) {
				if (strings[i] != null) presence[i / 8] |= (1 << (i % 8));
			}
			data.write(presence);

			for (String s : strings) {
				if (s == null) continue;
				byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
				data.writeByte(utf8.length); // safe: at most 50 chars, well below 255 bytes even at 4 bytes/char
				data.write(utf8);
			}
		}

		data.flush();
	}

	public void deserialize(InputStream input) throws IOException {
		// TODO Implement method
		DataInputStream data = new DataInputStream(input);

		int packed = data.readUnsignedShort();
		bool = (packed & 0x4000) != 0;
		integer = packed & 0x3FFF;

		int marker = data.readUnsignedByte();
		if (marker == NULL_ARRAY_MARKER) {
			strings = null;
		} else {
			int length = marker;
			strings = new String[length];

			byte[] presence = new byte[(length + 7) / 8];
			data.readFully(presence);

			for (int i = 0; i < length; i++) {
				if ((presence[i / 8] & (1 << (i % 8))) == 0) continue;
				int size = data.readUnsignedByte();
				byte[] utf8 = new byte[size];
				data.readFully(utf8);
				strings[i] = new String(utf8, StandardCharsets.UTF_8);
			}
		}
	}

	// ##############
	// # CONVERSION #
	// ##############

	public static byte[] message2bytes(DSGCustomMessage message) {
		// TODO Implement method
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			message.serialize(bytes);
			return bytes.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	public static DSGCustomMessage bytes2message(byte[] array) {
		// TODO Implement method
		try {
			ByteArrayInputStream bytes = new ByteArrayInputStream(array);
			DSGCustomMessage message = new DSGCustomMessage();
			message.deserialize(bytes);
			return message;
		} catch (IOException e) {
			return null;
		}
	}

}
