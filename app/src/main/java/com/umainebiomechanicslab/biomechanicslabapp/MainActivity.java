package com.umainebiomechanicslab.biomechanicslabapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.OptimizedThighExtensionStudyManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.OriginalThighExtensionStudyManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.RecordIMUDataManager;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.TestHapticCellsManager;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ExperimenterMenuUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.IMURecordingPageUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LoadingWindowUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.LogPopupWindowUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.OptimizedThighExtensionStudyUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.OriginalThighExtensionStudyUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.ParticipantMenuUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.StartPageUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.TestHapticCellsUI;
import com.umainebiomechanicslab.biomechanicslabapp.userinterfaces.UserInterface;

import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    //Declare FileManager
    private FileManager fileManager;

    private static final int PERMISSION_REQUEST_CODE = 100;

    //Declare the permission queue
    private Queue<String> permissionQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiate Log Popup Window
        LogPopupWindowUI logPopupWindowUI = new LogPopupWindowUI(this, R.id.log_popup_window);

        //Instantiate Loading Window
        LoadingWindowUI loadingWindowUI = new LoadingWindowUI(this, R.id.loading_page);

        //Instantiate FileManager
        fileManager = new FileManager(this, this, logPopupWindowUI, loadingWindowUI);

        //Instantiate UI Objects
        TestHapticCellsUI testHapticCellsUI = new TestHapticCellsUI(this, R.id.test_haptic_cells_page);
        OptimizedThighExtensionStudyUI optimizedThighExtensionStudyUI = new OptimizedThighExtensionStudyUI(this, R.id.optimized_thigh_extension_study_trial_page, logPopupWindowUI, fileManager);
        OriginalThighExtensionStudyUI originalThighExtensionStudyUI = new OriginalThighExtensionStudyUI(this, R.id.original_thigh_extension_study_trial_page, logPopupWindowUI, fileManager);
        IMURecordingPageUI imuRecordingPageUI = new IMURecordingPageUI(this, R.id.imu_recording_page, logPopupWindowUI, fileManager);
        ExperimenterMenuUI experimenterMenuUI = new ExperimenterMenuUI(this, R.id.experimenter_menu, imuRecordingPageUI, testHapticCellsUI, originalThighExtensionStudyUI, optimizedThighExtensionStudyUI);
        ParticipantMenuUI participantMenuUI = new ParticipantMenuUI(this, R.id.participant_menu);
        StartPageUI startPageUI = new StartPageUI(this, R.id.start_page, experimenterMenuUI, participantMenuUI);

        //Link User Interfaces
        optimizedThighExtensionStudyUI.linkUserInterfaceForBackButton(experimenterMenuUI);
        originalThighExtensionStudyUI.linkUserInterfaceForBackButton(experimenterMenuUI);
        testHapticCellsUI.linkUserInterfaceForBackButton(experimenterMenuUI);
        imuRecordingPageUI.linkUserInterfaceForBackButton(experimenterMenuUI);
        experimenterMenuUI.linkUserInterfaceForBackButton(startPageUI);
        participantMenuUI.linkUserInterfaceForBackButton(startPageUI);

        //Instantiate Trial Manager Objects
        TestHapticCellsManager testHapticCellsManager = new TestHapticCellsManager(testHapticCellsUI, experimenterMenuUI, loadingWindowUI, fileManager);
        RecordIMUDataManager recordIMUDataManager = new RecordIMUDataManager(imuRecordingPageUI, this, fileManager);
        OptimizedThighExtensionStudyManager optimizedThighExtensionStudyManager = new OptimizedThighExtensionStudyManager(optimizedThighExtensionStudyUI, experimenterMenuUI, loadingWindowUI, this, fileManager);
        OriginalThighExtensionStudyManager originalThighExtensionStudyManager = new OriginalThighExtensionStudyManager(originalThighExtensionStudyUI, experimenterMenuUI, loadingWindowUI, this, fileManager);


        //Link Trial Manager Objects
        testHapticCellsUI.linkTestHapticCellsManager(testHapticCellsManager);
        imuRecordingPageUI.linkIMUManager(recordIMUDataManager);
        optimizedThighExtensionStudyUI.linkIMUManager(optimizedThighExtensionStudyManager);
        originalThighExtensionStudyUI.linkIMUManager(originalThighExtensionStudyManager);

        //Show Start Page
        startPageUI.showPage();

        //Initialize the permission queue
        permissionQueue = new LinkedList<>();
        permissionQueue.add(android.Manifest.permission.BLUETOOTH);
        permissionQueue.add(android.Manifest.permission.BLUETOOTH_ADMIN);
        permissionQueue.add(android.Manifest.permission.BLUETOOTH_SCAN);
        permissionQueue.add(android.Manifest.permission.BLUETOOTH_ADVERTISE);
        permissionQueue.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        permissionQueue.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissionQueue.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionQueue.add(android.Manifest.permission.VIBRATE);
        permissionQueue.add(android.Manifest.permission.INTERNET);
        permissionQueue.add(android.Manifest.permission.ACCESS_WIFI_STATE);

        //Start requesting/checking permissions
        requestNextPermission();

    }

    @Override
    protected void onStart() {
        super.onStart();
        fileManager.checkGoogleDriveSignIn();
    }

    private void requestNextPermission() {
        if (!permissionQueue.isEmpty()) {
            String permission = permissionQueue.poll();
            assert permission != null;
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
            } else {
                //trialManager.writeToLogs(permission + " is already granted");
                fileManager.writeToLogFile(permission + " is already granted");

                //Request the next permission
                requestNextPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fileManager.writeToLogFile(permissions[0] + " permission granted");
            } else {
                fileManager.writeToLogFile(permissions[0] + " permission denied");
                UserInterface.textPopUp(permissions[0] + " permission denied", this);
            }

            //Request the next permission
            requestNextPermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FileManager.RC_SIGN_IN) {

            fileManager.onSignInResult(data);

        }
    }
}
