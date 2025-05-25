package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met.
 * …
 */
public class AlertGenerator {
    private final DataStorage dataStorage;
    private final List<Alert> alerts = new ArrayList<>();

    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /* ==============================================================
       ================  PUBLIC  =====================================
       ============================================================== */

    /**
     * Evaluates the specified patient's data to determine if any alert
     * conditions are met.  If a condition is met, an alert is triggered via
     * {@link #triggerAlert(Alert)}.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        int    id  = patient.getPatientId();
        long   now = System.currentTimeMillis();
        List<PatientRecord> all =
                dataStorage.getRecords(id, 0, now);

        // --- latest individual readings ------------------------------------
        Integer sys = latest(all, this::isSystolic);
        Integer dia = latest(all, this::isDiastolic);
        Integer spo = latest(all, this::isSpo2);

        // --- 1. critical BP thresholds -------------------------------------
        if (criticalBp(sys, dia)) {
            triggerAlert(new Alert(idString(id),
                    "Critical BP " + fmt(sys) + "/" + fmt(dia), now));
        }

        // --- 2. BP trend (3 consecutive ±10 mmHg steps) --------------------
        if (trend(latestThree(all, this::isSystolic))
                || trend(latestThree(all, this::isDiastolic))) {
            triggerAlert(new Alert(idString(id), "BP Trend Alert", now));
        }

        // --- 3. persistent low SpO₂ (<92 %) --------------------------------
        if (spo != null && spo < 92) {
            triggerAlert(new Alert(idString(id),
                    "Low SpO\u2082 (" + spo + "%)", now));
        }

        // --- 4. rapid SpO₂ drop (≥5 % between last two readings) -----------
        if (rapidDrop(latestThree(all, this::isSpo2), 5)) {
            triggerAlert(new Alert(idString(id), "Rapid SpO\u2082 Drop", now));
        }

        // --- 5. combined hypotensive-hypoxemia -----------------------------
        if (sys != null && spo != null && sys < 90 && spo < 92) {
            triggerAlert(new Alert(idString(id),
                    "Hypotensive-Hypoxemia", now));
        }
    }

    /* package-private getter used only by the JUnit tests */
    public List<Alert> getAlerts() { return alerts; }

    /**
     * Triggers an alert for the monitoring system…
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // avoid duplicate alerts for the same patient / condition / run
        boolean seen = alerts.stream()
                             .anyMatch(a -> a.getPatientId().equals(alert.getPatientId())
                                         && a.getCondition().equals(alert.getCondition()));
        if (!seen) {
            alerts.add(alert);
            System.out.printf("ALERT: patient %s - %s%n",
                              alert.getPatientId(), alert.getCondition());
        }
    }

    /* ---------------- label helpers -------------------------------------- */

    private boolean isSystolic(String t)  { return t.toLowerCase().contains("systolic"); }
    private boolean isDiastolic(String t) { return t.toLowerCase().contains("diastolic"); }

    /** any label that mentions “spo” or “saturation” counts as SpO₂ */
    private boolean isSpo2(String t) {
        String s = t.toLowerCase();
        return s.contains("spo") || s.contains("saturation");
    }

    /* ---------------- data-extraction helpers ----------------------------- */

    private Integer latest(List<PatientRecord> list,
                           java.util.function.Predicate<String> labelMatch) {
        for (int i = list.size() - 1; i >= 0; i--) {
            PatientRecord r = list.get(i);
            if (labelMatch.test(r.getRecordType())) {
                return (int) r.getMeasurementValue();
            }
        }
        return null;
    }

    /** newest N values (oldest-to-newest order) matching predicate */
    private List<Integer> latestThree(List<PatientRecord> list,
                                      java.util.function.Predicate<String> labelMatch) {
        return list.stream()
                   .filter(r -> labelMatch.test(r.getRecordType()))
                   .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                   .map(r -> (int) r.getMeasurementValue())
                   .collect(Collectors.collectingAndThen(
                           Collectors.toList(),
                           l -> l.subList(Math.max(0, l.size() - 3), l.size())));
    }

    /* ---------------- rule checks ---------------------------------------- */

    private boolean criticalBp(Integer sys, Integer dia) {
        return (sys != null && (sys > 180 || sys < 90))
            || (dia != null && (dia > 120 || dia < 60));
    }

    /** three consecutive monotone steps of >10 mmHg */
    private boolean trend(List<Integer> vals) {
        if (vals.size() < 3) return false;
        int a = vals.get(vals.size() - 3);
        int b = vals.get(vals.size() - 2);
        int c = vals.get(vals.size() - 1);
        return  (c - b > 10 && b - a > 10)   // rising
             || (a - b > 10 && b - c > 10);  // falling
    }

    /** drop of ≥delta % between the newest two SpO₂ readings */
    private boolean rapidDrop(List<Integer> vals, int delta) {
        if (vals.size() < 2) return false;
        int prev = vals.get(vals.size() - 2);
        int last = vals.get(vals.size() - 1);
        return prev - last >= delta;
    }

    private static String idString(int id) { return Integer.toString(id); }
    private static String fmt(Integer v)   { return v == null ? "?" : v.toString(); }
}
