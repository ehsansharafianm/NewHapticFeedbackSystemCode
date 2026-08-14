package com.umainebiomechanicslab.gait_training_app.imus;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.SyncDiagnosticsLogger;
import com.umainebiomechanicslab.gait_training_app.studymanagers.IMUManager;
import com.umainebiomechanicslab.gait_training_app.trials.Trial;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.UserInterfaceWithIMU;
import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.interfaces.DotMeasurementCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.DotPayload;
import com.xsens.dot.android.sdk.utils.DotLogger;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StreamingIMU extends UniversalIMU implements DotMeasurementCallback {

    private final String TAG = "StreamingIMU";

    protected String trialName;
    protected int sampleCounter;
    protected int trialDurationMin;
    protected double trialAngleSum;
    private boolean isStoppingInitialization = false;

    protected FileManager.DotLogFile dotLogFile;
    protected boolean offsetAnglesInitialized;
    protected boolean isHeadingReset, isAwaitingHeadingResetAfterMeasurementStart;
    protected boolean isAwaitingHeadingRevertAfterMeasurementStart, isHeadingReverting;
    protected int offsetInitializationDurationSec;
    protected double offsetEulerAngle;
    private final Object sampleCounterLock = new Object();

    //Diagnostic-only logger for investigating cross-IMU timer desync on Samsung (logging only)
    protected SyncDiagnosticsLogger syncDiagnosticsLogger;

    public StreamingIMU(String nameOfIMU, Context context, IMUManager imuManager, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode){

        super(nameOfIMU, context, imuManager, userInterface, fileManager, measurementMode);

        //Offset Angles are not Initialized By Default
        this.offsetAnglesInitialized = false;

        //Heading is not reset by default
        this.isHeadingReset = false;
        this.isAwaitingHeadingResetAfterMeasurementStart = false;
        this.isHeadingReverting = false;
        this.isAwaitingHeadingRevertAfterMeasurementStart = false;

        //Set Duration for Initialization of Offset Angles
        offsetInitializationDurationSec = 3;

    }

    @Override
    public DotDevice connectMovellaDotDevice(BluetoothDevice bluetoothDevice) {
        // Create the DotDevice with THIS class as the callback listener.
        movellaDotDevice = new DotDevice(context.getApplicationContext(), bluetoothDevice, this);
        isScanned = true;
        userInterface.updateIMUStatus(nameOfIMU, "Scanned");
        imuStatus = "Scanned";
        fileManager.writeToLogFile(nameOfIMU + " is Scanned");
        movellaDotDevice.connect();
        movellaDotDevice.setDotMeasurementCallback(this);
        return movellaDotDevice;
    }

    public boolean getIsHeadingReset() {
        return isHeadingReset;
    }

    public double getOffsetEulerAngle() {
        return offsetEulerAngle;
    }

    public boolean getOffsetAnglesInitialized() {
        return offsetAnglesInitialized;
    }

    public void performHeadingReset() {


        // ⭐ Skipping the Heading reset for now
        isHeadingReset = true;  // Pretend it's already reset
        fileManager.writeToLogFile(nameOfIMU + " Heading reset skipped (disabled)");
        userInterface.updateIMUStatus(nameOfIMU, "Ready");
        imuManager.onHeadingResetComplete();  // Tell manager we're "done"
        return;  // Exit early - don't do the reset


        // Skipping the Heading reset for now
 /*       // Set the flag to false.
        isHeadingReset = false;

        try {

            //If a revert is required, perform it.
            if (movellaDotDevice != null && movellaDotDevice.getHeadingStatus() == DotDevice.HEADING_STATUS_XRM_HEADING && movellaDotDevice.startMeasuring()) {
                trialName = "HeadingRevert";
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started for Heading Revert. Waiting for first data packet...");
                userInterface.updateIMUStatus(nameOfIMU, "Reverting Heading...");
                fileManager.writeToLogFile(nameOfIMU + " reverting heading");

                // Set the flag to true. The reset command will be sent in onDotDataChanged.
                isHeadingReverting = true;
                isAwaitingHeadingRevertAfterMeasurementStart = true;
            }
            //If no revert is required, reset the heading.
            else if (movellaDotDevice != null && movellaDotDevice.startMeasuring()) {
                trialName = "HeadingReset";
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started for Heading Reset. Waiting for first data packet...");
                userInterface.updateIMUStatus(nameOfIMU, "Resetting Heading...");
                fileManager.writeToLogFile(nameOfIMU + " resetting heading");

                // Set the flag to true. The reset command will be sent in onDotDataChanged.
                isAwaitingHeadingResetAfterMeasurementStart = true;
            } else {
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected or failed to start measurement for reset.");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }

        } catch (NullPointerException e) {
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "HeadingReset", e);
        }*/

    }


    public void startOffsetInitialization(){
        if (!isHeadingReset) {
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " heading not reset. Please reconnect the sensor.");
            fileManager.writeToLogFile("Error: " + nameOfIMU + " startOffsetInitialization failed. Heading not reset.");
            return;
        }

        trialName = "Initialization";
        sampleCounter = 0;
        trialAngleSum = 0;
        offsetAnglesInitialized = false;

        // ✅ ADD THIS
        fileManager.writeToLogFile(nameOfIMU + " startOffsetInitialization called. trialName set to: " + trialName);
        fileManager.writeToLogFile(nameOfIMU + " Target sample count: " + (outputFrequency * offsetInitializationDurationSec));

        try{
            boolean started = movellaDotDevice.startMeasuring();
            fileManager.writeToLogFile(nameOfIMU + " startMeasuring() returned: " + started);

            if(started) {
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started Successfully");
                userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
            }
            else{
                fileManager.writeToLogFile("ERROR: " + nameOfIMU + " startMeasuring() returned FALSE");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        }
        catch (NullPointerException e){
            fileManager.writeToLogFile("ERROR: " + nameOfIMU + " Not Connected (NullPointerException)");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "startOffsetInitialization", e);
        }
    }

    public void stopOffsetInitialization(){
        // ✅ Prevent multiple calls
        if (isStoppingInitialization) {
            fileManager.writeToLogFile(nameOfIMU + " stopOffsetInitialization already called, skipping");
            return;
        }
        isStoppingInitialization = true;

        if(movellaDotDevice != null){
            try{
                if(movellaDotDevice.stopMeasuring()){
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Stopped");
                }
                else{
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e){
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "stopOffsetInitialization", e);
            }
        }

        imuManager.onAngleOffsetInitializationComplete();
    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        // Prevent trials from starting if the IMU has not been initialized (heading reset).
        if (!isHeadingReset || !offsetAnglesInitialized) {
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " not initialized. Please run Initialization first.");
            fileManager.writeToLogFile("Error: " + nameOfIMU + " startTrial failed. Heading not reset or offset not calculated.");
            return;
        }

        this.trialName = trialName;
        this.trialDurationMin = trialDurationMin;
        sampleCounter = 0;

        if(logData){
            //Stream and save results to logFile
            dotLogFile = createDataLog(trialName, timeStamp);
        }
        else{
            dotLogFile = null;
        }

        try{
            if(movellaDotDevice.startMeasuring()) {
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started");
            }
            else{
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        }
        catch (NullPointerException e){
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "startTrial", e);
        }

    }

    @Override
    public FileManager.DotLogFile stopTrial(boolean logData) {

        if(movellaDotDevice != null){
            try{
                if(movellaDotDevice.stopMeasuring()){
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Stopped");
                    if(logData){

                        //Pause for 0.1 seconds to allow the IMU to connect
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "stopTrial", e);
                        }

                        //Stop the logger that was created in startTrial()
                        dotLogFile.getDotLogger().stop();
                        return dotLogFile;
                    }
                }
                else{
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e){
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "stopTrial", e);
            }
        }
        else{
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
        }
        return null;
    }

    @Override
    protected FileManager.DotLogFile createDataLog(String trialName, String timeStamp){
        try{
            File sessionFolderPath = fileManager.getSessionFolderPath();
            String dotLogFileFolder = sessionFolderPath.getPath() + "/Dot Log Files";

            //Ensure dotLogFileFolder exists
            if(!new File(dotLogFileFolder).exists()){
                if(!new File(dotLogFileFolder).mkdirs()){
                    Log.e(TAG, "createDataLog: Failed to create dotLogFileFolder");
                }
            }
            String dotLogFileName = movellaDotDevice.getTag() + "_" + nameOfIMU + "_" + trialName + "_" + timeStamp + ".csv";
            String dotLogFilePath = dotLogFileFolder + "/" + dotLogFileName;
            File dotLogFile = new File(dotLogFilePath);

            fileManager.writeToLogFile(dotLogFileName + " created");

            DotLogger logger = new DotLogger(context.getApplicationContext(), 1, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1, dotLogFilePath, movellaDotDevice.getTag(), movellaDotDevice.getFirmwareVersion(),true,60,null,"25",0);

            return new FileManager.DotLogFile(dotLogFileName, dotLogFile, logger);
        }
        catch(NullPointerException e){
            fileManager.writeToLogFile("Error: Sensor Not Found");
            userInterface.errorMessagePopUp("Error: Sensor not found");
            Log.e(TAG, "createDataLog", e);
            return null;
        }
    }

    public void setSyncDiagnosticsLogger(SyncDiagnosticsLogger syncDiagnosticsLogger) {
        this.syncDiagnosticsLogger = syncDiagnosticsLogger;
    }

    /*
     * Diagnostic-only. Writes one row (roughly once per second) comparing three independent
     * clocks for this IMU: real elapsed time, the app's received-packet counter, and the
     * sensor's own packet counter/timestamp. Used to locate the origin of the cross-IMU timer
     * desync observed on Samsung. Does NOT affect trial timing or signal processing.
     */
    protected void logSyncDiagnostics(DotData dotData) {

        SyncDiagnosticsLogger logger = syncDiagnosticsLogger;
        if (logger == null) {
            return;
        }

        //Throttle to roughly one row per second per IMU
        if ((sampleCounter % outputFrequency) != 0) {
            return;
        }

        //Real wall-clock time since the single shared trial start
        long elapsedMs = SystemClock.elapsedRealtime() - logger.getStartElapsedMs();

        //Ground-truth values carried inside the sensor packet
        int sensorPacketCounter = dotData.getPacketCounter();
        long sensorTimeFineUs = dotData.getSampleTimeFine();

        //Currently configured output rate for this sensor (Hz)
        int outputRate = -1;
        try {
            if (movellaDotDevice != null) {
                outputRate = movellaDotDevice.getCurrentOutputRate();
            }
        } catch (Exception e) {
            //Leave outputRate as -1 if it cannot be read
        }

        logger.logRow(nameOfIMU, elapsedMs, sampleCounter, sensorPacketCounter, sensorTimeFineUs, outputRate);
    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {



        /*if (sampleCounter % 60 == 0) {  // Log every second (every 60 samples)
            fileManager.writeToLogFile(nameOfIMU + " onDotDataChanged: trialName=" + trialName + ", sampleCounter=" + sampleCounter + ", targetSample=" + (outputFrequency * offsetInitializationDurationSec));
        }*/

        if (isAwaitingHeadingResetAfterMeasurementStart) {
            isAwaitingHeadingResetAfterMeasurementStart = false; // Consume the flag so this only runs once.
            fileManager.writeToLogFile(nameOfIMU + " is now measuring. Sending resetHeading command.");
            movellaDotDevice.resetHeading();
            return; // Exit here. We don't want to process this first data packet.
        }
        else if (isAwaitingHeadingRevertAfterMeasurementStart) {
            isAwaitingHeadingRevertAfterMeasurementStart = false; // Consume the flag so this only runs once.
            fileManager.writeToLogFile(nameOfIMU + " is now measuring. Sending revertHeading command.");
            movellaDotDevice.revertHeading();
            return; // Exit here. We don't want to process this first data packet.
        }

        if(trialName == null){
            return;
        }

        double eulerAngleX = dotData.getEuler()[0];

        synchronized(sampleCounterLock) {

            //Diagnostic-only: capture the sensor's ground-truth clocks BEFORE the app
            //overwrites the packet counter below (Samsung desync investigation)
            logSyncDiagnostics(dotData);

            switch (trialName) {
                case "HeadingReset":
                case "HeadingRevert":
                    // Do nothing here. We are just waiting for the onDotHeadingChanged callback.
                    break;
            /*case "Initialization":
                if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)){
                    if ((sampleCounter % outputFrequency) == 0) {
                        userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
                    }
                    trialAngleSum += eulerAngleX;
                } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)){
                    offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                    userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"Initialized %.3f",offsetEulerAngle));
                    fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);
                    offsetAnglesInitialized = true;
                    stopOffsetInitialization();
                }
                break;*/
            /*case "Initialization":
                if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)){
                    if ((sampleCounter % outputFrequency) == 0) {
                        userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
                    }
                    trialAngleSum += eulerAngleX;
                }
                else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)){
                    // Calculate offset
                    offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);

                    // ✅ SET FLAG FIRST - CRITICAL!
                    offsetAnglesInitialized = true;

                    // Update UI and log
                    userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"Initialized %.3f",offsetEulerAngle));
                    fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);

                    // Stop measuring
                    // stopOffsetInitialization();
                }
                // ✅ ADD THIS: Continue processing one more sample after stopping
                else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec) + 1){
                    // This ensures we process data even after stopping
                    // Don't do anything, just let the sample counter increment
                }
                break;*/
                case "Initialization":
                    if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)) {
                        if ((sampleCounter % outputFrequency) == 0) {
                            userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
                            // ✅ ADD THIS
                            fileManager.writeToLogFile(nameOfIMU + " Initializing... sample " + sampleCounter + "/" + (outputFrequency * offsetInitializationDurationSec));
                        }
                        trialAngleSum += eulerAngleX;
                    } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)) {
                        // ✅ ADD THIS
                        fileManager.writeToLogFile(nameOfIMU + " *** REACHED COMPLETION at sample " + sampleCounter + " ***");

                        offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                        offsetAnglesInitialized = true;

                        userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "Initialized %.3f", offsetEulerAngle));
                        fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);

                        stopOffsetInitializationWithoutManagerCall();
                    }
                    // ✅ ADD THIS - check if we somehow passed the target
                    else if (sampleCounter > (outputFrequency * offsetInitializationDurationSec)) {
                        if (sampleCounter % 60 == 0) {
                            fileManager.writeToLogFile(nameOfIMU + " WARNING: Passed target sample! Currently at " + sampleCounter);
                        }
                    }
                    break;
                default:
                    if (trialDurationMin == 0) {
                        dotData.setPacketCounter(sampleCounter);
                        if ((sampleCounter % (outputFrequency / 3)) == 0) {
                            userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%.3f", (eulerAngleX - offsetEulerAngle)));
                        }
                        if (dotLogFile != null && dotLogFile.getDotLogger() != null) {
                            dotLogFile.getDotLogger().update(dotData);
                        }
                    } else {
                        dotData.setPacketCounter(sampleCounter);
                        if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {
                            if ((sampleCounter % outputFrequency) == 0) {
                                userInterface.updateIMUDataOutput(nameOfIMU, ((sampleCounter / outputFrequency / 60) + ":" + String.format(Locale.US, "%02d", ((sampleCounter / outputFrequency) % 60))));
                            }
                            if (dotLogFile != null && dotLogFile.getDotLogger() != null) {
                                dotLogFile.getDotLogger().update(dotData);
                            }
                        } else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {
                            userInterface.updateIMUDataOutput(nameOfIMU, "DONE");
                        }
                    }
                    break;
            }

            //Increase the sample counter by 1
            sampleCounter++;

        }

    }


    @Override
    public void onDotHeadingChanged(String address, int status, int result) {

        // ✅ ADD THIS LINE FIRST - before the existing log
        Log.d("DATA_FLOW", nameOfIMU + " DATA packet received. trialName=" + trialName + ", sample=" + sampleCounter);

        // Your existing log (keep this too)
        if (sampleCounter % 60 == 0) {
            fileManager.writeToLogFile(nameOfIMU + " onDotDataChanged: trialName=" + trialName + ", sampleCounter=" + sampleCounter + ", targetSample=" + (outputFrequency * offsetInitializationDurationSec));
        }

        //Check to see if heading is reverting (indicating the heading just reverted)
        if(isHeadingReverting){

            //Indicate that heading is not reverting
            isHeadingReverting = false;
            Log.d(TAG, "onDotHeadingChanged: heading reverted");

            //Now that the heading is reverted, we can reset the heading
            performHeadingReset();

        } else {
            if (movellaDotDevice != null) {
                if (trialName.equals("HeadingReset")) {
                    if (movellaDotDevice.stopMeasuring()) {
                        fileManager.writeToLogFile(nameOfIMU + " Measurement stopped after heading reset procedure.");
                    }
                }

                if (status == DotDevice.HEADING_STATUS_XRM_HEADING && result == DotDevice.HEADING_SUCCESS) {
                    isHeadingReset = true;
                    fileManager.writeToLogFile(nameOfIMU + " Heading Reset Successful.");
                    userInterface.updateIMUStatus(nameOfIMU, "Ready");
                    imuManager.onHeadingResetComplete();
                } else {
                    isHeadingReset = false;
                    fileManager.writeToLogFile(nameOfIMU + " Heading Reset Failed. Status: " + status + ", Result: " + result);
                    userInterface.errorMessagePopUp(nameOfIMU + " Heading Reset Failed.");
                }

            }
        }
    }

    @Override
    public void onDotRotLocalRead(String s, float[] floats) {

    }

    private void stopOffsetInitializationWithoutManagerCall(){
        if(movellaDotDevice != null){
            try{
                if(movellaDotDevice.stopMeasuring()){
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Stopped");
                }
                else{
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e){
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "stopOffsetInitialization", e);
            }
        }

        // ✅ DON'T call manager here - let polling handle it
        // imuManager.onAngleOffsetInitializationComplete();
    }
}
