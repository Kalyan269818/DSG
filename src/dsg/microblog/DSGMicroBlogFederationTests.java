package dsg.microblog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.activitypub.DSGActivityPubFederator;
import dsg.activitypub.DSGActivityPubMailboxSkeleton;
import dsg.activitypub.DSGActivityPubMailboxStub;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.http.DSGHTTPClient;
import dsg.json.DSGJSONException;
import dsg.rest.DSGRESTSkeleton;
import dsg.rest.DSGRESTStub;

public class DSGMicroBlogFederationTests {

	// Constants
	String STORAGE_DIR_1 = Paths.get(System.getProperty("user.dir"), "microblog1", "storage").toString();
	String STORAGE_DIR_2 = Paths.get(System.getProperty("user.dir"), "microblog2", "storage").toString();
	String STATIC_DIR_1 = Paths.get(System.getProperty("user.dir"), "microblog1", "static").toString();
	String STATIC_DIR_2 = Paths.get(System.getProperty("user.dir"), "microblog2", "static").toString();
	int PORT_1 = 8001;
	int PORT_2 = 8002;

	// Storage
	DSGMicroBlogTestStorage storage1;
	DSGMicroBlogTestStorage storage2;

	// Server
	DSGMicroBlogServer server1;
	DSGMicroBlogServer server2;
	Thread serverThread1;
	Thread serverThread2;

	// Client
	DSGHTTPClient httpClient;

	// Skeletons
	DSGActivityPubMailboxSkeleton skeletonInA;
	DSGActivityPubMailboxSkeleton skeletonOutA;
	DSGActivityPubMailboxSkeleton skeletonInB;
	DSGActivityPubMailboxSkeleton skeletonOutB;

	// Stubs
	DSGActivityPubMailboxStub stubInA;
	DSGActivityPubMailboxStub stubOutA;
	DSGActivityPubMailboxStub stubInB;
	DSGActivityPubMailboxStub stubOutB;

	// Actors
	DSGMicroBlogUser actorA;
	DSGMicroBlogUser actorB;


	// ###############
	// # PREPARATION #
	// ###############

	@BeforeEach
	void init() throws IOException, URISyntaxException {
		// Set BaseURIs
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
		URI baseURI_1 = new URI("http", null, hostname, PORT_1, "/", null, null);
		URI baseURI_2 = new URI("http", null, hostname, PORT_2, "/", null, null);
		
		// Init server components
		this.storage1 = new DSGMicroBlogTestStorage(STORAGE_DIR_1);
		this.storage2 = new DSGMicroBlogTestStorage(STORAGE_DIR_2);
		DSGMicroBlogAuthenticator authenticator1 = new DSGMicroBlogAuthenticator(baseURI_1, storage1);
		DSGMicroBlogAuthenticator authenticator2 = new DSGMicroBlogAuthenticator(baseURI_2, storage2);
		DSGActivityPubFederator federator1 = new DSGActivityPubFederator(baseURI_1, new DSGHTTPClient());
		DSGActivityPubFederator federator2 = new DSGActivityPubFederator(baseURI_2, new DSGHTTPClient());
		DSGRESTSkeleton router1 = new DSGRESTSkeleton();
		DSGRESTSkeleton router2 = new DSGRESTSkeleton();
		DSGMicroBlogHandler handler1 = new DSGMicroBlogHandler(STATIC_DIR_1, authenticator1, router1);
		DSGMicroBlogHandler handler2 = new DSGMicroBlogHandler(STATIC_DIR_2, authenticator2, router2);
		DSGMicroBlogService service1 = new DSGMicroBlogService(baseURI_1, storage1, authenticator1, router1, federator1);
		DSGMicroBlogService service2 = new DSGMicroBlogService(baseURI_2, storage2, authenticator2, router2, federator2);

		// Init Servers
		router1.exportResource("/user", new DSGMicroBlogServiceSkeleton(service1, authenticator1));
	    server1 = new DSGMicroBlogServer(PORT_1, handler1, federator1);
	    router2.exportResource("/user", new DSGMicroBlogServiceSkeleton(service2, authenticator2));
	    server2 = new DSGMicroBlogServer(PORT_2, handler2, federator2);

		// Create and Register Actors
	    this.actorA = (DSGMicroBlogUser) service1.register("actorA", "12345");
	    this.actorA.setAuthorization(authenticator1.createAuthorization("actorA", "12345"));
	    this.actorB = (DSGMicroBlogUser) service2.register("actorB", "56789");
	    this.actorB.setAuthorization(authenticator2.createAuthorization("actorB", "56789"));

	    // Inbox Skeleton ActorA Inbox (server 1)
		DSGMicroBlogInbox inboxA = new DSGMicroBlogInbox(actorA.getInbox().getTarget(), actorA.getId(), this.storage1);
		this.skeletonInA = new DSGActivityPubMailboxSkeleton(inboxA, authenticator1);
		// Outbox Skeleton ActorA Outbox (server 1)
		DSGMicroBlogOutbox outboxA = new DSGMicroBlogOutbox(actorA.getOutbox().getTarget(), actorA.getId(), router1, this.storage1, federator1, authenticator1);
		this.skeletonOutA = new DSGActivityPubMailboxSkeleton(outboxA, authenticator1);
		
		// Inbox Skeleton ActorB Inbox (server 2)
		DSGMicroBlogInbox inboxB = new DSGMicroBlogInbox(actorB.getInbox().getTarget(), actorB.getId(), this.storage2);
		this.skeletonInB = new DSGActivityPubMailboxSkeleton(inboxB, authenticator2);
		// Outbox Skeleton ActorB Outbox (server 2)
		DSGMicroBlogOutbox outboxB = new DSGMicroBlogOutbox(actorB.getOutbox().getTarget(), actorB.getId(), router2, this.storage2, federator2, authenticator2);
		this.skeletonOutB = new DSGActivityPubMailboxSkeleton(outboxB, authenticator2);

		// Init Client
		this.httpClient = new DSGHTTPClient(false, true);
		// Init Stub ActorA Inbox
		this.stubInA = new DSGActivityPubMailboxStub(new DSGRESTStub(actorA.getInbox().getTarget(), httpClient));
		// Init Stub ActorA Outbox
		this.stubOutA = new DSGActivityPubMailboxStub(new DSGRESTStub(actorA.getOutbox().getTarget(), httpClient));
		// Init Stub ActorB Inbox
		this.stubInB = new DSGActivityPubMailboxStub(new DSGRESTStub(actorB.getInbox().getTarget(), httpClient));
		// Init Stub ActorB Outbox
		this.stubOutB = new DSGActivityPubMailboxStub(new DSGRESTStub(actorB.getOutbox().getTarget(), httpClient));

		// Start server thread
		this.serverThread1 = new Thread() {
			@Override
				public void run() {
	                 try {
	                     server1.serve();
	                 } catch (Exception e) {
	                	 System.err.println("Server thread 1 terminated due to an Exception");
	                     e.printStackTrace();
	                 }
	             }
			};
		// Start server thread
		this.serverThread2 = new Thread() {
			@Override
				public void run() {
					try {
						server2.serve();
					} catch (Exception e) {
						System.err.println("Server thread 2 terminated due to an Exception");
							e.printStackTrace();
					}
				}
		};
	         
		this.serverThread1.start();
		this.serverThread2.start();
		}

		@AfterEach
		void cleanup() {
			this.httpClient.shutdown();
			this.serverThread1.interrupt();
			this.serverThread2.interrupt();
			this.server1.shutdown();
			this.server2.shutdown();
			try {
	            serverThread1.join();
	            serverThread2.join();
	        } catch (InterruptedException e) {
	            fail("Failed to shutdown server thread", e);
	        }
			this.storage1.cleanup();
			this.storage2.cleanup();
		}


	    // #########
		// # TESTS #
		// #########

		@Test
		public void testFederatedCommunication() throws DSGJSONException, InterruptedException {

			DSGActivityStreamsActivity activityA = DSGMicroBlogTestActivityGenerator.createActivity(actorA, actorB);
			URI locationA;
			try {
				// Use client stub to deliver an activity to actorA's outbox
				locationA = stubOutA.deliver(actorA, activityA);
			} catch (DSGActivityPubAuthorizationException e) {
				throw new RuntimeException("Activity could not be delived to inbox due to wrong authorization of the actor");
			} catch (DSGActivityPubException e) {
				throw new RuntimeException("Activity could not be delived to inbox");
			}

			try {
				int TOTAL_RETRIES = 5;
				DSGActivityStreamsCollection<DSGActivityStreamsActivity> collection = null;
				for (int i = 0; i < TOTAL_RETRIES; i++) {
					// Use client stub to fetch activites from actorB's inbox
					collection = stubInB.fetch(actorB);
					assert collection != null: "Inbox content is null";
					if (collection.getTotalItems() > 0) break;
				} 
				assertEquals(1, collection.getTotalItems(), "Inbox does not contain correct number of activities");
				assertEquals(locationA, collection.getItems().get(0).getId(), "Inbox is does not have an activity of URI " + locationA.toASCIIString());
			} catch (DSGActivityPubAuthorizationException e) {
				throw new RuntimeException("Activities could not be fetched from inbox due to wrong authorization of the actor");
			} catch (DSGActivityPubException e) {
				throw new RuntimeException("Activities could not be fetched from inbox");
			}
		}
		
}
