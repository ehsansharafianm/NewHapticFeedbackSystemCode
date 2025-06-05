package com.umainebiomechanicslab.biomechanicslabapp.studymanagers;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.HapticControlModule;
import com.umainebiomechanicslab.biomechanicslabapp.HttpRequestResponses;
import com.umainebiomechanicslab.biomechanicslabapp.UDPListenerThread;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ExperimenterMenuUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LoadingWindowUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.TestHapticCellsUI;

public class TestHapticCellsManager {

    //Declare the HapticControlModule objects
    private final HapticControlModule hapticControlModule1;
    private final HapticControlModule hapticControlModule2;

    //Declare the TestHapticCellsUI, ExperimenterMenuUI, and LoadingWindowUI User Interfaces
    private final TestHapticCellsUI testHapticCellsUI;
    private final LoadingWindowUI loadingWindowUI;
    private final ExperimenterMenuUI experimenterMenuUI;

    //Declare the FileManager
    private final FileManager fileManager;

    //Declare the variable to store if the gateway IP was found
    private boolean gateWayIPFound;

    //Declare the variables to store the vibration durations for each cell (in milliseconds)
    private int device1CellADuration, device1CellBDuration, device2CellADuration, device2CellBDuration;

    //Declare maximum and minimum vibration durations
    public final int MAX_VIBRATION_DURATION = 10000;
    public final int MIN_VIBRATION_DURATION = 250;

    public TestHapticCellsManager(TestHapticCellsUI testHapticCellsUI, ExperimenterMenuUI experimenterMenuUI, LoadingWindowUI loadingWindowUI, FileManager fileManager) {

        //Initialize the HapticControlModule objects
        hapticControlModule1 = new HapticControlModule();
        hapticControlModule2 = new HapticControlModule();

        //Link the TestHapticCellsUI to the TestHapticCellsManager
        this.testHapticCellsUI = testHapticCellsUI;

        //Link the LoadingWindowUI to the TestHapticCellsManager
        this.loadingWindowUI = loadingWindowUI;

        //Link the ExperimenterMenuUI to the TestHapticCellsManager
        this.experimenterMenuUI = experimenterMenuUI;

        //Link the FileManager to the TestHapticCellsManager
        this.fileManager = fileManager;

        //Set the default vibration durations for each cell (0.25 seconds)
        device1CellADuration = 250;
        device1CellBDuration = 250;
        device2CellADuration = 250;
        device2CellBDuration = 250;

        //Set the gateWayIPFound variable to false by default
        gateWayIPFound = false;

    }

    public void sendHapticFeedback(int moduleNumber, String cellLetter){

        //Declare the cell duration variable
        int cellDuration;

        //Create the vibration type String (e.g. "A?duration=")
        final String vibrationType;

        //Determine which module to send the feedback to
        if(moduleNumber == 1) {

            //Determine the vibration duration for the cell
            if(cellLetter.equals("A")){
                cellDuration = device1CellADuration;
            }
            else {
                cellDuration = device1CellBDuration;
            }

            //Set the vibration type String
            vibrationType = cellLetter + "?duration=" + cellDuration;

            hapticControlModule1.sendHapticFeedback(vibrationType, new HttpRequestResponses() {

                @Override
                public void onRequestSent() {
                    fileManager.writeToLogFile("Sending vibration to device 1 (" + hapticControlModule1.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onSuccessfulRequest() {
                    fileManager.writeToLogFile("Successfully sent vibration to device 1 (" + hapticControlModule1.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onFailedRequest() {
                    fileManager.writeToLogFile("Failed to send vibration to device 1 (" + hapticControlModule1.getIPAddress() + "/" + vibrationType + ")");
                }
            });
        }
        else {

            //Determine the vibration duration for the cell
            if(cellLetter.equals("A")){
                cellDuration = device2CellADuration;
            }
            else {
                cellDuration = device2CellBDuration;
            }

            //Set the vibration type String
            vibrationType = cellLetter + "?duration=" + cellDuration;

            hapticControlModule2.sendHapticFeedback(vibrationType, new HttpRequestResponses() {

                @Override
                public void onRequestSent() {
                    fileManager.writeToLogFile("Sending vibration to device 1 (" + hapticControlModule2.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onSuccessfulRequest() {
                    fileManager.writeToLogFile("Successfully sent vibration to device 1 (" + hapticControlModule2.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onFailedRequest() {
                    fileManager.writeToLogFile("Failed to send vibration to device 1 (" + hapticControlModule2.getIPAddress() + "/" + vibrationType + ")");
                }
            });
        }
    }

    public void setCellVibrationDuration(int moduleNumber, String cellLetter, int duration){

        if(moduleNumber == 1) {
            if(cellLetter.equals("A")){
                device1CellADuration = duration;
            }
            else {
                device1CellBDuration = duration;
            }

        }
        else {
            if(cellLetter.equals("A")) {
                device2CellADuration = duration;
            }
            else {
                device2CellBDuration = duration;
            }
        }

    }

    public void setDeviceNumber(int deviceNumber, int deviceIP){

        if(deviceNumber == 1){
            hapticControlModule1.setIPBlock(4, deviceIP);
            testHapticCellsUI.updateHapticCellIPAddress(1, 4, deviceIP);
        }
        else{
            hapticControlModule2.setIPBlock(4, deviceIP);
            testHapticCellsUI.updateHapticCellIPAddress(2, 4, deviceIP);
        }

    }

    public boolean getGateWayIPFound(){
        return gateWayIPFound;
    }

    public void findGateWayIP(){

        //Declare the UDP Listener Thread
        UDPListenerThread udpListenerThread = new UDPListenerThread(experimenterMenuUI, testHapticCellsUI, loadingWindowUI, new UDPListenerThread.onUDPReceivedListener() {
            @Override
            public void onUDPReceived(int block1, int block2, int block3) {

                //Set the Gateway IP blocks for the haptic control modules
                hapticControlModule1.setIPBlock(1, block1);
                hapticControlModule1.setIPBlock(2, block2);
                hapticControlModule1.setIPBlock(3, block3);
                hapticControlModule2.setIPBlock(1, block1);
                hapticControlModule2.setIPBlock(2, block2);
                hapticControlModule2.setIPBlock(3, block3);

                //Set the device IP addresses in the UI
                testHapticCellsUI.updateHapticCellIPAddress(1, 1, block1);
                testHapticCellsUI.updateHapticCellIPAddress(1, 2, block2);
                testHapticCellsUI.updateHapticCellIPAddress(1, 3, block3);
                testHapticCellsUI.updateHapticCellIPAddress(2, 1, block1);
                testHapticCellsUI.updateHapticCellIPAddress(2, 2, block2);
                testHapticCellsUI.updateHapticCellIPAddress(2, 3, block3);

                //Set the gateWayIPFound variable to true
                gateWayIPFound = true;

                //Continue to the Test Haptic Cells Page
                loadingWindowUI.onLoadingComplete();
            }
        });

        //Start the UDP Listener Thread
        udpListenerThread.start();
    }
}
