package com.umainebiomechanicslab.biomechanicslabapp;

import static com.xsens.dot.android.sdk.models.DotPayload.PAYLOAD_TYPE_CUSTOM_MODE_1;

import android.content.Context;

import java.util.Date;

public class RecordIMUDataManager extends IMUManager{

    public RecordIMUDataManager(IMURecordingPageUI imuRecordingPageUI, Context context, FileManager fileManager){

        super(context, imuRecordingPageUI, fileManager);

    }

    public void updateIMUCode(String imuName, String imuCode){

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

    public void deleteIMU(String imuName){

        //Loop through all the IMUs in the IMU ArrayList and remove the one with the given name
        for(UniversalIMU IMU : IMUArrayList) {
            if (IMU.getNameOfIMU().equals(imuName)) {
                IMUArrayList.remove(IMU);
                return;
            }
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

        //Start trial for all IMUs in the IMU ArrayList
        for(UniversalIMU IMU : IMUArrayList){
            IMU.startTrial(trialName, currentTimeStamp, 0, true);
        }

    }

    @Override
    public void stopTrial() {

        //Stop trial for all IMUs in the IMU ArrayList
        for(UniversalIMU IMU : IMUArrayList){
            IMU.stopTrial(true);
        }

    }
}
