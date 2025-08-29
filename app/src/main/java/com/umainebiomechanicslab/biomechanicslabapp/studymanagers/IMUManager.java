package com.umainebiomechanicslab.biomechanicslabapp.studymanagers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.imus.UniversalIMU;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithIMU;
import com.xsens.dot.android.sdk.interfaces.DotScannerCallback;
import com.xsens.dot.android.sdk.interfaces.DotSyncCallback;
import com.xsens.dot.android.sdk.models.DotDevice;
import com.xsens.dot.android.sdk.models.DotSyncManager;
import com.xsens.dot.android.sdk.utils.DotScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    //Declare the Processing or Connected Addresses Set (containing the addresses of IMUs that are connecting or connected)
    private final Set<String> processingOrConnectedAddresses;

    //Declare the Scan Status and Sync Status booleans
    protected boolean isScanning, isSyncing, isSynced;

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
        this.isScanning = false;
        this.isSyncing = false;
        this.isSynced = false;

        //Initialize the Processing or Connected Addresses Set
        processingOrConnectedAddresses = new HashSet<>();

    }

    public abstract void updateIMUCode(String imuName, String imuCode);

    public abstract void startAngleOffsetInitialization();

    public abstract void onAngleOffsetInitializationComplete();

    public abstract void startTrial(String trialName);

    public abstract void stopTrial(String trialName);

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
            case "V2–06":
                macAddress = "D4:22:CD:00:9F:88";
                break;
            case "V2–07":
                macAddress = "D4:22:CD:00:63:8A";
                break;
            case "V2–08":
                macAddress = "D4:22:CD:00:63:71";
                break;
            case "V2–09":
                macAddress = "D4:22:CD:00:63:83";
                break;
            case "V2–10":
                macAddress = "D4:22:CD:00:63:7F";
                break;
            case "V2–11":
                macAddress = "D4:22:CD:00:63:70";
                break;
            case "V2–12":
                macAddress = "D4:22:CD:00:A1:5B";
                break;
            case "V2–13":
                macAddress = "D4:22:CD:00:A7:91";
                break;
            case "V2–14":
                macAddress = "D4:22:CD:00:64:00";
                break;
            case "V2–15":
                macAddress = "D4:22:CD:00:63:7D";
                break;
            case "V2–16":
                macAddress = "D4:22:CD:00:63:D6";
                break;
            case "V2–17":
                macAddress = "D4:22:CD:00:63:8B";
                break;
            case "V2–18":
                macAddress = "D4:22:CD:00:A1:76";
                break;
            case "V2–19":
                macAddress = "D4:22:CD:00:63:A4";
                break;
            case "V2–20":
                macAddress = "D4:22:CD:00:9F:95";
                break;
            case "V1–01":
                macAddress = "D4:CA:6E:F1:77:9B";
                break;
            case "V1–02":
                macAddress = "D4:CA:6E:F1:7D:D4";
                break;
            case "V1–03":
                macAddress = "D4:22:CD:00:05:C5";
                break;
            case "V1–04":
                macAddress = "D4:22:CD:00:05:BA";
                break;
            case "V1–05":
                macAddress = "D4:22:CD:00:05:BB";
                break;
            case "V1–06":
                macAddress = "D4:CA:6E:F1:66:87";
                break;
            case "V1–07":
                macAddress = "D4:CA:6E:F1:84:BE";
                break;
            case "V1–08":
                macAddress = "D4:CA:6E:F1:78:4E";
                break;
            case "V1–09":
                macAddress = "D4:CA:6E:F1:72:76";
                break;
            case "V1–10":
                macAddress = "D4:CA:6E:F1:72:BF";
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

    public void onHeadingResetComplete() {



        for(UniversalIMU IMU : IMUArrayList) {
            //This only applies to instances of StreamingIMU
            if (IMU instanceof StreamingIMU) {
                Log.d(TAG, "onHeadingResetComplete");

                //Exit the method if one of the IMUs isn't reset
                if (!((StreamingIMU) IMU).getIsHeadingReset()) {
                    Log.d(TAG, "onHeadingResetComplete exit");
                    return;
                }
            }
        }

        fileManager.writeToLogFile("Heading Reset Complete");
        userInterface.onSyncComplete(isSynced);

    }

    public void startSync(){

        //Set is synced to false
        isSynced = false;

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

    public boolean showBatteryPercentage(boolean showBatteryPercentage){

        if(showBatteryPercentage){

            //If all 6 IMUs are ready, show the battery percentage of each one
            for(UniversalIMU IMU : IMUArrayList){
                if(!IMU.getIsReady()){
                    userInterface.errorMessagePopUp("Error: Not all IMUs are connected");

                    //Return false meaning that battery percentage is not shown
                    return false;
                }
            }

            for(UniversalIMU IMU : IMUArrayList){
                userInterface.updateIMUStatus(IMU.getNameOfIMU(), IMU.getBatteryPercentage() + "% Battery");
            }

            //Return true if all 6 IMUs are connected
            return true;

        }

        else{

            //Go back to showing the IMU status of each IMU
            for(UniversalIMU IMU : IMUArrayList){
                userInterface.updateIMUStatus(IMU.getNameOfIMU(), IMU.getIMUStatus());
            }

            //Return false meaning that battery percentage is not shown
            return false;
        }


    }

    public void disconnectIMUs(){

        //Loop through all the IMUs and disconnect each one
        for(UniversalIMU IMU : IMUArrayList){
            IMU.disconnectMovellaDotDevice();
        }
    }

    public void onDotDisconnected(DotDevice disconnectedDotDevice){

        if((movellaDeviceList != null) && (disconnectedDotDevice != null) && (movellaDeviceList.contains(disconnectedDotDevice))){

            //Remove the disconnected DotDevice from the DotDevice List
            movellaDeviceList.remove(disconnectedDotDevice);

            //Remove the disconnected DotDevice from the Connecting or Connected Addresses Set
            if (disconnectedDotDevice.getAddress() != null) {
                processingOrConnectedAddresses.remove(disconnectedDotDevice.getAddress());
            }

            //Log that the disconnected DotDevice was disconnected
            fileManager.writeToLogFile(disconnectedDotDevice.getTag() + " was disconnected");

            //Log the number of connected DotDevices
            fileManager.writeToLogFile("Connected Devices: " + movellaDeviceList.size());
        }

    }

    @Override
    public void onDotScanned(BluetoothDevice bluetoothDevice, int i) {

        //Get the address of the scanned IMU
        String macAddress = bluetoothDevice.getAddress();

        Log.d(TAG, "IMUArrayList Size ");

        //Check to see if the IMU is already processing or connected
        if (movellaDeviceList.size() >= IMUArrayList.size() || processingOrConnectedAddresses.contains(macAddress)) {
            if (processingOrConnectedAddresses.contains(macAddress)) {
                Log.d(TAG, "onDotScanned: Skipping " + macAddress + " as it is already processing or connected.");
            } else {
                Log.d(TAG, "onDotScanned: Skipping scan as all target IMUs (" + IMUArrayList.size() + ") might have been found. Current connected: " + movellaDeviceList.size());
            }
            return;
        }

        //Add macAddress to processingOrConnectedAddresses immediately
        //processingOrConnectedAddresses.add(macAddress);
        //Log.d(TAG, "onDotScanned: Added " + macAddress + " to processingOrConnectedAddresses. Set size: " + processingOrConnectedAddresses.size());

        //Create a null targetIMU (this will be filled in later)
        UniversalIMU targetIMU = null;
        //Loop through all the IMUs to find the one that matches the macAddress
        for(UniversalIMU imu : IMUArrayList){
            // *** MODIFIED: Added null check for imu.getMacAddress() ***
            if(imu.getMacAddress() != null && imu.getMacAddress().equals(macAddress)){
                targetIMU = imu;
                break;
            }
        }

        if (targetIMU != null) {

            processingOrConnectedAddresses.add(macAddress);
            Log.d(TAG, "onDotScanned: Added " + macAddress + " to processingOrConnectedAddresses. Set size: " + processingOrConnectedAddresses.size());

            fileManager.writeToLogFile("Connecting to " + targetIMU.getNameOfIMU() + " (" + macAddress + ")");
            // *** MODIFIED: Capture result of connectMovellaDotDevice ***
            DotDevice connectedDevice = targetIMU.connectMovellaDotDevice(bluetoothDevice);

            // *** ADDED: Logic to handle connection success or failure based on connectedDevice ***
            if (connectedDevice != null) {
                movellaDeviceList.add(connectedDevice);
                fileManager.writeToLogFile(targetIMU.getNameOfIMU() + " connection initiated. Total connected: " + movellaDeviceList.size());
            } else {
                fileManager.writeToLogFile("Failed to initiate connection with " + targetIMU.getNameOfIMU() + " (" + macAddress + ")");
                processingOrConnectedAddresses.remove(macAddress);
                Log.d(TAG, "onDotScanned: Removed " + macAddress + " from processingOrConnectedAddresses due to connection failure. Set size: " + processingOrConnectedAddresses.size());
            }
        } else {
            // *** ADDED: Handle case where scanned device is not in our target list ***
            Log.w(TAG, "onDotScanned: Scanned device " + macAddress + " not found in target IMUArrayList or its MAC address is null.");
            processingOrConnectedAddresses.remove(macAddress);
            Log.d(TAG, "onDotScanned: Removed " + macAddress + " (not a target) from processingOrConnectedAddresses. Set size: " + processingOrConnectedAddresses.size());
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

        //Set isSynced to result of isSuccess
        isSynced = isSuccess;

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
