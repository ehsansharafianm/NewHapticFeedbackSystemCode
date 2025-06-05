package com.umainebiomechanicslab.biomechanicslabapp.targetmanagers;

import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.OptimizedThighExtensionStudyManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.OptimizedThighExtensionStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.OptimizedThighExtensionStudyUI;

import java.util.Arrays;
import java.util.Locale;

public class ThighExtensionStudyOptimizedTargetManager {

    private final String TAG = "ThighExtensionStudyOptimizedTargetManager";

    //Declare Study Manager and UI objects
    private final OptimizedThighExtensionStudyManager optimizedThighExtensionStudyManager;
    private final OptimizedThighExtensionStudyUI optimizedThighExtensionStudyUI;

    //Declare File Manager
    private final FileManager fileManager;

    //Declare Millisecond Durations For Feedback
    public final int FRONT_FEEDBACK_DURATION_MS = 2000;
    public final int BACK_FEEDBACK_DURATION_MS = 500;

    //Variables for Target Creation/Change
    private double leftPeakThighAngleTarget, rightPeakThighAngleTarget, initialLeftPeakThighAngleTarget, initialRightPeakThighAngleTarget;
    private final double PEAK_THIGH_ANGLE_TARGET_INCREMENT = -2;
    private double leftCadenceTarget, rightCadenceTarget;

    //Create arrays to store the 20 most recent peak thigh angles for each side of the body
    private final double[] leftLast20PeakThighAngles, rightLast20PeakThighAngles;

    //Create arrays to store the cadences of the 20 most recent strides for each side of the body
    private final double[] leftLast20Cadences, rightLast20Cadences;

    //Create an integer to store the most recent sample at which point front feedback was given
    private int lastFrontFeedbackSampleNumber;

    public ThighExtensionStudyOptimizedTargetManager(OptimizedThighExtensionStudyManager optimizedThighExtensionStudyManager, OptimizedThighExtensionStudyUI optimizedThighExtensionStudyUI, FileManager fileManager){

        this.optimizedThighExtensionStudyManager = optimizedThighExtensionStudyManager;
        this.optimizedThighExtensionStudyUI = optimizedThighExtensionStudyUI;
        this.fileManager = fileManager;

        //Initialize leftLast20PeakThighAngles to have 20 values, and set them all to 0 at the beginning
        leftLast20PeakThighAngles = new double[20];
        Arrays.fill(leftLast20PeakThighAngles, 0);

        //Initialize rightLast20PeakThighAngles to have 20 values, and set them all to 0 at the beginning
        rightLast20PeakThighAngles = new double[20];
        Arrays.fill(rightLast20PeakThighAngles, 0);

        //Initialize leftLast20Cadences to have 20 values, and set them all to 0 at the beginning
        leftLast20Cadences = new double[20];
        Arrays.fill(leftLast20Cadences, 0);

        //Initialize rightLast20Cadences to have 20 values, and set them all to 0 at the beginning
        rightLast20Cadences = new double[20];
        Arrays.fill(rightLast20Cadences, 0);

        //Set the frontFeedbackSampleNumber to 0
        lastFrontFeedbackSampleNumber = 0;

    }

    public boolean onPTEAngleDetected(double angle, String nameOfIMU, int sampleNumber, int sampleFrequency, OptimizedThighExtensionStudyTrial trial){

        boolean feedbackGiven = false;

        //Calculate the number of samples that need to pass after front feedback was given
        int numSamplesToWaitAfterFrontFeedback = (int) ((double)FRONT_FEEDBACK_DURATION_MS / 1000 * sampleFrequency);

        if(nameOfIMU.equals("Left Thigh IMU")){

            //If trial is not null, Log the current target angle to the Thigh Extension Study Trial
            if(trial != null){
                trial.appendToGaitParameterArrayList(nameOfIMU, "TargetAngle", leftPeakThighAngleTarget);
            }

            //Check to see if the angle doesn't meet the target angle (not negative enough)
            if(angle > leftPeakThighAngleTarget && (sampleNumber - lastFrontFeedbackSampleNumber) > numSamplesToWaitAfterFrontFeedback){

                //Send back feedback to the user
                optimizedThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + BACK_FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;
            }

            //Advance all values of the leftLast20PeakThighAngles array one space (dropping the last one)
            for(int i = (leftLast20PeakThighAngles.length-1); i > 0; i--){
                leftLast20PeakThighAngles[i] = leftLast20PeakThighAngles[i-1];
            }

            //Set index 0 of leftLast20PeakThighAngles array to newAngle
            leftLast20PeakThighAngles[0] = angle;

        }
        else if(nameOfIMU.equals("Right Thigh IMU")){

            //If trial is not null, Log the current target angle to the Thigh Extension Study Trial
            if(trial != null){
                trial.appendToGaitParameterArrayList(nameOfIMU, "TargetAngle", rightPeakThighAngleTarget);
            }

            //Check to see if the angle doesn't meet the target angle (not negative enough)
            if(angle > rightPeakThighAngleTarget && (sampleNumber - lastFrontFeedbackSampleNumber) > numSamplesToWaitAfterFrontFeedback){

                //Send back feedback to the user
                optimizedThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + BACK_FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;
            }

            //Advance all values of the rightLast20PeakThighAngles array one space (dropping the last one)
            for(int i = (rightLast20PeakThighAngles.length-1); i > 0; i--){
                rightLast20PeakThighAngles[i] = rightLast20PeakThighAngles[i-1];
            }

            //Set index 0 of rightLast20PeakThighAngles array to newAngle
            rightLast20PeakThighAngles[0] = angle;

        }
        else{
            Log.e(TAG, "onPTEAngleDetected: Invalid IMU Name");
        }

        //If the trial name is Optimized Feedback, check if the target should be changed
        if(trial != null && trial.getTrialName().equals("Optimization Feedback")){

            //Check if the target should be changed based on this new PTE (only if the trial is "Optimization Feedback"))
            checkIfTargetShouldChange(nameOfIMU, sampleNumber);
        }

        //Return if feedback was given or not
        return feedbackGiven;

    }

    public void onHeelStrikeDetected(double cadence, String nameOfIMU){

        if(nameOfIMU.equals("Left Foot IMU")){

            //Advance all values of the leftLast20Cadences array one space (dropping the last one)
            for(int i = (leftLast20Cadences.length-1); i > 0; i--){
                leftLast20Cadences[i] = leftLast20Cadences[i-1];
            }

            //Set index 0 of leftLast20Cadences array to cadence
            leftLast20Cadences[0] = cadence;

        }
        else if(nameOfIMU.equals("Right Thigh IMU")){

            //Advance all values of the rightLast20Cadences array one space (dropping the last one)
            for(int i = (rightLast20Cadences.length-1); i > 0; i--){
                rightLast20Cadences[i] = rightLast20Cadences[i-1];
            }

            //Set index 0 of rightLast20Cadences array to cadence
            rightLast20Cadences[0] = cadence;

        }
        else{
            Log.e(TAG, "onHeelStrikeDetected: Invalid IMU Name");
        }
    }

    private void checkIfTargetShouldChange(String nameOfIMU, int sampleNumber){

        int numberOfGoodSteps = 0;
        double averagePeakThighAngle, sum40PeakThighAngles = 0;
        double averageCadence, sum40Cadences = 0;

        //Target should not change after 7 minutes (needs to remain constant for final minute)
        if(sampleNumber > 25200){
            return;
        }

        //Cycle through all of the last 20 left strides to sum up cadences for calculating average
        for(double leftLast20Cadence : leftLast20Cadences) {

            //If any of the values in the arrays are still 0 (meaning the minimum number of steps is not reached), the target cannot be updated
            if (leftLast20Cadence == 0) {
                return;
            }

            sum40Cadences += leftLast20Cadence;
        }

        //Cycle through all of the last 20 right strides to sum up cadences for calculating average
        for(double rightLast20Cadence : rightLast20Cadences) {

            //If any of the values in the arrays are still 0 (meaning the minimum number of steps is not reached), the target cannot be updated
            if (rightLast20Cadence == 0) {
                return;
            }

            sum40Cadences += rightLast20Cadence;
        }

        averageCadence = sum40Cadences/(leftLast20Cadences.length + rightLast20Cadences.length);

        //Cycle through all of the last 20 left strides to count how many met the PTE target and calculate average PTE
        for(double leftLast20PeakThighAngle : leftLast20PeakThighAngles) {

            //Only bother comparing PTE to target if the nameOfIMU is Left Thigh IMU
            if(nameOfIMU.equals("Left Thigh IMU")){

                //If any of the values in the arrays are still 0 (meaning the minimum number of steps is not reached), the target cannot be updated
                if (leftLast20PeakThighAngle == 0) {
                    return;
                } else if (leftLast20PeakThighAngle <= leftPeakThighAngleTarget) {
                    numberOfGoodSteps++;
                }

            }

            sum40PeakThighAngles += leftLast20PeakThighAngle;

        }

        //Cycle through all of the last 20 right strides to count how many met the PTE target and calculate average PTE
        for(double rightLast20PeakThighAngle : rightLast20PeakThighAngles) {

            //Only bother comparing PTE to target if the nameOfIMU is Right Thigh IMU
            if(nameOfIMU.equals("Right Thigh IMU")){

                //If any of the values in the arrays are still 0 (meaning the minimum number of steps is not reached), the target cannot be updated
                if (rightLast20PeakThighAngle == 0) {
                    return;
                } else if (rightLast20PeakThighAngle <= rightPeakThighAngleTarget) {
                    numberOfGoodSteps++;
                }

            }

            sum40PeakThighAngles += rightLast20PeakThighAngle;

        }

        averagePeakThighAngle = sum40PeakThighAngles/(leftLast20PeakThighAngles.length + rightLast20PeakThighAngles.length);

        //If the average cadence is below the target and averagePeakThighAngle is still above the initialTarget, the PTE target must be reduced
        if(averageCadence < Math.min(leftCadenceTarget, rightCadenceTarget)){

            if(averagePeakThighAngle < Math.min(initialLeftPeakThighAngleTarget, initialRightPeakThighAngleTarget)){

                resetLast20StepsArray("Left Thigh IMU");
                resetLast20StepsArray("Right Thigh IMU");

                //The target will only decrease if it is already above the initial target
                if((leftPeakThighAngleTarget < initialLeftPeakThighAngleTarget) && (rightPeakThighAngleTarget < initialRightPeakThighAngleTarget)) {

                    //Decrease the target by the increase increment
                    leftPeakThighAngleTarget -= PEAK_THIGH_ANGLE_TARGET_INCREMENT;
                    rightPeakThighAngleTarget -= PEAK_THIGH_ANGLE_TARGET_INCREMENT;

                    //Send front feedback to the user
                    optimizedThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FRONT_FEEDBACK_DURATION_MS);
                    optimizedThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FRONT_FEEDBACK_DURATION_MS);
                    fileManager.writeToLogFile("Front Feedback Provided At Sample Number " + sampleNumber);

                    //Update lastFrontFeedbackSampleNumber to the current sample number
                    lastFrontFeedbackSampleNumber = sampleNumber;

                    //Update the log with the change in target
                    fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: " + sampleNumber);
                    fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: " + sampleNumber);

                    //Update the UI with the change in target
                    optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
                    optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));

                }

            }
            return;

        }

        if(nameOfIMU.equals("Left Thigh IMU")){

            //If 80% or more of the steps meet the criteria of being "good," then increase the target
            if((double)numberOfGoodSteps/leftLast20PeakThighAngles.length >= 0.8){

                //Reset the leftLast20PeakThighAngles array to 0
                resetLast20StepsArray(nameOfIMU);

                //Increase the target by the increase increment
                leftPeakThighAngleTarget += PEAK_THIGH_ANGLE_TARGET_INCREMENT;

                //Update the log with the change in target
                fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: " + sampleNumber);

                //Update the UI with the change in target
                optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
            }

            //If 20% or less of the steps meet the criteria of being "good," then decrease the target
            else if((double)numberOfGoodSteps/leftLast20PeakThighAngles.length <= 0.2){

                //Reset the leftLast20PeakThighAngles array to 0
                resetLast20StepsArray(nameOfIMU);

                //The target will only decrease if it is already above the initial target
                if(leftPeakThighAngleTarget < initialLeftPeakThighAngleTarget) {

                    //Decrease the target by the increase increment
                    leftPeakThighAngleTarget -= PEAK_THIGH_ANGLE_TARGET_INCREMENT;

                    //Update the log with the change in target
                    fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: " + sampleNumber);

                    //Update the UI with the change in target
                    optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
                }

            }

        }

        else if(nameOfIMU.equals("Right Thigh IMU")){

            //If 80% or more of the steps meet the criteria of being "good," then increase the target
            if((double)numberOfGoodSteps/rightLast20PeakThighAngles.length >= 0.8){

                //Reset the rightLast20PeakThighAngles array to 0
                resetLast20StepsArray(nameOfIMU);

                //Increase the target by the increase increment
                rightPeakThighAngleTarget += PEAK_THIGH_ANGLE_TARGET_INCREMENT;

                //Update the log with the change in target
                fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: " + sampleNumber);

                //Update the UI with the change in target
                optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));
            }

            //If 20% or less of the steps meet the criteria of being "good," then decrease the target
            else if((double)numberOfGoodSteps/rightLast20PeakThighAngles.length <= 0.2){

                //Reset the rightLast20PeakThighAngles array to 0
                resetLast20StepsArray(nameOfIMU);

                //The target will only decrease if it is already above the initial target
                if(rightPeakThighAngleTarget < initialRightPeakThighAngleTarget) {

                    //Decrease the target by the increase increment
                    rightPeakThighAngleTarget -= PEAK_THIGH_ANGLE_TARGET_INCREMENT;

                    //Update the log with the change in target
                    fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: " + sampleNumber);

                    //Update the UI with the change in target
                    optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));
                }

            }

        }
        else{
            Log.e(TAG, "checkIfTargetShouldChange: Invalid IMU Name");
        }

    }

    public void resetLast20StepsArray(String nameOfIMU){

        switch (nameOfIMU) {
            case "Left Thigh IMU":
                //Reset the leftLast20PeakThighAngles array to 0
                Arrays.fill(leftLast20PeakThighAngles, 0);
                break;
            case "Right Thigh IMU":
                //Reset the rightLast20PeakThighAngles array to 0
                Arrays.fill(rightLast20PeakThighAngles, 0);
                break;
            case "Left Foot IMU":
                //Reset the leftLast20Cadences array to 0
                Arrays.fill(leftLast20Cadences, 0);
                break;
            case "Right Foot IMU":
                //Reset the rightLast20Cadences array to 0
                Arrays.fill(rightLast20Cadences, 0);
                break;
            default:
                Log.e(TAG, "resetLast20StepsArray: Invalid IMU Name");
                break;
        }

    }

    public void resetTarget(){

        //Reset the target values to their initial values and the lastFrontFeedbackSampleNumber to 0
        leftPeakThighAngleTarget = initialLeftPeakThighAngleTarget;
        rightPeakThighAngleTarget = initialRightPeakThighAngleTarget;
        lastFrontFeedbackSampleNumber = 0;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Left Cadence Target Set To: " + leftCadenceTarget);
        fileManager.writeToLogFile("Right Cadence Target Set To: " + rightCadenceTarget);

        //Update the UI with the generated target values
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetCadence", "Left Foot IMU", String.format(Locale.US,"%.3f",leftCadenceTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetCadence", "Right Foot IMU", String.format(Locale.US,"%.3f",rightCadenceTarget));
    }

    public void generatePeakThighTarget(OptimizedThighExtensionStudyTrial baselineTrial){

        double leftBaselineAverage, rightBaselineAverage;

        leftBaselineAverage = baselineTrial.getLeftPeakThighAngleAverage();
        rightBaselineAverage = baselineTrial.getRightPeakThighAngleAverage();
        leftCadenceTarget = 0.85*baselineTrial.getLeftAverageCadence();
        rightCadenceTarget = 0.85*baselineTrial.getRightAverageCadence();

        leftPeakThighAngleTarget = leftBaselineAverage + PEAK_THIGH_ANGLE_TARGET_INCREMENT;
        initialLeftPeakThighAngleTarget = leftPeakThighAngleTarget;

        rightPeakThighAngleTarget = rightBaselineAverage + PEAK_THIGH_ANGLE_TARGET_INCREMENT;
        initialRightPeakThighAngleTarget = rightPeakThighAngleTarget;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Left Cadence Target Set To: " + leftCadenceTarget);
        fileManager.writeToLogFile("Right Cadence Target Set To: " + rightCadenceTarget);

        //Update the UI with the generated target values
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetCadence", "Left Foot IMU", String.format(Locale.US,"%.3f",leftCadenceTarget));
        optimizedThighExtensionStudyUI.updateGaitParameterOutput("TargetCadence", "Right Foot IMU", String.format(Locale.US,"%.3f",rightCadenceTarget));

    }

}
