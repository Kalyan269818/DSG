package dsg.microblog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dsg.activitypub.DSGActivityPubFederator;
import dsg.activitypub.DSGActivityPubMailboxSkeleton;
import dsg.activitypub.DSGActivityPubReader;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONException;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRepresentation;
import dsg.rest.DSGRESTSkeleton;


public class DSGMicroBlogSkeletonTests {
	
	// Storage
	String STORAGE_DIRECTORY = Paths.get(System.getProperty("user.dir"), "storage").toString();
	DSGMicroBlogTestStorage storage;

	// Actors
	DSGMicroBlogUser actorA;
	DSGMicroBlogUser actorB;
		
	// Skeletons
	DSGActivityPubMailboxSkeleton skeletonInA;
	DSGActivityPubMailboxSkeleton skeletonOutA;
	DSGActivityPubMailboxSkeleton skeletonInB;	
	DSGActivityPubMailboxSkeleton skeletonOutB;
	
	// Service
	DSGMicroBlogAuthenticator authenticator;
	DSGRESTSkeleton router;
	DSGActivityPubFederator federator;
	DSGMicroBlogService service;
	
	
	// ###############
	// # PREPARATION #
	// ###############
		
	@BeforeEach
	void init() throws URISyntaxException, IOException {
		// Set BaseURI
		String hostname = InetAddress.getLocalHost().getCanonicalHostName();
	    URI baseURI = new URI("http", null, hostname, 12345, "/", null, null);
			    
	    // Create Storage
	    this.storage = new DSGMicroBlogTestStorage(STORAGE_DIRECTORY);
	    
	    // Create server components
	    this.authenticator = new DSGMicroBlogAuthenticator(baseURI, this.storage);
	    this.router = new DSGRESTSkeleton();
		this.federator = new DSGActivityPubFederator(baseURI, new DSGHTTPClient(false, true));
		this.service = new DSGMicroBlogService(baseURI, this.storage, this.authenticator, this.router, this.federator);
	    
	    // Create and Register Actors
	    this.actorA = (DSGMicroBlogUser) service.register("actorA", "12345");
	    this.actorA.setAuthorization(authenticator.createAuthorization("actorA", "12345"));
	    this.actorB = (DSGMicroBlogUser) service.register("actorB", "56789");
	    this.actorB.setAuthorization(authenticator.createAuthorization("actorB", "56789"));
	    
		// Inbox Skeleton ActorA
		DSGMicroBlogInbox inboxA = new DSGMicroBlogInbox(actorA.getInbox().getTarget(), actorA.getId(), this.storage);
		this.skeletonInA = new DSGActivityPubMailboxSkeleton(inboxA, authenticator);

	    // Outbox Skeleton ActorA 
		DSGMicroBlogOutbox outboxA = new DSGMicroBlogOutbox(actorA.getOutbox().getTarget(), actorA.getId(), this.router, this.storage, this.federator, this.authenticator);
		this.skeletonOutA = new DSGActivityPubMailboxSkeleton(outboxA, authenticator);
		
		// Inbox Skeleton ActorB
		DSGMicroBlogInbox inboxB = new DSGMicroBlogInbox(actorB.getInbox().getTarget(), actorB.getId(), this.storage);
		this.skeletonInB = new DSGActivityPubMailboxSkeleton(inboxB, authenticator);
		
		// Outbox Skeleton ActorB 
		DSGMicroBlogOutbox outboxB = new DSGMicroBlogOutbox(actorB.getOutbox().getTarget(), actorB.getId(), this.router, this.storage, this.federator, this.authenticator);
		this.skeletonOutB = new DSGActivityPubMailboxSkeleton(outboxB, authenticator);

	}
	
	@AfterEach
	void cleanup() {
		storage.cleanup();
	}
	
	// #########
	// # TESTS #
	// #########
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSkeletonPostAndGet() throws IOException {
		
		// Create new activity from actorA to actorB
		DSGActivityStreamsActivity activity = DSGMicroBlogTestActivityGenerator.createActivity(actorA, actorB);		
		
		// Convert activity into RESTRepresentation
		DSGRESTRepresentation rest_activity = DSGMicroBlogTestActivityGenerator.toRestRepresentation(activity, actorA);
		DSGRESTContext ctx = new DSGRESTContext();
		ctx.addAcceptedRepresentation(DSGStandardMediaTypes.ACTIVITY_STREAMS);
        
		try {
			// Post activity to actorA via its outbox skeleton
			DSGRESTRepresentation result = skeletonOutA.post(ctx, rest_activity);
			
			// Compare result
			assertEquals(DSGHTTPStatus.CREATED, result.getStatus(), "REST Representation does not have the status CREATED");
			assert result.getHeader().get("Location") != null: "The REST Representation header field Location is not set";
		} catch (DSGRESTException e) {
			e.printStackTrace();
			throw new RuntimeException("Post activity has thrown a RESTException status " + e.getStatus());
		}
		
		
		try {
			// Get activity from actorB's inbox via its inbox skeleton
			DSGHTTPHeader header = new DSGHTTPHeader(); 
			header.set("Authorization", actorB.getAuthorization());
			DSGRESTRepresentation rest_result = skeletonInB.get(ctx, header);
			
			try (DSGActivityPubReader reader = new DSGActivityPubReader(rest_result)){
				DSGActivityStreamsObject result = reader.read();
				if (result instanceof DSGActivityStreamsCollection) {
					DSGActivityStreamsCollection<DSGActivityStreamsActivity> collection = (DSGActivityStreamsCollection<DSGActivityStreamsActivity>) result;
					
					// Compare result
					assert collection != null: "Inbox is null";
					assertEquals(1, collection.getTotalItems(), "Inbox does not contain correct number of activities");
					assert !collection.getItems().contains(activity): "Inbox is missing activity \n" + activity.toJSON().toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Activities could not be read from input stream");
			}
		} catch (DSGRESTException e) {
			e.printStackTrace();
			throw new RuntimeException("Activities could not be fetched from skeleton due to " + e.getStatus());
		}
	}
	
	@Test
	public void testPostFaultyAuthorization() throws DSGJSONException {
		// Create new activity from actorB to actorA (faulty request)
		DSGActivityStreamsActivity activity = DSGMicroBlogTestActivityGenerator.createSpoofedActivity(actorA, actorB);		
				
		// Convert activity into RESTRepresentation
		DSGRESTRepresentation rest_activity = DSGMicroBlogTestActivityGenerator.toRestRepresentation(activity, actorB);
		DSGRESTContext ctx = new DSGRESTContext();
		ctx.addAcceptedRepresentation(DSGStandardMediaTypes.ACTIVITY_STREAMS);
		        
		try {
			// Post activity to actorA's outbox via its outbox skeleton
			DSGRESTRepresentation result = skeletonOutA.post(ctx, rest_activity);
			fail("post() should have thrown an exception due to faulty authorization");
		} catch (DSGRESTException e) {
			// Expect DSGRESTException with status UNAUTHORIZED
			assertEquals(e.getStatus(), DSGHTTPStatus.UNAUTHORIZED);
		}
	}
	
}
