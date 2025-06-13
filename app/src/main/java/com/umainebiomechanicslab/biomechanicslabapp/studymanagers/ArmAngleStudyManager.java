package com.umainebiomechanicslab.biomechanicslabapp.studymanagers;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.HapticControlModule;
import com.umainebiomechanicslab.biomechanicslabapp.HttpRequestResponses;
import com.umainebiomechanicslab.biomechanicslabapp.UDPListenerThread;
import com.umainebiomechanicslab.biomechanicslabapp.imus.RecordingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMUWithArmAlgorithmForArmAngleStudy;
import com.umainebiomechanicslab.biomechanicslabapp.imus.StreamingIMUWithFootDataAlgorithmForStrideCalculation;
import com.umainebiomechanicslab.biomechanicslabapp.imus.UniversalIMU;
import com.umainebiomechanicslab.biomechanicslabapp.targetmanagers.ArmAngleStudyTargetManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.ArmAngleStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ArmAngleStudyUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ExperimenterMenuUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LoadingWindowUI;
import com.xsens.dot.android.sdk.models.DotPayload;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ArmAngleStudyManager extends IMUManagerWithRecordingIMUs{

    private final String TAG = "ArmAngleStudyManager";

    //Declare the HapticControlModule objects
    private final HapticControlModule leftHapticControlModule;
    private final HapticControlModule rightHapticControlModule;

    //Declare the ExperimenterMenuUI, ThighExtensionStudyUI and LoadingWindowUI User Interfaces
    private final ArmAngleStudyUI armAngleStudyUI;
    private final LoadingWindowUI loadingWindowUI;
    private final ExperimenterMenuUI experimenterMenuUI;

    //Declare the variable to store if the gateway IP was found
    private boolean gateWayIPFound;

    //Declare the IMU Objects
    private final StreamingIMU leftArmIMU;
    private final StreamingIMU rightArmIMU;
    private final RecordingIMU leftThighIMU;
    private final RecordingIMU rightThighIMU;
    private final StreamingIMU leftFootIMU;
    private final StreamingIMU rightFootIMU;

    //Declare the Target Manager
    private final ArmAngleStudyTargetManager targetManager;

    //Create ArrayList to store Trial Objects
    private final ArrayList<ArmAngleStudyTrial> trialArrayList = new ArrayList<>();

    // Map to store trial names and their durations
    private final Map<String, Integer> trialDurations;

    public ArmAngleStudyManager(ArmAngleStudyUI armAngleStudyUI, ExperimenterMenuUI experimenterMenuUI, LoadingWindowUI loadingWindowUI, Context context, FileManager fileManager) {

        super(context, armAngleStudyUI, fileManager, loadingWindowUI);

        //Initialize the HapticControlModule objects
        leftHapticControlModule = new HapticControlModule();
        rightHapticControlModule = new HapticControlModule();

        //Link the ThighExtensionStudyUI to the TestHapticCellsManager
        this.armAngleStudyUI = armAngleStudyUI;

        //Link the LoadingWindowUI to the TestHapticCellsManager
        this.loadingWindowUI = loadingWindowUI;

        //Link the ExperimenterMenuUI to the TestHapticCellsManager
        this.experimenterMenuUI = experimenterMenuUI;

        //Set the gateWayIPFound variable to false by default
        gateWayIPFound = false;

        //Initialize the Target Manager
        targetManager = new ArmAngleStudyTargetManager(this, armAngleStudyUI, fileManager);

        //Initialize the IMU Objects
        leftArmIMU = new StreamingIMUWithArmAlgorithmForArmAngleStudy("Left Arm IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION, targetManager);
        rightArmIMU = new StreamingIMUWithArmAlgorithmForArmAngleStudy("Right Arm IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_COMPLETE_QUATERNION, targetManager);
        leftThighIMU = new RecordingIMU("Left Thigh IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1);
        rightThighIMU = new RecordingIMU("Right Thigh IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1);
        leftFootIMU = new StreamingIMUWithFootDataAlgorithmForStrideCalculation("Left Foot IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1);
        rightFootIMU = new StreamingIMUWithFootDataAlgorithmForStrideCalculation("Right Foot IMU", context, this, armAngleStudyUI, fileManager, DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1);

        //Add the IMU Objects to the IMU ArrayList
        IMUArrayList.add(leftArmIMU);
        IMUArrayList.add(rightArmIMU);
        IMUArrayList.add(leftThighIMU);
        IMUArrayList.add(rightThighIMU);
        IMUArrayList.add(leftFootIMU);
        IMUArrayList.add(rightFootIMU);

        //Add the Recording IMU Objects to the Recording IMU ArrayList
        recordingIMUArrayList.add(leftThighIMU);
        recordingIMUArrayList.add(rightThighIMU);

        // Initialize and populate the trialDurations Map
        // Example: trialDurations.put("Trial Name", durationInMinutes);
        trialDurations = new HashMap<>();
        trialDurations.put("Testing", 0);
        trialDurations.put("Baseline Normal", 2);
        trialDurations.put("Fast", 2);
        trialDurations.put("Positive Forward Feedback Familiarization", 1);
        trialDurations.put("Positive Backward Feedback Familiarization", 1);
        trialDurations.put("Error Forward Feedback Familiarization", 1);
        trialDurations.put("Error Backward Feedback Familiarization", 1);
        trialDurations.put("Positive Forward Feedback 50%", 5);
        trialDurations.put("Positive Backward Feedback 50%", 5);
        trialDurations.put("Error Forward Feedback 50%", 5);
        trialDurations.put("Error Backward Feedback 50%", 5);
        trialDurations.put("Positive Forward Feedback 100%", 5);
        trialDurations.put("Positive Backward Feedback 100%", 5);
        trialDurations.put("Error Forward Feedback 100%", 5);
        trialDurations.put("Error Backward Feedback 100%", 5);

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
                Log.d(TAG, "Starting angle offset initialization for " + IMU.getNameOfIMU());
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
        armAngleStudyUI.onOffsetInitializationComplete(true);

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
        fileManager.writeToLogFile(String.format(Locale.US,"Left Arm Initialization Offset for %s mode: %.3f", trialName, leftArmIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Right Arm Initialization Offset for %s mode: %.3f", trialName, rightArmIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Left Foot Initialization Offset for %s mode: %.3f", trialName, leftFootIMU.getOffsetEulerAngle()));
        fileManager.writeToLogFile(String.format(Locale.US,"Right Foot Initialization Offset for %s mode: %.3f", trialName, rightFootIMU.getOffsetEulerAngle()));

        /*
         * Not all trial modes (ie Testing, Familiarization) require data recording,
         * therefore the arms don't always need to be started and loggers for the
         * streaming IMUs don't always need to be created.
         * */
        switch(trialName) {
            case "Testing":
            case "Positive Forward Feedback Familiarization":
            case "Positive Backward Feedback Familiarization":
            case "Error Forward Feedback Familiarization":
            case "Error Backward Feedback Familiarization":

                leftArmIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                rightArmIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                leftFootIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                rightFootIMU.startTrial(trialName, currentTimeStamp, null, trialDurationMin, false);
                break;

            case "Baseline Normal":
            case "Fast":
            case "Positive Forward Feedback 50%":
            case "Positive Backward Feedback 50%":
            case "Error Forward Feedback 50%":
            case "Error Backward Feedback 50%":
            case "Positive Forward Feedback 100%":
            case "Positive Backward Feedback 100%":
            case "Error Forward Feedback 100%":
            case "Error Backward Feedback 100%":

                //Create a new Trial Object to store data and add it to the trialArrayList
                trialArrayList.add(new ArmAngleStudyTrial(trialName, currentTimeStamp));

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
            case "Positive Forward Feedback Familiarization":
            case "Positive Backward Feedback Familiarization":
            case "Error Forward Feedback Familiarization":
            case "Error Backward Feedback Familiarization":

                //Stop all IMUs
                leftArmIMU.stopTrial(false);
                rightArmIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                break;

            case "Baseline Normal":

                //Stop all IMUs
                leftThighIMU.stopTrial(false);
                rightThighIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                leftArmIMU.stopTrial(false);
                rightArmIMU.stopTrial(false);

                //Calculate the Arm Angle Feedback Target
                targetManager.generatePeakArmTargets(trialArrayList.get(trialArrayList.size() - 1));
                break;

            case "Fast":
            case "Positive Forward Feedback 50%":
            case "Positive Backward Feedback 50%":
            case "Error Forward Feedback 50%":
            case "Error Backward Feedback 50%":
            case "Positive Forward Feedback 100%":
            case "Positive Backward Feedback 100%":
            case "Error Forward Feedback 100%":
            case "Error Backward Feedback 100%":

                //Stop all IMUs
                leftThighIMU.stopTrial(false);
                rightThighIMU.stopTrial(false);
                leftFootIMU.stopTrial(false);
                rightFootIMU.stopTrial(false);
                leftArmIMU.stopTrial(false);
                rightArmIMU.stopTrial(false);
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
            armAngleStudyUI.updateHapticCellIPAddress("Left", 4, deviceIP);
        }
        else{
            rightHapticControlModule.setIPBlock(4, deviceIP);
            armAngleStudyUI.updateHapticCellIPAddress("Right", 4, deviceIP);
        }

    }

    public boolean getGateWayIPFound(){
        return gateWayIPFound;
    }

    public void findGateWayIP(){

        //Declare the UDP Listener Thread
        UDPListenerThread udpListenerThread = new UDPListenerThread(experimenterMenuUI, armAngleStudyUI, loadingWindowUI, new UDPListenerThread.onUDPReceivedListener() {
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
                armAngleStudyUI.updateHapticCellIPAddress("Left", 1, block1);
                armAngleStudyUI.updateHapticCellIPAddress("Left", 2, block2);
                armAngleStudyUI.updateHapticCellIPAddress("Left", 3, block3);
                armAngleStudyUI.updateHapticCellIPAddress("Right", 1, block1);
                armAngleStudyUI.updateHapticCellIPAddress("Right", 2, block2);
                armAngleStudyUI.updateHapticCellIPAddress("Right", 3, block3);

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
