package dsg.network;

import java.io.IOException;
import java.net.SocketAddress;

import dsg.executor.DSGTask;

public abstract class DSGCall implements DSGTask {

	// ##################
	// # INITIALIZATION #
	// ##################
	
	protected DSGCall(DSGCallType type, SocketAddress remote) {
		this.type = type;
		this.remote = remote;
		this.status = DSGCallStatus.PENDING;
		this.exception = null;
	}
	
	// ########
	// # TYPE #
	// ########
	
	/* State */
	private final DSGCallType type;
	
	/* Type */
	public static enum DSGCallType {
		ACCEPT,
		CONNECT,
		SEND,
		RECEIVE,
		CLOSE
	}
	
	/* Getter */
	public DSGCallType getType() {
		return type;
	}
	
	// ###########
	// # NETWORK #
	// ###########
	
	/* State */
	protected DSGNetwork network;
	
	/* Getter and setter */
	public DSGNetwork getNetwork() {
		return network;
	}
	
	public void setNetwork(DSGNetwork network) {
		this.network = network;
	}
	
	// ###########
	// # ADDRESS #
	// ###########
	
	/* State */
	protected SocketAddress remote;
	
	/* Getter and setter */
	public SocketAddress getRemote() {
		return remote;
	}
	
	public void setRemote(SocketAddress remote) {
		this.remote = remote;
	}
	
	// ##########
	// # STATUS #
	// ##########
	
	/* State */
	protected DSGCallStatus status;
	
	/* Type */
	public static enum DSGCallStatus {
		PENDING,
		SUCCESS,
		FAILURE
	}

	/* Getter and setter */
	public DSGCallStatus getStatus() {
		return status;
	}
	
	public void setStatus(DSGCallStatus status) {
		this.status = status;
	}
	
	// #############
	// # EXCEPTION #
	// #############
	
	/* State */
	protected IOException exception;
	
	/* Getter and setter */
	public IOException getException() {
		return exception;
	}
	
	public void setException(IOException exception) {
		this.exception = exception;
	}
	
	// ###########
	// # FACTORY #
	// ###########
	
	public static DSGCall create(DSGCallType type, SocketAddress remote, DSGMessage message) {
		// TODO Implement method
		switch (type) {
		case ACCEPT:
			return new DSGCallAccept();
		case CONNECT:
			return new DSGCallConnect(remote);
		case SEND:
			return new DSGCallSend(remote, message);
		case RECEIVE:
			return new DSGCallReceive(remote, message);
		case CLOSE:
			return new DSGCallClose(remote);
		default:
			throw new IllegalArgumentException("Unknown call type: " + type);
		}
	}
	
}
