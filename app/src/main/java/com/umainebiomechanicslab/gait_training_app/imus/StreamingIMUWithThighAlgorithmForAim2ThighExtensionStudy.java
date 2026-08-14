package com.umainebiomechanicslab.gait_training_app.imus;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.studymanagers.IMUManager;
import com.umainebiomechanicslab.gait_training_app.targetmanagers.ThighExtensionStudyAim2TargetManager;
import com.umainebiomechanicslab.gait_training_app.trials.Aim2ThighExtensionStudyTrial;
import com.umainebiomechanicslab.gait_training_app.trials.Trial;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.Aim2ThighExtensionStudyUI;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.Arrays;
import java.util.Locale;

public class StreamingIMUWithThighAlgorithmForAim2ThighExtensionStudy extends StreamingIMU{

    private Aim2ThighExtensionStudyTrial thighExtensionStudyTrial;
    private final Aim2ThighExtensionStudyUI thighExtensionStudyUI;
    private final ThighExtensionStudyAim2TargetManager targetManager;

    private final int MIN_SAMPLES_BETWEEN_PEAKS = 15;
    private final int MIN_EXTENSION_ANGLE = -4;
    private final int NUMBER_OF_CYCLES_UNTIL_STEADY_STATE = 10;
    private final int PACKET_COUNTER_STEP_OFFSET = 100000;
    private final int FEEDBACK_GIVEN_OFFSET = 100000000;

    private final double[] last5Angles;
    private double trialAngleSumForAverage;
    private int lastPeakSample;
    private int stepCounter;
    private double mostRecentPeakThighAngle;
    private final Object sampleCounterLock = new Object();

    public StreamingIMUWithThighAlgorithmForAim2ThighExtensionStudy(String nameOfIMU, Context context, IMUManager imuManager,
                                                                    Aim2ThighExtensionStudyUI thighExtensionStudyUI,
                                                                    FileManager fileManager, int measurementMode,
                                                                    ThighExtensionStudyAim2TargetManager targetManager) {

        super(nameOfIMU, context, imuManager, thighExtensionStudyUI, fileManager, measurementMode);

        this.thighExtensionStudyUI = thighExtensionStudyUI;
        this.targetManager = targetManager;

        last5Angles = new double[5];

        trialAngleSumForAverage = 0;
        lastPeakSample = 0;
        stepCounter = 0;
        mostRecentPeakThighAngle = 0;

    }

    private void updateRecentAnglesArray(double newAngle, double[] recentAngles){

        //Advance all values of the recentAngles array one space (dropping the last one)
        for(int i = (recentAngles.length-1); i > 0; i--){
            recentAngles[i] = recentAngles[i-1];
        }

        //Set index 0 of recentAngles array to newAngle
        recentAngles[0] = newAngle;

    }

    private boolean isMinPeak(double [] recentAngles, double threshold){
        int potentialPeakIndex = recentAngles.length/2;

        /*
         * An angle cannot be considered a PTE unless falls below a certain threshold. This prevents
         * a PTE from being detected during slight movements when the subject is standing still. The
         * threshold we have used before is -4 degrees (4 degrees of backwards extension), but this
         * can be changed as necessary.
         * */
        if(recentAngles[potentialPeakIndex] > threshold){
            return false;
        }

        for(int i = 1; i <= potentialPeakIndex; i++){
            if(recentAngles[potentialPeakIndex] > recentAngles[potentialPeakIndex+i]){
                return false;
            }
            else if(recentAngles[potentialPeakIndex] > recentAngles[potentialPeakIndex-i]){
                return false;
            }
        }

        mostRecentPeakThighAngle = recentAngles[potentialPeakIndex];
        return true;

    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        trialAngleSumForAverage = 0;
        lastPeakSample = 0;
        stepCounter = 0;

        mostRecentPeakThighAngle = 0;
        Arrays.fill(last5Angles, 0);

        this.thighExtensionStudyTrial = (Aim2ThighExtensionStudyTrial) trial;

        /*
         * After running the startTrial lines of code unique to a Streaming IMU with a data algorithm, run the
         * startTrial lines of code that all Streaming IMUs used (this is inherited from the parent StreamingIMU class
         * */
        super.startTrial(trialName, timeStamp, trial, trialDurationMin, logData);

    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {

        // ✅ ADD THIS LINE FIRST - before the existing log
        //Log.d("DATA_FLOW", nameOfIMU + " DATA packet received. trialName=" + trialName + ", sample=" + sampleCounter);


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
            switch (trialName) {
                case "HeadingReset":
                case "HeadingRevert":
                    // Do nothing here. We are just waiting for the onDotHeadingChanged callback.
                    break;
            /*case "Initialization":
                if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)){
                    if ((sampleCounter % outputFrequency) == 0) {
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "Initializing...");
                    }
                    trialAngleSum += eulerAngleX;
                } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)){
                    offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                    thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"Initialized %.3f",offsetEulerAngle));
                    fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);
                    offsetAnglesInitialized = true;
                    stopOffsetInitialization();
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

                case "Baseline Normal":
                case "Fast":
                case "Cooldown":
                case "Retention":

                    //Update the EulerX Value To The Offset Value
                    eulerAngleX = eulerAngleX - offsetEulerAngle;

                    //Set the packet counter to be the sample counter (may be changed in subsequent steps)
                    dotData.setPacketCounter(sampleCounter);

                    //Check to see that we haven't reached the end of the trial duration
                    if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                        //Update the last3Angles array to include the most recently measured angle
                        updateRecentAnglesArray(eulerAngleX, last5Angles);

                        //Update the User Interface (time counter) if the sample falls on a whole number second
                        if ((sampleCounter % outputFrequency) == 0) {
                            //thighExtensionStudyUI.updateTextViewText(outputIMUTextViewID,((sampleCounter / outputFrequency / 60) + ":" + String.format(Locale.US, "%02d", ((sampleCounter / outputFrequency) % 60))));
                            thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                        }

                        //Check to see if there was PTE in the most recent 3-sample window
                        if ((sampleCounter > 180) && (sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                            //The most recent Peak Sample is now the current sample
                            lastPeakSample = sampleCounter;

                            //Increase the number of steps taken by 1
                            stepCounter++;

                            //Update the packet counter to also show the number of steps taken
                            dotData.setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                            //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                            if (stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                                //Update the sum of all PTE Angles for use in the average angle calculation later
                                trialAngleSumForAverage += mostRecentPeakThighAngle;
                                thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PTE", mostRecentPeakThighAngle);
                            }

                            //Update the User Interface with the most recent PTE Angle
                            thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakThighAngle));

                            //Update the User Interface to show the new number of steps taken
                            thighExtensionStudyUI.updateGaitParameterOutput("PTECycleCount", nameOfIMU, String.valueOf(stepCounter));
                        }

                        //Update the Data Log File with the Latest Data Packet
                        dotLogFile.getDotLogger().update(dotData);
                    }

                    //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                    else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                        //Calculate the average PTE Angle for the trial
                        double peakThighAngleAverage = (trialAngleSumForAverage / (stepCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));

                        //Write the average PTE Angle and number of PTEs to the log file
                        fileManager.writeToLogFile(String.format(Locale.US, "%s %s Angle Average: %.3f", nameOfIMU, trialName, peakThighAngleAverage));
                        fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                        //Update the completed trial data in the Trial Manager
                        thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTE", peakThighAngleAverage);
                        thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTECycleCount", stepCounter);

                        //Update the User Interface to show that the trial is complete
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                    }
                    break;

                case "Error Feedback Familiarization":

                    //Update the EulerX Value To The Offset Value
                    eulerAngleX = eulerAngleX - offsetEulerAngle;

                    //Check to see that we haven't reached the end of the trial duration
                    if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                        //Update the last3Angles array to include the most recently measured angle
                        updateRecentAnglesArray(eulerAngleX, last5Angles);

                        //Update the User Interface (time counter) if the sample falls on a whole number second
                        if ((sampleCounter % outputFrequency) == 0) {
                            thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                        }

                        //Check to see if there was PTE in the most recent 3-sample window
                        if ((sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                            //The most recent Peak Sample is now the current sample
                            lastPeakSample = sampleCounter;

                            //Increase the number of steps taken by 1
                            stepCounter++;

                            //Send PTE to target manager to see if feedback should be given
                            targetManager.onPTEAngleDetected(mostRecentPeakThighAngle, nameOfIMU, sampleCounter, trialName, thighExtensionStudyTrial);

                            //Update the User Interface with the most recent PTE Angle
                            thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakThighAngle));

                            //Update the User Interface to show the new number of steps taken
                            thighExtensionStudyUI.updateGaitParameterOutput("PTECycleCount", nameOfIMU, String.valueOf(stepCounter));
                        }

                    }

                    //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                    else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                        //Update the User Interface to show that the trial is complete
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                    }
                    break;

                case "Error Feedback":
                case "Error Feedback 1":
                case "Error Feedback 2":
                case "Error Feedback 3":

                    //Update the EulerX Value To The Offset Value
                    eulerAngleX = eulerAngleX - offsetEulerAngle;

                    //Set the packet counter to be the sample counter (may be changed in subsequent steps)
                    dotData.setPacketCounter(sampleCounter);

                    //Check to see that we haven't reached the end of the trial duration
                    if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                        //Update the last3Angles array to include the most recently measured angle
                        updateRecentAnglesArray(eulerAngleX, last5Angles);

                        //Update the User Interface (time counter) if the sample falls on a whole number second
                        if ((sampleCounter % outputFrequency) == 0) {
                            thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                        }

                        //Check to see if there was PTE in the most recent 3-sample window
                        if ((sampleCounter > 180) && (sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                            //The most recent Peak Sample is now the current sample
                            lastPeakSample = sampleCounter;

                            //Increase the number of steps taken by 1
                            stepCounter++;

                            //Update the packet counter to also show the number of steps taken
                            dotData.setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                            //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                            if (stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                                //Update the sum of all PTE Angles for use in the average angle calculation later
                                trialAngleSumForAverage += mostRecentPeakThighAngle;
                                thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PTE", mostRecentPeakThighAngle);

                                //Send PTE to target manager to see if feedback should be given (and if target needs to be changed)
                                boolean feedbackProvided = targetManager.onPTEAngleDetected(mostRecentPeakThighAngle, nameOfIMU,
                                        sampleCounter, trialName, thighExtensionStudyTrial);

                                //If feedback was given, update the packet counter to show that feedback was given
                                if (feedbackProvided) {
                                    dotData.setPacketCounter(FEEDBACK_GIVEN_OFFSET + (stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);
                                }
                            }

                            //Update the User Interface with the most recent PTE Angle
                            thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakThighAngle));

                            //Update the User Interface to show the new number of steps taken
                            thighExtensionStudyUI.updateGaitParameterOutput("PTECycleCount", nameOfIMU, String.valueOf(stepCounter));
                        }

                        //Update the Data Log File with the Latest Data Packet
                        dotLogFile.getDotLogger().update(dotData);
                    }

                    //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                    else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                        //Calculate the average PTE Angle for the trial
                        double peakThighAngleAverage = (trialAngleSumForAverage / (stepCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));

                        //Write the average PTE Angle and number of PTEs to the log file
                        fileManager.writeToLogFile(String.format(Locale.US, "%s %s Angle Average: %.3f", nameOfIMU, trialName, peakThighAngleAverage));
                        fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                        //Update the completed trial data in the Trial Manager
                        thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTE", peakThighAngleAverage);
                        thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTECycleCount", stepCounter);

                        //Update the User Interface to show that the trial is complete
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                    }
                    break;
            }

            //Diagnostic-only: record clock comparison for Samsung desync investigation
            logSyncDiagnostics(dotData);

            //Increase the sample counter by 1
            sampleCounter++;
        }

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
            }
        }

        // ✅ DON'T call manager here - let polling handle it
        // imuManager.onAngleOffsetInitializationComplete();
    }


}
