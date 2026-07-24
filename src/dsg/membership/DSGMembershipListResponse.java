package dsg.membership;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import dsg.network.DSGMessage;

/* Sent by a membership server in response to a DSGMembershipListRequest */
public class DSGMembershipListResponse implements DSGMessage {

	/* State */
	private SocketAddress[] addresses;

	/* Constructors */
	public DSGMembershipListResponse() {
		this.addresses = null;
	}

	public DSGMembershipListResponse(SocketAddress[] addresses) {
		this.addresses = addresses;
	}

	/* Getter */
	public SocketAddress[] getAddresses() {
		return addresses;
	}

	/* Serialization */
	@Override
	public void serialize(OutputStream stream) throws IOException {
		DataOutputStream data = new DataOutputStream(stream);
		data.writeInt(addresses.length);

		for (SocketAddress address : addresses) {
			InetSocketAddress inet = (InetSocketAddress) address;
			data.writeUTF(inet.getHostString());
			data.writeInt(inet.getPort());
		}

		data.flush();
	}

	@Override
	public void deserialize(InputStream stream) throws IOException {
		DataInputStream data = new DataInputStream(stream);
		int count = data.readInt();
		SocketAddress[] result = new SocketAddress[count];

		for (int i = 0; i < count; i++) {
			String host = data.readUTF();
			int port = data.readInt();
			result[i] = new InetSocketAddress(host, port);
		}

		this.addresses = result;
	}

}
