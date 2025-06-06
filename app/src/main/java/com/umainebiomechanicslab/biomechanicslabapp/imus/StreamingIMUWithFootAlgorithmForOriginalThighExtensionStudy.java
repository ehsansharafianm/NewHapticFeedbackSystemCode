package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.content.Context;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.FootStrideData;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.OptimizedThighExtensionStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Trial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.OptimizedThighExtensionStudyUI;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.Locale;

public class StreamingIMUWithFootAlgorithmForOriginalThighExtensionStudy extends StreamingIMUWithFootDataAlgorithmForStrideCalculation {

    //Declare the trial object for the study
    private OptimizedThighExtensionStudyTrial thighExtensionStudyTrial;

    //Declare the user interface for the study
    private final OptimizedThighExtensionStudyUI thighExtensionStudyUI;

    public StreamingIMUWithFootAlgorithmForOriginalThighExtensionStudy(String nameOfIMU, Context context, IMUManager imuManager,
                                                                       OptimizedThighExtensionStudyUI thighExtensionStudyUI,
                                                                       FileManager fileManager, int measurementMode) {

        super(nameOfIMU, context, imuManager, thighExtensionStudyUI, fileManager, measurementMode);

        this.thighExtensionStudyUI = thighExtensionStudyUI;

    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        strides.clear();
        currentStride = new FootStrideData(nameOfIMU, offsetEulerAngle);

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

                    //Update the last5Angles array to include the most recently measured angle
                    updateRecentAnglesArray(eulerAngleX, last5Angles);

                    //Update the last3Samples array to include the most recent dotData sample
                    updateRecentSamplesArray(dotData, last3Samples);

                    //Add sample to the current stride
                    currentStride.appendStrideDataSample(last3Samples[2]);

                    //Update the User Interface (time counter) if the sample falls on a whole number second
                    if ((sampleCounter % outputFrequency) == 0) {
                        thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
                    }

                    //Check to see if there was Heel Strike in the most recent 3-sample window
                    if ((sampleCounter >= last2HeelStrikes[0] + MIN_SAMPLES_BETWEEN_PEAKS) && isMaxPeak(last5Angles, MIN_ANGLE_FOR_HEEL_STRIKE)){

                        //Update last2HeelStrikes[0] to the current sample, and make last2HeelStrikes[1] the previous heel strike sample
                        last2HeelStrikes[1] = last2HeelStrikes[0];
                        last2HeelStrikes[0] = sampleCounter-2;
                        double mostRecentCadence = 120 * (outputFrequency / ((double) (last2HeelStrikes[0] - last2HeelStrikes[1])));

                        //Increase the number of steps taken by 1
                        stepCounter++;

                        //Add currentStride to the strides ArrayList and start a new currentStride
                        strides.add(currentStride);
                        currentStride = new FootStrideData(nameOfIMU, offsetEulerAngle);
                        currentStride.appendStrideDataSample(last3Samples[2]);

                        //Update the packet counter to also show the number of steps taken
                        last3Samples[2].setPacketCounter((stepCounter * PACKET_COUNTER_STEP_OFFSET) + sampleCounter - 2);

                        //If we have exceeded the steady-state walking threshold, use that PTE for average PTE calculation
                        if(stepCounter > NUMBER_OF_CYCLES_UNTIL_STEADY_STATE){

                            //Update the sum of all cadences for use in the average cadence calculation later
                            trialCadenceSum += mostRecentCadence;
                            thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "Cadence", mostRecentCadence);

                            SpeedCalculationFromFootIMU.calculateSpeed(strides.get(strides.size() - 2), strides.get(strides.size() - 3), strides.get(strides.size() - 1), new SpeedCalculationFromFootIMU.SpeedReturn() {
                                @Override
                                public void onSpeedCalculated(double[] strideLengthAndSpeed) {

                                    double strideLength = strideLengthAndSpeed[0];
                                    double strideSpeed = strideLengthAndSpeed[1];

                                    thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "StrideLength", strideLength);
                                    thighExtensionStudyTrial.appendToGaitParameterArrayList(nameOfIMU, "WalkingSpeed", strideSpeed);

                                    //Update the User Interface with the most recent stride length and speed
                                    thighExtensionStudyUI.updateGaitParameterOutput("StrideLength", nameOfIMU, String.valueOf(strideLength));
                                    thighExtensionStudyUI.updateGaitParameterOutput("WalkingSpeed", nameOfIMU, String.valueOf(strideSpeed));

                                    //Update the sum of all stride lengths and speeds for use in the average stride length and speed calculation later
                                    trialStrideLengthSum += strideLength;
                                    trialStrideSpeedSum += strideSpeed;

                                }
                            });
                        }

                        //Update the User Interface with the most recent cadence and heel strike cycle count
                        thighExtensionStudyUI.updateGaitParameterOutput("Cadence", nameOfIMU, String.format(Locale.US,"%.1f steps/min", mostRecentCadence));
                        thighExtensionStudyUI.updateGaitParameterOutput("HeelStrikeCycleCount", nameOfIMU, String.valueOf(stepCounter));

                    }

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(last3Samples[2]);
                }

                //If we have reached the set trial duration, complete necessary UI and Trial Manager functions to complete trial
                else if (sampleCounter == (outputFrequency * 60 * trialDurationMin) + 1) {
                    double averageCadence = trialCadenceSum / (stepCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE);
                    double averageStrideLength = trialStrideLengthSum / (stepCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE);
                    double averageStrideSpeed = trialStrideSpeedSum / (stepCounter - NUMBER_OF_CYCLES_UNTIL_STEADY_STATE);

                    //Write the average PTE Angle and number of PTEs to the log file
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Average Cadence: %.1f steps/min", nameOfIMU, trialName, averageCadence));
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Average Stride Length: %.5f meters", nameOfIMU, trialName, averageStrideLength));
                    fileManager.writeToLogFile(String.format(Locale.US,"%s %s Average Speed: %.5f m/s", nameOfIMU, trialName, averageStrideSpeed));
                    fileManager.writeToLogFile(nameOfIMU + " " + trialName + " Steps Taken: " + stepCounter);

                    //Update the completed trial data in the Trial Manager
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "StrideLength", averageStrideLength);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "WalkingSpeed", averageStrideSpeed);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "Cadence", averageCadence);
                    thighExtensionStudyTrial.updateCompletedTrialData(nameOfIMU, "HeelStrikeCycleCount", stepCounter);

                    //Update the User Interface to show that the trial is complete
                    thighExtensionStudyUI.updateIMUDataOutput(nameOfIMU, "DONE");

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(last3Samples[1]);
                    dotLogFile.getDotLogger().update(last3Samples[0]);

                    strides.add(currentStride);

                }
        }

        //Increase the sample counter by 1
        sampleCounter++;

    }
}
