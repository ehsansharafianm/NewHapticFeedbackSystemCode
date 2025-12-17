package com.umainebiomechanicslab.gait_training_app.studymanagers;

import static com.xsens.dot.android.sdk.models.DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1;

import android.content.Context;

import com.umainebiomechanicslab.gait_training_app.FileManager;
import com.umainebiomechanicslab.gait_training_app.imus.StreamingIMU;
import com.umainebiomechanicslab.gait_training_app.imus.UniversalIMU;
import com.umainebiomechanicslab.gait_training_app.userinterfaces.IMURecordingPageUI;

import java.util.Date;
import java.util.Locale;

public class RecordIMUDataManager extends IMUManager{

    public RecordIMUDataManager(IMURecordingPageUI imuRecordingPageUI, Context context, FileManager fileManager){

        super(context, imuRecordingPageUI, fileManager);

    }

    @Override
    public void updateIMUCode(String imuName, String imuCode){

        //If the imuCode is "--", delete the IMU with the given name
        if(imuCode.equals("--")){

            //Loop through all the IMUs in the IMU ArrayList and remove the one with the given name
            for(UniversalIMU IMU : IMUArrayList) {
                if (IMU.getNameOfIMU().equals(imuName)) {
                    IMUArrayList.remove(IMU);
                    return;
                }
            }

        }
        else{

            //If an IMU with the given name exists in the IMU ArrayList, update its code
            for(UniversalIMU IMU : IMUArrayList){
                if(IMU.getNameOfIMU().equals(imuName)){
                    setIMUMACAddress(imuName, imuCode);
                    return;
                }
            }

            //If an IMU with the given name doesn't exist in the IMU ArrayList, create a new IMU
            IMUArrayList.add(new StreamingIMU(imuName, context, this, userInterface, fileManager, PAYLOAD_TYPE_CUSTOM_MODE_1));
            setIMUMACAddress(imuName, imuCode);

        }

    }

    @Override
    public void startAngleOffsetInitialization() {

        //For all the streaming IMUs in the IMU ArrayList, start their angle offset initialization
        for(UniversalIMU IMU : IMUArrayList){
            if(IMU instanceof StreamingIMU){
                ((StreamingIMU) IMU).startOffsetInitialization();
            }
        }

    }

    @Override
    public void onAngleOffsetInitializationComplete() {

        //For all the streaming IMUs in the IMU ArrayList, check if their angle offset initialization is complete
        for(UniversalIMU IMU : IMUArrayList) {
            if (IMU instanceof StreamingIMU) {
                if (!((StreamingIMU) IMU).getOffsetAnglesInitialized()) {
                    return;
                }
            }
        }

        //If all streaming IMUs have their angle offset initialization complete, call onOffsetInitializationComplete
        userInterface.onOffsetInitializationComplete(true);

    }

    @Override
    public void startTrial(String trialName) {

        //Get the current date and time
        String currentTimeStamp = java.text.DateFormat.getDateTimeInstance().format(new Date());

        //Log the offset angles for all IMUs in the IMU ArrayList
        for(UniversalIMU IMU : IMUArrayList){
            if (IMU instanceof StreamingIMU) {
                fileManager.writeToLogFile(String.format(Locale.US, "Offset Angle for %s: %.3f", IMU.getNameOfIMU(), ((StreamingIMU) IMU).getOffsetEulerAngle()));
            }
        }

        //Start trial for all IMUs in the IMU ArrayList
        for(UniversalIMU IMU : IMUArrayList){
            IMU.startTrial(trialName, currentTimeStamp, null, 0, true);
        }

    }

    @Override
    public void stopTrial(String trialName) {

        //Stop trial for all IMUs in the IMU ArrayList
        for(UniversalIMU IMU : IMUArrayList){
            IMU.stopTrial(true);
        }

    }
}
