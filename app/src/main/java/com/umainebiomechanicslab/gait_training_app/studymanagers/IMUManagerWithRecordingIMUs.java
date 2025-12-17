package com.umainebiomechanicslab.gait_training_app.studymanagers;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.imus.RecordingIMU;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.LoadingWindowUI;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.UserInterfaceWithRecordingIMU;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public abstract class IMUManagerWithRecordingIMUs extends IMUManager{

    private final String TAG = "IMUManagerWithRecordingIMUs";

    private final LoadingWindowUI loadingWindowUI;

    private final UserInterfaceWithRecordingIMU userInterfaceWithRecordingIMU;

    protected final ArrayList<RecordingIMU> recordingIMUArrayList = new ArrayList<>();

    public IMUManagerWithRecordingIMUs(Context context, UserInterfaceWithRecordingIMU userInterfaceWithRecordingIMU, FileManager fileManager, LoadingWindowUI loadingWindowUI) {

        super(context, userInterfaceWithRecordingIMU, fileManager);

        this.userInterfaceWithRecordingIMU = userInterfaceWithRecordingIMU;

        this.loadingWindowUI = loadingWindowUI;
    }

    public void startRecordingExport(String nameOfIMU) {

        //Show the loading window
        loadingWindowUI.showPage();

        //Start the loading page
        loadingWindowUI.startLoadingPage("Exporting " + nameOfIMU + " Recorded Data...", new LoadingWindowUI.LoadingPageListener() {
            @Override
            public void onLoadingPageFinished() {
                userInterfaceWithRecordingIMU.showPage();
            }

            @Override
            public void onLoadingPageCancelled() {
                userInterfaceWithRecordingIMU.showPage();
            }
        });


        //Find the IMU with the given name in the recording IMUs ArrayList and start exporting its data
        fileManager.writeToLogFile("Searching for IMU with target name: '"+ nameOfIMU + "'");

        for (RecordingIMU IMU : recordingIMUArrayList) {
            String imuNameInList = IMU.getNameOfIMU();

            // Log the exact strings being compared
            fileManager.writeToLogFile("Comparing target '"+ nameOfIMU + "' with list item '" + imuNameInList + "'");

            // Use the robust comparison
            if (imuNameInList.trim().equalsIgnoreCase(nameOfIMU.trim())) {
                fileManager.writeToLogFile("SUCCESS: Match found! Starting export for " + imuNameInList);
                IMU.startRecordingExport();
                return; // Exit the function since we found our IMU
            }
        }

        // This log will only be written if the loop finishes without finding a match
        fileManager.writeToLogFile("ERROR: Loop finished. No matching IMU found for name '" + nameOfIMU + "'");


    }

    public void updateExportLoadingPage(int percentage, String progressComment){
        loadingWindowUI.updateInnerSpinnerText(percentage + "%");
        loadingWindowUI.updateLoadingCommentText(progressComment);
    }

    public void onRecordingExportComplete(String nameOfIMU){

        //Pause for 2 seconds
        try {
            TimeUnit.MILLISECONDS.sleep(2000);
        } catch (InterruptedException e) {
            Log.e(TAG, "uploadFilesToFirebaseCloudStorage", e);
        }

        loadingWindowUI.onLoadingComplete();
        // I changed here I added IMU
        if(nameOfIMU.equals("Left Arm IMU")){

            //Start the export of Right Arm Data after Left Arm Export Is Complete
            startRecordingExport("Right Arm IMU");

        }
        else{

            //After the Right Arm Export Is Complete, update the UI
            userInterfaceWithRecordingIMU.onRecordingExportComplete();

        }

    }
}
