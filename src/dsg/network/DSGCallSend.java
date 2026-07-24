package dsg.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallType;

public class DSGCallSend extends DSGCall {

	/* State */
	private final DSGMessage message;

	/* Constructor */
	protected DSGCallSend(SocketAddress remote, DSGMessage message) {
		super(DSGCallType.SEND, remote);
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
			message.serialize(socket.getOutputStream());
			setStatus(DSGCallStatus.SUCCESS);
		} catch (IOException e) {
			setException(e);
			setStatus(DSGCallStatus.FAILURE);
		}
	}

}
