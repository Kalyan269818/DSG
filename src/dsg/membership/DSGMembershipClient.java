package dsg.membership;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Random;

import dsg.network.DSGClient;

public class DSGMembershipClient {

	// ##################
	// # INITIALIZATION #
	// ##################

	/* State */
	private final SocketAddress[] servers;
	private final DSGClient client;
	private final Random random;

	public DSGMembershipClient(SocketAddress[] servers) {
		// TODO Implement constructor
		this.servers = servers;
		this.client = new DSGClient();
		this.random = new Random();
	}

	// ##############
	// # MEMBERSHIP #
	// ##############

	public boolean join() {
		// TODO Implement method
		boolean joined = false;

		for (SocketAddress server : servers) {
			try {
				client.connect(server);
				joined = true;
			} catch (IOException e) {
				// Ignore; try the remaining servers
			}
		}

		return joined;
	}

	public void leave() {
		// TODO Implement method
		for (SocketAddress server : servers) {
			try {
				client.close(server);
			} catch (IOException e) {
				// Ignore; connection might already be unavailable
			}
		}
	}

	public SocketAddress[] list(SocketAddress server) {
		// TODO Implement method
		try {
			client.send(server, new DSGMembershipListRequest());

			DSGMembershipListResponse response = new DSGMembershipListResponse();
			client.receive(server, response);

			return response.getAddresses();
		} catch (IOException e) {
			return null;
		}
	}

	public SocketAddress[] list() {
		// TODO Implement method
		int start = random.nextInt(servers.length);

		for (int i = 0; i < servers.length; i++) {
			SocketAddress[] result = list(servers[(start + i) % servers.length]);
			if (result != null) return result;
		}

		return null;
	}

}
