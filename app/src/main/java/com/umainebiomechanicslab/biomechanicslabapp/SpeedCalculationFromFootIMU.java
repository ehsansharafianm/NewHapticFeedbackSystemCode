package com.umainebiomechanicslab.biomechanicslabapp;

public class SpeedCalculationFromFootIMU implements Runnable{

    private final FootStrideData strideToBeProcessed, previousStride, nextStride;

    private final SpeedReturn speedReturn;

    interface SpeedReturn{

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
