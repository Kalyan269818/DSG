package dsg.echo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import dsg.http.DSGHTTPClient;

public class DSGRESTEchoEvaluation {

    // #################
    // # SERVER THREAD #
    // #################

    private static class ServerThread extends Thread {
        public DSGRESTEchoServer server;

        public ServerThread(DSGRESTEchoServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            server.serve();
        }

        private void terminate() throws InterruptedException {
            interrupt();
            join();
            server.shutdown();
        }
    }

    // ##############
    // # EXPERIMENT #
    // ##############

    private static int ITERATIONS = 10000;
    private static long NS_PER_US = 1000;

    private static void printResult(String name, long[] iterations) {
        Arrays.sort(iterations);

        long min = iterations[0] / NS_PER_US;
        long max = iterations[ITERATIONS - 1] / NS_PER_US;
        long median = ((iterations[ITERATIONS / 2] + iterations[ITERATIONS / 2 + 1]) / 2) / NS_PER_US;
        long avg = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            avg += iterations[i];
        }
        avg /= iterations.length;
        avg /= NS_PER_US;

        System.out.println("### EXPERIMENT RESULT ###");
        System.out.println("NAME: " + name);
        System.out.println("MIN: " + min + "us");
        System.out.println("MAX: " + max + "us");
        System.out.println("MEDIAN = " + median + "us");
        System.out.println("AVG = " + avg + "us");
        System.out.println();
    }

    private static void runExperiment(String name, DSGRESTEchoClient client) throws IOException {
        System.err.println("Running Experiment \"" + name + "\"");
        long[] iterations = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            client.echo(null);
            long end = System.nanoTime();
            iterations[i] = end - start;
        }
        printResult(name, iterations);
    }

    /**
     * Evaluate the behavior of keep-alive connections and print the result.
     *
     * @param args the command takes no commandline arguments.
     */
    public static void main(String[] args) {
        try {
            DSGRESTEchoServer server = new DSGRESTEchoServer(8000, 10);
            URI uri = new URI("http://localhost:8000/");
            ServerThread serverThread = new ServerThread(server);
            serverThread.start();

            DSGRESTEchoClient client = new DSGRESTEchoClient(uri, new DSGHTTPClient(false, false));
            runExperiment("Without Keep-Alive", client);
            client.shutdown();

            client = new DSGRESTEchoClient(uri, new DSGHTTPClient(false, true));
            runExperiment("With Keep-Alive", client);
            client.shutdown();

            serverThread.terminate();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
