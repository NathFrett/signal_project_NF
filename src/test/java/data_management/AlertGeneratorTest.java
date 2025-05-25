package data_management;

import com.alerts.AlertGenerator;
import com.alerts.Alert;

import com.data_management.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AlertGeneratorTest {

    // helper to create storage + patient with one reading
    private static void add(DataStorage s, int id, double v,
                            String label, long ts) {
        s.addPatientData(id, v, label, ts);
    }

    /* ---------------- critical threshold ---------------- */
    @Test
    void criticalBpAlertIsRaised() {
        DataStorage store = new DataStorage();
        add(store, 1, 185, "Systolic", 1);

        AlertGenerator ag = new AlertGenerator(store);
        ag.evaluateData(new Patient(1));

        assertTrue(ag.getAlerts().stream()
                     .anyMatch(a -> a.getCondition().startsWith("Critical BP")));
    }

    /* ---------------- monotone trend (+10) -------------- */
    @Test
    void bpTrendAlertIsRaised() {
        DataStorage s = new DataStorage();
        add(s, 2, 120, "Systolic", 1);
        add(s, 2, 131, "Systolic", 2);
        add(s, 2, 142, "Systolic", 3);

        AlertGenerator ag = new AlertGenerator(s);
        ag.evaluateData(new Patient(2));

        assertTrue(ag.getAlerts().stream()
                     .anyMatch(a -> a.getCondition().contains("Trend")));
    }

    /* ---------------- low SpO₂ (<92 %) ------------------ */
    @Test
    void lowSpo2AlertIsRaised() {
        DataStorage s = new DataStorage();
        add(s, 3, 90, "Saturation", 1);

        AlertGenerator ag = new AlertGenerator(s);
        ag.evaluateData(new Patient(3));

        assertTrue(ag.getAlerts().stream()
                     .anyMatch(a -> a.getCondition().contains("Low SpO₂")));
    }

    /* -------------- rapid SpO₂ drop (>=5 %) ------------- */
    @Test
    void rapidSpo2DropAlertIsRaised() {
        DataStorage s = new DataStorage();
        add(s, 4, 97, "Saturation", 1);          // older
        add(s, 4, 90, "Saturation", 2);          // newer (drop 7)

        AlertGenerator ag = new AlertGenerator(s);
        // need two calls to build history
        ag.evaluateData(new Patient(4));         // first value
        ag.evaluateData(new Patient(4));         // second → evaluation

        assertTrue(ag.getAlerts().stream()
                     .anyMatch(a -> a.getCondition().contains("Rapid SpO₂ Drop")));
    }

    /* -------- combined hypotensive-hypoxemia ------------ */
    @Test
    void combinedAlertIsRaised() {
        DataStorage s = new DataStorage();
        add(s, 5, 85, "Systolic",    1); // low BP
        add(s, 5, 91, "Saturation",  1); // low SpO₂

        AlertGenerator ag = new AlertGenerator(s);
        ag.evaluateData(new Patient(5));

        assertTrue(ag.getAlerts().stream()
                     .anyMatch(a -> a.getCondition().contains("Hypotensive-Hypoxemia")));
    }
}
