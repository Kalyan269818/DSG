package dsg.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallType;

public class DSGCallReceive extends DSGCall {

	/* State */
	private final DSGMessage message;

	/* Constructor */
	protected DSGCallReceive(SocketAddress remote, DSGMessage message) {
		super(DSGCallType.RECEIVE, remote);
		this.message = message;
	}

	/* Getter */
	public DSGMessage getMessage() {
		return message;
	}

	/* Execution */
	@Override
	public void execute() {
		try {
			Socket socket = network.getSocket(remote);
			message.deserialize(socket.getInputStream());
			setStatus(DSGCallStatus.SUCCESS);
		} catch (IOException e) {
			setException(e);
			setStatus(DSGCallStatus.FAILURE);
		}
	}

}
