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
    private double armExtensionTarget50Lower, armFlexionTarget50Lower, armExtensionTarget100Lower, armFlexionTarget100Lower;
    private double armExtensionTarget50Upper, armFlexionTarget50Upper, armExtensionTarget100Upper, armFlexionTarget100Upper;

    public ArmAngleStudyTargetManager(ArmAngleStudyManager armAngleStudyManager, ArmAngleStudyUI armAngleStudyUI, FileManager fileManager){

        this.armAngleStudyManager = armAngleStudyManager;
        this.armAngleStudyUI = armAngleStudyUI;
        this.fileManager = fileManager;

    }

    public boolean onPAEAngleDetected(double angle, String nameOfIMU, String trialName){

        boolean feedbackGiven = false;
        double armExtensionTargetLower, armExtensionTargetUpper;

        if(trialName.contains("50%")){
            armExtensionTargetLower = armExtensionTarget50Lower;
            armExtensionTargetUpper = armExtensionTarget50Upper;
        }
        else if (trialName.contains("100%")){
            armExtensionTargetLower = armExtensionTarget100Lower;
            armExtensionTargetUpper = armExtensionTarget100Upper;
        }
        else{
            Log.e(TAG, "Invalid trial name: " + trialName);
            return false;
        }

        //Check to see if the angle is between midline axis and the lower target (not negative enough / needs to extend arm more backward)
        if(angle > armExtensionTargetLower){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }
        //Check to see if the angle is beyond the upper target (too negative / too much backwards extension)
        else if(angle < armExtensionTargetUpper){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send front feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FEEDBACK_DURATION_MS);

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
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }

        //Return if feedback was given or not
        return feedbackGiven;

    }

    public boolean onPAFAngleDetected(double angle, String nameOfIMU, String trialName){

        boolean feedbackGiven = false;
        double armFlexionTargetLower, armFlexionTargetUpper;

        if(trialName.contains("50%")){
            armFlexionTargetLower = armFlexionTarget50Lower;
            armFlexionTargetUpper = armFlexionTarget50Upper;
        }
        else if (trialName.contains("100%")){
            armFlexionTargetLower = armFlexionTarget100Lower;
            armFlexionTargetUpper = armFlexionTarget100Upper;
        }
        else{
            Log.e(TAG, "Invalid trial name: " + trialName);
            return false;
        }

        //Check to see if the angle is between midline axis and the lower target (not positive enough / needs to flex arm more forward)
        if(angle < armFlexionTargetLower){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send front feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "A?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }
        //Check to see if the angle is beyond the upper target (too positive / too much forward flexion)
        else if(angle > armFlexionTargetUpper){

            //If the trial name contains error
            if(trialName.contains("Error")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);

                //Set feedbackGiven to true
                feedbackGiven = true;

            }
        }
        //Otherwise, the PAF angle is within the target range
        else{

            //If feedbackType is Positive, send back feedback
            if(trialName.contains("Positive")){

                //Send back feedback to the user
                armAngleStudyManager.sendHapticFeedback(nameOfIMU, "B?delay=" + FEEDBACK_DURATION_MS);
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

        //Set upper and lower targets for 50% (40%-60%)
        armFlexionTarget50Lower = 1.4 * peakArmFlexionBaseline;
        armFlexionTarget50Upper = 1.6 * peakArmFlexionBaseline;
        armExtensionTarget50Lower = 1.4 * peakArmExtensionBaseline;
        armExtensionTarget50Upper = 1.6 * peakArmExtensionBaseline;

        //Set lower targets for 100% (this is actually 100%)
        armFlexionTarget100Lower = 2 * peakArmFlexionBaseline;
        armExtensionTarget100Lower = 2 * peakArmExtensionBaseline;

        //Set upper targets for 100% (we don't have a upper target, so this is just a really high number)
        armFlexionTarget100Upper = 100 * peakArmFlexionBaseline;
        armExtensionTarget100Upper = 100 * peakArmExtensionBaseline;

        //Update the log with the generated target values
        fileManager.writeToLogFile("Peak Arm 50% Extension Lower Target Set To: " + armExtensionTarget50Lower);
        fileManager.writeToLogFile("Peak Arm 50% Extension Upper Target Set To: " + armExtensionTarget50Upper);
        fileManager.writeToLogFile("Peak Arm 50% Flexion Lower Target Set To: " + armFlexionTarget50Lower);
        fileManager.writeToLogFile("Peak Arm 50% Flexion Upper Target Set To: " + armFlexionTarget50Upper);
        fileManager.writeToLogFile("Peak Arm 100% Extension Lower Target Set To: " + armExtensionTarget100Lower);
        fileManager.writeToLogFile("Peak Arm 100% Extension Upper Target Set To: N/A (" + armExtensionTarget100Upper + ")");
        fileManager.writeToLogFile("Peak Arm 100% Flexion Lower Target Set To: " + armFlexionTarget100Lower);
        fileManager.writeToLogFile("Peak Arm 100% Flexion Upper Target Set To: N/A (" + armFlexionTarget100Upper + ")");

        //Update the UI with the generated target values
        armAngleStudyUI.updateGaitParameterOutput("Target50PAELower", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAELower", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAEUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAEUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFLower", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFLower", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target50PAFUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget50Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAELower", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAELower", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAEUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAEUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armExtensionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFLower", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFLower", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Lower));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFUpper", "Left Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Upper));
        armAngleStudyUI.updateGaitParameterOutput("Target100PAFUpper", "Right Arm IMU", String.format(Locale.US,"%.3f",armFlexionTarget100Upper));

    }

}
