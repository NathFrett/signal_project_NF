package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates realistic SpO(2) (blood-oxygen saturation) values for each patient.
 * Every call to {@link #generate(int, OutputStrategy)} nudges the previous
 * saturation value up or down by +-1 % and clamps it to a safe range [90, 100].
 *
 * Thread-safety: one instance per scheduled task, therefore no synchronisation
 * is required (all state is private to the generator).
 */

public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    // Last emitted saturation level for every patient (index 0 unused).
    private int[] lastSaturationValues;

    // @param patientCount highest patient ID that will ever be generated
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Simulate a new SpO(2) value and forward it to the output layer.
     *
     * @param patientId      1-based patient identifier
     * @param outputStrategy output target
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
