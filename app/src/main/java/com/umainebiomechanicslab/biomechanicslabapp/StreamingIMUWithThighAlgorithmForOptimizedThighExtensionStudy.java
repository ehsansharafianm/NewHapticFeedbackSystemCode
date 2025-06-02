package com.umainebiomechanicslab.biomechanicslabapp;

import android.content.Context;

import com.xsens.dot.android.sdk.events.DotData;

import java.util.Arrays;
import java.util.Locale;

public class StreamingIMUWithThighAlgorithmForOptimizedThighExtensionStudy extends StreamingIMU{

    private OptimizedThighExtensionStudyTrial thighExtensionStudyTrial;
    private final OptimizedThighExtensionStudyUI thighExtensionStudyUI;
    private final ThighExtensionStudyOptimizedTargetManager targetManager;

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

    public StreamingIMUWithThighAlgorithmForOptimizedThighExtensionStudy(String nameOfIMU, Context context, IMUManager imuManager,
                                                                         OptimizedThighExtensionStudyUI thighExtensionStudyUI,
                                                                         FileManager fileManager, int measurementMode,
                                                                         ThighExtensionStudyOptimizedTargetManager targetManager) {

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

        this.thighExtensionStudyTrial = (OptimizedThighExtensionStudyTrial) trial;

        /*
         * After running the startTrial lines of code unique to a Streaming IMU with a data algorithm, run the
         * startTrial lines of code that all Streaming IMUs used (this is inherited from the parent StreamingIMU class
         * */
        super.startTrial(trialName, timeStamp, trial, trialDurationMin, logData);

    }

    @Override
    public void onDotDataChanged(String address, DotData dotData) {

        /*
         * Declare the double variable for cases where the app needs to store the
         * angle value from the IMU Data Packet for display or use
         * */
        //double eulerAngleX = DotParser.quaternion2Euler(dotData.getQuat())[0];
        double eulerAngleX = dotData.getEuler()[0];

        /*
         * Different trial modes require different handling of the IMU Data. Initialization doesn't
         * log the data to a file, but needs to access the angle to calculate the offset. Testing needs
         * access to the angle in real time, but doesn't store it or any of the other data from the packet.
         * Familiarization just needs to keep track of trial time. All other trials store data to a log
         * file without accessing any specific data for in-app use.
         * */
        switch(trialName){
            case "Initialization":
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
                break;
            case "Testing":
                if ((sampleCounter % (outputFrequency/3)) == 0) {
                    if (offsetAnglesInitialized) {
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"%.3f",eulerAngleX - offsetEulerAngle));
                    } else {
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, ("*" + String.format(Locale.US,"%.3f",eulerAngleX)));
                    }
                }
                break;

            case "Baseline Normal":
            case "Baseline with Cognitive Task":
            case "Fast":

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
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)){

                        //The most recent Peak Sample is now the current sample
                        lastPeakSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        stepCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if(stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE){

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialAngleSumForAverage += mostRecentPeakThighAngle;
                            thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PTE", mostRecentPeakThighAngle);
                        }

                        //Update the User Interface with the most recent PTE Angle
                        thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US,"%.3f",mostRecentPeakThighAngle));

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
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Angle Average: %.3f", nameOfIMU, trialName, peakThighAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                    //Update the completed trial data in the Trial Manager
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTE", peakThighAngleAverage);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTECycleCount", stepCounter);

                    //Update the User Interface to show that the trial is complete
                    thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Optimization Familiarization":

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
                    if ((sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)){

                        //The most recent Peak Sample is now the current sample
                        lastPeakSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        stepCounter++;

                        //Send PTE to target manager to see if feedback should be given
                        targetManager.onPTEAngleDetected(mostRecentPeakThighAngle, nameOfIMU, sampleCounter, outputFrequency, null);

                        //Update the User Interface with the most recent PTE Angle
                        thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US,"%.3f",mostRecentPeakThighAngle));

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

            case "Optimization Feedback":

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
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)){

                        //The most recent Peak Sample is now the current sample
                        lastPeakSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        stepCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if(stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE){

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialAngleSumForAverage += mostRecentPeakThighAngle;
                            thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PTE", mostRecentPeakThighAngle);

                            //Send PTE to target manager to see if feedback should be given (and if target needs to be changed)
                            boolean feedbackProvided = targetManager.onPTEAngleDetected(mostRecentPeakThighAngle, nameOfIMU,
                                    sampleCounter, outputFrequency, thighExtensionStudyTrial);

                            //If feedback was given, update the packet counter to show that feedback was given
                            if(feedbackProvided){
                                dotData.setPacketCounter(FEEDBACK_GIVEN_OFFSET + (stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);
                            }

                        }

                        //Update the User Interface with the most recent PTE Angle
                        thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US,"%.3f",mostRecentPeakThighAngle));

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
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Angle Average: %.3f", nameOfIMU, trialName, peakThighAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                    //Update the completed trial data in the Trial Manager
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTE", peakThighAngleAverage);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTECycleCount", stepCounter);

                    //Update the User Interface to show that the trial is complete
                    thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;

            case "Optimization Feedback with Cognitive Task":

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
                    if ((sampleCounter > 180) && (sampleCounter >= lastPeakSample + MIN_SAMPLES_BETWEEN_PEAKS) && isMinPeak(last5Angles, MIN_EXTENSION_ANGLE)){

                        //The most recent Peak Sample is now the current sample
                        lastPeakSample = sampleCounter;

                        //Increase the number of steps taken by 1
                        stepCounter++;

                        //Update the packet counter to also show the number of steps taken
                        dotData.setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if(stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE){

                            //Update the sum of all PTE Angles for use in the average angle calculation later
                            trialAngleSumForAverage += mostRecentPeakThighAngle;
                            thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "PTE", mostRecentPeakThighAngle);

                            //Send PTE to target manager to see if feedback should be given
                            boolean feedbackProvided = targetManager.onPTEAngleDetected(mostRecentPeakThighAngle, nameOfIMU,
                                    sampleCounter, outputFrequency, thighExtensionStudyTrial);

                            //If feedback was given, update the packet counter to show that feedback was given
                            if(feedbackProvided){
                                dotData.setPacketCounter(FEEDBACK_GIVEN_OFFSET + (stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter);
                            }

                        }

                        //Update the User Interface with the most recent PTE Angle
                        thighExtensionStudyUI.updateGaitParameterOutput("PTE", nameOfIMU, String.format(Locale.US,"%.3f",mostRecentPeakThighAngle));

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
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Angle Average: %.3f", nameOfIMU, trialName, peakThighAngleAverage));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                    //Update the completed trial data in the Trial Manager
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTE", peakThighAngleAverage);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "PTECycleCount", stepCounter);

                    //Update the User Interface to show that the trial is complete
                    thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");
                }
                break;
        }

        //Increase the sample counter by 1
        sampleCounter++;

    }


}
