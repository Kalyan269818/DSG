package dsg.network;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallStatus;
import dsg.network.DSGCall.DSGCallType;

public class DSGServer {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* State */
	protected final DSGNetwork network;

	/* Constructor */
	public DSGServer(int port, int threads) throws IOException {
		this.network = new DSGNetwork(port, threads);
	}

	// ##########
	// # EVENTS #
	// ##########
	
	protected void accepted(SocketAddress remote) {
		// Should be left empty; override in subclass if necessary
	}

	protected void sent(SocketAddress to, DSGMessage message) {
		// Should be left empty; override in subclass if necessary
	}

	protected void received(SocketAddress from, DSGMessage message) {
		// Should be left empty; override in subclass if necessary
	}

	protected void terminated(SocketAddress remote) {
		// Should be left empty; override in subclass if necessary
	}
	
	protected void failed(DSGCall call) {
		// Should be left empty; override in subclass if necessary
	}

	// ##############
	// # OPERATIONS #
	// ##############

	public void serve() {
		// TODO Implement method
		network.dispatch(DSGCall.create(DSGCallType.ACCEPT, null, null));

		DSGCall call;
		while ((call = network.collect()) != null) {
			if (call.getStatus() == DSGCallStatus.FAILURE) {
				if (call.getType() == DSGCallType.RECEIVE && call.getException() instanceof EOFException) {
					// Peer closed the connection; close the local socket and report it as terminated
					network.dispatch(DSGCall.create(DSGCallType.CLOSE, call.getRemote(), null));
				} else {
					failed(call);
				}
			} else {
				switch (call.getType()) {
				case ACCEPT:
					accepted(call.getRemote());
					break;
				case SEND:
					sent(call.getRemote(), ((DSGCallSend) call).getMessage());
					break;
				case RECEIVE:
					received(call.getRemote(), ((DSGCallReceive) call).getMessage());
					break;
				case CLOSE:
					terminated(call.getRemote());
					break;
				case CONNECT:
					break;
				}
			}

			if (call.getType() == DSGCallType.ACCEPT) {
				network.dispatch(DSGCall.create(DSGCallType.ACCEPT, null, null));
			}
		}
	}

	public void shutdown() {
		// TODO Implement method
		network.shutdown();
	}

}
