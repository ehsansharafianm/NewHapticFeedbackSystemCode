package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.content.Context;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManager;
import com.umainebiomechanicslab.biomechanicslabapp.targetmanagers.ArmAngleStudyTargetManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.ArmAngleStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Trial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ArmAngleStudyUI;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.Arrays;
import java.util.Locale;

public class StreamingIMUWithArmAlgorithmForArmAngleStudy extends StreamingIMU{

    private ArmAngleStudyTrial armAngleStudyTrial;
    private final ArmAngleStudyUI armAngleStudyUI;
    private final ArmAngleStudyTargetManager targetManager;

    private final int MIN_SAMPLES_BETWEEN_PEAKS = 15;
    private final int MIN_EXTENSION_ANGLE = -3;
    private final int MIN_FLEXION_ANGLE = 3;
    private final int NUMBER_OF_CYCLES_UNTIL_STEADY_STATE = 10;
    private final int PACKET_COUNTER_STEP_OFFSET = 100000;
    private final int FEEDBACK_GIVEN_OFFSET = 100000000;

    private final double[] last5Angles;
    private double trialFlexionAngleSumForAverage, trialExtensionAngleSumForAverage;
    private int lastPeakFlexionSample, lastPeakExtensionSample;
    private int peakArmFlexionCounter, peakArmExtensionCounter;
    private double mostRecentPeakArmFlexionAngle, mostRecentPeakArmExtensionAngle;

    public StreamingIMUWithArmAlgorithmForArmAngleStudy(String nameOfIMU, Context context, IMUManager imuManager,
                                                        ArmAngleStudyUI armAngleStudyUI,
                                                        FileManager fileManager, int measurementMode,
                                                        ArmAngleStudyTargetManager targetManager) {

        super(nameOfIMU, context, imuManager, armAngleStudyUI, fileManager, measurementMode);

        this.armAngleStudyUI = armAngleStudyUI;
        this.targetManager = targetManager;

        last5Angles = new double[5];

        trialFlexionAngleSumForAverage = 0;
        trialExtensionAngleSumForAverage = 0;
        lastPeakFlexionSample = 0;
        lastPeakExtensionSample = 0;
        peakArmFlexionCounter = 0;
        peakArmExtensionCounter = 0;
        mostRecentPeakArmFlexionAngle = 0;
        mostRecentPeakArmExtensionAngle = 0;

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

        mostRecentPeakArmExtensionAngle = recentAngles[potentialPeakIndex];
        return true;

    }

    protected boolean isMaxPeak(double [] recentAngles, double threshold){
        int potentialPeakIndex = recentAngles.length/2;

        /*
         * A sample cannot be considered a heel strike unless it falls above a certain threshold.
         * This prevents a heel strike from being detected during slight movements when the subject
         * is standing still. The threshold we are using is 10 degrees, but this can be changed
         * if necessary.
         * */
        if(recentAngles[potentialPeakIndex] < threshold){
            return false;
        }

        for(int i = 1; i <= potentialPeakIndex; i++){
            if(recentAngles[potentialPeakIndex] < recentAngles[potentialPeakIndex+i]){
                return false;
            }
            else if(recentAngles[potentialPeakIndex] < recentAngles[potentialPeakIndex-i]){
                return false;
            }
        }

        mostRecentPeakArmFlexionAngle = recentAngles[potentialPeakIndex];
        return true;

    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        trialFlexionAngleSumForAverage = 0;
        trialExtensionAngleSumForAverage = 0;
        lastPeakFlexionSample = 0;
        lastPeakExtensionSample = 0;
        peakArmFlexionCounter = 0;
        peakArmExtensionCounter = 0;
        mostRecentPeakArmFlexionAngle = 0;
        mostRecentPeakArmExtensionAngle = 0;

        Arrays.fill(last5Angles, 0);

        this.armAngleStudyTrial = (ArmAngleStudyTrial) trial;

        /*
         * After running the startTrial lines of code unique to a Streaming IMU with a data algorithm, run the
         * startTrial lines of code that all Streaming IMUs used (this is inherited from the parent StreamingIMU class
         * */
        super.startTrial(trialName, timeStamp, trial, trialDurationMin, logData);

    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {

        float[] quat = dotData.getQuat();

        double r23 = 2*(quat[2]*quat[3] - quat[0]*quat[1]);
        double r13 = 2*(quat[0]*quat[2] + quat[1]*quat[3]);

        double phi = Math.atan2(r23, r13);

        double r21 = 2*(quat[1]*quat[2] + quat[0]*quat[3]);
        double r22 = Math.pow(quat[0], 2) - Math.pow(quat[1], 2) + Math.pow(quat[2], 2) - Math.pow(quat[3], 2);
        double r11 = Math.pow(quat[0], 2) + Math.pow(quat[1], 2) - Math.pow(quat[2], 2) - Math.pow(quat[3], 2);
        double r12 = 2*(quat[1]*quat[2] - quat[0]*quat[3]);

        double eulerAngleZ = Math.toDegrees(Math.atan2(r21*Math.cos(phi) - r11*Math.sin(phi), r22*Math.cos(phi) - r12*Math.sin(phi)));

        /*
         * Different trial modes require different handling of the IMU Data. Initialization doesn't
         * log the data to a file, but needs to access the angle to calculate the offset. Testing needs
         * access to the angle in real time, but doesn't store it or any of the other data from the packet.
         * Familiarization just needs to keep track of trial time. All other trials store data to a log
         * file without accessing any specific data for in-app use.
         * */
        switch(trialName) {
            case "Initialization":
                if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)) {
                    if ((sampleCounter % outputFrequency) == 0) {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "Initializing...");
                    }
                    trialAngleSum += eulerAngleZ;
                } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)) {
                    offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "Initialized %.3f", offsetEulerAngle));
                    fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);
                    offsetAnglesInitialized = true;
                    stopOffsetInitialization();
                }
                break;
            case "Testing":
                if ((sampleCounter % (outputFrequency / 3)) == 0) {
                    if (offsetAnglesInitialized) {

                        //Update the EulerX Value To The Offset Value
                        eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                        if (eulerAngleZ >= 180) {
                            eulerAngleZ = eulerAngleZ - 360;
                        } else if (eulerAngleZ <= -180) {
                            eulerAngleZ = eulerAngleZ + 360;
                        }

                        if (nameOfIMU.equals("Left Arm IMU")) {
                            eulerAngleZ = -eulerAngleZ;
                        }

                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%.3f", eulerAngleZ - offsetEulerAngle));
                    } else {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, ("*" + String.format(Locale.US, "%.3f", eulerAngleZ)));
                    }
                }
                break;

            case "Baseline Normal":
            case "Fast":

                //Update the EulerX Value To The Offset Value
                eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                if (eulerAngleZ >= 180) {
                    eulerAngleZ = eulerAngleZ - 360;
                } else if (eulerAngleZ <= -180) {
                    eulerAngleZ = eulerAngleZ + 360;
                }

                if (nameOfIMU.equals("Left Arm IMU")) {
                    eulerAngleZ = -eulerAngleZ;
                }

                //Set the packet counter to be the sample counter (may be changed in subsequent steps)
                dotData.setPacketCounter(sampleCounter);

                //Check to see that we haven't reached the end of the trial duration
                if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                    //Update the last3Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleZ, last5Angles);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        //thighExtensionStudyUI.updateTextViewText(outputIMUTextViewID,((sampleCounter / outputFrequency / 60) + ":" + String.format(Locale.US, "%02d", ((sampleCounter / outputFrequency) % 60))));
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Peak Arm Extension in the most recent 3-sample window
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakExtensionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakExtensionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmExtensionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmExtensionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialExtensionAngleSumForAverage += mostRecentPeakArmExtensionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAE", mostRecentPeakArmExtensionAngle);
                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmExtensionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAECycleCount", nameOfIMU, String.valueOf(peakArmExtensionCounter));
                    }

                    //Check to see if there was Peak Arm Flexion in the most recent 3-sample window
                    else if ((sampleCounter > 180) && (sampleCounter >= lastPeakFlexionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_FLEXION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakFlexionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmFlexionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmFlexionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialFlexionAngleSumForAverage += mostRecentPeakArmFlexionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAF", mostRecentPeakArmFlexionAngle);
                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAF", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmFlexionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAFCycleCount", nameOfIMU, String.valueOf(peakArmFlexionCounter));
                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(dotData);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                    double peakArmFlexionAngleAverage, peakArmExtensionAngleAverage;

                    //Calculate the average PAF Angle for the trial
                    if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmFlexionAngleAverage = (trialFlexionAngleSumForAverage / (peakArmFlexionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmFlexionAngleAverage = 0;
                    }

                    //Calculate the average PAE Angle for the trial
                    if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmExtensionAngleAverage = (trialExtensionAngleSumForAverage / (peakArmExtensionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmExtensionAngleAverage = 0;
                    }

                    //Write the average PTE Angle and number of PTEs to the log file
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Flexion Angle Average: %.3f", nameOfIMU, trialName, peakArmFlexionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Flexion Count: " + peakArmFlexionCounter);
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Extension Angle Average: %.3f", nameOfIMU, trialName, peakArmExtensionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Extension Count: " + peakArmExtensionCounter);

                    //Update the completed trial data in the Trial Manager
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAE", peakArmExtensionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAECycleCount", peakArmExtensionCounter);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAF", peakArmFlexionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAFCycleCount", peakArmFlexionCounter);

                    //Update the User Interface to show that the trial is complete
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Positive Backward Feedback Familiarization":
            case "Error Backward Feedback Familiarization":

                //Update the EulerZ Value To The Offset Value
                eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                if (eulerAngleZ >= 180) {
                    eulerAngleZ = eulerAngleZ - 360;
                } else if (eulerAngleZ <= -180) {
                    eulerAngleZ = eulerAngleZ + 360;
                }

                if (nameOfIMU.equals("Left Arm IMU")) {
                    eulerAngleZ = -eulerAngleZ;
                }

                //Check to see that we haven't reached the end of the trial duration
                if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                    //Update the last3Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleZ, last5Angles);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Peak Arm Extension in the most recent 3-sample window
                    if ((sampleCounter >= lastPeakExtensionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakExtensionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmExtensionCounter++;

                        //Send the PAE to the target manager to see if feedback should be given
                        targetManager.onPAEAngleDetected(mostRecentPeakArmExtensionAngle, nameOfIMU, trialName);

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmExtensionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAECycleCount", nameOfIMU, String.valueOf(peakArmExtensionCounter));
                    }

                    //Check to see if there was Peak Arm Flexion in the most recent 3-sample window
                    else if ((sampleCounter >= lastPeakFlexionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_FLEXION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakFlexionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmFlexionCounter++;

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAF", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmFlexionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAFCycleCount", nameOfIMU, String.valueOf(peakArmFlexionCounter));
                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(dotData);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                    //Update the User Interface to show that the trial is complete
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Positive Forward Feedback Familiarization":
            case "Error Forward Feedback Familiarization":

                //Update the EulerX Value To The Offset Value
                eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                if (eulerAngleZ >= 180) {
                    eulerAngleZ = eulerAngleZ - 360;
                } else if (eulerAngleZ <= -180) {
                    eulerAngleZ = eulerAngleZ + 360;
                }

                if (nameOfIMU.equals("Left Arm IMU")) {
                    eulerAngleZ = -eulerAngleZ;
                }

                //Check to see that we haven't reached the end of the trial duration
                if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                    //Update the last3Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleZ, last5Angles);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Peak Arm Extension in the most recent 3-sample window
                    if ((sampleCounter >= lastPeakExtensionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakExtensionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmExtensionCounter++;

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmExtensionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAECycleCount", nameOfIMU, String.valueOf(peakArmExtensionCounter));
                    }

                    //Check to see if there was Peak Arm Flexion in the most recent 3-sample window
                    else if ((sampleCounter >= lastPeakFlexionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_FLEXION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakFlexionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmFlexionCounter++;

                        //Send the PAE to the target manager to see if feedback should be given
                        targetManager.onPAFAngleDetected(mostRecentPeakArmFlexionAngle, nameOfIMU, trialName);

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAF", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmFlexionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAFCycleCount", nameOfIMU, String.valueOf(peakArmFlexionCounter));
                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(dotData);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                    //Update the User Interface to show that the trial is complete
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Positive Backward Feedback 50%":
            case "Error Backward Feedback 50%":
            case "Positive Backward Feedback 100%":
            case "Error Backward Feedback 100%":

                //Update the EulerX Value To The Offset Value
                eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                if (eulerAngleZ >= 180) {
                    eulerAngleZ = eulerAngleZ - 360;
                } else if (eulerAngleZ <= -180) {
                    eulerAngleZ = eulerAngleZ + 360;
                }

                if (nameOfIMU.equals("Left Arm IMU")) {
                    eulerAngleZ = -eulerAngleZ;
                }

                //Set the packet counter to be the sample counter (may be changed in subsequent steps)
                dotData.setPacketCounter(sampleCounter);

                //Check to see that we haven't reached the end of the trial duration
                if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                    //Update the last3Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleZ, last5Angles);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Peak Arm Extension in the most recent 3-sample window
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakExtensionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakExtensionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmExtensionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmExtensionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialExtensionAngleSumForAverage += mostRecentPeakArmExtensionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAE", mostRecentPeakArmExtensionAngle);

                            //Send the PAE to the target manager to see if feedback should be given and store whether feedback was provided
                            boolean feedbackProvided = targetManager.onPAEAngleDetected(mostRecentPeakArmExtensionAngle, nameOfIMU, trialName);

                            //If feedback was given, update the packet counter to show that feedback was given
                            if (feedbackProvided) {
                                dotData.setPacketCounter(FEEDBACK_GIVEN_OFFSET + (peakArmExtensionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);
                            }
                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmExtensionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAECycleCount", nameOfIMU, String.valueOf(peakArmExtensionCounter));
                    }

                    //Check to see if there was Peak Arm Flexion in the most recent 3-sample window
                    else if ((sampleCounter > 180) && (sampleCounter >= lastPeakFlexionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_FLEXION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakFlexionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmFlexionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmFlexionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialFlexionAngleSumForAverage += mostRecentPeakArmFlexionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAF", mostRecentPeakArmFlexionAngle);
                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAF", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmFlexionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAFCycleCount", nameOfIMU, String.valueOf(peakArmFlexionCounter));
                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(dotData);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                    double peakArmFlexionAngleAverage, peakArmExtensionAngleAverage;

                    //Calculate the average PAF Angle for the trial
                    if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmFlexionAngleAverage = (trialFlexionAngleSumForAverage / (peakArmFlexionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmFlexionAngleAverage = 0;
                    }

                    //Calculate the average PAE Angle for the trial
                    if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmExtensionAngleAverage = (trialExtensionAngleSumForAverage / (peakArmExtensionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmExtensionAngleAverage = 0;
                    }

                    //Write the average PTE Angle and number of PTEs to the log file
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Flexion Angle Average: %.3f", nameOfIMU, trialName, peakArmFlexionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Flexion Count: " + peakArmFlexionCounter);
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Extension Angle Average: %.3f", nameOfIMU, trialName, peakArmExtensionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Extension Count: " + peakArmExtensionCounter);

                    //Update the completed trial data in the Trial Manager
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAE", peakArmExtensionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAECycleCount", peakArmExtensionCounter);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAF", peakArmFlexionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAFCycleCount", peakArmFlexionCounter);

                    //Update the User Interface to show that the trial is complete
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Positive Forward Feedback 50%":
            case "Error Forward Feedback 50%":
            case "Positive Forward Feedback 100%":
            case "Error Forward Feedback 100%":

                //Update the EulerX Value To The Offset Value
                eulerAngleZ = eulerAngleZ - offsetEulerAngle;

                if (eulerAngleZ >= 180) {
                    eulerAngleZ = eulerAngleZ - 360;
                } else if (eulerAngleZ <= -180) {
                    eulerAngleZ = eulerAngleZ + 360;
                }

                if (nameOfIMU.equals("Left Arm IMU")) {
                    eulerAngleZ = -eulerAngleZ;
                }

                //Set the packet counter to be the sample counter (may be changed in subsequent steps)
                dotData.setPacketCounter(sampleCounter);

                //Check to see that we haven't reached the end of the trial duration
                if (sampleCounter <= (outputFrequency * 60 * trialDurationMin)) {

                    //Update the last3Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleZ, last5Angles);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        armAngleStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Peak Arm Extension in the most recent 3-sample window
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakExtensionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakExtensionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmExtensionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmExtensionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialExtensionAngleSumForAverage += mostRecentPeakArmExtensionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAE", mostRecentPeakArmExtensionAngle);

                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAE", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmExtensionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAECycleCount", nameOfIMU, String.valueOf(peakArmExtensionCounter));
                    }

                    //Check to see if there was Peak Arm Flexion in the most recent 3-sample window
                    else if ((sampleCounter > 180) && (sampleCounter >= lastPeakFlexionSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_FLEXION_ANGLE)) {

                        //The most recent Peak Sample is now the current sample
                        lastPeakFlexionSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        peakArmFlexionCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((peakArmFlexionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialFlexionAngleSumForAverage += mostRecentPeakArmFlexionAngle;
                            armAngleStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PAF", mostRecentPeakArmFlexionAngle);

                            //Send the PAE to the target manager to see if feedback should be given and store whether feedback was provided
                            boolean feedbackProvided = targetManager.onPAFAngleDetected(mostRecentPeakArmFlexionAngle, nameOfIMU, trialName);

                            //If feedback was given, update the packet counter to show that feedback was given
                            if (feedbackProvided) {
                                dotData.setPacketCounter(FEEDBACK_GIVEN_OFFSET + (peakArmFlexionCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);
                            }
                        }

                        //Update the User Interface with the most recent PTE Angle
                        armAngleStudyUI.updateGaitParameterOutput("PAF", nameOfIMU, String.format(Locale.US, "%.3f", mostRecentPeakArmFlexionAngle));

                        //Update the User Interface to show the new number of extensions
                        armAngleStudyUI.updateGaitParameterOutput("PAFCycleCount", nameOfIMU, String.valueOf(peakArmFlexionCounter));
                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(dotData);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {

                    double peakArmFlexionAngleAverage, peakArmExtensionAngleAverage;

                    //Calculate the average PAF Angle for the trial
                    if (peakArmFlexionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmFlexionAngleAverage = (trialFlexionAngleSumForAverage / (peakArmFlexionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmFlexionAngleAverage = 0;
                    }

                    //Calculate the average PAE Angle for the trial
                    if (peakArmExtensionCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE) {
                        peakArmExtensionAngleAverage = (trialExtensionAngleSumForAverage / (peakArmExtensionCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE));
                    } else {
                        peakArmExtensionAngleAverage = 0;
                    }

                    //Write the average PTE Angle and number of PTEs to the log file
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Flexion Angle Average: %.3f", nameOfIMU, trialName, peakArmFlexionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Flexion Count: " + peakArmFlexionCounter);
                    fileManager.writeToLogFile(String.format(Locale.US, "%s %s Extension Angle Average: %.3f", nameOfIMU, trialName, peakArmExtensionAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Extension Count: " + peakArmExtensionCounter);

                    //Update the completed trial data in the Trial Manager
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAE", peakArmExtensionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAECycleCount", peakArmExtensionCounter);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAF", peakArmFlexionAngleAverage);
                    armAngleStudyTrial.updateCompletedTrialData(nameOfIMU, "PAFCycleCount", peakArmFlexionCounter);

                    //Update the User Interface to show that the trial is complete
                    armAngleStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }

                break;

        }

        //Increase the sample counter by 1
        sampleCounter++;

    }


}
