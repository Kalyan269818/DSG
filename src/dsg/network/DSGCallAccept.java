package dsg.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallType;

public class DSGCallAccept extends DSGCall {

	/* Constructor */
	protected DSGCallAccept() {
		super(DSGCallType.ACCEPT, null);
	}

	/* Execution */
	@Override
	public void execute() {
		try {
			ServerSocket serverSocket = network.getServerSocket();
			if (serverSocket == null) {
				throw new IOException("No server socket available");
			}

			Socket socket = serverSocket.accept();
			SocketAddress client = socket.getRemoteSocketAddress();

			network.putSocket(client, socket);
			setRemote(client);
			setStatus(DSGCallStatus.SUCCESS);
		} catch (IOException e) {
			setException(e);
			setStatus(DSGCallStatus.FAILURE);
		}
	}

}
