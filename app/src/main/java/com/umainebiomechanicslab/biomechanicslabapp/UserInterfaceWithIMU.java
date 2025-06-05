package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;

public abstract class UserInterfaceWithIMU extends UserInterface{

    protected Button startScanButton, startSyncButton, disconnectButton, showBatteryPercentageButton, startInitializationButton,
            startTrialButton, goBackButton, uploadDataToCloudButton, showLogButton;

    protected boolean validSubjectEntered;

    public UserInterfaceWithIMU(Activity activity, int pageID) {
        super(activity, pageID);

        //Initialize validSubjectEntered to false when the app starts
        validSubjectEntered = false;
    }

    public abstract void updateIMUStatus(String nameOfIMU, String status);

    public abstract void updateIMUDataOutput(String nameOfIMU, String data);

    public abstract void onScanComplete(boolean success);

    public abstract void onSyncComplete(boolean success);

    public abstract void onOffsetInitializationComplete(boolean success);

}
