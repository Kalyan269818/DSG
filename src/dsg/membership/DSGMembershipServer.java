package dsg.membership;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import dsg.network.DSGCall;
import dsg.network.DSGCall.DSGCallType;
import dsg.network.DSGMessage;
import dsg.network.DSGServer;

public class DSGMembershipServer extends DSGServer {

	// ##################
	// # INITIALIZATION #
	// ##################

	/* State */
	private final Set<SocketAddress> clients = new HashSet<>();

	public DSGMembershipServer(int port, int threads) throws IOException {
		// Initialize super class
		super(port, threads);

		// TODO Implement constructor
	}

	// ##########
	// # EVENTS #
	// ##########

	@Override
	protected void accepted(SocketAddress remote) {
		synchronized (clients) {
			clients.add(remote);
		}

		network.dispatch(DSGCall.create(DSGCallType.RECEIVE, remote, new DSGMembershipListRequest()));
	}

	@Override
	protected void received(SocketAddress from, DSGMessage message) {
		SocketAddress[] snapshot;
		synchronized (clients) {
			snapshot = clients.toArray(new SocketAddress[0]);
		}

		network.dispatch(DSGCall.create(DSGCallType.SEND, from, new DSGMembershipListResponse(snapshot)));
		network.dispatch(DSGCall.create(DSGCallType.RECEIVE, from, new DSGMembershipListRequest()));
	}

	@Override
	protected void terminated(SocketAddress remote) {
		synchronized (clients) {
			clients.remove(remote);
		}
	}

}