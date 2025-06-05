package com.umainebiomechanicslab.biomechanicslabapp.userinterfaces;

import android.app.Activity;
import android.widget.Button;

public abstract class UserInterfaceWithRecordingIMU extends UserInterfaceWithIMU {

    protected Button exportRecordedDataButton;

    public UserInterfaceWithRecordingIMU(Activity activity, int pageID) {
        super(activity, pageID);

    }

    public abstract void onRecordingExportComplete();
}
