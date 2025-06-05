package com.umainebiomechanicslab.biomechanicslabapp.studymanagers;

import android.content.Context;
import android.util.Log;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.imus.RecordingIMU;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LoadingWindowUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterfaceWithRecordingIMU;

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
        for (RecordingIMU IMU : recordingIMUArrayList) {
            if (IMU.getNameOfIMU().equals(nameOfIMU)) {
                IMU.startRecordingExport();
                return;
            }
        }
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

        if(nameOfIMU.equals("Left Arm")){

            //Start the export of Right Arm Data after Left Arm Export Is Complete
            startRecordingExport("Right Arm");

        }
        else{

            //After the Right Arm Export Is Complete, update the UI
            userInterfaceWithRecordingIMU.onRecordingExportComplete();

        }

    }
}
