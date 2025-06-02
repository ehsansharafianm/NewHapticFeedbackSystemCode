package com.umainebiomechanicslab.biomechanicslabapp;

import android.app.Activity;
import android.widget.Button;

public class ExperimenterMenuUI extends UserInterface{

    public ExperimenterMenuUI(Activity activity, int pageID, IMURecordingPageUI imuRecordingPageUI, TestHapticCellsUI testHapticCellsUI, OptimizedThighExtensionStudyUI thighExtensionStudyUI) {

        super(activity, pageID);

        //Declare UI Objects
        Button recordIMUDataButton = activity.findViewById(R.id.experimenter_menu_RecordIMUData);
        Button testHapticCellsButton = activity.findViewById(R.id.experimenter_menu_TestHapticCells);
        Button thighExtensionStudyButton = activity.findViewById(R.id.experimenter_menu_ThighExtensionStudy);
        Button walkingClassificationStudyButton = activity.findViewById(R.id.experimenter_menu_WalkingClassificationStudy);
        Button armCuingStudyButton = activity.findViewById(R.id.experimenter_menu_ArmCuingStudy);
        Button armExtensionStudyButton = activity.findViewById(R.id.experimenter_menu_ArmExtensionStudy);
        Button goBackButton = activity.findViewById(R.id.experimenter_menu_GoBack);

        //Set Record IMU Data Button Click Listener
        recordIMUDataButton.setOnClickListener(view -> imuRecordingPageUI.showPage());

        //Set Test Haptic Cells Button Click Listener
        testHapticCellsButton.setOnClickListener(view -> testHapticCellsUI.showPage());

        //Set Thigh Extension Study Button Click Listener
        thighExtensionStudyButton.setOnClickListener(view -> thighExtensionStudyUI.showPage());

        //Set Walking Classification Study Button Click Listener
        walkingClassificationStudyButton.setOnClickListener(view -> textPopUp("Walking Classification Study Button Clicked"));

        //Set Arm Cuing Study Button Click Listener
        armCuingStudyButton.setOnClickListener(view -> textPopUp("Arm Cuing Study Button Clicked"));

        //Set Arm Extension Study Button Click Listener
        armExtensionStudyButton.setOnClickListener(view -> textPopUp("Arm Extension Study Button Clicked"));

        //Set Go Back Button Click Listener
        goBackButton.setOnClickListener(view -> userInterfaceForBackButton.showPage());

    }
}
