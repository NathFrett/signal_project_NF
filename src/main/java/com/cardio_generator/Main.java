package com.cardio_generator;

import java.io.IOException;

import com.data_management.DataStorage;

public class Main {

    /**
     * Dispatches to the desired entry-point.
     * <pre>
     *   java -jar cardio_generator.jar              → HealthDataSimulator
     *   java -jar cardio_generator.jar DataStorage  → DataStorage
     * </pre>
     */
    public static void main(String[] args) throws IOException{
        if (args.length > 0 && "DataStorage".equalsIgnoreCase(args[0])) {
            DataStorage.main(new String[0]);
        } else {
            HealthDataSimulator.main(new String[0]);
        }
    }
}
