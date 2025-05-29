package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;

public abstract class UserInterfaceWithIMU extends UserInterface{

    Button startScanButton, startSyncButton, disconnectButton, showBatteryPercentageButton, startInitializationButton,
            startTrialButton, goBackButton, uploadDataToCloudButton, showLogButton;

    public UserInterfaceWithIMU(Activity activity, int pageID) {
        super(activity, pageID);
    }

    public abstract void updateIMUStatus(String nameOfIMU, String status);

    public abstract void updateIMUDataOutput(String nameOfIMU, String data);

    public abstract void onScanComplete(boolean success);

    public abstract void onSyncComplete(boolean success);

    public abstract void onOffsetInitializationComplete(boolean success);

}
