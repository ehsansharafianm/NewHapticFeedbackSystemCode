package com.umainebiomechanicslab.biomechanicslabapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.xsens.dot.android.sdk.interfaces.DotScannerCallback;
import com.xsens.dot.android.sdk.interfaces.DotSyncCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.DotSyncManager;
import com.xsens.dot.android.sdk.utils.DotScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public abstract class IMUManager implements DotScannerCallback, DotSyncCallback {

    //Declare the TAG for Error Logging
    private final String TAG = "IMUManager";

    //Declare the context
    protected final Context context;

    //Declare the File Manager Object
    protected final FileManager fileManager;

    //Declare the User Interface Object
    protected final UserInterfaceWithIMU userInterface;

    //Declare the Dot Scanner Object
    protected final DotScanner movellaDotScanner;

    //Declare the Dot Device List
    protected final ArrayList<DotDevice> movellaDeviceList;

    //Declare the IMU Object List (abstract)
    protected final ArrayList<UniversalIMU> IMUArrayList;

    //Declare the Scan Status and Sync Status booleans
    protected boolean isScanning, isSyncing;

    public IMUManager(Context context, UserInterfaceWithIMU userInterface, FileManager fileManager){

        //Initialize the context
        this.context = context;

        //Initialize the File Manager Object
        this.fileManager = fileManager;

        //Initialize the User Interface Object
        this.userInterface = userInterface;

        //Initialize the Dot Scanner Object
        movellaDotScanner = new DotScanner(context.getApplicationContext(), this);
        movellaDotScanner.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

        //Initialize the IMU Object List but don't fill it (this will be done in sub classes)
        IMUArrayList = new ArrayList<>();

        //Initialize the Dot Device List
        movellaDeviceList = new ArrayList<>();

        //Initialize the Scan Status and Sync Status booleans
        isScanning = false;
        isSyncing = false;

    }

    public abstract void startAngleOffsetInitialization();

    public abstract void onAngleOffsetInitializationComplete();

    public abstract void startTrial(String trialName);

    public abstract void stopTrial();

    public boolean getScanStatus(String nameOfIMU){

        //Loop through all the IMUs to find the one that matches the nameOfIMU
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU.getNameOfIMU().equals(nameOfIMU)){

                //Return the scan status of the IMU
                return IMU.getIsScanned();
            }
        }

        //If none of the IMUs match the nameOfIMU, return false
        return false;
    }

    public boolean getIsScanning(){
        return isScanning;
    }

    public boolean getIsSyncing(){
        return isSyncing;
    }

    public void setIMUMACAddress(String nameOfIMU, String IMUCode){

        //Declare macAddress variable
        String macAddress;

        //Set the macAddress variable based on the code on the physical IMU
        switch (IMUCode){
            case "V2–01":
                macAddress = "D4:22:CD:00:9F:A0";
                break;
            case "V2–02":
                macAddress = "D4:22:CD:00:A2:12";
                break;
            case "V2–03":
                macAddress = "D4:22:CD:00:A2:0E";
                break;
            case "V2–04":
                macAddress = "D4:22:CD:00:A0:50";
                break;
            case "V2–05":
                macAddress = "D4:22:CD:00:9F:C3";
                break;
            case "V2–LA":
                macAddress = "D4:22:CD:00:63:8A";
                break;
            case "V2–RA":
                macAddress = "D4:22:CD:00:63:83";
                break;
            case "V2–A2":
                macAddress = "D4:22:CD:00:63:71";
                break;
            case "V2–LT":
                macAddress = "D4:22:CD:00:63:7F";
                break;
            case "V2–RT":
                macAddress = "D4:22:CD:00:63:70";
                break;
            case "V2–LS":
                macAddress = "D4:22:CD:00:A1:5B";
                break;
            case "V2–RS":
                macAddress = "D4:22:CD:00:A7:91";
                break;
            case "V2–LF":
                macAddress = "D4:22:CD:00:64:00";
                break;
            case "V2–RF":
                macAddress = "D4:22:CD:00:63:7D";
                break;
            case "V2–X1":
                macAddress = "D4:22:CD:00:63:D6";
                break;
            case "V2–X2":
                macAddress = "D4:22:CD:00:9F:88";
                break;
            case "ES–LT":
                macAddress = "D4:22:CD:00:63:8B";
                break;
            case "ES–RT":
                macAddress = "D4:22:CD:00:A1:76";
                break;
            case "ES–LF":
                macAddress = "D4:22:CD:00:63:A4";
                break;
            case "ES–RF":
                macAddress = "D4:22:CD:00:9F:95";
                break;
            case "V1–LA":
                macAddress = "D4:CA:6E:F1:72:BF";
                break;
            case "V1–RA":
                macAddress = "D4:CA:6E:F1:72:76";
                break;
            case "V1–A2":
                macAddress = "D4:CA:6E:F1:78:4E";
                break;
            case "V1–LT":
                macAddress = "D4:CA:6E:F1:84:BE";
                break;
            case "V1–RT":
                macAddress = "D4:CA:6E:F1:66:87";
                break;
            case "V1–LS":
                macAddress = "D4:22:CD:00:05:BB";
                break;
            case "V1–RS":
                macAddress = "D4:22:CD:00:05:BA";
                break;
            case "V1–LF":
                macAddress = "D4:22:CD:00:05:C5";
                break;
            case "V1–RF":
                macAddress = "D4:CA:6E:F1:7D:D4";
                break;
            case "V1–X1":
                macAddress = "D4:CA:6E:F1:77:9B";
                break;
            default:
                userInterface.textPopUp("Non-Existent IMU Code");
                return;
        }

        //Set the macAddress of the IMU
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU.getNameOfIMU().equals(nameOfIMU)){
                IMU.setMacAddress(macAddress);
            }
        }
    }

    public void startScan(){

        //Start the scan (if there is a problem with the start scan, the method will return false
        if(movellaDotScanner.startScan()){
            fileManager.writeToLogFile("Scan started.");
            userInterface.textPopUp("Scan Started");
            isScanning = true;
        }
        else{
            userInterface.errorMessagePopUp("Scanning Error");

            //Call onScanComplete with a false isSuccess value indicating unsuccessful scan
            userInterface.onScanComplete(false);
        }

    }

    public void onInitializationComplete(){

        //Only run this code if the IMUManager was scanning (not syncing)
        if(isScanning){

            //Exit the method if one of the IMUs isn't "Ready"
            //(Initialization is only complete if they are all "Ready"
            for(UniversalIMU IMU : IMUArrayList){
                if(!IMU.getIsReady()){
                    fileManager.writeToLogFile("Initialization not complete.");
                    return;
                }
            }

            //Stop the scan (if there is a problem with the stop scan, the method will return false)
            if(movellaDotScanner.stopScan()){

                //Log that scan was completed
                fileManager.writeToLogFile("Scan complete.");

                //Log number of connected devices
                fileManager.writeToLogFile("Number of IMUs Connected: " + movellaDeviceList.size());

                //If the number of DotDevices that were connected doesn't match the expected number of IMUs, log an error
                if(movellaDeviceList.size() != IMUArrayList.size()){
                    fileManager.writeToLogFile("Error: Incorrect number of IMUs connected");
                    userInterface.errorMessagePopUp("Error: Incorrect number of IMUs connected");
                }

                //Set is scanning to false
                isScanning = false;

                //Call onScanComplete with a true isSuccess value indicating successful scan
                userInterface.onScanComplete(true);

            }
            else{
                userInterface.errorMessagePopUp("Error Stopping Scan");
            }
        }
    }

    public void startSync(){

        //Update IMU status of all IMUs to "Syncing"
        for(UniversalIMU IMU : IMUArrayList){
            userInterface.updateIMUStatus(IMU.getNameOfIMU(), "Syncing");
        }

        //Stop Any Previous Syncing Before Starting
        DotSyncManager.getInstance(this).stopSyncing();

        //Select 1 IMU to Be The Root Device
        movellaDeviceList.get(0).setRootDevice(true);

        //Start Syncing the IMUs
        isSyncing = true;
        DotSyncManager.getInstance(this).startSyncing(movellaDeviceList, 1);

    }

    public void disconnectIMUs(){

        //Loop through all the IMUs and disconnect each one
        for(UniversalIMU IMU : IMUArrayList){
            IMU.disconnectMovellaDotDevice();
        }
    }

    public void onDotDisconnected(DotDevice disconnectedDotDevice){

        if((movellaDeviceList != null) && (disconnectedDotDevice != null) && (movellaDeviceList.contains(disconnectedDotDevice))){
            movellaDeviceList.remove(disconnectedDotDevice);
            fileManager.writeToLogFile(disconnectedDotDevice.getTag() + " was disconnected");
            fileManager.writeToLogFile("Connected Devices: " + movellaDeviceList.size());
        }

    }

    @Override
    public void onDotScanned(BluetoothDevice bluetoothDevice, int i) {

        //Get the address of the scanned IMU
        String macAddress = bluetoothDevice.getAddress();

        //If all IMUs in the IMU Array List have been scanned, skip this method
        if(movellaDeviceList.size() == IMUArrayList.size()){
            return;
        }

        //Pause for 0.5 seconds to allow the IMU to connect
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }

        //Loop through all the IMUs to find the one that matches the macAddress
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU.getMacAddress().equals(macAddress)){
                movellaDeviceList.add(IMU.connectMovellaDotDevice(bluetoothDevice));
            }
        }

    }

    @Override
    public void onSyncingStarted(String address, boolean b, int i) {

        //Update IMU status of the IMU with the given address to "Syncing"
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU.getMacAddress().equals(address)){
                userInterface.updateIMUStatus(IMU.getNameOfIMU(), "Syncing");
            }
        }

    }

    @Override
    public void onSyncingProgress(int i, int i1) {

    }

    @Override
    public void onSyncingResult(String address, boolean isSuccess, int i) {

        //Find the name of the IMU with the given address
        for(UniversalIMU IMU : IMUArrayList) {
            if (IMU.getMacAddress().equals(address)) {

                //Check to see if IMU syncing was a success
                if (isSuccess) {
                    //Log that the IMU with the name matching the address was synced
                    fileManager.writeToLogFile(IMU.getNameOfIMU() + " was synced");

                    //Set the measurement mode of the IMU
                    IMU.setMeasurementMode();

                } else {
                    fileManager.writeToLogFile(IMU.getNameOfIMU() + " was not synced");
                }

            }
        }
    }

    @Override
    public void onSyncingDone(HashMap<String, Boolean> hashMap, boolean isSuccess, int i) {

        isSyncing = false;

        userInterface.onSyncComplete(isSuccess);
        if(isSuccess){
            fileManager.writeToLogFile("Sync Successful");
            userInterface.textPopUp("Sync Successful");
        }
        else{
            fileManager.writeToLogFile("Sync Unsuccessful");
            userInterface.errorMessagePopUp("Sync Unsuccessful");
        }

    }

    @Override
    public void onSyncingStopped(String s, boolean b, int i) {

    }
}
