package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes each measurement type to its own text file under a base directory.
 *
 * Thread-safe: a {@link ConcurrentHashMap} is used so that multiple generator
 * threads can log simultaneously.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Changed field name to lowerCamelCase and marked final (immutable after construction)
    private final String baseDirectory;

    // Changed name to lowerCamelCase, made private & final to obey encapsulation rules.
    private final ConcurrentHashMap<String, Path> fileMap = new ConcurrentHashMap<>();

    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Google style: method calls indented + ensure the directory exists
            Files.createDirectories(Path.of(baseDirectory));
        } catch (IOException ioException) { // Renamed variable from 'e' to 'ioException' for clarity
            System.err.println("Error creating base directory: " + ioException.getMessage());
            return;
        }
        // Set the filePath variable
        // Set filePath to lowerCamelCase
        Path filePath = fileMap.computeIfAbsent(label, l -> Path.of(baseDirectory, l + ".txt"));

        // Try‑with‑resources: automatically closes the writer
        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            writer.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (IOException ioException) { // Catch the specific checked exception.
            System.err.println("Error writing to file " + filePath + ": " + ioException.getMessage());
        }
    }
}