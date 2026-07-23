package dsg.network;

import java.io.IOException;
import java.net.SocketAddress;

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
	}

	public void shutdown() {
		// TODO Implement method
	}

}
