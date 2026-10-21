package com.umainebiomechanicslab.gait_training_app;

import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/*
 * Diagnostic-only logger used to investigate the cross-IMU timer desync seen on Samsung
 * devices (Aim2 thigh extension study). It writes ONE CSV per trial into the current
 * session folder, recording three independent "clocks" for every streaming IMU once per
 * second:
 *
 *   - elapsedMs          : real wall-clock time since a single shared trial start
 *                          (SystemClock.elapsedRealtime). This is the same reference for
 *                          all IMUs, so their rows can be compared on one time axis.
 *   - appSampleCounter   : the app's per-IMU counter (incremented once per received BLE
 *                          packet). This is what the current code uses as "trial time".
 *   - sensorPacketCounter: the sensor's OWN packet counter carried inside each packet.
 *                          Increments inside the sensor regardless of what Bluetooth
 *                          delivers, so a gap between this and appSampleCounter reveals
 *                          dropped packets.
 *   - sensorTimeFineUs   : the sensor's hardware timestamp (microseconds). Lets us compute
 *                          the sensor's TRUE production rate, independent of Bluetooth.
 *   - outputRate         : the sensor's currently configured output rate (Hz).
 *
 * Comparing these columns across the four streaming IMUs pinpoints WHY the timers drift:
 *   - sensorPacketCounter tracks real time but appSampleCounter lags  -> dropped packets
 *   - sensorPacketCounter / sensorTimeFineUs itself lags real time    -> sub-rate throughput
 *   - outputRate is not the expected value                            -> wrong rate applied
 *
 * This class performs NO timing or signal-processing logic; it only writes a log file.
 */
public class SyncDiagnosticsLogger {

    private static final String TAG = "SyncDiagnosticsLogger";

    //Single shared trial-start reference (real wall clock) for every IMU using this logger
    private final long startElapsedMs;

    private FileWriter writer;
    private final Object writeLock = new Object();
    private boolean closed = false;

    public SyncDiagnosticsLogger(FileManager fileManager, String trialName, String timeStamp) {

        //Capture one shared start time; all elapsedMs values are measured against this
        startElapsedMs = SystemClock.elapsedRealtime();

        try {
            File sessionFolder = fileManager.getSessionFolderPath();

            //Sanitize the trial name and timestamp so they are safe for a file name
            String safeTrialName = trialName == null ? "Unknown" : trialName.replaceAll("[^a-zA-Z0-9]", "_");
            String safeTimeStamp = timeStamp == null ? "" : timeStamp.replaceAll("[^a-zA-Z0-9]", "_");

            String fileName = "SyncDiagnostics_" + safeTrialName + "_" + safeTimeStamp + ".csv";
            File csvFile = new File(sessionFolder, fileName);

            writer = new FileWriter(csvFile, true);
            writer.append("imuName,elapsedMs,appSampleCounter,sensorPacketCounter,sensorTimeFineUs,outputRate\n");
            writer.flush();

            fileManager.writeToLogFile("Sync diagnostics logging started: " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create sync diagnostics file", e);
            writer = null;
        }
    }

    public long getStartElapsedMs() {
        return startElapsedMs;
    }

    public void logRow(String imuName, long elapsedMs, int appSampleCounter,
                       int sensorPacketCounter, long sensorTimeFineUs, int outputRate) {
        if (writer == null) {
            return;
        }
        synchronized (writeLock) {
            if (closed) {
                return;
            }
            try {
                writer.append(String.format(Locale.US, "%s,%d,%d,%d,%d,%d\n",
                        imuName, elapsedMs, appSampleCounter, sensorPacketCounter,
                        sensorTimeFineUs, outputRate));
                writer.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write sync diagnostics row", e);
            }
        }
    }

    public void close() {
        synchronized (writeLock) {
            closed = true;
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close sync diagnostics file", e);
                }
                writer = null;
            }
        }
    }
}
