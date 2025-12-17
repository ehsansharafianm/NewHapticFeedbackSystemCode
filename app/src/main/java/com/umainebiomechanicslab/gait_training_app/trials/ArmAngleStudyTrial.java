package com.umainebiomechanicslab.gait_training_app.trials;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.UserInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ArmAngleStudyTrial extends Trial{

    private static final String TAG = "ArmAngleStudyTrial";

    //Variables to store the full-trial gait parameters (averages/totals)
    private int leftArmAngleExtensionCount, rightArmAngleExtensionCount, leftArmAngleFlexionCount, rightArmAngleFlexionCount, leftHeelStrikeCount, rightHeelStrikeCount;
    private double leftPeakArmExtensionAngleAverage, rightPeakArmExtensionAngleAverage, leftPeakArmFlexionAngleAverage, rightPeakArmFlexionAngleAverage;
    private double leftAverageSpeed, rightAverageSpeed;
    private double leftAverageStrideLength, rightAverageStrideLength;
    private double leftAverageCadence, rightAverageCadence;

    //ArrayLists to store the individual gait parameters for the trial
    private final ArrayList<String> leftPeakArmFlexionAngles, leftPeakArmExtensionAngles, leftStrideLength, leftSpeed, leftCadence;
    private final ArrayList<String> rightPeakArmFlexionAngles, rightPeakArmExtensionAngles, rightStrideLength, rightSpeed, rightCadence;

    public ArmAngleStudyTrial(String trialName, String trialTimeStamp) {
        super(trialName, trialTimeStamp);

        //Initializing ArrayLists to store the gait parameters for the trial
        leftPeakArmFlexionAngles = new ArrayList<>();
        leftPeakArmExtensionAngles = new ArrayList<>();
        leftStrideLength = new ArrayList<>();
        leftSpeed = new ArrayList<>();
        leftCadence = new ArrayList<>();
        rightPeakArmFlexionAngles = new ArrayList<>();
        rightPeakArmExtensionAngles = new ArrayList<>();
        rightStrideLength = new ArrayList<>();
        rightSpeed = new ArrayList<>();
        rightCadence = new ArrayList<>();

    }

    public double getLeftPeakArmFlexionAngleAverage(){
        return leftPeakArmFlexionAngleAverage;
    }

    public double getRightPeakArmFlexionAngleAverage(){
        return rightPeakArmFlexionAngleAverage;
    }

    public double getLeftPeakArmExtensionAngleAverage(){
        return leftPeakArmExtensionAngleAverage;
    }

    public double getRightPeakArmExtensionAngleAverage(){
        return rightPeakArmExtensionAngleAverage;
    }

    @Override
    public String getCommaSeparatedTrialData(){

        return trialName + "," + trialTimeStamp + "," + leftArmAngleExtensionCount + "," + leftPeakArmExtensionAngleAverage +
                "," + leftArmAngleFlexionCount + "," + leftPeakArmFlexionAngleAverage + "," + leftHeelStrikeCount +
                "," + leftAverageStrideLength + "," + leftAverageSpeed + "," + leftAverageCadence +
                "," + rightArmAngleExtensionCount + "," + rightPeakArmExtensionAngleAverage +
                "," + rightArmAngleFlexionCount + "," + rightPeakArmFlexionAngleAverage + "," + rightHeelStrikeCount +
                "," + rightAverageStrideLength + "," + rightAverageSpeed + "," + rightAverageCadence;

    }

    @Override
    public ArrayList<FileManager.DotLogFile> getDotLogFiles() {
        return null;
    }

    @Override
    public void appendToGaitParameterArrayList(String nameOfIMU, String gaitParameter, double data){

        //Update the IMU Gait Parameter Output on the Screen based on the name of the IMU
        switch (gaitParameter){
            case "PAE":
                //If nameOfIMU is Left Arm, update the Left Last Stride PAE
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftPeakArmExtensionAngles.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Arm, update the Right Last Stride PAE
                else if(nameOfIMU.equals("Right Arm IMU")){
                    rightPeakArmExtensionAngles.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for PAE Update");
                }
                break;
            case "PAF":
                //If nameOfIMU is Left Arm, update the Left Last Stride PAF
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftPeakArmFlexionAngles.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Arm, update the Right Last Stride PAF
                else if(nameOfIMU.equals("Right Arm IMU")){
                    rightPeakArmFlexionAngles.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for PAF Update");
                }
                break;
            case "StrideLength":
                //If nameOfIMU is Left Foot, update the Left Stride Length
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftStrideLength.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Foot, update the Right Stride Length
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightStrideLength.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for Stride Length Update");
                }
                break;
            case "WalkingSpeed":
                //If nameOfIMU is Left Foot, update the Left Walking Speed
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftSpeed.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Foot, update the Right Walking Speed
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightSpeed.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for Walking Speed Update");
                }
                break;
            case "Cadence":
                //If nameOfIMU is Left Foot, update the Left Cadence
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftCadence.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Foot, update the Right Cadence
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightCadence.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for Cadence Update");
                }
                break;
        }

    }

    @Override
    public void updateCompletedTrialData(String nameOfIMU, String gaitParameter, double data){

        //Update the IMU Gait Parameter Output on the Screen based on the name of the IMU
        switch (gaitParameter){
            case "PAE":
                //If nameOfIMU is Left Arm, update the Left Last Stride PAE
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftPeakArmExtensionAngleAverage = data;
                }
                //If nameOfIMU is Right Arm, update the Right Last Stride PAE
                else if(nameOfIMU.equals("Right Arm IMU")){
                    rightPeakArmExtensionAngleAverage = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PAE Update");
                }
                break;
            case "PAF":
                //If nameOfIMU is Left Arm, update the Left Last Stride PAF
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftPeakArmFlexionAngleAverage = data;
                }
                //If nameOfIMU is Right Arm, update the Right Last Stride PAF
                else if(nameOfIMU.equals("Right Arm IMU")){
                    rightPeakArmFlexionAngleAverage = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PAF Update");
                }
                break;
            case "StrideLength":
                //If nameOfIMU is Left Foot, update the Left Stride Length
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftAverageStrideLength = data;
                }
                //If nameOfIMU is Right Foot, update the Right Stride Length
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightAverageStrideLength = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for Stride Length Update");
                }
                break;
            case "WalkingSpeed":
                //If nameOfIMU is Left Foot, update the Left Walking Speed
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftAverageSpeed = data;
                }
                //If nameOfIMU is Right Foot, update the Right Walking Speed
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightAverageSpeed = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for Walking Speed Update");
                }
                break;
            case "Cadence":
                //If nameOfIMU is Left Foot, update the Left Cadence
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftAverageCadence = data;
                }
                //If nameOfIMU is Right Foot, update the Right Cadence
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightAverageCadence = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for Cadence Update");
                }
                break;
            case "PAECycleCount":
                //If nameOfIMU is Left Arm, update the Left Arm Extension Cycle Count
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftArmAngleExtensionCount = (int) data;
                }
                //If nameOfIMU is Right Arm, update the Right Arm Extension Cycle Count
                else if(nameOfIMU.equals("Right Arm IMU")) {
                    rightArmAngleExtensionCount = (int) data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PAE Cycle Count Update");
                }
                break;
            case "PAFCycleCount":
                //If nameOfIMU is Left Arm, update the Left Arm Extension Cycle Count
                if(nameOfIMU.equals("Left Arm IMU")) {
                    leftArmAngleFlexionCount = (int) data;
                }
                //If nameOfIMU is Right Arm, update the Right Arm Extension Cycle Count
                else if(nameOfIMU.equals("Right Arm IMU")) {
                    rightArmAngleFlexionCount = (int) data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PAF Cycle Count Update");
                }
                break;
            case "HeelStrikeCycleCount":
                //If nameOfIMU is Left Foot, update the Left Target Cadence
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftHeelStrikeCount = (int) data;
                }
                //If nameOfIMU is Right Foot, update the Right Target Cadence
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightHeelStrikeCount = (int) data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for Heel Strike Cycle Count Update");
                }
        }

    }

    @Override
    public File createTrialCSVFile(String subjectTitle, Context context, FileManager fileManager, UserInterface userInterface){
        FileWriter fileWriter = null;
        File csvFile;

        try {

            String logFileName = subjectTitle + " " + trialName + " " + trialTimeStamp + " GaitParameters.csv";

            // Define the file name and path
            //csvFile = new File(context.getApplicationContext().getExternalFilesDir("logs"), logFileName);
            csvFile = new File(context.getApplicationContext().getExternalFilesDir("SubjectData/" + subjectTitle + "/GaitParameterCSVs"), logFileName);


            // Create the file if it doesn't exist
            if (!csvFile.exists()) {
                boolean isFileCreated = csvFile.createNewFile();
                if (!isFileCreated) {
                    userInterface.errorMessagePopUp("Error Creating " + trialName + " CSV Data File");
                    return null;
                }
            }

            // Initialize FileWriter
            fileWriter = new FileWriter(csvFile);

            String columnTitles;
            if(trialName.contains("Feedback")){
                columnTitles = "Left PTE, Left Target, Left Stride Length, Left Speed, Left Cadence, Right PTE, Right Target, Right Stride Length, Right Speed, Right Cadence";
            }
            else{
                columnTitles = "Left PTE, Left Stride Length, Left Speed, Left Cadence, Right PTE, Right Stride Length, Right Speed, Right Cadence";
            }
            fileWriter.append(columnTitles);
            fileWriter.append("\n");

            //Get Tester Entries
            ArrayList<String> CSVStringEntries = createCSVStringEntries();

            // Write data to CSV
            for(int i = 0; i<CSVStringEntries.size(); i++){
                fileWriter.append(CSVStringEntries.get(i));
                fileWriter.append("\n");
            }

            fileManager.writeToLogFile("CSV Data File Created");

        } catch (IOException e) {
            Log.e(TAG, "Error creating " + trialName + " CSV Data File", e);
            userInterface.errorMessagePopUp("Error Creating " + trialName + " CSV Data File");
            return null;
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing file writer", e);
            }
        }

        return csvFile;

    }

    private ArrayList<String> createCSVStringEntries(){

        ArrayList<String> fileEntries = new ArrayList<>();
        int largestArraySize = getLargestArraySize();

        for(int i = 0; i < largestArraySize; i++){

            String entry = "";

            if(i<leftPeakArmFlexionAngles.size()){
                entry = entry + leftPeakArmFlexionAngles.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<leftPeakArmExtensionAngles.size()){
                entry = entry + leftPeakArmExtensionAngles.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<leftStrideLength.size()){
                entry = entry + leftStrideLength.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<leftSpeed.size()){
                entry = entry + leftSpeed.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<leftCadence.size()){
                entry = entry + leftCadence.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<rightPeakArmFlexionAngles.size()){
                entry = entry + rightPeakArmFlexionAngles.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<rightPeakArmExtensionAngles.size()){
                entry = entry + rightPeakArmExtensionAngles.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<rightStrideLength.size()){
                entry = entry + rightStrideLength.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<rightSpeed.size()){
                entry = entry + rightSpeed.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            if(i<rightCadence.size()){
                entry = entry + rightCadence.get(i) + ",";
            }
            else{
                entry = entry + " ,";
            }

            fileEntries.add(entry);

        }

        return fileEntries;

    }

    private int getLargestArraySize() {
        int largestArraySize = 0;

        if(leftPeakArmExtensionAngles.size() > largestArraySize){
            largestArraySize = leftPeakArmExtensionAngles.size();
        }

        if(leftPeakArmFlexionAngles.size() > largestArraySize){
            largestArraySize = leftPeakArmFlexionAngles.size();
        }

        if(leftStrideLength.size() > largestArraySize){
            largestArraySize = leftStrideLength.size();
        }

        if(leftSpeed.size() > largestArraySize){
            largestArraySize = leftSpeed.size();
        }

        if(leftCadence.size() > largestArraySize){
            largestArraySize = leftCadence.size();
        }

        if(rightPeakArmExtensionAngles.size() > largestArraySize){
            largestArraySize = rightPeakArmExtensionAngles.size();
        }

        if(rightPeakArmFlexionAngles.size() > largestArraySize){
            largestArraySize = rightPeakArmFlexionAngles.size();
        }

        if(rightStrideLength.size() > largestArraySize){
            largestArraySize = rightStrideLength.size();
        }

        if(rightSpeed.size() > largestArraySize){
            largestArraySize = rightSpeed.size();
        }

        if(rightCadence.size() > largestArraySize){
            largestArraySize = rightCadence.size();
        }
        return largestArraySize;
    }
}
