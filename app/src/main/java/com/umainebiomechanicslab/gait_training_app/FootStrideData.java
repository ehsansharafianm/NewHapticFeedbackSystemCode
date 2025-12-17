package com.umainebiomechanicslab.gait_training_app;

import android.util.Log;

import com.xsens.dot.android.sdk.events.DotData;

import java.util.ArrayList;


public class FootStrideData {

    double angleOffset;
    public ArrayList<DotData> strideIMUData;

    private final String sideOfBody;
    private final ArrayList<Double> footAngle;
    private final ArrayList<Double> xAcceleration;
    private final ArrayList<Double> yAcceleration;

    private int flatFootIndex;

    public FootStrideData(String sideOfBody, double angleOffset){

        strideIMUData = new ArrayList<>();

        this.angleOffset = angleOffset;
        footAngle = new ArrayList<>();
        xAcceleration = new ArrayList<>();
        yAcceleration = new ArrayList<>();

        this.sideOfBody = sideOfBody;

        //Set flatFootIndex to 0 as a placeholder
        flatFootIndex = 0;

    }

    public void appendStrideDataSample(DotData dotData){
        strideIMUData.add(dotData);
    }

    public void extractDesiredSampleData(){
        if((footAngle.size() != strideIMUData.size()) || (xAcceleration.size() != strideIMUData.size()) || (yAcceleration.size() != strideIMUData.size())){
            for(int i = 0; i<strideIMUData.size(); i++){
                footAngle.add(strideIMUData.get(i).getEuler()[0] - angleOffset);
                xAcceleration.add((double)strideIMUData.get(i).getFreeAcc()[0]);
                yAcceleration.add((double)strideIMUData.get(i).getFreeAcc()[1]);
            }
        }
    }

    public int getNumberOfSamplesInStride(){
        return footAngle.size();
    }

    public ArrayList<Double> getXAcceleration(){
        return xAcceleration;
    }

    public ArrayList<Double> getYAcceleration(){
        return yAcceleration;
    }

    public int getFlatFootIndex(){

        //If the flatFootIndex has not been set (ie is still 0), find the flatFootIndex before returning
        if (flatFootIndex == 0) {

            double smallestSum = 100;

            //Set the range on either side of a given angle that the method will check to see if there is a flat foot point at that angle index
            int flatFootWindowRange = 2;

            for (int i = flatFootWindowRange; i < footAngle.size() - flatFootWindowRange; i++) {

                //Set the absolute max value that a flatFootAngle can be. This makes it so mistakenly identify the heel strike or toe off points as flat foot points
                double flatFootAngleThreshold = 10.0;

                if (Math.abs(footAngle.get(i)) < flatFootAngleThreshold) {
                    double currentSum = 0;
                    currentSum += Math.abs(footAngle.get(i) - footAngle.get(i - 2));
                    currentSum += Math.abs(footAngle.get(i) - footAngle.get(i - 1));
                    currentSum += Math.abs(footAngle.get(i) - footAngle.get(i + 1));
                    currentSum += Math.abs(footAngle.get(i) - footAngle.get(i + 2));

                    if (currentSum < smallestSum) {
                        smallestSum = currentSum;
                        flatFootIndex = i;

                    }
                }
            }
        }

        return flatFootIndex;

    }

    public double[] getStrideSpeed(FootStrideData previousStride, FootStrideData nextStride){

        /*
         * Calculating the stride speed for a given stride requires data from the previous stride and the next stride.
         * This allows for resetting the velocity to 0 at the flat foot index of the previous stride, current stride,
         * and next stride to prevent compounding error in the velocity.
         */

        //ArrayList for all the X Acceleration Values from the previous stride's flat foot index to the current stride's flat foot index
        ArrayList<Double> firstFlatFootSegmentAccX = new ArrayList<>();

        //ArrayList for all the X Acceleration Values from the current stride's flat foot index to the next stride's flat foot index
        ArrayList<Double> secondFlatFootSegmentAccX = new ArrayList<>();

        //ArrayList for all the Y Acceleration Values from the previous stride's flat foot index to the current stride's flat foot index
        ArrayList<Double> firstFlatFootSegmentAccY = new ArrayList<>();

        //ArrayList for all the Y Acceleration Values from the current stride's flat foot index to the next stride's flat foot index
        ArrayList<Double> secondFlatFootSegmentAccY = new ArrayList<>();

        //Extract the X and Y acceleration data starting at the previous stride's flat foot index until the end of the previous
        //stride and add it to the appropriate Array List for the first flat foot segment
        for(int i = previousStride.getFlatFootIndex(); i < previousStride.getNumberOfSamplesInStride(); i++){
            firstFlatFootSegmentAccX.add(previousStride.getXAcceleration().get(i));
            firstFlatFootSegmentAccY.add(previousStride.getYAcceleration().get(i));
        }

        //Extract the X and Y acceleration data starting at the beginning of the current stride until the current stride's flat
        //foot index and add it to the appropriate Array List for the first flat foot segment
        for(int i = 1; i <= this.getFlatFootIndex(); i++){
            firstFlatFootSegmentAccX.add(this.getXAcceleration().get(i));
            firstFlatFootSegmentAccY.add(this.getYAcceleration().get(i));
        }

        //Extract the X and Y acceleration data starting at the current stride's flat foot index until the end of the current
        //stride and add it to the appropriate Array List for the second flat foot segment
        for(int i = this.getFlatFootIndex(); i < this.getNumberOfSamplesInStride(); i++){
            secondFlatFootSegmentAccX.add(this.getXAcceleration().get(i));
            secondFlatFootSegmentAccY.add(this.getYAcceleration().get(i));
        }

        //Extract the X and Y acceleration data starting at the beginning of the next stride until the next stride's flat
        //foot index and add it to the appropriate Array List for the second flat foot segment
        for(int i = 1; i <= nextStride.getFlatFootIndex(); i++){
            secondFlatFootSegmentAccX.add(nextStride.getXAcceleration().get(i));
            secondFlatFootSegmentAccY.add(nextStride.getYAcceleration().get(i));
        }

        Log.d(sideOfBody+ "SpeedCalc", "Acceleration Extracted");
        Log.d(sideOfBody+ "SpeedCalc", "First Seg Size: " + firstFlatFootSegmentAccX.size());
        Log.d(sideOfBody+ "SpeedCalc", "Second Seg Size: " + secondFlatFootSegmentAccX.size());

        //ArrayList for all the X Velocity Values from the previous stride's flat foot index to the current stride's flat foot index
        ArrayList<Double> firstFlatFootSegmentVelX = new ArrayList<>();

        //ArrayList for all the Y Velocity Values from the previous stride's flat foot index to the current stride's flat foot index
        ArrayList<Double> firstFlatFootSegmentVelY = new ArrayList<>();

        //Since the first Velocity is at a flat foot index, it must be 0
        firstFlatFootSegmentVelX.add(0.0);
        firstFlatFootSegmentVelY.add(0.0);

        //For the entire segment of Acceleration data, take the average acceleration of two consecutive points, multiply it by
        //the time between the two points to find the change in velocity between the two points. Add that to the sum of all
        //previous changes in velocity to get the velocity at that point. Add that to the velocity ArrayList
        for(int i = 1; i < firstFlatFootSegmentAccX.size(); i++){
            double averageAccelerationX = ((firstFlatFootSegmentAccX.get(i)+firstFlatFootSegmentAccX.get(i-1))/2)*(1.0/60);
            double averageAccelerationY = ((firstFlatFootSegmentAccY.get(i)+firstFlatFootSegmentAccY.get(i-1))/2)*(1.0/60);
            firstFlatFootSegmentVelX.add(firstFlatFootSegmentVelX.get(i-1) + averageAccelerationX);
            firstFlatFootSegmentVelY.add(firstFlatFootSegmentVelY.get(i-1) + averageAccelerationY);
        }

        //The final velocity of the ArrayList should also be 0 given that this is another flat foot index. To adjust for the
        //numerical integration error, we take the actual value of the the final velocity, and subtract a proportion of that from
        //each calculated velocity to account for error. This means that the full amount is subtracted from the final velocity
        //value, half that amount is subtracted from the middle value, nothing is subtracted from the first value, and so on.
        double firstHalfVelocitiesOffsetFactorX = firstFlatFootSegmentVelX.get(firstFlatFootSegmentVelX.size()-1);
        double firstHalfVelocitiesOffsetFactorY = firstFlatFootSegmentVelY.get(firstFlatFootSegmentVelY.size()-1);
        for(int i = 0; i < firstFlatFootSegmentAccX.size(); i++){
            firstFlatFootSegmentVelX.set(i, (firstFlatFootSegmentVelX.get(i)-((i/(firstFlatFootSegmentVelX.size()-1.0))*firstHalfVelocitiesOffsetFactorX)));
            firstFlatFootSegmentVelY.set(i, (firstFlatFootSegmentVelY.get(i)-((i/(firstFlatFootSegmentVelY.size()-1.0))*firstHalfVelocitiesOffsetFactorY)));
        }

        //ArrayList for all the X Velocity Values from the current stride's flat foot index to the next stride's flat foot index
        ArrayList<Double> secondFlatFootSegmentVelX = new ArrayList<>();

        //ArrayList for all the Y Velocity Values from the current stride's flat foot index to the next stride's flat foot index
        ArrayList<Double> secondFlatFootSegmentVelY = new ArrayList<>();

        //Since the first Velocity is at a flat foot index, it must be 0
        secondFlatFootSegmentVelX.add(0.0);
        secondFlatFootSegmentVelY.add(0.0);

        //For the entire segment of Acceleration data, take the average acceleration of two consecutive points, multiply it by
        //the time between the two points to find the change in velocity between the two points. Add that to the sum of all
        //previous changes in velocity to get the velocity at that point. Add that to the velocity ArrayList
        for(int i = 1; i < secondFlatFootSegmentAccX.size(); i++){
            double averageAccelerationX = ((secondFlatFootSegmentAccX.get(i)+secondFlatFootSegmentAccX.get(i-1))/2)*(1.0/60);
            double averageAccelerationY = ((secondFlatFootSegmentAccY.get(i)+secondFlatFootSegmentAccY.get(i-1))/2)*(1.0/60);
            secondFlatFootSegmentVelX.add(secondFlatFootSegmentVelX.get(i-1) + averageAccelerationX);
            secondFlatFootSegmentVelY.add(secondFlatFootSegmentVelY.get(i-1) + averageAccelerationY);
        }

        //The final velocity of the ArrayList should also be 0 given that this is another flat foot index. To adjust for the
        //numerical integration error, we take the actual value of the the final velocity, and subtract a proportion of that from
        //each calculated velocity to account for error. This means that the full amount is subtracted from the final velocity
        //value, half that amount is subtracted from the middle value, nothing is subtracted from the first value, and so on.
        double secondHalfVelocitiesOffsetFactorX = secondFlatFootSegmentVelX.get(secondFlatFootSegmentVelX.size()-1);
        double secondHalfVelocitiesOffsetFactorY = secondFlatFootSegmentVelY.get(secondFlatFootSegmentVelY.size()-1);
        for(int i = 0; i < secondFlatFootSegmentAccX.size(); i++){
            secondFlatFootSegmentVelX.set(i, (secondFlatFootSegmentVelX.get(i)-((i/(secondFlatFootSegmentVelX.size()-1.0))*secondHalfVelocitiesOffsetFactorX)));
            secondFlatFootSegmentVelY.set(i, (secondFlatFootSegmentVelY.get(i)-((i/(secondFlatFootSegmentVelY.size()-1.0))*secondHalfVelocitiesOffsetFactorY)));
        }

        Log.d("SpeedCalc", "Velocity Extracted");

        //ArrayList that will store all the calculated X velocities for the current stride
        ArrayList<Double> currentStrideVelX = new ArrayList<>();

        //ArrayList that will store all the calculated Y velocities for the current stride
        ArrayList<Double> currentStrideVelY = new ArrayList<>();

        //Extract the X and Y velocities that belong to the current stride from the first flat foot segment
        for(int i = (previousStride.getNumberOfSamplesInStride()-previousStride.getFlatFootIndex()-1); i<firstFlatFootSegmentAccX.size(); i++){
            currentStrideVelX.add(firstFlatFootSegmentVelX.get(i));
            currentStrideVelY.add(firstFlatFootSegmentVelY.get(i));
        }

        //Extract the X and Y velocities that belong to the current stride from the second flat foot segment
        for(int i = 1; i<(this.getNumberOfSamplesInStride()-this.getFlatFootIndex()); i++){
            currentStrideVelX.add(secondFlatFootSegmentVelX.get(i));
            currentStrideVelY.add(secondFlatFootSegmentVelY.get(i));
        }

        //Sum up the area under the x and y velocity curves to get the distance traveled in the x and y direction
        double currentStridePosX = 0, currentStridePosY = 0;
        for(int i = 1; i<currentStrideVelX.size(); i++){
            currentStridePosX += ((currentStrideVelX.get(i)+currentStrideVelX.get(i-1))/2)*(1.0/60);
            currentStridePosY += ((currentStrideVelY.get(i)+currentStrideVelY.get(i-1))/2)*(1.0/60);
        }

        double[] returnValues = new double[2];

        //Calculate the magnitude of the distance traveled from the X and Y Position Values (sqrt(x^2+y^2))
        double strideDistance = Math.sqrt(Math.pow(currentStridePosX, 2) + Math.pow(currentStridePosY, 2));
        Log.d(sideOfBody+ "SpeedCalc", "Stride Length: "+strideDistance);

        //Return The Stride Distance Divided By The Time Between Heel Strikes
        Log.d(sideOfBody+ "SpeedCalc", "Speed: " + (strideDistance/((this.getNumberOfSamplesInStride()-1)/60.0)));
        double strideSpeed = strideDistance/((this.getNumberOfSamplesInStride()-1)/60.0);

        returnValues[0] = strideDistance;
        returnValues[1] = strideSpeed;

        return returnValues;

    }

}



