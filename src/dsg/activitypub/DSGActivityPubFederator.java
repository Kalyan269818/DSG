package dsg.activitypub;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.http.DSGHTTPClient;
import dsg.rest.DSGRESTStub;

/**
 * Delivers activities posted to a user's inbox to its recipients.
 */
public class DSGActivityPubFederator extends Thread {
    /** Poison pull used for shutdown. */
    private static final DSGDeliverJob POISON_PILL = new DSGDeliverJob(null);
    /** Maximum number of retries until the federator gives up sending a request. */
    private static final int MAX_RETRIES = 5;

    /** The local server's base URI. */
    private URI baseURI;
    /** The HTTP client used to issue requests to other servers. */
    private DSGHTTPClient httpClient;
    /** The queue of delivery jobs. */
    private PriorityBlockingQueue<DSGDeliverJob> queue;

    /**
     * Initialize a federator instance.
     *
     * @param baseURI    the base URI of the local server that all local recipients
     *                   share.
     * @param httpClient the HTTP client that will be used to forward activities to
     *                   remote servers.
     */
    public DSGActivityPubFederator(URI baseURI, DSGHTTPClient httpClient) {
        this.baseURI = baseURI;
        this.httpClient = httpClient;
        this.queue = new PriorityBlockingQueue<>();
    }

    @Override
    public void run() {
        for (;;) {
            DSGDeliverJob job;
            try {
                job = queue.take();
                if (job == POISON_PILL) {
                    return;
                }
            } catch (InterruptedException e) {
                continue;
            }

            try {
                long now = System.currentTimeMillis();
                long nextTry = job.getRetryTimeout();
                if (now < nextTry) {
                    synchronized (queue) {
                        queue.wait(nextTry - now);
                    }
                    queue.add(job);
                    continue;
                }
            } catch (InterruptedException e) {
                queue.add(job);
                continue;
            }

            DSGActivityStreamsActivity activity = job.getActivity();
            Set<DSGActivityStreamsLink> recipients = job.getRecipients();
            Iterator<DSGActivityStreamsLink> iterator = recipients.iterator();
            System.out.println("[FEDERATOR] Trying to deliver " + activity.getId());
            while (iterator.hasNext()) {
                DSGActivityStreamsLink recipient = iterator.next();
                // Make sure we never try to deliver to the public pseudo-address or to local
                // addresses.
                if (recipient.isPublicAddress() || recipient.hasSameOrigin(baseURI)) {
                    iterator.remove();
                    continue;
                }
                System.out.println("[FEDERATOR] Trying recipient " + recipient.getTarget());
                try {
                    DSGRESTStub stub = new DSGRESTStub(recipient.getTarget(), httpClient);
                    DSGActivityPubObject object = new DSGActivityPubObjectStub(stub);
                    DSGActivityStreamsObject profile = object.get(null);
                    // Actually, The ActivityPub specification states that the recipient of a
                    // message can either be a collection or an actor and if it is a collection
                    // (e.g. a user's followers), we would have to fetch and then iterate over it
                    // recursively to find the actual recipients. Considering that the exercise is
                    // already relatively complex, we skip that part and only implement delivery to
                    // actors.
                    if (!(profile instanceof DSGActivityPubActor)) {
                        System.err.println("[FEDERATOR] " + recipient + ": target address is not an ActivityPub actor");
                        iterator.remove();
                        continue;
                    }

                    DSGActivityPubActor actor = (DSGActivityPubActor) profile;
                    if (actor.getInbox() == null) {
                        System.err.println("[FEDERATOR] " + recipient + ": actor has no inbox, skipping.");
                        iterator.remove();
                        continue;
                    }
                    DSGRESTStub inboxStub = new DSGRESTStub(actor.getInbox().getTarget(), httpClient);
                    DSGActivityPubMailbox inbox = new DSGActivityPubMailboxStub(inboxStub);
                    inbox.deliver(null, activity);
                    iterator.remove();
                    System.out.println("[FEDERATOR] " + recipient + ": delivery was successful");
                    continue;
                } catch (UnsupportedOperationException uoe) {
                    System.err.println("[FEDERATOR] " + recipient + ": server does not support federation, skipping.");
                    iterator.remove();
                } catch (Exception e) {
                    System.err.println("FEDERATOR] " + recipient + ": delivery failed, retrying some other time");
                    e.printStackTrace();
                }
            }

            int retry = job.nextTry();
            // We are done sending this activity.
            if (recipients.size() == 0 || retry >= MAX_RETRIES) {
                continue;
            }
            queue.add(job);
        }

    }

    /**
     * Deliver the given {@code activity} to its remote recipients.
     *
     * @param activity the activity to deliver to remote servers.
     */
    public void federate(DSGActivityStreamsActivity activity) {
        queue.add(new DSGDeliverJob(activity));
        synchronized (queue) {
            this.queue.notify();
        }
    }

    /**
     * Shutdown the federator, stopping all delivery attempts.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for
     *                              the federator to shut down.
     */
    public void shutdown() throws InterruptedException {
        queue.add(POISON_PILL);
        synchronized (queue) {
            this.queue.notify();
        }
        this.join();
    }

    private static class DSGDeliverJob implements Comparable<DSGDeliverJob> {
        /** Number of milliseconds between each retry (10 Seconds). */
        public static final long RETRY_RATE_LIMIT_MS = (10 * 1000);
        /** The activity to send to remote servers. */
        private DSGActivityStreamsActivity activity;
        /** Number of times it has been tried to deliver the activity. */
        private int retries;
        /** List of recipients that still have to receive the activity. */
        private Set<DSGActivityStreamsLink> recipients;
        /** Timestamp when we should try to deliver this job the next time. */
        private long retryTimeout;

        public DSGDeliverJob(DSGActivityStreamsActivity activity) {
            this.activity = activity;
            this.retries = 0;
            this.retryTimeout = 0;
            this.recipients = new HashSet<>();
            if (activity == null) {
                return;
            }
            if (activity.getTo() != null) {
                for (DSGActivityStreamsLink recipient : activity.getTo()) {
                    // Prevent local deliveries.
                    if (activity.hasSameOrigin(recipient)) {
                        continue;
                    }
                    recipients.add(recipient);
                }
            }
        }

        public DSGActivityStreamsActivity getActivity() {
            return activity;
        }

        public long getRetryTimeout() {
            return retryTimeout;
        }

        public int nextTry() {
            this.retries += 1;
            this.retryTimeout = System.currentTimeMillis() + (retries * RETRY_RATE_LIMIT_MS);
            return retries;
        }

        public Set<DSGActivityStreamsLink> getRecipients() {
            return recipients;
        }

        @Override
        public int compareTo(DSGDeliverJob other) {
            return (int) (this.retryTimeout - other.retryTimeout);
        }
    }
}
