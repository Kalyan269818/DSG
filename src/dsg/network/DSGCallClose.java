package dsg.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallType;

public class DSGCallClose extends DSGCall {

	/* Constructor */
	protected DSGCallClose(SocketAddress remote) {
		super(DSGCallType.CLOSE, remote);
	}

	/* Execution */
	@Override
	public void execute() {
		try {
			Socket socket = network.removeSocket(remote);
			if (socket == null) {
				throw new IOException("No such connection: " + remote);
			}

			socket.close();
			setStatus(DSGCallStatus.SUCCESS);
		} catch (IOException e) {
			setException(e);
			setStatus(DSGCallStatus.FAILURE);
		}
	}

}
