package com.cardio_generator.outputs;

/**
 * Every generator calls {@link #output(int, long, String, String)} exactly once
 * for each data point it produces.
 */
public interface OutputStrategy {
     /**
     * Accepts a single data point.
     *
     * @param patientId  unique patient identifier (1-based)
     * @param timestamp  epoch milliseconds when the measurement was taken
     * @param label      human-readable label, e.g. {@code "ECG"} or {@code "Saturation"}
     * @param data       value as string; formatting depends on generator
     */
    void output(int patientId, long timestamp, String label, String data);
}
