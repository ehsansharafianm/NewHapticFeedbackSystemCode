package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.FootStrideData;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.Trial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithIMU;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class StreamingIMUWithFootDataAlgorithmForStrideCalculation extends StreamingIMU{

    private final String TAG = "StreamingIMUWithFootDataAlgorithmForStrideCalculation";

    protected final int MIN_SAMPLES_BETWEEN_PEAKS = 15;
    protected final int MIN_ANGLE_FOR_HEEL_STRIKE = 10;
    protected final int NUMBER_OF_CYCLES_UNTIL_STEADY_STATE = 10;
    protected final int PACKET_COUNTER_STEP_OFFSET = 100000;

    protected final double[] last5Angles;
    protected final DotData[] last3Samples;
    protected final int[] last2HeelStrikes;
    protected double trialCadenceSum;
    protected double trialStrideLengthSum;
    protected double trialStrideSpeedSum;
    protected int stepCounter;

    protected final ArrayList<FootStrideData> strides;
    protected FootStrideData currentStride;

    //Declare Trial object for the study
    protected Trial trial;

    public StreamingIMUWithFootDataAlgorithmForStrideCalculation(String nameOfIMU, Context context, IMUManager imuManager,
                                                                 UserInterfaceWithIMU userInterface, FileManager fileManager,
                                                                 int measurementMode) {

        super(nameOfIMU, context, imuManager, userInterface, fileManager, measurementMode);

        last5Angles = new double[5];
        last2HeelStrikes = new int[2];
        last3Samples = new DotData[3];

        trialCadenceSum = 0;
        trialStrideLengthSum = 0;
        trialStrideSpeedSum = 0;
        stepCounter = 0;

        Arrays.fill(last5Angles, 0);
        Arrays.fill(last2HeelStrikes, 0);
        Arrays.fill(last3Samples, null);

        strides = new ArrayList<>();
        currentStride = new FootStrideData(nameOfIMU, offsetEulerAngle);
    }

    protected void updateRecentAnglesArray(double newAngle, double[] recentAngles){

        //Advance all values of the recentAngles array one space (dropping the last one)
        for(int i = (recentAngles.length-1); i > 0; i--){
            recentAngles[i] = recentAngles[i-1];
        }

        //Set index 0 of recentAngles array to newAngle
        recentAngles[0] = newAngle;

    }

    protected void updateRecentSamplesArray(DotData newDotDataSample, DotData[] recentDotDataSample){

        //Advance all values of the recentAngles array one space (dropping the last one)
        for(int i = (recentDotDataSample.length-1); i > 0; i--){
            recentDotDataSample[i] = recentDotDataSample[i-1];
        }

        //Set index 0 of recentAngles array to newAngle
        recentDotDataSample[0] = newDotDataSample;

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

        return true;

    }

    @Override
    public void startTrial(String trialName, String timeStamp, Trial trial, int trialDurationMin, boolean logData) {

        strides.clear();
        currentStride = new FootStrideData(nameOfIMU, offsetEulerAngle);

        this.trial = trial;

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
        //Log.d(TAG, "EulerX: " + eulerAngleX);

        //Log the first sample
        //if(sampleCounter == 1){
        //    Log.d(TAG, trialName + sampleCounter + nameOfIMU);
        //}

        //Log.d(TAG, trialName + sampleCounter + nameOfIMU);

        /*
         * Different trial modes require different handling of the IMU Data. Initialization doesn't
         * log the data to a file, but needs to access the angle to calculate the offset. Testing needs
         * access to the angle in real time, but doesn't store it or any of the other data from the packet.
         * Familiarization just needs to keep track of trial time. All other trials store data to a log
         * file without accessing any specific data for in-app use.
         * */
        switch(trialName){
            case "HeadingReset":
                // Do nothing here. We are just waiting for the onDotHeadingChanged callback.
                break;
            case "Initialization":
                if (sampleCounter < (outputFrequency * offsetInitializationDurationSec)){
                    if ((sampleCounter % outputFrequency) == 0) {
                        userInterface.updateIMUDataOutput(nameOfIMU, "Initializing...");
                        //Log.d(TAG, "Initializing...");
                    }
                    trialAngleSum += eulerAngleX;
                } else if (sampleCounter == (outputFrequency * offsetInitializationDurationSec)){
                    offsetEulerAngle = trialAngleSum / (outputFrequency * offsetInitializationDurationSec);
                    userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"Initialized %.3f",offsetEulerAngle));
                    fileManager.writeToLogFile(nameOfIMU + " Initialized Angle Offset: " + offsetEulerAngle);
                    offsetAnglesInitialized = true;
                    stopOffsetInitialization();
                }
                break;
            case "Testing":
                if ((sampleCounter % (outputFrequency/3)) == 0) {
                    if (offsetAnglesInitialized) {
                        userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US,"%.3f",eulerAngleX - offsetEulerAngle));
                    } else {
                        userInterface.updateIMUDataOutput(nameOfIMU, ("*" + String.format(Locale.US,"%.3f",eulerAngleX)));
                    }
                }
                break;

            default:

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
                        userInterface.updateIMUDataOutput(nameOfIMU, String.format(Locale.US, "%d:%02d", (sampleCounter / outputFrequency / 60), ((sampleCounter / outputFrequency) % 60)));
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
                            trial.appendToGaitParameterArrayList(nameOfIMU, "Cadence", mostRecentCadence);

                            SpeedCalculationFromFootIMU.calculateSpeed(strides.get(strides.size() - 2), strides.get(strides.size() - 3), strides.get(strides.size() - 1), new SpeedCalculationFromFootIMU.SpeedReturn() {
                                @Override
                                public void onSpeedCalculated(double[] strideLengthAndSpeed) {

                                    double strideLength = strideLengthAndSpeed[0];
                                    double strideSpeed = strideLengthAndSpeed[1];

                                    trial.appendToGaitParameterArrayList(nameOfIMU, "StrideLength", strideLength);
                                    trial.appendToGaitParameterArrayList(nameOfIMU, "WalkingSpeed", strideSpeed);

                                    //Update the User Interface with the most recent stride length and speed
                                    userInterface.updateGaitParameterOutput("StrideLength", nameOfIMU, String.format(Locale.US, "%.3f", strideLength));
                                    userInterface.updateGaitParameterOutput("WalkingSpeed", nameOfIMU, String.format(Locale.US, "%.3f", strideSpeed));

                                    //Update the sum of all stride lengths and speeds for use in the average stride length and speed calculation later
                                    trialStrideLengthSum += strideLength;
                                    trialStrideSpeedSum += strideSpeed;

                                }
                            });
                        }

                        //Update the User Interface with the most recent cadence and heel strike cycle count
                        userInterface.updateGaitParameterOutput("Cadence", nameOfIMU, String.format(Locale.US,"%.1f steps/min", mostRecentCadence));
                        userInterface.updateGaitParameterOutput("HeelStrikeCycleCount", nameOfIMU, String.valueOf(stepCounter));

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
                    trial.updateCompletedTrialData(nameOfIMU, "StrideLength", averageStrideLength);
                    trial.updateCompletedTrialData(nameOfIMU, "WalkingSpeed", averageStrideSpeed);
                    trial.updateCompletedTrialData(nameOfIMU, "Cadence", averageCadence);
                    trial.updateCompletedTrialData(nameOfIMU, "HeelStrikeCycleCount", stepCounter);

                    //Update the User Interface to show that the trial is complete
                    userInterface.updateIMUDataOutput(nameOfIMU, "DONE");

                    //Update the Data Log File with the Latest Data Packet
                    dotLogFile.getDotLogger().update(last3Samples[1]);
                    dotLogFile.getDotLogger().update(last3Samples[0]);

                    strides.add(currentStride);

                }
                break;

        }

        //Increase the sample counter by 1
        sampleCounter++;

    }

    protected static class SpeedCalculationFromFootIMU implements Runnable{

        private final FootStrideData strideToBeProcessed, previousStride, nextStride;

        protected final SpeedReturn speedReturn;

        protected interface SpeedReturn{

            void onSpeedCalculated(double[] strideLengthAndSpeed);

        }

        public SpeedCalculationFromFootIMU(FootStrideData strideToBeProcessed, FootStrideData previousStride, FootStrideData nextStride, SpeedReturn speedReturn){

            this.strideToBeProcessed = strideToBeProcessed;
            this.previousStride = previousStride;
            this.nextStride = nextStride;
            this.speedReturn = speedReturn;

        }

        public static void calculateSpeed(FootStrideData strideToBeProcessed, FootStrideData previousStride, FootStrideData nextStride, SpeedReturn speedReturn){

            SpeedCalculationFromFootIMU speedCalculationFromFootIMU = new SpeedCalculationFromFootIMU(strideToBeProcessed, previousStride, nextStride, speedReturn);
            Thread thread = new Thread(speedCalculationFromFootIMU);
            thread.start();

        }

        @Override
        public void run(){

            previousStride.extractDesiredSampleData();
            strideToBeProcessed.extractDesiredSampleData();
            nextStride.extractDesiredSampleData();

            double[] strideLengthAndSpeed = strideToBeProcessed.getStrideSpeed(previousStride, nextStride);

            speedReturn.onSpeedCalculated(strideLengthAndSpeed);

        }


    }
}
