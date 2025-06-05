package com.umainebiomechanicslab.biomechanicslabapp.userinterfaces;

import android.app.Activity;
import android.widget.Button;

import com.umainebiomechanicslab.biomechanicslabapp.R;

public class ExperimenterMenuUI extends UserInterface {

    public ExperimenterMenuUI(Activity activity, int pageID, IMURecordingPageUI imuRecordingPageUI, TestHapticCellsUI testHapticCellsUI,
                              OriginalThighExtensionStudyUI originalThighExtensionStudyUI,
                              OptimizedThighExtensionStudyUI optimizedThighExtensionStudyUI) {

        super(activity, pageID);

        //Declare UI Objects
        Button recordIMUDataButton = activity.findViewById(R.id.experimenter_menu_RecordIMUData);
        Button testHapticCellsButton = activity.findViewById(R.id.experimenter_menu_TestHapticCells);
        Button OriginalhighExtensionStudyButton = activity.findViewById(R.id.experimenter_menu_ThighExtensionStudy);
        Button OptimizedThighExtensionStudyButton = activity.findViewById(R.id.experimenter_menu_OptimizedThighExtensionStudy);
        Button walkingClassificationStudyButton = activity.findViewById(R.id.experimenter_menu_WalkingClassificationStudy);
        Button armCuingStudyButton = activity.findViewById(R.id.experimenter_menu_ArmCuingStudy);
        Button armExtensionStudyButton = activity.findViewById(R.id.experimenter_menu_ArmExtensionStudy);
        Button goBackButton = activity.findViewById(R.id.experimenter_menu_GoBack);

        //Set Record IMU Data Button Click Listener
        recordIMUDataButton.setOnClickListener(view -> imuRecordingPageUI.showPage());

        //Set Test Haptic Cells Button Click Listener
        testHapticCellsButton.setOnClickListener(view -> testHapticCellsUI.showPage());

        //Set Original Thigh Extension Study Button Click Listener
        OriginalhighExtensionStudyButton.setOnClickListener(view -> originalThighExtensionStudyUI.showPage());

        //Set Optimized Thigh Extension Study Button Click Listener
        OptimizedThighExtensionStudyButton.setOnClickListener(view -> optimizedThighExtensionStudyUI.showPage());

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
