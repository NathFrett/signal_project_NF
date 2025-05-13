package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

public class AlertGenerator implements PatientDataGenerator {

    // Changed constant name to UPPER_CASE to follow Google style
    public static final Random RANDOM_GENERATOR = new Random();

    // Changed array name to lowerCamelCase to follow Google style
    private boolean[] alertStates; // false = resolved, true = pressed

    // Extracted magic numbers into constant
    private static final double RESOLUTION_PROBABILITY= 0.9; // 90% chance to resolve
    private static final double LAMBDA = 0.1; // Average number of alerts per time unit

    public AlertGenerator(int patientCount) {
        // Added 'this.' for clarity; array sized (patientCount + 1) to allow 1‑based IDs
        this.alertStates = new boolean[patientCount + 1];
    }

    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                // Patient already in alert state – attempt resolution
                if (RANDOM_GENERATOR.nextDouble() < RESOLUTION_PROBABILITY) {
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Changed variable name to lowerCamelCase (was 'p')
                double probabilityOfAlert = -Math.expm1(-LAMBDA); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < probabilityOfAlert;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception exception) { // Changed variable name from 'e' ➔ 'exception' for clarity
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            exception.printStackTrace();
        }
    }
}
