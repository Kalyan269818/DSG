package dsg.microblog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.activitypub.DSGActivityPubFederator;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.json.DSGJSONException;
import dsg.http.DSGHTTPClient;
import dsg.rest.DSGRESTSkeleton;


public class DSGMicroBlogMailboxTests {
	// Storage for Mailboxes
	String STORAGE_DIRECTORY = Paths.get(System.getProperty("user.dir"), "storage").toString();
	DSGMicroBlogTestStorage storage; // TODO change back to DSGMicroBlogTestStorage
	
	// Actors
	DSGMicroBlogUser actorA;
	DSGMicroBlogUser actorB;
	
	// Mailboxes
	DSGMicroBlogInbox inboxA;
	DSGMicroBlogOutbox outboxA;
	DSGMicroBlogInbox inboxB;
	DSGMicroBlogOutbox outboxB;
	
	// Components
	DSGActivityPubFederator federator;
	DSGRESTSkeleton router;
	DSGMicroBlogAuthenticator authenticator;
	DSGMicroBlogService service;
	
	
    // ###############
	// # PREPARATION #
	// ###############
	
	@BeforeEach
	public void init() throws IOException, URISyntaxException {
		// Set BaseURI
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
	    URI baseURI = new URI("http", null, hostname, 12345, "/", null, null);
	    
	    // Create in-memory storage for mailbox
	    this.storage = new DSGMicroBlogTestStorage(STORAGE_DIRECTORY);
	 
		// Create federator, router and authenticator
	    this.authenticator = new DSGMicroBlogAuthenticator(baseURI, this.storage);
	    this.router = new DSGRESTSkeleton();
		this.federator = new DSGActivityPubFederator(baseURI, new DSGHTTPClient(false, true));
		this.service = new DSGMicroBlogService(baseURI, this.storage, this.authenticator, this.router, this.federator);
		
		// Create and register Actors
		this.actorA = (DSGMicroBlogUser) service.register("actorA", "12345");
		this.actorB = (DSGMicroBlogUser) service.register("actorB", "56789");
		
		// Create mailboxes
		this.inboxA = new DSGMicroBlogInbox(actorA.getInbox().getTarget(), actorA.getId(), this.storage);
		this.outboxA = new DSGMicroBlogOutbox(actorA.getOutbox().getTarget(), actorA.getId(), this.router, this.storage,this.federator, this.authenticator);
		this.inboxB = new DSGMicroBlogInbox(actorB.getInbox().getTarget(), actorB.getId(), this.storage);
		this.outboxB = new DSGMicroBlogOutbox(actorB.getOutbox().getTarget(), actorB.getId(), this.router, this.storage,this.federator, this.authenticator);
		
	}
	
	@AfterEach
	public void cleanup() {
		storage.cleanup();
	}
	
	
    // #########
	// # TESTS #
	// #########
	
	@Test
	public void testNormalMailboxCommunication() throws DSGJSONException {
		
		// Create new activity for actorA
		DSGActivityStreamsActivity activity = DSGMicroBlogTestActivityGenerator.createActivity(actorA, actorB);		
			
		try {
			// Deliver activities to actorA's outbox
			outboxA.deliver(actorA, activity); 
			
		} catch (DSGActivityPubAuthorizationException e) {
			throw new RuntimeException("Activity could not be delivered to inbox due to wrong authorization of the actor");
		} catch (DSGActivityPubException e) {
			e.printStackTrace();
			throw new RuntimeException("Activity could not be delived to inbox");
		}
			
		
		try {
			// Fetch activity from actorB's inbox
			Collection<DSGActivityStreamsActivity> inbox_collection = inboxB.fetch(actorB).getItems();
			
			// Check that the inbox collection contains the previously delivered activity
			assert inbox_collection != null: "inbox is null";
			assertEquals(1, inbox_collection.size(), "inbox does not contain correct number of activities");
			assert !inbox_collection.contains(activity): "inbox is missing activity \n" + activity.toJSON().toString();
			
		} catch (DSGActivityPubAuthorizationException e) {
			throw new RuntimeException("Activities could not be fetched from inbox due to wrong authorization of the actor");
		} catch (DSGActivityPubException e) {
			e.printStackTrace();
			throw new RuntimeException("Activities could not be fetched from inbox");
		}
			
		
	}
		
	@Test
	public void testFaultyAuthentication() throws DSGJSONException {
		// Create new activity for actorA
		DSGActivityStreamsActivity activity = DSGMicroBlogTestActivityGenerator.createActivity(actorA, actorB);
					
		// ActorA tries to deliver an activity to actorB's outbox
		assertThrows(DSGActivityPubAuthorizationException.class, () -> {
			outboxB.deliver(actorA, activity);
		}, "Unauthorized actor tried to add an activity to another actor's inbox");
					
		// ActorA tries to fetch activities from actorB's inbox
		assertThrows(DSGActivityPubAuthorizationException.class, () -> {
			inboxB.fetch(actorA).getItems();
		}, "Unauthorized actor tried to fetch activities from another actor's inbox");
			
	}
		
}
