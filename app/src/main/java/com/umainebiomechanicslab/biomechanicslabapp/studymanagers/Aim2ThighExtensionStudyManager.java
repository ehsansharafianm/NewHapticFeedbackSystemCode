package com.umainebiomechanicslab.biomechanicslabapp.studymanagers;

import static com.xsens.dot.android.sdk.models.DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.HapticControlModule;
import com.umainebiomechanicslab.biomechanicslabapp.HttpRequestResponses;
import com.umainebiomechanicslab.biomechanicslabapp.UDPListenerThread;
import com.umainebiomechanicslab.biomechanicslabapp.imus.RecordingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMUWithFootDataAlgorithmForStrideCalculation;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMUWithThighAlgorithmForAim2ThighExtensionStudy;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMUWithThighAlgorithmForOriginalThighExtensionStudy;
import com.umainebiomechanicslab.biomechanicslabapp.imus.UniversalIMU;
import com.umainebiomechanicslab.biomechanicslabapp.targetmanagers.ThighExtensionStudyAim2TargetManager;
import com.umainebiomechanicslab.biomechanicslabapp.targetmanagers.ThighExtensionStudyOriginalTargetManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Aim2ThighExtensionStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.Aim2ThighExtensionStudyUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ExperimenterMenuUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LoadingWindowUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.OriginalThighExtensionStudyUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Aim2ThighExtensionStudyManager extends IMUManagerWithRecordingIMUs{

    private final String TAG = "Aim2ThighExtensionStudyManager";

    //Declare the HapticControlModule objects
    private final HapticControlModule leftHapticControlModule;
    private final HapticControlModule rightHapticControlModule;

    //Declare the ExperimenterMenuUI, ThighExtensionStudyUI and LoadingWindowUI User Interfaces
    private final Aim2ThighExtensionStudyUI thighExtensionStudyUI;
    private final LoadingWindowUI loadingWindowUI;
    private final ExperimenterMenuUI experimenterMenuUI;

    //Declare the variable to store if the gateway IP was found
    private boolean gateWayIPFound;

    //Declare the IMU Objects
    private final RecordingIMU leftArmIMU;
    private final RecordingIMU rightArmIMU;
    private final StreamingIMU leftThighIMU;
    private final StreamingIMU rightThighIMU;
    private final StreamingIMU leftFootIMU;
    private final StreamingIMU rightFootIMU;

    //Declare the Target Manager
    private final ThighExtensionStudyAim2TargetManager targetManager;

    //Create ArrayList to store Trial Objects
    private final ArrayList<Aim2ThighExtensionStudyTrial> trialArrayList = new ArrayList<>();

    // Map to store trial names and their durations
    private final Map<String, Integer> trialDurations;

    public Aim2ThighExtensionStudyManager(Aim2ThighExtensionStudyUI thighExtensionStudyUI, ExperimenterMenuUI experimenterMenuUI, LoadingWindowUI loadingWindowUI, Context context, FileManager fileManager) {

        super(context, thighExtensionStudyUI, fileManager, loadingWindowUI);

        //Initialize the HapticControlModule objects
        leftHapticControlModule = new HapticControlModule();
        rightHapticControlModule = new HapticControlModule();

        //Link the ThighExtensionStudyUI to the TestHapticCellsManager
        this.thighExtensionStudyUI = thighExtensionStudyUI;

        //Link the LoadingWindowUI to the TestHapticCellsManager
        this.loadingWindowUI = loadingWindowUI;

        //Link the ExperimenterMenuUI to the TestHapticCellsManager
        this.experimenterMenuUI = experimenterMenuUI;

        //Set the gateWayIPFound variable to false by default
        gateWayIPFound = false;

        //Initialize the Target Manager
        targetManager = new ThighExtensionStudyAim2TargetManager(this, thighExtensionStudyUI, fileManager);

        //Initialize the IMU Objects
        leftArmIMU = new RecordingIMU("Left Arm IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1);
        rightArmIMU = new RecordingIMU("Right Arm IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1);
        leftThighIMU = new StreamingIMUWithThighAlgorithmForAim2ThighExtensionStudy("Left Thigh IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1, targetManager);
        rightThighIMU = new StreamingIMUWithThighAlgorithmForAim2ThighExtensionStudy("Right Thigh IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1, targetManager);
        leftFootIMU = new StreamingIMUWithFootDataAlgorithmForStrideCalculation("Left Foot IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1);
        rightFootIMU = new StreamingIMUWithFootDataAlgorithmForStrideCalculation("Right Foot IMU", context, this, thighExtensionStudyUI, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1);

        //Add the IMU Objects to the IMU ArrayList
        IMUArrayList.add(leftArmIMU);
        IMUArrayList.add(rightArmIMU);
        IMUArrayList.add(leftThighIMU);
        IMUArrayList.add(rightThighIMU);
        IMUArrayList.add(leftFootIMU);
        IMUArrayList.add(rightFootIMU);

        //Add the Recording IMU Objects to the Recording IMU ArrayList
        recordingIMUArrayList.add(leftArmIMU);
        recordingIMUArrayList.add(rightArmIMU);

        // Initialize and populate the trialDurations Map
        // Example: trialDurations.put("Trial Name", durationInMinutes);
        trialDurations = new HashMap<>();
        trialDurations.put("Testing", 0);
        trialDurations.put("Baseline Normal", 2);
        trialDurations.put("Fast", 2);
        trialDurations.put("Error Feedback Familiarization", 2);
        trialDurations.put("Error Feedback", 6);
        trialDurations.put("Cooldown", 2);
        trialDurations.put("Retention", 2);

    }

    @Override
    public void updateIMUCode(String imuName, String imuCode) {

        //Find the IMU with the given name and update its MAC address
        for (UniversalIMU IMU : IMUArrayList) {
            if (IMU.getNameOfIMU().equals(imuName)) {
                setIMUMACAddress(imuName, imuCode);
                return;
            }
        }
    }

    @Override
    public void startAngleOffsetInitialization() {

        //For all the streaming IMUs in the IMU ArrayList, start their angle offset initialization
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU instanceof StreamingIMU){
                ((StreamingIMU) IMU).startOffsetInitialization();
            }
        }

    }

    @Override
    public void onAngleOffsetInitializationComplete() {

        //For all the streaming IMUs in the IMU ArrayList, check if their angle offset initialization is complete
        for(UniversalIMU IMU : IMUArrayList) {
            if (IMU instanceof StreamingIMU) {
                if (!((StreamingIMU) IMU).getOffsetAnglesInitialized()) {
                    return;
                }
            }
        }

        //If all streaming IMUs have their angle offset initialization complete, call onOffsetInitializationComplete
        thighExtensionStudyUI.onOffsetInitializationComplete(true);

    }

    @Override
    public void startTrial(String trialName) {

        //Log the start of the trial
        fileManager.writeToLogFile("Starting " + trialName + " trial");

        //Get the current date and time
        String currentTimeStamp = java.text.DateFormat.getDateTimeInstance().format(new Date());

        //Get the trial duration from the trialDurations Map
        Integer trialDurationMin = trialDurations.get(trialName);
        if (trialDurationMin == null) {
            Log.e(TAG, "Invalid trial name: " + trialName);
            userInterface.errorMessagePopUp("Invalid trial name, setting unlimited trial duration.");
            trialDurationMin = 0;
        }

        //Log the offset angles for each IMU
        fileManager.writeToLogFile(String.format(Locale.US,"Left Thigh Initialization Offset for %s mode: %.3f", trialName, leftThighIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Right Thigh Initialization Offset for %s mode: %.3f", trialName, rightThighIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Left Foot Initialization Offset for %s mode: %.3f", trialName, leftFootIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Right Foot Initialization Offset for %s mode: %.3f", trialName, rightFootIMU.getOffsetEulerAngle()));

        /*
         * Not all trial modes (ie Testing, Familiarization) require data recording,
         * therefore the arms don't always need to be started and loggers for the
         * streaming IMUs don't always need to be created.
         * */
        switch(trialName) {
            case "Testing":
            case "Error Feedback Familiarization":

                leftThighIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                rightThighIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                leftFootIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                rightFootIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                break;

            case "Baseline Normal":
            case "Fast":
            case "Cooldown":
            case "Retention":

                //Create a new Trial Object to store data and add it to the trialArrayList
                trialArrayList.add(new Aim2ThighExtensionStudyTrial(trialName, currentTimeStamp));

                //Call each IMUs startTrial function to start streaming/recording for each IMU
                leftThighIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightThighIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                leftFootIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightFootIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                leftArmIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightArmIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                break;

            case "Error Feedback":

                //Ensure the arrays that store the last 20 steps of data are reset before starting the trial
                targetManager.resetLast40StepsArray();
                targetManager.resetTarget();

                //Create a new Trial Object to store data and add it to the trialArrayList
                trialArrayList.add(new Aim2ThighExtensionStudyTrial(trialName, currentTimeStamp));

                //Call each IMUs startTrial function to start streaming/recording for each IMU
                leftThighIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightThighIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1),  trialDurationMin, true);
                leftFootIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightFootIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                leftArmIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                rightArmIMU.startTrial(trialName, currentTimeStamp, trialArrayList.get(trialArrayList.size()-1), trialDurationMin, true);
                break;
        }

    }

    @Override
    public void stopTrial(String trialName) {

        switch(trialName) {
            case "Testing":
            case "Error Feedback Familiarization":

                //Stop all IMUs
                leftThighIMU.stopTrial(false);
                rightThighIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                break;

            case "Fast":
            case "Cooldown":
            case "Retention":
            case "Error Feedback":

                //Stop all IMUs
                leftThighIMU.stopTrial(false);
                rightThighIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                leftArmIMU.stopTrial(false);
                rightArmIMU.stopTrial(false);
                break;

            case "Baseline Normal":

                //Stop all IMUs
                leftThighIMU.stopTrial(false);
                rightThighIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                leftArmIMU.stopTrial(false);
                rightArmIMU.stopTrial(false);

                //Generate the peak thigh target
                targetManager.generatePeakThighTarget(trialArrayList.get(trialArrayList.size() - 1));
                break;

        }
    }

    public void sendHapticFeedback(String nameOfIMU, String vibrationType){

        //Determine which module to send the feedback to
        if(nameOfIMU.contains("Left")) {

            //Send the vibration type to the left haptic control module
            leftHapticControlModule.sendHapticFeedback(vibrationType, new HttpRequestResponses() {

                @Override
                public void onRequestSent() {
                    fileManager.writeToLogFile("Sending vibration to " + nameOfIMU + " (" + leftHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onSuccessfulRequest() {
                    fileManager.writeToLogFile("Successfully sent vibration to " + nameOfIMU + " (" + leftHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onFailedRequest() {
                    fileManager.writeToLogFile("Failed to send vibration to " + nameOfIMU + " (" + leftHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }
            });
        }
        else {

            //Send the vibration type to the right haptic control module
            rightHapticControlModule.sendHapticFeedback(vibrationType, new HttpRequestResponses() {

                @Override
                public void onRequestSent() {
                    fileManager.writeToLogFile("Sending vibration to " + nameOfIMU + " (" + rightHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onSuccessfulRequest() {
                    fileManager.writeToLogFile("Successfully sent vibration to " + nameOfIMU + " (" + rightHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }

                @Override
                public void onFailedRequest() {
                    fileManager.writeToLogFile("Failed to send vibration to " + nameOfIMU + " (" + rightHapticControlModule.getIPAddress() + "/" + vibrationType + ")");
                }
            });
        }
    }

    public void setDeviceNumber(String sideOfBody, int deviceIP){

        if(sideOfBody.equals("Left")){
            leftHapticControlModule.setIPBlock(4, deviceIP);
            thighExtensionStudyUI.updateHapticCellIPAddress("Left", 4, deviceIP);
        }
        else{
            rightHapticControlModule.setIPBlock(4, deviceIP);
            thighExtensionStudyUI.updateHapticCellIPAddress("Right", 4, deviceIP);
        }

    }

    public boolean getGateWayIPFound(){
        return gateWayIPFound;
    }

    public void findGateWayIP(){

        //Declare the UDP Listener Thread
        UDPListenerThread udpListenerThread = new UDPListenerThread(experimenterMenuUI, thighExtensionStudyUI, loadingWindowUI, new UDPListenerThread.onUDPReceivedListener() {
            @Override
            public void onUDPReceived(int block1, int block2, int block3) {

                //Set the Gateway IP blocks for the haptic control modules
                leftHapticControlModule.setIPBlock(1, block1);
                leftHapticControlModule.setIPBlock(2, block2);
                leftHapticControlModule.setIPBlock(3, block3);
                rightHapticControlModule.setIPBlock(1, block1);
                rightHapticControlModule.setIPBlock(2, block2);
                rightHapticControlModule.setIPBlock(3, block3);

                //Set the device IP addresses in the UI
                thighExtensionStudyUI.updateHapticCellIPAddress("Left", 1, block1);
                thighExtensionStudyUI.updateHapticCellIPAddress("Left", 2, block2);
                thighExtensionStudyUI.updateHapticCellIPAddress("Left", 3, block3);
                thighExtensionStudyUI.updateHapticCellIPAddress("Right", 1, block1);
                thighExtensionStudyUI.updateHapticCellIPAddress("Right", 2, block2);
                thighExtensionStudyUI.updateHapticCellIPAddress("Right", 3, block3);

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
