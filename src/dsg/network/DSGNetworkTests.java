package dsg.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import dsg.network.DSGCall.DSGCallStatus;
import dsg.network.DSGCall.DSGCallType;

public class DSGNetworkTests {

	// ###########
	// # MESSAGE #
	// ###########

	/* Scenario */
	private static enum DSGNetworkScenario {
		
		// Values
		FAST,
		SLOW,
		SERIALIZATION_ERROR,
		DESERIALIZATION_ERROR;
		
		// Constants
		public static final DSGNetworkScenario[] SCENARIOS = values();
		
	}
	
	/* Type */
	private static class DSGNetworkTestMessage implements DSGMessage {
		
		/* State */
		public DSGNetworkScenario scenario;
		
		/* Constructor */
		public DSGNetworkTestMessage(DSGNetworkScenario scenario) {
			this.scenario = scenario;
		}
		
		/* Serialization */
		@Override
		public void serialize(OutputStream stream) throws IOException {
			// Implement scenario-specific behavior
			switch(scenario) {
			case FAST:
			case DESERIALIZATION_ERROR:
				// Do nothing
				break;
			case SLOW:
				try {
					Thread.sleep(3_000L);
				} catch(InterruptedException ie) {
					// Do nothing
				}
				break;
			case SERIALIZATION_ERROR:
				throw new IOException("Simulated serialization error");
			default:
				System.err.println("Unexpected scenario: " + scenario);
			}
			
			// Serialize message
			stream.write(scenario.ordinal());
		}
		
		@Override
		public void deserialize(InputStream stream) throws IOException {
			// Deserialize message
			int b = stream.read();
			if(b < 0) throw new EOFException();
			scenario = DSGNetworkScenario.SCENARIOS[b];
			
			// Implement scenario-specific behavior
			switch(scenario) {
			case FAST:
			case SLOW:
			case SERIALIZATION_ERROR:
				// Do nothing
				break;
			case DESERIALIZATION_ERROR:
				throw new IOException("Simulated deserialization error");
			default:
				System.err.println("Unexpected scenario: " + scenario);
			}
		}
		
	}
	
	// #########
	// # TESTS #
	// #########
	
	/* Constants */
	private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 12345);
	
	/* Tests */
	@Test
	public void testRequestResponse() throws IOException {
		// Create server-side network core
		DSGNetwork server = new DSGNetwork(ADDRESS.getPort(), 4);
		DSGCall accept = DSGCall.create(DSGCallType.ACCEPT, null, null);
		server.dispatch(accept);
		
		// Create client-side network core and establish connection
		DSGNetwork client = new DSGNetwork();
		DSGCall connect = DSGCall.create(DSGCallType.CONNECT, ADDRESS, null);
		client.dispatch(connect);
		assertEquals(connect, client.collect(), "Client-side network core should return the previously submitted connect task");
		
		// Wait for the accept at the server to complete
		assertEquals(accept, server.collect(), "Server-side network core should return the previously submitted accept task");
		
		// Submit receive task at the server
		DSGNetworkTestMessage srqm = new DSGNetworkTestMessage(null);
		DSGCall srqr = DSGCall.create(DSGCallType.RECEIVE, accept.getRemote(), srqm);
		server.dispatch(srqr);
		
		// Send request from client to server
		DSGNetworkTestMessage crqm = new DSGNetworkTestMessage(DSGNetworkScenario.FAST);
		DSGCall crqs = DSGCall.create(DSGCallType.SEND, ADDRESS, crqm);
		client.dispatch(crqs);
		assertEquals(crqs, client.collect(), "Client-side network core should return the previously submitted send task");
		
		// Submit receive task at the client
		DSGNetworkTestMessage crpm = new DSGNetworkTestMessage(null);
		DSGCall crpr = DSGCall.create(DSGCallType.RECEIVE, ADDRESS, crpm);
		client.dispatch(crpr);
		
		// Receive request at the server
		assertEquals(srqr, server.collect(), "Server-side network core should return the previously submitted receive task");
		assertEquals(crqm.scenario, srqm.scenario, "The received message should be the request");
		
		// Send response from server to client
		DSGNetworkTestMessage srpm = new DSGNetworkTestMessage(DSGNetworkScenario.SLOW);
		DSGCall srps = DSGCall.create(DSGCallType.SEND, accept.getRemote(), srpm);
		server.dispatch(srps);
		assertEquals(srps, server.collect(), "Server-side network core should return the previously submitted send task");
		
		// Receive response at the client
		assertEquals(crpr, client.collect(), "Client-side network core should return the previously submitted receive task");
		assertEquals(srpm.scenario, crpm.scenario, "The received message should be the response");
		
		// Shut down system 
		server.shutdown();
		client.shutdown();
	}
	
	@Test
	public void testSequentialCallExecution() throws IOException {
		// Create server-side network core
		DSGNetwork server = new DSGNetwork(ADDRESS.getPort(), 4);
		DSGCall accept = DSGCall.create(DSGCallType.ACCEPT, null, null);
		server.dispatch(accept);
		
		// Create client-side network core and establish connection
		DSGNetwork client = new DSGNetwork();
		DSGCall connect = DSGCall.create(DSGCallType.CONNECT, ADDRESS, null);
		client.dispatch(connect);
		assertEquals(connect, client.collect(), "Client-side network core should return the previously submitted connect task");
		
		// Wait for the accept at the server to complete
		assertEquals(accept, server.collect(), "Server-side network core should return the previously submitted accept task");
		
		// Send slow message from server to client
		DSGNetworkTestMessage slow = new DSGNetworkTestMessage(DSGNetworkScenario.SLOW);
		DSGCall first = DSGCall.create(DSGCallType.SEND, accept.getRemote(), slow);
		server.dispatch(first);
		
		// Send fast message from server to client
		DSGNetworkTestMessage fast = new DSGNetworkTestMessage(DSGNetworkScenario.FAST);
		DSGCall second = DSGCall.create(DSGCallType.SEND, accept.getRemote(), fast);
		server.dispatch(second);
		
		// Receive first message at the client
		DSGNetworkTestMessage message = new DSGNetworkTestMessage(null);
		DSGCall receive = DSGCall.create(DSGCallType.RECEIVE, ADDRESS, message);
		client.dispatch(receive);
		assertEquals(receive, client.collect(), "Client-side network core should return the previously submitted receive task");
		assertEquals(message.scenario, slow.scenario, "Slow message should arrive first");
		
		// Receive second message at the client
		client.dispatch(receive);
		assertEquals(receive, client.collect(), "Client-side network core should return the previously submitted receive task");
		assertEquals(message.scenario, fast.scenario, "Fast message should arrive second");
		
		// Shut down system 
		server.shutdown();
		client.shutdown();
	}
	
	@Test
	public void testSerializationError() throws IOException {
		// Create server-side network core
		DSGNetwork server = new DSGNetwork(ADDRESS.getPort(), 4);
		DSGCall accept = DSGCall.create(DSGCallType.ACCEPT, null, null);
		server.dispatch(accept);
		
		// Create client-side network core and establish connection
		DSGNetwork client = new DSGNetwork();
		DSGCall connect = DSGCall.create(DSGCallType.CONNECT, ADDRESS, null);
		client.dispatch(connect);
		assertEquals(connect, client.collect(), "Client-side network core should return the previously submitted connect task");
		
		// Wait for the accept at the server to complete
		assertEquals(accept, server.collect(), "Server-side network core should return the previously submitted accept task");
		
		// Send request from client to server
		DSGNetworkTestMessage crqm = new DSGNetworkTestMessage(DSGNetworkScenario.SERIALIZATION_ERROR);
		DSGCall crqs = DSGCall.create(DSGCallType.SEND, ADDRESS, crqm);
		client.dispatch(crqs);
		assertEquals(crqs, client.collect(), "Client-side network core should return the previously submitted send task");
		assertEquals(DSGCallStatus.FAILURE, crqs.getStatus(), "Unexpected call status");
		
		// Shut down system 
		server.shutdown();
		client.shutdown();
	}
	
	@Test
	public void testDeserializationError() throws IOException {
		// Create server-side network core
		DSGNetwork server = new DSGNetwork(ADDRESS.getPort(), 4);
		DSGCall accept = DSGCall.create(DSGCallType.ACCEPT, null, null);
		server.dispatch(accept);
		
		// Create client-side network core and establish connection
		DSGNetwork client = new DSGNetwork();
		DSGCall connect = DSGCall.create(DSGCallType.CONNECT, ADDRESS, null);
		client.dispatch(connect);
		assertEquals(connect, client.collect(), "Client-side network core should return the previously submitted connect task");
		
		// Wait for the accept at the server to complete
		assertEquals(accept, server.collect(), "Server-side network core should return the previously submitted accept task");
		
		// Submit receive task at the server
		DSGNetworkTestMessage srqm = new DSGNetworkTestMessage(null);
		DSGCall srqr = DSGCall.create(DSGCallType.RECEIVE, accept.getRemote(), srqm);
		server.dispatch(srqr);
		
		// Send request from client to server
		DSGNetworkTestMessage crqm = new DSGNetworkTestMessage(DSGNetworkScenario.DESERIALIZATION_ERROR);
		DSGCall crqs = DSGCall.create(DSGCallType.SEND, ADDRESS, crqm);
		client.dispatch(crqs);
		assertEquals(crqs, client.collect(), "Client-side network core should return the previously submitted send task");
		
		// Receive request at the server
		assertEquals(srqr, server.collect(), "Server-side network core should return the previously submitted receive task");
		assertEquals(DSGCallStatus.FAILURE, srqr.getStatus(), "Unexpected call status");
		
		// Shut down system 
		server.shutdown();
		client.shutdown();
	}
	
}
