package dsg.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.jupiter.api.Test;

public class DSGMembershipTests {

	// ##########
	// # SERVER #
	// ##########
	
	private static class DSGMembershipServerThread extends Thread {
		
		/* State */
		public final DSGMembershipServer server;
		
		/* Constructor */
		public DSGMembershipServerThread(int port, int threads) throws IOException {
			this.server = new DSGMembershipServer(port, threads);
		}
		
		/* Thread execution logic */
		@Override
		public void run() {
			server.serve();
		}
		
		/* Operations */
		public void terminate() throws InterruptedException {
			interrupt();
			join();
			server.shutdown();
		}
		
	}
	
	// #########
	// # TESTS #
	// #########
	
	/* Constants */
	private static final InetSocketAddress[] ADDRESSES = { new InetSocketAddress("localhost", 12345), new InetSocketAddress("localhost", 12346) };
	private static final int NUMBER_OF_CLIENTS = 5;
	
	/* Tests */
	@Test
	public void testNormalCase() throws IOException, InterruptedException {
		// Create and start servers
		DSGMembershipServerThread[] servers = new DSGMembershipServerThread[ADDRESSES.length];
		for(int i = 0; i < servers.length; i++) {
			servers[i] = new DSGMembershipServerThread(ADDRESSES[i].getPort(), NUMBER_OF_CLIENTS + 1);
			servers[i].start();
		}
		
		// Create clients
		DSGMembershipClient[] clients = new DSGMembershipClient[NUMBER_OF_CLIENTS];
		for(int i = 0; i < clients.length; i++) clients[i] = new DSGMembershipClient(ADDRESSES);
		
		// Make clients join the system
		for(int i = 0; i < clients.length; i++) {
			// Join the system
			assertTrue(clients[i].join(), "Attempts to join the servers should be successful");
			
			// Fetch address lists; notice that list() can only be successful if the server completed the accept for the connection
			for(int s = 0; s < servers.length; s++) {
				SocketAddress[] list = clients[i].list(ADDRESSES[s]);
				assertNotNull(list, "Result list at this moment should not be null");
				assertEquals(i + 1, list.length, "Result list at this moment should comprise " + (i + 1) + " elements");
			}
		}
		
		// Make clients leave the system
		for(int i = 0; i < clients.length; i++) {
			// Fetch address lists
			for(int s = 0; s < servers.length; s++) {
				SocketAddress[] list = clients[i].list(ADDRESSES[s]);
				assertNotNull(list, "Result list at this moment should not be null");
				assertEquals(clients.length - i, list.length, "Result list at this moment should comprise " + (clients.length - i) + " elements");
			}
			
			// Leave the system
			clients[i].leave();
			
			// Increase the chance that the servers have updated their address lists by now
			Thread.sleep(500L);
		}
		
		// Terminate servers
		for(DSGMembershipServerThread server: servers) server.terminate();
	}
	
	@Test
	public void testFailover() throws IOException, InterruptedException {
		// Create and start servers
		DSGMembershipServerThread[] servers = new DSGMembershipServerThread[ADDRESSES.length];
		for(int i = 0; i < servers.length; i++) {
			servers[i] = new DSGMembershipServerThread(ADDRESSES[i].getPort(), NUMBER_OF_CLIENTS + 1);
			servers[i].start();
		}
		
		// Create clients
		DSGMembershipClient[] clients = new DSGMembershipClient[NUMBER_OF_CLIENTS];
		for(int i = 0; i < clients.length; i++) clients[i] = new DSGMembershipClient(ADDRESSES);
		
		// Make clients join the system
		for(int i = 0; i < clients.length; i++) {
			// Join the system
			assertTrue(clients[i].join(), "Attempts to join the servers should be successful");
			
			// Fetch address lists; notice that list() can only be successful if the server completed the accept for the connection
			for(int s = 0; s < servers.length; s++) {
				SocketAddress[] list = clients[i].list(ADDRESSES[s]);
				assertNotNull(list, "Result list at this moment should not be null");
				assertEquals(i + 1, list.length, "Result list at this moment should comprise a different number of elements");
			}
		}

		// Terminate all but one server
		for(int i = 0; i < (servers.length - 1); i++) servers[i].terminate();

		// Fetch address lists
		for(int a = 0; a < 3; a++) {
			for(int i = 0; i < clients.length; i++) {
				SocketAddress[] list = clients[i].list();
				assertNotNull(list, "Result list at this moment should not be null");
				assertEquals(clients.length, list.length, "Result list at this moment should comprise a different number of elements");
			}
		}
		
		// Terminate remaining server
		if(1 < servers.length) servers[servers.length - 1].terminate();
	}
	
}
