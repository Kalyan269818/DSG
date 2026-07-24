package dsg.microblog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dsg.activitypub.DSGActivityPubReader;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.json.DSGJSONSerializable;
import dsg.json.DSGJSONValue;

	/**
	 * In-memory storage for testing purposes
	 */
	
	public class DSGMicroBlogTestStorage extends DSGMicroBlogStorage {
		
		//###################
		// # INITIALIZATION #
		// ##################
		
		Map<Path, DSGJSONValue> storage;
		
		public DSGMicroBlogTestStorage(String directory) throws IOException {
			super(directory);
			this.storage = new HashMap<>();
		}
		
		//###########
		// # ACCESS #
		// ##########
		
		@Override
		public boolean exists(URI id) {
	        if (id == null) {
	            throw new IllegalArgumentException("id is null");
	        }
	        Path path = getPath(id);
	        return storage.containsKey(path);
	    }
		
		@Override
		protected InputStream fetch(Path path) throws IOException {
			DSGJSONValue value = storage.get(path);
			if (value == null) return null;
			return value.toInputStream();
	    }

		@Override
	    public Collection<DSGActivityStreamsObject> getObjects() throws IOException {
	        Collection<DSGActivityStreamsObject> result = new ArrayList<>();
	        for(Path path: storage.keySet()) {
	        	if (path.endsWith(".metadata") || path.endsWith(".tmp")) 
	        		continue;
	        	
	        	InputStream stream = fetch(path);
	        	
	        	try (DSGActivityPubReader reader = new DSGActivityPubReader(stream)) {
	                result.add(reader.read());
	        	}
	        }
	        return result;
	    }
	     
		@Override
		protected void store(Path filename, DSGJSONSerializable object) throws IOException {
			// Make sure the storage exists
			if (this.storage == null) storage = new HashMap<>();
			
			// Store JSON representation of the object
			DSGJSONValue value = object.toJSON();
			storage.put(filename, value);
		}
		
		/**
		 * Delete all resources for this storage
		 */
		public void cleanup() {
			storage.clear();
		}
	}
