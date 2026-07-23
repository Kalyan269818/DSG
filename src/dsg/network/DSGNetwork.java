package dsg.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dsg.executor.DSGBlockingQueue;
import dsg.executor.DSGExecutorService;
import dsg.executor.DSGTask;
import dsg.network.DSGCall.DSGCallType;

public class DSGNetwork {

	/* Sentinel used as the connection key shared by all ACCEPT calls, which do not yet have a remote address */
	private static final Object ACCEPT_KEY = new Object();

	/* Sentinel call inserted into the results queue on shutdown() so blocked/future collect() calls return null */
	private static final DSGCall SHUTDOWN_SIGNAL = new DSGCall(null, null) {
		@Override
		public void execute() {
			// never invoked; used only as a sentinel
		}
	};

	/* State */
	private final ServerSocket serverSocket;
	private final DSGExecutorService executor;
	private final Map<SocketAddress, Socket> sockets = new HashMap<>();
	private final Map<Object, Deque<DSGCall>> pending = new HashMap<>();
	private final Set<Object> active = new HashSet<>();
	private final DSGBlockingQueue<DSGCall> results = new DSGBlockingQueue<>();
	private final Thread reactor;

	// ##################
	// # INITIALIZATION #
	// ##################

	// Client-side constructor
	public DSGNetwork() {
		// TODO Implement constructor
		this.serverSocket = null;
		this.executor = new DSGExecutorService(1);
		this.reactor = startReactor();
	}

	// Server-side constructor
	public DSGNetwork(int port, int threads) throws IOException {
		// TODO Implement constructor
		this.serverSocket = new ServerSocket(port);
		this.executor = new DSGExecutorService(threads);
		this.reactor = startReactor();
	}

	/* Continuously drains the internal executor service and reacts to finished calls */
	private Thread startReactor() {
		Thread thread = new Thread(() -> {
			while (true) {
				DSGTask finished = executor.collect();
				if (finished == null) break;
				handleCompletion((DSGCall) finished);
			}
		});
		thread.start();
		return thread;
	}

	/* Frees up the connection lane the finished call occupied, submits the next buffered call (if any), and publishes the result */
	private void handleCompletion(DSGCall call) {
		synchronized (this) {
			Object key = connectionKey(call);
			active.remove(key);
			submitNext(key);
		}

		results.insert(call);
	}

	/* Identifies the connection a call operates on; ACCEPT calls share a single lane since they have no remote yet */
	private Object connectionKey(DSGCall call) {
		return call.getType() == DSGCallType.ACCEPT ? ACCEPT_KEY : call.getRemote();
	}

	/* Submits the next buffered call for the given connection to the executor, if there is one and none is in-flight; caller must hold the monitor */
	private void submitNext(Object key) {
		Deque<DSGCall> queue = pending.get(key);
		if (queue == null || queue.isEmpty()) return;

		DSGCall next = queue.pollFirst();
		active.add(key);
		executor.dispatch(next);
	}

	// #################
	// # SOCKET ACCESS #
	// #################

	/* Used by DSGCall subclasses; throws if no connection is stored for the given remote address */
	synchronized Socket getSocket(SocketAddress remote) throws IOException {
		Socket socket = sockets.get(remote);
		if (socket == null) throw new IOException("No connection to " + remote);
		return socket;
	}

	/* Used by DSGCall subclasses to register a newly established connection */
	synchronized void putSocket(SocketAddress remote, Socket socket) {
		sockets.put(remote, socket);
	}

	/* Used by DSGCall subclasses to discard a connection; returns null if none was stored */
	synchronized Socket removeSocket(SocketAddress remote) {
		return sockets.remove(remote);
	}

	/* Used by DSGCallAccept; null on the client side */
	ServerSocket getServerSocket() {
		return serverSocket;
	}

	// #########
	// # CALLS #
	// #########

	public void dispatch(DSGCall call) {
		// TODO Implement method
		call.setNetwork(this);

		synchronized (this) {
			Object key = connectionKey(call);
			pending.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(call);
			if (!active.contains(key)) submitNext(key);
		}
	}

	public DSGCall collect() {
		// TODO Implement method
		try {
			DSGCall call = results.retrieve();

			if (call == SHUTDOWN_SIGNAL) {
				results.insert(SHUTDOWN_SIGNAL);
				return null;
			}

			return call;
		} catch (InterruptedException e) {
			return null;
		}
	}

	// ############
	// # SHUTDOWN #
	// ############

	public void shutdown() {
		// TODO Implement method

		// Close the server socket and all connections first so any call currently blocked on I/O
		// (e.g. an in-flight ACCEPT or RECEIVE) fails instead of preventing the executor from
		// joining its worker threads.
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// Ignore; socket is being discarded anyway
			}
		}

		synchronized (this) {
			for (Socket socket : sockets.values()) {
				try {
					socket.close();
				} catch (IOException e) {
					// Ignore; socket is being discarded anyway
				}
			}
			sockets.clear();
		}

		try {
			executor.shutdown();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		try {
			reactor.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		results.insert(SHUTDOWN_SIGNAL);
	}

}
