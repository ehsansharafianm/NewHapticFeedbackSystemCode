package com.umainebiomechanicslab.biomechanicslabapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.xsens.dot.android.sdk.events.DotData;
import com.xsens.dot.android.sdk.interfaces.DotDeviceCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.FilterProfileInfo;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public abstract class UniversalIMU implements DotDeviceCallback {

    private final String TAG = "UniversalIMU";

    //Declare variable for storing mac address of IMU
    protected String macAddress;

    //Declare dotDevice object for this IMU
    protected DotDevice movellaDotDevice;

    //Declare variable for storing measurement mode
    protected int measurementMode;

    //Declare variable for storing name of IMU
    protected String nameOfIMU;

    //Declare context object
    protected Context context;

    //Declare IMU Trial Manager
    protected IMUManager imuManager;

    //Declare User Interface
    protected UserInterfaceWithIMU userInterface;

    //Declare FileManager Object
    protected FileManager fileManager;

    //Declare IMU status booleans
    protected boolean isScanned, isConnected, isReady;

    //Declare outputFrequency variable
    protected int outputFrequency;

    public UniversalIMU(String nameOfIMU, Context context, IMUManager imuManager, UserInterfaceWithIMU userInterface, FileManager fileManager, int measurementMode) {

        //Set Mac Address To 0.0.0.0 By Default
        macAddress = "0.0.0.0";

        //Link measurementMode to measurementMode
        this.measurementMode = measurementMode;

        //Link nameOfIMU to nameOfIMU
        this.nameOfIMU = nameOfIMU;

        //Link context to context
        this.context = context;

        //Link imuManager to imuManager
        this.imuManager = imuManager;

        //Link userInterface to userInterface
        this.userInterface = userInterface;

        //Link fileManager to fileManager
        this.fileManager = fileManager;

        //Set IMU Statuses To False By Default
        this.isScanned = false;
        this.isConnected = false;
        this.isReady = false;

        //Assign standard output frequency for all IMUs
        this.outputFrequency = 60;

    }

    public void setMacAddress(String macAddress) {
        fileManager.writeToLogFile(nameOfIMU + " Mac Address set to " + macAddress);
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getNameOfIMU() {
        return nameOfIMU;
    }

    public boolean getIsScanned() {
        return isScanned;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public int getBatteryPercentage() {
        return movellaDotDevice.getBatteryPercentage();
    }

    public DotDevice connectMovellaDotDevice(BluetoothDevice bluetoothDevice) {
        movellaDotDevice = new DotDevice(context.getApplicationContext(), bluetoothDevice, UniversalIMU.this);
        isScanned = true;
        userInterface.updateIMUStatus(nameOfIMU, "Scanned");
        fileManager.writeToLogFile(nameOfIMU + " is Scanned");
        movellaDotDevice.connect();
        return movellaDotDevice;
    }

    public void disconnectMovellaDotDevice() {
        movellaDotDevice.disconnect();
    }

    public void setMeasurementMode() {
        movellaDotDevice.setMeasurementMode(measurementMode);
    }

    public abstract void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData);

    public abstract FileManager.DotLogFile stopTrial(boolean logData);

    protected abstract FileManager.DotLogFile createDataLog(String trialMode, String timeStamp);

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
                userInterface.updateIMUStatus(nameOfIMU, "Disconnected");
                fileManager.writeToLogFile(nameOfIMU + " is Disconnected");
                userInterface.errorMessagePopUp(nameOfIMU + " is Disconnected");
                imuManager.onDotDisconnected(movellaDotDevice);
            }
        }

    }

    @Override
    public void onDotServicesDiscovered(String address, int i) {

    }

    @Override
    public void onDotFirmwareVersionRead(String s, String s1) {

    }

    @Override
    public void onDotTagChanged(String s, String s1) {

    }

    @Override
    public void onDotBatteryChanged(String s, int i, int i1) {

    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {
        /*
         * This method is deliberately left empty. This method is overridden in the StreamingIMU
         * child class, because only IMUs that stream data have to do anything in response to
         * onDotDataChanged. Recording IMUs simply store the data, thus they have no reason to
         * communicate with the app when a new packet of data is created. However, this method
         * is required when implementing the DotDeviceCallback interface, therefore we must keep
         * this placeholder here.
         * */
    }

    @Override
    public void onDotInitDone(String address) {
        isReady = true;
        if (imuManager.getIsScanning()) {
            if (!movellaDotDevice.setOutputRate(outputFrequency)) {
                fileManager.writeToLogFile(nameOfIMU + " Error Setting Output Frequency");
                userInterface.errorMessagePopUp("Output Frequency Error");
            }

            //Pause for 500 milliseconds between each file upload
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
            }

            if (!movellaDotDevice.setFilterProfile(0)) {
                fileManager.writeToLogFile(nameOfIMU + " Error Setting Filter Profile");
                userInterface.errorMessagePopUp("Filter Profile Error");
            }
        }
        userInterface.updateIMUStatus(nameOfIMU, "Ready");
        fileManager.writeToLogFile(nameOfIMU + " is Ready");
        imuManager.onInitializationComplete();
    }

    @Override
    public void onDotButtonClicked(String s, long l) {

    }

    @Override
    public void onDotPowerSavingTriggered(String s) {
        fileManager.writeToLogFile(nameOfIMU + " has entered Power Saving Mode");
        userInterface.errorMessagePopUp("IMU entered Power Saving");
    }

    @Override
    public void onReadRemoteRssi(String s, int i) {

    }

    @Override
    public void onDotOutputRateUpdate(String address, int outputRate) {
        fileManager.writeToLogFile(nameOfIMU + " Output Rate Set to " + outputRate + "Hz");
    }

    @Override
    public void onDotFilterProfileUpdate(String address, int filterProfileIndex) {
        if (filterProfileIndex == 0) {
            fileManager.writeToLogFile(nameOfIMU + " Filter Profile set to General");
        } else {
            fileManager.writeToLogFile(nameOfIMU + " Filter Profile set to Dynamic");
        }
    }

    @Override
    public void onDotGetFilterProfileInfo(String s, ArrayList<FilterProfileInfo> arrayList) {

    }

    @Override
    public void onSyncStatusUpdate(String address, boolean isSynced) {
        if (isSynced) {
            fileManager.writeToLogFile(nameOfIMU + " is Synced");
        } else {
            fileManager.writeToLogFile(nameOfIMU + " is not Synced");
        }
    }
}