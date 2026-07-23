package dsg.network;

import java.io.IOException;
import java.net.SocketAddress;

public class DSGClient {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	/* Constructor */
	public DSGClient() {
		// TODO Implement constructor
	}
	
	// #################
	// # COMMUNICATION #
	// #################
	
	public void connect(SocketAddress to) throws IOException {
		// TODO Implement method
	}
	
	public void send(SocketAddress to, DSGMessage message) throws IOException {
		// TODO Implement method
	}
	
	public void receive(SocketAddress from, DSGMessage message) throws IOException {
		// TODO Implement method
	}
	
	public void close(SocketAddress address) throws IOException {
		// TODO Implement method
	}
	
	// ############
	// # SHUTDOWN #
	// ############
	
	public void shutdown() {
		// TODO Implement method
	}
	
}
