package dsg.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Test;

import dsg.network.DSGCall.DSGCallType;

public class DSGClientServerTests {

	// #########
	// # EVENT #
	// #########
	
	/* Type */
	private static enum DSGTestEventType {
		ACCEPTED,
		SENT,
		RECEIVED,
		TERMINATED,
		FAILED
	}
	
	/* Event */
	private static class DSGTestEvent {
		
		/* State */
		public final DSGTestEventType type;
		public final SocketAddress address;
		public final DSGMessage message;
		public final DSGCall call;
		
		/* Constructors */
		public DSGTestEvent(DSGTestEventType type, SocketAddress address, DSGMessage message, DSGCall call) {
			this.type = type;
			this.address = address;
			this.message = message;
			this.call = call;
		}
		
	}
	
	// ###########
	// # MESSAGE #
	// ###########
	
	private static class DSGTestMessage implements DSGMessage {
	
		/* State */
		public byte value;
		
		/* Constructor */
		public DSGTestMessage(byte value) {
			this.value = value;
		}

		/* Serialization */
		@Override
		public void serialize(OutputStream output) throws IOException {
			output.write(value);
		}
		
		@Override
		public void deserialize(InputStream input) throws IOException {
			int b = input.read();
			if(b < 0) throw new EOFException();
			value = (byte) b;
		}
		
	}
	
	// ##########
	// # SERVER #
	// ##########
	
	private static class DSGTestServer extends DSGServer implements Runnable {
	
		/* State */
		private final BlockingQueue<DSGTestEvent> events;
		
		/* Constructor */
		public DSGTestServer(int port, int threads) throws IOException {
			// Initialize super class
			super(port, threads);
			
			// Initialize attributes
			this.events = new LinkedBlockingQueue<DSGTestEvent>();
		}
		
		/* Operations */
		public DSGTestEvent collect() throws InterruptedException {
			return events.take();
		}
		
		/* Events */
		@Override
		protected void accepted(SocketAddress remote) {
			// Record event
			events.add(new DSGTestEvent(DSGTestEventType.ACCEPTED, remote, null, null));
			
			// Submit receive task
			DSGTestMessage request = new DSGTestMessage((byte) 0);
			DSGCall call = DSGCall.create(DSGCallType.RECEIVE, remote, request);
			network.dispatch(call);
		}
		
		@Override
		protected void sent(SocketAddress to, DSGMessage message) {
			events.add(new DSGTestEvent(DSGTestEventType.SENT, to, message, null));
		}
		
		@Override
		protected void received(SocketAddress from, DSGMessage message) {
			// Record event
			events.add(new DSGTestEvent(DSGTestEventType.RECEIVED, from, message, null));
			
			// Echo message
			DSGCall send = DSGCall.create(DSGCallType.SEND, from, message);
			network.dispatch(send);
			
			// Send desperate message
			DSGCall desperate = DSGCall.create(DSGCallType.SEND, new InetSocketAddress(ADDRESS.getHostName(), ADDRESS.getPort() + 1), message);
			network.dispatch(desperate);
			
			// Submit receive task
			DSGTestMessage request = new DSGTestMessage((byte) 0);
			DSGCall receive = DSGCall.create(DSGCallType.RECEIVE, from, request);
			network.dispatch(receive);
		}
		
		@Override
		protected void terminated(SocketAddress remote) {
			events.add(new DSGTestEvent(DSGTestEventType.TERMINATED, remote, null, null));
		}
		
		@Override
		protected void failed(DSGCall call) {
			events.add(new DSGTestEvent(DSGTestEventType.FAILED, null, null, call));
		}
		
		/* Thread execution logic */
		@Override
		public void run() {
			serve();
		}
		
	}
	
	// #########
	// # TESTS #
	// #########
	
	/* Constants */
	private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 12345);
	
	/* Tests */
	@Test
	public void testClientServer() throws IOException, InterruptedException {
		// Create and start server
		DSGTestServer server = new DSGTestServer(ADDRESS.getPort(), 2);
		Thread thread = new Thread(server);
		thread.start();
		
		// Create and connect client
		DSGClient client = new DSGClient();
		client.connect(ADDRESS);
		
		// Collect ACCEPTED event
		DSGTestEvent event = server.collect();
		assertNotNull(event, "Event should not be null");
		assertEquals(DSGTestEventType.ACCEPTED, event.type, "Event-type mismatch");
		assertNotNull(event.address, "Address in accepted() event should not be null");

		// Send request
		DSGTestMessage request = new DSGTestMessage((byte) 47);
		client.send(ADDRESS, request);
		
		// Collect RECEIVED event
		event = server.collect();
		assertNotNull(event, "Event should not be null");
		assertEquals(DSGTestEventType.RECEIVED, event.type, "Event-type mismatch");
		assertNotNull(event.address, "Address in received() event should not be null");
		assertNotNull(event.message, "Message in received() event should not be null");
		
		// Receive response
		DSGTestMessage response = new DSGTestMessage((byte) 0);
		client.receive(ADDRESS, response);
		assertEquals(request.value, response.value, "Response-value mismatch");
		
		// Collect SENT event
		event = server.collect();
		assertNotNull(event, "Event should not be null");
		assertEquals(DSGTestEventType.SENT, event.type, "Event-type mismatch");
		assertNotNull(event.address, "Address in sent() event should not be null");
		assertNotNull(event.message, "Message in sent() event should not be null");
		
		// Collect FAILED event
		event = server.collect();
		assertNotNull(event, "Event should not be null");
		assertEquals(DSGTestEventType.FAILED, event.type, "Event-type mismatch");
		assertNotNull(event.call, "Call in failed() event should not be null");
		assertEquals(DSGCallType.SEND, event.call.getType(), "Call-type mismatch");
		
		// Close connection
		client.close(ADDRESS);
		
		// Collect TERMINATED event
		event = server.collect();
		assertNotNull(event, "Event should not be null");
		assertEquals(DSGTestEventType.TERMINATED, event.type, "Event-type mismatch");
		assertNotNull(event.address, "Address in terminated() event should not be null");
		
		// Shutdown
		client.shutdown();
		thread.interrupt();
		thread.join();
		server.shutdown();
	}
	
}
