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

public class DSGMicroBlogIntegrationTests {

	// Constants
	String STORAGE_DIR = Paths.get(System.getProperty("user.dir"), "microblog", "storage").toString();
	String STATIC_DIR = Paths.get(System.getProperty("user.dir"), "microblog", "static").toString();
	int PORT = 8002;

	// Storage
	String STORAGE_DIRECTORY = Paths.get(System.getProperty("user.dir"), "storage").toString();
	DSGMicroBlogTestStorage storage;

	// Server
	DSGMicroBlogServer server;
	Thread serverThread;

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
		// Set BaseURI
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
		URI baseURI = new URI("http", null, hostname, PORT, "/", null, null);

		// Init server components
		this.storage = new DSGMicroBlogTestStorage(STORAGE_DIR);
		DSGMicroBlogAuthenticator authenticator = new DSGMicroBlogAuthenticator(baseURI, storage);
		DSGActivityPubFederator federator = new DSGActivityPubFederator(baseURI, new DSGHTTPClient());
		DSGRESTSkeleton router = new DSGRESTSkeleton();
		DSGMicroBlogHandler handler = new DSGMicroBlogHandler(STATIC_DIR, authenticator, router);
		DSGMicroBlogService service = new DSGMicroBlogService(baseURI, storage, authenticator, router, federator);

		// Init Server
		router.exportResource("/user", new DSGMicroBlogServiceSkeleton(service, authenticator));
        server = new DSGMicroBlogServer(PORT, handler, federator);

		// Create and Register Actors
	    this.actorA = (DSGMicroBlogUser) service.register("actorA", "12345");
	    this.actorA.setAuthorization(authenticator.createAuthorization("actorA", "12345"));
	    this.actorB = (DSGMicroBlogUser) service.register("actorB", "56789");
	    this.actorB.setAuthorization(authenticator.createAuthorization("actorB", "56789"));

		 // Inbox Skeleton ActorA Inbox
		 DSGMicroBlogInbox inboxA = new DSGMicroBlogInbox(actorA.getInbox().getTarget(), actorA.getId(), this.storage);
		 this.skeletonInA = new DSGActivityPubMailboxSkeleton(inboxA, authenticator);
		 // Outbox Skeleton ActorA Outbox
		 DSGMicroBlogOutbox outboxA = new DSGMicroBlogOutbox(actorA.getOutbox().getTarget(), actorA.getId(), router, this.storage, federator, authenticator);
		 this.skeletonOutA = new DSGActivityPubMailboxSkeleton(outboxA, authenticator);
		 // Inbox Skeleton ActorB Inbox
		 DSGMicroBlogInbox inboxB = new DSGMicroBlogInbox(actorB.getInbox().getTarget(), actorB.getId(), this.storage);
		 this.skeletonInB = new DSGActivityPubMailboxSkeleton(inboxB, authenticator);
		 // Outbox Skeleton ActorB Outbox
		 DSGMicroBlogOutbox outboxB = new DSGMicroBlogOutbox(actorB.getOutbox().getTarget(), actorB.getId(), router, this.storage, federator, authenticator);
		 this.skeletonOutB = new DSGActivityPubMailboxSkeleton(outboxB, authenticator);

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
		 this.serverThread = new Thread() {
             @Override
             public void run() {
                 try {
                     server.serve();
                 } catch (Exception e) {
                	 System.err.println("Server thread terminated due to an Exception");
                     e.printStackTrace();
                 }
             }
         };
         this.serverThread.start();
	}

	@AfterEach
	void cleanup() {
		this.httpClient.shutdown();
		this.serverThread.interrupt();
		this.server.shutdown();
		try {
            serverThread.join();
        } catch (InterruptedException e) {
            fail("Failed to shutdown server thread", e);
        }
		this.storage.cleanup();
	}


    // #########
	// # TESTS #
	// #########

	@Test
	public void testNormalCommunication() throws DSGJSONException {

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
			// Use client stub to fetch activites from actorB's inbox
			DSGActivityStreamsCollection<DSGActivityStreamsActivity> collection = stubInB.fetch(actorB);
			assert collection != null: "Inbox content is null";
			assertEquals(collection.getTotalItems(), 1, "Inbox does not contain correct number of activities");
			assertEquals(collection.getItems().get(0).getId(), locationA, "Inbox is does not have an activity of URI " + locationA.toASCIIString());
		} catch (DSGActivityPubAuthorizationException e) {
			throw new RuntimeException("Activities could not be fetched from inbox due to wrong authorization of the actor");
		} catch (DSGActivityPubException e) {
			throw new RuntimeException("Activities could not be fetched from inbox");
		}
	}
	
	@Test
	public void testStubAuthenticationErrors() throws DSGJSONException {

		DSGActivityStreamsActivity activityA = DSGMicroBlogTestActivityGenerator.createActivity(actorA, actorB);
		URI locationA;
		
		// Use client stub to deliver an activity to actorA's outbox
		assertThrows(DSGActivityPubAuthorizationException.class, () -> {
			stubOutA.deliver(actorB, activityA);
		});
	}

}
