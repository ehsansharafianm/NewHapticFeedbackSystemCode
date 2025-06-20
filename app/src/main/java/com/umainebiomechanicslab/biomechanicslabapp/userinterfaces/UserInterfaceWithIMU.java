package com.umainebiomechanicslab.biomechanicslabapp.userinterfaces;

import android.app.Activity;
import android.widget.Button;

public abstract class UserInterfaceWithIMU extends UserInterface {

    protected Button startScanButton, startSyncButton, disconnectButton, showBatteryPercentageButton, startInitializationButton,
            startTrialButton, goBackButton, uploadDataToCloudButton, showLogButton;

    protected boolean validSubjectEntered;

    protected boolean haveIMUsBeenScanned;

    protected boolean logPopUpWindowVisible, batteryPercentageVisible;

    public UserInterfaceWithIMU(Activity activity, int pageID) {
        super(activity, pageID);

        //Initialize validSubjectEntered to false when the app starts
        validSubjectEntered = false;

        //Initialize haveIMUsBeenScanned to false when the app starts
        haveIMUsBeenScanned = false;

        //Initialize logPopUpWindowVisible and batteryPercentageVisible
        logPopUpWindowVisible = false;
        batteryPercentageVisible = false;
    }

    public abstract void updateIMUStatus(String nameOfIMU, String status);

    public abstract void updateIMUDataOutput(String nameOfIMU, String data);

    public abstract void updateGaitParameterOutput(String gaitParameter, String nameOfIMU, String data);

    public abstract void onScanComplete(boolean success);

    public abstract void onSyncComplete(boolean success);

    public abstract void onOffsetInitializationComplete(boolean success);

}
