package com.umainebiomechanicslab.biomechanicslabapp.targetmanagers;

import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.ArmAngleStudyManager;
import com.umainebiomechanicslab.biomechanicslabapp.trials.ArmAngleStudyTrial;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ArmAngleStudyUI;

import java.util.Locale;

public class ArmAngleStudyTargetManager {

    private final String TAG = "ArmAngleStudyTargetManager";

    //Declare Study Manager and UI objects
    private final ArmAngleStudyManager armAngleStudyManager;
    private final ArmAngleStudyUI armAngleStudyUI;

    //Declare File Manager
    private final FileManager fileManager;

    //Declare Millisecond Durations For Feedback
    public final int FEEDBACK_DURATION_MS = 500;

    //Variables for Target Creation/Change
    private double armExtensionTarget100, armFlexionTarget100, armExtensionTarget200, armFlexionTarget200;


    public ArmAngleStudyTargetManager(ArmAngleStudyManager armAngleStudyManager, ArmAngleStudyUI armAngleStudyUI, FileManager fileManager){

        this.armAngleStudyManager = armAngleStudyManager;
        this.armAngleStudyUI = armAngleStudyUI;
        this.fileManager = fileManager;

    }

    public boolean onPAEAngleDetected(double angle, String nameOfIMU, String trialName){

        boolean feedbackGiven = false;
        double armExtensionTarget;

        if(trialName.contains("100%")){
            armExtensionTarget = armExtensionTarget100;
        }
        else if (trialName.contains("200%")){
            armExtensionTarget = armExtensionTarget200;
        }
        else{
            Log.e(TAG, "Invalid trial name: " + trialName);
            return false;
        }

        //Check to see if the angle is between midline axis and the lower target (not negative enough / needs to extend arm more backward)
        if(angle > armExtensionTarget){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Otherwise, the PAE angle is within the target range
        else{

            //If feedbackType is Positive, send back feedback
            if(trialName.contains("Positive")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Return if feedback was given or not
        return feedbackGiven;

    }

    public boolean onPAFAngleDetected(double angle, String nameOfIMU, String trialName){

        boolean feedbackGiven = false;
        double armFlexionTarget;

        if(trialName.contains("100%")){
            armFlexionTarget = armFlexionTarget100;
        }
        else if (trialName.contains("200%")){
            armFlexionTarget = armFlexionTarget200;
        }
        else{
            Log.e(TAG, "Invalid trial name: " + trialName);
            return false;
        }

        //Check to see if the angle is between midline axis and the lower target (not positive enough / needs to flex arm more forward)
        if(angle < armFlexionTarget){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send front feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Otherwise, the PAF angle is within the target range
        else{

            //If feedbackType is Positive, send back feedback
            if(trialName.contains("Positive")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Return if feedback was given or not
        return feedbackGiven;

    }

    public void generatePeakArmTargets(ArmAngleStudyTrial baselineTrial){

        double leftPeakArmFlexionAngleAverage, rightPeakArmFlexionAngleAverage;
        double leftPeakArmExtensionAngleAverage, rightPeakArmExtensionAngleAverage;

        leftPeakArmFlexionAngleAverage = baselineTrial.getLeftPeakArmFlexionAngleAverage();
        rightPeakArmFlexionAngleAverage = baselineTrial.getRightPeakArmFlexionAngleAverage();
        leftPeakArmExtensionAngleAverage = baselineTrial.getLeftPeakArmExtensionAngleAverage();
        rightPeakArmExtensionAngleAverage = baselineTrial.getRightPeakArmExtensionAngleAverage();

        //Find the greater of the two average arm flexion angles and set that as the peak arm baseline value
        double peakArmFlexionBaseline = Math.max(leftPeakArmFlexionAngleAverage, rightPeakArmFlexionAngleAverage);

        //Find the lesser (more negative) of the two average arm extension angles and set that as the peak arm baseline value
        double peakArmExtensionBaseline = Math.min(leftPeakArmExtensionAngleAverage, rightPeakArmExtensionAngleAverage);

        //Set targets for 100%
        armFlexionTarget100 = 2 * peakArmFlexionBaseline;
        armExtensionTarget100 = 2 * peakArmExtensionBaseline;

        //Set lower targets for 200%
        armFlexionTarget200 = 3 * peakArmFlexionBaseline;
        armExtensionTarget200 = 3 * peakArmExtensionBaseline;


        //Update the log with the generated target values
        fileManager.writeToLogFile("Peak Arm 50% Extension Lower Target Set To: " + armExtensionTarget100);
        fileManager.writeToLogFile("Peak Arm 50% Extension Upper Target Set To: " + armExtensionTarget50Upper);
        fileManager.writeToLogFile("Peak Arm 50% Flexion Lower Target Set To: " + armFlexionTarget100);
        fileManager.writeToLogFile("Peak Arm 50% Flexion Upper Target Set To: " + armFlexionTarget50Upper);
        fileManager.writeToLogFile("Peak Arm 100% Extension Lower Target Set To: " + armExtensionTarget200);
        fileManager.writeToLogFile("Peak Arm 100% Extension Upper Target Set To: N/A (" + armExtensionTarget100Upper + ")");
        fileManager.writeToLogFile("Peak Arm 100% Flexion Lower Target Set To: " + armFlexionTarget200);
        fileManager.writeToLogFile("Peak Arm 100% Flexion Upper Target Set To: N/A (" + armFlexionTarget100Upper + ")");

        //Update the UI with the generated target values
        armAngleStudyUI.updateGaitParameterOutput("Target50PAELower", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAELower", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAEUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAEUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFLower", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFLower", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAELower", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget200));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAELower", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget200));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAEUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAEUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFLower", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget200));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFLower", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget200));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Upper));

    }

}
