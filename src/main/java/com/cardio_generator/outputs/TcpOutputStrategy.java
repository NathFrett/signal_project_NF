package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Streams newline-delimited CSV over a plain TCP socket to a "single" client.
 *
 * Only the first client that connects is served; subsequent connections are ignored
 * until the current client disconnects.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Opens a server socket and waits for one client.
     *
     * @param port TCP port to listen on
     * @throws IllegalArgumentException if {@code port} is not between 0 and 65535
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends one CSV line to the connected client, if present.
     *
     * @param patientId unique patient ID
     * @param timestamp epoch milliseconds
     * @param label     measurement label
     * @param data      measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
