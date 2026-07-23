package dsg.membership;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dsg.network.DSGMessage;

/* Sent by a client to request the current list of active clients from a membership server */
public class DSGMembershipListRequest implements DSGMessage {

	@Override
	public void serialize(OutputStream stream) throws IOException {
		stream.write(1);
	}

	@Override
	public void deserialize(InputStream stream) throws IOException {
		int b = stream.read();
		if (b < 0) throw new EOFException();
	}

}
