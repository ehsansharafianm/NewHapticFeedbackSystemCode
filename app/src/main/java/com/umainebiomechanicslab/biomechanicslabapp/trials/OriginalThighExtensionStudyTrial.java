package com.umainebiomechanicslab.biomechanicslabapp.trials;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OriginalThighExtensionStudyTrial extends Trial{

    private static final String TAG = "ThighExtensionStudyTrial";

    //Variables to store the full-trial gait parameters (averages/totals)
    private int leftPeakThighExtensions, rightPeakThighExtensions, leftHeelStrikes, rightHeelStrikes;
    private double leftPeakThighAngleAverage, rightPeakThighAngleAverage;
    private double leftAverageSpeed, rightAverageSpeed;
    private double leftAverageStrideLength, rightAverageStrideLength;

    //ArrayLists to store the individual gait parameters for the trial
    private final ArrayList<String> leftPeakThighAngles, leftPeakThighAngleTarget, leftStrideLength, leftSpeed, leftCadence;
    private final ArrayList<String> rightPeakThighAngles, rightPeakThighAngleTarget, rightStrideLength, rightSpeed, rightCadence;

    public OriginalThighExtensionStudyTrial(String trialName, String trialTimeStamp) {
        super(trialName, trialTimeStamp);

        //Initializing ArrayLists to store the gait parameters for the trial
        leftPeakThighAngles = new ArrayList<>();
        leftPeakThighAngleTarget = new ArrayList<>();
        leftStrideLength = new ArrayList<>();
        leftSpeed = new ArrayList<>();
        leftCadence = new ArrayList<>();
        rightPeakThighAngles = new ArrayList<>();
        rightPeakThighAngleTarget = new ArrayList<>();
        rightStrideLength = new ArrayList<>();
        rightSpeed = new ArrayList<>();
        rightCadence = new ArrayList<>();

    }

    public double getLeftPeakThighAngleAverage(){
        return leftPeakThighAngleAverage;
    }

    public double getRightPeakThighAngleAverage(){
        return rightPeakThighAngleAverage;
    }


    public String getCommaSeparatedTrialData(){

        return trialName + "," + trialTimeStamp + "," + leftPeakThighExtensions + "," + leftPeakThighAngleAverage + "," + leftHeelStrikes +
                "," + leftAverageStrideLength + "," + leftAverageSpeed + "," + rightPeakThighExtensions + "," +
                rightPeakThighAngleAverage + "," + rightHeelStrikes +  "," + rightAverageStrideLength + "," + rightAverageSpeed;

    }

    @Override
    public ArrayList<FileManager.DotLogFile> getDotLogFiles() {
        return null;
    }

    public void appendToGaitParameterArrayList(String nameOfIMU, String gaitParameter, double data){

        //Update the IMU Gait Parameter Output on the Screen based on the name of the IMU
        switch (gaitParameter){
            case "PTE":
                //If nameOfIMU is Left Thigh, update the Left Last Stride PTE
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    leftPeakThighAngles.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Thigh, update the Right Last Stride PTE
                else if(nameOfIMU.equals("Right Thigh IMU")){
                    rightPeakThighAngles.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for PTE Update");
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
            case "TargetAngle":
                //If nameOfIMU is Left Thigh, update the Left Thigh Target Angle
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    leftPeakThighAngleTarget.add(String.valueOf(data));
                }
                //If nameOfIMU is Right Thigh, update the Right Thigh Target Angle
                else if(nameOfIMU.equals("Right Thigh IMU")) {
                    rightPeakThighAngleTarget.add(String.valueOf(data));
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "appendToGaitParameterArrayList", "ERROR: Invalid IMU Name for Target Angle Update");
                }
                break;
        }

    }

    public void updateCompletedTrialData(String nameOfIMU, String gaitParameter, double data){

        //Update the IMU Gait Parameter Output on the Screen based on the name of the IMU
        switch (gaitParameter){
            case "PTE":
                //If nameOfIMU is Left Thigh, update the Left Last Stride PTE
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    leftPeakThighAngleAverage = data;
                }
                //If nameOfIMU is Right Thigh, update the Right Last Stride PTE
                else if(nameOfIMU.equals("Right Thigh IMU")){
                    rightPeakThighAngleAverage = data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PTE Update");
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
            case "PTECycleCount":
                //If nameOfIMU is Left Thigh, update the Left Thigh Target Angle
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    leftPeakThighExtensions = (int) data;
                }
                //If nameOfIMU is Right Thigh, update the Right Thigh Target Angle
                else if(nameOfIMU.equals("Right Thigh IMU")) {
                    rightPeakThighExtensions = (int) data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for PTE Cycle Count Update");
                }
                break;
            case "HeelStrikeCycleCount":
                //If nameOfIMU is Left Foot, update the Left Target Cadence
                if(nameOfIMU.equals("Left Foot IMU")) {
                    leftHeelStrikes = (int) data;
                }
                //If nameOfIMU is Right Foot, update the Right Target Cadence
                else if(nameOfIMU.equals("Right Foot IMU")){
                    rightHeelStrikes = (int) data;
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateCompletedTrialData", "ERROR: Invalid IMU Name for Heel Strike Cycle Count Update");
                }
        }

    }

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

            if(i<leftPeakThighAngles.size()){
                entry = entry + leftPeakThighAngles.get(i) + ",";
                if(trialName.contains("Feedback")){
                    entry = entry + leftPeakThighAngleTarget.get(i) + ",";
                }
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

            if(i<rightPeakThighAngles.size()){
                entry = entry + rightPeakThighAngles.get(i) + ",";
                if(trialName.contains("Feedback")){
                    entry = entry + rightPeakThighAngleTarget.get(i) + ",";
                }
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

        if(leftPeakThighAngles.size() > largestArraySize){
            largestArraySize = leftPeakThighAngles.size();
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

        if(rightPeakThighAngles.size() > largestArraySize){
            largestArraySize = rightPeakThighAngles.size();
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
