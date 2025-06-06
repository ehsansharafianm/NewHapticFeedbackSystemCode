package com.umainebiomechanicslab.biomechanicslabapp.imus;

import android.content.Context;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.FootStrideData;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.IMUManager;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithIMU;
import com.xsens.dot.android.sdk.events.DotData;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class StreamingIMUWithFootDataAlgorithmForStrideCalculation extends StreamingIMU{

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
