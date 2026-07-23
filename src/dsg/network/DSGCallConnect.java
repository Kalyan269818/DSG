package dsg.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallType;

public class DSGCallConnect extends DSGCall {

	/* Constructor */
	protected DSGCallConnect(SocketAddress remote) {
		super(DSGCallType.CONNECT, remote);
	}

	/* Execution */
	@Override
	public void execute() {
		try {
			Socket socket = new Socket();
			socket.connect(remote);

			network.putSocket(remote, socket);
			setStatus(DSGCallStatus.SUCCESS);
		} catch (IOException e) {
			setException(e);
			setStatus(DSGCallStatus.FAILURE);
		}
	}

}
