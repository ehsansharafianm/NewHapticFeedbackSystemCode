package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class TestHapticCellsUI extends UserInterface{

    //Declare the variables to store the vibration durations for each cell (in seconds)
    private double device1CellADuration, device1CellBDuration, device2CellADuration, device2CellBDuration;

    //Declare the TestHapticCellsManager
    private TestHapticCellsManager testHapticCellsManager;

    //Declare the haptic cell IP address Strings
    private String hapticCell1IPAddress, hapticCell2IPAddress;

    //Declare TextViews that need to be accessed outside of the constructor
    private final TextView device1IPAddressTextView;
    private final TextView device2IPAddressTextView;

    //Declare Buttons that need to be accessed outside of the constructor
    private final Button device1CellAButton;
    private final Button device1CellBButton;
    private final Button device2CellAButton;
    private final Button device2CellBButton;

    public TestHapticCellsUI(Activity activity, int pageID) {

        super(activity, pageID);

        //Set the default vibration durations for each cell (0.25 seconds)
        device1CellADuration = 0.25;
        device1CellBDuration = 0.25;
        device2CellADuration = 0.25;
        device2CellBDuration = 0.25;

        //Set the default haptic cell IP addresses to 0.0.0.0
        hapticCell1IPAddress = "0.0.0.0";
        hapticCell2IPAddress = "0.0.0.0";

        //Declare UI EditTexts
        EditText device1NumberTextBox = activity.findViewById(R.id.test_haptic_cells_page_Device1NumberEntryBox);
        EditText device2NumberTextBox = activity.findViewById(R.id.test_haptic_cells_page_Device2NumberEntryBox);

        //Declare UI Buttons
        device1CellAButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellATestButton);
        Button device1CellADecreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellADecreaseDuration);
        Button device1CellAIncreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellAIncreaseDuration);
        device1CellBButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellBTestButton);
        Button device1CellBDecreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellBDecreaseDuration);
        Button device1CellBIncreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device1CellBIncreaseDuration);
        device2CellAButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellATestButton);
        Button device2CellADecreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellADecreaseDuration);
        Button device2CellAIncreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellAIncreaseDuration);
        device2CellBButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellBTestButton);
        Button device2CellBDecreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellBDecreaseDuration);
        Button device2CellBIncreaseDurationButton = activity.findViewById(R.id.test_haptic_cells_page_Device2CellBIncreaseDuration);
        Button goBackButton = activity.findViewById(R.id.test_haptic_cells_page_GoBack);

        //Set Feedback Test Button Enabled Status to False by Default
        device1CellAButton.setEnabled(false);
        device1CellBButton.setEnabled(false);
        device2CellAButton.setEnabled(false);
        device2CellBButton.setEnabled(false);

        //Declare UI Text Views
        TextView device1CellADurationTextView = activity.findViewById(R.id.test_haptic_cells_page_Device1CellADuration);
        TextView device1CellBDurationTextView = activity.findViewById(R.id.test_haptic_cells_page_Device1CellBDuration);
        TextView device2CellADurationTextView = activity.findViewById(R.id.test_haptic_cells_page_Device2CellADuration);
        TextView device2CellBDurationTextView = activity.findViewById(R.id.test_haptic_cells_page_Device2CellBDuration);

        //Link the device IP address text views
        device1IPAddressTextView = activity.findViewById(R.id.test_haptic_cells_page_Device1IP);
        device2IPAddressTextView = activity.findViewById(R.id.test_haptic_cells_page_Device2IP);

        //Set Device 1 Number Text Box Entry Listener
        device1NumberTextBox.addTextChangedListener(new TextWatcher() {
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
                    int deviceNumber = Integer.parseInt(device1NumberTextBox.getText().toString());

                    //Check to see if device number is valid (0-255)
                    if(deviceNumber >= 0 && deviceNumber <= 255){
                        testHapticCellsManager.setDeviceNumber(1, deviceNumber);

                        //If Device 1 Cell A or Cell B test buttons had been disabled, enable them
                        if(!device1CellAButton.isEnabled()){
                            device1CellAButton.setEnabled(true);
                        }
                        if(!device1CellBButton.isEnabled()) {
                            device1CellBButton.setEnabled(true);
                        }
                    }
                    else{

                        //Show an error message
                        textPopUp("WARNING! Invalid Input");

                        //Disable the Device 1 Cell A and Cell B buttons
                        device1CellAButton.setEnabled(false);
                        device1CellBButton.setEnabled(false);
                    }

                }
                catch (NumberFormatException e){

                    //Show an error message
                    textPopUp("WARNING! Invalid Input");

                    //Disable the Device 1 Cell A and Cell B buttons
                    device1CellAButton.setEnabled(false);
                    device1CellBButton.setEnabled(false);

                }

            }
        });

        //Set Device 2 Number Text Box Entry Listener
        device2NumberTextBox.addTextChangedListener(new TextWatcher() {
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
                    int deviceNumber = Integer.parseInt(device2NumberTextBox.getText().toString());

                    //Check to see if device number is valid (0-255)
                    if(deviceNumber >= 0 && deviceNumber <= 255){
                        testHapticCellsManager.setDeviceNumber(2, deviceNumber);

                        //If Device 1 Cell A or Cell B test buttons had been disabled, enable them
                        if(!device2CellAButton.isEnabled()){
                            device2CellAButton.setEnabled(true);
                        }
                        if(!device2CellBButton.isEnabled()) {
                            device2CellBButton.setEnabled(true);
                        }
                    }
                    else{

                        //Show an error message
                        textPopUp("WARNING! Invalid Input");

                        //Disable the Device 1 Cell A and Cell B buttons
                        device2CellAButton.setEnabled(false);
                        device2CellBButton.setEnabled(false);
                    }

                }
                catch (NumberFormatException e){

                    //Show an error message
                    textPopUp("WARNING! Invalid Input");

                    //Disable the Device 1 Cell A and Cell B buttons
                    device2CellAButton.setEnabled(false);
                    device2CellBButton.setEnabled(false);

                }

            }
        });

        //Set Device 1 Cell A Button Click Listener
        device1CellAButton.setOnClickListener(view -> testHapticCellsManager.sendHapticFeedback(1, "A"));

        //Set Device 1 Cell A Decrease Duration Button Click Listener
        device1CellADecreaseDurationButton.setOnClickListener(view -> {

            //Decrease the device 1 cell A duration by 0.25 seconds
            device1CellADuration -= 0.25;

            //Set the device 1 cell A duration
            testHapticCellsManager.setCellVibrationDuration(1, "A", (int) (device1CellADuration * 1000));
            
            //Update the device 1 cell A duration text view
            updateTextViewText(device1CellADurationTextView, String.format(Locale.US, "%.2f\nsec.", device1CellADuration));

            //If the device 1 cell A duration is less than or equal to 0.25 seconds, disable the decrease duration button
            if(device1CellADuration <= testHapticCellsManager.MIN_VIBRATION_DURATION / 1000.0){
                device1CellADecreaseDurationButton.setEnabled(false);
            }
            //If the device 1 cell A increase button was previously disabled, enable it
            else if(!device1CellAIncreaseDurationButton.isEnabled()){
                device1CellAIncreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 1 Cell A Increase Duration Button Click Listener
        device1CellAIncreaseDurationButton.setOnClickListener(view -> {

            //Increase the device 1 cell A duration by 0.25 seconds
            device1CellADuration += 0.25;

            //Set the device 1 cell A duration
            testHapticCellsManager.setCellVibrationDuration(1, "A", (int) (device1CellADuration * 1000));
            
            //Update the device 1 cell A duration text view
            updateTextViewText(device1CellADurationTextView, String.format(Locale.US, "%.2f\nsec.", device1CellADuration));

            //If the device 1 cell A duration is greater than or equal to 10 seconds, disable the increase duration button
            if(device1CellADuration >= testHapticCellsManager.MAX_VIBRATION_DURATION / 1000.0){
                device1CellAIncreaseDurationButton.setEnabled(false);
            }
            //If the device 1 cell A decrease button was previously disabled, enable it
            else if(!device1CellADecreaseDurationButton.isEnabled()){
                device1CellADecreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 1 Cell B Button Click Listener
        device1CellBButton.setOnClickListener(view -> testHapticCellsManager.sendHapticFeedback(1, "B"));

        //Set Device 1 Cell B Decrease Duration Button Click Listener
        device1CellBDecreaseDurationButton.setOnClickListener(view -> {

            //Decrease the device 1 cell B duration by 0.25 seconds
            device1CellBDuration -= 0.25;

            //Set the device 1 cell B duration
            testHapticCellsManager.setCellVibrationDuration(1, "B", (int) (device1CellBDuration * 1000));
            
            //Update the device 1 cell B duration text view
            updateTextViewText(device1CellBDurationTextView, String.format(Locale.US, "%.2f\nsec.", device1CellBDuration));

            //If the device 1 cell B duration is less than or equal to 0.25 seconds, disable the decrease duration button
            if (device1CellBDuration <= testHapticCellsManager.MIN_VIBRATION_DURATION / 1000.0) {
                device1CellBDecreaseDurationButton.setEnabled(false);
            }
            //If the device 1 cell B increase button was previously disabled, enable it
            else if (!device1CellBIncreaseDurationButton.isEnabled()) {
                device1CellBIncreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 1 Cell B Increase Duration Button Click Listener
        device1CellBIncreaseDurationButton.setOnClickListener(view -> {

            //Increase the device 1 cell B duration by 0.25 seconds
            device1CellBDuration += 0.25;

            //Set the device 1 cell B duration
            testHapticCellsManager.setCellVibrationDuration(1, "B", (int) (device1CellBDuration * 1000));
            
            //Update the device 1 cell B duration text view
            updateTextViewText(device1CellBDurationTextView, String.format(Locale.US, "%.2f\nsec.", device1CellBDuration));

            //If the device 1 cell B duration is greater than or equal to 10 seconds, disable the increase duration button
            if(device1CellBDuration >= testHapticCellsManager.MAX_VIBRATION_DURATION / 1000.0){
                device1CellBIncreaseDurationButton.setEnabled(false);
            }
            //If the device 1 cell B decrease button was previously disabled, enable it
            else if(!device1CellBDecreaseDurationButton.isEnabled()){
                device1CellBDecreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 2 Cell A Button Click Listener
        device2CellAButton.setOnClickListener(view -> testHapticCellsManager.sendHapticFeedback(2, "A"));

        //Set Device 2 Cell A Decrease Duration Button Click Listener
        device2CellADecreaseDurationButton.setOnClickListener(view -> {

            //Decrease the device 2 cell A duration by 0.25 seconds
            device2CellADuration -= 0.25;

            //Set the device 2 cell A duration
            testHapticCellsManager.setCellVibrationDuration(2, "A", (int) (device2CellADuration * 1000));
            
            //Update the device 2 cell A duration text view
            updateTextViewText(device2CellADurationTextView, String.format(Locale.US, "%.2f\nsec.", device2CellADuration));

            //If the device 2 cell A duration is less than or equal to 0.25 seconds, disable the decrease duration button
            if(device2CellADuration <= testHapticCellsManager.MIN_VIBRATION_DURATION / 1000.0){
                device2CellADecreaseDurationButton.setEnabled(false);
            }
            //If the device 2 cell A increase button was previously disabled, enable it
            else if(!device2CellAIncreaseDurationButton.isEnabled()){
                device2CellAIncreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 2 Cell A Increase Duration Button Click Listener
        device2CellAIncreaseDurationButton.setOnClickListener(view -> {

            //Increase the device 2 cell A duration by 0.25 seconds
            device2CellADuration += 0.25;

            //Set the device 2 cell A duration
            testHapticCellsManager.setCellVibrationDuration(2, "A", (int) (device2CellADuration * 1000));
            
            //Update the device 2 cell A duration text view
            updateTextViewText(device2CellADurationTextView, String.format(Locale.US, "%.2f\nsec.", device2CellADuration));

            //If the device 2 cell A duration is greater than or equal to 10 seconds, disable the increase duration button
            if(device2CellADuration >= testHapticCellsManager.MAX_VIBRATION_DURATION / 1000.0){
                device2CellAIncreaseDurationButton.setEnabled(false);
            }
            //If the device 2 cell A decrease button was previously disabled, enable it
            else if(!device2CellADecreaseDurationButton.isEnabled()){
                device2CellADecreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 2 Cell B Button Click Listener
        device2CellBButton.setOnClickListener(view -> testHapticCellsManager.sendHapticFeedback(2, "B"));

        //Set Device 2 Cell B Decrease Duration Button Click Listener
        device2CellBDecreaseDurationButton.setOnClickListener(view -> {

            //Decrease the device 2 cell B duration by 0.25 seconds
            device2CellBDuration -= 0.25;

            //Set the device 2 cell B duration
            testHapticCellsManager.setCellVibrationDuration(2, "B", (int) (device2CellBDuration * 1000));
            
            //Update the device 2 cell B duration text view
            updateTextViewText(device2CellBDurationTextView, String.format(Locale.US, "%.2f\nsec.", device2CellBDuration));

            //If the device 2 cell B duration is less than or equal to 0.25 seconds, disable the decrease duration button
            if(device2CellBDuration <= testHapticCellsManager.MIN_VIBRATION_DURATION / 1000.0){
                device2CellBDecreaseDurationButton.setEnabled(false);
            }
            //If the device 2 cell B increase button was previously disabled, enable it
            else if(!device2CellBIncreaseDurationButton.isEnabled()){
                device2CellBIncreaseDurationButton.setEnabled(true);
            }

        });

        //Set Device 2 Cell B Increase Duration Button Click Listener
        device2CellBIncreaseDurationButton.setOnClickListener(view -> {

            //Increase the device 2 cell B duration by 0.25 seconds
            device2CellBDuration += 0.25;

            //Set the device 2 cell B duration
            testHapticCellsManager.setCellVibrationDuration(2, "B", (int) (device2CellBDuration * 1000));
            
            //Update the device 2 cell B duration text view
            updateTextViewText(device2CellBDurationTextView, String.format(Locale.US, "%.2f\nsec.", device2CellBDuration));

            //If the device 2 cell B duration is greater than or equal to 10 seconds, disable the increase duration button
            if(device2CellBDuration >= testHapticCellsManager.MAX_VIBRATION_DURATION / 1000.0){
                device2CellBIncreaseDurationButton.setEnabled(false);
            }
            //If the device 2 cell B decrease button was previously disabled, enable it
            else if(!device2CellBDecreaseDurationButton.isEnabled()){
                device2CellBDecreaseDurationButton.setEnabled(true);
            }

        });

        //Set Go Back Button Click Listener
        goBackButton.setOnClickListener(view -> userInterfaceForBackButton.showPage());

    }

    @Override
    public void showPage() {

        //Show the page if the gate way IP was found, if not, show the loading page and find the gate way IP
        if(testHapticCellsManager.getGateWayIPFound()){
            Log.d("UDPListenerThread", "Gate Way IP Found");
            super.showPage();
        }
        else{
            Log.d("UDPListenerThread", "Gate Way IP Not Found");
            testHapticCellsManager.findGateWayIP();
        }

    }

    public void linkTestHapticCellsManager(TestHapticCellsManager testHapticCellsManager){
        this.testHapticCellsManager = testHapticCellsManager;
    }

    public void updateHapticCellIPAddress(int cellNumber, int blockNumber, int IPBlock){

        //Declare the IP address blocks
        int[] IPAddressBlocks;

        if(cellNumber == 1){

            //Extract the IP address blocks from the haptic cell 1 IP address
            IPAddressBlocks = HapticControlModule.getIPBlocksFromString(hapticCell1IPAddress);

            //Update the specified IP block
            IPAddressBlocks[blockNumber-1] = IPBlock;

            //Update the haptic cell 1 IP address
            hapticCell1IPAddress = IPAddressBlocks[0] + "." + IPAddressBlocks[1] + "." +
                    IPAddressBlocks[2] + "." + IPAddressBlocks[3];

            //Update the device 1 IP address text view
            updateTextViewText(device1IPAddressTextView, "IP Address:\n" + hapticCell1IPAddress);
        }
        else{

            //Extract the IP address blocks from the haptic cell 2 IP address
            IPAddressBlocks = HapticControlModule.getIPBlocksFromString(hapticCell2IPAddress);

            //Update the specified IP block
            IPAddressBlocks[blockNumber-1] = IPBlock;

            //Update the haptic cell 2 IP address
            hapticCell2IPAddress = IPAddressBlocks[0] + "." + IPAddressBlocks[1] + "." +
                    IPAddressBlocks[2] + "." + IPAddressBlocks[3];

            //Update the device 2 IP address text view
            updateTextViewText(device2IPAddressTextView, "IP Address:\n" + hapticCell2IPAddress);
        }
    }
}
