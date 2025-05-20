package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Common contract for every data-generator in the simulator.
 * Implementations inject one or more data points into the chosen
 * {@link com.cardio_generator.outputs.OutputStrategy OutputStrategy}
 * each time {@link #generate(int, OutputStrategy)} is called.
 *
 * This interface remains intentionally lean:
 * only one method, no side-effects mandated.
 */
public interface PatientDataGenerator {
    /**
     * Generate one tick of data for a single patient and pass it to the output layer.
     *
     * @param patientId      the 1-based identifier of the simulated patient
     * @param outputStrategy callback that persists, prints, or streams the data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
