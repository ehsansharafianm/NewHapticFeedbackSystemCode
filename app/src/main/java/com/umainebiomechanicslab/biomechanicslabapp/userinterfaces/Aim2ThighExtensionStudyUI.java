package com.umainebiomechanicslab.biomechanicslabapp.userinterfaces;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.umainebiomechanicslab.biomechanicslabapp.FileManager;
import com.umainebiomechanicslab.biomechanicslabapp.HapticControlModule;
import com.umainebiomechanicslab.biomechanicslabapp.R;
import com.umainebiomechanicslab.biomechanicslabapp.studymanagers.Aim2ThighExtensionStudyManager;

public class Aim2ThighExtensionStudyUI extends UserInterfaceWithRecordingIMU {

    private final String TAG = "ThighExtensionStudyUI";

    private Aim2ThighExtensionStudyManager imuManager;

    //Declare the haptic cell IP address Strings
    private String leftHapticCellIPAddress, rightHapticCellIPAddress;

    //Declare TextViews that need to be accessed outside of the constructor
    private final TextView leftHapticCellIPAddressTextView;
    private final TextView rightHapticCellIPAddressTextView;

    private boolean IMUsAngleOffsetInitialized;
    private boolean startTrialButtonActivated;
    private boolean logPopUpWindowVisible;
    private boolean validLeftHapticCellIPAddress, validRightHapticCellIPAddress;

    private boolean isLeftArmIMUSpinnerDefaultSelection = true;
    private boolean isRightArmIMUSpinnerDefaultSelection = true;
    private boolean isLeftThighIMUSpinnerDefaultSelection = true;
    private boolean isRightThighIMUSpinnerDefaultSelection = true;
    private boolean isLeftFootIMUSpinnerDefaultSelection = true;
    private boolean isRightFootIMUSpinnerDefaultSelection = true;

    private String trialName;

    public Aim2ThighExtensionStudyUI(Activity activity, int pageID, LogPopupWindowUI logPopupWindowUI, FileManager fileManager) {
        
        super(activity, pageID);

        //Initialize IMUsAngleOffsetInitialized
        IMUsAngleOffsetInitialized = false;

        //Initialize startTrialButtonActivated
        startTrialButtonActivated = false;

        //Initialize logPopUpWindowVisible
        logPopUpWindowVisible = false;

        //Initialize validLeftHapticCellIPAddress and validRightHapticCellIPAddress
        validLeftHapticCellIPAddress = false;
        validRightHapticCellIPAddress = false;

        //Set the default haptic cell IP addresses to 0.0.0.0
        leftHapticCellIPAddress = "0.0.0.0";
        rightHapticCellIPAddress = "0.0.0.0";

        //Link the device IP address text views
        leftHapticCellIPAddressTextView = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftDeviceIP);
        rightHapticCellIPAddressTextView = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightDeviceIP);

        //Declare UI Buttons
        goBackButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_GoBackButton);
        startScanButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_StartScanButton);
        startSyncButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_StartSyncButton);
        showBatteryPercentageButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_ShowIMUsBatteryButton);
        disconnectButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_DisconnectIMUsButton);
        Button leftThighFeedbackTestButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftThighFeedbackButton);
        Button rightThighFeedbackTestButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightThighFeedbackButton);
        startInitializationButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_StartInitializationButton);
        startTrialButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_StartTrialButton);
        showLogButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_ShowLogButton);
        uploadDataToCloudButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_UploadDataToCloudButton);
        exportRecordedDataButton = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_ExportRecordedDataButton);

        //Set enabled status of buttons that shouldn't be enabled at the start of the app
        updateButtonEnabledStatus(startScanButton, false);
        updateButtonEnabledStatus(startSyncButton, false);
        updateButtonEnabledStatus(disconnectButton, false);
        updateButtonEnabledStatus(startInitializationButton, false);
        updateButtonEnabledStatus(startTrialButton, false);
        updateButtonEnabledStatus(exportRecordedDataButton, false);
        updateButtonEnabledStatus(uploadDataToCloudButton, false);

        //Declare UI EditText
        EditText subjectNumberEditText = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_SubjectNumberEntryBox);
        EditText sessionNumberEditText = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_SessionNumberEntryBox);
        EditText leftHapticFeedbackModuleIPEditText = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftDeviceNumberEntryBox);
        EditText rightHapticFeedbackModuleIPEditText = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightDeviceNumberEntryBox);

        //Declare UI Dropdown Spinners
        Spinner leftArmIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftArmSpinner);
        Spinner rightArmIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightArmSpinner);
        Spinner leftThighIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftThighSpinner);
        Spinner rightThighIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightThighSpinner);
        Spinner leftFootIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_LeftFootSpinner);
        Spinner rightFootIMUSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_RightFootSpinner);
        Spinner trialModeSpinner = activity.findViewById(R.id.aim2_thigh_extension_study_trial_page_TrialModeSpinner);

        //Set the Dropdown Spinner Array Adapter
        ArrayAdapter<CharSequence> IMUMacAddresses = new ArrayAdapter<>(activity, R.layout.popup_window_for_dropdown_spinner, activity.getResources().getStringArray(R.array.IMU_MAC_Addresses)); // Use your layout for the trigger
        IMUMacAddresses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> Session1TrialModes = new ArrayAdapter<>(activity, R.layout.popup_window_for_dropdown_spinner, activity.getResources().getStringArray(R.array.Aim2ThighExtensionStudyFirstSessionTrialTypes)); // Use your layout for the trigger
        Session1TrialModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> TrainingSessionTrialModes = new ArrayAdapter<>(activity, R.layout.popup_window_for_dropdown_spinner, activity.getResources().getStringArray(R.array.Aim2ThighExtensionStudyTrainingSessionsTrialTypes)); // Use your layout for the trigger
        TrainingSessionTrialModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> RetentionSessionTrialModes = new ArrayAdapter<>(activity, R.layout.popup_window_for_dropdown_spinner, activity.getResources().getStringArray(R.array.Aim2ThighExtensionStudyRetentionSessionTrialTypes)); // Use your layout for the trigger
        RetentionSessionTrialModes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Set the Dropdown Spinners
        leftArmIMUSpinner.setAdapter(IMUMacAddresses);
        rightArmIMUSpinner.setAdapter(IMUMacAddresses);
        leftThighIMUSpinner.setAdapter(IMUMacAddresses);
        rightThighIMUSpinner.setAdapter(IMUMacAddresses);
        leftFootIMUSpinner.setAdapter(IMUMacAddresses);
        rightFootIMUSpinner.setAdapter(IMUMacAddresses);
        trialModeSpinner.setAdapter(Session1TrialModes);

        //============  Previous Listener ===================
        //Set the behavior for the subject number text box
        subjectNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Extract the subject number from the text box
                try{
                    int subjectNumber = Integer.parseInt(subjectNumberEditText.getText().toString());

                    //Rename the session folder (if there is an error, set validSubjectEntered to false)
                    //We're using a temp variable here because we don't want to change validSubjectEntered to true unless both subject and session numbers are valid
                    boolean tempValidSubjectEntered = fileManager.renameSessionFolder("Subject " + subjectNumber);

                    //If there is an error, show an error message
                    if(!tempValidSubjectEntered){
                        validSubjectEntered = false;
                        errorMessagePopUp("ERROR: Unable to rename session folder");
                        fileManager.writeToLogFile("ERROR: Unable to rename session folder! Please try again.");
                    }
                }
                catch (NumberFormatException e){
                    textPopUp("WARNING! Invalid Input");
                    validSubjectEntered = false;
                }
            }
        });

        //Set the behavior for the subject number text box
        sessionNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Extract the session number from the text box
                try{
                    int subjectNumber = Integer.parseInt(subjectNumberEditText.getText().toString());
                    int sessionNumber = Integer.parseInt(sessionNumberEditText.getText().toString());

                    if ((sessionNumber < 1) || (sessionNumber > 12)) {
                        validSubjectEntered = false;
                    }
                    else{

                        //Rename the session folder (if there is an error, set validSubjectEntered to false)
                        // validSubjectEntered = fileManager.renameSessionFolder("Subject " + subjectNumber + " Session " + sessionNumber);

                        // ================= New Way to Rename Session Folder ===================
                        //Create a unique timestamp
                        String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new java.util.Date());
                        //Rename the session folder using the subject, session, AND timestamp
                        validSubjectEntered = fileManager.renameSessionFolder("Subject " + subjectNumber + " Session " + sessionNumber + " (" + timeStamp + ")");
                        // =================                                    ===================

                        //Set the trial spinner dropdown for the given session
                        if(sessionNumber == 1){
                            trialModeSpinner.setAdapter(Session1TrialModes);
                        }
                        else if(sessionNumber == 12){
                            trialModeSpinner.setAdapter(RetentionSessionTrialModes);
                        }
                        else{
                            trialModeSpinner.setAdapter(TrainingSessionTrialModes);
                        }
                    }

                    //If there is an error, show an error message
                    if(!validSubjectEntered){
                        errorMessagePopUp("ERROR: Unable to rename session folder");
                        fileManager.writeToLogFile("ERROR: Unable to rename session folder! Please try again.");
                    }
                }
                catch (NumberFormatException e){
                    textPopUp("WARNING! Invalid Input");
                    validSubjectEntered = false;
                }
            }
        });



        //Set the Left Arm Dropdown Spinner Behavior
        leftArmIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isLeftArmIMUSpinnerDefaultSelection){
                    isLeftArmIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Left Arm IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the Right Arm Dropdown Spinner Behavior
        rightArmIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isRightArmIMUSpinnerDefaultSelection){
                    isRightArmIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Right Arm IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the Left Thigh Dropdown Spinner Behavior
        leftThighIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isLeftThighIMUSpinnerDefaultSelection){
                    isLeftThighIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Left Thigh IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the Right Thigh Dropdown Spinner Behavior
        rightThighIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isRightThighIMUSpinnerDefaultSelection){
                    isRightThighIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Right Thigh IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the Left Foot Dropdown Spinner Behavior
        leftFootIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isLeftFootIMUSpinnerDefaultSelection){
                    isLeftFootIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Left Foot IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the Right Foot Dropdown Spinner Behavior
        rightFootIMUSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isRightFootIMUSpinnerDefaultSelection){
                    isRightFootIMUSpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", disable the start scan button
                if (spinnerSelection.equals("--")) {
                    updateButtonEnabledStatus(startScanButton, false);
                }
                //Otherwise, if none of the other spinners are "--", enable the start scan button
                else if (!leftArmIMUSpinner.getSelectedItem().toString().equals("--") && !rightArmIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftThighIMUSpinner.getSelectedItem().toString().equals("--") && !rightThighIMUSpinner.getSelectedItem().toString().equals("--") &&
                        !leftFootIMUSpinner.getSelectedItem().toString().equals("--") && !rightFootIMUSpinner.getSelectedItem().toString().equals("--")) {
                    updateButtonEnabledStatus(startScanButton, true);
                }

                //Update the IMU MacAddress based on the IMU Code
                imuManager.updateIMUCode("Right Foot IMU", spinnerSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the behavior for the left haptic cell IP address text box
        leftHapticFeedbackModuleIPEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try{

                    //Extract the device number from the text box
                    int deviceNumber = Integer.parseInt(leftHapticFeedbackModuleIPEditText.getText().toString());

                    //Check to see if device number is valid (0-255)
                    if(deviceNumber >= 0 && deviceNumber <= 255){
                        imuManager.setDeviceNumber("Left", deviceNumber);

                        //Set validLeftHapticCellIPAddress to true
                        validLeftHapticCellIPAddress = true;

                    }
                    else{

                        //Show an error message
                        textPopUp("WARNING! Invalid Input");

                        //Set validLeftHapticCellIPAddress to false
                        validLeftHapticCellIPAddress = false;
                    }

                }
                catch (NumberFormatException e){

                    //Show an error message
                    textPopUp("WARNING! Invalid Input");

                    //Set validLeftHapticCellIPAddress to false
                    validLeftHapticCellIPAddress = false;

                }
            }
        });

        //Set the behavior for the right haptic cell IP address text box
        rightHapticFeedbackModuleIPEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try{

                    //Extract the device number from the text box
                    int deviceNumber = Integer.parseInt(rightHapticFeedbackModuleIPEditText.getText().toString());

                    //Check to see if device number is valid (0-255)
                    if(deviceNumber >= 0 && deviceNumber <= 255){
                        imuManager.setDeviceNumber("Right", deviceNumber);

                        //Set validRightHapticCellIPAddress to true
                        validRightHapticCellIPAddress = true;

                    }
                    else{

                        //Show an error message
                        textPopUp("WARNING! Invalid Input");

                        //Set validRightHapticCellIPAddress to false
                        validRightHapticCellIPAddress = false;
                    }

                }
                catch (NumberFormatException e){

                    //Show an error message
                    textPopUp("WARNING! Invalid Input");

                    //Set validRightHapticCellIPAddress to false
                    validRightHapticCellIPAddress = false;

                }
            }
        });

        //Set the behavior for the startScanButton
        startScanButton.setOnClickListener(view -> {

            //If any of the IMU Spinners match one another, show an error message
            if (leftArmIMUSpinner.getSelectedItem().toString().equals(rightArmIMUSpinner.getSelectedItem().toString()) ||
                    leftArmIMUSpinner.getSelectedItem().toString().equals(leftThighIMUSpinner.getSelectedItem().toString()) ||
                    leftArmIMUSpinner.getSelectedItem().toString().equals(rightThighIMUSpinner.getSelectedItem().toString()) ||
                    leftArmIMUSpinner.getSelectedItem().toString().equals(leftFootIMUSpinner.getSelectedItem().toString()) ||
                    leftArmIMUSpinner.getSelectedItem().toString().equals(rightFootIMUSpinner.getSelectedItem().toString()) ||
                    rightArmIMUSpinner.getSelectedItem().toString().equals(leftThighIMUSpinner.getSelectedItem().toString()) ||
                    rightArmIMUSpinner.getSelectedItem().toString().equals(rightThighIMUSpinner.getSelectedItem().toString()) ||
                    rightArmIMUSpinner.getSelectedItem().toString().equals(leftFootIMUSpinner.getSelectedItem().toString()) ||
                    rightArmIMUSpinner.getSelectedItem().toString().equals(rightFootIMUSpinner.getSelectedItem().toString()) ||
                    leftThighIMUSpinner.getSelectedItem().toString().equals(rightThighIMUSpinner.getSelectedItem().toString()) ||
                    leftThighIMUSpinner.getSelectedItem().toString().equals(leftFootIMUSpinner.getSelectedItem().toString()) ||
                    leftThighIMUSpinner.getSelectedItem().toString().equals(rightFootIMUSpinner.getSelectedItem().toString()) ||
                    rightThighIMUSpinner.getSelectedItem().toString().equals(leftFootIMUSpinner.getSelectedItem().toString()) ||
                    rightThighIMUSpinner.getSelectedItem().toString().equals(rightFootIMUSpinner.getSelectedItem().toString()) ||
                    leftFootIMUSpinner.getSelectedItem().toString().equals(rightFootIMUSpinner.getSelectedItem().toString())) {

                errorMessagePopUp("ERROR: Duplicate IMU Selected");

            }
            //Otherwise, continue with the scan process
            else{
                updateButtonText(startScanButton, "Scanning...");
                updateButtonEnabledStatus(startScanButton, false);
                updateButtonEnabledStatus(goBackButton, false);
                updateButtonEnabledStatus(disconnectButton, false);

                //Disable IMU Spinners
                updateSpinnerEnabledStatus(leftArmIMUSpinner, false);
                updateSpinnerEnabledStatus(rightArmIMUSpinner, false);
                updateSpinnerEnabledStatus(leftThighIMUSpinner, false);
                updateSpinnerEnabledStatus(rightThighIMUSpinner, false);
                updateSpinnerEnabledStatus(leftFootIMUSpinner, false);
                updateSpinnerEnabledStatus(rightFootIMUSpinner, false);

                //Set haveIMUsBeenScanned to true
                haveIMUsBeenScanned = true;

                //Start the scan process
                imuManager.startScan();
            }
        });

        //Set the behavior for the startSyncButton
        startSyncButton.setOnClickListener(view -> {

            updateButtonText(startSyncButton, "Syncing...");
            updateButtonEnabledStatus(startSyncButton, false);
            updateButtonEnabledStatus(goBackButton, false);
            updateButtonEnabledStatus(disconnectButton, false);

            imuManager.startSync();

        });

        //Set the behavior for the show showBatteryPercentageButton
        showBatteryPercentageButton.setOnClickListener(view -> {

            if(batteryPercentageVisible){
                batteryPercentageVisible = imuManager.showBatteryPercentage(false);
                updateButtonText(showBatteryPercentageButton, "Show Battery Percentage");
            }
            else{
                batteryPercentageVisible = imuManager.showBatteryPercentage(true);

                //Only change the button text if the battery percentage is successfully shown
                if(batteryPercentageVisible){
                    updateButtonText(showBatteryPercentageButton, "Hide Battery Percentage");
                }

            }

        });

        //Set the behavior for the disconnectButton
        disconnectButton.setOnClickListener(view -> {

            //Disconnect all IMUs
            imuManager.disconnectIMUs();

            //Update Enabled Status of UI Buttons
            onIMUsFullyConnected(false);

            //Enable IMU Spinners
            updateSpinnerEnabledStatus(leftArmIMUSpinner, true);
            updateSpinnerEnabledStatus(rightArmIMUSpinner, true);
            updateSpinnerEnabledStatus(leftThighIMUSpinner, true);
            updateSpinnerEnabledStatus(rightThighIMUSpinner, true);
            updateSpinnerEnabledStatus(leftFootIMUSpinner, true);
            updateSpinnerEnabledStatus(rightFootIMUSpinner, true);

            //Set haveIMUsBeenScanned to false
            haveIMUsBeenScanned = false;

        });

        leftThighFeedbackTestButton.setOnClickListener(view -> {

            //Check to see if both haptic cells have valid device numbers
            if(validLeftHapticCellIPAddress){
                imuManager.sendHapticFeedback("Left", "B?delay=500");
            }
            //Otherwise, show an error message
            else{
                errorMessagePopUp("ERROR: Invalid Device Number");
            }

        });

        rightThighFeedbackTestButton.setOnClickListener(view -> {

            //Check to see if both haptic cells have valid device numbers
            if(validRightHapticCellIPAddress){
                imuManager.sendHapticFeedback("Right", "B?delay=500");
            }
            //Otherwise, show an error message
            else{
                errorMessagePopUp("ERROR: Invalid Device Number");
            }

        });

        //Set the Trial Mode Dropdown Spinner Behavior
        trialModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //Extract the selected item and convert it to a string
                trialName = adapterView.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the behavior for the startInitializationButton
        startInitializationButton.setOnClickListener(view -> {

            //Update Initialization Button Characteristics
            updateButtonText(startInitializationButton, "Initializing...");
            updateButtonEnabledStatus(startInitializationButton, false);

            //Disable Certain Buttons
            updateButtonEnabledStatus(goBackButton, false);
            updateButtonEnabledStatus(disconnectButton, false);
            updateButtonEnabledStatus(startScanButton, false);
            updateButtonEnabledStatus(startSyncButton, false);
            updateButtonEnabledStatus(startTrialButton, false);

            //Start Angle Offset Initialization
            imuManager.startAngleOffsetInitialization();

        });

        //Set the behavior for the startTrialButton
        startTrialButton.setOnClickListener(view -> {

            if(startTrialButtonActivated){

                //Stop the current trial
                imuManager.stopTrial(trialName);

                //Update Trial Button Characteristics
                startTrialButtonActivated = false;
                updateButtonText(startTrialButton, "Start Trial");

                //Enable Certain Buttons
                updateButtonEnabledStatus(startScanButton, true);
                updateButtonEnabledStatus(startSyncButton, true);
                updateButtonEnabledStatus(disconnectButton, true);
                updateButtonEnabledStatus(startInitializationButton, true);
                updateButtonEnabledStatus(goBackButton, true);
                updateButtonEnabledStatus(exportRecordedDataButton, true);
                updateButtonEnabledStatus(uploadDataToCloudButton, true);

            }
            else{

                //Only start a trial if all IMUs are initialized and there is a valid subject entered
                if(IMUsAngleOffsetInitialized && validSubjectEntered){

                    //Start the current trial
                    imuManager.startTrial(trialName);

                    //Update Trial Button Characteristics
                    startTrialButtonActivated = true;
                    updateButtonText(startTrialButton, "Stop Trial");

                    //Disable Certain Buttons
                    updateButtonEnabledStatus(startScanButton, false);
                    updateButtonEnabledStatus(startSyncButton, false);
                    updateButtonEnabledStatus(disconnectButton, false);
                    updateButtonEnabledStatus(startInitializationButton, false);
                    updateButtonEnabledStatus(goBackButton, false);
                    updateButtonEnabledStatus(exportRecordedDataButton, false);
                    updateButtonEnabledStatus(uploadDataToCloudButton, false);
                }
                //If IMUs are not initialized, show an error message
                else if(!IMUsAngleOffsetInitialized){
                    errorMessagePopUp("ERROR: IMUs not Initialized");
                }
                //If there is no subject entered, show an error message
                else {
                    errorMessagePopUp("ERROR: No Subject Entered");
                }
            }

        });

        //Set the behavior for the showLogButton
        showLogButton.setOnClickListener(view -> {

            if(logPopUpWindowVisible){
                logPopupWindowUI.showLogPopUpWindow(false);
                logPopUpWindowVisible = false;
                updateButtonText(showLogButton, "Show Log");
            }
            else{
                logPopupWindowUI.showLogPopUpWindow(true);
                logPopUpWindowVisible = true;
                updateButtonText(showLogButton, "Hide Log");
            }

        });

        // TODO
        //Set the behavior for the uploadDataToCloudButton
        uploadDataToCloudButton.setOnClickListener(view -> fileManager.uploadFilesToFirebaseCloudStorage(this));

        //Set the behavior for the exportRecordedIMUDataButton
        exportRecordedDataButton.setOnClickListener(view -> {

            //Start with export of the Left Arm Data
            imuManager.startRecordingExport("Left Arm IMU");

        });

        //Set Go Back Button Click Listener
        goBackButton.setOnClickListener(view -> {

            //If any IMUs are connected, do not allow the user to go back
            if(haveIMUsBeenScanned){
                errorMessagePopUp("ERROR: IMUs are connected. Disconnect IMUs before going back.");
            }
            else{
                userInterfaceForBackButton.showPage();
            }

        });

    }
    // ================= For Bypassing the connection to haptic feedback modules this code should be commented out =================
    @Override
    public void showPage() {

        //Show the page if the gate way IP was found, if not, show the loading page and find the gate way IP
        if(imuManager.getGateWayIPFound()){
            Log.d("UDPListenerThread", "Gate Way IP Found");
            super.showPage();
        }
        else{
            Log.d("UDPListenerThread", "Gate Way IP Not Found");
            imuManager.findGateWayIP();
        }

    }

    // Bypassing the connection to haptic feedback modules #Bypass
    /*@Override
    public void showPage() {

        // Bypassing the gateway IP check to allow proceeding without haptic module connection.
        Log.d("HapticBypass", "Skipping gateway IP check and showing page directly.");
        super.showPage();

    }*/
    // ================= ================= =================

    public void linkIMUManager(Aim2ThighExtensionStudyManager imuManager) {
        this.imuManager = imuManager;
    }

    @Override
    public void updateIMUStatus(String nameOfIMU, String status){

        //Update the IMU Status based on the name of the IMU
        switch (nameOfIMU){
            case "Left Arm IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftArmStatusView, status);
                break;
            case "Right Arm IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightArmStatusView, status);
                break;
            case "Left Thigh IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighStatusView, status);
                break;
            case "Right Thigh IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighStatusView, status);
                break;
            case "Left Foot IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftFootStatusView, status);
                break;
            case "Right Foot IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightFootStatusView, status);
                break;
        }

    }

    @Override
    public void updateIMUDataOutput(String nameOfIMU, String data) {

        //Update the IMU DataOutput based on the name of the IMU
        switch (nameOfIMU){
            case "Left Thigh IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighDataOutputView, data);
                break;
            case "Right Thigh IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighDataOutputView, data);
                break;
            case "Left Foot IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftFootDataOutputView, data);
                break;
            case "Right Foot IMU":
                updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightFootDataOutputView, data);
                break;
        }

    }

    public void updateGaitParameterOutput(String gaitParameter, String nameOfIMU, String data){

        //Update the IMU Gait Parameter Output on the Screen based on the name of the IMU
        switch (gaitParameter){
            case "PTE":
                //If nameOfIMU is Left Thigh, update the Left Last Stride PTE
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighLastPeak, data);
                }
                //If nameOfIMU is Right Thigh, update the Right Last Stride PTE
                else if(nameOfIMU.equals("Right Thigh IMU")){
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighLastPeak, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for PTE Update");
                }
                break;
            case "StrideLength":
                //If nameOfIMU is Left Foot, update the Left Stride Length
                if(nameOfIMU.equals("Left Foot IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighLastStrideLength, data);
                }
                //If nameOfIMU is Right Foot, update the Right Stride Length
                else if(nameOfIMU.equals("Right Foot IMU")){
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighLastStrideLength, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for Stride Length Update");
                }
                break;
            case "WalkingSpeed":
                //If nameOfIMU is Left Foot, update the Left Walking Speed
                if(nameOfIMU.equals("Left Foot IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighLastSpeed, data);
                }
                //If nameOfIMU is Right Foot, update the Right Walking Speed
                else if(nameOfIMU.equals("Right Foot IMU")){
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighLastSpeed, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for Walking Speed Update");
                }
                break;
            case "Cadence":
                //If nameOfIMU is Left Foot, update the Left Cadence
                if(nameOfIMU.equals("Left Foot IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighLastCadence, data);
                }
                //If nameOfIMU is Right Foot, update the Right Cadence
                else if(nameOfIMU.equals("Right Foot IMU")){
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighLastCadence, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for Cadence Update");
                }
                break;
            case "PTECycleCount":
                //If nameOfIMU is Left Thigh, update the Left Thigh PTE Cycle Count
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighCycle, data);
                }
                //If nameOfIMU is Right Thigh, update the Right Thigh PTE Cycle Count
                else if(nameOfIMU.equals("Right Thigh IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighCycle, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for PTE Cycle Count Update");
                }
                break;
            case "HeelStrikeCycleCount":
                //If nameOfIMU is Left Foot, update the Left Heel Strike Cycle Count
                if(nameOfIMU.equals("Left Foot IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftFootCycle, data);
                }
                //If nameOfIMU is Right Foot, update the Right Heel Strike Cycle Count
                else if(nameOfIMU.equals("Right Foot IMU")){
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightFootCycle, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for Heel Strike Cycle Count Update");
                }
                break;
            case "TargetAngle":
                //If nameOfIMU is Left Thigh, update the Left Thigh Target Angle
                if(nameOfIMU.equals("Left Thigh IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_LeftThighTargetAngle, data);
                }
                //If nameOfIMU is Right Thigh, update the Right Thigh Target Angle
                else if(nameOfIMU.equals("Right Thigh IMU")) {
                    updateTextViewText(R.id.aim2_thigh_extension_study_trial_page_RightThighTargetAngle, data);
                }
                //If nameOFIMU is anything else, log as an error
                else{
                    Log.e(TAG + "updateGaitParameterOutput", "ERROR: Invalid IMU Name for Target Angle Update");
                }
                break;
        }

    }

    @Override
    public void onScanComplete(boolean success) {

        updateButtonText(startScanButton, "Start Scan");
        updateButtonEnabledStatus(startScanButton, true);
        updateButtonEnabledStatus(goBackButton, true);

        onIMUsFullyConnected(success);

    }

    @Override
    public void onSyncComplete(boolean success) {

        updateButtonText(startSyncButton, "Start Sync");
        updateButtonEnabledStatus(startSyncButton, true);
        updateButtonEnabledStatus(goBackButton, true);

        onIMUsFullySynced(success);

    }

    @Override
    public void onOffsetInitializationComplete(boolean success) {

        IMUsAngleOffsetInitialized = success;

        updateButtonText(startInitializationButton, "Start Init");
        updateButtonEnabledStatus(startInitializationButton, true);
        updateButtonEnabledStatus(goBackButton, true);

        updateButtonEnabledStatus(startScanButton, true);
        updateButtonEnabledStatus(startSyncButton, true);
        updateButtonEnabledStatus(disconnectButton, true);
        updateButtonEnabledStatus(startTrialButton, true);

    }

    private void onIMUsFullyConnected(boolean fullyScanned){
        if(fullyScanned) {
            updateButtonEnabledStatus(startSyncButton, true);
            updateButtonEnabledStatus(disconnectButton, true);
        }
        else{
            updateButtonEnabledStatus(startSyncButton, false);
            updateButtonEnabledStatus(startInitializationButton, false);
            updateButtonEnabledStatus(startTrialButton, false);
        }
    }

    private void onIMUsFullySynced(boolean fullySynced){
        updateButtonEnabledStatus(disconnectButton, true);
        if(fullySynced){
            updateButtonEnabledStatus(startInitializationButton, true);
            updateButtonEnabledStatus(startTrialButton, true);
        }
        else{
            updateButtonEnabledStatus(startInitializationButton, false);
            updateButtonEnabledStatus(startTrialButton, false);
        }
    }

    public void updateHapticCellIPAddress(String sideOfBody, int blockNumber, int IPBlock){

        //Declare the IP address blocks
        int[] IPAddressBlocks;

        if(sideOfBody.equals("Left")){

            //Extract the IP address blocks from the haptic cell 1 IP address
            IPAddressBlocks = HapticControlModule.getIPBlocksFromString(leftHapticCellIPAddress);

            //Update the specified IP block
            IPAddressBlocks[blockNumber-1] = IPBlock;

            //Update the haptic cell 1 IP address
            leftHapticCellIPAddress = IPAddressBlocks[0] + "." + IPAddressBlocks[1] + "." +
                    IPAddressBlocks[2] + "." + IPAddressBlocks[3];

            //Update the device 1 IP address text view
            updateTextViewText(leftHapticCellIPAddressTextView, "IP Address:\n" + leftHapticCellIPAddress);
        }
        else{

            //Extract the IP address blocks from the haptic cell 2 IP address
            IPAddressBlocks = HapticControlModule.getIPBlocksFromString(rightHapticCellIPAddress);

            //Update the specified IP block
            IPAddressBlocks[blockNumber-1] = IPBlock;

            //Update the haptic cell 2 IP address
            rightHapticCellIPAddress = IPAddressBlocks[0] + "." + IPAddressBlocks[1] + "." +
                    IPAddressBlocks[2] + "." + IPAddressBlocks[3];

            //Update the device 2 IP address text view
            updateTextViewText(rightHapticCellIPAddressTextView, "IP Address:\n" + rightHapticCellIPAddress);
        }
    }

    @Override
    public void onRecordingExportComplete() {

    }
}
