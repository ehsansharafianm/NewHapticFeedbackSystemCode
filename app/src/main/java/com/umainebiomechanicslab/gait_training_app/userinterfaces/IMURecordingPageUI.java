package com.umainebiomechanicslab.gait_training_app.userinterfaces;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.R;
import com.umainebiomechanicslab.gait_training_app.studymanagers.RecordIMUDataManager;

public class IMURecordingPageUI extends UserInterfaceWithIMU {

    private RecordIMUDataManager imuManager;

    private boolean IMUsAngleOffsetInitialized;
    private boolean startTrialButtonActivated;
    private boolean logPopUpWindowVisible;
    private boolean validTrialName;

    private boolean isIMU1SpinnerDefaultSelection = true;
    private boolean isIMU2SpinnerDefaultSelection = true;
    private boolean isIMU3SpinnerDefaultSelection = true;
    private boolean isIMU4SpinnerDefaultSelection = true;

    private String trialName;

    public IMURecordingPageUI(Activity activity, int pageID, LogPopupWindowUI logPopupWindowUI, FileManager fileManager) {

        super(activity, pageID);

        //Initialize IMUsAngleOffsetInitialized
        IMUsAngleOffsetInitialized = false;

        //Initialize startTrialButtonActivated
        startTrialButtonActivated = false;

        //Initialize logPopUpWindowVisible
        logPopUpWindowVisible = false;

        //Initialize validTrialName
        validTrialName = false;

        //Declare UI Buttons
        goBackButton = activity.findViewById(R.id.imu_recording_page_GoBackButton);
        startScanButton = activity.findViewById(R.id.imu_recording_page_StartScanButton);
        startSyncButton = activity.findViewById(R.id.imu_recording_page_StartSyncButton);
        showBatteryPercentageButton = activity.findViewById(R.id.imu_recording_page_ShowIMUsBatteryButton);
        disconnectButton = activity.findViewById(R.id.imu_recording_page_DisconnectIMUsButton);
        startInitializationButton = activity.findViewById(R.id.imu_recording_page_StartInitializationButton);
        startTrialButton = activity.findViewById(R.id.imu_recording_page_StartTrialButton);
        showLogButton = activity.findViewById(R.id.imu_recording_page_ShowLogButton);
        uploadDataToCloudButton = activity.findViewById(R.id.imu_recording_page_UploadDataToCloudButton);

        //Set enabled status of buttons that shouldn't be enabled at the start of the app
        updateButtonEnabledStatus(startScanButton, false);
        updateButtonEnabledStatus(startSyncButton, false);
        updateButtonEnabledStatus(disconnectButton, false);
        updateButtonEnabledStatus(startInitializationButton, false);
        updateButtonEnabledStatus(startTrialButton, false);

        //Declare UI EditText
        EditText trialNameEditText = activity.findViewById(R.id.imu_recording_page_TrialNameEntryBox);

        //Declare UI Dropdown Spinners
        Spinner IMU1Spinner = activity.findViewById(R.id.imu_recording_page_IMU1Spinner);
        Spinner IMU2Spinner = activity.findViewById(R.id.imu_recording_page_IMU2Spinner);
        Spinner IMU3Spinner = activity.findViewById(R.id.imu_recording_page_IMU3Spinner);
        Spinner IMU4Spinner = activity.findViewById(R.id.imu_recording_page_IMU4Spinner);

        //Set the Dropdown Spinner Array Adapter
        ArrayAdapter<CharSequence> hapticCellIPAddresses = new ArrayAdapter<>(activity, R.layout.popup_window_for_dropdown_spinner, activity.getResources().getStringArray(R.array.IMU_MAC_Addresses)); // Use your layout for the trigger
        hapticCellIPAddresses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Set the Dropdown Spinners
        IMU1Spinner.setAdapter(hapticCellIPAddresses);
        IMU2Spinner.setAdapter(hapticCellIPAddresses);
        IMU3Spinner.setAdapter(hapticCellIPAddresses);
        IMU4Spinner.setAdapter(hapticCellIPAddresses);

        //Set the IMU1 Dropdown Spinner Listener
        IMU1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isIMU1SpinnerDefaultSelection){
                    isIMU1SpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", delete the IMU
                if(spinnerSelection.equals("--")){
                    imuManager.updateIMUCode("IMU1", "--");

                    //Disable Start Scan Button if all other spinners are "--"
                    if(IMU2Spinner.getSelectedItem().toString().equals("--") && IMU3Spinner.getSelectedItem().toString().equals("--") && IMU4Spinner.getSelectedItem().toString().equals("--")){
                        updateButtonEnabledStatus(startScanButton, false);
                    }

                }

                //Otherwise, update the IMU code (either create a new IMU or update an existing one)
                else{
                    imuManager.updateIMUCode("IMU1", spinnerSelection);

                    //Enable Start Scan Button (since now at least one IMU is selected)
                    updateButtonEnabledStatus(startScanButton, true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the IMU2 Dropdown Spinner Listener
        IMU2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isIMU2SpinnerDefaultSelection){
                    isIMU2SpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", delete the IMU
                if(spinnerSelection.equals("--")){
                    imuManager.updateIMUCode("IMU1", "--");

                    //Disable Start Scan Button if all other spinners are "--"
                    if(IMU1Spinner.getSelectedItem().toString().equals("--") && IMU3Spinner.getSelectedItem().toString().equals("--") && IMU4Spinner.getSelectedItem().toString().equals("--")){
                        updateButtonEnabledStatus(startScanButton, false);
                    }

                }

                //Otherwise, update the IMU code (either create a new IMU or update an existing one)
                else{
                    imuManager.updateIMUCode("IMU2", spinnerSelection);

                    //Enable Start Scan Button (since now at least one IMU is selected)
                    updateButtonEnabledStatus(startScanButton, true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the IMU3 Dropdown Spinner Listener
        IMU3Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isIMU3SpinnerDefaultSelection){
                    isIMU3SpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", delete the IMU
                if(spinnerSelection.equals("--")){
                    imuManager.updateIMUCode("IMU1", "--");

                    //Disable Start Scan Button if all other spinners are "--"
                    if(IMU1Spinner.getSelectedItem().toString().equals("--") && IMU2Spinner.getSelectedItem().toString().equals("--") && IMU4Spinner.getSelectedItem().toString().equals("--")){
                        updateButtonEnabledStatus(startScanButton, false);
                    }

                }

                //Otherwise, update the IMU code (either create a new IMU or update an existing one)
                else{
                    imuManager.updateIMUCode("IMU3", spinnerSelection);

                    //Enable Start Scan Button (since now at least one IMU is selected)
                    updateButtonEnabledStatus(startScanButton, true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the IMU4 Dropdown Spinner Listener
        IMU4Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                //If this is the default initialization of the spinner, do not update the IMU code
                if(isIMU4SpinnerDefaultSelection){
                    isIMU4SpinnerDefaultSelection = false;
                    return;
                }

                //Extract the selected item and convert it to a string
                String spinnerSelection = adapterView.getItemAtPosition(position).toString();

                //If the selected item is "--", delete the IMU
                if(spinnerSelection.equals("--")){
                    imuManager.updateIMUCode("IMU1", "--");

                    //Disable Start Scan Button if all other spinners are "--"
                    if(IMU1Spinner.getSelectedItem().toString().equals("--") && IMU2Spinner.getSelectedItem().toString().equals("--") && IMU3Spinner.getSelectedItem().toString().equals("--")){
                        updateButtonEnabledStatus(startScanButton, false);
                    }

                }

                //Otherwise, update the IMU code (either create a new IMU or update an existing one)
                else{
                    imuManager.updateIMUCode("IMU4", spinnerSelection);

                    //Enable Start Scan Button (since now at least one IMU is selected)
                    updateButtonEnabledStatus(startScanButton, true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Set the behavior for the startScanButton
        startScanButton.setOnClickListener(view -> {

            //If the selected value in IMU1Spinner is not "--" and matches any of the other three spinners, show an error message
            if(!IMU1Spinner.getSelectedItem().toString().equals("--") &&
                    (IMU2Spinner.getSelectedItem().toString().equals(IMU1Spinner.getSelectedItem().toString()) ||
                    IMU3Spinner.getSelectedItem().toString().equals(IMU1Spinner.getSelectedItem().toString()) ||
                    IMU4Spinner.getSelectedItem().toString().equals(IMU1Spinner.getSelectedItem().toString()))){
                errorMessagePopUp("ERROR: Duplicate IMU Selected");
            }
            //If the selected value in IMU2Spinner is not "--" and matches any of the other three spinners, show an error message
            else if(!IMU2Spinner.getSelectedItem().toString().equals("--") &&
                    (IMU1Spinner.getSelectedItem().toString().equals(IMU2Spinner.getSelectedItem().toString()) ||
                    IMU3Spinner.getSelectedItem().toString().equals(IMU2Spinner.getSelectedItem().toString()) ||
                    IMU4Spinner.getSelectedItem().toString().equals(IMU2Spinner.getSelectedItem().toString()))){
                errorMessagePopUp("ERROR: Duplicate IMU Selected");
            }
            //If the selected value in IMU3Spinner is not "--" and matches any of the other three spinners, show an error message
            else if(!IMU3Spinner.getSelectedItem().toString().equals("--") &&
                    (IMU1Spinner.getSelectedItem().toString().equals(IMU3Spinner.getSelectedItem().toString()) ||
                    IMU2Spinner.getSelectedItem().toString().equals(IMU3Spinner.getSelectedItem().toString()) ||
                    IMU4Spinner.getSelectedItem().toString().equals(IMU3Spinner.getSelectedItem().toString()))){
                errorMessagePopUp("ERROR: Duplicate IMU Selected");
            }
            //If the selected value in IMU4Spinner is not "--" and matches any of the other three spinners, show an error message
            else if(!IMU4Spinner.getSelectedItem().toString().equals("--") &&
                    (IMU1Spinner.getSelectedItem().toString().equals(IMU4Spinner.getSelectedItem().toString()) ||
                    IMU2Spinner.getSelectedItem().toString().equals(IMU4Spinner.getSelectedItem().toString()) ||
                    IMU3Spinner.getSelectedItem().toString().equals(IMU4Spinner.getSelectedItem().toString()))){
                errorMessagePopUp("ERROR: Duplicate IMU Selected");
            }
            //Otherwise, continue with the scan process
            else{
                updateButtonText(startScanButton, "Scanning...");
                updateButtonEnabledStatus(startScanButton, false);
                updateButtonEnabledStatus(goBackButton, false);
                updateButtonEnabledStatus(disconnectButton, false);

                //Disable IMU Spinners
                updateSpinnerEnabledStatus(IMU1Spinner, false);
                updateSpinnerEnabledStatus(IMU2Spinner, false);
                updateSpinnerEnabledStatus(IMU3Spinner, false);
                updateSpinnerEnabledStatus(IMU4Spinner, false);

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
            updateSpinnerEnabledStatus(IMU1Spinner, true);
            updateSpinnerEnabledStatus(IMU2Spinner, true);
            updateSpinnerEnabledStatus(IMU3Spinner, true);
            updateSpinnerEnabledStatus(IMU4Spinner, true);

            //Set haveIMUsBeenScanned to false
            haveIMUsBeenScanned = false;

        });

        //Set the behavior for the trialNameEditText
        trialNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                //Extract the text from the EditText and convert it to a string, trimming the white space
                String textEntry = editable.toString().trim();

                //Define a regular expression for the allowed characters
                String allowedCharacters = "^[a-zA-Z0-9_]*$";

                //Check if the text entry is valid (not named initializing or contains only alphanumeric characters and underscores)
                if (textEntry.equalsIgnoreCase("Initializing") || !textEntry.matches(allowedCharacters) || textEntry.isEmpty()) {
                    errorMessagePopUp("ERROR: Invalid Trial Name");
                    onValidTrialNameEntered(false);
                }
                else{
                    onValidTrialNameEntered(true);
                    trialName = textEntry;
                }

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

            }
            else{

                //Only start a trial if all IMUs are initialized
                if(IMUsAngleOffsetInitialized){

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
                }
                else{
                    errorMessagePopUp("ERROR: IMUs not Initialized");
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

        //Set the behavior for the uploadDataToCloudButton
        uploadDataToCloudButton.setOnClickListener(view -> {

            fileManager.uploadFilesToFirebaseCloudStorage(this);

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

    public void linkIMUManager(RecordIMUDataManager imuManager) {
        this.imuManager = imuManager;
    }

    @Override
    public void updateIMUStatus(String nameOfIMU, String status){

        //Update the IMU Status based on the name of the IMU
        switch (nameOfIMU){
            case "IMU1":
                updateTextViewText(R.id.imu_recording_page_IMU1StatusView, status);
                break;
            case "IMU2":
                updateTextViewText(R.id.imu_recording_page_IMU2StatusView, status);
                break;
            case "IMU3":
                updateTextViewText(R.id.imu_recording_page_IMU3StatusView, status);
                break;
            case "IMU4":
                updateTextViewText(R.id.imu_recording_page_IMU4StatusView, status);
                break;
        }

    }

    @Override
    public void updateIMUDataOutput(String nameOfIMU, String data) {

        //Update the IMU Data Output based on the name of the IMU
        switch (nameOfIMU){
            case "IMU1":
                updateTextViewText(R.id.imu_recording_page_IMU1DataOutputView, data);
                break;
            case "IMU2":
                updateTextViewText(R.id.imu_recording_page_IMU2DataOutputView, data);
                break;
            case "IMU3":
                updateTextViewText(R.id.imu_recording_page_IMU3DataOutputView, data);
                break;
            case "IMU4":
                updateTextViewText(R.id.imu_recording_page_IMU4DataOutputView, data);
                break;
        }

    }

    @Override
    public void updateGaitParameterOutput(String gaitParameter, String nameOfIMU, String data) {
        //LEAVE THIS METHOD EMPTY
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
        updateButtonEnabledStatus(startTrialButton, validTrialName);

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

    private void onValidTrialNameEntered(boolean validEntry){

        //Set validTrialName to the value of validEntry
        validTrialName = validEntry;

        if (validEntry) {
            updateButtonEnabledStatus(startInitializationButton, true);
            updateButtonEnabledStatus(startTrialButton, true);
        }
        else{
            updateButtonEnabledStatus(startInitializationButton, false);
            updateButtonEnabledStatus(startTrialButton, false);
        }
    }
}
