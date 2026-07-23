package dsg.microblog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dsg.activitypub.DSGActivityPubReader;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.json.DSGJSONArray;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONReader;
import dsg.json.DSGJSONSerializable;
import dsg.json.DSGJSONString;
import dsg.json.DSGJSONValue;

/**
 * Persistently stores ActivityStreams objects created by the microblog to the
 * server's local filesystem.
 */
public class DSGMicroBlogStorage {
    /** File storing storage metadata. */
    private static final String METADATA_FILE = ".metadata";

    /** The root directory where files are stored. */
    private Path directory;
    /** Path to the metadatafile. */
    private Path metadataPath;

    /**
     * Initialize the storage, storing all objects in the named {@code directory}.
     *
     * @param directory the local directory where all object will be stored.
     * @throws IOException if the creation of the directory or the metadata file
     *                     fails.
     */
    public DSGMicroBlogStorage(String directory) throws IOException {
        this.directory = Path.of(directory).toAbsolutePath();
        this.metadataPath = this.directory.resolve(METADATA_FILE);
    }

    /**
     * Return all users stored in this storage.
     *
     * @return an array containing all users
     * @throws IOException if an error occurs while reading a file.
     */
    public Collection<DSGMicroBlogUser> getUsers() throws IOException {
        Metadata metadata = getMetadata();
        Collection<DSGMicroBlogUser> users = new ArrayList<>();
        for (String userID : metadata.getUserIDs()) {
            try {
                URI id = new URI(userID);
                DSGMicroBlogUser user = getUser(id);
                if (user == null) {
                    continue;
                }
                users.add(user);
            } catch (URISyntaxException _) {
                continue;
            }
        }
        return users;
    }

    /**
     * Get the user with the named {@code id}.
     *
     * @param id the user's ID.
     * @return the user or null, if no user with the given ID exists.
     * @throws IOException if an error occurs while reading from storage.
     */
    public DSGMicroBlogUser getUser(URI id) throws IOException {
        InputStream input = fetch(id);
        if (input == null) {
            return null;
        }
        try (DSGJSONReader reader = new DSGJSONReader(input)) {
            DSGJSONObject value = reader.readObject();
            if (value == null) {
                return null;
            }
            return new DSGMicroBlogUser(value);
        }
    }

    /**
     * Store the user in the persistent storage.
     *
     * @param user the user to store.
     * @throws IOException if an error occurs while writing to storage.
     */
    public void storeUser(DSGMicroBlogUser user) throws IOException {
        Metadata metadata = getMetadata();
        metadata.addUser(user.getId());
        storeMetadata(metadata);
        store(user.getId(), user);
    }

    /**
     * Get the ActivityStreams object identified by {@code link}.
     *
     * @param link the link pointing to the object.
     * @return the object or null, if the object does not exist.
     * @throws IOException if an error occurs while reading from storage.
     */
    public DSGActivityStreamsObject getObject(DSGActivityStreamsLink link) throws IOException {
        if (link.getTarget() == null) {
            return null;
        }
        InputStream input = fetch(link.getTarget());
        if (input == null) {
            return null;
        }
        try (DSGActivityPubReader reader = new DSGActivityPubReader(input)) {
            return reader.read();
        }
    }

    /**
     * Get the ActivityStreams object identified by {@code id}.
     *
     * @param id the object's ID.
     * @return the object or null, if the object does not exist.
     * @throws IOException if an error occurs while reading from storage.
     */
    public DSGActivityStreamsObject getObject(URI id) throws IOException {
        InputStream input = fetch(id);
        if (input == null) {
            return null;
        }
        try (DSGActivityPubReader reader = new DSGActivityPubReader(input)) {
            return reader.read();
        }
    }

    /**
     * Store an ActivityStreams object to persistent storage.
     *
     * @param object the object to store.
     * @throws IOException if an error occurs while writing to storage.
     */
    public void storeObject(DSGActivityStreamsObject object) throws IOException {
        store(object.getId(), object);
    }

    /**
     * Return, whether the object identified by {@code id} is stored in this
     * storage.
     *
     * @param id the object's ID.
     * @return true if the object is stored in this storage, false otherwise.
     */
    public boolean exists(URI id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        Path path = getPath(id);
        return Files.exists(path);
    }

    /**
     * Check if the given {@code object} is already stored in this storage.
     *
     * @param object the object to check.
     * @return true if the object is stored in this storage, false otherwise.
     */
    public boolean exists(DSGActivityStreamsObject object) {
        return exists(object.getId());
    }

    /**
     * Return all objects stored in this storage.
     *
     * @return a collection containing all objects currently stored in this storage.
     * @throws IOException if an error occurs while reading from storage.
     */
    public Collection<DSGActivityStreamsObject> getObjects() throws IOException {
        Collection<DSGActivityStreamsObject> result = new ArrayList<>();
        Files.walkFileTree(directory, new HashSet<>(), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
                // Ignore metadatafile and temporary files
                if (path.getFileName().equals(METADATA_FILE) || path.endsWith(".tmp")) {
                    return FileVisitResult.CONTINUE;
                }

                try (InputStream input = fetch(path); DSGActivityPubReader reader = new DSGActivityPubReader(input)) {
                    DSGActivityStreamsObject object = (DSGActivityStreamsObject) reader.read();
                    result.add(object);
                } catch (Exception e) {
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }

    /**
     * Fetch an input stream for the object identified by {@code id}.
     *
     * @param id the ID of the desired object.
     * @return an input stream of the object or null, if it does not exist.
     * @throws IOException if reading the object from storage fails.
     */
    protected InputStream fetch(URI id) throws IOException {
        Path path = getPath(id);
        return fetch(path);
    }

    /**
     * Return an {@link InputStream} for the file located at {@code path}.
     *
     * @param path the path to the desired file.
     * @return an {@link InputStream} of the file, or null, if it does not exist.
     * @throws IOException if reading the target path fails.
     */
    protected InputStream fetch(Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }
        return new FileInputStream(path.toFile());
    }

    /**
     * Store the given {@code object} in the named file.
     *
     * @param filename the name of the file where the object will be stored.
     * @param object   the object to store.
     * @throws IOException if writing to the file fails.
     */
    protected void store(Path filename, DSGJSONSerializable object) throws IOException {
        // Make sure the storage directory exists.
        File storageDirectory = directory.toFile();
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }

        Path tmpFilename = Path.of(filename.toString() + ".tmp");
        tmpFilename.toFile().createNewFile();
        InputStream content = object.toJSON().toInputStream();
        try (FileOutputStream output = new FileOutputStream(tmpFilename.toFile())) {
            content.transferTo(output);
            output.flush();
            // Force the operating system to write the file to persistent storage, otherwise
            // we might return that the store was successful, when in fact it was not.
            output.getFD().sync();
        }

        // Finally, move the file such that the store operation either succeeds or fails
        // atomically.
        Files.move(tmpFilename, filename, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Return the path to the file where the object with the named {@code id} is
     * stored.
     *
     * @param id the ID of the desired object.
     * @return the path to the file storing the desired object.
     */
    protected Path getPath(URI id) {
        if (!id.isAbsolute() || id.isOpaque()) {
            throw new IllegalArgumentException("ID must be a full URI");
        }
        String filename = Base64.getUrlEncoder().encodeToString(id.toASCIIString().getBytes());
        return Path.of(directory.toString(), filename);
    }

    private Metadata getMetadata() throws IOException {
        InputStream input = fetch(metadataPath);
        if (input == null) {
            return new Metadata();
        }
        try (DSGJSONReader reader = new DSGJSONReader(input)) {
            DSGJSONValue value = reader.read();
            if (!(value instanceof DSGJSONObject)) {
                return new Metadata();
            }
            return new Metadata((DSGJSONObject) value);
        }
    }

    private void storeMetadata(Metadata metadata) throws IOException {
        store(this.metadataPath, metadata);
    }

    private void store(URI id, DSGJSONSerializable object) throws IOException {
        Path filename = getPath(id);
        store(filename, object);
    }

    private static class Metadata implements DSGJSONSerializable {
        /** IDs pointing to users. */
        private Set<String> userIDs;

        /** Initialize empty metadata */
        public Metadata() {
            this.userIDs = new HashSet<>();
        }

        /** Deserialize metadata from a JSONObject */
        public Metadata(DSGJSONObject object) {
            this();
            DSGJSONValue v = object.getMember("users");
            if (!(v instanceof DSGJSONArray)) {
                return;

            }
            DSGJSONArray ids = (DSGJSONArray) v;
            for (DSGJSONValue item : ids) {
                try {
                    DSGJSONString s = (DSGJSONString) item;
                    this.userIDs.add(s.toString());
                } catch (ClassCastException _) {
                    continue;
                }
            }
        }

        public void addUser(URI id) {
            if (id == null) {
                return;
            }
            this.userIDs.add(id.toASCIIString());
        }

        public Set<String> getUserIDs() {
            return userIDs;
        }

        @Override
        public DSGJSONValue toJSON() {
            DSGJSONObject object = new DSGJSONObject();
            DSGJSONArray users = new DSGJSONArray();
            object.setMember("users", users);
            for (String id : userIDs) {
                users.add(id);
            }
            return object;
        }

    }
}
