package dsg.microblog;

import java.util.ArrayList;

import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONValue;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTRepresentation;

public class DSGMicroBlogTestActivityGenerator {

	
	public static DSGActivityStreamsActivity createActivity(DSGMicroBlogUser from, DSGMicroBlogUser to) throws DSGJSONException {
		// Create new object
		DSGActivityStreamsObject object = new DSGActivityStreamsObject(from.getNextObjectID());
		object.setAttributedTo(new DSGActivityStreamsLink(from.getId()));
		object.addRecipient(new DSGActivityStreamsLink(to.getId()));
		//object.setTo(new DSGActivityStreamsLink(to.getId()));
		object.setType("Note");
		object.setContent("This is a new activity");
		
		// Create new activity with object
		DSGActivityStreamsActivity activity = new DSGActivityStreamsActivity(from.getNextObjectID(), from, object);
		activity.addRecipient(new DSGActivityStreamsLink(to.getId()));
		
		return activity;
	}
	
	
	public static DSGActivityStreamsActivity createSpoofedActivity(DSGMicroBlogUser from, DSGMicroBlogUser to) throws DSGJSONException {
		// Create new object
		DSGActivityStreamsObject object = new DSGActivityStreamsObject(from.getNextObjectID());
		object.setAttributedTo(new DSGActivityStreamsLink(to.getId()));
		object.addRecipient(new DSGActivityStreamsLink(from.getId()));
		
		// Create new activity with object
		DSGActivityStreamsActivity activity = new DSGActivityStreamsActivity(from.getNextObjectID(), from, object);
		activity.addRecipient(new DSGActivityStreamsLink(to.getId()));
		
		return new DSGActivityStreamsActivity(activity.toJSON());
	}
	
	
	public static DSGRESTRepresentation toRestRepresentation(DSGActivityStreamsActivity activity, DSGMicroBlogUser actor) {
		DSGJSONValue value = activity.toJSON();
        DSGRESTRepresentation representation = new DSGRESTRepresentation(DSGStandardMediaTypes.ACTIVITY_STREAMS, value.toInputStream());
        DSGHTTPHeader header = representation.getHeader();
        if (actor != null && actor.getAuthorization() != null) {
            header.set("Authorization", actor.getAuthorization());
        }
		return representation;//new DSGRESTRepresentation(DSGHTTPStatus.OK, DSGStandardMediaTypes.ACTIVITY_STREAMS, activity.toJSON().toInputStream());
		
	}
}
