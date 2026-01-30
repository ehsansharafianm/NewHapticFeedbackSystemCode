package com.umainebiomechanicslab.gait_training_app.targetmanagers;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.studymanagers.Aim2ThighExtensionStudyManager;
import com.umainebiomechanicslab.gait_training_app.trials.Aim2ThighExtensionStudyTrial;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.Aim2ThighExtensionStudyUI;

import java.util.Arrays;
import java.util.Locale;

public class ThighExtensionStudyAim2TargetManager {

    //Declare Study Manager and UI objects
    private final Aim2ThighExtensionStudyManager aim2ThighExtensionStudyManager;
    private final Aim2ThighExtensionStudyUI aim2ThighExtensionStudyUI;

    //Declare File Manager
    private final FileManager fileManager;

    //Declare Millisecond Durations For Feedback
    public final int FEEDBACK_DURATION_MS = 500;

    //Variables for Target Creation/Change
    private double peakThighAngleTarget, lowPeakThighAngleTarget, highPeakThighAngleTarget;

    //Boolean value to store if the target should has been increased during this trial
    private boolean hasTargetIncreased;

    //Create arrays to store the 20 most recent peak thigh angles for each side of the body
    private final boolean[] last40Steps;

    public ThighExtensionStudyAim2TargetManager(Aim2ThighExtensionStudyManager aim2ThighExtensionStudyManager, Aim2ThighExtensionStudyUI aim2ThighExtensionStudyUI, FileManager fileManager){

        this.aim2ThighExtensionStudyManager = aim2ThighExtensionStudyManager;
        this.aim2ThighExtensionStudyUI = aim2ThighExtensionStudyUI;
        this.fileManager = fileManager;

        //Initialize last40Steps to have 40 values, and set them all to false at the beginning
        last40Steps = new boolean[40];
        Arrays.fill(last40Steps, false);

    }

    public boolean onPTEAngleDetected(double angle, String nameOfIMU, int sampleNumber, String feedbackType, Aim2ThighExtensionStudyTrial trial){

        boolean feedbackGiven = false;
        boolean targetMet = false;

        //If trial is not null, Log the current target angle to the Thigh Extension Study Trial
        if(trial != null){
            trial.appendToGaitParameterArrayList(nameOfIMU, "TargetAngle", peakThighAngleTarget);
        }

        //Check to see if the angle doesn't meet the target angle (not negative enough)
        if(angle > peakThighAngleTarget){

            //If feedbackType is Error, send back feedback
            if(feedbackType.contains("Error")){

                //Send back feedback to the user (to the leg associated with the IMU)
                aim2ThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }
        else{

            //Change targetMet to true
            targetMet = true;

            //If feedbackType is Positive, send back feedback
            if(feedbackType.contains("Positive")){

                //Send back feedback to the user (to the leg associated with the IMU)
                aim2ThighExtensionStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Advance all values of the last40Steps array one space (dropping the last one)
        for(int i = (last40Steps.length-1); i > 0; i--){
            last40Steps[i] = last40Steps[i-1];
        }

        //Set index 0 of leftLast20PeakThighAngles array to newAngle
        last40Steps[0] = targetMet;

        //If the trial name is exactly Error Feedback or Positive Feedback
        if(trial != null && (trial.getTrialName().equals("Error Feedback 1") ||
                trial.getTrialName().equals("Error Feedback 2") ||
                trial.getTrialName().equals("Error Feedback 3") ||
                trial.getTrialName().equals("Positive Feedback"))){

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

            //Change the target to the high target
            peakThighAngleTarget = highPeakThighAngleTarget;

            //Update the log with the change in target
            fileManager.writeToLogFile("Peak Thigh Angle Target Set To: " + peakThighAngleTarget + " At sample number: " + sampleNumber);

            //Update the UI with the change in target
            aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));
            aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));

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
        peakThighAngleTarget = lowPeakThighAngleTarget;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Peak Thigh Angle Target Set To: " + peakThighAngleTarget + " At sample number: 0");

        //Update the UI with the change in target
        aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));
        aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));
    }

    public void generatePeakThighTarget(Aim2ThighExtensionStudyTrial baselineTrial){

        double leftBaselineAverage, rightBaselineAverage;

        leftBaselineAverage = baselineTrial.getLeftPeakThighAngleAverage();
        rightBaselineAverage = baselineTrial.getRightPeakThighAngleAverage();

        //Calculate the target values
        lowPeakThighAngleTarget = 1.17 * (leftBaselineAverage + rightBaselineAverage)/2;
        highPeakThighAngleTarget = 1.34 * (leftBaselineAverage + rightBaselineAverage)/2;

        //Set the targets to the original values
        peakThighAngleTarget = lowPeakThighAngleTarget;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Peak Thigh Angle Target Set To: " + peakThighAngleTarget + " At sample number: 0");

        //Update the UI with the change in target
        aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Left Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));
        aim2ThighExtensionStudyUI.updateGaitParameterOutput("TargetAngle", "Right Thigh IMU", String.format(Locale.US,"%.3f",peakThighAngleTarget));

    }

}
