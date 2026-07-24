package dsg.network;

import java.io.IOException;
import java.net.SocketAddress;

import dsg.network.DSGCall.DSGCallStatus;
import dsg.network.DSGCall.DSGCallType;

public class DSGClient {

	// ##################
	// # INITIALIZATION #
	// ##################

	/* State */
	private final DSGNetwork network;

	/* Constructor */
	public DSGClient() {
		// TODO Implement constructor
		this.network = new DSGNetwork();
	}

	// #################
	// # COMMUNICATION #
	// #################

	public void connect(SocketAddress to) throws IOException {
		// TODO Implement method
		perform(DSGCall.create(DSGCallType.CONNECT, to, null));
	}

	public void send(SocketAddress to, DSGMessage message) throws IOException {
		// TODO Implement method
		perform(DSGCall.create(DSGCallType.SEND, to, message));
	}

	public void receive(SocketAddress from, DSGMessage message) throws IOException {
		// TODO Implement method
		perform(DSGCall.create(DSGCallType.RECEIVE, from, message));
	}

	public void close(SocketAddress address) throws IOException {
		// TODO Implement method
		perform(DSGCall.create(DSGCallType.CLOSE, address, null));
	}

	/* Used by subclasses (e.g. DSGHTTPClient) to check whether a connection to address is already open */
	protected boolean isConnected(SocketAddress address) {
		return network.isConnected(address);
	}

	/* Submits a call, blocks until it terminates, and signals a failure via an exception */
	private void perform(DSGCall call) throws IOException {
		network.dispatch(call);
		DSGCall finished = network.collect();

		if (finished == null) {
			throw new IOException("Operation interrupted or network already shut down");
		}
		if (finished.getStatus() == DSGCallStatus.FAILURE) {
			throw finished.getException();
		}
	}

	// ############
	// # SHUTDOWN #
	// ############

	public void shutdown() {
		// TODO Implement method
		network.shutdown();
	}

}
