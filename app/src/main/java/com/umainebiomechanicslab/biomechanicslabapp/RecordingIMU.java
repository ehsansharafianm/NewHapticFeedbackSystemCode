package com.umainebiomechanicslab.biomechanicslabapp;

import android.content.Context;
import android.util.Log;

import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.interfaces.DotRecordingCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.DotRecordingFileInfo;
import com.xsens.dot.android.sdk.models.DotRecordingState;
import com.xsens.dot.android.sdk.recording.DotRecordingManager;
import com.xsens.dot.android.sdk.utils.DotLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RecordingIMU extends UniversalIMU implements DotRecordingCallback {

    private final String TAG = "RecordingIMU";

    private DotRecordingManager movellaDotRecordingManager;
    private final byte[] mSelectedExportedDataIds;

    private int exportedFilesCounter, exportedPacketsCounter, totalPacketsToExport;

    private final ArrayList<String> trialNames;
    private final ArrayList<String> trialTimeStamps;
    private final ArrayList<FileManager.DotLogFile> dotLogFiles;

    private boolean isErased;
    private boolean isExporting;

    public RecordingIMU(String nameOfIMU, Context context, IMUManager imuManager, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode) {
        super(nameOfIMU, context, imuManager, userInterface, fileManager, measurementMode);

        //Initialize the mSelectedExportedDataIds based on the measurementMode
        mSelectedExportedDataIds = new byte[4];

        //Initialize the ArrayLists
        trialNames = new ArrayList<>();
        trialTimeStamps = new ArrayList<>();
        dotLogFiles = new ArrayList<>();

        //Set isErased and isExporting to false to Start
        isErased = false;
        isExporting = false;

        //Set exportedFilesCounter to 0 to Start
        exportedFilesCounter = 0;

        //Set exportedPacketsCounter to 0 to Start
        exportedPacketsCounter = 0;

    }

    @Override
    public boolean getIsReady(){

        //For recordingIMUs to be ready, they must be ready and erased
        return isReady && isErased;
    }

    @Override
    public void onDotConnectionChanged(String address, int state) {

        if (state == DotDevice.CONN_STATE_CONNECTED) {
            isScanned = true;
            isConnected = true;
            userInterface.updateIMUStatus(nameOfIMU, "Connected");
            fileManager.writeToLogFile(nameOfIMU + " is Connected");
        } else {
            if (!(imuManager.getIsScanning() || imuManager.getIsSyncing())) {
                isScanned = false;
                isConnected = false;
                isReady = false;
                isErased = false;
                userInterface.updateIMUStatus(nameOfIMU, "Disconnected");
                fileManager.writeToLogFile(nameOfIMU + " is Disconnected");
                userInterface.errorMessagePopUp(nameOfIMU + " is Disconnected");
                imuManager.onDotDisconnected(movellaDotDevice);
            }
        }

    }

    @Override
    public void onDotInitDone(String address) {
        isReady = true;
        if (imuManager.getIsScanning()) {
            if (!movellaDotDevice.setOutputRate(outputFrequency)) {
                fileManager.writeToLogFile(nameOfIMU + " Error Setting Output Frequency");
                userInterface.errorMessagePopUp("Output Frequency Error");
            }

            //Pause for 2 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            if (!movellaDotDevice.setFilterProfile(0)) {
                fileManager.writeToLogFile(nameOfIMU + " Error Setting Filter Profile");
                userInterface.errorMessagePopUp("Filter Profile Error");
            }

            //Pause for 2 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            movellaDotRecordingManager = new DotRecordingManager(context.getApplicationContext(), movellaDotDevice, this);

            //Pause for 2 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            if (!movellaDotRecordingManager.enableDataRecordingNotification()) {
                fileManager.writeToLogFile(nameOfIMU + " Error Enabling Recording Notification");
                userInterface.errorMessagePopUp("Recording Notification Error");
            }
        }
        else{
            userInterface.updateIMUStatus(nameOfIMU, "Ready");
            fileManager.writeToLogFile(nameOfIMU + " is Ready");
        }
    }

    @Override
    public void startTrial(String trialName, String timeStamp, int trialDurationMin, boolean logData) {

        trialNames.add(trialName);
        trialTimeStamps.add(timeStamp);

        try{
            if(movellaDotRecordingManager.startRecording()) {
                fileManager.writeToLogFile(nameOfIMU + " Starting Recording");
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

        try{
            if(movellaDotRecordingManager.stopRecording()) {
                fileManager.writeToLogFile(nameOfIMU + " Stopping Recording");
            }
            else{
                fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
                userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            }
        }
        catch (NullPointerException e){
            fileManager.writeToLogFile("Error: " + nameOfIMU + " Not Connected");
            userInterface.errorMessagePopUp("Error: " + nameOfIMU + " Not Connected");
            Log.e(TAG, "stopTrial", e);
        }

        //RecordingIMUs don't return the DotLogFile in the stopTrial Method
        return null;
    }

    @Override
    protected FileManager.DotLogFile createDataLog(String trialName, String timeStamp) {

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

            DotLogger logger = DotLogger.createRecordingsLogger(context.getApplicationContext(), mSelectedExportedDataIds, dotLogFilePath, movellaDotDevice.getTag(), movellaDotDevice.getFirmwareVersion(), "25", 0);

            return new FileManager.DotLogFile(dotLogFileName, dotLogFile, logger);
        }
        catch(NullPointerException e){
            fileManager.writeToLogFile("Error: Sensor Not Found");
            userInterface.errorMessagePopUp("Error: Sensor not found");
            Log.e(TAG, "createDataLog", e);
            return null;
        }

    }

    public void startRecordingExport(){
        isExporting = true;
        exportedFilesCounter = 0;
        exportedPacketsCounter = 0;
        dotLogFiles.clear();

        //Pause for 2 seconds
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }

        if (!movellaDotRecordingManager.enableDataRecordingNotification()) {
            fileManager.writeToLogFile(nameOfIMU + " Error Enabling Recording Notification");
            userInterface.errorMessagePopUp("Recording Notification Error");
        }

    }

    @Override
    public void onDotRecordingNotification(String address, boolean isEnabled) {
        if(isEnabled){
            fileManager.writeToLogFile(nameOfIMU + " Data Recording Notification Enabled");

            //Pause for 2 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            if(!movellaDotRecordingManager.requestFlashInfo()){
                fileManager.writeToLogFile(nameOfIMU + " !Error Getting Flash Information");
                userInterface.errorMessagePopUp("!Flash Info Error");
            }
        }
        else{
            fileManager.writeToLogFile(nameOfIMU + " Error Enabling Recording Notification");
            userInterface.errorMessagePopUp("Recording Notification Error");
        }
    }

    @Override
    public void onDotEraseDone(String address, boolean isSuccess) {
        if(isSuccess){
            fileManager.writeToLogFile(nameOfIMU + " Data Erase Successful");
            isErased = true;
            userInterface.updateIMUStatus(nameOfIMU, "Ready");
            imuManager.onInitializationComplete();
        }
        else{
            fileManager.writeToLogFile(nameOfIMU + " Error Erasing IMU Recording Memory");
            userInterface.errorMessagePopUp("Memory Erase Error");
        }
    }

    @Override
    public void onDotRequestFlashInfoDone(String s, int i, int i1) {

    }

    @Override
    public void onDotRecordingAck(String s, int i, boolean b, DotRecordingState dotRecordingState) {

    }

    @Override
    public void onDotGetRecordingTime(String s, int i, int i1, int i2) {

    }

    @Override
    public void onDotRequestFileInfoDone(String s, ArrayList<DotRecordingFileInfo> arrayList, boolean b) {

    }

    @Override
    public void onDotDataExported(String s, DotRecordingFileInfo dotRecordingFileInfo, DotData dotData) {

    }

    @Override
    public void onDotDataExported(String s, DotRecordingFileInfo dotRecordingFileInfo) {

    }

    @Override
    public void onDotAllDataExported(String s) {

    }

    @Override
    public void onDotStopExportingData(String s) {

    }
}