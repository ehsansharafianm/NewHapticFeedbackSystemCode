package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManagerWithRecordingIMUs;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Trial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithIMU;
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
    
    IMUManagerWithRecordingIMUs imuManagerWithRecordingIMUs;

    private DotRecordingManager movellaDotRecordingManager;
    private final byte[] mSelectedExportedDataIds;

    private int exportedFilesCounter, exportedPacketsCounter, totalPacketsToExport;

    private final ArrayList<String> trialNames;
    private final ArrayList<String> trialTimeStamps;
    private final ArrayList<FileManager.DotLogFile> dotLogFiles;

    private boolean isErased;
    private boolean isExporting;

    public RecordingIMU(String nameOfIMU, Context context, IMUManagerWithRecordingIMUs imuManagerWithRecordingIMUs, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode) {
        
        super(nameOfIMU, context, imuManagerWithRecordingIMUs, userInterface, fileManager, measurementMode);
        
        this.imuManagerWithRecordingIMUs = imuManagerWithRecordingIMUs;

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
            imuStatus = "Connected";
            fileManager.writeToLogFile(nameOfIMU + " is Connected");
        } else {
            if (!(imuManager.getIsScanning() || imuManager.getIsSyncing())) {
                isScanned = false;
                isConnected = false;
                isReady = false;
                isErased = false;
                userInterface.updateIMUStatus(nameOfIMU, "Disconnected");
                imuStatus = "Disconnected";
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

            //Check to see if the output frequency is set to desired frequency
            if(movellaDotDevice.getCurrentOutputRate() != outputFrequency){

                //Attempt to set the output rate
                if (!movellaDotDevice.setOutputRate(outputFrequency)) {
                    fileManager.writeToLogFile(nameOfIMU + " Error Setting Output Frequency");
                    userInterface.errorMessagePopUp("Output Frequency Error");
                }

            }

            //Check to see if the filter profile is set to General
            if(movellaDotDevice.getCurrentFilterProfileIndex() != 0){

                //Attempt to set the filter profile
                if (!movellaDotDevice.setFilterProfile(0)) {
                    fileManager.writeToLogFile(nameOfIMU + " Error Setting Filter Profile");
                    userInterface.errorMessagePopUp("Filter Profile Error");
                }
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
            imuStatus = "Ready";
            fileManager.writeToLogFile(nameOfIMU + " is Ready");
        }
    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

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
            imuStatus = "Ready";
            imuManager.onInitializationComplete();
        }
        else{
            fileManager.writeToLogFile(nameOfIMU + " Error Erasing IMU Recording Memory");
            userInterface.errorMessagePopUp("Memory Erase Error");
        }
    }

    @Override
    public void onDotRequestFlashInfoDone(String address, int usedFlashSpace, int totalFlashSpace) {
        fileManager.writeToLogFile(nameOfIMU + " Flash Information Received");
        fileManager.writeToLogFile(nameOfIMU + " Used Flash: " + usedFlashSpace);
        fileManager.writeToLogFile(nameOfIMU + " Total Flash: " + totalFlashSpace);

        double percent = (double) usedFlashSpace/totalFlashSpace;

        //Pause for 2 seconds
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }

        if(imuManager.getIsScanning()){
            if ((percent>0.001) || (Double.isNaN(percent)) || (totalFlashSpace == 0)){
                fileManager.writeToLogFile(nameOfIMU + " Erasing IMU Recording Memory");
                userInterface.updateIMUStatus(nameOfIMU, "Erasing");
                imuStatus = "Erasing";
                if(!movellaDotRecordingManager.eraseRecordingData()){
                    fileManager.writeToLogFile(nameOfIMU + " !Error Erasing IMU Recording Memory");
                    userInterface.errorMessagePopUp("!Memory Erase Error");
                }
            }
            else{
                fileManager.writeToLogFile(nameOfIMU + " No Need To Erase (%" + (percent*100) + ")");
                userInterface.updateIMUStatus(nameOfIMU, "Ready");
                imuStatus = "Ready";
                isErased = true;
                imuManager.onInitializationComplete();
            }
        }
        else if(isExporting){

            int availableRecordingTime;

            if(movellaDotDevice.isProductV2()){
                availableRecordingTime = 365; //V2 Devices Have about 365 minutes of recording time at 60Hz
            }
            else{
                availableRecordingTime = 90; //V1 Devices Have about 90 minutes of recording time at 60Hz
            }

            totalPacketsToExport = (int)(percent * availableRecordingTime * outputFrequency * 60);
            fileManager.writeToLogFile(nameOfIMU + " Packets to Export: " + totalPacketsToExport);

            fileManager.writeToLogFile(nameOfIMU + " Requesting File Info");
            imuManagerWithRecordingIMUs.updateExportLoadingPage(0, "Requesting File Info");
            movellaDotRecordingManager.requestFileInfo();
        }
    }

    @Override
    public void onDotRecordingAck(String address, int recordingId, boolean isSuccess, DotRecordingState recordingState) {
        if(recordingId == DotRecordingManager.RECORDING_ID_START_RECORDING){
            fileManager.writeToLogFile(nameOfIMU + " Recording Started");
        }
        else if(recordingId == DotRecordingManager.RECORDING_ID_STOP_RECORDING){
            fileManager.writeToLogFile(nameOfIMU + " Recording Stopped");
        }
    }

    @Override
    public void onDotGetRecordingTime(String s, int i, int i1, int i2) {

    }

    @Override
    public void onDotRequestFileInfoDone(String address, ArrayList<DotRecordingFileInfo> recordingList, boolean isSuccess) {
        if(recordingList.size() != trialNames.size()){

            //Pause for 1 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            fileManager.writeToLogFile("Requesting " + nameOfIMU + " recording files again. Only received " +
                    recordingList.size() + " out of " + trialNames.size() + " files");

            //Request the file info again (hopefully it will update to the correct number of files)
            movellaDotRecordingManager.requestFileInfo();

        }
        else{
            mSelectedExportedDataIds[0] = DotRecordingManager.RECORDING_DATA_ID_TIMESTAMP;
            mSelectedExportedDataIds[1] = DotRecordingManager.RECORDING_DATA_ID_ORIENTATION;
            mSelectedExportedDataIds[2] = DotRecordingManager.RECORDING_DATA_ID_CALIBRATED_ACC;
            mSelectedExportedDataIds[3] = DotRecordingManager.RECORDING_DATA_ID_CALIBRATED_GYR;

            //Pause for 1 seconds
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            if(movellaDotRecordingManager.selectExportedData(mSelectedExportedDataIds)){
                fileManager.writeToLogFile(nameOfIMU + " Data Values Selected For Export");

                //Pause for 1 second
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
                }

                if(movellaDotRecordingManager.startExporting(recordingList)){
                    fileManager.writeToLogFile(nameOfIMU + " Files Selected For Export");
                    imuManagerWithRecordingIMUs.updateExportLoadingPage(0, "Files Selected For Export");
                }
                else{
                    fileManager.writeToLogFile(nameOfIMU + " !Error Selecting Files For Export");
                    userInterface.errorMessagePopUp("!File Selection Error");
                }
            }
            else{
                fileManager.writeToLogFile(nameOfIMU + " !Error Selecting Data Values For Export");
                userInterface.errorMessagePopUp("!Data Selection Error");
            }


        }

    }

    @Override
    public void onDotDataExported(String address, DotRecordingFileInfo dotRecordingFileInfo, DotData dotData) {

        if(dotLogFiles.size() == exportedFilesCounter){
            dotLogFiles.add(createDataLog(trialNames.get(exportedFilesCounter), trialTimeStamps.get(exportedFilesCounter)));

            //Pause for 1 second
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }
        }

        dotLogFiles.get(exportedFilesCounter).getDotLogger().update(dotData);

        exportedPacketsCounter++;
        if(exportedPacketsCounter%(totalPacketsToExport/100) == 0){
            imuManagerWithRecordingIMUs.updateExportLoadingPage((exportedPacketsCounter/(totalPacketsToExport/100)), 
                    "Recording Files Exported:\t\t\t" + exportedFilesCounter + "/" + trialNames.size());
            
        }

    }

    @Override
    public void onDotDataExported(String s, DotRecordingFileInfo dotRecordingFileInfo) {
        exportedFilesCounter++;
        fileManager.writeToLogFile(nameOfIMU + " A File Was Exported");

        //Pause for 2 seconds
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }
    }

    @Override
    public void onDotAllDataExported(String s) {
        fileManager.writeToLogFile(nameOfIMU + " All Files Exported");
        imuManagerWithRecordingIMUs.onRecordingExportComplete(nameOfIMU);
    }

    @Override
    public void onDotStopExportingData(String s) {

    }
}