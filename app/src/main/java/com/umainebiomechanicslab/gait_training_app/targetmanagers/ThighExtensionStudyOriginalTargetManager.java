package com.umainebiomechanicslab.gait_training_app.targetmanagers;

import android.util.Log;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.studymanagers.OriginalThighExtensionStudyManager;
import com.umainebiomechanicslab.gait_training_app.trials.OriginalThighExtensionStudyTrial;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.OriginalThighExtensionStudyUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ThighExtensionStudyOriginalTargetManager {

    private final String TAG = "ThighExtensionStudyOriginalTargetManager";

    //Declare Study Manager and UI objects
    private final OriginalThighExtensionStudyManager originalThighExtensionStudyManager;
    private final OriginalThighExtensionStudyUI originalThighExtensionStudyUI;

    //Declare File Manager
    private final FileManager fileManager;

    //Declare Millisecond Durations For Feedback
    public final int FEEDBACK_DURATION_MS = 500;

    //Variables for Target Creation/Change
    private double leftPeakThighAngleTarget, rightPeakThighAngleTarget, initialLeftPeakThighAngleTarget, initialRightPeakThighAngleTarget;
    private double leftPeakThighAngleTargetIncreaseIncrement, rightPeakThighAngleTargetIncreaseIncrement;

    //Boolean value to store if the target should has been increased during this trial
    private boolean hasTargetIncreased;

    //Create arrays to store the 20 most recent peak thigh angles for each side of the body
    private final boolean[] last40Steps;

    public ThighExtensionStudyOriginalTargetManager(OriginalThighExtensionStudyManager originalThighExtensionStudyManager, OriginalThighExtensionStudyUI originalThighExtensionStudyUI, FileManager fileManager){

        this.originalThighExtensionStudyManager = originalThighExtensionStudyManager;
        this.originalThighExtensionStudyUI = originalThighExtensionStudyUI;
        this.fileManager = fileManager;

        //Initialize last40Steps to have 40 values, and set them all to false at the beginning
        last40Steps = new boolean[40];
        Arrays.fill(last40Steps, false);

    }

    public boolean onPTEAngleDetected(double angle, String nameOfIMU, int sampleNumber, String feedbackType, OriginalThighExtensionStudyTrial trial){

        boolean feedbackGiven = false;
        boolean targetMet = false;

        if(nameOfIMU.equals("Left Thigh IMU")){

            //If trial is not null, Log the current target angle to the Thigh Extension Study Trial
            if(trial != null){
                trial.appendToGaitParameterArrayList(nameOfIMU, "TargetAngle", leftPeakThighAngleTarget);
            }

            //Check to see if the angle doesn't meet the target angle (not negative enough)
            if(angle > leftPeakThighAngleTarget){

                //If feedbackType is Error, send back feedback
                if(feedbackType.contains("Error")){

                    //Send back feedback to the user
                    originalThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                    //Set feedbackGiven to true
                    feedbackGiven = true;

                }
            }
            else{

                //Change targetMet to true
                targetMet = true;

                //If feedbackType is Positive, send back feedback
                if(feedbackType.contains("Positive")){

                    //Send back feedback to the user
                    originalThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                    //Set feedbackGiven to true
                    feedbackGiven = true;

                }
            }
        }
        else if(nameOfIMU.equals("Right Thigh IMU")){

            //If trial is not null, Log the current target angle to the Thigh Extension Study Trial
            if(trial != null){
                trial.appendToGaitParameterArrayList(nameOfIMU, "TargetAngle", rightPeakThighAngleTarget);
            }

            //Check to see if the angle doesn't meet the target angle (not negative enough)
            if(angle > rightPeakThighAngleTarget){

                //If feedbackType is Error, send back feedback
                if(feedbackType.contains("Error")){

                    //Send back feedback to the user
                    originalThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                    //Set feedbackGiven to true
                    feedbackGiven = true;

                }
            }
            else{

                //Change targetMet to true
                targetMet = true;

                //If feedbackType is Positive, send back feedback
                if(feedbackType.contains("Positive")){

                    //Send back feedback to the user
                    originalThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                    //Set feedbackGiven to true
                    feedbackGiven = true;

                }
            }

        }
        else{
            Log.e(TAG, "onPTEAngleDetected: Invalid IMU Name");
        }

        //Advance all values of the leftLast20PeakThighAngles array one space (dropping the last one)
        for(int i = (last40Steps.length-1); i > 0; i--){
            last40Steps[i] = last40Steps[i-1];
        }

        //Set index 0 of leftLast20PeakThighAngles array to newAngle
        last40Steps[0] = targetMet;

        //If the trial name is exactly Error Feedback or Positive Feedback
        if(trial != null && (trial.getTrialName().equals("Error Feedback") || trial.getTrialName().equals("Positive Feedback"))){

            //Check if the target should be changed based on this new PTE
            checkIfTargetShouldChange(sampleNumber);
        }

        //Return if feedback was given or not
        return feedbackGiven;

    }

    private void checkIfTargetShouldChange(int sampleNumber){

        if(hasTargetIncreased){
            return;
        }

        int numberOfGoodSteps = 0;

        //Cycle through all of the last 40 steps (20 on each side) to count how many met the PTE target
        for(boolean last40Step : last40Steps) {

            //If the step was good, increase number of good steps
            if(last40Step) {
                numberOfGoodSteps++;
            }

        }

        if((double)numberOfGoodSteps/last40Steps.length >= 0.8){

            //Increase the target by the increase increment
            leftPeakThighAngleTarget += leftPeakThighAngleTargetIncreaseIncrement;
            rightPeakThighAngleTarget += rightPeakThighAngleTargetIncreaseIncrement;

            //Update the log with the change in target
            fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: " + sampleNumber);
            fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: " + sampleNumber);

            //Update the UI with the change in target
            originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
            originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));

            //Set hasTargetIncreased to true
            hasTargetIncreased = true;

        }

    }

    public void resetLast40StepsArray(){
        Arrays.fill(last40Steps, false);
        hasTargetIncreased = false;
    }

    public void resetTarget(){

        //Set the targets to the original values
        leftPeakThighAngleTarget = initialLeftPeakThighAngleTarget;
        rightPeakThighAngleTarget = initialRightPeakThighAngleTarget;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: 0");
        fileManager.writeToLogFile("Left Peak Thigh Angle Target Increment Set To: " + leftPeakThighAngleTargetIncreaseIncrement);
        fileManager.writeToLogFile("Right Peak Thigh Angle Target Increment Set To: " + rightPeakThighAngleTargetIncreaseIncrement);

        //Update the UI with the change in target
        originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
        originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));
    }

    public void generatePeakThighTarget(ArrayList<OriginalThighExtensionStudyTrial> trialArrayList){

        double leftBaselineAverage = 0, rightBaselineAverage = 0, leftFastAverage = 0, rightFastAverage = 0;

        //Loop through all trials to find the last baseline and fast trials
        for(OriginalThighExtensionStudyTrial trial : trialArrayList){

            //Get the last baseline trial angle average (overriding old baseline trials if there are any)
            if(trial.getTrialName().equals("Baseline Normal")){
                leftBaselineAverage = trial.getLeftPeakThighAngleAverage();
                rightBaselineAverage = trial.getRightPeakThighAngleAverage();
            }
            //Get the last fast trial angle average (overriding old fast trials if there are any)
            else if(trial.getTrialName().equals("Fast")){
                leftFastAverage = trial.getLeftPeakThighAngleAverage();
                rightFastAverage = trial.getRightPeakThighAngleAverage();
            }

        }

        if((leftBaselineAverage != 0) && (rightBaselineAverage != 0) && (leftFastAverage != 0) && (rightFastAverage != 0)){

            leftPeakThighAngleTargetIncreaseIncrement = 0.5 * (leftFastAverage - leftBaselineAverage);
            if (leftPeakThighAngleTargetIncreaseIncrement > -2.0){
                leftPeakThighAngleTargetIncreaseIncrement = -2.0;
            }
            leftPeakThighAngleTarget = leftBaselineAverage + leftPeakThighAngleTargetIncreaseIncrement;
            initialLeftPeakThighAngleTarget = leftPeakThighAngleTarget;

            rightPeakThighAngleTargetIncreaseIncrement = 0.5 * (rightFastAverage - rightBaselineAverage);
            if (rightPeakThighAngleTargetIncreaseIncrement > -2.0){
                rightPeakThighAngleTargetIncreaseIncrement = -2.0;
            }
            rightPeakThighAngleTarget = rightBaselineAverage + rightPeakThighAngleTargetIncreaseIncrement;
            initialRightPeakThighAngleTarget = rightPeakThighAngleTarget;

            //Update the log with the generated target values
            fileManager.writeToLogFile("Left Peak Thigh Angle Target Set To: " + leftPeakThighAngleTarget + " At sample number: 0");
            fileManager.writeToLogFile("Right Peak Thigh Angle Target Set To: " + rightPeakThighAngleTarget + " At sample number: 0");
            fileManager.writeToLogFile("Left Peak Thigh Angle Target Increment Set To: " + leftPeakThighAngleTargetIncreaseIncrement);
            fileManager.writeToLogFile("Right Peak Thigh Angle Target Increment Set To: " + rightPeakThighAngleTargetIncreaseIncrement);

            //Update the UI with the change in target
            originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",leftPeakThighAngleTarget));
            originalThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",rightPeakThighAngleTarget));

            hasTargetIncreased = false;

        }

        else{
            fileManager.writeToLogFile("Not enough trial data to generate Old Target");
            originalThighExtensionStudyUI.errorMessagePopUp("Old Target Error");
        }

    }

}
