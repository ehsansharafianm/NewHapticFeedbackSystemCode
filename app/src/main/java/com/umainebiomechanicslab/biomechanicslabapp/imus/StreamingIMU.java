package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Trial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithIMU;
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

    protected FileManager.DotLogFile dotLogFile;
    protected boolean offsetAnglesInitialized;
    protected boolean isHeadingReset;
    protected int offsetInitializationDurationSec;
    protected double offsetEulerAngle;

    public StreamingIMU(String nameOfIMU, Context context, IMUManager imuManager, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode){

        super(nameOfIMU, context, imuManager, userInterface, fileManager, measurementMode);

        //Offset Angles are not Initialized By Default
        this.offsetAnglesInitialized = false;

        //Heading is not reset by default
        this.isHeadingReset = false;

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
        trialName = "HeadingReset";
        isHeadingReset = false; // Reset flag for the new procedure

        //Set the measurement callback
        movellaDotDevice.setDotMeasurementCallback(this);

        try {
            // A revert is required before a new heading reset can be performed.
            if (movellaDotDevice != null) {
                fileManager.writeToLogFile(nameOfIMU + " reverting heading");
                movellaDotDevice.revertHeading();
            }

            if (movellaDotDevice != null && movellaDotDevice.startMeasuring()) {
                fileManager.writeToLogFile(nameOfIMU + " Measurement Started for Heading Reset.");
                movellaDotDevice.resetHeading();
                userInterface.updateIMUStatus(nameOfIMU, "Resetting Heading...");
                fileManager.writeToLogFile(nameOfIMU + " resetting heading");
            } else {
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected or failed to start measurement for reset.");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        } catch (NullPointerException e) {
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "HeadingReset", e);
        }
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

        try{
            if(movellaDotDevice != null && movellaDotDevice.startMeasuring()) {
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
            Log.e(TAG, "startOffsetInitialization", e);
        }

    }

    public void stopOffsetInitialization(){

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
                Log.e(TAG, "startOffsetInitialization", e);
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

    /*@Override
    public void onDotInitDone(String address) {
        // First, execute all the logic from the parent UniversalIMU class.
        super.onDotInitDone(address);

        //Set the measurement callback
        movellaDotDevice.setDotMeasurementCallback(this);

        // This check is important because the parent class calls onInitializationComplete(),
        // which might trigger other logic. We only want to start the reset if scanning.
        if (imuManager.getIsScanning()) {
            trialName = "HeadingReset";
            isHeadingReset = false; // Reset flag for the new procedure

            try {
                // A revert is required before a new heading reset can be performed.
                if (movellaDotDevice != null) {
                    fileManager.writeToLogFile(nameOfIMU + " reverting heading");
                    movellaDotDevice.revertHeading();
                }

                if (movellaDotDevice != null && movellaDotDevice.startMeasuring()) {
                    fileManager.writeToLogFile(nameOfIMU + " Measurement Started for Heading Reset.");
                    movellaDotDevice.resetHeading();
                    userInterface.updateIMUStatus(nameOfIMU, "Resetting Heading...");
                    fileManager.writeToLogFile(nameOfIMU + " resetting heading");
                } else {
                    fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected or failed to start measurement for reset.");
                    userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                }
            } catch (NullPointerException e) {
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
                Log.e(TAG, "HeadingReset", e);
            }
        }
    }*/

    @Override
    public void onDotDataChanged(String address, DotData dotData) {

        if(trialName == null){
            return;
        }

        double eulerAngleX = dotData.getEuler()[0];

        switch(trialName){
            case "HeadingReset":
                //Log that heading is being reset
                Log.d(TAG, nameOfIMU + " Heading Reset");

                // Do nothing here. We are just waiting for the onDotHeadingChanged callback.
                break;
            case "Initialization":
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
                break;
            default: // Handles all other trials
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


    @Override
    public void onDotHeadingChanged(String address, int status, int result) {
        fileManager.writeToLogFile(nameOfIMU + " Heading Reset Successful.");
        if (movellaDotDevice != null && address.equals(movellaDotDevice.getAddress())) {
            if (status == DotDevice.HEADING_STATUS_XRM_HEADING && result == DotDevice.HEADING_SUCCESS) {
                isHeadingReset = true;
                fileManager.writeToLogFile(nameOfIMU + " Heading Reset Successful.");
                userInterface.updateIMUStatus(nameOfIMU, "Ready");
                imuManager.onHeadingResetComplete();

                // If this callback was for our automatic reset trial, stop the measurement.
                if ("HeadingReset".equals(trialName)) {
                    if(movellaDotDevice.stopMeasuring()){
                        fileManager.writeToLogFile(nameOfIMU + " Measurement stopped after heading reset.");
                    }
                }

            } else {
                isHeadingReset = false;
                fileManager.writeToLogFile(nameOfIMU + " Heading Reset Failed. Status: " + status + ", Result: " + result);
                userInterface.errorMessagePopUp(nameOfIMU + " Heading Reset Failed.");

                // If this callback was for our automatic reset trial, stop the measurement.
                if ("HeadingReset".equals(trialName)) {
                    if(movellaDotDevice.stopMeasuring()){
                        fileManager.writeToLogFile(nameOfIMU + " Measurement stopped after failed heading reset.");
                    }
                }
            }
        }


    }

    @Override
    public void onDotRotLocalRead(String s, float[] floats) {

    }
}
